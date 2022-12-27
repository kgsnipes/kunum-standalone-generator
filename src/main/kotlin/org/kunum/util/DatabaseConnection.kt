package org.kunum.util

import java.sql.Connection
import java.sql.DriverManager


class DatabaseConnection(val jdbcUrl:String,val user:String,val password:String)
{
    private var connection: Connection? =null
    private val ds=DataSource(jdbcUrl,user,password)
    init {
        getConnection()
    }
    fun getConnection(): Connection {
        return if(this.connection!=null) {
            if(this.connection!!.isClosed())
            {
                this.connection= ds.getConnection()
            }
            this.connection!!
        } else {
            this.connection= ds.getConnection()
            //this.connection!!.autoCommit=false
            this.connection!!
        }
    }
    fun closeConnection():Unit{
        this.connection?.close()
    }
    fun commit():Unit{
        this.connection?.commit()
    }

    fun rollback():Unit{
        this.connection?.rollback()
    }

}