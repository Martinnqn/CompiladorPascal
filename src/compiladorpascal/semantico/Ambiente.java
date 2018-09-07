/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiladorpascal.semantico;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 *
 * @author Martin
 */
public class Ambiente {

    Ambiente padre; //ambiente padre
    //puede ser program, function o procedure
    private String typeEnv;
    //nombre del ambiente
    private String nombre;
    //Asocia un identificador a su type Void para procedimientos
    private HashMap<String, String> tipos;
    //Asocia un nombre de un identificador de funcion o procedimiento a su lista 
    //de parametros (solo el type de los parametros), como <nombreFuncion, parametros>
    private HashMap<String, LinkedList<String>> parametros;
    //identificador que esta en conflicto de unicidad
    private String identConflicto;

    public Ambiente(String type, String nam, Ambiente p) {
        typeEnv = type;
        padre = p;
        nombre = nam;
        tipos = new HashMap<>();
        parametros = new HashMap<>();
    }

    public String getTypeEnv() {
        return typeEnv;
    }

    public void setTypeEnv(String typeEnv) {
        this.typeEnv = typeEnv;
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
     * @param ident
     * @return
     */
    public LinkedList<String> getParametros(String ident) {
        return parametros.get(ident);
    }

    public void setParametros(String identificador, LinkedList<String> param) {
        parametros.put(identificador, param);
    }

    public void addParametro(String identificador, String type) {
        parametros.get(identificador).add(type);
    }

    public String getConflicto() {
        return identConflicto;
    }

    public Ambiente getPadre() {
        return padre;
    }

    /**
     * Devuelve el tipo de un identificador. Se recorre los ancestros del
     * identificador hasta hallarlo o hasta null.
     *
     * @param id
     * @return
     */
    public String getType(String id) {
        String type = tipos.get(id);
        if (type == null && padre != null) {
            type = padre.getType(id);
        }
        return type;
    }

    /**
     * Asocia un identificador a su type.
     *
     * @param id
     * @param clase
     * @param type
     * @return
     */
    public void addIdentificador(String id, String type) {
        tipos.put(id, type);
    }

    /**
     * Asocia un identificador a su type y le crea una lista con parametros
     * vacios.
     *
     * @param id
     * @param type
     */
    public void addFunction(String id, String type) {
        tipos.put(id, type);
        parametros.put(id, new LinkedList<>());
    }

    /**
     * Asocia un identificador y le crea una lista con parametros vacios.
     *
     * @param id
     */
    public void addProcedure(String id) {
        tipos.put(id, "VOID");
        parametros.put(id, new LinkedList<>());
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
        LinkedList<String> param2 = parametros.get(ident);
        if (param2 != null) {
            if (param2.size() == param.size()) {
                int i = 0;
                res = true;
                while (i < param2.size() && res) {
                    res = param2.get(i).equals(param.get(i));
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
        for (Map.Entry<String, String> entry : tipos.entrySet()) {
            String ident = entry.getKey();
            String type = entry.getValue();
            if (type.equals("VOID")) {
                res += "procedure " + ident + " (" + parametros.get(ident).toString() + "), ";
            } else {
                if (parametros.get(ident) != null) {
                    res += "funcion " + ident + " (" + parametros.get(ident).toString() + "): " + tipos.get(ident) + ", ";
                } else {
                    res += "identificador " + ident + ":" + type + ", ";
                }
            }
        }
        res = res.substring(0, res.length() - 2);
        return res;
    }
}
