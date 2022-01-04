package com.cryptoProtocol;

import com.components.Chat;
import com.math.MathRing;
import java.io.IOException;

/*
Криптопротокол 1.2.
    Обмен ключами, использующий криптографию с открытым ключом
    Боб генерирует пару «открытый ключ / закрытый ключ» и открытый ключ отправляет Тренту.
    1. Алиса получает от Трента открытый ключ Боба.
    2. Алиса генерирует случайный сеансовый ключ K , шифрует его открытым ключом Боба и отправляет Бобу.
    3. Боб расшифровывает сообщение Алисы своим закрытым ключом.
    4. Алиса и Боб шифруют свои сообщения, используя сеансовый ключ K .
 */
public class KeyExchange implements IcryptoProtocol {

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
                int primeNumber = MathRing.generatorLargeNumber(1000, 10000, 10);
                bob.appendOutMessage("Простое число: " + primeNumber);
                int primitiveRoot  = MathRing.findPrimitive(primeNumber);
                bob.appendOutMessage("Первообразный корень: " + primitiveRoot);
                int closeKey = MathRing.randimIntFromTo(2, primeNumber - 2);
                bob.appendOutMessage("Закрытый ключ: " + closeKey);
                int calculationResult  = MathRing.exponentiationRing(primitiveRoot, closeKey, primeNumber);
                bob.appendOutMessage("g^a mod p = " + calculationResult);
                bob.connectServer(ip, port + shiftTrent);

                bob.getSocketClient().getOutputSocket().writeInt(calculationResult);
                bob.getSocketClient().getOutputSocket().writeInt(primitiveRoot);
                bob.getSocketClient().getOutputSocket().writeInt(primeNumber);

                bob.getSocketClient().getOutputSocket().flush();
                bob.closeClient();
                int firstEncryptText = bob.getSocketServer().getInputSocket().readInt();
                int secondEncryptText = bob.getSocketServer().getInputSocket().readInt();

                int sessionKeyGlobal = (secondEncryptText * MathRing.returnElementRing( MathRing.exponentiationRing(firstEncryptText,closeKey,primeNumber),primeNumber)) % primeNumber;
                bob.appendOutMessage("Сеансовый ключ K: " + sessionKeyGlobal);

                bob.getSocketServer().startСommunication(sessionKeyGlobal);

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
                trent.connectServer(ip, port + shiftAlice);
                int[] openKeyBobTrent = new int[3];
                for (int i = 0; i < openKeyBobTrent.length; i++) {
                    openKeyBobTrent[i] = trent.getSocketServer().getInputSocket().readInt();
                    trent.getSocketClient().getOutputSocket().writeInt(openKeyBobTrent[i]);
                }
                trent.appendOutMessage("Открытый ключ Боба: ( " + openKeyBobTrent[0] + "," + openKeyBobTrent[1] + "," + openKeyBobTrent[2] + ")");
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
                int calculationResult  = alice.getSocketServer().getInputSocket().readInt();
                int primitiveRoot  = alice.getSocketServer().getInputSocket().readInt();
                int primeNumber  = alice.getSocketServer().getInputSocket().readInt();
                alice.closeServer();

                alice.appendOutMessage("Открытый ключ Боба: ( " + calculationResult + "," + primitiveRoot + "," + primeNumber + ")");
                int sessionKeyGlobal = MathRing.randimIntFromTo(2, primeNumber - 2);
                alice.appendOutMessage("Сеансовый ключ K: " + sessionKeyGlobal);
                int sessionKeyLocal = MathRing.randomMutuallySimpleNumber(primeNumber - 2);
                alice.connectServer(ip, port + shiftBob);
                alice.getSocketClient().getOutputSocket().writeInt(MathRing.exponentiationRing(primitiveRoot, sessionKeyLocal, primeNumber));
                int EncryptMessage = (sessionKeyGlobal * MathRing.exponentiationRing(calculationResult, sessionKeyLocal, primeNumber)) % primeNumber;
                alice.getSocketClient().getOutputSocket().writeInt(EncryptMessage);

                alice.getSocketClient().startСommunication(sessionKeyGlobal);

            }catch (
                    IOException e) {
                e.printStackTrace();
            }
        }
    }
}
