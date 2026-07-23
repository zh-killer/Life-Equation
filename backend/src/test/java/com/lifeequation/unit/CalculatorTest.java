package com.lifeequation.unit;

import com.lifeequation.utils.Calculator;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 核心功能演示测试类 — 测试一个简单的计算器逻辑。
 *
 * 对应文档 7.2 第一步：JUnit 5 基础测试 (CalculatorTest)
 */
@DisplayName("计算器工具类单元测试")
class CalculatorTest {

    private Calculator calculator;

    /**
     * @BeforeEach：每个 @Test 方法执行之前运行。
     * 用于初始化测试环境，重置状态，保证每个测试方法的独立性。
     */
    @BeforeEach
    void setUp() {
        calculator = new Calculator();
        System.out.println("@BeforeEach: 初始化计算器实例...");
    }

    /**
     * @AfterEach：每个 @Test 方法执行之后运行。
     * 用于执行清理工作，释放资源。
     */
    @AfterEach
    void tearDown() {
        calculator = null;
        System.out.println("@AfterEach: 清理计算器实例...");
    }

    // ==================== 加法测试 ====================

    @Test
    @DisplayName("测试两正数相加")
    void testAddition() {
        System.out.println(">>> 执行测试: testAddition");
        int result = calculator.add(5, 3);
        assertEquals(8, result, "5 + 3 应该等于 8");
    }

    @Test
    @DisplayName("测试负数加法")
    void testAdditionWithNegative() {
        int result = calculator.add(-5, 3);
        assertEquals(-2, result, "-5 + 3 应该等于 -2");
    }

    // ==================== 减法测试 ====================

    @Test
    @DisplayName("测试减法运算")
    void testSubtraction() {
        int result = calculator.subtract(10, 4);
        assertEquals(6, result, "10 - 4 应该等于 6");
    }

    // ==================== 乘法测试 ====================

    @Test
    @DisplayName("测试乘法运算")
    void testMultiplication() {
        int result = calculator.multiply(6, 7);
        assertEquals(42, result, "6 * 7 应该等于 42");
    }

    // ==================== 除法测试 ====================

    @Test
    @DisplayName("测试除法 — 正常除法")
    void testDivision() {
        int result = calculator.divide(10, 2);
        assertEquals(5, result, "10 / 2 应该等于 5");
    }

    @Test
    @DisplayName("测试除法 — 除数为零应抛出异常")
    void testDivisionByZero() {
        // assertThrows：验证执行指定代码时抛出预期异常
        ArithmeticException exception = assertThrows(
                ArithmeticException.class,
                () -> calculator.divide(10, 0),
                "除数为零时应抛出 ArithmeticException"
        );
        assertEquals("除数不能为零", exception.getMessage());
    }

    // ==================== 圆面积测试 ====================

    @Test
    @DisplayName("测试圆面积 — 正半径")
    void testCircleArea() {
        double result = calculator.computeCircleArea(2.0);
        // assertEquals 重载：expected, actual, delta（浮点数容许误差）
        assertEquals(Math.PI * 4, result, 0.001, "半径为2的圆面积应为4π");
    }

    @Test
    @DisplayName("测试圆面积 — 负数半径应抛出异常")
    void testCircleAreaNegativeRadius() {
        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.computeCircleArea(-1.0),
                "负半径应抛出 IllegalArgumentException"
        );
    }

    // ==================== 组合断言测试 ====================

    @Test
    @DisplayName("测试组合断言 — assertAll 批量验证")
    void testMultipleAssertions() {
        double result = calculator.computeCircleArea(2.0);

        // assertAll：批量执行多个断言，所有断言都会执行，不会因一个失败而终止
        assertAll("圆面积计算验证",
                () -> assertTrue(result > 0, "面积应为正数"),
                () -> assertEquals(Math.PI * 4, result, 0.001, "半径为2的圆面积应为4π"),
                () -> assertNotEquals(0, result, "面积不应为零")
        );
    }
}
