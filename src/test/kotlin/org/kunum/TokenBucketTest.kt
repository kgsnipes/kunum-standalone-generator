package org.kunum

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.kunum.core.LongSequence
import org.kunum.core.TokenBucket
import java.lang.RuntimeException

class TokenBucketTest {

    val tokenBucket=TokenBucket("OrderBucket",LongSequence(1000L),1000)


    @Test
    fun getNextTestWithCoroutines()
    {
        val container= mutableListOf<String>()
        val threads=100
        val tokenLimit=1000
        runBlocking {
            for(x in 1..threads)
            {
                launch {
                    val threadNum=x
                    println("Starting thread ${threadNum}")
                    for(y in 1..tokenLimit) {
                        val token = tokenBucket.getToken()
                        if (container.contains(token)) {
                            throw RuntimeException("found duplicate token $token")
                        } else {
                            container.add(token)
                            println("${threadNum} consumed ${token}")
                        }
                    }

                }
            }
        }
        assert(threads*tokenLimit==container.size)
    }

    @Test
    fun getNextTestWithThreads()
    {
        val container= mutableListOf<String>()
        val threads=100
        val tokenLimit=1000
        runBlocking {
            for(x in 1..threads)
            {
                val t=Thread {
                    val threadNum=x
                    println("Starting thread ${threadNum}")
                    for(y in 1..tokenLimit) {
                        val token = tokenBucket.getToken()
                        if (container.contains(token)) {
                            throw RuntimeException("found duplicate token $token")
                        } else {
                            container.add(token)
                            println("${threadNum} consumed ${token}")
                        }
                    }

                }
                t.start()
                t.join()
            }
        }
        assert(threads*tokenLimit==container.size)
    }
}