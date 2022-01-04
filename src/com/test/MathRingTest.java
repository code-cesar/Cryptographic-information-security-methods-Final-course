package com.test;

import com.math.MathRing;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

class MathRingTest {

    @org.junit.jupiter.api.Test
    void exponentiationRing() {
        assertEquals(9, MathRing.exponentiationRing(3,2,10));
        assertEquals(2, MathRing.exponentiationRing(5,1,3));
    }

    @org.junit.jupiter.api.Test
    void greatestCommonDivisor() {
        assertEquals(30, MathRing.greatestCommonDivisor(180, 150));
        assertEquals(1, MathRing.greatestCommonDivisor(500000, 23451));
    }

    @org.junit.jupiter.api.Test
    void returnElementRing() {
        assertEquals(7, MathRing.returnElementRing(15, 26));
    }

    @org.junit.jupiter.api.Test
    void isPrime() {
        assertEquals(true, MathRing.isPrime(11, 3));
    }

    @org.junit.jupiter.api.Test
    void generatorLargeNumber() {
        System.out.println("Простое сгенерированное число: " + MathRing.generatorLargeNumber(1300,3444, 15));
    }
}