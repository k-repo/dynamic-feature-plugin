package com.soprahr.foryou.hub.tools.mavenplugin.service.impl

import com.soprahr.foryou.hub.tools.mavenplugin.domain.DynamicFeatureCfg
import com.soprahr.foryou.hub.tools.mavenplugin.service.RecapService

class RecapServiceImpl (
        private val dynamicFeatureCfg: DynamicFeatureCfg,
        var summary: String = ""
) : RecapService {

    override fun execute(): String {
        summary = "" +
                "#\n" +
                "###################################################################\n" +
                "#\n" +
                "#                                    FEATURE DYNAMIQUE\n" +
                "#\n" +
                "#        ${dynamicFeatureCfg.customComponentCounter.size} CUSTOMIZED COMPONENTS ATTACHED\n" +
                "#        ${dynamicFeatureCfg.customComponentsListing()}\n" +
                "#        ${dynamicFeatureCfg.installCounter.size} NEW COMPONENTS ATTACHED\n" +
                "#        ${dynamicFeatureCfg.installListing()}\n" +
                "#        ${dynamicFeatureCfg.disabledCounter.size} STANDARD COMPONENT WERE DEACTIVATED\n" +
                "#        ${dynamicFeatureCfg.disableListing()}\n" +
                "#        X CUSTOMIZED COMPONENTS ARE BASED ON A NON-COMPATIBLE STANDARD VERSION.\n" +
                "#        X CUSTOMIZED COMPONENTS  NEEDS TO BE UPGRADED\n" +
                "#        X CUSTOMIZED COMPONENTS DIDN’T HAVE A MATCHING STANDARD COMPONENT IN THE ASSEMBLY\n" +
                "#\n" +
                "#\n" +
                "##################################################################\n"+
                ""
        return summary
    }
}