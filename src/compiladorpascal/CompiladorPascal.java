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
        LinkedList<ErroresLexicos> errores = lexico.getErroresLexicos();

        for (ErroresLexicos error : errores) {
            System.out.print(error.tratamientoSugerido());
        }

        for (Token token : tokens) {
            System.out.print("<" + token.getNombre() + ">");
        }
        System.out.println();
        for (Token token : tokens) {
            System.out.print(token.getValor() + " ");
        }
    }

}
