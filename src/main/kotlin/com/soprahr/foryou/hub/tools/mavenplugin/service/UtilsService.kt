package com.soprahr.foryou.hub.tools.mavenplugin.service

interface UtilsService {
    fun readFile(fileName: String): List<String>
}