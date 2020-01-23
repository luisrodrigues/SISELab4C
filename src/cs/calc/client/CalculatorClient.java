package cs.calc.client;

import javax.swing.*;
import javax.xml.ws.BindingProvider;
import java.awt.*;

public class CalculatorClient {

    /*
     * The main function initialize the remote instance and connects to the remote service
     * */
    public static void main(String[] args) throws InterruptedException {
        CalculatorService calcService = new CalculatorService();

        final Calculator calc = (Calculator) calcService.getCalculatorPort();

        ((BindingProvider) calc).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                "http://localhost:8090/calcservice");

        runCalculator(calc);

    }

    /*
     * This method executes the calculator client by receiving an expression as input
     * from the user with numbers, multiplications and summations and printing the result
     * Client must run until the user input 'exit'
     * */
    public static void runCalculator(Calculator calc) {
        // implement in this method

        while (true) {
            String expression = JOptionPane.showInputDialog("Insert expression: ");
            try {
                if (expression.equals("exit")) {
                    break;
                }
                int res = eval(expression, calc);
                JOptionPane.showMessageDialog(null, "result is: " + res);
            } catch (Exception e) {

            }
        }
    }

    /*
     *  This method evaluates an arithmentic expression and call the calculator object to
     * solve operations.
     * You dont need to change this function.
     * */
    public static int eval(final String str, final Calculator calc) {
        class Parser {
            int pos = -1, c;

            void eatChar() {
                c = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            void eatSpace() {
                while (Character.isWhitespace(c))
                    eatChar();
            }

            int parse() {
                eatChar();
                int v = parseExpression();
                if (c != -1)
                    throw new RuntimeException("Unexpected: " + (char) c);
                return v;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor | term brackets
            // factor = brackets | number | factor `^` factor
            // brackets = `(` expression `)`

            int parseExpression() {
                int v = parseTerm();
                for (; ; ) {
                    eatSpace();
                    if (c == '+') { // addition
                        eatChar();
                        v = calc.sum(v, parseTerm());
                    } else {
                        return v;
                    }
                }
            }

            int parseTerm() {
                int v = parseFactor();
                for (; ; ) {
                    eatSpace();
                    if (c == '*' || c == '(') { // multiplication
                        if (c == '*')
                            eatChar();
                        v = calc.mul(v, parseFactor());
                    } else {
                        return v;
                    }
                }
            }

            int parseFactor() {
                int v;
                boolean negate = false;
                eatSpace();
                if (c == '+' || c == '-') { // unary plus & minus
                    negate = c == '-';
                    eatChar();
                    eatSpace();
                }
                if (c == '(') { // brackets
                    eatChar();
                    v = parseExpression();
                    if (c == ')')
                        eatChar();
                } else if (c == 'A') {
                    eatChar();
                    v = calc.last();
                } else { // numbers
                    StringBuilder sb = new StringBuilder();
                    while ((c >= '0' && c <= '9') || c == '.') {
                        sb.append((char) c);
                        eatChar();
                    }
                    if (sb.length() == 0)
                        throw new RuntimeException("Unexpected: " + (char) c);
                    v = Integer.parseInt(sb.toString());
                }
                eatSpace();
                if (negate)
                    v = -v; // unary minus is applied after exponentiation; e.g.
                // -3^2=-9
                return v;
            }
        }
        return new Parser().parse();
    }
}
