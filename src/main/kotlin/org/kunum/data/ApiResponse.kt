package org.kunum.data

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse (val result:String,val messages:List<String>?,val errors:List<String>?,val status:String)