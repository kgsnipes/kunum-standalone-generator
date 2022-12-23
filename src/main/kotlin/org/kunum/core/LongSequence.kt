package org.kunum.core

import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicLong


class LongSequence(startingValue: Long=1L)
{
    var value: AtomicLong = AtomicLong(1L)
    private val log= LoggerFactory.getLogger("LongSequence")
    init {
        value= AtomicLong(startingValue);
    }

    fun getNext():String {
        if(this.value.get()>0 && this.value.get()<=Long.MAX_VALUE){
            return value.getAndIncrement().toString()
        }
        else
        {
            log.error("The current sequence is maxxed out")
            throw java.lang.RuntimeException("Maxxed out")
        }
    }
}
