import java.io.*;
import java.util.HashSet;

public class Ex2Sheet implements Sheet {
    private Cell[][] table;

    public Ex2Sheet(int x, int y) {
        table = new SCell[x][y];
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                table[i][j] = new SCell("", this); // Initialize as empty cells
            }
        }
    }

    public Ex2Sheet() {
        this(Ex2Utils.WIDTH, Ex2Utils.HEIGHT);
    }

    @Override
    public String value(int x, int y) {
        Cell cell = get(x, y);
        if (cell == null) {
            return Ex2Utils.EMPTY_CELL;
        }

        // Check if the cell is a formula and evaluate it
        if (cell.getType() == Ex2Utils.FORM) {
            try {
                Double result = Main.computeForm(cell.getData(), this, new HashSet<>());
                return result.toString();
            } catch (Exception e) {
                return Ex2Utils.ERR_FORM; // Handle errors in formula evaluation
            }
        }

        // For other cell types, return the stored data
        return cell.toString();
    }

    @Override
    public Cell get(int x, int y) {
        if (!isIn(x, y)) {
            throw new IllegalArgumentException("Coordinates out of bounds: (" + x + ", " + y + ")");
        }
        return table[x][y];
    }

    @Override
    public Cell get(String cords) {
        if (cords == null || cords.length() < 2) {
            return null; // Invalid input
        }
        try {
            char col = cords.charAt(0);
            int x = col - 'A'; // convert column letter to index
            int y = Integer.parseInt(cords.substring(1)) - 1; // convert row to index
            return isIn(x, y) ? get(x, y) : null;
        } catch (Exception e) {
            return null; // handle parsing errors
        }
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
    public void set(int x, int y, String s) {
        if (!isIn(x, y)) {
            throw new IllegalArgumentException("Coordinates out of bounds: (" + x + ", " + y + ")");
        }
        table[x][y] = new SCell(s, this); // Create a new SCell with the given data
    }


    @Override
        public void eval () {
            for (int x = 0; x < width(); x++) {
                for (int y = 0; y < height(); y++) {
                    Cell cell = get(x, y);
                    if (cell != null && cell.getType() == Ex2Utils.FORM) {
                        try {
                            Main.computeForm(cell.getData(), this, new HashSet<>());
                        } catch (Exception e) {
                            cell.setType(Ex2Utils.ERR_FORM_FORMAT);
                        }
                    }
                }
            }
        }
    @Override
    public boolean isIn(int xx, int yy) {
        return xx >= 0 && yy >= 0 && xx < width() && yy < height();
    }

    @Override
    public int[][] depth() {
        int[][] depthMap = new int[width()][height()];
        boolean[][] visited = new boolean[width()][height()]; // tracks visited cells to prevent cycles
        for (int x = 0; x < width(); x++) {
            for (int y = 0; y < height(); y++) {
                if (!visited[x][y]) {
                    depthMap[x][y] = calculateDepth(x, y, visited, depthMap);
                }
            }
        }
        return depthMap;
    }

    // helper method to calculateDepth: since for some reason i crash when i do it inside depth we try it here
    private int calculateDepth(int x, int y, boolean[][] visited, int[][] depthMap) {
        if (visited[x][y]) {
            return -1; // value for cell references
        }
        visited[x][y] = true;

        Cell cell = get(x, y);
        if (cell == null || cell.getType() == Ex2Utils.TEXT || cell.getType() == Ex2Utils.NUMBER) {
            return 0; // No dependencies
        }

        if (cell.getType() == Ex2Utils.FORM) {
            String formula = cell.getData().substring(1); // remove the =
            String[] references = extractCellReferences(formula); // extract the cell reference
            int maxDepth = 0;

            for (String ref : references) {
                Cell refCell = get(ref);
                if (refCell != null) {
                    int[] coords = parseCellRef(ref);
                    maxDepth = Math.max(maxDepth, calculateDepth(coords[0], coords[1], visited, depthMap));
                }
            }
            depthMap[x][y] = maxDepth + 1;
        }
        visited[x][y] = false;
        return depthMap[x][y];
    }

    private String[] extractCellReferences(String formula) {
        return formula.split("[^A-Za-z0-9]"); // MIGHT BE A PROBLEM
    }

    private int[] parseCellRef(String ref) {
        char col = ref.charAt(0);
        int x = col - 'A';
        int y = Integer.parseInt(ref.substring(1)) - 1;
        return new int[]{x, y};
    }

    @Override
    public void load(String fileName) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            for (int x = 0; x < width(); x++) {
                String line = br.readLine();
                if (line == null) break;
                String[] cells = line.split(",");
                for (int y = 0; y < Math.min(cells.length, height()); y++) {
                    set(x, y, cells[y]);
                }
            }
        }
    }

    @Override
    public void save(String fileName) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName))) {
            for (int x = 0; x < width(); x++) {
                StringBuilder sb = new StringBuilder();
                for (int y = 0; y < height(); y++) {
                    sb.append(get(x, y).getData());
                    if (y < height() - 1) sb.append(",");
                }
                bw.write(sb.toString());
                bw.newLine();
            }
        }
    }

    @Override
    public String eval(int x, int y) {
        Cell cell = get(x, y);
        if (cell == null || cell.getData().isEmpty()) {
            return Ex2Utils.EMPTY_CELL;
        }

        if (cell.getType() == Ex2Utils.NUMBER || cell.getType() == Ex2Utils.TEXT) {
            return cell.getData();
        } else if (cell.getType() == Ex2Utils.FORM) {
            try {
                Double result = Main.computeForm(cell.getData(), this, new HashSet<>());
                return result.toString();
            } catch (Exception e) {
                return Ex2Utils.ERR_FORM;
            }
        }

        return Ex2Utils.ERR_FORM;
    }
}
