package org.kunum

import org.kunum.services.ApplicationService
import org.kunum.util.CommonUtil
import org.slf4j.LoggerFactory


fun main(args:Array<String>){
    val log=LoggerFactory.getLogger("kunum main function")
    try {
        log.info("trying to read file ${args[0]} to read application config")
        val props= CommonUtil.readFileToProps(args[0])
        log.info("Trying to launch the application kunum")
        val kunum= ApplicationService(props)
        log.info("Application started!!!")
        log.info("Adding runtime hook")
        Runtime.getRuntime().addShutdownHook(Thread(Runnable { kunum.stop() }))
    }
    catch (e:Exception)
    {
        log.error(e.toString())
    }

}




