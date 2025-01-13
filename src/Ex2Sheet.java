import java.io.*;

public class Ex2Sheet implements Sheet {
    private final Cell[][] table; // the 2D array storing all the cells in the spreadsheet

    public Ex2Sheet(int width, int height) {
        // initialize the 2D array with specified dimensions
        this.table = new SCell[width][height];

        // initialize empty cells
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                table[i][j] = new SCell("", this); // empty cell with reference to this sheet
            }
        }
    }

    public Ex2Sheet() {
        this(Ex2Utils.WIDTH, Ex2Utils.HEIGHT);
    }

    @Override
    public boolean isIn(int x, int y) {
        return x >= 0 && x < width() && y >= 0 && y < height();
    }

    @Override
    public int width() {
        return table.length;
    }

    @Override
    public int height() {
        return table[0].length;
    }

    @Override
    public Cell get(int x, int y) {
        if (!isIn(x, y)) {
            throw new IllegalArgumentException("Cell coordinates out of bounds");
        }
        return table[x][y];
    }

    @Override
    public Cell get(String coord) {
        if (coord == null || coord.length() < 2) {
            return null;
        }
        try {
            // convert letter to number (A=0, B=1, etc...)
            int x = Character.toUpperCase(coord.charAt(0)) - 'A';
            // convert string number to int
            int y = Integer.parseInt(coord.substring(1));
            return isIn(x, y) ? get(x, y) : null;
        } catch (Exception e) {
            return null; // return null for any parsing errors
        }
    }

    @Override
    public void set(int x, int y, String str) {
        if (!isIn(x, y)) {
            throw new IllegalArgumentException("Cell coordinates out of bounds");
        }
        table[x][y] = new SCell(str, this); // create new cell with content
    }

    @Override
    public String value(int x, int y) {
        return eval(x, y);
    }

    @Override
    public String eval(int x, int y) {
        if (!isIn(x, y)) {
            return "";
        }
        return table[x][y].toString(); // use cell toString for evaluation
    }

    @Override
    public void eval() {
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                eval(i, j);
            }
        }
    }
    // calculates depth matrix for formula dependencies
    // used to detect circular references and evaluate formulas in correct order
    @Override
    public int[][] depth() {
        int[][] depths = new int[width()][height()];
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                depths[i][j] = calculateDepth(i, j, new boolean[width()][height()]);
            }
        }
        return depths;
    }

    // recursive helper method to calculate cell dependency depth
    private int calculateDepth(int x, int y, boolean[][] visited) {
        if (visited[x][y]) {
            return Ex2Utils.ERR;
        }

        Cell cell = get(x, y);
        if (cell.getType() != Ex2Utils.FORM) {
            return 0;
        }

        visited[x][y] = true;
        int maxDepth = 0;

        String data = cell.getData();
        if (data.startsWith("=")) {
            String formula = data.substring(1).trim();

            // handle negative sign
            if (formula.startsWith("-")) { // handles negative sign ath the start of the formula
                formula = formula.substring(1).trim();
            }

            if (formula.matches("[A-Za-z]\\d+")) {
                Cell referenced = get(formula);
                if (referenced != null) { // convert the reference into cords
                    int refX = formula.toUpperCase().charAt(0) - 'A';
                    int refY = Integer.parseInt(formula.substring(1));
                    int depth = calculateDepth(refX, refY, visited);
                    if (depth == Ex2Utils.ERR) {
                        visited[x][y] = false;
                        return Ex2Utils.ERR;
                    }
                    maxDepth = Math.max(maxDepth, depth + 1);
                }
            }
        }

        visited[x][y] = false; // unmark visited cells and return
        return maxDepth;
    }

    @Override
    public void save(String filename) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (int y = 0; y < height(); y++) {
                for (int x = 0; x < width(); x++) {
                    String value = table[x][y].getData();
                    if (!value.isEmpty()) {
                        writer.write(x + "," + y + "," + value);
                        writer.newLine();
                    }
                }
            }
        }
    }

    @Override
    public void load(String filename) throws IOException {
        // clear current table
        for (int i = 0; i < width(); i++) {
            for (int j = 0; j < height(); j++) {
                table[i][j] = new SCell("", this);
            }
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    try {
                        int x = Integer.parseInt(parts[0]);
                        int y = Integer.parseInt(parts[1]);
                        if (isIn(x, y)) {
                            set(x, y, parts[2]);
                        }
                    } catch (NumberFormatException ignored) {
                        // skip invalid lines
                    }
                }
            }
        }
    }
}