package com.soprahr.foryou.hub.tools.mavenplugin.infrastructure

import com.soprahr.foryou.hub.tools.mavenplugin.service.FeatureService
import com.soprahr.foryou.hub.tools.mavenplugin.service.impl.FeatureServiceImpl
import org.apache.maven.artifact.resolver.filter.ArtifactFilter
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter
import org.apache.maven.execution.MavenSession
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.*
import org.apache.maven.plugins.dependency.tree.DOTDependencyNodeVisitor
import org.apache.maven.plugins.dependency.tree.GraphmlDependencyNodeVisitor
import org.apache.maven.plugins.dependency.tree.TGFDependencyNodeVisitor
import org.apache.maven.project.DefaultProjectBuildingRequest
import org.apache.maven.project.MavenProject
import org.apache.maven.project.ProjectBuildingRequest
import org.apache.maven.shared.artifact.filter.StrictPatternExcludesArtifactFilter
import org.apache.maven.shared.artifact.filter.StrictPatternIncludesArtifactFilter
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException
import org.apache.maven.shared.dependency.graph.DependencyNode
import org.apache.maven.shared.dependency.graph.filter.AncestorOrSelfDependencyNodeFilter
import org.apache.maven.shared.dependency.graph.filter.AndDependencyNodeFilter
import org.apache.maven.shared.dependency.graph.filter.ArtifactDependencyNodeFilter
import org.apache.maven.shared.dependency.graph.filter.DependencyNodeFilter
import org.apache.maven.shared.dependency.graph.traversal.*
import org.apache.maven.shared.dependency.graph.traversal.SerializingDependencyNodeVisitor.GraphTokens
import java.io.IOException
import java.io.StringWriter
import java.io.Writer
import java.util.*


@Mojo(name = "feature-dynamic-resolver",
        defaultPhase = LifecyclePhase.CLEAN,
        requiresDependencyCollection = ResolutionScope.TEST,
        threadSafe = true)
class FeatureResolverMojo() : AbstractMojo() {
    @Parameter(defaultValue = "\${project}", readonly = true, required = true)
    val project: MavenProject? = null
    @Parameter(defaultValue = "\${session}", readonly = true, required = true)
    private val session: MavenSession? = null
    @Parameter(defaultValue = "\${reactorProjects}", readonly = true, required = true)
    private val reactorProjects: List<MavenProject>? = null
    @Component(hint = "default")
    private val dependencyGraphBuilder: DependencyGraphBuilder? = null
    @Parameter(property = "outputType", defaultValue = "text")
    private val outputType: String? = null
    @Parameter(property = "scope")
    private val scope: String? = null
    @Parameter(property = "tokens", defaultValue = "standard")
    private val tokens: String? = null
    @Parameter(property = "includes")
    private val includes: String? = null
    @Parameter(property = "excludes")
    private val excludes: String? = null
    private var rootNode: DependencyNode? = null

    @Throws(MojoExecutionException::class, MojoFailureException::class)
    override fun execute() {
        try {
            val (dependencyTreeString: String, featureService: FeatureService) = adaptCfg()
            featureService.resolveCfg(dependencyTreeString)
        } catch (exception: DependencyGraphBuilderException) {
            throw MojoExecutionException("Cannot build project dependency graph", exception)
        } catch (exception: IOException) {
            throw MojoExecutionException("Cannot serialise project dependency graph", exception)
        }
    }

    private fun adaptCfg(): Pair<String, FeatureService> {
        val artifactFilter = createResolvingArtifactFilter()
        val buildingRequest: ProjectBuildingRequest = DefaultProjectBuildingRequest(session!!.projectBuildingRequest)
        buildingRequest.project = project
        rootNode = dependencyGraphBuilder!!.buildDependencyGraph(buildingRequest, artifactFilter, reactorProjects)
        val dependencyTreeString: String = serializeDependencyTree(rootNode)
        val featureService: FeatureService = FeatureServiceImpl(log, project!!, arrayListOf(), scope)
        return Pair(dependencyTreeString, featureService)
    }


    private fun createResolvingArtifactFilter(): ArtifactFilter? {
        val filter: ArtifactFilter?
        if (scope != null) {
            log.debug("+ Resolving dependency tree for scope '$scope'")
            filter = ScopeArtifactFilter(scope)
        } else {
            filter = null
        }
        return filter
    }

    private fun serializeDependencyTree(rootNode: DependencyNode?): String {
        val writer = StringWriter()
        var visitor: DependencyNodeVisitor? = getSerializingDependencyNodeVisitor(writer)
        visitor = BuildingDependencyNodeVisitor(visitor)
        val filter: DependencyNodeFilter? = createDependencyNodeFilter()
        if (filter != null) {
            val collectingVisitor = CollectingDependencyNodeVisitor()
            val firstPassVisitor: DependencyNodeVisitor = FilteringDependencyNodeVisitor(collectingVisitor, filter)
            rootNode!!.accept(firstPassVisitor)
            val secondPassFilter: DependencyNodeFilter = AncestorOrSelfDependencyNodeFilter(collectingVisitor.getNodes())
            visitor = FilteringDependencyNodeVisitor(visitor, secondPassFilter)
        }
        rootNode!!.accept(visitor)
        return writer.toString()
    }

    fun getSerializingDependencyNodeVisitor(writer: Writer?): DependencyNodeVisitor {
        return if ("graphml" == outputType) {
            GraphmlDependencyNodeVisitor(writer)
        } else if ("tgf" == outputType) {
            TGFDependencyNodeVisitor(writer)
        } else if ("dot" == outputType) {
            DOTDependencyNodeVisitor(writer)
        } else {
            SerializingDependencyNodeVisitor(writer, toGraphTokens(tokens))
        }
    }

    private fun toGraphTokens(tokens: String?): GraphTokens {
        return when (tokens) {
            "whitespace" -> {
                log.debug("+ Using whitespace tree tokens")
                SerializingDependencyNodeVisitor.WHITESPACE_TOKENS
            }
            "extended" -> {
                log.debug("+ Using extended tree tokens")
                SerializingDependencyNodeVisitor.EXTENDED_TOKENS
            }
            else -> {
                SerializingDependencyNodeVisitor.STANDARD_TOKENS
            }
        }
    }

    private fun createDependencyNodeFilter(): DependencyNodeFilter? {
        val filters: MutableList<DependencyNodeFilter> = ArrayList<DependencyNodeFilter>()
        if (includes != null) {
            val patterns = Arrays.asList(*includes.split(",".toRegex()).toTypedArray())
            log.debug("+ Filtering dependency tree by artifact include patterns: $patterns")
            val artifactFilter: ArtifactFilter = StrictPatternIncludesArtifactFilter(patterns)
            filters.add(ArtifactDependencyNodeFilter(artifactFilter))
        }
        if (excludes != null) {
            val patterns = Arrays.asList(*excludes.split(",".toRegex()).toTypedArray())
            log.debug("+ Filtering dependency tree by artifact exclude patterns: $patterns")
            val artifactFilter: ArtifactFilter = StrictPatternExcludesArtifactFilter(patterns)
            filters.add(ArtifactDependencyNodeFilter(artifactFilter))
        }
        return if (filters.isEmpty()) null else AndDependencyNodeFilter(filters)
    }
}

