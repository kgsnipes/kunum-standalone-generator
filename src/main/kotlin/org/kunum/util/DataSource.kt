package org.kunum.util

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.sql.Connection


class DataSource(val url:String, val user:String,val pwd:String) {

    private val config = HikariConfig()
    private var ds: HikariDataSource? = null

    init
    {
        config.jdbcUrl = url
        config.username = user
        config.password =pwd
        config.maximumPoolSize=100
        config.minimumIdle=10
        ds = HikariDataSource(config)
    }

    fun getConnection(): Connection? {
        return ds!!.connection
    }
}