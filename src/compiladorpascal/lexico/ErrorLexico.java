/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiladorpascal.lexico;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Martin
 */
public class ErrorLexico {

    private int line;
    private int pos;
    private String description;
    private String state;
    private String lexema;
    private char caracterCorte;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public ErrorLexico(int lin, int p, String desc, String st, String lex, char car) {
        line = lin;
        pos = p;
        description = desc;
        state = st;
        lexema = lex;
        caracterCorte = car;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String tratamientoSugerido() {
        String tratamiento = "Error linea " + line + " posicion " + pos + " ";
        switch (state) {
            case "symbol":
                tratamiento += tratamientoSymbol();
                break;
            case "letter":
                tratamiento += tratamientoLetter();
                break;
            case "start":
                tratamiento += tratamientoStart();
                break;
            case "digit":
                tratamiento += tratamientoDigit();
                break;
            default:
                tratamiento += "Error desconocido";
        }
        return tratamiento;
    }

    private String tratamientoSymbol() {
        String res = "Esperado: Variable o Literal, pero encontrado " + caracterCorte;
        return res;
    }

    private String tratamientoStart() {
        String res = "Esperado: Variable o Literal, pero encontrado " + caracterCorte;
        return res;
    }

    private String tratamientoLetter() {
        String res = "";
        int tamLex = lexema.length();
        HashMap<String, String> tokensPalabras = Tokens.generarTokensPalabras();
        Iterator<String> keys = tokensPalabras.keySet().iterator();
        boolean stap = false;
        String key;
        while ((key = keys.next()) != null && !stap) {
            if (key.length() >= tamLex) {
                if (key.substring(0, tamLex).equals(lexema)) {
                    stap = true;
                }
            }
        }
        if (stap) {
            res = "Caracter " + caracterCorte + " no reconocido en identificador. Quizá quiso decir " + key + ".";
        } else {
            res = "Caracter " + caracterCorte + " no permitido en identificador";
        }
        return res;
    }

    private String tratamientoDigit() {
        String res = "Esperaba la formación de un literal Integer, pero encontrado caracter "
                + "'" + caracterCorte + "' no permitido.";
        return res;
    }
}
