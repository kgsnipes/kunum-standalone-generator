package org.kunum

import org.kunum.services.ApplicationService
import org.kunum.util.CommonUtil
import org.slf4j.LoggerFactory
import picocli.CommandLine
import java.util.*
import java.util.concurrent.Callable
import kotlin.system.exitProcess


fun main(args:Array<String>){
    CommandLine(KunumCommand()).execute(*args)
}

@CommandLine.Command(name = "kunum", mixinStandardHelpOptions = true, version = ["kunum 1.0"],
    description = ["Starts the kunum app"])
class KunumCommand : Callable<Int> {

    @CommandLine.Option(names =["-f","--file"], description = ["The file to load the kunum configuration"], defaultValue = CommandLine.Option.NULL_VALUE, required=false)
    var file: String?=null

    override fun call(): Int {
        launchKunumApp(file)
        return 0
    }
}

fun launchKunumApp(filePath:String?)
{
    val log=LoggerFactory.getLogger("kunum main function")
    try {
        var props= getDefaultProperties()
        if(filePath!=null && filePath.isNotEmpty())
        {
            log.info("trying to read file ${filePath} to read application config")
            props= CommonUtil.readFileToProps(filePath)
        }
        log.info("Trying to start the application with the API key : ${props["kunum.dummy.oauth.token"]}")
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

fun getDefaultProperties(): Properties {

    val defaultConfig:String="""
    kunum.mode.generator=true
    kunum.generator.jdbcURL=jdbc:sqlite:./local.db
    kunum.generator.jdbc.user= 
    kunum.generator.jdbc.password=
    kunum.generator.bucket.limit=100
    kunum.generator.server.port=9000
    kunum.dummy.oauth.token=${UUID.randomUUID().toString()}
    kunum.generator.web.api.host=0.0.0.0
    kunum.generator.node.name=node_1
    kunum.generator.api.mode=true
    """

    return CommonUtil.readStringToProps(defaultConfig)
}




