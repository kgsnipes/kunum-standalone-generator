package org.kunum.services


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.kunum.api.GeneratorNodeWebAPI
import org.kunum.core.TokenBucket
import org.kunum.data.Bucket
import org.kunum.data.BucketToken
import org.kunum.util.*
import org.slf4j.LoggerFactory
import java.sql.PreparedStatement
import java.util.*
import kotlin.coroutines.EmptyCoroutineContext

class GeneratorNode(val config:Properties):Generator {

    private var JDBC_URL=config.getString("kunum.generator.jdbcURL")
    private val log= LoggerFactory.getLogger("GeneratorNode")

    val BUCKET_LIMIT=config.getInt("kunum.generator.bucket.limit")
    var serverPort=config.getInt("kunum.generator.server.port")
    var localStorage:DatabaseConnection?=null

    var api:GeneratorNodeWebAPI?=null

    val VALID_TOKEN="Bearer ${config.getString("kunum.dummy.oauth.token")}"
    private val sqlUtil:SQLUtil= SQLUtil()

    val bucketMap= mutableMapOf<String,TokenBucket>()

    val nodeValue=config.getString("kunum.generator.node.name")
    val apiMode=config.getBoolean("kunum.generator.api.mode")


    init {
            log.info("Starting up Generator Node")
            log.info("Creating Local Storage connection")
            localStorage=DatabaseConnection(JDBC_URL,config.getString("kunum.generator.jdbc.user"),config.getString("kunum.generator.jdbc.password"))
            log.info("Creating Local Storage tables")
            sqlUtil.createSequenceTable(localStorage!!)
            sqlUtil.createSequenceEntryTable(localStorage!!)
            log.info("Starting up Web API for Generator Node")
           if(apiMode) {
               api = GeneratorNodeWebAPI(this)
           }

    }

    fun getAPIToken():String=VALID_TOKEN


    private fun getBucketFromDB(bucketName:String): Bucket? {

        localStorage?.getConnection().use {
            val pstat=it?.prepareStatement("SELECT * FROM sequence WHERE name=? AND deleted=0")
            pstat?.setString(1,bucketName)
            val rs=pstat?.executeQuery()
            if(rs?.next()==true)
            {
                return sqlUtil.resultSetToBucket(rs,BUCKET_LIMIT)
            }
            else
            {
                return null
            }
        }

    }


    private fun isSequenceAvailable(bucketName:String):Boolean{
            localStorage?.getConnection().use {
            val pstat=it?.prepareStatement("SELECT * FROM sequence WHERE name=? AND deleted=0")
            pstat?.setString(1,bucketName)
            return pstat?.executeQuery()?.next()?:false
        }
    }

    override fun createBucket(bucketName:String, startValue:Long):Bucket?
    {
        if(isSequenceAvailable(bucketName))
        {
            throw java.lang.RuntimeException("Sequence already available")
        }
        if(bucketMap.size>=BUCKET_LIMIT)
        {
            throw RuntimeException("Cannot fit more buckets")
        }
        localStorage?.getConnection().use {
            val pstatement: PreparedStatement? =it?.prepareStatement("""
                INSERT INTO sequence(name,startvalue,mostrecent,paused,deleted,created,updated,node) VALUES(?,?,?,?,?,?,?,?)
            """.trimIndent())
            val now=System.currentTimeMillis()
            pstatement?.setString(1,bucketName)
            pstatement?.setString(2,startValue.toString())
            pstatement?.setString(3,"0")
            pstatement?.setInt(4,0)
            pstatement?.setInt(5,0)
            pstatement?.setString(6,now.toString())
            pstatement?.setString(7,now.toString())
            pstatement?.setString(8,this.nodeValue)

            val rows=pstatement?.executeUpdate()
            if (rows != null && rows>0) {
                return getBucketFromDB(bucketName)
            }
            else
            {
                throw java.lang.RuntimeException("Cannot create a bucket")
            }
        }
        return null
    }


    override fun getTokenFromBucket(bucketName:String):String{
        val token=getBucket(bucketName)?.getToken()
        token?.let{
            CoroutineScope(EmptyCoroutineContext).launch{
                val time=System.currentTimeMillis()
                addEntryToLocalStorage(BucketToken(token.toLong(),bucketName,time,nodeValue))
                updateEntryToSequenceStorage(BucketToken(token.toLong(),bucketName,time,nodeValue))
            }
        }?:throw java.lang.RuntimeException("Bucket not available!!")
        return token!!
    }

    private fun getBucket(bucketName:String):TokenBucket?{
        var tokenBucket:TokenBucket?=bucketMap.get(bucketName)
        if(tokenBucket==null)
        {
            val bucket=getBucketFromDB(bucketName)

            bucket?.let {
                if(bucketMap.size<=BUCKET_LIMIT) {
                    bucketMap.put(it.name, it.sequencer!!)
                    tokenBucket = it.sequencer
                }
                else
                    throw java.lang.RuntimeException("Cannot add bucket, out of capacity")
            }
        }
        return tokenBucket
    }

    private fun updateEntryToSequenceStorage(bucketToken:BucketToken):Unit{
        localStorage?.getConnection().use {
            val pstatement: PreparedStatement? = it?.prepareStatement(
                """
                UPDATE sequence SET mostrecent=? WHERE name=?
            """.trimIndent()
            )
            pstatement?.setString(1,bucketToken.id.toString())
            pstatement?.setString(2,bucketToken.bucket)

            val rows=pstatement?.executeUpdate()
            if(rows != null && rows > 0) log.info("Update done")
        }
    }

    private fun resetSequenceInStorage(bucketName: String,value:Long):Unit{
        localStorage?.getConnection().use {
            val pstatement: PreparedStatement? = it?.prepareStatement(
                """
                UPDATE sequence SET startvalue=?, mostrecent=? WHERE name=?
            """.trimIndent()
            )
            pstatement?.setLong(1,value)
            pstatement?.setLong(2,value)
            pstatement?.setString(3,bucketName)

            val rows=pstatement?.executeUpdate()
            if(rows != null && rows > 0) log.info("Update done")
        }
    }

    private fun deleteSequenceInStorage(bucketName: String):Unit{
        localStorage?.getConnection().use {
            val pstatement: PreparedStatement? = it?.prepareStatement(
                """
                UPDATE sequence SET deleted=? WHERE name=?
            """.trimIndent()
            )
            pstatement?.setInt(1,1)
            pstatement?.setString(2,bucketName)

            val rows=pstatement?.executeUpdate()
            if(rows != null && rows > 0) log.info("Update done")
        }
    }

    private fun addEntryToLocalStorage(bucketToken: BucketToken):Unit{
        localStorage?.getConnection().use {
            val pstatement: PreparedStatement? = it?.prepareStatement(
                """
                INSERT INTO sequenceentries(token,name,created,node) VALUES(?,?,?,?)
            """.trimIndent()
            )
            pstatement?.setString(1,bucketToken.id.toString())
            pstatement?.setString(2,bucketToken.bucket)
            pstatement?.setString(3,System.currentTimeMillis().toString())
            pstatement?.setString(4,this.nodeValue)

            val rows=pstatement?.executeUpdate()
            if(rows != null && rows > 0) log.info("Update done")
        }
    }

    override fun resetBucket(name:String, value:Long){
        var bucket=getBucket(name)
        bucket?.let {
            resetSequenceInStorage(it.name,value)
            bucketMap.remove(it.name)
            bucketMap.put(it.name, getBucket(it.name)!!)
        }
    }

    override fun deleteBucket(name:String){
        var bucket=getBucket(name)
        bucket?.let {
            deleteSequenceInStorage(it.name)
            bucketMap.remove(it.name)
        }
    }


    override fun stop():Unit{
        log.info("Stopping the Generator node : ${this.nodeValue}")
        this.localStorage?.getConnection()?.close()
        api?.stop()
    }

}