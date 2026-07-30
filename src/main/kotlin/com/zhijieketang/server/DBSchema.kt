package com.zhijieketang.server

//import com.beust.klaxon.JsonObject
//import com.zhijieketang.client.COMMAND_LOGIN
//import com.zhijieketang.client.parser
//import com.zhijieketang.client.socket
import org.jetbrains.exposed.sql.Table
//import java.net.DatagramPacket

const val URL = "jdbc:mysql://localhost:3306/qq?serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8"
const val DRIVER_CLASS = "com.mysql.cj.jdbc.Driver"
const val DB_USER = "root"
const val DB_PASSWORD = "wasd2488130937"

object Users: Table() {
    val user_id = varchar("user_id", length = 80)
    override val primaryKey = PrimaryKey(user_id,name = "PK_UserID")

    val user_pwd = varchar("user_pwd", length = 25)
    val user_name = varchar("user_name", length = 80)
    val user_icon = varchar("user_icon", length = 100)

}

object Friends: Table() {
    val user_id1 = varchar("user_id1", length = 10)
    val user_id2 = varchar("user_id2", length = 10)
    override val primaryKey = PrimaryKey(user_id1,user_id2,name = "PK_UserId1Id2")
}



