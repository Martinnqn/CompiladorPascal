package compiladorpascal.analizadorlexico;

import compiladorpascal.Token;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class AnalizadorLexico {

    private int pos; //posicion actual del caracter de la linea que se lee
    private String linea; //linea actual que se esta procesando del archivo de entrada
    private File program; //archivo de entrada
    private BufferedReader buffer; //buffer de donde se lee el archivo de entrada
    /*HashMap palabras reservadas. <lexema,token>. Si devuelve algo es porque el 
    lexema ingresado es una palabra reservada.*/
    private HashMap<String, String> palabrasReservadas;
    /*Lista de token encontrados*/
    private HashMap<String, String> tokens;

    public AnalizadorLexico(File prog, HashMap<String, String> palabras) throws IOException {
        program = prog;
        buffer = new BufferedReader(new FileReader(program));
        palabrasReservadas = palabras;
    }

    public void iniciarAnalisis() throws IOException {
        if (linea.length() == pos) {
            linea = buffer.readLine();
        }
        if (linea != null) {
            char c = linea.charAt(pos);

            /*caracter de comentarios*/
            if (c == '{') {
                avanzar();
            } else if (c == '(') {
                tokens.put("(", Token.TK_OPAR);
            } else if (c == ')') {
                tokens.put(")", Token.TK_CPAR);
            } else if (c == ';') {
                tokens.put(";", Token.TK_COMMA);
            } else if (c == '.') {
                tokens.put(".", Token.TK_POINT);
            } else if (isWS(c)) {
            }
            //... etc
            
            
            iniciarAnalisis();
        }

    }

    /**
     * Devuelve true si el caracter c es un espacio, tabulacion, o fin de linea.
     *
     * @param c
     * @return
     */
    private boolean isWS(char c) {
        return c == ' ' || c == '\t' || c == '\n';
    }

    /**
     * Recorre el archivo hasta que encuentra un lexema de cierre de comentario.
     *
     * @throws IOException
     */
    private void avanzar() throws IOException {
        pos++;
        if (linea.length() == pos) {
            linea = buffer.readLine();
            pos = 0;
        }
        while (linea != null && linea.charAt(pos) != '}') {
            pos++;
            if (linea.length() == pos) {
                linea = buffer.readLine();
                pos = 0;
            }
        }
        /*cuando encuentra un lexema de cierre de comentario sale y deja pos 
            en la posicion del caracter que debe leerse en el proximo lexema*/
        if (linea != null) {
            pos++;
        }
    }

    /**
     * Si el lexema recibido es un id, se agrega el token id a la lista de
     * token, si es una palabra reservada se agrega su token correspondiente a
     * la lista de token.
     *
     * @param lexema
     */
    private void setToken(String lexema) {
        String toUp = lexema.toUpperCase();
        String res = palabrasReservadas.get(toUp);
        if (res == null) {
            tokens.put(toUp, Token.TK_ID);
        } else {
            tokens.put(toUp, res);
        }
    }
}
