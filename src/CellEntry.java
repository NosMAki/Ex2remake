;
// Add your documentation below:

public class CellEntry implements Index2D {
    private final String index;  // The cell reference

    public CellEntry(String index) {
        this.index = index;
    }

    @Override
    public boolean isValid() {
        if (index == null || index.length() < 2) return false;

        char col = Character.toUpperCase(index.charAt(0));
        if (col < 'A' || col > 'Z') return false;

        try {
            int row = Integer.parseInt(index.substring(1));
            return row >= 0 && row < Ex2Utils.HEIGHT;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public int getX() {
        if (!isValid()) {
            return Ex2Utils.ERR;
        }
        // Convert column letter to number (A=0, B=1, etc.)
        return Character.toUpperCase(index.charAt(0)) - 'A';
    }

    @Override
    public int getY() {
        if (!isValid()) {
            return Ex2Utils.ERR;
        }
        // Get row number, converting to 0-based index
        return Integer.parseInt(index.substring(1));
    }

    @Override
    public String toString() {
        return isValid() ? index.toUpperCase() : "";
    }
}
