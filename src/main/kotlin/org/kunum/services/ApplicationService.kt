package org.kunum.services

import org.kunum.util.getBoolean
import org.slf4j.LoggerFactory
import java.util.*


class ApplicationService(val config:Properties)
{
    private val log= LoggerFactory.getLogger("ApplicationService")

    private var generatorNode: GeneratorNode?=null

    init {

        log.info("Application service init started")
        log.info("Trying to start kunum on generator mode.")
        generatorNode= GeneratorNode(config)
    }


    fun stop():Unit{
        generatorNode?.stop()
    }

}