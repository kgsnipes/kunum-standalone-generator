package org.kunum.util

import io.javalin.http.Context
import java.util.*

class AuthUtil(val config:Properties) {

    fun isValidAPIToken(ctx: Context):Boolean{ return ctx.header("Authorization")=="Bearer ${config.getString("kunum.dummy.oauth.token")}"}

    fun isValidUser(userName:String,password:String):Boolean{return userName==config.getString("kunum.monitor.admin.username") && password==config.getString("kunum.monitor.admin.pwd")}


}