package compiladorpascal;

import compiladorpascal.lexico.*;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

/**
 *
 * @author Martin Bermudez y Giuliano Marinelli
 */
public class CompiladorPascal {

    /**
     *
     * @param args: args[0] es la direccion del archivo .ext a compilar
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Falta indicar el archivo de entrada");
        } else {
            File fuente = new File(args[0]);

            //prueba con archivodeprueba.ext
            //File fuente = new File("src/compiladorpascal/archivodeprueba.ext");
            //llamada al anlizador léxico que retornará una lista de tokens
            LinkedList<Token> tokens = AnalizadorLexico.analizar(fuente);

            //muestra los tokens devueltos
            int i = 1;
            for (Token token : tokens) {
                System.out.print("<" + token.getNombre() + ">");
                if (i % 6 == 0) {
                    System.out.println("");
                }
                i++;
            }
            System.out.println();

            //muestra el código recreado a partir de los tokens
            for (Token token : tokens) {
                System.out.print(token.getValor() + " ");
            }
        }
    }

}
