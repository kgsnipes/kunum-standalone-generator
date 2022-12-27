package org.kunum.util

import java.io.File
import java.io.FileInputStream
import java.nio.charset.StandardCharsets
import java.util.*

class CommonUtil {
    companion object{
        @JvmStatic
        fun readFileToProps(filePath:String): Properties {
            val file = File(filePath)
            val prop = Properties()
            FileInputStream(file).use {
                prop.load(it)
            }
            return prop
        }

        @JvmStatic
        fun readStringToProps(content:String): Properties {

            val prop = Properties()
            content.byteInputStream().use {
                prop.load(it)
            }
            return prop
        }
    }

}