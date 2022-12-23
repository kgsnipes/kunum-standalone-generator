package org.kunum.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.EmptyCoroutineContext

class TokenBucket(val name:String, val sequencer: LongSequence, val bucketSize:Int)
{
    private val primary: LinkedBlockingQueue<String> = LinkedBlockingQueue<String>(bucketSize)
    private val secondary: LinkedBlockingQueue<String> = LinkedBlockingQueue<String>(bucketSize)
    private var currentList= AtomicReference(primary);
    var refreshNow: AtomicBoolean = AtomicBoolean(false)
    private val log= LoggerFactory.getLogger("TokenBucket")

    init {
        for(x in 1..bucketSize)
        {
            currentList.get().put(sequencer.getNext())
        }
        for(x in 1..bucketSize)
        {
            secondary.put(sequencer.getNext())
        }
        CoroutineScope(EmptyCoroutineContext).launch{
            monitorAndRefreshToken()
        }
    }

    fun getToken():String= if(currentList.get().isNotEmpty() && currentList.get().peek()!=null) {
        currentList.get().take();
    }
    else {
        log.info("Switching list")
        currentList.set(if(currentList===primary) {
            secondary
        } else {
            primary
        })
        refreshNow.set(true)
        if(currentList.get().peek()==null)
        {
            while (refreshNow.get())
            {
            }
        }
        getToken()
    }

    private suspend fun monitorAndRefreshToken(){
        while(true)
        {
            delay(5)
            if(refreshNow.get() && primary.peek()==null)
            {
                log.info("refreshing tokens primary")
                primary.clear()
                for(x in 1..bucketSize)
                {
                    primary.put(sequencer.getNext())
                }
                refreshNow.set(false)
            }
            if(refreshNow.get() && secondary.peek()==null)
            {
                log.info("refreshing tokens secondary")
                secondary.clear()
                for(x in 1..bucketSize)
                {
                    secondary.put(sequencer.getNext())
                }
                refreshNow.set(false)
            }
        }
    }

}