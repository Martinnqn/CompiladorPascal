package compiladorpascal.lexico;

import java.util.HashMap;
import java.util.LinkedList;

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

       public static String parametrosALexema(LinkedList<String> tkParametros) {
        String res = "";
        for (int i = 0; i < tkParametros.size(); i++) {
            res += tokenALexema(tkParametros.get(i)) + ", ";
        }
        if (res.length() > 2) {
            res = res.substring(0, res.length() - 2);
        }
        return res;
    }

    /**
     * Recibe un Token y devuelve el lexema correspondiente, para imprimir los
     * mensajes de error
     *
     * @param tk
     * @return
     */
    public static String tokenALexema(String tk) {
        switch (tk) {
            case "TK_TYPE_INT":
                tk = "integer";
                break;
            case "TK_TYPE_BOOL":
                tk = "boolean";
                break;
            case "TK_ASSIGN":
                tk = "asignacion";
                break;
            case "TK_REL_OP_EQ":
                tk = "=";
                break;
            case "TK_REL_OP_NEQ":
                tk = "<>";
                break;
            case "TK_REL_OP_MIN":
                tk = "<";
                break;
            case "TK_REL_OP_MAY":
                tk = ">";
                break;
            case "TK_REL_OP_LEQ":
                tk = "<=";
                break;
            case "TK_REL_OP_GEQ":
                tk = ">=";
                break;
            case "TK_ADD_OP_SUM":
                tk = "+";
                break;
            case "TK_ADD_OP_REST":
                tk = "-";
                break;
            case "TK_MULT_OP_POR":
                tk = "*";
                break;
            case "TK_MULT_OP_DIV":
                tk = "/";
                break;
            case "TK_BOOL_OP_AND":
                tk = "AND";
                break;
            case "TK_BOOL_OP_OR":
                tk = "OR";
                break;
            case "TK_NOT_OP":
                tk = "NOT";
                break;
            case "TK_END":
                tk = "end";
        }
        return tk;
    }
}
