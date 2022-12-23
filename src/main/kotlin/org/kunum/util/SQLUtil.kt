package org.kunum.util

import org.kunum.core.LongSequence
import org.kunum.core.TokenBucket
import org.kunum.data.Bucket
import java.sql.ResultSet

class SQLUtil {

    fun createSequenceEntryTable(storage: DatabaseConnection)
    {
        val entriesTableSQL="""
            CREATE TABLE IF NOT EXISTS sequenceentries(
            token TEXT, 
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

    fun createSequenceTable(storage: DatabaseConnection)
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


    fun resultSetToBucket(rs: ResultSet,bucketSize:Int): Bucket
    {
        return Bucket(rs.getLong("id"),
            rs.getString("name"),
            rs.getString("startvalue").toLong(),
            rs.getString("created").toLong(),
            rs.getString("updated").toLong(),
            rs.getInt("deleted")==1,
            rs.getInt("paused")==1,
            rs.getString("node"),
            TokenBucket( rs.getString("name"), LongSequence( if(rs.getString("mostrecent").toLong()>0L) rs.getString("mostrecent").toLong()+1L else rs.getString("startvalue").toLong()),bucketSize),
            rs.getString("mostrecent").toLong())
    }

}