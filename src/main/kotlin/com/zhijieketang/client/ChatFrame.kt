package com.zhijieketang.client

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.beust.klaxon.json
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
//import java.awt.BorderLayout
import java.awt.Color
import java.awt.Toolkit
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.net.DatagramPacket
import java.net.InetAddress
import java.text.SimpleDateFormat
import java.util.Date
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea
//import javax.tools.Tool

class ChatFrame(
        private val friendsFrame:FriendsFrame,
        user:Map<String,Any>,
        friend: Map<String, String>): JFrame() {

    private var isRunning = true
    private val userId = user["user_id"] as String
    private val friendUserId: String
    private val friendUserName: String

    private val screenHeight = Toolkit.getDefaultToolkit().screenSize.getHeight()
    private val screenWidth = Toolkit.getDefaultToolkit().screenSize.getWidth()

    private val frameWidth = 360
    private val frameHeight = 330

    private val txtMainInfo = JTextArea()
    private val txtInfo = JTextArea()

    private val infoLog = StringBuffer()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    private var job: Job? = null

    private val panLine1: JPanel
    get() {
        txtMainInfo.isEditable = false

        val panLine1 = JPanel().apply{
            layout = null
            setBounds(5,5,330,210)
            border = BorderFactory.createLineBorder(Color.blue,1)

        }
        with(JScrollPane()){
            setBounds(5,5,320,200)
            panLine1.add(this)
            setViewportView(txtMainInfo)
        }
        return panLine1

    }

    private val panLine2: JPanel
        get() {
            val panLine2 = JPanel().apply{
                layout = null
                setBounds(5,220,330,50)
                border = BorderFactory.createLineBorder(Color.blue,1)
                add(sendButton)
            }
            with(JScrollPane()){
                setBounds(5,5,222,40)
                panLine2.add(this)
                setViewportView(txtInfo)
            }
            return panLine2
        }
    private val sendButton:JButton
        get() {
            val button = JButton("发送").apply {
                setBounds(232,10,90,30)
            }
            button.addActionListener {
                sendMessage()
                txtInfo.text = ""
            }
            return button
        }
    private fun sendMessage() {
        //TODO 发送信息
        if(txtInfo.text != ""){
            val date = dateFormat.format(Date())

            val info = "#$date#\n您对${friendUserName}说：${txtInfo.text}"
            infoLog.append(info).append("\n")
            txtMainInfo.text = infoLog.toString()

            val jsonObj = JsonObject()
            jsonObj["receive_user_id"] = friendUserId
            jsonObj["user_id"] = userId
            jsonObj["message"] = txtInfo.text
            jsonObj["command"] = COMMAND_SENDMSG

            val address = InetAddress.getByName(SERVER_IP)
            val buffer = jsonObj.toJsonString().toByteArray()
            val packet = DatagramPacket(buffer, buffer.size, address,SERVER_PORT)
            socket.send(packet)
        }
    }

    init {
        val userIcon = user["user_icon"]!!
        friendUserId = friend["user_id"]!!
        friendUserName = friend["user_name"]!!

        val iconFile = "/images/$userIcon.jpg"
        iconImage = Toolkit.getDefaultToolkit().getImage(iconFile)
        title = "与${friendUserName}聊天中。。。"
        isResizable = false
        layout = null
        setSize(frameWidth, frameHeight)
        val x = (screenWidth - frameWidth).toInt() / 2
        val y = (screenHeight - frameHeight).toInt() / 2

        setLocation(x, y)

        contentPane.add(panLine1)
        contentPane.add(panLine2)

        addWindowListener(object : WindowAdapter(){
            override fun windowClosing(e: WindowEvent) {
                stopCoroutine()
                isVisible = false
                friendsFrame.resetCoroutine()
            }
        })
        resetCoroutine()
    }

    fun resetCoroutine() = runBlocking<Unit> {
        isRunning = true




        job = GlobalScope.launch {
            run()
        }
    }

    fun stopCoroutine() = runBlocking<Unit> {
        isRunning = false
        job?.cancelAndJoin()
    }

    suspend fun run(){
        val buffer = ByteArray(1024)
        while (isRunning) {
            val address = InetAddress.getByName(SERVER_IP)
            val packet = DatagramPacket(buffer, buffer.size,address, SERVER_PORT)

            try{
                socket.receive(packet)
                val stringObj = String(buffer, 0, packet.length)
                println("从服务器接收的数据：$stringObj")

                val jsonObj = parser.parse(StringBuilder(stringObj)) as JsonObject

                val cmd = jsonObj.int("command")

                if(cmd != null && cmd == COMMAND_REFRESH){
                    val userIdList = jsonObj["OnlineUserList"] as List<String>
                }else{
                    //TODO接收聊天信息
                    val date = dateFormat.format(Date())
                    val message = jsonObj.string("message")
                    if (message != null){
                        val info = "#$date#\n${friendUserName}对您说：$message"
                        infoLog.append(info).append("\n")

                        txtMainInfo.text = infoLog.toString()
                        txtMainInfo.caretPosition = txtMainInfo.document.length
                    }
                }
                delay(100L)
            }catch (e:Exception){
                //捕获超时异常，继续
            }
        }

    }




    //TODO接收信息
}