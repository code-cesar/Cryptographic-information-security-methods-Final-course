package com.math;

public class ElGamal {

    public static int [] Signature(String msg, int primitiveRoot, int primeNumber, int closeKey ){
        int[] arraySignatureElGamal  = new int[2];
        int hashMessage = Math.abs(msg.hashCode());
        int randomNumber = MathRing.randimIntFromTo(2, primeNumber - 1);
        int reverseRandomNumber = MathRing.returnElementRing(randomNumber, primeNumber - 1);
        while(MathRing.greatestCommonDivisor(randomNumber,primeNumber - 1) != 1 || (randomNumber * reverseRandomNumber) % (primeNumber - 1)  != 1)
        {
            randomNumber = MathRing.randimIntFromTo(2, primeNumber - 1);
            reverseRandomNumber = MathRing.returnElementRing(randomNumber,primeNumber - 1);
        }
        arraySignatureElGamal[0] = MathRing.exponentiationRing(primitiveRoot,randomNumber, primeNumber);
        int numberU = Math.floorMod((hashMessage - (closeKey * arraySignatureElGamal[0])), primeNumber - 1);
        arraySignatureElGamal[1] = (reverseRandomNumber * numberU) % (primeNumber - 1);
        return arraySignatureElGamal;
    }

    public static boolean isSignature(String msg, int calculationResult, int primitiveRoot, int primeNumber, int signatureR, int signatureS ){
        if(signatureR < 0 || signatureR >= primeNumber || signatureS < 0 || signatureS >= (primeNumber - 1) ) return false;
        int hashMessage = Math.abs(msg.hashCode());
        int leftComposition = ( MathRing.exponentiationRing(calculationResult,signatureR,primeNumber) *
                MathRing.exponentiationRing(signatureR,signatureS,primeNumber)) % primeNumber;
        int rightComposition =  MathRing.exponentiationRing(primitiveRoot, hashMessage, primeNumber);
        return leftComposition == rightComposition;
    }

    public static int [] EncrypA( int primitiveRoot, int primeNumber ){
        int[] arrayEncryptElGamal  = new int[2];
        arrayEncryptElGamal[0] = MathRing.randomMutuallySimpleNumber(primeNumber - 2);
        arrayEncryptElGamal[1] = MathRing.exponentiationRing(primitiveRoot, arrayEncryptElGamal[0], primeNumber);
        return arrayEncryptElGamal;
    }

    public static int EncryptB(int msg, int sessionKeyLocal, int calculationResult, int primeNumber ){
        return (msg * MathRing.exponentiationRing(calculationResult, sessionKeyLocal, primeNumber)) % primeNumber;
    }

    public static String EncryptB(String msg, int sessionKeyLocal, int calculationResult, int primeNumber ){
        String result = "";
        for(int i = 0; i < msg.length(); i++){
            result += (char)Integer.parseInt(String.valueOf(EncryptB(msg.charAt(i), sessionKeyLocal, calculationResult, primeNumber)));
        }
        return result;
    }

    public static int DencrypB(int msg, int firstEncryptText, int closeKey, int primeNumber ){
        return (msg * MathRing.returnElementRing( MathRing.exponentiationRing(firstEncryptText,closeKey,primeNumber),primeNumber)) % primeNumber;
    }

    public static String DencrypB(String msg, int firstEncryptText, int closeKey, int primeNumber ){
        String result = "";
        for(int i = 0; i < msg.length(); i++){
            result += (char)Integer.parseInt(String.valueOf(DencrypB(msg.charAt(i), firstEncryptText, closeKey, primeNumber)));
        }
        return result;
    }

}
