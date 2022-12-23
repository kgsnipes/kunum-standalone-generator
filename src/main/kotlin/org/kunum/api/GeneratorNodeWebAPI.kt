package org.kunum.api

import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder
import io.javalin.http.Context
import io.javalin.http.HttpStatus
import io.javalin.http.InternalServerErrorResponse
import io.javalin.http.staticfiles.Location
import io.javalin.rendering.template.JavalinThymeleaf
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.kunum.data.ApiResponse
import org.kunum.data.Role
import org.kunum.services.GeneratorNode
import org.kunum.util.AuthUtil
import org.kunum.util.getString

class GeneratorNodeWebAPI(val generatorNode:GeneratorNode) {

    private var webApp: Javalin? = null
    val authUtil = AuthUtil(generatorNode.config)

    init {
        createAndStartJavalinApp()
    }

    fun serverSupplier(): Server {
        val server = Server()
        val serverConnector = ServerConnector(server)
        serverConnector.host = generatorNode.config.getString("kunum.generator.web.api.host")
        serverConnector.port = generatorNode.serverPort
        server.addConnector(serverConnector)
        return server
    }

    fun getUserRole(ctx: Context): Role = when (authUtil.isValidAPIToken(ctx)) {
        true -> Role.USER
        false -> Role.ANONYMOUS
    }

    fun createAndStartJavalinApp() {

        JavalinThymeleaf.init()
        var api = Javalin.create { config ->
            config.jetty.server(::serverSupplier)
            config.accessManager { handler, ctx, routeRoles ->
                val userRole = getUserRole(ctx) // determine user role based on request
                println(userRole)
                if (routeRoles.contains(userRole)) {
                    handler.handle(ctx)
                } else {
                    ctx.status(HttpStatus.FORBIDDEN).result("Unauthorized")
                }
            }
            config.showJavalinBanner = false
        }

        api.routes {
                ApiBuilder.path("api/bucket")
                {
                    ApiBuilder.get("{name}/token", { ctx ->
                        ctx.result(
                            """
                                {
                                    "token":${generatorNode.getTokenFromBucket(ctx.pathParam("name").toString())}
                                }
                            """.trimIndent()
                        )
                        ctx.status(HttpStatus.OK)
                    }, Role.USER)
                    ApiBuilder.get("{name}/create/{startValue}", { ctx ->
                        val bucket=generatorNode.createBucket(ctx.pathParam("name"),ctx.pathParam("startValue").toLong())
                        bucket?.let {
                            ctx.result("""
                                {
                                  "bucketId":"${it.id}",
                                  "bucketName":"${it.name}",
                                  "bucketStartValue":${it.startValue}
                                }
                            """.trimIndent())
                        }
                        ctx.status(HttpStatus.OK)
                    }, Role.USER)
                    ApiBuilder.get("{name}/reset/{resetValue}", { ctx ->
                        //TODO
                        ctx.status(HttpStatus.OK)
                    }, Role.USER)
                    ApiBuilder.get("{name}/delete", { ctx ->
                        //TODO
                        ctx.status(HttpStatus.OK)
                    }, Role.USER)
                }

            }

        api.exception(Exception::class.java) { e, ctx ->
            e.printStackTrace()
            throw InternalServerErrorResponse(e.message?:"Error Occurred")
        }?.get("/",{ ctx ->
            ctx.result(Json.encodeToString(ApiResponse("Service is up and running",null,null,"SUCCESS")))
            ctx.status(HttpStatus.OK) },Role.ANONYMOUS)
            ?.start()

        this.webApp=api
    }

    fun stop()
    {
        this.webApp?.stop()
    }


}