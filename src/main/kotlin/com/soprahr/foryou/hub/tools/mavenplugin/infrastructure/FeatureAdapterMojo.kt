package com.soprahr.foryou.hub.tools.mavenplugin.infrastructure

import com.soprahr.foryou.hub.tools.mavenplugin.service.FeatureService
import com.soprahr.foryou.hub.tools.mavenplugin.service.impl.FeatureServiceImpl
import org.apache.maven.model.Dependency
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.LifecyclePhase
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject


@Mojo(name = "feature-dynamic-adapter", defaultPhase = LifecyclePhase.CLEAN)
class FeatureAdapterMojo(
        @Parameter(property = "scope")
        var scope: String? = null,
        @Parameter(defaultValue = "\${project}", required = true, readonly = true)
        var project: MavenProject? = null
) : AbstractMojo() {

    @Throws(MojoExecutionException::class, MojoFailureException::class)
    override fun execute() {
        val dependencies: MutableList<Dependency?>? = project!!.dependencies as MutableList<Dependency?>?
        val featureService :  FeatureService = FeatureServiceImpl(log, project!!, dependencies, scope)
        featureService.execute()
    }


}