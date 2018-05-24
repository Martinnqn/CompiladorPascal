package compiladorpascal.lexico;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Analizador léxico del lenguaje Pascal.
 *
 * @author Martin Bermudez y Giuliano Marinelli
 */
public class AnalizadorLexico {

    private static final HashMap<String, String> TOKENS_SIMBOLOS = Tokens.generarTokensSimbolos();
    private static final HashMap<String, String> TOKENS_PALABRAS = Tokens.generarTokensPalabras();

    /**
     * Recibe un archivo fuente y realiza un análisis léxico, mostrando los
     * errores encontrados.
     *
     * @param fuente
     * @return Lista de tokens correspondientes al análisis.
     * @throws IOException
     */
    public static LinkedList<Token> analizar(File fuente) throws IOException {
        LinkedList<Token> tokens = new LinkedList<>();

        //inicializa un buffer para leer el archivo fuente
        BufferedReader buffer = new BufferedReader(new FileReader(fuente));
        //para leer y moverse por las líneas del archivo
        String linea;
        int pos;
        int nroLinea = 1;
        char caracter;

        //para formar lexemas y moverse entre estados
        String lexema = "";
        String estado = "start";
        String estadoSig = "";

        //determinará el corte del análisis en caso de encontrar error
        boolean error = false;

        while ((linea = buffer.readLine()) != null && !error) {
            pos = 0;
            /*permite continuar con el estado "comment" al terminar la línea en
            caso de tener un comentario de varias líneas*/
            if (!estado.equals("comment")) {
                estado = "start";
            }
            while (pos <= linea.length() && !error) {
                /*debido que el caracter de salto de línea no se lee, se lo 
                simula con una iteración extra, para cortar los lexema que se 
                está formando*/
                if (pos != linea.length()) {
                    caracter = linea.charAt(pos);
                } else {
                    caracter = '\n';
                }
                if (estado.equals("start")) {/*en este estado no se ha
                    comenzando un lexema*/
                    //cambia al estado correspondiente según el caracter que lee
                    if (TOKENS_SIMBOLOS.containsKey(caracter + "")) {
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
                    } else {//error: un símbolo fuera del alfabeto
                        System.err.println("Error linea " + nroLinea + " posicion " + (pos + 1) + ". Caracter '" + caracter + "' desconocido.");
                        lexema = "";
                        estadoSig = "start";
                        error = true;
                    }
                } else if (estado.equals("symbol")) {/*en este estado el lexema 
                    formado hasta el momento tiene uno o mas simbolos 
                    operacionales, correspondientes a los del conjunto
                    TOKENS_SIMBOLOS*/
                    if (TOKENS_SIMBOLOS.containsKey(lexema + caracter)) {
                        //arma el lexema con el nuevo caracter y los anteriores
                        lexema = lexema + caracter;
                        estadoSig = "symbol";
                    } else if (Character.isLetter(caracter) || caracter == '_'
                            || Character.isDigit(caracter)
                            || Character.isWhitespace(caracter)
                            || TOKENS_SIMBOLOS.containsKey(caracter + "")
                            || caracter == '{') {
                        /*corta el lexema, genera el token, vuelve una posición
                        atras para leer el caracter de corte nuevamente y 
                        regresa al estado "start" para formar un nuevo lexema*/
                        tokens.add(new Token(TOKENS_SIMBOLOS.get(lexema), lexema));
                        pos--;
                        estadoSig = "start";
                    } else {//error: un símbolo fuera del alfabeto
                        System.err.println("Error linea " + nroLinea + " posicion " + (pos + 1) + ". Caracter '" + caracter + "' desconocido.");
                        lexema = "";
                        estadoSig = "start";
                        error = true;
                    }
                } else if (estado.equals("letter")) {/*en este estado el lexema
                    formado hasta el momento tiene una o mas letras o 
                    caracteres '_'*/
                    if (Character.isLetter(caracter) || caracter == '_'
                            || Character.isDigit(caracter)) {
                        //arma el lexema con el nuevo caracter y los anteriores
                        lexema = lexema + caracter;
                        estadoSig = "letter";
                    } else if (Character.isWhitespace(caracter)
                            || TOKENS_SIMBOLOS.containsKey(caracter + "")
                            || caracter == '{') {
                        /*corta el lexema, genera un token de palabra reservada,
                        correspondiente a los del conjunto TOKENS_SIMBOLOS o un 
                        token TK_ID en caso de no encontrar palabra reservada, 
                        vuelve una posición atras para leer el caracter de corte
                        nuevamente y regresa al estado "start" para formar un 
                        nuevo lexema*/
                        if (TOKENS_PALABRAS.containsKey(lexema.toUpperCase())) {
                            tokens.add(new Token(TOKENS_PALABRAS.get(lexema.toUpperCase()), lexema));
                        } else {
                            tokens.add(new Token("TK_ID", lexema));
                        }
                        pos--;
                        estadoSig = "start";
                    } else {//error: un símbolo fuera del alfabeto
                        System.err.println("Error linea " + nroLinea + " posicion " + (pos + 1) + ". Caracter '" + caracter + "' desconocido.");
                        lexema = "";
                        estadoSig = "start";
                        error = true;
                    }
                } else if (estado.equals("digit")) {/*en este estado el lexema
                    formado hasta el momento tiene uno o mas dígitos numéricos*/
                    if (Character.isDigit(caracter)) {
                        //arma el lexema con el nuevo caracter y los anteriores
                        lexema = lexema + caracter;
                        estadoSig = "digit";
                    } else if (Character.isWhitespace(caracter)
                            || TOKENS_SIMBOLOS.containsKey(caracter + "")
                            || caracter == '{'
                            || Character.isLetter(caracter) || caracter == '_') {
                        /*corta el lexema, genera el token, vuelve una posición
                        atras para leer el caracter de corte nuevamente y 
                        regresa al estado "start" para formar un nuevo lexema*/
                        tokens.add(new Token("TK_INTEGER", lexema));
                        pos--;
                        estadoSig = "start";
                    } else {//error: un símbolo fuera del alfabeto
                        System.err.println("Error linea " + nroLinea + " posicion " + (pos + 1) + ". Caracter '" + caracter + "' desconocido.");
                        lexema = "";
                        estadoSig = "start";
                        error = true;
                    }
                } else if (estado.equals("comment")) {/*este estado indica que
                    se encontró un caracter de apertura de comentario. 
                    Permanecerá en este estado hasta encontrar un caracter de 
                    cierre de comentario*/
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
        if (estado.equals("comment")) {//error: un comentario sin cerrar
            System.err.println("Error fin de comentario no encontrado.");
        }
        return tokens;
    }

}
