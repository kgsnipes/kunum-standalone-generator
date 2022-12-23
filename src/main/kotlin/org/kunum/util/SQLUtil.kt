package org.kunum.util

import org.kunum.core.LongSequence
import org.kunum.core.TokenBucket
import org.kunum.data.Bucket
import java.sql.ResultSet

class SQLUtil {

    fun createLocalDBTable(storage: DatabaseConnection)
    {
        val entriesTableSQL="""
            CREATE TABLE IF NOT EXISTS sequenceentries(
            token TEXT PRIMARY KEY, 
            name TEXT,
            created INTEGER,
            node TEXT
            )
        """.trimIndent()

        createTableInDB(entriesTableSQL,storage)
    }

    fun createTableInDB(sql:String,dbConnection: DatabaseConnection?):Unit{
        dbConnection?.getConnection().use {
            it?.createStatement()?.execute(sql)
        }
    }

    fun createSequenceDBTables(storage: DatabaseConnection)
    {
        val sequenceTableSQL="""
            CREATE TABLE IF NOT EXISTS sequence(
            id INTEGER PRIMARY KEY AUTOINCREMENT, 
            name TEXT,
            startvalue TEXT,
            mostrecent TEXT,
            paused INTEGER,
            deleted INTEGER,
            created INTEGER,
            updated INTEGER,
            node TEXT
            )
        """.trimIndent()
        createTableInDB(sequenceTableSQL,storage)
    }

    fun createNodeHealthDBTable(sequenceStorage: DatabaseConnection)
    {
        val nodeHealthTableSQL="""
            CREATE TABLE IF NOT EXISTS nodehealth(
            name TEXT PRIMARY KEY, 
            lastping INTEGER,
            bucketsize INTEGER,
            status TEXT
            )
        """.trimIndent()

        createTableInDB(nodeHealthTableSQL,sequenceStorage)
    }

    fun resultSetToBucket(rs: ResultSet): Bucket
    {
        return Bucket(rs.getLong("id"),
            rs.getString("name"),
            rs.getString("startvalue").toLong(),
            rs.getString("created").toLong(),
            rs.getString("updated").toLong(),
            rs.getInt("deleted")==1,
            rs.getInt("paused")==1,
            rs.getString("node"),
            TokenBucket( rs.getString("name"), LongSequence( if(rs.getString("mostrecent").toLong()>0L) rs.getString("mostrecent").toLong()+1L else rs.getString("startvalue").toLong()),1000),
            rs.getString("mostrecent").toLong())
    }

}