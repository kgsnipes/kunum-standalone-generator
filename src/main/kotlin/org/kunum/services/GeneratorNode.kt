package org.kunum.services


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.kunum.api.GeneratorNodeWebAPI
import org.kunum.core.LongSequence
import org.kunum.core.TokenBucket
import org.kunum.data.BucketDTO
import org.kunum.data.BucketToken
import org.kunum.util.DatabaseConnection
import org.kunum.util.SQLUtil
import org.kunum.util.getInt
import org.kunum.util.getString
import org.slf4j.LoggerFactory
import java.io.IOException
import java.security.SecureRandom
import java.sql.PreparedStatement
import java.util.*
import kotlin.coroutines.EmptyCoroutineContext

class GeneratorNode(val config:Properties) {

    private var JDBC_URL=config.getString("kunum.generator.jdbcURL")
    private val log= LoggerFactory.getLogger("GeneratorNode")

    val BUCKET_LIMIT=config.getInt("kunum.generator.bucket.limit")
    var serverPort=config.getInt("kunum.generator.server.port")
    var localStorage:DatabaseConnection?=null

    var api:GeneratorNodeWebAPI?=null

    val VALID_TOKEN="Bearer ${config.getString("kunum.dummy.oauth.token")}"
    private val client = OkHttpClient()
    private val sqlUtil:SQLUtil= SQLUtil()

    val bucketMap= mutableMapOf<String,TokenBucket>()

    val nodeValue=config.getString("kunum.generator.node.name")


    init {
            log.info("Starting up Generator Node")
            log.info("Creating Local Storage connection")
            localStorage=DatabaseConnection(JDBC_URL)
            log.info("Creating Local Storage tables")
            sqlUtil.createLocalDBTable(localStorage!!)
            log.info("Starting up Web API for Generator Node")
            api=GeneratorNodeWebAPI(this)

    }

    fun getAPIToken():String=VALID_TOKEN


    fun getTokenFromBucket(bucketName:String):String{

        if(bucketMap.get(bucketName)==null)
        {
            val bucket=fetchBucketInformationFromMonitorNode(bucketName)
            bucket?.let {
                bucketMap.put(it.name, TokenBucket(it.name, LongSequence(it.mostRecentValue?:it.startingValue),BUCKET_LIMIT))
            }

        }

        val token=bucketMap.get(bucketName)?.getToken()
        token?.let{
            CoroutineScope(EmptyCoroutineContext).launch{
                val time=System.currentTimeMillis()
                addEntryToLocalStorage(BucketToken(token.toLong(),bucketName,time,nodeValue))
                updateEntryToSequenceStorage(BucketToken(token.toLong(),bucketName,time,nodeValue))
            }
        }
        return token?:throw java.lang.RuntimeException("No Token available")
    }

    fun addEntryToLocalStorage(bucketToken: BucketToken):Unit{
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

    fun stop():Unit{
        log.info("Stopping the Generator node : ${this.nodeValue}")
        this.localStorage?.getConnection()?.close()
        api?.stop()
    }

}