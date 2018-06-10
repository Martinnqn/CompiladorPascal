package compiladorpascal;

import compiladorpascal.lexico.*;
import compiladorpascal.sintactico.AnalizadorSintactico;
import java.io.File;
import java.io.IOException;

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
            //File fuente = new File("src/compiladorpascal/files/archivodeprueba.ext");

            //crea un objeto de analizador léxico
            AnalizadorLexico lexico = new AnalizadorLexico(fuente);

            //imprime los token que va obteniendo del analizador léxico
            /*Token token;
            while ((token = lexico.tokenSiguiente()) != null) {
                System.out.print("<" + token.getNombre() + "|" + token.getValor() + "> ");
                //System.out.print("<\033[32m" + token.getNombre() + "\033[30m|\033[36m" + token.getValor() + "\033[30m>");
            }*/
            
            AnalizadorSintactico sintactico = new AnalizadorSintactico(lexico);
            sintactico.program();
        }
    }

}
