
import java.util.HashSet;
import java.util.Set;

public class Main {
    public static boolean isNumber(String text) {
        if (text == null || text.isEmpty()) {
            return false; // null or empty strings are not numbers
        }
        try {
            Double.parseDouble(text); // try parsing the string as a double
            return true;
        } catch (NumberFormatException e) {
            return false; // if parsing failed return false
        }
    }


    public static boolean isText(String text) {
        if (text == null || text.isEmpty()) {
            return false; // null or empty strings are not valid text
        }
        return !isNumber(text) && !isForm(text); // not a number or a form
    }

    public static boolean isForm(String text) {
        // Basic validation
        if (text == null || text.isEmpty() || text.charAt(0) != '=') {
            return false;
        }

        String formula = text.substring(1).trim(); // Remove '=' and whitespace

        // Handle simple cases (e.g., numbers)
        if (formula.matches("-?\\d+(\\.\\d+)?")) {
            return true; // Simple numeric formula like =5
        }

        // Check for cell references (e.g., =A0)
        if (formula.matches("[A-Za-z]\\d+")) {
            return true; // Valid cell reference
        }

        // Validate formulas with operators and parentheses
        int openParentheses = 0;
        boolean expectingOperand = true; // At start or after operator, expect operand

        for (char c : formula.toCharArray()) {
            if (Character.isWhitespace(c)) {
                continue; // Ignore whitespace
            }

            if (Character.isLetter(c) && expectingOperand) {
                // Check if it's a valid cell reference
                if (!formula.matches("[A-Za-z]\\d+")) {
                    return false;
                }
                expectingOperand = false;
            } else if (Character.isDigit(c) || (c == '-' && expectingOperand)) {
                // Handle numbers
                expectingOperand = false;
            } else if ("+-*/".indexOf(c) >= 0) {
                if (expectingOperand) {
                    return false; // Can't have two operators in a row
                }
                expectingOperand = true;
            } else if (c == '(') {
                openParentheses++;
            } else if (c == ')') {
                if (openParentheses == 0 || expectingOperand) {
                    return false;
                }
                openParentheses--;
            } else {
                return false; // Invalid character
            }
        }

        return openParentheses == 0 && !expectingOperand; // Valid if parentheses are balanced and ends with an operand
    }
    // helper method to isForm: consumeNumber used to process the entire number and return
    // the index pointing to the next operator or action
    private static int consumeNumber(String formula, int index) {
        while (index + 1 < formula.length() &&
                (Character.isDigit(formula.charAt(index + 1)) ||
                        formula.charAt(index + 1) == '.')) {
            index++;
        }
        return index; // return the index after the number ends
    }


    // Helper method to validate cell references
    private static boolean isCellReference(String ref) {
        // Must start with letter, followed by number
        if (ref.length() < 2) return false;

        char col = Character.toUpperCase(ref.charAt(0));
        if (col < 'A' || col > 'Z') return false;

        // Check if rest is valid number
        String rowPart = ref.substring(1);
        try {
            int row = Integer.parseInt(rowPart);
            return row > 0 && row <= 99; // Assuming valid rows are 1-99
        } catch (NumberFormatException e) {
            return false;
        }
    }


    // helper method to isForm: checks for cell references and skips over it
    private static int consumeCellReference(String formula, int index) {
        while (index + 1 < formula.length() &&
                Character.isLetter(formula.charAt(index + 1))) {
            index++;
        }

        while (index + 1 < formula.length() &&
                Character.isDigit(formula.charAt(index + 1))) {
            index++;
        }
        return index;
    }

    // helper method to isForm: checks if the next char is an operator
    private static boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    public static Double computeForm(String formula, Sheet sheet, Set<String> visited) {
        if (formula == null || !formula.startsWith("=")) {
            throw new IllegalArgumentException(Ex2Utils.ERR_FORM);
        }

        String content = formula.substring(1).trim(); // Remove the "="

        System.out.println("Evaluating formula: " + formula);

        // Handle simple numbers
        try {
            return Double.parseDouble(content);
        } catch (NumberFormatException e) {
            // Not a simple number, continue
        }

        // Handle cell references
        if (content.matches("[A-Za-z]\\d+")) {
            Cell refCell = sheet.get(content);
            if (refCell == null) {
                System.out.println("Invalid reference: " + content);
                throw new IllegalArgumentException(Ex2Utils.ERR_FORM);
            }
            if (!visited.add(content)) {
                System.out.println("Circular reference detected: " + content);
                throw new IllegalArgumentException(Ex2Utils.ERR_CYCLE);
            }
            try {
                if (refCell.getType() == Ex2Utils.NUMBER) {
                    return Double.parseDouble(refCell.getData());
                } else if (refCell.getType() == Ex2Utils.FORM) {
                    return computeForm(refCell.getData(), sheet, visited);
                } else {
                    System.out.println("Reference is not a number or formula: " + content);
                    throw new IllegalArgumentException(Ex2Utils.ERR_FORM);
                }
            } finally {
                visited.remove(content);
            }
        }

        // Parse and evaluate more complex expressions
        char[] chars = content.toCharArray();
        int[] index = new int[]{0};
        return parseExpression(chars, index, sheet, visited);
    }

    // Helper method for computeForm
    private static Double computeFormHelper(char[] chars, int[] index, Sheet sheet, Set<String> visited) {
        String formulaKey = new String(chars);
        return parseExpression(chars, index, sheet, visited);
    }

    // helper method to: computeForm to process every digit
    private static Double parseNumber(char[] chars, int[] index) {
        StringBuilder sb = new StringBuilder(); // we throw the numbers into a String and then parse them
        boolean hasDecimal = false; //track if we have a decimal in the number

        while (index[0] < chars.length) {
            char current = chars[index[0]];

            if (Character.isDigit(current)) {
                sb.append(current); // adds the digit to the sb
            } else if (current == '.' && !hasDecimal) {
                sb.append(current); // adds the decimal point and makes sure there is only one
            } else {
                break; // stop the parse when finding a not number Character
            }
            index[0]++; // move to the next Character
        }
        return Double.parseDouble(sb.toString());
    }

    // helper method to: computeNumber
    private static Double parseFactor(char[] chars, int[] index, Sheet sheet, Set<String> visited) {

        
        // handle parentheses
        if (chars[index[0]] == '(') {
            index[0]++; // Skip the '('
            double result = parseExpression(chars, index, sheet, visited);
            if (index[0] >= chars.length || chars[index[0]] != ')') {
                throw new IllegalArgumentException("Unmatched parentheses");
            }
            index[0]++; // Skip the ')'
            return result;
        }

        // handle cell references
        if (Character.isLetter(chars[index[0]])) {
            StringBuilder cellRef = new StringBuilder();
            while (index[0] < chars.length &&
                    (Character.isLetter(chars[index[0]]) || Character.isDigit(chars[index[0]]))) {
                cellRef.append(chars[index[0]++]);
            }
            String ref = cellRef.toString();

            // Check visited BEFORE adding
            if (!visited.add(ref)) {
                throw new IllegalArgumentException("Circular reference detected: " + ref);
            }

            try {
                Cell cell = sheet.get(ref);
                if (cell == null) {
                    throw new IllegalArgumentException("Invalid cell reference: " + ref);
                }

                // evaluate based on cell type
                if (cell.getType() == Ex2Utils.NUMBER) {
                    return Double.parseDouble(cell.getData());
                } else if (cell.getType() == Ex2Utils.FORM) {
                    return computeForm(cell.getData(), sheet, visited);
                } else {
                    throw new IllegalArgumentException("ERR_REF " + ref);
                }
            } finally {
                // Always remove from visited when done with this reference
                visited.remove(ref);
            }

        }

        // handle numbers
        return parseNumber(chars, index);
    }

    // helper method to: computeForm (handles multiplication and division)
    private static Double parseTerm(char[] chars, int[] index, Sheet sheet, Set<String> visited) {
        // start with the first factor
        double result = parseFactor(chars, index, sheet, visited);

        // handle multiplication and division
        while (index[0] < chars.length) {
            char operator = chars[index[0]];

            if (operator == '*' || operator == '/') {
                index[0]++; // move past the operator
                double nextFactor = parseFactor(chars, index, sheet, visited); // parse the next factor

                if (operator == '*') {
                    result *= nextFactor; // perform multiplication
                } else {
                    // handle division
                    if (nextFactor == 0) {
                        throw new IllegalArgumentException("ERR_INF"); // return specific error for division by zero
                    }
                    result /= nextFactor; // perform division
                }
            } else {
                break; // stop if the operator is not * or /
            }
        }
        return result;
    }

    private static Double parseExpression(char[] chars, int[] index, Sheet sheet, Set<String> visited) {
        // Start with the first term
        double result = parseTerm(chars, index, sheet, visited);

        // Process addition and subtraction
        while (index[0] < chars.length) {
            char operator = chars[index[0]];

            if (operator == '+' || operator == '-') {
                index[0]++; // Move past the operator
                double nextTerm = parseTerm(chars, index, sheet, visited); // Parse the next term

                if (operator == '+') {
                    result += nextTerm; // Perform addition
                } else {
                    result -= nextTerm; // Perform subtraction
                }
            } else {
                break; // Stop if the operator is not + or -
            }
        }
        return result;
    }

}
