package com.zhijieketang.server

import java.net.InetAddress

data class ClientInfo (
    val port:Int,
    val address: InetAddress,
    val userId: String)
