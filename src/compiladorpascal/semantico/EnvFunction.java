/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package compiladorpascal.semantico;

import java.util.LinkedList;

/**
 *
 * @author Martin
 */
public class EnvFunction extends Environment {

    private String type;

    public EnvFunction(String nom, int linea, String ty) {
        super(nom, linea);
        type = ty;
        parametros = new LinkedList<>();
    }

    public String getType() {
        return type;
    }

}
