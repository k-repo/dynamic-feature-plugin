package com.soprahr.foryou.hub.tools.mavenplugin.service.impl

import com.soprahr.foryou.hub.tools.mavenplugin.service.UtilsService
import java.io.File

class UtilsServiceImpl : UtilsService {


    override fun readFile(fileName: String): List<String>
            = File(fileName).readLines()


}