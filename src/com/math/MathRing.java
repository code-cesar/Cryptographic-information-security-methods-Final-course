package com.math;

import java.util.HashSet;

public class MathRing {

    public static int exponentiationRing(int x, int y, int p)
    {
        int res = 1; // Initialize result

        x = x % p; // Update x if it is more than or
        // equal to p

        if (x == 0)
            return 0; // In case x is divisible by p;

        while (y > 0)
        {
            // If y is odd, multiply x with result
            if ((y & 1) != 0)
                res = (res * x) % p;

            // y must be even now
            y = y >> 1; // y = y/2
            x = (x * x) % p;
        }
        return res;
    }

    public static float exponentiationRing(float x, float y, int p)
    {
        float res = 1; // Initialize result

        x = x % p; // Update x if it is more than or
        // equal to p

        if (x == 0)
            return 0; // In case x is divisible by p;

        while (y > 0)
        {
            // If y is odd, multiply x with result
            if (((int)y & 1) != 0)
                res = (res * x) % p;

            // y must be even now
            y = (int)y >> 1; // y = y/2
            x = (x * x) % p;
        }
        return res;
    }

    public static int greatestCommonDivisor (int p, int q) {
        if (q == 0) return p;
        if (p == 0) return q;

        // p and q even
        if ((p & 1) == 0 && (q & 1) == 0) return greatestCommonDivisor(p >> 1, q >> 1) << 1;

            // p is even, q is odd
        else if ((p & 1) == 0) return greatestCommonDivisor(p >> 1, q);

            // p is odd, q is even
        else if ((q & 1) == 0) return greatestCommonDivisor(p, q >> 1);

            // p and q odd, p >= q
        else if (p >= q) return greatestCommonDivisor((p-q) >> 1, q);

            // p and q odd, p < q
        else return greatestCommonDivisor(p, (q-p) >> 1);
    }

    public static int  randomMutuallySimpleNumber(int n){
        int r = randimIntFromTo(0,n), t;
        while ((t = greatestCommonDivisor(r, n)) > 1)
            r /= t;
        return r;
    }

    public static int returnElementRing (int a, int m){ // нахождение обратного элемента a^-1
        a = a % m; // делим по модулю число a
        for (int x = 1; x < m; x++)
            if ((a * x) % m == 1)return x;  // перебором находим обратный элемент
        return 1;
    }

    private static boolean isTestRabinMiller(int number, int mod){
        if(mod <= 4) return false;
        int randNumber = 2 + (int)(Math.random() % (mod - 4)); // Случайное число в диапозоне [2, mod - 2 ]
        int moduloNumber = exponentiationRing(randNumber, number, mod);
        if( moduloNumber == 1 || moduloNumber == mod - 1) return true;
        while ( number != mod - 1){
            moduloNumber = (moduloNumber * moduloNumber) % mod;
            number = number * 2;
            if( moduloNumber == 1 ) return false;
            if( moduloNumber == mod - 1 ) {
                return true;
            }
        }

        return false;
    }

    public static boolean isPrime(int n, int k) {
        if (n <= 1 || n == 4) return false;
        if (n <= 3) return true;
        int d = n - 1;
        while (d % 2 == 0)
            d /= 2;

        for (int i = 0; i < k; i++)
            if (!isTestRabinMiller(d, n))
                return false;
        return true;
    }

    public static int generatorLargeNumber( int min, int max, int k){
        int i = min + (int)(Math.random() * max);
        for(; i <= max; i ++) {
            if(isPrime(i, k)) return i;
        }
        for(; i >= min; i --) {
            if(isPrime(i, k)) return i;
        }
        return -1;
    }

    public static int randimIntFromTo(int from, int before){
        return from + (int) (Math.random() * before);
    }

    public static String ssesionXor(String text, int ssesionKey){
        String result = "";
        for(int i = 0; i < text.length(); i++){
            result += (char)Integer.parseInt(String.valueOf(text.charAt(i) ^ ssesionKey));
        }
        return result;
    }

    public static int ssesionXor(int number, int ssesionKey){
        return number ^ ssesionKey;
    }

    // Utility function to store prime factors of a number
    public static void findPrimefactors(HashSet<Integer> s, int n)
    {
        // Print the number of 2s that divide n
        while (n % 2 == 0)
        {
            s.add(2);
            n = n / 2;
        }

        // n must be odd at this point. So we can skip
        // one element (Note i = i +2)
        for (int i = 3; i <= Math.sqrt(n); i = i + 2)
        {
            // While i divides n, print i and divide n
            while (n % i == 0)
            {
                s.add(i);
                n = n / i;
            }
        }

        // This condition is to handle the case when
        // n is a prime number greater than 2
        if (n > 2)
        {
            s.add(n);
        }
    }

    // Function to find smallest primitive root of n
    public static int findPrimitive(int n)
    {
        HashSet<Integer> s = new HashSet<Integer>();

        // Check if n is prime or not
        if (isPrime(n,5) == false)
        {
            return -1;
        }

        // Find value of Euler Totient function of n
        // Since n is a prime number, the value of Euler
        // Totient function is n-1 as there are n-1
        // relatively prime numbers.
        int phi = n - 1;

        // Find prime factors of phi and store in a set
        findPrimefactors(s, phi);

        // Check for every number from 2 to phi
        for (int r = 2; r <= phi; r++)
        {
            // Iterate through all prime factors of phi.
            // and check if we found a power with value 1
            boolean flag = false;
            for (Integer a : s)
            {

                // Check if r^((phi)/primefactors) mod n
                // is 1 or not
                if (exponentiationRing(r, phi / (a), n) == 1)
                {
                    flag = true;
                    break;
                }
            }

            // If there was no power with value 1.
            if (flag == false)
            {
                return r;
            }
        }

        // If no primitive root found
        return -1;
    }


}
