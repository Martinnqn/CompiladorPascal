package compiladorpascal.lexico;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

public class AnalizadorLexico {

    private static final HashMap<String, String> tokensSimbolos = Tokens.generarTokensSimbolos();
    private static final HashMap<String, String> tokensPalabras = Tokens.generarTokensPalabras();

    public static LinkedList<Token> analizar(File fuente) throws IOException {
        LinkedList<Token> tokens = new LinkedList<>();

        BufferedReader buffer = new BufferedReader(new FileReader(fuente));
        String linea;
        int pos;
        int nroLinea = 1;
        char caracter;

        String lexema = "";
        String estado = "start";
        String estadoSig = "";

        boolean error = false;

        while ((linea = buffer.readLine()) != null && !error) {
            pos = 0;
            if (!estado.equals("comment")) {
                estado = "start";
            }
            while (pos <= linea.length() && !error) {
                if (pos != linea.length()) {
                    caracter = linea.charAt(pos);
                } else {
                    caracter = '\n';
                }
                if (estado.equals("start")) {
                    if (tokensSimbolos.containsKey(caracter + "")) {
                        lexema = caracter + "";
                        estadoSig = "symbol";
                    } else if (Character.isLetter(caracter) || caracter == '_') {
                        lexema = caracter + "";
                        estadoSig = "letter";
                    } else if (Character.isDigit(caracter)) {
                        lexema = caracter + "";
                        estadoSig = "digit";
                    } else if (Character.isWhitespace(caracter)) {
                        estadoSig = "start";
                    } else if (caracter == '{') {
                        estadoSig = "comment";
                    } else {
                        System.err.println("Error linea " + nroLinea + " posicion " + (pos + 1) + ". Caracter '" + caracter + "' desconocido.");
                        lexema = "";
                        estadoSig = "start";
                        error = true;
                    }
                } else if (estado.equals("symbol")) {
                    if (tokensSimbolos.containsKey(lexema + caracter)) {
                        lexema = lexema + caracter;
                        estadoSig = "symbol";
                    } else if (Character.isLetter(caracter) || caracter == '_'
                            || Character.isDigit(caracter)
                            || Character.isWhitespace(caracter)
                            || tokensSimbolos.containsKey(caracter + "")
                            || caracter == '{') {
                        tokens.add(new Token(tokensSimbolos.get(lexema), lexema));
                        pos--;
                        estadoSig = "start";
                    } else {
                        System.err.println("Error linea " + nroLinea + " posicion " + (pos + 1) + ". Caracter '" + caracter + "' desconocido.");
                        lexema = "";
                        estadoSig = "start";
                        error = true;
                    }
                } else if (estado.equals("letter")) {
                    if (Character.isLetter(caracter) || caracter == '_'
                            || Character.isDigit(caracter)) {
                        lexema = lexema + caracter;
                        estadoSig = "letter";
                    } else if (Character.isWhitespace(caracter)
                            || tokensSimbolos.containsKey(caracter + "")
                            || caracter == '{') {
                        if (tokensPalabras.containsKey(lexema.toUpperCase())) {
                            tokens.add(new Token(tokensPalabras.get(lexema.toUpperCase()), lexema));
                        } else {
                            tokens.add(new Token("TK_ID", lexema));
                        }
                        pos--;
                        estadoSig = "start";
                    } else {
                        System.err.println("Error linea " + nroLinea + " posicion " + (pos + 1) + ". Caracter '" + caracter + "' desconocido.");
                        lexema = "";
                        estadoSig = "start";
                        error = true;
                    }
                } else if (estado.equals("digit")) {
                    if (Character.isDigit(caracter)) {
                        lexema = lexema + caracter;
                        estadoSig = "digit";
                    } else if (Character.isWhitespace(caracter)
                            || tokensSimbolos.containsKey(caracter + "")
                            || caracter == '{'
                            || Character.isLetter(caracter) || caracter == '_') {
                        tokens.add(new Token("TK_INTEGER", lexema));
                        pos--;
                        estadoSig = "start";
                    } else {
                        System.err.println("Error linea " + nroLinea + " posicion " + (pos + 1) + ". Caracter '" + caracter + "' desconocido.");
                        lexema = "";
                        estadoSig = "start";
                        error = true;
                    }
                } else if (estado.equals("comment")) {
                    if (caracter != '}') {
                        estadoSig = "comment";
                    } else {
                        estadoSig = "start";
                    }
                }
                estado = estadoSig;
                pos++;
            }
            nroLinea++;
        }
        if (estado.equals("comment")) {
            System.err.println("Error fin de comentario no encontrado.");
        }
        return tokens;
    }

}
