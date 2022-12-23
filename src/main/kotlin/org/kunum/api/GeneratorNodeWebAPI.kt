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
            config.staticFiles.add { staticFiles ->
                staticFiles.hostedPath = "/"                    // change to host files on a subpath, like '/assets'
                staticFiles.directory = "/public"               // the directory where your files are located
                staticFiles.location =
                    Location.CLASSPATH       // Location.CLASSPATH (jar) or Location.EXTERNAL (file system)
                staticFiles.precompress =
                    false                 // if the files should be pre-compressed and cached in memory (optimization)
                staticFiles.aliasCheck =
                    null                   // you can configure this to enable symlinks (= ContextHandler.ApproveAliases())
                //                staticFiles.headers = mapOf(...)                // headers that will be set for the files
                staticFiles.skipFileFunction =
                    { req -> false } // you can use this to skip certain files in the dir, based on the HttpServletRequest
            }
            config.showJavalinBanner = false
        }

        api.routes {
                ApiBuilder.path("api/token")
                {
                    ApiBuilder.get("{name}", { ctx ->
                        ctx.result(
                            """
                                {
                                    "token":${generatorNode.getTokenFromBucket(ctx.pathParam("name").toString())}
                                }
                            """.trimIndent()
                        )
                        ctx.status(HttpStatus.OK)
                    }, Role.USER)
                }

            }

        api.exception(Exception::class.java) { e, ctx ->
            e.printStackTrace()
            throw InternalServerErrorResponse(e.message?:"Error Occurred")
        }?.get("/",{ ctx ->

            ctx.result(Json.encodeToString(ApiResponse("Service is up and running",null,null,"SUCCESS")))
            ctx.status(HttpStatus.OK)


                   },Role.ANONYMOUS)
            ?.start()

        this.webApp=api
    }

    fun stop()
    {
        this.webApp?.stop()
    }


}