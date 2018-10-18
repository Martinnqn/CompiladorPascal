package compiladorpascal.codigointermedio;

import compiladorpascal.lexico.*;
import java.util.LinkedList;

/**
 *
 * @author Martin Bermudez y Giuliano Marinelli
 */
public class GeneradorCodigoIntermedio {

    private Ambiente ambiente;
    private AnalizadorLexico lexico;
    private Token preanalisis;

    private String mepa = "";
    private int mepaLabel = 0;

    public String getMepa() {
        return mepa;
    }

    public GeneradorCodigoIntermedio(AnalizadorLexico lex) {
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
                errorSintactico("");
            }
        } else {
            errorSintactico(terminal);
        }
    }

    public void program() {
        if (preanalisis.getNombre().equals("TK_PROGRAM")) {
            mepa += "INPP\n";
            program_heading();
            block();
            match("TK_POINT");
            mostrarPila(ambiente);
            //System.out.println("------------------ desapila ambiente del programa principal -----------------");
            //se elimina la  tabla de simbolos del programa
            ambiente = null;
            mepa += "PARA\n";
        } else {
            errorSintactico("inicio del programa (program)");
        }
    }

    private void program_heading() {
        if (preanalisis.getNombre().equals("TK_PROGRAM")) {
            match("TK_PROGRAM");
            String nombre = identifier();
            //se crea la tabla de simbolos para el ambiente del programa
            ambiente = new Ambiente("TK_PROGRAM", nombre, null);
            match("TK_ENDSTNC");
        } else {
            errorSintactico("inicio del programa (program)");
        }
    }

    private void block() {
        switch (preanalisis.getNombre()) {
            case "TK_VAR":
            case "TK_PROCEDURE":
            case "TK_FUNCTION":
                int varCount = declaration_block();
                multiple_statement();
                if (varCount > 0) {
                    mepa += "LMEM " + varCount + "\n";
                }
                break;
            case "TK_BEGIN":
                multiple_statement();
                break;
            default:
                errorSintactico("la declaracion o comienzo de un bloque (begin, var, procedure o function)");
                break;
        }
    }

    private int declaration_block() {
        int varCount = 0;
        switch (preanalisis.getNombre()) {
            case "TK_VAR":
                varCount = variable_declaration_block();
                if (varCount > 0) {
                    mepa += "RMEM " + varCount + "\n";
                }
                declaration_block_1();
                break;
            case "TK_PROCEDURE":
            case "TK_FUNCTION":
                declaration_block_1();
                break;
            default:
                errorSintactico("la declaracion de un bloque (var, procedure o function)");
                break;
        }
        return varCount;
    }

    private void declaration_block_1() {
        switch (preanalisis.getNombre()) {
            case "TK_PROCEDURE":
            case "TK_FUNCTION":
                procedure_and_function_declaration_list();
                break;
        }
    }

    private int variable_declaration_block() {
        int varCount = 0;
        if (preanalisis.getNombre().equals("TK_VAR")) {
            match("TK_VAR");
            varCount = variable_declaration_list(0);
        } else {
            errorSintactico("la declaracion de un bloque de variables (var)");
        }
        return varCount;
    }

    private int variable_declaration_list(int varCount) {
        if (preanalisis.getNombre().equals("TK_ID")) {
            varCount += variable_declaration();
            match("TK_ENDSTNC");
            varCount = variable_declaration_list_1(varCount);
        } else {
            errorSintactico("un identificador");
        }
        return varCount;
    }

    private int variable_declaration_list_1(int varCount) {
        if (preanalisis.getNombre().equals("TK_ID")) {
            varCount = variable_declaration_list(varCount);
        }
        return varCount;
    }

    private int variable_declaration() {
        int varCount = 0;
        if (preanalisis.getNombre().equals("TK_ID")) {
            LinkedList<String> idents = new LinkedList<>();
            identifier_list(idents);
            match("TK_TPOINTS");
            String type = type();
            //carga los identificadores y sus tipos.
            for (String id : idents) {
                //chequear unicidad
                if (ambiente.getTipos().containsKey(id.toUpperCase()) || ambiente.getNombre().equalsIgnoreCase(id)) {
                    errorSemantico("unicidad", id);
                } else {
                    ambiente.addVariable(id, type, ambiente.getProfundidad(), false);
                }
            }
            varCount = idents.size();
        } else {
            errorSintactico("un identificador");
        }
        return varCount;
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
                errorSintactico("la declaracion de un bloque de procedimiento o funcion (procedure o function)");
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
            mepaLabel++;
            int endProcedureLabel = mepaLabel;
            mepa += "DSVS L" + endProcedureLabel + "\n";
            mepaLabel++;
            int procedureLabel = mepaLabel;
            procedure_heading(procedureLabel);
            mepa += "L" + procedureLabel + " ENPR " + ambiente.getProfundidad() + "\n";
            match("TK_ENDSTNC");
            block();
            mepa += "RTPR " + ambiente.getProfundidad() + " " + ambiente.getPadre().getParametros(ambiente.getNombre()).size() + "\n";
            mepa += "L" + endProcedureLabel + " NADA\n";
            //cuando termina el block del procedure, se puede eliminar su tabla de simbolos
            mostrarPila(ambiente);
            //System.out.println("------------------ desapila ambiente " + ambiente.getNombre() + " -----------------");
            ambiente = ambiente.getPadre();
        } else {
            errorSintactico("la declaracion de un bloque de procedimiento (procedure)");
        }
    }

    private void procedure_heading(int procedureLabel) {
        if (preanalisis.getNombre().equals("TK_PROCEDURE")) {
            match("TK_PROCEDURE");
            String nombre = identifier();
            LinkedList<LinkedList<String>> listaParametros = new LinkedList<>();
            parameters(listaParametros);
            //se agrega el identificador al padre
            if (ambiente.getTipos().containsKey(nombre.toUpperCase())) {
                errorSemantico("unicidad", nombre);
            } else {
                ambiente.addProcedure(nombre, procedureLabel);
            }
            int parameterCount = 0;
            for (LinkedList<String> listaParametro : listaParametros) {
                for (String string : listaParametro) {
                    parameterCount++;
                }
                parameterCount--;
            }
            //se crea la nueva tabla para el ambiente actual del procedimiento
            Ambiente padre = ambiente;
            ambiente = new Ambiente("TK_PROCEDURE", nombre, padre, procedureLabel, parameterCount);
            String id;
            String type;
            int i = 0;
            LinkedList<String> aux;
            while (i < listaParametros.size()) {
                aux = listaParametros.get(i);
                type = aux.get(0);
                for (int j = 1; j < aux.size(); j++) {
                    id = aux.get(j);
                    if (ambiente.getTipos().containsKey(id.toUpperCase()) || ambiente.getNombre().equalsIgnoreCase(id)) {
                        errorSemantico("unicidad", id);
                    } else {
                        ambiente.addVariable(id, type, ambiente.getProfundidad(), true);
                        //se le asigna al padre 
                        ambiente.getPadre().addParametro(nombre, type);
                    }
                }
                i++;
            }
        } else {
            errorSintactico("la declaracion de un bloque de procedimiento (procedure)");
        }
    }

    private void function_declaration() {
        if (preanalisis.getNombre().equals("TK_FUNCTION")) {
            mepaLabel++;
            int endFunctionLabel = mepaLabel;
            mepa += "DSVS L" + endFunctionLabel + "\n";
            mepaLabel++;
            int functionLabel = mepaLabel;
            function_heading(functionLabel);
            mepa += "L" + functionLabel + " ENPR " + ambiente.getProfundidad() + "\n";
            match("TK_ENDSTNC");
            block();
            mepa += "RTPR " + ambiente.getProfundidad() + " " + ambiente.getPadre().getParametros(ambiente.getNombre()).size() + "\n";
            mepa += "L" + endFunctionLabel + " NADA\n";
            //si regresa de block y no tiene asignada un valor de retorno entonces falla la compilacion.
            if (!ambiente.hasReturn()) {
                errorSemantico("no_return", "La funcion " + ambiente.getNombre() + " no tiene establecido un valor de retorno.");
            }
            //cuando termina el block de function, se puede eliminar su tabla de simbolos
            mostrarPila(ambiente);
            //System.out.println("------------------ desapila ambiente " + ambiente.getNombre() + " -----------------");
            ambiente = ambiente.getPadre();
        } else {
            errorSintactico("la declaracion de un bloque de funcion (function)");
        }
    }

    private void function_heading(int functionLabel) {
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
            if (ambiente.getTipos().containsKey(nombre.toUpperCase())) {
                errorSemantico("unicidad", nombre);
            } else {
                ambiente.addFunction(nombre, type, functionLabel);
            }
            int parameterCount = 0;
            for (LinkedList<String> listaParametro : listaParametros) {
                for (String string : listaParametro) {
                    parameterCount++;
                }
                parameterCount--;
            }
            //se crea el ambiente para esa funcion
            Ambiente padre = ambiente;
            ambiente = new Ambiente("TK_FUNCTION", nombre, padre, functionLabel, parameterCount);
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
                    if (ambiente.getTipos().containsKey(id.toUpperCase()) || ambiente.getNombre().equalsIgnoreCase(id)) {
                        errorSemantico("unicidad", id);
                    } else {
                        ambiente.addVariable(id, type, ambiente.getProfundidad(), true);
                        //se le asigna al padre 
                        ambiente.getPadre().addParametro(nombre, type);
                    }
                }
                i++;
            }
        } else {
            errorSintactico("la declaracion de un bloque de funcion (function)");
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
            errorSintactico("un identificador");
        }
    }

    private void parameter_declaration_list_1(LinkedList<LinkedList<String>> listaParametros) {
        if (preanalisis.getNombre().equals("TK_ENDSTNC")) {
            match("TK_ENDSTNC");
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
            errorSintactico("el comienzo de un bloque (begin)");
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
            errorSintactico("el comienzo de un bloque (begin)");
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
                errorSintactico("una sentencia de control, repetitiva, asignacion o llamada a prodedimiento o funcion");
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
                errorSintactico("una sentencia de control, repetitiva, asignacion o llamada a prodedimiento o funcion");
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
                errorSintactico("una sentencia de asignacion o llamada a prodedimiento o funcion");
                break;
        }
    }

    private void simple_statement_1(String id) {
        switch (preanalisis.getNombre()) {
            case "TK_ASSIGN":
                String type = ambiente.getTipo(id);
                //verificar si el identificador es un identificador declarado en el ambiente,
                //o es un identificador que sirve como retorno dentro de una funcion.
                if (id.equalsIgnoreCase(ambiente.getNombre())) {
                    if (ambiente.getTipoAmbiente().equals("TK_PROCEDURE")) {
                        errorSemantico("id", "El identificador '" + id + "' no es valido");
                    } else if (ambiente.getTipoAmbiente().equals("TK_FUNCTION")) {
                        ambiente.setHasReturn(true);
                    }
                } else if (type != null && ambiente.getParametros(id) != null) {
                    //puede que sea un identificador declarado, pero que sea una funcion o procedimiento dentro del ambiente.
                    errorSemantico("id", "El identificador " + id + " no es valido para una asignacion");
                }
                //si no es ningun caso, todavia puede ser un identificador no declarado
                if (type == null) {
                    errorSemantico("id", "Identificador '" + id + "' no declarado.");
                }
                assignment_statement(id, type);
                break;
            default:
                if (ambiente.getParametros(id) != null) {
                    if (ambiente.getClase(id).equalsIgnoreCase("procedimiento")) {
                        call_procedure_or_function(id);
                    } else {
                        //aca si es una funcion deberia liberarse 1 de memoria, que es reservado en call_procedure_or_function, 
                        //ya que no se usa porque no es una asignacion, ni una llamada dentro de una expresion. 
                        //call_procedure_or_function(id);
                        //mepa += "LMEM 1\n";

                        //Esto pasa por la definicion de la gramatica, explicar en el informe que no es bueno tratar a las funciones
                        //y procedimientos de igual manera, ya que se usan en diferentes contextos.
                        errorSemantico("bad_uso_funcion", "Llamada a funcion sin utilizar su valor de retorno.");
                    }
                } else {
                    errorSemantico("no_subrutina", "El identificador '" + id + "' no corresponde a una subrutina declarada.");
                }

                break;
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
                errorSintactico("una sentencia de control o repetitiva");
                break;
        }
    }

    private void assignment_statement(String id, String type1) {
        if (preanalisis.getNombre().equals("TK_ASSIGN")) {
            match("TK_ASSIGN");
            String type2 = expression_or();
            if (!(type1.equalsIgnoreCase(type2))) {
                errorSemantico("type", "se esperaba un " + TK2lexema(type1)
                        + " pero se encontro un " + TK2lexema(type2)
                );
            }
            mepa += "ALVL " + ambiente.getProfundidad(id) + " " + ambiente.getOffset(id) + "\n";
            //mepa += "ALVL " + id.toUpperCase() + "\n";
        } else {
            errorSintactico("una sentencia de asignacion");
        }
    }

    private void call_procedure_or_function(String id) {
        if (preanalisis.getNombre().equals("TK_OPAR")) {
            match("TK_OPAR");
            LinkedList<String> list = new LinkedList<>();
            if (id.equalsIgnoreCase("TK_WRITE")) {
                call_procedure_or_function_1(list);
                if (list.size() != 1) {
                    errorSemantico("WRITE", "Write debe recibir un parametro");
                } else if (!list.get(0).equalsIgnoreCase("TK_TYPE_INT")) {
                    errorSemantico("WRITE", "Procedimiento Write solo acepta expresiones de tipo integer.");
                }
                mepa += "IMPR\n";
            } else if (id.equalsIgnoreCase("TK_READ")) {
                identifier_list(list);
                if (list.isEmpty()) {
                    errorSemantico("READ", "Read debe recibir al menos un parametro");
                } else {
                    for (String var : list) {
                        if (ambiente.getTipo(var) != null) {
                            if (!ambiente.getTipo(var).equalsIgnoreCase("TK_TYPE_INT")) {
                                errorSemantico("READ", "Procedimiento Read solo acepta "
                                        + "parametros de tipo integer. Identificador '" + var
                                        + "' no es integer");
                            } else if (ambiente.getParametros(var) != null) {
                                errorSemantico("READ", "Procedimiento Read no puede recibir la subrutina '" + var + "' por parametro.");
                            }
                        } else {
                            errorSemantico("id", "Identificador '" + var + "' no declarado");
                        }
                        mepa += "LEER\n";
                        mepa += "ALVL " + ambiente.getProfundidad(var) + " " + ambiente.getOffset(var) + "\n";
                        //mepa += "ALVL " + var.toUpperCase() + "\n";
                    }
                }
            } else {
                if (ambiente.getClase(id).equals("funcion")) {
                    mepa += "RMEM 1\n";
                }
                call_procedure_or_function_1(list);
                boolean res = ambiente.equals(id, list);
                if (!res) {
                    if (!ambiente.getParametros(id).isEmpty()) {
                        errorSemantico("call", "La lista de parametros no coincide con la "
                                + "definicion de la subrutina '" + id + "'. Se esperaban " + ambiente.getParametros(id).size() + " parametros: ("
                                + param2lexema(ambiente.getParametros(id)) + ").");
                    } else {
                        errorSemantico("call", "La lista de parametros no coincide con la "
                                + "definicion de la subrutina '" + id + "'. Se esperaban cero parametros.");
                    }
                }
                if (ambiente.getClase(id).equals("funcion") || ambiente.getClase(id).equals("procedimiento")) {
                    mepa += "LLPR L" + ambiente.getLabel(id) + "\n";
                }
            }
            match("TK_CPAR");
        } else {
            //si no tiene parentesis hay que verificar que el identificador no sea una funcion,
            //o que sea una funcion con cero parametros
            if (ambiente.getParametros(id) != null) {
                if (!ambiente.getParametros(id).isEmpty()) {
                    errorSemantico("call", "La lista de parametros no coincide con la "
                            + "definicion de la subrutina '" + id + "'. Se esperaban " + ambiente.getParametros(id).size() + " parametros: ("
                            + param2lexema(ambiente.getParametros(id)) + ").");
                } else {
                    if (ambiente.getClase(id).equals("funcion")) {
                        mepa += "RMEM 1\n";
                    }
                    mepa += "LLPR L" + ambiente.getLabel(id) + "\n";
                }
            } else {
                errorSemantico("no_subrutina", "El identificador '" + id + "' no corresponde a una subrutina declarada.");
            }
        }
        /*else {
            errorSintactico("una declaracion de parametros de procediento o funcion");
        }*/
    }

    private void call_procedure_or_function_1(LinkedList<String> types) {
        switch (preanalisis.getNombre()) {
            case "TK_ID":
            case "TK_OPAR":
            case "TK_ADD_OP_REST":
            case "TK_NOT_OP":
            case "TK_BOOLEAN_TRUE":
            case "TK_BOOLEAN_FALSE":
            case "TK_TYPE_BOOL":
            case "TK_NUMBER":
            case "TK_TYPE_INT":
                expression_list(types);
                break;
            default:
                errorSintactico("una expresion aritmetica o relacional");
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
            mepaLabel++;
            int thenLable = mepaLabel;
            mepa += "DSVF L" + thenLable + "\n";
            match("TK_THEN");
            statement_block();
            else_statement(thenLable);
        } else {
            errorSintactico("una sentencia de control");
        }
    }

    private void else_statement(int thenLabel) {
        if (preanalisis.getNombre().equals("TK_ELSE")) {
            mepaLabel++;
            int elseLabel = mepaLabel;
            mepa += "DSVS L" + elseLabel + "\n";
            mepa += "L" + thenLabel + " NADA\n";
            match("TK_ELSE");
            statement_block();
            mepa += "L" + elseLabel + " NADA\n";
        } else {
            mepa += "L" + thenLabel + " NADA\n";
        }
    }

    private void repetitive_statement() {
        if (preanalisis.getNombre().equals("TK_WHILE")) {
            match("TK_WHILE");
            mepaLabel++;
            int whileLabel = mepaLabel;
            mepa += "L" + whileLabel + " NADA\n";
            String type = expression_or();
            //creo que boolean ya no es necesario. revisar.
            if (!type.equalsIgnoreCase("TK_TYPE_BOOL") && !type.equalsIgnoreCase("boolean")) {
                errorSemantico("while", "Se espera una expresion booleana");
            }
            mepaLabel++;
            int doLabel = mepaLabel;
            mepa += "DSVF L" + doLabel + "\n";
            match("TK_DO");
            statement_block();
            mepa += "DSVS L" + whileLabel + "\n";
            mepa += "L" + doLabel + " NADA\n";
        } else {
            errorSintactico("una sentencia de repetitiva");
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
                errorSintactico("una expresion aritmetica o relacional");
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
                errorSintactico("una expresion aritmetica o relacional");
                break;
        }
        return type;
    }

    private String expression_or_1(String type) {
        if (preanalisis.getNombre().equals("TK_BOOL_OP_OR")) {
            match("TK_BOOL_OP_OR");
            String type2 = expression_and();
            if (!((type.equalsIgnoreCase(type2)) && type.equalsIgnoreCase("TK_TYPE_BOOL"))) {
                errorSemantico(type, type2, "TK_BOOL_OP_OR");
            }
            mepa += "DISJ\n";
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
                errorSintactico("una expresion aritmetica o relacional");
                break;
        }
        return type;
    }

    private String expression_and_1(String type) {
        if (preanalisis.getNombre().equals("TK_BOOL_OP_AND")) {
            match("TK_BOOL_OP_AND");
            String type2 = expression_rel();
            if (!((type.equalsIgnoreCase(type2)) && type.equalsIgnoreCase("TK_TYPE_BOOL"))) {
                errorSemantico(type, type2, "TK_BOOL_OP_AND");
            }
            mepa += "CONJ\n";
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
                errorSintactico("una expresion aritmetica o relacional");
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
                        //aca puede ser cualquier operando, todos contra todos.
                        type = "TK_TYPE_BOOL";
                        break;
                    case "TK_REL_OP_MIN":
                    case "TK_REL_OP_MAY":
                    case "TK_REL_OP_LEQ":
                    case "TK_REL_OP_GEQ":
                        if (!((type.equalsIgnoreCase(type2)) && (type.equalsIgnoreCase("TK_TYPE_INT")))) {
                            errorSemantico(type, type2, op);
                        }
                        type = "TK_TYPE_BOOL";
                        break;
                }
                switch (op) {
                    case "TK_REL_OP_EQ":
                        mepa += "CMIG\n";
                        break;
                    case "TK_REL_OP_NEQ":
                        mepa += "CMDG\n";
                        break;
                    case "TK_REL_OP_MIN":
                        mepa += "CMME\n";
                        break;
                    case "TK_REL_OP_MAY":
                        mepa += "CMMA\n";
                        break;
                    case "TK_REL_OP_LEQ":
                        mepa += "CMNI\n";
                        break;
                    case "TK_REL_OP_GEQ":
                        mepa += "CMYI\n";
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
                errorSintactico("una expresion aritmetica o relacional");
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
                    errorSemantico(type, type2, op);
                }
                switch (op) {
                    case "TK_ADD_OP_SUM":
                        mepa += "SUMA\n";
                        break;
                    case "TK_ADD_OP_REST":
                        mepa += "SUST\n";
                        break;
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
                errorSintactico("una expresion aritmetica o relacional");
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
                    errorSemantico(type, type2, op);
                }
                switch (op) {
                    case "TK_MULT_OP_POR":
                        mepa += "MULT\n";
                        break;
                    case "TK_MULT_OP_DIV":
                        mepa += "DIVI\n";
                        break;
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
                type = ambiente.getTipo(id);
                if (type == null) {
                    errorSemantico("id", "Identificador '" + id + "' no declarado");
                }
                if (ambiente.getClase(id).equals("variable") || ambiente.getClase(id).equals("parametro")) {
                    mepa += "APVL " + ambiente.getProfundidad(id) + " " + ambiente.getOffset(id) + "\n";
                    //mepa += "APVL " + id.toUpperCase() + "\n";
                } else {
                    if (ambiente.getClase(id).equalsIgnoreCase("funcion")) {
                        call_procedure_or_function(id);
                    } else {
                        errorSemantico("bad_uso_proc", "No se permite uso de procedimientos en expresiones. Causa: retorno void.");
                    }
                }
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
                switch (op) {
                    case "TK_ADD_OP_REST":
                        if (!type.equalsIgnoreCase("TK_TYPE_INT")) {
                            errorSemantico(type, null, op);
                        }
                        type = "TK_TYPE_INT";
                        mepa += "UMEN\n";
                        break;
                    case "TK_NOT_OP":
                        if (!type.equalsIgnoreCase("TK_TYPE_BOOL")) {
                            errorSemantico(type, null, op);
                        }
                        type = "TK_TYPE_BOOL";
                        mepa += "NEGA\n";
                        break;
                }
                break;
            case "TK_BOOLEAN_TRUE":
            case "TK_BOOLEAN_FALSE":
            case "TK_NUMBER":
                type = literal();
                break;
            default:
                errorSintactico("una expresion aritmetica o relacional");
                break;
        }
        return type;
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
                errorSintactico("un operador de negacion (- o not)");
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
                errorSintactico("un operador de adicion o substraccion");
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
                errorSintactico("un operador de multiplicacion o division");
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
                errorSintactico("un tipo de dato (boolean o integer)");
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
            errorSintactico("un identificador");
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
            errorSintactico("un identificador");
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
                errorSintactico("un literal booleano o numerico");
                break;
        }
        return type;
    }

    private void number() {
        if (preanalisis.getNombre().equals("TK_NUMBER")) {
            mepa += "APCT " + preanalisis.getValor() + "\n";
            match("TK_NUMBER");
        } else {
            errorSintactico("un literal numerico");
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
                mepa += "APCT 1\n";
                match("TK_BOOLEAN_TRUE");
                break;
            case "TK_BOOLEAN_FALSE":
                mepa += "APCT 0\n";
                match("TK_BOOLEAN_FALSE");
                break;
            default:
                errorSintactico("un literal booleano");
                break;
        }
    }

    /**
     * Lanza un RuntimeException("semantico", Causa).
     *
     * @param term
     */
    private void errorSemantico(String type1, String type2, String op) {
        op = TK2lexema(op);
        if (type1 != null) {
            type1 = TK2lexema(type1);
            if (type2 != null) {
                type2 = TK2lexema(type2);
                throw new RuntimeException("semantico", new Throwable("\nError semantico: linea " + lexico.getNroLinea()
                        + ".\n" + type1 + " y " + type2 + " no aplicables a operador " + op + "."));
            } else {
                throw new RuntimeException("semantico", new Throwable("\nError semantico: linea " + lexico.getNroLinea()
                        + ".\n" + type1 + " no aplicable a operador " + op + "."));
            }
        }
    }

    private String param2lexema(LinkedList<String> tkParametros) {
        String res = "";
        for (int i = 0; i < tkParametros.size(); i++) {
            res += TK2lexema(tkParametros.get(i)) + ", ";
        }
        if (res.length() > 2) {
            res = res.substring(0, res.length() - 2);
        }
        return res;
    }

    /**
     * Recibe un Token y devuelve el lexema correspondiente, para imprimir los
     * mensajes de error
     *
     * @param tk
     * @return
     */
    private String TK2lexema(String tk) {
        switch (tk) {
            case "TK_TYPE_INT":
                tk = "integer";
                break;
            case "TK_TYPE_BOOL":
                tk = "boolean";
                break;
            case "TK_ASSIGN":
                tk = "asignacion";
                break;
            case "TK_REL_OP_EQ":
                tk = "=";
                break;
            case "TK_REL_OP_NEQ":
                tk = "<>";
                break;
            case "TK_REL_OP_MIN":
                tk = "<";
                break;
            case "TK_REL_OP_MAY":
                tk = ">";
                break;
            case "TK_REL_OP_LEQ":
                tk = "<=";
                break;
            case "TK_REL_OP_GEQ":
                tk = ">=";
                break;
            case "TK_ADD_OP_SUM":
                tk = "+";
                break;
            case "TK_ADD_OP_REST":
                tk = "-";
                break;
            case "TK_MULT_OP_POR":
                tk = "*";
                break;
            case "TK_MULT_OP_DIV":
                tk = "/";
                break;
            case "TK_BOOL_OP_AND":
                tk = "AND";
                break;
            case "TK_BOOL_OP_OR":
                tk = "OR";
                break;
            case "TK_NOT_OP":
                tk = "NOT";
                break;
            case "TK_END":
                tk = "end";
        }
        return tk;
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
            throw new RuntimeException("semantico", new Throwable("\nError semantico: linea "
                    + lexico.getNroLinea() + ".\n" + msg));
        }
    }

    /**
     * Lanza un RuntimeException("sintactico", Causa).
     *
     * @param term
     */
    private void errorSintactico(String term) {
        Throwable msj = null;
        if (term.equals("")) {
            msj = new Throwable("\nError sintactico: linea " + lexico.getNroLinea()
                    + " posicion " + (lexico.getPos() + 1) + ".\nFin del archivo alcanzado. "
                    + "Programa incompleto.");
        } else {
            msj = new Throwable("\nError sintactico: linea " + lexico.getNroLinea()
                    + " posicion " + (lexico.getPos() + 1) + ".\nSimbolo '" + preanalisis.getValor()
                    + "' no esperado. Se esperaba " + TK2lexema(term) + ".");
        }
        throw new RuntimeException("sintactico", msj);
    }

    private void mostrarPila(Ambiente tabla) {
        if (tabla != null) {
            //System.out.println("Ambiente: " + tabla.getNombre() + ": \n" + tabla.toString() + "\n");
            mostrarPila(tabla.getPadre());
        }
    }
}
