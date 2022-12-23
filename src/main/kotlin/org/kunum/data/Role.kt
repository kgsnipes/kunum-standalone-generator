package org.kunum.data

import io.javalin.security.RouteRole

enum class Role : RouteRole {
    ANONYMOUS,USER
}