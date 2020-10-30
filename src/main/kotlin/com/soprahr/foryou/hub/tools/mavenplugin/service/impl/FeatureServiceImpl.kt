package com.soprahr.foryou.hub.tools.mavenplugin.service.impl

import com.soprahr.foryou.hub.tools.mavenplugin.domain.DynamicFeatureCfg
import com.soprahr.foryou.hub.tools.mavenplugin.service.FeatureService
import org.apache.maven.model.Dependency
import org.apache.maven.plugin.logging.Log
import org.apache.maven.project.MavenProject
import java.io.File
import java.util.*

class FeatureServiceImpl(
        val log: Log,
        val project: MavenProject,
        val dependencies: MutableList<Dependency?>?,
        val scope: String?,
        val dynamicFeatureCfg: DynamicFeatureCfg = DynamicFeatureCfg(),
        val utilsService: UtilsServiceImpl = UtilsServiceImpl()
) : FeatureService
{

    override fun execute() {
        val file = generateCfg()
        fetchDependencies(file)
        recap()
    }


    private fun fetchDependencies(file: File) {
        dependencies?.map {
            val customPrefix = it?.artifactId!!.split(dynamicFeatureCfg.delimiters)[0]
            replaceJob(it, customPrefix, file)
            installJob(it, customPrefix, file)
        }
    }

    private fun generateCfg(): File {
        val file = File(dynamicFeatureCfg.cfg)
        file.writeText("")
        return file
    }

    private fun replaceJob(dependency: Dependency?, customPrefix: String, file: File) {
        if (dependency?.groupId!!.contains(dynamicFeatureCfg.delimiters) && customPrefix.isNotEmpty()) {
            val oldGroupId = dependency?.groupId?.replace(customPrefix, "")
            val oldArtifactId = dependency?.artifactId?.replace(customPrefix, "")
            val oldVersion = "#ToBeReplacedInMavenPluginForyouAssembly#"
            dynamicFeatureCfg.customReplaceJob(dependency, oldGroupId!!)
            file.appendText("replace_transaction_${dynamicFeatureCfg.replaceCounter.size} = " +
                    "[$oldGroupId/$oldArtifactId/$oldVersion]" +
                    "[${dependency?.groupId}/${dependency?.artifactId}/${dependency?.version}]" +
                    "\n")
        }
    }


    private fun installJob(dependency: Dependency?, customPrefix: String, file: File) {
        if (dependency?.groupId!!.contains(dynamicFeatureCfg.delimiters) && customPrefix.isNotEmpty()) {
            dynamicFeatureCfg.customInstallJob(dependency)
            file.appendText("install_transaction_${dynamicFeatureCfg.installCounter.size} = " +
                    "[${dependency?.groupId}/${dependency?.artifactId}/${dependency?.version}]" +
                    "\n")
        }
    }

    override fun resolveCfg(dependencyTreeString: String) {
        val adapterCfg = utilsService.readFile(dynamicFeatureCfg.cfg)
        val installTransactions: ArrayList<String> = arrayListOf()
        val replaceTransactions: ArrayList<String> = arrayListOf()
        adapterCfg.forEach {
            if (it.contains("replace_transaction")) {
                replaceTransactions.add(it)
            } else if (it.contains("install_transaction")) {
                installTransactions.add(it)
            }
        }
        val transactionInstallToClean = arrayListOf<Int>()
        replaceTransactions.map { replace ->
            installTransactions.map { install ->
                if (replace.contains(install.split(" = ")[1])) {
                    transactionInstallToClean.add(installTransactions.indexOf(install))
                }
            }
        }
        transactionInstallToClean.map { installTransactions.removeAt(it) }
        val finalReplaceTransactions = arrayListOf<String>()
        replaceTransactions.map {
            val oldArtifact = it.split(" = ")[1].split("][")[0].replace("[", "")
            val newArtifact = it.split("][")[1].replace("]", "")
            dependencyTreeString.lines().map { line ->
                if (line.contains(oldArtifact.split("/")[0])) {
                    val version = line.split(":")[3]
                    finalReplaceTransactions.add(it.split(" = ")[0] + " = " +
                            "[" + oldArtifact.split("/")[0] +
                            "/" + oldArtifact.split("/")[0] +
                            "/" + version + "][" + newArtifact + "]")
                    dynamicFeatureCfg.customComponentCounter.add(newArtifact.split("/")[0])
                }
            }
        }
        val file = File(dynamicFeatureCfg.cfg)
        file.writeText("")
        installTransactions.map {
            file.appendText(it + "\n")
            dynamicFeatureCfg.installCounter.add(it.split(" = ")[1].split("/")[1])
        }
        finalReplaceTransactions.map {
            file.appendText(it + "\n")
            dynamicFeatureCfg.replaceCounter.add(it.split(" = ")[1].split("/")[1])
            dynamicFeatureCfg.disabledCounter.add(it.split(" = ")[1].split("/")[1])
        }
        recap()
    }


    private fun recap() {
        log.info(RecapServiceImpl(dynamicFeatureCfg).execute())
    }
}