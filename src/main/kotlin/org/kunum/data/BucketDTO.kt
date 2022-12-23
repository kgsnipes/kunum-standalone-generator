package org.kunum.data

@kotlinx.serialization.Serializable
data class BucketDTO(val name:String, val startingValue:Long, val mostRecentValue:Long,val paused:Boolean, val deleted:Boolean,val created:Long)