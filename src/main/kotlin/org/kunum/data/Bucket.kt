package org.kunum.data

import org.kunum.core.TokenBucket


data class Bucket(val id:Long, val name:String, val startValue:Long, val dateCreated:Long, val dateModified:Long, val deleted: Boolean, val paused: Boolean, val node: String, var sequencer: TokenBucket?, val mostRecent:Long)

