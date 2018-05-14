package compiladorpascal.analizadorlexico;

import compiladorpascal.Token;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class AnalizadorLexico {

    /*posicion actual del caracter de la linea que se lee*/
    private int pos;
    /*linea actual que se esta procesando del archivo de entrada*/
    private String linea;
    /*archivo de entrada*/
    private File program;
    /*buffer de donde se lee el archivo de entrada*/
    private BufferedReader buffer;
    /*HashMap palabras reservadas. <lexema,token>. Si devuelve algo es porque el 
    lexema ingresado es una palabra reservada.*/
    private HashMap<String, String> palabrasReservadas;
    /*Lista de token encontrados*/
    private HashMap<String, String> tokens;
    /*el lexema que se esta formando actualmente*/
    private String lexemaEncontrado;
    /*state mantiene el estado actual referente al lexema que se está encontrando. El estado
    por defecto es el cero. Si el lexema que se está encontrando empieza con _ o una letra
    podria ser una palabra reservada o un identificador, y el estado pasa a ser uno.
    Si el lexema comienza con digito, puede ser un numero y el estado es dos.*/
    private int state;

    public AnalizadorLexico(File prog, HashMap<String, String> palabras) throws IOException {
        program = prog;
        buffer = new BufferedReader(new FileReader(program));
        palabrasReservadas = palabras;
        state = 0;
        lexemaEncontrado = "";
    }

    public void iniciarAnalisis() throws IOException {
        if (linea.length() == pos) {
            linea = buffer.readLine();
        }
        if (linea != null) {
            char c = linea.charAt(pos);

            /*caracter inicio comentario*/
            if (c == '{') {
                automateComment();
            } else if (c == '(') {
                if (!lexemaEncontrado.equals("")) {
                    setToken(lexemaEncontrado, state);
                    state = 0;
                }
                tokens.put("(", Token.TK_OPAR);
            } else if (c == ')') {
                if (!lexemaEncontrado.equals("")) {
                    setToken(lexemaEncontrado, state);
                    state = 0;
                }
                tokens.put(")", Token.TK_CPAR);
            } else if (c == ';') {
                if (!lexemaEncontrado.equals("")) {
                    setToken(lexemaEncontrado, state);
                    state = 0;
                }
                tokens.put(";", Token.TK_COMMA);
            } else if (c == '.') {
                if (!lexemaEncontrado.equals("")) {
                    setToken(lexemaEncontrado, state);
                    state = 0;
                }
                tokens.put(".", Token.TK_POINT);
                //caracteres de espacio en blanco
            } else if (isWS(c)) {
                if (!lexemaEncontrado.equals("")) {
                    setToken(lexemaEncontrado, state);
                    state = 0;
                }
                //si el caracter es un digito
            } else if (Character.isDigit(c) && (state == 0 || state == 2)) {
                state = 2;
                lexemaEncontrado += c;
                /*si el caracter es una letra y el estado es cero entonces es el 
                primer caracter y puede ser un identificador*/
            } else if ((Character.isLetter(c) || c == '_') && state == 0) {
                state = 1;
                lexemaEncontrado += c;
                /*para el caso del identificador*/
            } else if ((Character.isLetter(c) || c == '_' || Character.isDigit(c)) && state == 1) {
                lexemaEncontrado += c;
            }//else if... else {lanzar error lexico).
            lexemaEncontrado = "";
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
    private void automateComment() throws IOException {
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
     * Setea el token segun el state.
     *
     * @param lexema
     */
    private void setToken(String lexema, int state) {
        String toUp = lexema.toUpperCase();
        if (state == 0) {
        } else if (state == 1) {
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
