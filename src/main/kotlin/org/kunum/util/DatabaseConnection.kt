package org.kunum.util

import java.sql.Connection
import java.sql.DriverManager


class DatabaseConnection(val jdbcUrl:String,val user:String,val password:String)
{
    private var connection: Connection? =null
    init {
        getConnection()
    }
    fun getConnection(): Connection {
        return if(this.connection!=null) {
            if(this.connection!!.isClosed())
            {
                this.connection= DriverManager.getConnection(jdbcUrl,user,password)
            }
            this.connection!!
        } else {
            this.connection= DriverManager.getConnection(jdbcUrl,user,password)
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