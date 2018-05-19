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
        LinkedList<ErrorLexico> errores = lexico.getErroresLexicos();

        for (ErrorLexico error : errores) {
            System.out.println(error.tratamientoSugerido());
        }

        int i = 1;
        for (Token token : tokens) {
            System.out.print("<" + token.getNombre() + ">");
            if (i % 6 == 0) {
                System.out.println("");
            }
            i++;
        }
        System.out.println();
        for (Token token : tokens) {
            System.out.print(token.getValor() + " ");
        }
    }

}
