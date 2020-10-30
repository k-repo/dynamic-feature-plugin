package com.soprahr.foryou.hub.tools.mavenplugin.service

interface FeatureService {
    fun execute()
    fun resolveCfg(dependencyTreeString: String)
}