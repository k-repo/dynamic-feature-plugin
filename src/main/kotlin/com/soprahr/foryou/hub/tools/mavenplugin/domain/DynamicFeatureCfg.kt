package com.soprahr.foryou.hub.tools.mavenplugin.domain

import org.apache.maven.model.Dependency
import java.util.ArrayList

class DynamicFeatureCfg {
    val cfg: String = "src/main/resources/dynamic-feature.cfg"
    val delimiters: String = "com.soprahr."
    var customComponentCounter: ArrayList<String> = arrayListOf()
    var installCounter: ArrayList<String> = arrayListOf()
    var disabledCounter: ArrayList<String> = arrayListOf()
    var replaceCounter: ArrayList<String> = arrayListOf()

    fun customComponentsListing(): String {
        var list: String = ""
        customComponentCounter.map {
            list += "\t $it \n"
        }
        return list
    }

    fun installListing(): String {
        var list: String = ""
        installCounter.map {
            list += "\t $it \n"
        }
        return list
    }

    fun disableListing(): String {
        var list: String = ""
        disabledCounter.map {
            list += "\t $it \n"
        }
        return list
    }

    fun replaceListing(): String {
        var list: String = ""
        replaceCounter.map {
            list += "\t $it \n"
        }
        return list
    }

    fun customReplaceJob(dependency: Dependency?, oldGroupId: String) {
        customInstallJob(dependency)
        addDisabled(oldGroupId)
        addReplace(dependency)
    }

    fun customInstallJob(dependency: Dependency?) {
        addComponent(dependency)
        addInstall(dependency)
    }


    fun addComponent(dependency: Dependency?) {
        if (customComponentCounter.none{extractDep(dependency) == it}) {
            customComponentCounter.add(extractDep(dependency))
        }
    }


    fun addDisabled(dependency: String?) {
        val extracted = "${dependency.toString()}/${dependency.toString()}"
        if (disabledCounter.none{extracted == it}) {
            disabledCounter.add(extracted)
        }
    }

    fun addInstall(dependency: Dependency?) {
        if (installCounter.none{extractDep(dependency) == it}) {
            installCounter.add(extractDep(dependency))
        }
    }

    fun addReplace(dependency: Dependency?) {
        if (replaceCounter.none{extractDep(dependency) == it}) {
            replaceCounter.add(extractDep(dependency))
        }
    }

    fun extractDep(dependency: Dependency?): String {
        return "${dependency?.groupId.toString()}/${dependency?.artifactId.toString()}/${dependency?.version.toString()}"
    }

}