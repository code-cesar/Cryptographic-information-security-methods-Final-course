package com.cryptoProtocol;

import com.Main;
import com.components.Chat;
import com.math.ElGamal;
import com.math.MathRing;
import com.wsocket.WSocket;

import java.io.IOException;

public class DenningSacco implements IcryptoProtocol {
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
    private int [] GetOpenKey(WSocket handelServer){
        int[] arrayOpenKey  = new int[3];
        try {
            for (int i = 0; i < arrayOpenKey.length; i++) {
                arrayOpenKey[i] = handelServer.getInputSocket().readInt();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return arrayOpenKey;
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
                bob.appendOutMessage("Открытый ключ: (" + calculationResult + "," + primitiveRoot + "," + primeNumber + ")");

                bob.connectServer(ip, port + shiftTrent); // Send OpenKeyBob Trent

                bob.getSocketClient().getOutputSocket().writeInt(calculationResult);
                bob.getSocketClient().getOutputSocket().writeInt(primitiveRoot);
                bob.getSocketClient().getOutputSocket().writeInt(primeNumber);

                bob.getSocketClient().getOutputSocket().flush();
                bob.closeClient();

                int [] openKeyTrent = GetOpenKey(bob.getSocketServer());
                bob.stopСommunicationServer();

                int sessionLocalKey = bob.getSocketServer().getInputSocket().readInt();

                int sessionKeyGlobal = ElGamal.DencrypB(bob.getSocketServer().getInputSocket().readInt(),sessionLocalKey, closeKey, primeNumber);
                long labelTime = Long.valueOf(ElGamal.DencrypB(bob.getSocketServer().getInputSocket().readUTF(),sessionLocalKey, closeKey, primeNumber));
                int [] signatureseSsionKeyAndLabelTime = new int[2];
                signatureseSsionKeyAndLabelTime[0] =  ElGamal.DencrypB(bob.getSocketServer().getInputSocket().readInt(),sessionLocalKey, closeKey, primeNumber);
                signatureseSsionKeyAndLabelTime[1] =  ElGamal.DencrypB(bob.getSocketServer().getInputSocket().readInt(),sessionLocalKey, closeKey, primeNumber);

                String nameBob = bob.getSocketServer().getInputSocket().readUTF();
                String nameBobAndOpenKeyBob = nameBob;
                int [] openKeyBob = GetOpenKey(bob.getSocketServer());
                for (int i = 0; i < openKeyBob.length; i++) {
                    nameBobAndOpenKeyBob += openKeyBob[i];
                }
                int keyBobR = bob.getSocketServer().getInputSocket().readInt();
                int keyBobS = bob.getSocketServer().getInputSocket().readInt();
                boolean isBob = ElGamal.isSignature(nameBobAndOpenKeyBob, openKeyTrent[0],openKeyTrent[1],openKeyTrent[2],keyBobR,keyBobS);

                bob.appendOutMessage("Подпись Боба " + nameBobAndOpenKeyBob +
                        (isBob ?
                                " соответствует" : " не соответствует") +
                        " Key: " + keyBobR + ", " + keyBobS);

                String nameAlice = bob.getSocketServer().getInputSocket().readUTF();
                String nameAliceAndOpenKeyAlice = nameAlice;
                int [] openKeyAlice = GetOpenKey(bob.getSocketServer());
                for (int i = 0; i < openKeyAlice.length; i++) {
                    nameAliceAndOpenKeyAlice = nameAliceAndOpenKeyAlice + openKeyAlice[i];
                }
                int keyAliceR = bob.getSocketServer().getInputSocket().readInt();
                int keyAliceS = bob.getSocketServer().getInputSocket().readInt();
                boolean isAlice = ElGamal.isSignature(nameAliceAndOpenKeyAlice, openKeyTrent[0],openKeyTrent[1],openKeyTrent[2],keyAliceR,keyAliceS);

                bob.appendOutMessage("Подпись Алисы " + nameAliceAndOpenKeyAlice +
                        (isAlice ?
                                " соответствует" : " не соответствует") +
                        " Key: " + keyAliceR + ", " + keyAliceS);

                String sessionKeyGlobalLabelTime = Integer.toString(sessionKeyGlobal) + labelTime;
                boolean isSessionKey = ElGamal.isSignature(sessionKeyGlobalLabelTime, openKeyAlice[0],openKeyAlice[1],openKeyAlice[2],signatureseSsionKeyAndLabelTime[0],signatureseSsionKeyAndLabelTime[1]);
                bob.appendOutMessage("Сенсовый ключ и метка времени " + sessionKeyGlobalLabelTime +
                        (isSessionKey ?
                                " соответствует" : " не соответствует") +
                        " Key: " + signatureseSsionKeyAndLabelTime[0] + ", " + signatureseSsionKeyAndLabelTime[1]);
                long labelTimeBob = System.currentTimeMillis() / 1000L;
                boolean isLabelTime = Math.abs(labelTimeBob - labelTime ) < 300;
                if(!isLabelTime)bob.appendOutMessage("Метка времени просрочена на 5 минут");
                if(isBob && isAlice && isSessionKey && isLabelTime) bob.getSocketServer().startСommunication(sessionKeyGlobal);

            } catch (IOException e) {
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
                trent.appendOutMessage("Простое число: " + primeNumber);
                int primitiveRoot  = MathRing.findPrimitive(primeNumber);
                trent.appendOutMessage("Первообразный корень: " + primitiveRoot);
                int closeKey = MathRing.randimIntFromTo(2, primeNumber - 2);
                trent.appendOutMessage("Закрытый ключ: " + closeKey);
                int calculationResult  = MathRing.exponentiationRing(primitiveRoot, closeKey, primeNumber);
                trent.appendOutMessage("g^a mod p = " + calculationResult);
                trent.appendOutMessage("Открытый ключ: (" + calculationResult + "," + primitiveRoot + "," + primeNumber + ")");
                int[] openKeyBob = null;
                int[] openKeyAlice = null;
                while(openKeyBob == null || openKeyAlice == null) {
                    WSocket getServer = trent.getSocketServer();
                    if (trent.partnerUsername.equals("Bob")) {
                        openKeyBob = GetOpenKey(getServer);
                        trent.stopСommunicationServer();
                    }

                    if (trent.partnerUsername.equals("Alice")) {
                        openKeyAlice = GetOpenKey(getServer);
                        trent.stopСommunicationServer();
                    }
                }
                trent.appendOutMessage("Открытый ключ Боба: (" + openKeyBob[0] + "," + openKeyBob[1] + "," + openKeyBob[2] + ")");
                trent.appendOutMessage("Открытый ключ Алисы: (" + openKeyAlice[0] + "," + openKeyAlice[1] + "," + openKeyAlice[2] + ")");

                trent.connectServer(ip, port + shiftAlice); // Send OpenKeyTrent Alice

                trent.getSocketClient().getOutputSocket().writeInt(calculationResult);
                trent.getSocketClient().getOutputSocket().writeInt(primitiveRoot);
                trent.getSocketClient().getOutputSocket().writeInt(primeNumber);

                trent.getSocketClient().getOutputSocket().flush();
                trent.closeClient();

                trent.connectServer(ip, port + shiftBob); // Send OpenKeyTrent Bob

                trent.getSocketClient().getOutputSocket().writeInt(calculationResult);
                trent.getSocketClient().getOutputSocket().writeInt(primitiveRoot);
                trent.getSocketClient().getOutputSocket().writeInt(primeNumber);

                trent.getSocketClient().getOutputSocket().flush();
                trent.closeClient();

                String nameAlice = trent.getSocketServer().getInputSocket().readUTF();
                String nameBob = trent.getSocketServer().getInputSocket().readUTF();
                trent.stopСommunicationServer();

                String nameBobAndOpenKeyBob = nameBob + openKeyBob[0] + openKeyBob[1] + openKeyBob[2];
                int [] signatureNameBobAndOpenKeyBob = ElGamal.Signature(nameBobAndOpenKeyBob,primitiveRoot,primeNumber,closeKey );

                String nameAliceAndOpenKeyAlice = nameAlice + openKeyAlice[0] + openKeyAlice[1] + openKeyAlice[2];
                int [] signaturenameAliceAndOpenKeyAlice = ElGamal.Signature(nameAliceAndOpenKeyAlice,primitiveRoot,primeNumber,closeKey );

                trent.connectServer(ip, port + shiftAlice); // Send Name Alice

                // Send Alice Message name Bob and Open Key Bob
                trent.getSocketClient().getOutputSocket().writeUTF(nameBob);
                for (int i = 0; i < openKeyBob.length; i++) {
                    trent.getSocketClient().getOutputSocket().writeInt(openKeyBob[i]);
                }
                // Send Alice Message signature name Bob and Open Key Bob
                trent.getSocketClient().getOutputSocket().writeInt(signatureNameBobAndOpenKeyBob[0]);
                trent.getSocketClient().getOutputSocket().writeInt(signatureNameBobAndOpenKeyBob[1]);

                // Send Alice Message name Alice and Open Key Alice
                trent.getSocketClient().getOutputSocket().writeUTF(nameAlice);
                for (int i = 0; i < openKeyAlice.length; i++) {
                    trent.getSocketClient().getOutputSocket().writeInt(openKeyAlice[i]);
                }
                // Send Alice Message signature name Alice and Open Key Alice
                trent.getSocketClient().getOutputSocket().writeInt(signaturenameAliceAndOpenKeyAlice[0]);
                trent.getSocketClient().getOutputSocket().writeInt(signaturenameAliceAndOpenKeyAlice[1]);

                trent.getSocketClient().getOutputSocket().flush();
                trent.appendOutMessage("Подпись Боба " + nameBobAndOpenKeyBob + " Key: " + signatureNameBobAndOpenKeyBob[0] + ", " + signatureNameBobAndOpenKeyBob[1] +
                        " Is: " + (ElGamal.isSignature(nameBobAndOpenKeyBob, calculationResult,primitiveRoot,primeNumber,signatureNameBobAndOpenKeyBob[0],signatureNameBobAndOpenKeyBob[1])));
                trent.appendOutMessage("Подпись Алисы " + nameAliceAndOpenKeyAlice + " Key: " + signaturenameAliceAndOpenKeyAlice[0] + ", " + signaturenameAliceAndOpenKeyAlice[1] +
                        " Is: " + (ElGamal.isSignature(nameAliceAndOpenKeyAlice, calculationResult,primitiveRoot,primeNumber,signaturenameAliceAndOpenKeyAlice[0],signaturenameAliceAndOpenKeyAlice[1])));
                trent.closeClient();
                trent.closeServer();


            } catch (IOException e) {
                e.printStackTrace();
            }




        }
    }

    private class Alice extends Thread {
        @Override
        public void run() {
            try {
                Chat alice = new Chat("Alice").startServer(port + shiftAlice);
                int primeNumber = MathRing.generatorLargeNumber(1000, 10000, 10);
                alice.appendOutMessage("Простое число: " + primeNumber);
                int primitiveRoot  = MathRing.findPrimitive(primeNumber);
                alice.appendOutMessage("Первообразный корень: " + primitiveRoot);
                int closeKey = MathRing.randimIntFromTo(2, primeNumber - 2);
                alice.appendOutMessage("Закрытый ключ: " + closeKey);
                int calculationResult  = MathRing.exponentiationRing(primitiveRoot, closeKey, primeNumber);
                alice.appendOutMessage("g^a mod p = " + calculationResult);
                alice.appendOutMessage("Открытый ключ: (" + calculationResult + "," + primitiveRoot + "," + primeNumber + ")");

                alice.connectServer(ip, port + shiftTrent); // Send OpenKeyAlice Trent

                alice.getSocketClient().getOutputSocket().writeInt(calculationResult);
                alice.getSocketClient().getOutputSocket().writeInt(primitiveRoot);
                alice.getSocketClient().getOutputSocket().writeInt(primeNumber);

                alice.getSocketClient().getOutputSocket().flush();
                alice.closeClient();

                int [] openKeyTrent = GetOpenKey(alice.getSocketServer());
                alice.stopСommunicationServer();

                alice.connectServer(ip, port + shiftTrent); // Send Name Alice and Bob

                alice.getSocketClient().getOutputSocket().writeUTF(alice.username);
                alice.getSocketClient().getOutputSocket().writeUTF("Bob");

                alice.getSocketClient().getOutputSocket().flush();
                alice.closeClient();

                //Read Message Trent

                String nameBob = alice.getSocketServer().getInputSocket().readUTF();
                String nameBobAndOpenKeyBob = nameBob;
                int [] openKeyBob = GetOpenKey(alice.getSocketServer());
                for (int i = 0; i < openKeyBob.length; i++) {
                    nameBobAndOpenKeyBob += openKeyBob[i];
                }
                int keyBobR = alice.getSocketServer().getInputSocket().readInt();
                int keyBobS = alice.getSocketServer().getInputSocket().readInt();

                alice.appendOutMessage("Подпись Боба " + nameBobAndOpenKeyBob +
                        (ElGamal.isSignature(nameBobAndOpenKeyBob, openKeyTrent[0],openKeyTrent[1],openKeyTrent[2],keyBobR,keyBobS) ?
                        " соответствует" : " не соответствует") +
                        " Key: " + keyBobR + ", " + keyBobS);

                String nameAlice = alice.getSocketServer().getInputSocket().readUTF();
                String nameAliceAndOpenKeyAlice = nameAlice;
                int [] openKeyAlice = GetOpenKey(alice.getSocketServer());
                for (int i = 0; i < openKeyAlice.length; i++) {
                    nameAliceAndOpenKeyAlice = nameAliceAndOpenKeyAlice + openKeyAlice[i];
                }
                int keyAliceR = alice.getSocketServer().getInputSocket().readInt();
                int keyAliceS = alice.getSocketServer().getInputSocket().readInt();

                alice.appendOutMessage("Подпись Алисы " + nameAliceAndOpenKeyAlice +
                        (ElGamal.isSignature(nameAliceAndOpenKeyAlice, openKeyTrent[0],openKeyTrent[1],openKeyTrent[2],keyAliceR,keyAliceS) ?
                                " соответствует" : " не соответствует") +
                        " Key: " + keyAliceR + ", " + keyAliceS);
                alice.stopСommunicationServer();

                int sessionKeyGlobal = MathRing.randimIntFromTo(2, primeNumber - 2);
                long labelTime = System.currentTimeMillis() / 1000L;
                String sessionKeyGlobalLabelTime = Integer.toString(sessionKeyGlobal) + labelTime;
                int [] signatureseSsionKeyAndLabelTime = ElGamal.Signature(sessionKeyGlobalLabelTime,primitiveRoot,primeNumber,closeKey );
                alice.appendOutMessage("Сенасовый ключ " + sessionKeyGlobal);
                alice.appendOutMessage("Метка времени " + labelTime);
                alice.appendOutMessage("Подпись " + sessionKeyGlobalLabelTime + " KeyR: " + signatureseSsionKeyAndLabelTime[0]  +
                        " KeyS: " +  signatureseSsionKeyAndLabelTime[1]);
                alice.connectServer(ip, port + shiftBob); // Send sessionKeyGlobal, labelTime; OpenKey, Name Alice and Bob
                int [] sessionLocalKey = ElGamal.EncrypA(openKeyBob[1],openKeyBob[2]);
                alice.getSocketClient().getOutputSocket().writeInt(sessionLocalKey[1]); // Send Bob Message Encrypt First

                // Send Bob Message Encrypt Second
                alice.getSocketClient().getOutputSocket().writeInt(ElGamal.EncryptB(sessionKeyGlobal,sessionLocalKey[0],openKeyBob[0],openKeyBob[2]));
                alice.getSocketClient().getOutputSocket().writeUTF(ElGamal.EncryptB(String.valueOf(labelTime),sessionLocalKey[0],openKeyBob[0],openKeyBob[2]));
                alice.getSocketClient().getOutputSocket().writeInt(ElGamal.EncryptB(signatureseSsionKeyAndLabelTime[0],sessionLocalKey[0],openKeyBob[0],openKeyBob[2]));
                alice.getSocketClient().getOutputSocket().writeInt(ElGamal.EncryptB(signatureseSsionKeyAndLabelTime[1],sessionLocalKey[0],openKeyBob[0],openKeyBob[2]));

                // Send Bob Message name Bob and Open Key Bob
                alice.getSocketClient().getOutputSocket().writeUTF(nameBob);
                for (int i = 0; i < openKeyBob.length; i++) {
                    alice.getSocketClient().getOutputSocket().writeInt(openKeyBob[i]);
                }
                // Send Bob Message signature name Bob and Open Key Bob
                alice.getSocketClient().getOutputSocket().writeInt(keyBobR);
                alice.getSocketClient().getOutputSocket().writeInt(keyBobS);

                // Send Bob Message name Alice and Open Key Alice
                alice.getSocketClient().getOutputSocket().writeUTF(nameAlice);
                for (int i = 0; i < openKeyAlice.length; i++) {
                    alice.getSocketClient().getOutputSocket().writeInt(openKeyAlice[i]);
                }
                // Send Bob Message signature name Alice and Open Key Alice
                alice.getSocketClient().getOutputSocket().writeInt(keyAliceR);
                alice.getSocketClient().getOutputSocket().writeInt(keyAliceS);

                alice.getSocketClient().startСommunication(sessionKeyGlobal);


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
