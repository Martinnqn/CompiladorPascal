package compiladorpascal;

import compiladorpascal.lexico.*;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

public class CompiladorPascal {

    public static void main(String[] args) throws IOException {
        //File fuente = new File(args[0]);
        File fuente = new File("src/compiladorpascal/archivodeprueba.ext");
        AnalizadorLexico lexico = new AnalizadorLexico();
        LinkedList<Token> tokens = lexico.analizar(fuente);
        for (Token token : tokens) {
            System.out.print("<" + token.getNombre() + ">");
        }
        System.out.println();
        for (Token token : tokens) {
            System.out.print(token.getValor() + " ");
        }
    }

}
