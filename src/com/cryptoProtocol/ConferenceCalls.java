package com.cryptoProtocol;

import com.components.Chat;
import com.math.MathRing;
import com.wsocket.WSocket;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;


public class ConferenceCalls  implements IcryptoProtocol {

    private final static int MaxUsers = 4;
    private final static int allPrimeNumber = MathRing.generatorLargeNumber(10000, 100000, 10);
    private final static int formingElement = MathRing.findPrimitive(allPrimeNumber - 1);

    private int port;
    private String ip;

    @Override
    public void action(String ip, int port) {
        this.ip = ip;
        this.port = port;
        for(int i = 0; i < MaxUsers; i ++) {
            new Users(i).start();
        }
    }

    private class Users extends Thread {
        private int shiftIp;
        private ConcurrentHashMap<Integer, Integer> userZ = new ConcurrentHashMap<>();
        private ConcurrentHashMap<Integer, Integer> userX = new ConcurrentHashMap<>();
        public Users(Object parameter) {
            this.shiftIp = (int)parameter;
        }

        public void run() {
            int nextUser = ((shiftIp + 1) >= MaxUsers) ? 0 : (shiftIp + 1);
            int previousUser = ((shiftIp - 1) <= -1 ? MaxUsers - 1 : (shiftIp - 1));
            Chat user = new Chat("User" + shiftIp).startServer(port + shiftIp);
            int randNumber = MathRing.randimIntFromTo(1, allPrimeNumber - 2);
            int exponentZ = MathRing.exponentiationRing(formingElement, randNumber, allPrimeNumber);
            user.appendOutMessage(" Простое число " + allPrimeNumber + " | Его образующий " + formingElement);
            user.appendOutMessage(" Моё случайное число: " + randNumber + " | Открытая экспонента " + exponentZ);
            ;
            userZ.put(shiftIp,exponentZ);
            new getServer(user).start();
            for(int i = 0; i < MaxUsers; i ++) {
                if(i == shiftIp || !user.connectServer(ip, port + i))continue;
                user.clientSocket = null;
            }

            for (WSocket vr : user.clientSocketList) {
                try {
                    vr.getOutputSocket().writeInt(shiftIp);
                    vr.getOutputSocket().writeInt(exponentZ);
                    vr.getOutputSocket().flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            synchronized (user.serverSocketList) {
                for (WSocket vr : user.serverSocketList) {
                    try {
                        int userId = vr.getInputSocket().readInt();
                        int userExponentz = vr.getInputSocket().readInt();
                        userZ.put(userId, userExponentz);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(userZ.size() < MaxUsers) {
                    try {
                        wait(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            user.appendOutMessage("set Z: " + userZ );
            int nextUserZ = (userZ.get(nextUser) > userZ.get(previousUser)) ? userZ.get(nextUser) : userZ.get(previousUser) ;
            int previousUserZ =  (userZ.get(nextUser) < userZ.get(previousUser)) ? userZ.get(previousUser) : userZ.get(nextUser);
            int exponentX = MathRing.exponentiationRing(nextUserZ / previousUserZ, randNumber, allPrimeNumber);

            userX.put(shiftIp,exponentX);

            for (WSocket vr : user.clientSocketList) {
                try {
                    vr.getOutputSocket().writeInt(shiftIp);
                    vr.getOutputSocket().writeFloat(exponentX);
                    vr.getOutputSocket().flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            for (WSocket vr : user.serverSocketList) {
                try {
                    int userId = vr.getInputSocket().readInt();
                    int userExponentX = vr.getInputSocket().readInt();
                    userX.put(userId, userExponentX);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            user.appendOutMessage("set X: " + userX);

            int previousUserZExpMaxUserAnd = MathRing.exponentiationRing(userZ.get(previousUser), randNumber * MaxUsers, allPrimeNumber);
            int ssesionKey = previousUserZExpMaxUserAnd;
            int nextUserCount = shiftIp;
            for(int i = MaxUsers - 1; i > -1; i --) {
                ssesionKey = (ssesionKey * MathRing.exponentiationRing(userX.get(nextUserCount), i, allPrimeNumber ) ) % allPrimeNumber;
                nextUserCount = ((nextUserCount + 1) >= MaxUsers) ? 0 : (nextUserCount + 1);
            }
            user.appendOutMessage("Сеансовый ключ: " + ssesionKey);

            for (WSocket vr : user.clientSocketList) {
                vr.startСommunication(ssesionKey);
            }

            for (WSocket vr : user.serverSocketList) {
                vr.startСommunication(ssesionKey);
            }

        }

        private class getServer extends Thread {
            private Chat user;
            public getServer(Chat user) {
                this.user = user;
            }

            public void run() {
                for(int i = 0; i < MaxUsers - 1; i ++) {
                    user.getSocketServer();
                    user.serverSocket = null;
                }
            }
        }

    }
}
