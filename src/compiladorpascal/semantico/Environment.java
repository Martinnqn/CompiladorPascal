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
public class Environment {

    protected String typeEnv; //puede ser program, function o procedure

    protected String nombre; //nombre del programa, la funcion o el procedure

    protected int lineaDeclaracion; //no se si es la linea o un puntero o que.
    //los parametros son una lista de tipos para comparar metodos sobrecargados. 
    //El nombre del identificador y su tipo tambien se encuentran en el hasmap identificadores
    protected LinkedList<String> parametros;
    protected HashMap<String, String> identificadores;
    protected HashMap<String, String> functionType; //hashmap <nombreProcedimiento,type>

    public Environment(String type, int linea) {
        typeEnv = type;
        lineaDeclaracion = linea;
        parametros = new LinkedList<>();
        identificadores = new HashMap<>();
        functionType = new HashMap<>();
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setLineaDeclaracion(int lineaDeclaracion) {
        this.lineaDeclaracion = lineaDeclaracion;
    }

    public int getLineaDeclaracion() {
        return lineaDeclaracion;
    }

    public String getTypeEnv() {
        return typeEnv;
    }

    public void setTypeEnv(String typeEnv) {
        this.typeEnv = typeEnv;
    }

    public LinkedList<String> getParametros() {
        return parametros;
    }

    public HashMap<String, String> getFunctionType() {
        return functionType;
    }

    public void setFunctionType(HashMap<String, String> functionType) {
        this.functionType = functionType;
    }

    public void setParametros(LinkedList<String> parametros) {
        this.parametros = parametros;
    }

    public void addIdentificador(String var, String type) {
        identificadores.put(var, type);
    }

    public void addFunctionType(String nameFunc, String type) {
        functionType.put(nameFunc, type);
    }

    public String getFunctionType(String nameFunc) {
        return identificadores.get(nameFunc);
    }

    public void addParametro(String var, String type) {
        identificadores.put(var, type);
        parametros.add(type);
    }

    public String getType(String var) {
        return identificadores.get(var);
    }

    public HashMap<String, String> getIdentificadores() {
        return identificadores;
    }

    /**
     * Devuelve true si la signatura de las declaraciones son iguales.
     *
     * @param env2
     * @return
     */
    public boolean equals(Environment env2) {
        boolean res = false;
        int size = parametros.size();
        if (nombre.equals(env2.getNombre())) {
            if (size == env2.getParametros().size()) {
                int i = 0;
                res = true;
                while (i < size && res) {
                    res = parametros.get(i).equals(env2.getParametros().get(i));
                    i++;
                }
            }
        }
        return res;
    }

    @Override
    public String toString() {
        String res = "";
        for (Map.Entry<String, String> entry : identificadores.entrySet()) {
            String ident = entry.getKey();
            String type = entry.getValue();
            if (type.equals("FUNCTION")) {
                res += "function " + ident + ": " + functionType.get(ident) + ", ";
            } else if (type.equals("PROCEDURE")) {
                res += "procedure " + ident + ", ";
            } else {
                res += "identificador " + ident + ":" + type + ", ";
            }
        }
        res = res.substring(0, res.length() - 2);
        return res;
    }
}
