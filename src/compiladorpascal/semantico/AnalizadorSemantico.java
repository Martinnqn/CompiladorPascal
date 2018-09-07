package compiladorpascal.semantico;

import compiladorpascal.lexico.*;
import java.util.LinkedList;

/**
 *
 * @author Martin Bermudez y Giuliano Marinelli
 */
public class AnalizadorSemantico {

    private Ambiente tablaActual;
//    private LinkedList<String> vars;
    private AnalizadorLexico lexico;
    private Token preanalisis;

    public AnalizadorSemantico(AnalizadorLexico lex) {
        lexico = lex;
        preanalisis = lexico.tokenSiguiente();
    }

    /**
     * Verifica si el String terminal unifica con el simbolo de preanalisis
     * devuelto por el analizador lexico. Si no unifican lanza error sintactico.
     *
     * @param terminal
     */
    private void match(String terminal) {
        //System.out.print("\033[32m");
        //System.out.print("<" + terminal + ">");
        //System.out.print("\033[30m");
        if (preanalisis.getNombre().equals(terminal)) {
            //System.out.print("<" + preanalisis.getNombre() + ">");
            preanalisis = lexico.tokenSiguiente();
            if (preanalisis == null && !terminal.equals("TK_POINT")) {
                errorSintactico();
            }
        } else {
            errorSintactico(terminal);
        }
    }

    public void program() {
        if (preanalisis.getNombre().equals("TK_PROGRAM")) {
            program_heading();
            block();
            match("TK_POINT");
            mostrarPila(tablaActual);
            System.out.println("------------------ desapila ambiente del programa principal -----------------");
            //se elimina la  tabla de simbolos del programa
            tablaActual = null;
        } else {
            errorSintactico("TK_PROGRAM");
        }
    }

    private void program_heading() {
        if (preanalisis.getNombre().equals("TK_PROGRAM")) {
            match("TK_PROGRAM");
            String nombre = identifier();
            //se crea la tabla de simbolos para el ambiente del programa
            tablaActual = new Ambiente("TK_PROGRAM", nombre, null);
            match("TK_ENDSTNC");
        } else {
            errorSintactico("TK_PROGRAM");
        }
    }

    private void block() {
        switch (preanalisis.getNombre()) {
            case "TK_VAR":
            case "TK_PROCEDURE":
            case "TK_FUNCTION":
                declaration_block();
                multiple_statement();
                break;
            case "TK_BEGIN":
                multiple_statement();
                break;
            default:
                errorSintactico("TK_VAR o TK_PROCEDURE o TK_FUNCTION o TK_BEGIN");
                break;
        }
    }

    private void declaration_block() {
        switch (preanalisis.getNombre()) {
            case "TK_VAR":
                variable_declaration_block();
                declaration_block_1();
                break;
            case "TK_PROCEDURE":
            case "TK_FUNCTION":
                declaration_block_1();
                break;
            default:
                errorSintactico("TK_VAR o TK_PROCEDURE o TK_FUNCTION");
                break;
        }
    }

    private void declaration_block_1() {
        switch (preanalisis.getNombre()) {
            case "TK_PROCEDURE":
            case "TK_FUNCTION":
                procedure_and_function_declaration_list();
                break;
        }
    }

    private void variable_declaration_block() {
        if (preanalisis.getNombre().equals("TK_VAR")) {
            match("TK_VAR");
            variable_declaration_list();
        } else {
            errorSintactico("TK_VAR");
        }
    }

    private void variable_declaration_list() {
        if (preanalisis.getNombre().equals("TK_ID")) {
            variable_declaration();
            match("TK_ENDSTNC");
            variable_declaration_list_1();
        } else {
            errorSintactico("TK_ID");
        }
    }

    private void variable_declaration_list_1() {
        if (preanalisis.getNombre().equals("TK_ID")) {
            variable_declaration_list();
        }
    }

    private void variable_declaration() {
        if (preanalisis.getNombre().equals("TK_ID")) {
            LinkedList<String> idents = new LinkedList<>();
            identifier_list(idents);
            match("TK_TPOINTS");
            String type = type();
            //carga los identificadores y sus tipos.
            for (String id : idents) {
                //chequear unicidad
                if (tablaActual.getTipos().containsKey(id) || tablaActual.getNombre().equals(id)) {
                    errorSemantico("unicidad", id);
                } else {
                    tablaActual.addIdentificador(id, type);
                }
            }
        } else {
            errorSintactico("TK_ID");
        }
    }

    private void procedure_and_function_declaration_list() {
        switch (preanalisis.getNombre()) {
            case "TK_PROCEDURE":
                procedure_declaration();
                match("TK_ENDSTNC");
                procedure_and_function_declaration_list_1();
                break;
            case "TK_FUNCTION":
                function_declaration();
                match("TK_ENDSTNC");
                procedure_and_function_declaration_list_1();
                break;
            default:
                errorSintactico("TK_PROCEDURE o TK_FUNCTION");
                break;
        }
    }

    private void procedure_and_function_declaration_list_1() {
        switch (preanalisis.getNombre()) {
            case "TK_PROCEDURE":
            case "TK_FUNCTION":
                procedure_and_function_declaration_list();
                break;
        }
    }

    private void procedure_declaration() {
        if (preanalisis.getNombre().equals("TK_PROCEDURE")) {
            procedure_heading();
            match("TK_ENDSTNC");
            block();
            //cuando termina el block del procedure, se puede eliminar su tabla de simbolos
            mostrarPila(tablaActual);
            System.out.println("------------------ desapila ambiente " + tablaActual.getNombre() + " -----------------");
            tablaActual = tablaActual.getPadre();
        } else {
            errorSintactico("TK_PROCEDURE");
        }
    }

    private void procedure_heading() {
        if (preanalisis.getNombre().equals("TK_PROCEDURE")) {
            match("TK_PROCEDURE");
            String nombre = identifier();
            LinkedList<LinkedList<String>> listaParametros = new LinkedList<>();
            parameters(listaParametros);
            //se agrega el identificador al padre
            if (tablaActual.getTipos().containsKey(nombre)) {
                errorSemantico("unicidad", nombre);
            } else {
                tablaActual.addProcedure(nombre);
            }
            //se crea la nueva tabla para el ambiente actual del procedimiento
            Ambiente padre = tablaActual;
            tablaActual = new Ambiente("TK_PROCEDURE", nombre, padre);
            String id;
            String type;
            int i = 0;
            LinkedList<String> aux;
            while (i < listaParametros.size()) {
                aux = listaParametros.get(i);
                type = aux.get(0);
                for (int j = 1; j < aux.size(); j++) {
                    id = aux.get(j);
                    if (tablaActual.getTipos().containsKey(id) || tablaActual.getNombre().equals(id)) {
                        errorSemantico("unicidad", id);
                    } else {
                        tablaActual.addIdentificador(id, type);
                        //se le asigna al padre 
                        tablaActual.getPadre().addParametro(nombre, type);
                    }
                }
                i++;
            }
        } else {
            errorSintactico("TK_PROCEDURE");
        }
    }

    private void function_declaration() {
        if (preanalisis.getNombre().equals("TK_FUNCTION")) {
            function_heading();
            match("TK_ENDSTNC");
            block();
            //cuando termina el block de function, se puede eliminar su tabla de simbolos
            mostrarPila(tablaActual);
            System.out.println("------------------ desapila ambiente " + tablaActual.getNombre() + " -----------------");
            tablaActual = tablaActual.getPadre();
        } else {
            errorSintactico("TK_FUNCTION");
        }
    }

    private void function_heading() {
        String nombre;
        String type;
        if (preanalisis.getNombre().equals("TK_FUNCTION")) {
            match("TK_FUNCTION");
            nombre = identifier();
            LinkedList<LinkedList<String>> listaParametros = new LinkedList<>();
            parameters(listaParametros);
            match("TK_TPOINTS");
            type = type();
            //se agrega el identificador y type de esa funcion al padre
            if (tablaActual.getTipos().containsKey(nombre)) {
                errorSemantico("unicidad", nombre);
            } else {
                tablaActual.addFunction(nombre, type);
            }
            //se crea el ambiente para esa funcion
            Ambiente p = tablaActual;
            tablaActual = new Ambiente("TK_FUNCTION", nombre, p);
            //se asignan los parametros a la funcion, y se insertan los tipos de 
            //los parametros en el padre
            String id;
            int i = 0;
            LinkedList<String> aux;
            while (i < listaParametros.size()) {
                aux = listaParametros.get(i);
                type = aux.get(0);
                for (int j = 1; j < aux.size(); j++) {
                    id = aux.get(j);
                    if (tablaActual.getTipos().containsKey(id) || tablaActual.getNombre().equals(id)) {
                        errorSemantico("unicidad", id);
                    } else {
                        tablaActual.addIdentificador(id, type);
                        //se le asigna al padre 
                        tablaActual.getPadre().addParametro(nombre, type);
                    }
                }
                i++;
            }
        } else {
            errorSintactico("TK_FUNCTION");
        }
    }

    private void parameters(LinkedList<LinkedList<String>> listaParametros) {
        if (preanalisis.getNombre().equals("TK_OPAR")) {
            match("TK_OPAR");
            parameters_2(listaParametros);
            match("TK_CPAR");
        }
    }

    private void parameters_2(LinkedList<LinkedList<String>> listaParametros) {
        if (preanalisis.getNombre().equals("TK_ID")) {
            parameter_declaration_list(listaParametros);
        }
    }

    private void parameter_declaration_list(LinkedList<LinkedList<String>> listaParametros) {
        if (preanalisis.getNombre().equals("TK_ID")) {
            parameter_declaration(listaParametros);
            parameter_declaration_list_1(listaParametros);
        } else {
            errorSintactico("TK_ID");
        }
    }

    private void parameter_declaration_list_1(LinkedList<LinkedList<String>> listaParametros) {
        if (preanalisis.getNombre().equals("TK_COMMA")) {
            match("TK_COMMA");
            parameter_declaration_list(listaParametros);
        }
    }

    /**
     * Carga una lista de identificadores, y agrega como primer elemento el type
     * de los identificadores, y agrega la lista en listaParametros
     *
     * @param listaParametros
     */
    private void parameter_declaration(LinkedList<LinkedList<String>> listaParametros) {
        if (preanalisis.getNombre().equals("TK_ID")) {
            LinkedList<String> idents = new LinkedList<>();
            identifier_list(idents);
            match("TK_TPOINTS");
            String type = type();
            idents.addFirst(type);
            listaParametros.add(idents);
        } else {
            errorSintactico("TK_ID");
        }
    }

    private void statement_block() {
        switch (preanalisis.getNombre()) {
            case "TK_ID":
            case "TK_WRITE":
            case "TK_READ":
            case "TK_IF":
            case "TK_WHILE":
                statement();
                break;
            case "TK_BEGIN":
                multiple_statement();
                break;
        }
    }

    private void multiple_statement() {
        if (preanalisis.getNombre().equals("TK_BEGIN")) {
            match("TK_BEGIN");
            statement_list();
            match("TK_END");
        } else {
            errorSintactico("TK_BEGIN");
        }
    }

    private void statement_list() {
        switch (preanalisis.getNombre()) {
            case "TK_ID":
            case "TK_WRITE":
            case "TK_READ":
            case "TK_IF":
            case "TK_WHILE":
                statement();
                statement_list_1();
                break;
            default:
                errorSintactico("TK_ID o TK_WRITE o TK_READ o TK_IF o TK_WHILE");
                break;
        }
    }

    private void statement_list_1() {
        if (preanalisis.getNombre().equals("TK_ENDSTNC")) {
            match("TK_ENDSTNC");
            statement_list();
        }
    }

    private void statement() {
        switch (preanalisis.getNombre()) {
            case "TK_ID":
            case "TK_WRITE":
            case "TK_READ":
                simple_statement();
                break;
            case "TK_IF":
            case "TK_WHILE":
                structured_statement();
                break;
            default:
                errorSintactico("TK_ID o TK_WRITE o TK_READ o TK_IF o TK_WHILE");
                break;
        }
    }

    private void simple_statement() {
        switch (preanalisis.getNombre()) {
            case "TK_ID":
                String id = identifier();
                simple_statement_1(id);
                break;
            case "TK_WRITE":
                match("TK_WRITE");
                call_procedure_or_function("TK_WRITE");
                break;
            case "TK_READ":
                match("TK_READ");
                call_procedure_or_function("TK_READ");
                break;
            default:
                errorSintactico("TK_ID o TK_WRITE o TK_READ");
                break;
        }
    }

    /**
     * Devuelve un type = null si no entra a ningun case.
     *
     * @param id
     * @return
     */
    private void simple_statement_1(String id) {
        switch (preanalisis.getNombre()) {
            case "TK_ASSIGN":
                String type = tablaActual.getType(id);
                //verificar si el identificador es un identificador declarado en el ambiente,
                //o es un identificador que sirve como retorno dentro de una funcion.
                if (id.equalsIgnoreCase(tablaActual.getNombre())) {
                    if (tablaActual.getTypeEnv().equals("TK_PROCEDURE")) {
                        errorSemantico("id", "El identificador no es valido");
                    } else if (tablaActual.getTypeEnv().equals("TK_FUNCTION")) {
                        System.out.println("Asignacion de retorno "
                                + "dentro de una funcion en la linea " + lexico.getNroLinea() + ".");
                    }
                } else if (type != null && tablaActual.getParametros(id) != null) {
                    //puede que sea un identificador declarado, pero que sea una funcion o procedimiento dentro del ambiente.
                    errorSemantico("id", "El identificador " + id + " no es valido para una asignacion");
                }
                //si no es ningun caso, todavia puede ser un identificador no declarado
                if (type == null) {
                    errorSemantico("id", "Identificador no declarado.");
                }
                assignment_statement(type);
                break;
            case "TK_OPAR":
                call_procedure_or_function(id);
                break;
            default:
                //si entra aca es porque la forma de la sentencia es "identificador;". 
                //Verificar que ese id sea una funcion sin parametros.
                if (tablaActual.getParametros(id) == null) {
                    errorSemantico("no_subrutina", "El identificador '" + id + "' no corresponde a una subrutina declarada. ¿Faltan argumentos?");
                }
        }
    }

    private void structured_statement() {
        switch (preanalisis.getNombre()) {
            case "TK_IF":
                conditional_statement();
                break;
            case "TK_WHILE":
                repetitive_statement();
                break;
            default:
                errorSintactico("TK_IF o TK_WHILE");
                break;
        }
    }

    private void assignment_statement(String type1) {
        if (preanalisis.getNombre().equals("TK_ASSIGN")) {
            match("TK_ASSIGN");
            String type2 = expression_or();
            if (!(type1.equalsIgnoreCase(type2))) {
                errorSemantico("type", "Se esperaba un " + type1 + " pero se encontró " + type2);
            }
        } else {
            errorSintactico("TK_ASSIGN");
        }
    }

    private void call_procedure_or_function(String id) {
        if (preanalisis.getNombre().equals("TK_OPAR")) {
            match("TK_OPAR");
            LinkedList<String> types = new LinkedList<>();
            call_procedure_or_function_1(types);
            if (!id.equalsIgnoreCase("TK_WRITE") && !id.equalsIgnoreCase("TK_READ")) {
                boolean res = tablaActual.equals(id, types);
                if (!res) {
                    errorSemantico("call", "La lista de parametros no coincide con la definicion de la subrutina");
                }
            }
            match("TK_CPAR");
        } else {
            errorSintactico("TK_OPAR");
        }
    }

    private void call_procedure_or_function_1(LinkedList<String> types) {
        switch (preanalisis.getNombre()) {
            case "TK_ID":
            case "TK_OPAR":
            case "TK_ADD_OP_REST":
            case "TK_NOT_OP":
            case "TK_BOOLEAN_TRUE":
            case "TK_BOOLEAN_FALSE":
            case "TK_NUMBER":
                expression_list(types);
                break;
        }
    }

    private void conditional_statement() {
        if (preanalisis.getNombre().equals("TK_IF")) {
            match("TK_IF");
            String type = expression_or();
            //creo que boolean ya no es necesario. revisar.
            if (!type.equalsIgnoreCase("TK_TYPE_BOOL") && !type.equalsIgnoreCase("boolean")) {
                errorSemantico("if", "Se espera una expresion booleana");
            }
            match("TK_THEN");
            statement_block();
            else_statement();
        } else {
            errorSintactico("TK_IF");
        }
    }

    private void else_statement() {
        if (preanalisis.getNombre().equals("TK_ELSE")) {
            match("TK_ELSE");
            statement_block();
        }
    }

    private void repetitive_statement() {
        if (preanalisis.getNombre().equals("TK_WHILE")) {
            match("TK_WHILE");
            String type = expression_or();
            //creo que boolean ya no es necesario. revisar.
            if (!type.equalsIgnoreCase("TK_TYPE_BOOL") && !type.equalsIgnoreCase("boolean")) {
                errorSemantico("while", "Se espera una expresion booleana");
            }
            match("TK_DO");
            statement_block();
        } else {
            errorSintactico("TK_WHILE");
        }
    }

    private void expression_list(LinkedList<String> types) {
        switch (preanalisis.getNombre()) {
            case "TK_ID":
            case "TK_OPAR":
            case "TK_ADD_OP_REST":
            case "TK_NOT_OP":
            case "TK_BOOLEAN_TRUE":
            case "TK_BOOLEAN_FALSE":
            case "TK_NUMBER":
                String type = expression_or();
                types.add(type);
                expression_list_1(types);
                break;
            default:
                errorSintactico("TK_ID o TK_OPAR o TK_ADD_OP_REST o TK_NOT_OP o TK_BOOLEAN_TRUE o TK_BOOLEAN_FALSE o TK_NUMBER");
                break;
        }
    }

    private void expression_list_1(LinkedList<String> types) {
        if (preanalisis.getNombre().equals("TK_COMMA")) {
            match("TK_COMMA");
            expression_list(types);
        }
    }

    private String expression_or() {
        String type = null;
        switch (preanalisis.getNombre()) {
            case "TK_ID":
            case "TK_OPAR":
            case "TK_ADD_OP_REST":
            case "TK_NOT_OP":
            case "TK_BOOLEAN_TRUE":
            case "TK_BOOLEAN_FALSE":
            case "TK_NUMBER":
                type = expression_and();
                type = expression_or_1(type);
                break;
            default:
                /*como este es el procedimiento más general desde el cual se producen
                las expresiones, se puede mandar un mensaje diciendo que es lo que
                se esperaba.*/
                errorSintactico("una expresión");
                break;
        }
        return type;
    }

    private String expression_or_1(String type) {
        if (preanalisis.getNombre().equals("TK_BOOL_OP_OR")) {
            match("TK_BOOL_OP_OR");
            String type2 = expression_and();
            if (!(type.equalsIgnoreCase(type2))) {
                errorSemantico("type", type + " y " + type2 + " no aplicables a operador OR");
            }
            type = "TK_TYPE_BOOL";
            type = expression_or_1(type);
        }
        return type;
    }

    private String expression_and() {
        String type = null;
        switch (preanalisis.getNombre()) {
            case "TK_ID":
            case "TK_OPAR":
            case "TK_ADD_OP_REST":
            case "TK_NOT_OP":
            case "TK_BOOLEAN_TRUE":
            case "TK_BOOLEAN_FALSE":
            case "TK_NUMBER":
                type = expression_rel();
                type = expression_and_1(type);
                break;
            default:
                errorSintactico("una expresión");
                break;
        }
        return type;
    }

    private String expression_and_1(String type) {
        if (preanalisis.getNombre().equals("TK_BOOL_OP_AND")) {
            match("TK_BOOL_OP_AND");
            String type2 = expression_rel();
            if (!(type.equalsIgnoreCase(type2))) {
                errorSemantico("type", type + " y " + type2 + " no aplicables a operador AND");
            }
            type = "TK_TYPE_BOOL";
            type = expression_and_1(type);
        }
        return type;
    }

    private String expression_rel() {
        String type = null;
        switch (preanalisis.getNombre()) {
            case "TK_ID":
            case "TK_OPAR":
            case "TK_ADD_OP_REST":
            case "TK_NOT_OP":
            case "TK_BOOLEAN_TRUE":
            case "TK_BOOLEAN_FALSE":
            case "TK_NUMBER":
                type = expression_add();
                type = expression_rel_1(type);
                break;
            default:
                errorSintactico("una expresión");
                break;
        }
        return type;
    }

    private String expression_rel_1(String type) {
        switch (preanalisis.getNombre()) {
            case "TK_REL_OP_EQ":
            case "TK_REL_OP_NEQ":
            case "TK_REL_OP_MIN":
            case "TK_REL_OP_MAY":
            case "TK_REL_OP_LEQ":
            case "TK_REL_OP_GEQ":
                String op = relational_operator();
                String type2 = expression_add();
                switch (op) {
                    case "TK_REL_OP_EQ":
                    case "TK_REL_OP_NEQ":
                        if (!(type.equalsIgnoreCase(type2))) {
                            errorSemantico("type", type + " y " + type2 + " no aplicables a operador " + op);
                        }
                        type = "TK_TYPE_BOOL";
                        break;
                    case "TK_REL_OP_MIN":
                    case "TK_REL_OP_MAY":
                    case "TK_REL_OP_LEQ":
                    case "TK_REL_OP_GEQ":
                        if (!((type.equalsIgnoreCase(type2)) && (type.equalsIgnoreCase("TK_TYPE_INT")))) {
                            errorSemantico("type", type + " y " + type2 + " no aplicables a operador " + op);
                        }
                        type = "TK_TYPE_BOOL";
                        break;
                }
                type = expression_rel_1(type);
                break;
        }
        return type;
    }

    private String expression_add() {
        String type = null;
        switch (preanalisis.getNombre()) {
            case "TK_ID":
            case "TK_OPAR":
            case "TK_ADD_OP_REST":
            case "TK_NOT_OP":
            case "TK_BOOLEAN_TRUE":
            case "TK_BOOLEAN_FALSE":
            case "TK_NUMBER":
                type = expression_mult();
                type = expression_add_1(type);
                break;
            default:
                errorSintactico("una expresión");
                break;
        }
        return type;
    }

    private String expression_add_1(String type) {
        switch (preanalisis.getNombre()) {
            case "TK_ADD_OP_SUM":
            case "TK_ADD_OP_REST":
                String op = addition_operator();
                String type2 = expression_mult();
                if (!((type.equalsIgnoreCase(type2)) && (type.equalsIgnoreCase("TK_TYPE_INT")))) {
                    errorSemantico("type", type + " y " + type2 + " no aplicables a operador " + op);
                }
                type = "TK_TYPE_INT";
                type = expression_add_1(type);
                break;
        }
        return type;
    }

    private String expression_mult() {
        String type = null;
        switch (preanalisis.getNombre()) {
            case "TK_ID":
            case "TK_OPAR":
            case "TK_ADD_OP_REST":
            case "TK_NOT_OP":
            case "TK_BOOLEAN_TRUE":
            case "TK_BOOLEAN_FALSE":
            case "TK_NUMBER":
                type = factor();
                type = expression_mult_1(type);
                break;
            default:
                errorSintactico("una expresión");
                break;
        }
        return type;
    }

    private String expression_mult_1(String type) {
        switch (preanalisis.getNombre()) {
            case "TK_MULT_OP_POR":
            case "TK_MULT_OP_DIV":
                String op = multiplication_operator();
                String type2 = factor();
                if (!((type.equalsIgnoreCase(type2)) && (type.equalsIgnoreCase("TK_TYPE_INT")))) {
                    errorSemantico("type", type + " y " + type2 + " no aplicables a operador " + op);
                }
                type = "TK_TYPE_INT";
                type = expression_mult_1(type);
                break;
        }
        return type;
    }

    private String factor() {
        String type = null;
        switch (preanalisis.getNombre()) {
            case "TK_ID":
                String id = identifier();
                type = tablaActual.getType(id);
                if (type == null) {
                    errorSemantico("id", "Identificador no declarado");
                }
                factor_1(id);
                break;
            case "TK_OPAR":
                match("TK_OPAR");
                type = expression_or();
                match("TK_CPAR");
                break;
            case "TK_ADD_OP_REST":
            case "TK_NOT_OP":
                String op = unary_operator();
                type = factor();
                if (op.equals("TK_NOT_OP")) {
                    if (!type.equalsIgnoreCase("TK_TYPE_BOOL")) {
                        errorSemantico("type", type + " no aplicables a operador " + op);
                    }
                    type = "TK_TYPE_BOOL";
                } else {
                    if (!type.equalsIgnoreCase("TK_TYPE_INT")) {
                        errorSemantico("type", type + " no aplicables a operador " + op);
                    }
                    type = "TK_TYPE_INT";
                }
                break;
            case "TK_BOOLEAN_TRUE":
            case "TK_BOOLEAN_FALSE":
            case "TK_NUMBER":
                type = literal();
                break;
            default:
                errorSintactico("un factor");
                break;
        }
        return type;
    }

    private void factor_1(String id) {
        if (preanalisis.getNombre().equals("TK_OPAR")) {
            call_procedure_or_function(id);
        }
    }

    private String relational_operator() {
        String op = preanalisis.getNombre();
        switch (preanalisis.getNombre()) {
            case "TK_REL_OP_EQ":
                match("TK_REL_OP_EQ");
                break;
            case "TK_REL_OP_NEQ":
                match("TK_REL_OP_NEQ");
                break;
            case "TK_REL_OP_MIN":
                match("TK_REL_OP_MIN");
                break;
            case "TK_REL_OP_MAY":
                match("TK_REL_OP_MAY");
                break;
            case "TK_REL_OP_LEQ":
                match("TK_REL_OP_LEQ");
                break;
            case "TK_REL_OP_GEQ":
                match("TK_REL_OP_GEQ");
                break;
            default:
                errorSintactico("un operador relacional");
                break;
        }
        return op;
    }

    private String unary_operator() {
        String op = preanalisis.getNombre();
        switch (preanalisis.getNombre()) {
            case "TK_ADD_OP_REST":
                match("TK_ADD_OP_REST");
                break;
            case "TK_NOT_OP":
                match("TK_NOT_OP");
                break;
            default:
                errorSintactico("TK_ADD_OP_REST o TK_NOT_OP");
                break;
        }
        return op;
    }

    private String addition_operator() {
        String op = preanalisis.getNombre();
        switch (preanalisis.getNombre()) {
            case "TK_ADD_OP_SUM":
                match("TK_ADD_OP_SUM");
                break;
            case "TK_ADD_OP_REST":
                match("TK_ADD_OP_REST");
                break;
            default:
                errorSintactico("TK_ADD_OP_SUM o TK_ADD_OP_REST");
                break;
        }
        return op;
    }

    private String multiplication_operator() {
        String op = preanalisis.getNombre();
        switch (preanalisis.getNombre()) {
            case "TK_MULT_OP_POR":
                match("TK_MULT_OP_POR");
                break;
            case "TK_MULT_OP_DIV":
                match("TK_MULT_OP_DIV");
                break;
            default:
                errorSintactico("TK_MULT_OP_POR o TK_MULT_OP_DIV");
                break;
        }
        return op;
    }

    private String type() {
        String type = preanalisis.getNombre();
        switch (preanalisis.getNombre()) {
            case "TK_TYPE_INT":
                match("TK_TYPE_INT");
                break;
            case "TK_TYPE_BOOL":
                match("TK_TYPE_BOOL");
                break;
            default:
                errorSintactico("un tipo de dato");
                break;
        }
        return type;
    }

    private void identifier_list(LinkedList<String> vars) {
        if (preanalisis.getNombre().equals("TK_ID")) {
            //se guarda el identificador declarado en vars para luego ser volcado 
            //en la tabla de simbolo con su tipo correspondiente.
            String val = identifier();
            vars.add(val);
            identifier_list_1(vars);
        } else {
            errorSintactico("TK_ID");
        }
    }

    private void identifier_list_1(LinkedList<String> vars) {
        if (preanalisis.getNombre().equals("TK_COMMA")) {
            match("TK_COMMA");
            identifier_list(vars);
        }
    }

    private String identifier() {
        String name = null;
        if (preanalisis.getNombre().equals("TK_ID")) {
            name = preanalisis.getValor();
            match("TK_ID");
        } else {
            errorSintactico("TK_ID");
        }
        return name;
    }

    //private void identifier_1() {}
    private String literal() {
        String type = null;
        switch (preanalisis.getNombre()) {
            case "TK_BOOLEAN_TRUE":
            case "TK_BOOLEAN_FALSE":
                bool();
                type = "TK_TYPE_BOOL";
                break;
            case "TK_NUMBER":
                type = "TK_TYPE_INT";
                number();
                break;
            default:
                errorSintactico("TK_BOOLEAN_TRUE o TK_BOOLEAN_FALSE o TK_NUMBER");
                break;
        }
        return type;
    }

    private void number() {
        if (preanalisis.getNombre().equals("TK_NUMBER")) {
            preanalisis.getNombre();
            match("TK_NUMBER");
        } else {
            errorSintactico("TK_NUMBER");
        }
    }

    //private void number_1() {}
    //private void word() {}
    //private void word_1() {}
    //private void letter() {}
    //private void digit() {}
    private void bool() {
        switch (preanalisis.getNombre()) {
            case "TK_BOOLEAN_TRUE":
                match("TK_BOOLEAN_TRUE");
                break;
            case "TK_BOOLEAN_FALSE":
                match("TK_BOOLEAN_FALSE");
                break;
            default:
                errorSintactico("TK_BOOLEAN_TRUE o TK_BOOLEAN_FALSE");
                break;
        }
    }

    /**
     * Lanza un RuntimeException("semantico", Causa).
     *
     * @param term
     */
    private void errorSemantico(String term, String msg) {
        if (term.equals("unicidad")) {
            throw new RuntimeException("semantico", new Throwable("\nError semantico: linea " + lexico.getNroLinea()
                    + ".\nIdentificador " + msg + " ya declarado en el ambiente."));
        } else {
            throw new RuntimeException("semantico", new Throwable("\nError semantico linea "
                    + lexico.getNroLinea() + ".\n" + msg));
        }
    }

    /**
     * Lanza un RuntimeException("semantico", Causa).
     *
     * @param term
     */
    private void errorSintactico(String term) {
        throw new RuntimeException("sintactico", new Throwable("\nError sintactico: linea " + lexico.getNroLinea()
                + " posicion " + (lexico.getPos() + 1) + ".\nSimbolo de preanalisis " + preanalisis.getNombre()
                + " no esperado. Se esperaba " + term));
    }

    /**
     * Lanza un RuntimeException("sintactico", Causa).
     */
    private void errorSintactico() {
        if (preanalisis != null) {
            throw new RuntimeException("sintactico", new Throwable("\nError sintactico: linea " + lexico.getNroLinea()
                    + " posicion " + (lexico.getPos() + 1) + ".\nSimbolo de preanalisis " + preanalisis.getNombre()
                    + " no esperado."));
        } else {
            throw new RuntimeException("sintactico", new Throwable("\nError sintactico: linea " + lexico.getNroLinea()
                    + " posicion " + (lexico.getPos() + 1) + ".\nFin del archivo alcanzado. "
                    + "Programa incompleto."));
        }
    }

    private void mostrarPila(Ambiente tabla) {
        if (tabla != null) {
            System.out.println("Ambiente: " + tabla.getNombre() + ": \n" + tabla.toString() + "\n");
            mostrarPila(tabla.getPadre());
        }
    }
}
