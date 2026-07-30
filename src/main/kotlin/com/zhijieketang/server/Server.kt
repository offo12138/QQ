package com.zhijieketang.server

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
//import com.beust.klaxon.Parser
import com.beust.klaxon.json
//import com.zhijieketang.client.COMMAND_LOGIN
//import java.lang.Compiler.command
import java.net.DatagramPacket
import java.net.DatagramSocket

const val COMMAND_LOGIN = 1
const val COMMAND_LOGOUT = 2
const val COMMAND_SENDMSG = 3
const val COMMAND_REFRESH = 4



const val SERVER_PORT  = 7788

fun main() {
    println("服务器启动，监听自己的端口$SERVER_PORT....")

    val parser = Parser.default()

    val dao = UserDAO()

    val clientList = mutableListOf<ClientInfo>()

    DatagramSocket(SERVER_PORT).use { socket ->

        while (true) {
            var buffer = ByteArray(1024)
            var packet = DatagramPacket(buffer, buffer.size)
            socket.receive(packet)
            val jsonString = String(buffer, 0, packet.length)

            val address = packet.address
            val port = packet.port
            println("服务器接收客户端，消息:$jsonString")

            val jsonObject = parser.parse(StringBuilder(jsonString)) as JsonObject

            val cmd = jsonObject.int("command")

            when (cmd) {
                COMMAND_LOGIN -> {
                    val userId = jsonObject["user_id"] as String
                    val userPwd = jsonObject["user_pwd"] as String
                    val user = dao.findById(userId)

                    if (user != null && userPwd == user["user_pwd"]) {
                        val sendJsonObj = JsonObject(user)

                        sendJsonObj["result"] = "0"

                        val cInfo = ClientInfo(port,address,userId)
                        if(clientList.none{it.userId == userId}) {
                            clientList.add(cInfo)
                        }
                        val friends = dao.findFriends(userId)!!.map {
                            val friend = it.toMutableMap()
                            val fid = it["user_id"]

                            if(clientList.any {it.userId == fid}) friend["online"] = "1" else friend["online"] = "0"

                            friend
                        }.map{
                            JsonObject(it)
                        }
                        sendJsonObj["friends"] = json {
                            array(friends)
                        }
                        println("服务器发送用户成功，消息：${sendJsonObj.toJsonString()}")

                        buffer = sendJsonObj.toJsonString().toByteArray()
                        packet = DatagramPacket(buffer, buffer.size,address,port)
                        socket.send(packet)

                    }else {
                        val jsonObj = json {
                            obj("result" to "-1")
                        }
                        println("服务器给用户登录失败，消息：${jsonObj.toJsonString()}")
                        buffer = jsonObj.toJsonString().toByteArray()
                        packet = DatagramPacket(buffer, buffer.size,address,port)
                        socket.send(packet)
                    }

                }
                COMMAND_SENDMSG -> {
                    //TODO用户发送消息
                    val friendUserId = jsonObject["receive_user_id"] as String
                    clientList.filter {
                        it.userId == friendUserId
                    }.forEach {
                        println("服务器转发聊天，消息：${jsonObject.toJsonString()}")
                        buffer = jsonObject.toJsonString().toByteArray()
                        packet = DatagramPacket(buffer, buffer.size,it.address,it.port)
                        socket.send(packet)
                    }

                }
                COMMAND_LOGOUT ->{
                    //用户发送注销命令
                    val userId = jsonObject["user_id"] as String
                    val clientInfo = clientList.first{
                        it.userId == userId

                    }
                    clientList.remove(clientInfo)
                }
            }

            if (clientList.isEmpty()) continue

            val jsonObj = JsonObject()
            jsonObj["command"] = COMMAND_REFRESH

            val userIdList = clientList.map { it.userId }

            jsonObj["OnlineUserList"] = json{
                array(userIdList)
            }
            println("服务器向客户端发送消息，刷新用户列表：${jsonObj.toJsonString()}")

            clientList.forEach {
                buffer = jsonObj.toJsonString().toByteArray()
                packet = DatagramPacket(buffer, buffer.size,it.address,it.port)
                socket.send(packet)
            }
        }
    }
}


