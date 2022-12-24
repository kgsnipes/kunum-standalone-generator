package org.kunum.services

import org.kunum.data.Bucket

interface Generator {
    fun createBucket( bucketName:String, startValue:Long): Bucket?
    fun getTokenFromBucket(bucketName:String):String
    fun resetBucket(name:String,value:Long)
    fun deleteBucket(name:String)
    fun stop()
}