public class SCell implements Cell {
    private String data; // store the raw content of the cell
    private int type; // stores the type (TEXT, NUMBER, FORM,)
    private int order;
    private final Sheet sheet;

    public SCell(String input, Sheet sheet) {
        this.sheet = sheet;
        this.data = "";
        this.type = Ex2Utils.TEXT;
        this.order = 0;
        setData(input);
    }

    @Override
    public String getData() {
        return data;
    }

    @Override
    public void setData(String input) {
        // handle null input by making it an empty string
        this.data = (input == null) ? "" : input.trim();
        evaluateType(); //determine and set the cell type based on its content
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public void setType(int t) {
        this.type = t;
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public void setOrder(int t) {
        this.order = t;
    }

    private void evaluateType() {
        if (data.isEmpty()) { // if the input in an empty string
            type = Ex2Utils.TEXT;
            return;
        }

        if (data.startsWith("=")) { // checks that the formula stats with "="
            type = Ex2Utils.FORM;
            return;
        }

        try {
            Double.parseDouble(data);
            type = Ex2Utils.NUMBER;
        } catch (NumberFormatException e) {
            type = Ex2Utils.TEXT;
        }
    }

    private double evaluateFormula() {
        if (!data.startsWith("=")) { // verify that the formula is valid
            throw new IllegalArgumentException("Not a formula");
        }

        String formula = data.substring(1).trim(); // remove the "=" and trim the form
        return evaluateExpression(formula);
    }

    private double evaluateExpression(String expression) {
        // handle negative reference to another cell ( -A0 )
        if (expression.matches("-[A-Za-z]\\d+")) { // remove the neg sign and get the cell ref
            String cellRef = expression.substring(1);
            Cell referencedCell = sheet.get(cellRef);
            if (referencedCell == null) {
                throw new IllegalArgumentException("Invalid cell reference");
            }
            return -getNumericValue(referencedCell); // return neg cell value
        }

        // handle direct cell reference
        if (expression.matches("[A-Za-z]\\d+")) {
            Cell referencedCell = sheet.get(expression);
            if (referencedCell == null) {
                throw new IllegalArgumentException("Invalid cell reference");
            }
            return getNumericValue(referencedCell);
        }

        // split expression by operators while keeping the operators
        // example: "A1 + A2 * 3" becomes [A1, + , A2, * ,3]
        String[] tokens = expression.split("(?<=[\\+\\-\\*\\/])|(?=[\\+\\-\\*\\/])");

        // process tokens and calc the results
        double result = 0;
        String operator = "+";
        for (String token : tokens) {
            token = token.trim();
            if (token.isEmpty()) continue;

            if ("+-*/".contains(token)) {
                operator = token;
            } else {
                double value;
                if (token.matches("[A-Za-z]\\d+")) {
                    // handle cell reference
                    Cell referencedCell = sheet.get(token);
                    if (referencedCell == null) {
                        throw new IllegalArgumentException("Invalid cell reference");
                    }
                    value = getNumericValue(referencedCell);
                } else {
                    // handle numeric value
                    try {
                        value = Double.parseDouble(token);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Invalid number format");
                    }
                }

                switch (operator) {
                    case "+":
                        result += value;
                        break;
                    case "-":
                        result -= value;
                        break;
                    case "*":
                        result *= value;
                        break;
                    case "/":
                        if (value == 0) {
                            throw new IllegalArgumentException("Division by zero");
                        }
                        result /= value;
                        break;
                }
            }
        }
        return result;
    }

    // gets the numeric value from a cell with both number and form types
    private double getNumericValue(Cell cell) {
        if (cell.getType() == Ex2Utils.NUMBER) {
            return Double.parseDouble(cell.getData());
        } else if (cell.getType() == Ex2Utils.FORM) {
            return Double.parseDouble(cell.toString());
        } else {
            throw new IllegalArgumentException("Cell is not numeric");// error for debugging
        }
    }

    @Override
    public String toString() {
        if (type == Ex2Utils.TEXT || data.isEmpty()) {
            return data;
        }

        if (type == Ex2Utils.NUMBER) {
            return data;
        }

        if (type == Ex2Utils.FORM) {
            try {
                double result = evaluateFormula();
                return String.valueOf(result);
            } catch (Exception e) {
                return Ex2Utils.ERR_FORM;
            }
        }

        if (type == Ex2Utils.ERR_CYCLE_FORM) {
            return Ex2Utils.ERR_CYCLE;
        }

        return Ex2Utils.ERR_FORM;
    }
}