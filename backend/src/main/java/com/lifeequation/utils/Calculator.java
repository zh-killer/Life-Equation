package com.lifeequation.utils;

/**
 * 简单计算器工具类 — 用于演示 JUnit 5 单元测试。
 */
public class Calculator {

    public int add(int a, int b) {
        return a + b;
    }

    public int subtract(int a, int b) {
        return a - b;
    }

    public int multiply(int a, int b) {
        return a * b;
    }

    public int divide(int a, int b) {
        if (b == 0) {
            throw new ArithmeticException("除数不能为零");
        }
        return a / b;
    }

    public double computeCircleArea(double radius) {
        if (radius < 0) {
            throw new IllegalArgumentException("半径不能为负数");
        }
        return Math.PI * radius * radius;
    }
}
