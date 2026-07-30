package com.zhijieketang.client

import com.beust.klaxon.Parser
import java.net.DatagramSocket

const val COMMAND_LOGIN = 1
const val COMMAND_LOGOUT = 2
const val COMMAND_SENDMSG = 3
const val COMMAND_REFRESH = 4

const val SERVER_IP = "127.0.0.1"
const val SERVER_PORT = 7788

var socket = DatagramSocket()

val parser = Parser.default()

fun main(){
    socket.soTimeout = 2000
    println("客户端运行...")
    LoginFrame().isVisible = true
}