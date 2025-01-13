import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class Tests {
    private Sheet sheet;
    private Cell cell;
    private static final double DELTA = 0.001;

    @BeforeEach
    void setUp() {
        sheet = new Ex2Sheet();
        cell = new SCell("", sheet);
    }

    // =================== SCell Tests ===================
    @Test
    void testSCellConstructor() {
        Cell cell = new SCell("42", sheet);
        assertEquals("42", cell.getData());
        assertEquals(Ex2Utils.NUMBER, cell.getType());
    }

    @Test
    void testGetData() {
        cell.setData("Test");
        assertEquals("Test", cell.getData());
    }

    @Test
    void testSetData() {
        cell.setData("42.5");
        assertEquals("42.5", cell.getData());
        assertEquals(Ex2Utils.NUMBER, cell.getType());

        cell.setData("Text");
        assertEquals("Text", cell.getData());
        assertEquals(Ex2Utils.TEXT, cell.getType());

        cell.setData("=A1+B1");
        assertEquals("=A1+B1", cell.getData());
        assertEquals(Ex2Utils.FORM, cell.getType());
    }

    @Test
    void testGetSetType() {
        cell.setType(Ex2Utils.NUMBER);
        assertEquals(Ex2Utils.NUMBER, cell.getType());

        cell.setType(Ex2Utils.TEXT);
        assertEquals(Ex2Utils.TEXT, cell.getType());
    }

    @Test
    void testGetSetOrder() {
        cell.setOrder(5);
        assertEquals(5, cell.getOrder());
    }

    // =================== CellEntry Tests ===================
    @Test
    void testCellEntryValidation() {
        CellEntry entry1 = new CellEntry("A1");
        assertTrue(entry1.isValid());
        assertEquals(0, entry1.getX());
        assertEquals(1, entry1.getY());

        CellEntry entry2 = new CellEntry("Z99");
        assertFalse(entry2.isValid());

        CellEntry entry3 = new CellEntry("AA1");
        assertFalse(entry3.isValid());
    }

    // =================== Ex2Sheet Tests ===================
    @Test
    void testSheetDimensions() {
        assertEquals(Ex2Utils.WIDTH, sheet.width());
        assertEquals(Ex2Utils.HEIGHT, sheet.height());
    }

    @Test
    void testIsIn() {
        assertTrue(sheet.isIn(0, 0));
        assertTrue(sheet.isIn(Ex2Utils.WIDTH - 1, Ex2Utils.HEIGHT - 1));
        assertFalse(sheet.isIn(-1, 0));
        assertFalse(sheet.isIn(Ex2Utils.WIDTH, Ex2Utils.HEIGHT));
    }

    @Test
    void testGetSet() {
        sheet.set(0, 0, "Test");
        assertEquals("Test", sheet.get(0, 0).getData());

        sheet.set(1, 1, "42");
        assertEquals("42", sheet.get(1, 1).getData());
    }

    @Test
    void testGetByString() {
        sheet.set(0, 0, "Test");
        Cell cell = sheet.get("A0");
        assertNotNull(cell);
        assertEquals("Test", cell.getData());
    }

    @Test
    void testValue() {
        sheet.set(0, 0, "42");
        assertEquals("42", sheet.value(0, 0));

        sheet.set(0, 1, "=A0+8");
        assertEquals("50.0", sheet.value(0, 1));
    }

    // =================== Formula Tests ===================
    @Test
    void testBasicFormulas() {
        sheet.set(0, 0, "=5+3");
        assertEquals("8.0", sheet.value(0, 0));

        sheet.set(0, 0, "=10-5");
        assertEquals("5.0", sheet.value(0, 0));

        sheet.set(0, 0, "=4*3");
        assertEquals("12.0", sheet.value(0, 0));

        sheet.set(0, 0, "=15/3");
        assertEquals("5.0", sheet.value(0, 0));
    }

    @Test
    void testCellReferenceFormulas() {
        sheet.set(0, 0, "10");
        sheet.set(1, 0, "20");
        sheet.set(0, 1, "=A0+B0");
        assertEquals("30.0", sheet.value(0, 1));
    }

    @Test
    void testFormulaErrors() {
        // Division by zero
        sheet.set(0, 0, "=5/0");
        assertEquals(Ex2Utils.ERR_FORM, sheet.value(0, 0));


    }

    @Test
    void testCircularReference() {
        sheet.set(0, 0, "=A1");
        sheet.set(0, 1, "=A0");
        int[][] depths = sheet.depth();
        assertEquals(Ex2Utils.ERR, depths[0][0]);
        assertEquals(Ex2Utils.ERR, depths[0][1]);
    }

    // =================== File I/O Tests ===================
    @Test
    void testFileOperations() {
        try {
            // Setup test data
            sheet.set(0, 0, "Test");
            sheet.set(1, 0, "42");
            sheet.set(0, 1, "=A0+B0");

            // Save
            String filename = "test_sheet.csv";
            sheet.save(filename);

            // Load into new sheet
            Sheet newSheet = new Ex2Sheet();
            newSheet.load(filename);

            // Verify data
            assertEquals("Test", newSheet.get(0, 0).getData());
            assertEquals("42", newSheet.get(1, 0).getData());
            assertEquals("=A0+B0", newSheet.get(0, 1).getData());
        } catch (Exception e) {
            fail("File operations failed: " + e.getMessage());
        }
    }

    // =================== Additional Edge Cases ===================
    @Test
    void testEmptyCells() {
        assertEquals("", sheet.get(0, 0).getData());
        assertEquals(Ex2Utils.TEXT, sheet.get(0, 0).getType());
    }

    @Test
    void testNegativeNumbers() {
        sheet.set(0, 0, "-42");
        assertEquals("-42", sheet.get(0, 0).getData());
        assertEquals(Ex2Utils.NUMBER, sheet.get(0, 0).getType());
    }

    @Test
    void testWhitespaceHandling() {
        sheet.set(0, 0, "  42  ");
        assertEquals("42", sheet.get(0, 0).getData());

        sheet.set(0, 1, "  =  42 + 8  ");
        assertEquals("50.0", sheet.value(0, 1));
    }

    @Test
    void testComplexFormulas() {
        sheet.set(0, 0, "10");
        sheet.set(1, 0, "20");
        sheet.set(2, 0, "30");
        sheet.set(0, 1, "=A0+B0*C0");
    }
}