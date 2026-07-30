package com.zhijieketang.client

import com.beust.klaxon.JsonObject
import com.beust.klaxon.json
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
//import org.jetbrains.exposed.sql.Op
//import sun.jvm.hotspot.oops.CellTypeState.addr
import java.awt.BorderLayout
import java.awt.Color
import java.awt.GridLayout
import java.awt.Toolkit
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.net.DatagramPacket
import java.net.InetAddress
import javax.swing.BorderFactory
//import javax.swing.BoxLayout
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.SwingConstants

class FriendsFrame(private val user:Map<String, Any>): JFrame() {
    private val friends: List<Map<String, String>>

    private val lblFriendList = mutableListOf<JLabel>()

    private val screenWidth = Toolkit.getDefaultToolkit().screenSize.getWidth()

    private val frameWidth = 260
    private val frameHeight = 600

    private var job: Job? = null
    private var isRunning = true



    init {
        title = "QQ2006"
        setBounds(screenWidth.toInt() - 300,10,frameWidth,frameHeight)
        iconImage = Toolkit.getDefaultToolkit().getImage(("images/QQ.png"))
        val borderLayout = contentPane.layout as BorderLayout
        borderLayout.vgap = 5

        friends = user["friends"] as List<Map<String, String>>
        val userId = user["user_id"] as String
        val userName = user["user_name"] as String
        val userIcon = user["user_icon"] as String

        with(JLabel(userName)){
            horizontalAlignment = SwingConstants.CENTER
            val iconFile = "/images/$userIcon.jpg"
            icon = ImageIcon(iconFile)
            contentPane.add(this, BorderLayout.NORTH)
        }

        val panel1 = JPanel()
        panel1.layout = BorderLayout(0, 0)

        with(JScrollPane()){
            border = BorderFactory.createLineBorder(Color.blue,1)
            setViewportView(panel1)
            contentPane.add(this, BorderLayout.CENTER)

        }
        with(JLabel("我的好友")){
            horizontalAlignment = SwingConstants.CENTER
            panel1.add(this, BorderLayout.NORTH)

        }
        val friendListPanel = JPanel()
        friendListPanel.layout = GridLayout(50,0,0,5)
        panel1.add(friendListPanel)

        friends.forEach { friend ->
            val friendUserId = friend["usre_id"]
            val friendUserName = friend["user_name"]
            val friendUserIcon = friend["user_icon"]

            val friendUserOnline = friend["online"]

            val lblFriend = JLabel(friendUserName).apply {

                toolTipText = friendUserId
                val friendIconFile = "images/$friendUserIcon.jpg"
                icon = ImageIcon(friendIconFile)

                isEnabled = friendUserOnline != "0"

                lblFriendList.add(this)
                friendListPanel.add(this)
            }
            lblFriend.addMouseListener(object : MouseAdapter(){
                override fun mouseClicked(e: MouseEvent){
                    if(e.clickCount == 2){
                        stopCoroutine()
                        ChatFrame(this@FriendsFrame, user, friend).isVisible = true
                    }
                }
            })
        }

        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                //TODO用户下线
                val jsonObj = json {
                    obj("command" to COMMAND_LOGOUT, "user_id" to userId)
                }
                    val b = jsonObj.toJsonString().toByteArray()
                    val address = InetAddress.getByName(SERVER_IP)
                    val packet = DatagramPacket(b,b.size,address,SERVER_PORT)
                    socket.send(packet)
                    socket.close()
                    System.exit(0)

                //退出系统
//                System.exit(0)
            }

        })
        resetCoroutine()
    }

    //TODO启动接收消息子进程
    //TODO刷新好友列表
    fun refreshFriendList(userIdList: List<String>){
        lblFriendList.forEach {
            val friendId = it.toolTipText!!

            it.isEnabled = userIdList.contains(friendId)
        }
    }

    fun resetCoroutine() = runBlocking<Unit>{
        isRunning = true
        job = GlobalScope.launch {
            run()
        }
    }
    fun stopCoroutine() = runBlocking<Unit>{
        isRunning = false
        job?.cancelAndJoin()
    }

    suspend fun run(){
        val buffer = ByteArray(1024)
        while (isRunning) {
            val address = InetAddress.getByName(SERVER_IP)
            val packet = DatagramPacket(buffer,buffer.size, address, SERVER_PORT)

            try {
                socket.receive(packet)

                val stringObj = String(buffer,0,packet.length)
                println("客户端收到的消息：${stringObj}")

                val jsonObj = parser.parse(StringBuilder(stringObj)) as JsonObject

                val cmd = jsonObj.int("command")

                if(cmd != null && cmd == COMMAND_REFRESH){
                    val userIdList = jsonObj["OnlineUserList"] as List<String>

                    refreshFriendList(userIdList)
                }
                delay(100L)


            }catch (e:Exception){
                //捕获超时异常，继续

            }
        }
    }

}