package com.zhijieketang.server

//import DB_PASSWORD
//import DB_USER
//import DRIVER_CLASS
//import URL
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class UserDAO {
    fun findById(id:String): Map<String, Any>?{

        var list: List<Map<String, String>> = emptyList()

        Database.connect(URL, user = DB_USER, password = DB_PASSWORD,driver = DRIVER_CLASS)

        transaction {
            addLogger(StdOutSqlLogger)
            list = Users.select {Users.user_id.eq(id)}.map {
                val row = mutableMapOf<String,String>()
                row["user_id"] = it[Users.user_id]
                row["user_pwd"] = it[Users.user_pwd]
                row["user_name"] = it[Users.user_name]
                row["user_icon"] = it[Users.user_icon]

                row
            }
        }
        return if (list.isEmpty()) null else list.first()
    }


    fun findFriends(id: String): List<Map<String, Any>>? {
        var list: List<Map<String, String>> = emptyList()

        Database.connect(URL, user = DB_USER, password = DB_PASSWORD,driver = DRIVER_CLASS)

        transaction {
            addLogger(StdOutSqlLogger)

            val userList1 = Friends.slice(Friends.user_id2).select {
                Friends.user_id1.eq(id)
            }.map{
                it[Friends.user_id2]
            }
            val userList2 = Friends.slice(Friends.user_id1).select {
                Friends.user_id2.eq(id)
            }.map{
                it[Friends.user_id1]
            }
            list = Users.select{
                Users.user_id.inList(userList1 + userList2)
            }.map {
                val row = mutableMapOf<String,String>()
                row["user_id"] = it[Users.user_id]
                row["user_pwd"] = it[Users.user_pwd]
                row["user_name"] = it[Users.user_name]
                row["user_icon"] = it[Users.user_icon]

                row
            }
        }
        return list
    }
}