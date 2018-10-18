package compiladorpascal.codigointermedio;

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
    //profundidad del ambiente
    private int profundidad = -1;
    //ultimo offset utilizado para una variable
    private int lastOffset = 0;
    //ultimo offset utilizado para un parametro
    private int lastParameterOffset = -3;
    //offset utilizado para el retorno de una funcion
    private int returnOffset = -4;
    //label de llamada para un procediemiento o funcion
    private int label = -1;
    //solo util para funciones, es true si se asigno un valor de retorno para la funcion.
    private boolean hasReturn;

    //Asocia un identificador a su type Void para procedimientos
    private HashMap<String, String> tipos;
    //Asocia un identificador de funcion o procedimiento a su lista 
    //de parametros (solo el type de los parametros), como <nombreFuncion, parametros>
    private HashMap<String, LinkedList<String>> parametros;
    //Asocia un identificador (variable) a su profundidad
    private HashMap<String, Integer> profundidades;
    //Asocia un identificador (variable) a su offset
    private HashMap<String, Integer> offsets;
    //Asocia un identificador a su clase (variable, parametro, funcion, procedimiento)
    private HashMap<String, String> clases;
    //Asocia un identificador de procedimiento o funcion a su label de llamada
    private HashMap<String, Integer> labels;

    public Ambiente(String tipoAmbiente, String nombre, Ambiente padre) {
        this.tipoAmbiente = tipoAmbiente;
        this.padre = padre;
        this.nombre = nombre;
        this.profundidad = padre != null ? padre.getProfundidad() + 1 : 0;
        //System.out.println(nombre + " - " + tipoAmbiente + " - profundidad: " + this.profundidad);
        this.tipos = new HashMap<>();
        this.parametros = new HashMap<>();
        this.profundidades = new HashMap<>();
        this.offsets = new HashMap<>();
        this.clases = new HashMap<>();
        this.labels = new HashMap<>();
    }

    public Ambiente(String tipoAmbiente, String nombre, Ambiente padre, int label, int parameterCount) {
        this(tipoAmbiente, nombre, padre);
        this.label = label;
        this.returnOffset = -parameterCount - 3;
        this.lastParameterOffset = -parameterCount - 2;
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

    public int getProfundidad() {
        return profundidad;
    }

    public boolean hasReturn() {
        return hasReturn;
    }

    public void setHasReturn(boolean val) {
        hasReturn = val;
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
        if (par == null && padre != null) {
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
     * Asocia un identificador a su type y profundidad.
     *
     * @param id
     * @param tipo
     */
    public void addVariable(String id, String tipo, int profundidad, boolean isParameter) {
        tipos.put(id.toUpperCase(), tipo.toUpperCase());
        profundidades.put(id.toUpperCase(), profundidad);
        if (isParameter) {
            offsets.put(id.toUpperCase(), lastParameterOffset++);
            clases.put(id.toUpperCase(), "parametro");
        } else {
            offsets.put(id.toUpperCase(), lastOffset++);
            clases.put(id.toUpperCase(), "variable");
        }
    }

    /**
     * Asocia un identificador a su type y le crea una lista con parametros
     * vacios.
     *
     * @param id
     * @param tipo
     */
    public void addFunction(String id, String tipo, int label) {
        tipos.put(id.toUpperCase(), tipo.toUpperCase());
        parametros.put(id.toUpperCase(), new LinkedList<>());
        clases.put(id.toUpperCase(), "funcion");
        labels.put(id.toUpperCase(), label);
    }

    /**
     * Asocia un identificador y le crea una lista con parametros vacios.
     *
     * @param id
     */
    public void addProcedure(String id, int label) {
        tipos.put(id.toUpperCase(), "VOID");
        parametros.put(id.toUpperCase(), new LinkedList<>());
        clases.put(id.toUpperCase(), "procedimiento");
        labels.put(id.toUpperCase(), label);
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
        if (!tipos.containsKey(id.toUpperCase()) && padre != null) {
            tipo = padre.getTipo(id);
        }
        return tipo;
    }

    public int getProfundidad(String id) {
        //System.out.println("profundidades: " + profundidades);
        //System.out.println("profundidad: " + profundidades.get(id.toUpperCase()));
        //System.out.println("exists: " + profundidades.containsKey(id.toUpperCase()));
        //System.out.println("exists padre: " + (!profundidades.containsKey(id.toUpperCase()) && padre != null));
        //System.out.println("profundidad de " + id + " en ambiente " + nombre);
        int profundidad = -1;
        if (id.toUpperCase().equals(nombre.toUpperCase())) {
            profundidad = this.profundidad;
        } else if (profundidades.containsKey(id.toUpperCase())) {
            profundidad = profundidades.get(id.toUpperCase());
        } else if (padre != null) {
            profundidad = padre.getProfundidad(id);
        }

        return profundidad;
    }

    public int getOffset(String id) {
        //System.out.println("offset de " + id + " en ambiente " + nombre);
        int offset = -1;
        if (id.toUpperCase().equals(nombre.toUpperCase())) {
            offset = returnOffset;
        } else if (offsets.containsKey(id.toUpperCase())) {
            offset = offsets.get(id.toUpperCase());
        } else if (padre != null) {
            offset = padre.getOffset(id);
        }
        return offset;
    }

    public String getClase(String id) {
        String clase = clases.get(id.toUpperCase());
        if (!clases.containsKey(id.toUpperCase()) && padre != null) {
            clase = padre.getClase(id);
        }
        return clase;
    }

    public int getLabel(String id) {
        //System.out.println("offset de " + id + " en ambiente " + nombre);
        int label = -1;
        if (id.toUpperCase().equals(nombre.toUpperCase())) {
            label = this.label;
        } else if (labels.containsKey(id.toUpperCase())) {
            label = labels.get(id.toUpperCase());
        } else if (padre != null) {
            label = padre.getLabel(id);
        }
        return label;
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
