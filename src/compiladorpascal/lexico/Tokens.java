package compiladorpascal.lexico;

import java.util.HashMap;

/**
 * Generador de conjuntos de tokens del lenguaje Pascal.
 *
 * @author Martin Bermudez y Giuliano Marinelli
 */
public class Tokens {

    /**
     * Genera un HashMap que asocia los simbolos operacionales de Pascal del
     * lenguaje con sus respectivos token.
     *
     * @return HashMap (patrón, token)
     */
    public static HashMap<String, String> generarTokensSimbolos() {
        HashMap<String, String> tokens = new HashMap<>();
        tokens.put(":=", "TK_ASSIGN");
        tokens.put("=", "TK_REL_OP_EQ");
        tokens.put("<>", "TK_REL_OP_NEQ");
        tokens.put("<", "TK_REL_OP_MIN");
        tokens.put(">", "TK_REL_OP_MAY");
        tokens.put("<=", "TK_REL_OP_LEQ");
        tokens.put(">=", "TK_REL_OP_GEQ");
        tokens.put("+", "TK_ADD_OP_SUM");
        tokens.put("-", "TK_ADD_OP_REST");
        tokens.put("*", "TK_MULT_OP_POR");
        tokens.put("/", "TK_MULT_OP_DIV");
        tokens.put("(", "TK_OPAR");
        tokens.put(")", "TK_CPAR");
        tokens.put(":", "TK_TPOINTS");
        tokens.put(";", "TK_ENDSTNC");
        tokens.put(".", "TK_POINT");
        tokens.put(",", "TK_COMMA");
        return tokens;
    }

    /**
     * Genera un HashMap que asocia las palabras reservadas del lenguaje Pascal
     * con sus respectivos token.
     *
     * @return HashMap (patrón, token)
     */
    public static HashMap<String, String> generarTokensPalabras() {
        HashMap<String, String> tokens = new HashMap<>();
        tokens.put("INTEGER", "TK_TYPE_INT");
        tokens.put("BOOLEAN", "TK_TYPE_BOOL");
        tokens.put("TRUE", "TK_BOOLEAN_TRUE");
        tokens.put("FALSE", "TK_BOOLEAN_FALSE");
        tokens.put("AND", "TK_BOOL_OP_AND");
        tokens.put("OR", "TK_BOOL_OP_OR");
        tokens.put("NOT", "TK_NOT_OP");
        tokens.put("IF", "TK_IF");
        tokens.put("THEN", "TK_THEN");
        tokens.put("ELSE", "TK_ELSE");
        tokens.put("WHILE", "TK_WHILE");
        tokens.put("DO", "TK_DO");
        tokens.put("PROGRAM", "TK_PROGRAM");
        tokens.put("BEGIN", "TK_BEGIN");
        tokens.put("END", "TK_END");
        tokens.put("VAR", "TK_VAR");
        tokens.put("PROCEDURE", "TK_PROCEDURE");
        tokens.put("FUNCTION", "TK_FUNCTION");
        tokens.put("READ", "TK_READ");
        tokens.put("WRITE", "TK_WRITE");
        return tokens;
    }
    
}
