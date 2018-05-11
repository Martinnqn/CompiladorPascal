/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiladorpascal;

import compiladorpascal.analizadorlexico.AnalizadorLexico;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 *
 * @author Martin
 */
public class CompiladorPascal {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        File path = new File(args[0]);
        HashMap<String, String> palabrasReservadas = null;
        cargarPalabrasReservadas(palabrasReservadas);
        AnalizadorLexico al = new AnalizadorLexico(path, palabrasReservadas);
        al.iniciarAnalisis();
    }

    public static void cargarPalabrasReservadas(HashMap<String, String> palabrasR) {
        palabrasR.put("if", Token.TK_IF);
        palabrasR.put("while", Token.TK_WHILE);
        //etc...
    }

}
