package org.kunum.util

import java.util.*

fun Properties.getBoolean(key:String):Boolean {
    return key?.let { this[it] as String =="true" }?:false
}

fun Properties.getInt(key:String):Int{
    return key?.let { (this[it] as String).toInt() }?:0
}

fun Properties.getString(key:String):String=this[key] as String