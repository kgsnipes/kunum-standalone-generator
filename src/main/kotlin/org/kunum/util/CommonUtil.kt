package org.kunum.util

import java.io.File
import java.io.FileInputStream
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
    }

}