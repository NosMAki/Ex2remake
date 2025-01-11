
// Add your documentation below:

import java.util.HashSet;

public class SCell implements Cell {
    private String line;
    private String evaluatedData;
    private int type;
    private int order;
    private Sheet sheet;


    public SCell(String s, Sheet sheet) {
        this.sheet = sheet;
        if (s == null || s.isEmpty()) {
            this.line = Ex2Utils.EMPTY_CELL;
            this.type = Ex2Utils.TEXT;
            this.evaluatedData = Ex2Utils.EMPTY_CELL;
        } else {
            setData(s);
        }
    }


    @Override
    public int getOrder() {
        if (type == Ex2Utils.TEXT || type == Ex2Utils.NUMBER) {
            return 0; // text and numbers have no dependencies
        }
        if (type == Ex2Utils.FORM){
            return order; // return the calculated order for formulas
        }
        return -1; // for invalid cells
    }

    //@Override
    @Override
    public String toString() {
        if (type == Ex2Utils.FORM) {
            try {
                if (evaluatedData == null || evaluatedData.equals(Ex2Utils.EMPTY_CELL)) {
                    // Evaluate the formula dynamically using Main.computeForm
                    Double result = Main.computeForm(line, sheet, new HashSet<>());
                    evaluatedData = result.toString();
                }
                return evaluatedData;
            } catch (Exception e) {
                // If evaluation fails, set type to error and return error message
                setType(Ex2Utils.ERR_FORM_FORMAT);
                return Ex2Utils.ERR_FORM;
            }
        }

        // For text and numbers, return the evaluated data
        return evaluatedData;
    }

    @Override
    public void setData(String s) {
        this.line = s; // Store the raw input

        if (s == null || s.isEmpty()) {
            this.type = Ex2Utils.TEXT;
            this.evaluatedData = Ex2Utils.EMPTY_CELL;
            return;
        }

        // Check if the input is a valid number
        if (Main.isNumber(s)) {
            this.type = Ex2Utils.NUMBER;
            this.evaluatedData = s;
        }
        // Check if the input is a valid formula
        else if (Main.isForm(s)) {
            this.type = Ex2Utils.FORM;
            try {
                // Attempt to compute the formula immediately
                Double result = Main.computeForm(s, sheet, new HashSet<>());
                this.evaluatedData = result.toString(); // Store the computed result
            } catch (IllegalArgumentException e) {
                // Handle formula errors (e.g., invalid formula, circular references)
                this.type = Ex2Utils.ERR_FORM_FORMAT;
                this.evaluatedData = Ex2Utils.ERR_FORM;
            }
        }
        // Default to TEXT for invalid inputs
        else {
            this.type = Ex2Utils.TEXT;
            this.evaluatedData = s;
        }
    }
    @Override
    public String getData() {
        return line; // return the original value
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
    public void setOrder(int t) {
        this.order = t;

    }
    // Helper method to update evaluated result without changing the formula
    public void setEvaluatedData(String value) {
        this.evaluatedData = value;
    }
}
