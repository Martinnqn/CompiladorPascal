package compiladorpascal.lexico;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

public class AnalizadorLexico {

    private static HashMap<String, String> tokensSimbolos = Tokens.generarTokensSimbolos();
    private static HashMap<String, String> tokensPalabras = Tokens.generarTokensPalabras();

    public static LinkedList<Token> analizar(File fuente) throws IOException {
        LinkedList<Token> tokens = new LinkedList<>();

        BufferedReader buffer = new BufferedReader(new FileReader(fuente));
        String linea;
        int pos;
        char caracter = '_';

        String lexema = "";
        String estado = "start";
        String estadoSig = "";

        while ((linea = buffer.readLine()) != null) {
            pos = 0;
            if (!estado.equals("comment")) {
                estado = "start";
            }
            while (pos <= linea.length()) {
                if (pos != linea.length()) {
                    caracter = linea.charAt(pos);
                } else {
                    caracter = '\n';
                }
                if (estado.equals("start")) {
                    if (tokensSimbolos.containsKey(caracter + "")) {
                        lexema = caracter + "";
                        estadoSig = "simbol";
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
                        System.err.println("Caracter '" + caracter + "' desconocido en estado 'start'.");
                    }
                } else if (estado.equals("simbol")) {
                    if (tokensSimbolos.containsKey(lexema + caracter)) {
                        lexema = lexema + caracter;
                        estadoSig = "simbol";
                    } else if (Character.isLetter(caracter) || caracter == '_'
                            || Character.isDigit(caracter)
                            || Character.isWhitespace(caracter)
                            || tokensSimbolos.containsKey(caracter + "")
                            || caracter == '{') {
                        tokens.add(new Token(tokensSimbolos.get(lexema), lexema));
                        pos--;
                        estadoSig = "start";
                    } else {
                        System.err.println("Caracter '" + caracter + "' desconocido en estado 'simbol'.");
                    }
                } else if (estado.equals("letter")) {
                    if (Character.isLetter(caracter) || caracter == '_'
                            || Character.isDigit(caracter)) {
                        lexema = lexema + caracter;
                        estadoSig = "letter";
                    } else if (Character.isWhitespace(caracter)
                            || tokensSimbolos.containsKey(caracter + "")
                            || caracter == '{') {
                        if (tokensPalabras.containsKey(lexema)) {
                            tokens.add(new Token(tokensPalabras.get(lexema), lexema));
                        } else {
                            tokens.add(new Token("TK_ID", lexema));
                        }
                        pos--;
                        estadoSig = "start";
                    } else {
                        System.err.println("Caracter '" + caracter + "' desconocido en estado 'letter'.");
                    }
                } else if (estado.equals("digit")) {
                    if (Character.isDigit(caracter)) {
                        lexema = lexema + caracter;
                        estadoSig = "digit";
                    } else if (Character.isLetter(caracter) || caracter == '_'
                            || Character.isWhitespace(caracter)
                            || tokensSimbolos.containsKey(caracter + "")
                            || caracter == '{') {
                        tokens.add(new Token("TK_INTEGER", lexema));
                        pos--;
                        estadoSig = "start";
                    } else {
                        System.err.println("Caracter '" + caracter + "' desconocido en estado 'digit'.");
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
        }
        return tokens;
    }

}
