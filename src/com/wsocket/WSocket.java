package com.wsocket;

import com.components.Chat;
import com.math.MathRing;

import java.io.*;
import java.net.Socket;

public class WSocket extends Thread {
        private Socket socket;
        private Chat wChat;
        private DataInputStream in;
        private DataOutputStream out;
        private Thread readHandel = null;
        private int ssesionKey;

        public WSocket(Socket socket, Chat wChat) throws IOException {
            this.socket = socket;
            this.wChat = wChat;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        }

        public void send(String msg) {
            try {
                out.writeUTF(MathRing.ssesionXor(msg, ssesionKey));
                out.flush();
            } catch (IOException ignored) {}
        }

        public void downService() {
            try {
                if(!socket.isClosed()) {
                    socket.close();
                    in.close();
                    out.close();
                    for (WSocket vr : wChat.serverSocketList) {
                        if(vr.equals(this)) vr.interrupt();
                        wChat.serverSocketList.remove(this);
                    }
                    for (WSocket vr : wChat.clientSocketList) {
                        if(vr.equals(this)) vr.interrupt();
                        wChat.clientSocketList.remove(this);
                    }
                }
            } catch (IOException ignored) {}
        }

        public DataOutputStream getOutputSocket(){
            if(readHandel != null)return null; // Если секретный ключ настроен, то общение напрямую запрещаем
            return out;
        }

        public DataInputStream getInputSocket(){
            if(readHandel != null)return null;
            return in;
        }

        public void startСommunication(int ssesionKey){
            this.ssesionKey = ssesionKey;
            wChat.onButtonSend(true);
            readHandel = new ReadMsg();
            readHandel.start();
        }

        public void stopСommunication(){
            if(readHandel == null)return;
            readHandel.interrupt();
            readHandel = null;
        }

        private class ReadMsg extends Thread {
            @Override
            public void run() {
                String word;
                try {
                    try {
                        while (true) {
                            word = in.readUTF();
                            System.out.println("Пришло сообщение " + word);
                            wChat.appendOutMessage(MathRing.ssesionXor(word, ssesionKey));
                        }
                    } catch (NullPointerException ignored) {
                    }


                } catch (IOException e) {
                    WSocket.this.downService();
                }
            }
        }

}