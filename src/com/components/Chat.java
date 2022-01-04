package com.components;

import com.wsocket.WSocket;

import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Chat  extends JFrame  {
    private JPanel ChatPanel;
    private JTextArea outMessage;
    private JTextArea inputMessage;
    private JButton bSend;
    private JButton bClear;
    private JLabel inputLabel;
    private ServerSocket server = null;
    public WSocket serverSocket = null;
    public WSocket clientSocket = null;
    private Chat wChat = null;

    public String username;
    public String partnerUsername;
    public ConcurrentLinkedQueue<WSocket> serverSocketList = new ConcurrentLinkedQueue<>();
    public ConcurrentLinkedQueue<WSocket> clientSocketList = new ConcurrentLinkedQueue<>();

    public Chat(String username)  {
        this.username = username;
        this.wChat = this;

        setContentPane(ChatPanel);

        setTitle(username);
        setSize(450,300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
        onButtonSend(false);
        bSend.addActionListener(new sendMessage());
        bClear.addActionListener(new clearMessage());
        inputMessage.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                inputMessage.setText("");
            }
        });
    }

    public void onButtonSend(boolean status){
        bSend.setVisible(status);
        bClear.setVisible(status);
        inputMessage.setVisible(status);
        inputLabel.setVisible(status);
    }

    public Chat startServer(int port) {
        if(server != null)return this;
        try {
            server = new ServerSocket(port);
            System.out.println("Server Started " + port);
        }catch (IOException e){
            e.printStackTrace();
        }
        return this;
    }
    private static final Object lock = new Object();

    public boolean connectServer(String hostName, int port) {
        if(clientSocket != null){
            closeClient();
        }
        SocketAddress socketAddress = new InetSocketAddress(hostName, port);
        Socket socket = new Socket();
        try {
            synchronized(lock) {
                socket.connect(socketAddress, 2000);
            }
            clientSocket = new WSocket(socket,wChat);
            clientSocketList.add(clientSocket);
            clientSocket.send(this.username);
            Chat.this.partnerUsername = clientSocket.getInputSocket().readUTF();
            Chat.this.appendOutMessage("Connecting " +Chat.this.partnerUsername);
            return true;
        } catch (IOException e) {
            System.err.println("Socket failed");
        }
        return false;
    }

    public synchronized WSocket getSocketServer() {
        try {
            while (serverSocket == null) {
                if(server == null)continue;
                Socket socket = server.accept();
                try {
                    serverSocket = new WSocket(socket,wChat);
                    serverSocketList.add(serverSocket);
                    Chat.this.partnerUsername = serverSocket.getInputSocket().readUTF();
                    Chat.this.appendOutMessage("Get Client " +  Chat.this.partnerUsername);
                    serverSocket.send(Chat.this.username);
                } catch (IOException e) {
                    serverSocket.downService();
                    socket.close();
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return serverSocket;
    }

    public synchronized WSocket getSocketClient(){
        try {
            while (clientSocket == null) {
                wait(1000);
            }
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        return clientSocket;
    }

    public void appendOutMessage(String Msg){
        outMessage.append(Msg + "\n");
    }


    public void closeClient(){
        if(clientSocket == null)return;
        clientSocket.downService();
        clientSocket.stopСommunication();
        clientSocket = null;
    }

    public void stopСommunicationServer() {
        if(serverSocket == null)return;
        serverSocket.downService();
        serverSocket.stopСommunication();
        serverSocket = null;
    }

    public void closeServer() {
        stopСommunicationServer();
        try {
            server.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        server = null;
    }

    class sendMessage implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String msg = "[" + username + "]" + inputMessage.getText();
            appendOutMessage(msg);
            if(serverSocket != null)serverSocket.send(msg);
            else if(clientSocket != null)clientSocket.send(msg);
            else if(!serverSocketList.isEmpty()) {
                for (WSocket vr : serverSocketList) {
                    vr.send(msg);
                }
            }
        }
    }

    class clearMessage implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            outMessage.setText("");
        }
    }

}
