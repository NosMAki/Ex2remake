import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.HashSet;

class MainTest {

    @Test
    void testIsNumber() {
        assertTrue(Main.isNumber("123")); // Valid integer
        assertTrue(Main.isNumber("45.67")); // Valid float
        assertFalse(Main.isNumber("abc")); // Invalid string
        assertFalse(Main.isNumber("")); // Empty string
        assertFalse(Main.isNumber(null)); // Null input
    }

    @Test
    void testIsText() {
        assertTrue(Main.isText("hello")); // Plain text
        assertFalse(Main.isText("123")); // Number
        assertFalse(Main.isText("=A1+B2")); // Formula
        assertTrue(Main.isText("some text")); // Text with spaces
        assertFalse(Main.isText("")); // Empty string
        assertFalse(Main.isText(null)); // Null input
    }

    @Test
    void testIsForm() {
        assertTrue(Main.isForm("=A1+B2")); // Valid formula
        assertTrue(Main.isForm("=5+3")); // Valid formula with numbers
        assertFalse(Main.isForm("A1+B2")); // Missing '='
        assertFalse(Main.isForm("=")); // Only '='
        assertFalse(Main.isForm("")); // Empty string
        assertFalse(Main.isForm(null)); // Null input
    }

    @Test
    void testComputeForm() {
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        sheet.set(0, 0, "5"); // A1 = 5
        sheet.set(1, 1, "10"); // B2 = 10

        double result = Main.computeForm("=A1+B2", sheet, new HashSet<>());
        assertEquals(15.0, result); // A1 + B2 = 15

        result = Main.computeForm("=10+5-3", sheet, new HashSet<>());
        assertEquals(12.0, result); // 10 + 5 - 3 = 12

        result = Main.computeForm("=5*6/3", sheet, new HashSet<>());
        assertEquals(10.0, result); // 5 * 6 / 3 = 10

        result = Main.computeForm("=5+(6*2)", sheet, new HashSet<>());
        assertEquals(17.0, result); // 5 + (6 * 2) = 17
    }

    @Test
    void testCellReferenceInComputeForm() {
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        sheet.set(0, 0, "5"); // A1 = 5
        sheet.set(1, 1, "10"); // B2 = 10
        sheet.set(2, 2, "=A1+B2"); // C3 = A1 + B2

        double result = Main.computeForm("=C3*2", sheet, new HashSet<>());
        assertEquals(30.0, result); // C3 (15) * 2 = 30
    }

    @Test
    void testCircularReference() {
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        sheet.set(0, 0, "=A1"); // A1 references itself

        StackOverflowError exception = assertThrows(StackOverflowError.class, () -> {
            Main.computeForm("=A1", sheet, new HashSet<>());
        });
        assertEquals("ERR_RECUR", exception.getMessage()); // Check for circular reference error
    }


    @Test
    void testComplexFormulas() {
        Ex2Sheet sheet = new Ex2Sheet(5, 5);
        sheet.set(0, 0, "10"); // A1 = 10
        sheet.set(1, 1, "=A1+20"); // B2 = A1 + 20 = 30
        sheet.set(2, 2, "=B2/2"); // C3 = B2 / 2 = 15

        double result = Main.computeForm("=C3*4", sheet, new HashSet<>());
        assertEquals(60.0, result); // C3 (15) * 4 = 60
    }

    @Test
    void testNegativeValuesAndParentheses() {
        Ex2Sheet sheet = new Ex2Sheet(5, 5);

        // Test direct negative numbers
        assertEquals(-5.0, Main.computeForm("=-5", sheet, new HashSet<>()));

        // Test parentheses
        assertEquals(-10.0, Main.computeForm("=-(5+5)", sheet, new HashSet<>()));

        // Test cell reference with negative
        sheet.set(0, 0, "5"); // A1 = 5
        assertEquals(-5.0, Main.computeForm("=-A1", sheet, new HashSet<>()));

        // Test combination of cell reference and parentheses
        sheet.set(1, 1, "=A1+5"); // B2 = A1 + 5 = 10
        assertEquals(-10.0, Main.computeForm("=-(B2)", sheet, new HashSet<>()));

        // Test nested parentheses
        assertEquals(-15.0, Main.computeForm("=-(A1+(5*2))", sheet, new HashSet<>()));
    }

    @Test
    void testCellReferences() {
        Ex2Sheet sheet = new Ex2Sheet(5, 5);

        // Test simple number in cell
        sheet.set(0, 0, "5");  // Put 5 in A0
        assertEquals("5", sheet.value(0, 0)); // Should display as 5
        assertEquals(Ex2Utils.NUMBER, sheet.get(0, 0).getType()); // Should be NUMBER type

        // Test reference to that cell
        sheet.set(0, 1, "=A0");  // Put =A0 in A1
        assertEquals("5", sheet.value(0, 1)); // Should display as 5
        assertEquals(Ex2Utils.FORM, sheet.get(0, 1).getType()); // Should be FORM type

        // Test invalid reference
        sheet.set(0, 2, "=Z9"); // Reference to non-existent cell
        assertEquals(Ex2Utils.ERR_FORM, sheet.value(0, 2)); // Should show error

        // Test circular reference
        sheet.set(1, 0, "=B1"); // B0 references B1
        sheet.set(1, 1, "=B0"); // B1 references B0
        assertEquals(Ex2Utils.ERR_CYCLE_FORM, sheet.get(1, 0).getType()); // Should detect cycle
    }

}
