package com.zhijieketang.client

import com.beust.klaxon.JsonObject
import com.beust.klaxon.json
//import sun.security.jgss.GSSUtil.login
import java.awt.Color
import java.awt.Font
import java.awt.Toolkit
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.net.DatagramPacket
import java.net.InetAddress
import javax.swing.BorderFactory
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JPasswordField
import javax.swing.JTextField

class LoginFrame: JFrame() {

    private val screenWidth = Toolkit.getDefaultToolkit().screenSize.getWidth()
    private val screenHeight = Toolkit.getDefaultToolkit().screenSize.getHeight()

    private val frameWidth = 329
    private val frameHeight = 250

    private var txtUserId = JTextField()
    private var txtUserPwd = JPasswordField()

    private val paneLine: JPanel
        get() {
            val paneLine = JPanel().apply {
                layout = null
                setBounds(7,54,308,118)
                border = BorderFactory.createLineBorder(Color(102, 153, 255), 1)
            }
            with(JLabel()) {
                setBounds(227,47,67,21)
                font = Font("Dialog", Font.PLAIN, 12)
                foreground = Color(51,51,255)
                text = "忘记密码？"
                paneLine.add(this)
            }
            with(JLabel()) {
                text = "QQ密码"
                font = Font("Dialog", Font.PLAIN, 12)
                setBounds(21,48,54,18)
                paneLine.add(this)
            }
            with(JLabel()){
                text = "QQ号码"
                font = Font("Dialog", Font.PLAIN, 12)
                setBounds(21,14,55,18)
                paneLine.add(this)
            }
            txtUserId.setBounds(84,14,132,18)
            paneLine.add(this.txtUserId)

            txtUserPwd.setBounds(84,48,132,18)
            paneLine.add(this.txtUserPwd)

            with(JCheckBox()){
                text = "自动登录"
                font = Font("Dialog", Font.PLAIN, 12)
                setBounds(79,77,73,19)
                paneLine.add(this)
            }
            with(JCheckBox()){
                text = "隐身登录"
                font = Font("Dialog", Font.PLAIN, 12)
                setBounds(155,77,73,19)
                paneLine.add(this)

            }
            return paneLine
        }
    init {
        iconImage = Toolkit.getDefaultToolkit().getImage("images/QQ.png")
        title = "QQ登录"
        isResizable = false
        layout = null
        setSize(frameWidth, frameHeight)
        val x = (screenWidth - frameWidth).toInt() / 2
        val y = (screenHeight - frameHeight).toInt() / 2

        setLocation(x, y)
        contentPane.add(paneLine)
        with(JLabel()){
            icon = ImageIcon("images/QQll.JPG")
            setBounds(0,0,325,48)
            contentPane.add(this)
        }

        val btnLogin = JButton().apply {
            setBounds(152,181,63,19)
            font = Font("Dialog", Font.PLAIN, 12)
            text = "登录"
            contentPane.add(this)
        }

//        btnLogin.addActionListener {
//         //TODO
//        }

        btnLogin.addActionListener {
            val userId = txtUserId.text
            val password = String(txtUserPwd.password)

            val user = login(userId, password)
            if (user != null) {
                println("登录成功调转界面")
                FriendsFrame(user).isVisible = true

                isVisible = false


            }else {
                JOptionPane.showMessageDialog(null,"您QQ号码或密码不正确")
            }
        }
        val btnCancel = JButton().apply {
            setBounds(233,181,63,19)
            font = Font("Dialog", Font.PLAIN, 12)
            text = "取消"
            contentPane.add(this)

        }
        btnCancel.addActionListener {
            socket.close()
            System.exit(0) }

        with(JButton()){
            setBounds(14,179,99,22)
            font = Font("Dialog", Font.PLAIN, 12)
            text = "申请号码"
            contentPane.add(this)
        }
        addWindowListener(object: WindowAdapter(){
            override fun windowClosing(e: WindowEvent) {
                socket.close()

                System.exit(0)
            }
        })

    }

    private fun login(userId: String, password: String): Map<String, Any>? {
        val address = InetAddress.getByName(SERVER_IP)

        var jsonObj = json {
            obj("command" to COMMAND_LOGIN,"user_id" to userId,"user_pwd" to password)
        }
        var buffer = jsonObj.toJsonString().toByteArray()

        var packet = DatagramPacket(buffer, buffer.size, address,SERVER_PORT)

        socket.send(packet)

        buffer = ByteArray(1024)
        packet = DatagramPacket(buffer, buffer.size,address,SERVER_PORT)
        socket.receive(packet)

        val jsonString = String(buffer,0,packet.length)
        println("从服务器返回的消息:$jsonString")
        jsonObj = parser.parse(StringBuilder(jsonString)) as JsonObject

        if (jsonObj.string("result") == "-1") return null

        return jsonObj as Map<String, Any>?
    }
}