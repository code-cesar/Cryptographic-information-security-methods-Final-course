package com.cryptoProtocol;

import com.components.Chat;
import com.math.MathRing;
import java.io.IOException;
/* Yahalom
Алиса и Трент используют общий секретный ключ AK Боб и Трент используют общий секретный ключ BK

1. Алиса объединяет свое имя и случайное число и отправляет созданное сообщение Бобу: AAR
2. Боб объединяет имя Алисы, ее случайное число, свое случайное число, шифрует созданное сообщение общим с Трентом ключом и результат посылает Тренту, добавляя свое имя:  BKABBEARR

3. Трент создает два сообщения. Первое включает имя Боба, случайный сеансовый ключ K , случайные числа Боба и Алисы, оно шифруется ключом, общим для Трента и Алисы. Второе состоит из имени Алисы, случайного сеансового ключа, оно шифруется ключом, общим для
Трента и Боба. Трент посылает оба сообщения Алисе:AKABEBKRR

4. Алиса расшифровывает первое сообщение, извлекает K и убеждается, что AR совпадает со значением, отправленным на этапе 1.
Алиса посылает Бобу два сообщения. Одним является сообщение Трента, зашифрованное ключом Боба. Второе - это BR, зашифрованное сеансовым ключом: (,) BKEAK , () KBER

5. Боб расшифровывает первое сообщение, извлекает K и убеждается, что BR совпадает с отправленным на этапе 2.
6. Алиса и Боб шифруют свои сообщения, используя сеансовый ключ
 */
public class Yahalom implements IcryptoProtocol {
    private final static int shiftBob = 0;
    private final static int shiftTrent = 1;
    private final static int shiftAlice = 2;

    private int port;
    private String ip;

    public void action(String ip, int port){
        this.ip = ip;
        this.port = port;
        new Trent().start();
        new Alice().start();
        new Bob().start();
    }
    private class Bob extends Thread {

        @Override
        public void run() {
            try {
                Chat bob = new Chat("Bob").startServer(port + shiftBob);
                int closeKey = bob.getSocketServer().getInputSocket().readInt();
                bob.stopСommunicationServer();
                bob.appendOutMessage( " Закрытый ключ: " + closeKey);
                int randNumber = MathRing.randimIntFromTo(0,99999);
                bob.appendOutMessage( " Случайное число: " + randNumber);

                String getMessageNameAlice = bob.getSocketServer().getInputSocket().readUTF();
                int getRandNumberAlice = bob.getSocketServer().getInputSocket().readInt();
                bob.stopСommunicationServer();
                bob.appendOutMessage( " Случайное число от Алисы: " + getRandNumberAlice);
                bob.connectServer(ip,port + shiftTrent);
                bob.getSocketClient().getOutputSocket().writeUTF(bob.username);
                bob.getSocketClient().getOutputSocket().writeUTF(MathRing.ssesionXor(getMessageNameAlice, closeKey ));
                bob.getSocketClient().getOutputSocket().writeInt(MathRing.ssesionXor(getRandNumberAlice, closeKey ));
                bob.getSocketClient().getOutputSocket().writeInt(MathRing.ssesionXor(randNumber, closeKey ));
                bob.getSocketClient().getOutputSocket().flush();
                bob.closeClient();

                String nameAlice = MathRing.ssesionXor(bob.getSocketServer().getInputSocket().readUTF(), closeKey);
                bob.appendOutMessage(nameAlice);
                int sessionKey = MathRing.ssesionXor(bob.getSocketServer().getInputSocket().readInt(), closeKey);
                bob.appendOutMessage( "Сеансовый ключ: " + sessionKey);
                int randBob =  MathRing.ssesionXor(bob.getSocketServer().getInputSocket().readInt(), sessionKey);
                if(randNumber == randBob) bob.appendOutMessage( randNumber + " == " + randBob);
                bob.getSocketServer().startСommunication(sessionKey);

            }catch (
                    IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class Trent extends Thread {
        @Override
        public void run() {
            try {
                Chat trent = new Chat("Trent").startServer(port + shiftTrent);
                int primeNumber = MathRing.generatorLargeNumber(1000, 10000, 10);
                int closeKeyBob = MathRing.randimIntFromTo(2, primeNumber - 2);
                int closeKeyAlice = MathRing.randimIntFromTo(2, primeNumber - 2);
                trent.connectServer(ip, port + shiftBob);
                trent.getSocketClient().getOutputSocket().writeInt(closeKeyBob);
                trent.getSocketClient().getOutputSocket().flush();
                trent.closeClient();
                trent.connectServer(ip, port + shiftAlice);
                trent.getSocketClient().getOutputSocket().writeInt(closeKeyAlice);
                trent.getSocketClient().getOutputSocket().flush();
                trent.closeClient();
                trent.appendOutMessage( " Закрытый ключ Боба: " + closeKeyBob);
                trent.appendOutMessage( " Закрытый ключ Алисы: " + closeKeyAlice);

                String getMessageNameBob = trent.getSocketServer().getInputSocket().readUTF();
                String getMessageNameAlice = MathRing.ssesionXor(trent.getSocketServer().getInputSocket().readUTF(), closeKeyBob);
                int getRandNumberAlice = MathRing.ssesionXor(trent.getSocketServer().getInputSocket().readInt(), closeKeyBob);
                int getRandNumberBob = MathRing.ssesionXor(trent.getSocketServer().getInputSocket().readInt(), closeKeyBob);
                int ssesionKey = MathRing.randimIntFromTo(2, primeNumber - 2);
                trent.appendOutMessage("Сеансовый ключ: " + ssesionKey);
                trent.appendOutMessage( getMessageNameBob + "," + getMessageNameAlice + " Ra:" + getRandNumberAlice + " Rb" + getRandNumberBob);

                trent.connectServer(ip, port + shiftAlice);
                trent.getSocketClient().getOutputSocket().writeUTF(MathRing.ssesionXor(getMessageNameBob, closeKeyAlice ));
                trent.getSocketClient().getOutputSocket().writeInt(MathRing.ssesionXor(ssesionKey, closeKeyAlice ));
                trent.getSocketClient().getOutputSocket().writeInt(MathRing.ssesionXor(getRandNumberAlice, closeKeyAlice ));
                trent.getSocketClient().getOutputSocket().writeInt(MathRing.ssesionXor(getRandNumberBob, closeKeyAlice ));

                trent.getSocketClient().getOutputSocket().writeUTF(MathRing.ssesionXor(getMessageNameAlice, closeKeyBob ));
                trent.getSocketClient().getOutputSocket().writeInt(MathRing.ssesionXor(ssesionKey, closeKeyBob ));
                trent.getSocketClient().getOutputSocket().flush();
                trent.closeClient();
                trent.closeServer();

            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class Alice extends Thread {
        @Override
        public void run() {
            try {
                Chat alice = new Chat("Alice").startServer(port + shiftAlice);
                int closeKey = alice.getSocketServer().getInputSocket().readInt();
                alice.stopСommunicationServer();
                alice.appendOutMessage( " Закрытый ключ: " + closeKey);
                alice.connectServer(ip,port + shiftBob);
                alice.getSocketClient().getOutputSocket().writeUTF(alice.username);
                int randNumber = MathRing.randimIntFromTo(0,99999);
                alice.getSocketClient().getOutputSocket().writeInt(randNumber);
                alice.appendOutMessage( " Случайное число: " + randNumber);
                alice.getSocketClient().getOutputSocket().flush();
                alice.closeClient();

                String nameBob = MathRing.ssesionXor(alice.getSocketServer().getInputSocket().readUTF(), closeKey);
                alice.appendOutMessage(nameBob);
                int sessionKey = MathRing.ssesionXor(alice.getSocketServer().getInputSocket().readInt(), closeKey);
                alice.appendOutMessage( "Сеансовый ключ: " + sessionKey);
                int randAlice = MathRing.ssesionXor(alice.getSocketServer().getInputSocket().readInt(), closeKey);
                if(randNumber == randAlice)alice.appendOutMessage( randNumber + " == " + randAlice);
                int randBob = MathRing.ssesionXor(alice.getSocketServer().getInputSocket().readInt(), closeKey);

                alice.connectServer(ip,port + shiftBob);
                alice.getSocketClient().getOutputSocket().writeUTF(alice.getSocketServer().getInputSocket().readUTF());
                alice.getSocketClient().getOutputSocket().writeInt(alice.getSocketServer().getInputSocket().readInt());
                alice.getSocketClient().getOutputSocket().writeInt(MathRing.ssesionXor(randBob, sessionKey));
                alice.getSocketClient().getOutputSocket().flush();

                alice.getSocketClient().startСommunication(sessionKey);
            }catch (
                    IOException e) {
                e.printStackTrace();
            }
        }
    }
}
