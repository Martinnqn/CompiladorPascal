package compiladorpascal.semantico;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 *
 * @author Martin Bermudez y Giuliano Marinelli
 */
public class Ambiente {

    //ambiente padre
    Ambiente padre;
    //puede ser program, function o procedure
    private String tipoAmbiente;
    //nombre del ambiente
    private String nombre;
    //Asocia un identificador a su type Void para procedimientos
    private HashMap<String, String> tipos;
    //Asocia un nombre de un identificador de funcion o procedimiento a su lista 
    //de parametros (solo el type de los parametros), como <nombreFuncion, parametros>
    private HashMap<String, LinkedList<String>> parametros;

    public Ambiente(String tipoAmbiente, String nombre, Ambiente padre) {
        this.tipoAmbiente = tipoAmbiente;
        this.padre = padre;
        this.nombre = nombre;
        this.tipos = new HashMap<>();
        this.parametros = new HashMap<>();
    }

    public Ambiente getPadre() {
        return padre;
    }

    public String getTipoAmbiente() {
        return tipoAmbiente;
    }

    public void setTipoAmbiente(String tipoAmbiente) {
        this.tipoAmbiente = tipoAmbiente;
    }

    public String getNombre() {
        return nombre;
    }

    public HashMap<String, String> getTipos() {
        return tipos;
    }

    /**
     * Devuelve una lista de parametros para el identificador dado. Si el ident
     * no es una subrutina entonces devuelve null
     *
     * @param id
     * @return
     */
    public LinkedList<String> getParametros(String id) {
        LinkedList<String> par = parametros.get(id.toUpperCase());
        if (par == null && padre != null){
            par = padre.getParametros(id);
        }
        return par;
    }

    public void setParametros(String id, LinkedList<String> parametros) {
        this.parametros.put(id.toUpperCase(), parametros);
    }

    public void addParametro(String id, String tipo) {
        parametros.get(id.toUpperCase()).add(tipo.toUpperCase());
    }

    /**
     * Asocia un identificador a su type.
     *
     * @param id
     * @param tipo
     */
    public void addVariable(String id, String tipo) {
        tipos.put(id.toUpperCase(), tipo.toUpperCase());
    }

    /**
     * Asocia un identificador a su type y le crea una lista con parametros
     * vacios.
     *
     * @param id
     * @param tipo
     */
    public void addFunction(String id, String tipo) {
        tipos.put(id.toUpperCase(), tipo.toUpperCase());
        parametros.put(id.toUpperCase(), new LinkedList<>());
    }

    /**
     * Asocia un identificador y le crea una lista con parametros vacios.
     *
     * @param id
     */
    public void addProcedure(String id) {
        tipos.put(id.toUpperCase(), "VOID");
        parametros.put(id.toUpperCase(), new LinkedList<>());
    }

    /**
     * Devuelve el tipo de un identificador. Se recorre los ancestros del
     * identificador hasta hallarlo o hasta null.
     *
     * @param id
     * @return
     */
    public String getTipo(String id) {
        String tipo = tipos.get(id.toUpperCase());
        if (tipo == null && padre != null) {
            tipo = padre.getTipo(id);
        }
        return tipo;
    }

    /**
     * Devuelve true si encuentra la signatura de un ambiente igual a la
     * recibida por parametro. Si no la encuentra en el ambiente actual, recorre
     * sus ancestros.
     *
     * @param ident
     * @param param
     * @return
     */
    public boolean equals(String ident, LinkedList<String> param) {
        boolean res = false;
        LinkedList<String> param2 = parametros.get(ident.toUpperCase());
        if (param2 != null) {
            if (param2.size() == param.size()) {
                int i = 0;
                res = true;
                while (i < param2.size() && res) {
                    res = param2.get(i).equalsIgnoreCase(param.get(i));
                    i++;
                }
            }
        }
        if (!res) {
            if (padre != null) {
                res = padre.equals(ident, param);
            }
        }
        return res;
    }

    @Override
    public String toString() {
        String res = "";
        if (!tipos.isEmpty()) {
            for (Map.Entry<String, String> entry : tipos.entrySet()) {
                String ident = entry.getKey();
                String type = entry.getValue();
                if (type.equals("VOID")) {
                    res += "procedure " + ident + " (" + parametros.get(ident.toUpperCase()).toString() + "), ";
                } else {
                    if (parametros.get(ident.toUpperCase()) != null) {
                        res += "funcion " + ident + " (" + parametros.get(ident.toUpperCase()).toString() + "): " + tipos.get(ident.toUpperCase()) + ", ";
                    } else {
                        res += "identificador " + ident + ":" + type + ", ";
                    }
                }
            }
            res = res.substring(0, res.length() - 2);
        } else {
            res = "sin identificadores locales.";
        }
        return res;
    }
}
