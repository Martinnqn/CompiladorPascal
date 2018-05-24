package compiladorpascal.lexico;

/**
 * Estructura que almacena el nombre y valor (lexema) de un token.
 *
 * @author Martin Bermudez y Giuliano Marinelli
 */
public class Token {

    private String nombre;
    private String valor;

    /**
     * Contructor de un token.
     *
     * @param nombre
     * @param valor
     */
    public Token(String nombre, String valor) {
        this.nombre = nombre;
        this.valor = valor;
    }

    /**
     * El nombre de un token es del tipo "TK_X".
     *
     * @return nombre
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * El nombre de un token es del tipo "TK_X".
     *
     * @param nombre
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * El valor de un token es el lexema específico reconocido.
     *
     * @return lexema
     */
    public String getValor() {
        return valor;
    }

    /**
     * El valor de un token es el lexema específico reconocido.
     *
     * @param valor
     */
    public void setValor(String valor) {
        this.valor = valor;
    }

}
