package compiladorpascal.sintactico;

import compiladorpascal.lexico.*;

/**
 *
 * @author Martin Bermudez y Giuliano Marinelli
 */
public class AnalizadorSintactico {

    protected Token preanalisis;
    protected AnalizadorLexico lexico;

    public AnalizadorSintactico(AnalizadorLexico lexico) {
        this.lexico = lexico;
        preanalisis = lexico.tokenSiguiente();
    }

    /**
     * Verifica si el String terminal unifica con el simbolo de preanalisis
     * devuelto por el analizador lexico. Si no unifican lanza error sintactico.
     *
     * @param terminal
     */
    protected void match(String terminal) {
        //System.out.print("\033[32m");
        //System.out.print("<" + terminal + ">");
        //System.out.print("\033[30m");
        if (preanalisis.getNombre().equals(terminal)) {
            //System.out.print("<" + preanalisis.getNombre() + ">");
            preanalisis = lexico.tokenSiguiente();
            if (preanalisis == null && !terminal.equals("TK_POINT")) {
                error();
            }
        } else {
            error(terminal);
        }
    }

    public void program() {
        if (preanalisis.getNombre().equals("TK_PROGRAM")) {
            program_heading();
            block();
            match("TK_POINT");
        } else {
            error("TK_PROGRAM");
        }
    }

    protected void program_heading() {
        if (preanalisis.getNombre().equals("TK_PROGRAM")) {
            match("TK_PROGRAM");
            identifier();
            match("TK_ENDSTNC");
        } else {
            error("TK_PROGRAM");
        }
    }

    protected void block() {
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
                error("TK_VAR o TK_PROCEDURE o TK_FUNCTION o TK_BEGIN");
                break;
        }
    }

    protected void declaration_block() {
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
                error("TK_VAR o TK_PROCEDURE o TK_FUNCTION");
                break;
        }
    }

    protected void declaration_block_1() {
        switch (preanalisis.getNombre()) {
            case "TK_PROCEDURE":
            case "TK_FUNCTION":
                procedure_and_function_declaration_list();
                break;
        }
    }

    protected void variable_declaration_block() {
        if (preanalisis.getNombre().equals("TK_VAR")) {
            match("TK_VAR");
            variable_declaration_list();
        } else {
            error("TK_VAR");
        }
    }

    protected void variable_declaration_list() {
        if (preanalisis.getNombre().equals("TK_ID")) {
            variable_declaration();
            match("TK_ENDSTNC");
            variable_declaration_list_1();
        } else {
            error("TK_ID");
        }
    }

    protected void variable_declaration_list_1() {
        if (preanalisis.getNombre().equals("TK_ID")) {
            variable_declaration_list();
        }
    }

    protected void variable_declaration() {
        if (preanalisis.getNombre().equals("TK_ID")) {
            identifier_list();
            match("TK_TPOINTS");
            type();
        } else {
            error("TK_ID");
        }
    }

    protected void procedure_and_function_declaration_list() {
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
                error("TK_PROCEDURE o TK_FUNCTION");
                break;
        }
    }

    protected void procedure_and_function_declaration_list_1() {
        switch (preanalisis.getNombre()) {
            case "TK_PROCEDURE":
            case "TK_FUNCTION":
                procedure_and_function_declaration_list();
                break;
        }
    }

    protected void procedure_declaration() {
        if (preanalisis.getNombre().equals("TK_PROCEDURE")) {
            procedure_heading();
            match("TK_ENDSTNC");
            block();
        } else {
            error("TK_PROCEDURE");
        }
    }

    protected void procedure_heading() {
        if (preanalisis.getNombre().equals("TK_PROCEDURE")) {
            match("TK_PROCEDURE");
            identifier();
            parameters();
        } else {
            error("TK_PROCEDURE");
        }
    }

    protected void function_declaration() {
        if (preanalisis.getNombre().equals("TK_FUNCTION")) {
            function_heading();
            match("TK_ENDSTNC");
            block();
        } else {
            error("TK_FUNCTION");
        }
    }

    protected void function_heading() {
        if (preanalisis.getNombre().equals("TK_FUNCTION")) {
            match("TK_FUNCTION");
            identifier();
            parameters();
            match("TK_TPOINTS");
            type();
        } else {
            error("TK_FUNCTION");
        }
    }

    protected void parameters() {
        if (preanalisis.getNombre().equals("TK_OPAR")) {
            match("TK_OPAR");
            parameters_2();
            match("TK_CPAR");
        }
    }

    protected void parameters_2() {
        if (preanalisis.getNombre().equals("TK_ID")) {
            parameter_declaration_list();
        }
    }

    protected void parameter_declaration_list() {
        if (preanalisis.getNombre().equals("TK_ID")) {
            parameter_declaration();
            parameter_declaration_list_1();
        } else {
            error("TK_ID");
        }
    }

    protected void parameter_declaration_list_1() {
        if (preanalisis.getNombre().equals("TK_COMMA")) {
            match("TK_COMMA");
            parameter_declaration_list();
        }
    }

    protected void parameter_declaration() {
        if (preanalisis.getNombre().equals("TK_ID")) {
            identifier_list();
            match("TK_TPOINTS");
            type();
        } else {
            error("TK_ID");
        }
    }

    protected void statement_block() {
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

    protected void multiple_statement() {
        if (preanalisis.getNombre().equals("TK_BEGIN")) {
            match("TK_BEGIN");
            statement_list();
            match("TK_END");
        } else {
            error("TK_BEGIN");
        }
    }

    protected void statement_list() {
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
                error("TK_ID o TK_WRITE o TK_READ o TK_IF o TK_WHILE");
                break;
        }
    }

    protected void statement_list_1() {
        if (preanalisis.getNombre().equals("TK_ENDSTNC")) {
            match("TK_ENDSTNC");
            statement_list();
        }
    }

    protected void statement() {
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
                error("TK_ID o TK_WRITE o TK_READ o TK_IF o TK_WHILE");
                break;
        }
    }

    protected void simple_statement() {
        switch (preanalisis.getNombre()) {
            case "TK_ID":
                identifier();
                simple_statement_1();
                break;
            case "TK_WRITE":
                match("TK_WRITE");
                call_procedure_or_function();
                break;
            case "TK_READ":
                match("TK_READ");
                call_procedure_or_function();
                break;
            default:
                error("TK_ID o TK_WRITE o TK_READ");
                break;
        }
    }

    protected void simple_statement_1() {
        switch (preanalisis.getNombre()) {
            case "TK_ASSIGN":
                assignment_statement();
                break;
            case "TK_OPAR":
                call_procedure_or_function();
                break;
        }
    }

    protected void structured_statement() {
        switch (preanalisis.getNombre()) {
            case "TK_IF":
                conditional_statement();
                break;
            case "TK_WHILE":
                repetitive_statement();
                break;
            default:
                error("TK_IF o TK_WHILE");
                break;
        }
    }

    protected void assignment_statement() {
        if (preanalisis.getNombre().equals("TK_ASSIGN")) {
            match("TK_ASSIGN");
            expression_or();
        } else {
            error("TK_ASSIGN");
        }
    }

    protected void call_procedure_or_function() {
        if (preanalisis.getNombre().equals("TK_OPAR")) {
            match("TK_OPAR");
            call_procedure_or_function_1();
            match("TK_CPAR");
        } else {
            error("TK_OPAR");
        }
    }

    protected void call_procedure_or_function_1() {
        switch (preanalisis.getNombre()) {
            case "TK_ID":
            case "TK_OPAR":
            case "TK_ADD_OP_REST":
            case "TK_NOT_OP":
            case "TK_BOOLEAN_TRUE":
            case "TK_BOOLEAN_FALSE":
            case "TK_NUMBER":
                expression_list();
                break;
        }
    }

    protected void conditional_statement() {
        if (preanalisis.getNombre().equals("TK_IF")) {
            match("TK_IF");
            expression_or();
            match("TK_THEN");
            statement_block();
            else_statement();
        } else {
            error("TK_IF");
        }
    }

    protected void else_statement() {
        if (preanalisis.getNombre().equals("TK_ELSE")) {
            match("TK_ELSE");
            statement_block();
        }
    }

    protected void repetitive_statement() {
        if (preanalisis.getNombre().equals("TK_WHILE")) {
            match("TK_WHILE");
            expression_or();
            match("TK_DO");
            statement_block();
        } else {
            error("TK_WHILE");
        }
    }

    protected void expression_list() {
        switch (preanalisis.getNombre()) {
            case "TK_ID":
            case "TK_OPAR":
            case "TK_ADD_OP_REST":
            case "TK_NOT_OP":
            case "TK_BOOLEAN_TRUE":
            case "TK_BOOLEAN_FALSE":
            case "TK_NUMBER":
                expression_or();
                expression_list_1();
                break;
            default:
                error("TK_ID o TK_OPAR o TK_ADD_OP_REST o TK_NOT_OP o TK_BOOLEAN_TRUE o TK_BOOLEAN_FALSE o TK_NUMBER");
                break;
        }
    }

    protected void expression_list_1() {
        if (preanalisis.getNombre().equals("TK_COMMA")) {
            match("TK_COMMA");
            expression_list();
        }
    }

    protected void expression_or() {
        switch (preanalisis.getNombre()) {
            case "TK_ID":
            case "TK_OPAR":
            case "TK_ADD_OP_REST":
            case "TK_NOT_OP":
            case "TK_BOOLEAN_TRUE":
            case "TK_BOOLEAN_FALSE":
            case "TK_NUMBER":
                expression_and();
                expression_or_1();
                break;
            default:
                /*como este es el procedimiento más general desde el cual se producen
                las expresiones, se puede mandar un mensaje diciendo que es lo que
                se esperaba.*/
                error("una expresión");
                break;
        }
    }

    protected void expression_or_1() {
        if (preanalisis.getNombre().equals("TK_OR")) {
            match("TK_OR");
            expression_and();
            expression_or_1();
        }
    }

    protected void expression_and() {
        switch (preanalisis.getNombre()) {
            case "TK_ID":
            case "TK_OPAR":
            case "TK_ADD_OP_REST":
            case "TK_NOT_OP":
            case "TK_BOOLEAN_TRUE":
            case "TK_BOOLEAN_FALSE":
            case "TK_NUMBER":
                expression_rel();
                expression_and_1();
                break;
            default:
                error("una expresión");
                break;
        }
    }

    protected void expression_and_1() {
        if (preanalisis.getNombre().equals("TK_AND")) {
            match("TK_AND");
            expression_rel();
            expression_and_1();
        }
    }

    protected void expression_rel() {
        switch (preanalisis.getNombre()) {
            case "TK_ID":
            case "TK_OPAR":
            case "TK_ADD_OP_REST":
            case "TK_NOT_OP":
            case "TK_BOOLEAN_TRUE":
            case "TK_BOOLEAN_FALSE":
            case "TK_NUMBER":
                expression_add();
                expression_rel_1();
                break;
            default:
                error("una expresión");
                break;
        }
    }

    protected void expression_rel_1() {
        switch (preanalisis.getNombre()) {
            case "TK_REL_OP_EQ":
            case "TK_REL_OP_NEQ":
            case "TK_REL_OP_MIN":
            case "TK_REL_OP_MAY":
            case "TK_REL_OP_LEQ":
            case "TK_REL_OP_GEQ":
                relational_operator();
                expression_add();
                expression_rel_1();
                break;
        }
    }

    protected void expression_add() {
        switch (preanalisis.getNombre()) {
            case "TK_ID":
            case "TK_OPAR":
            case "TK_ADD_OP_REST":
            case "TK_NOT_OP":
            case "TK_BOOLEAN_TRUE":
            case "TK_BOOLEAN_FALSE":
            case "TK_NUMBER":
                expression_mult();
                expression_add_1();
                break;
            default:
                error("una expresión");
                break;
        }
    }

    protected void expression_add_1() {
        switch (preanalisis.getNombre()) {
            case "TK_ADD_OP_SUM":
            case "TK_ADD_OP_REST":
                addition_operator();
                expression_mult();
                expression_add_1();
                break;
        }
    }

    protected void expression_mult() {
        switch (preanalisis.getNombre()) {
            case "TK_ID":
            case "TK_OPAR":
            case "TK_ADD_OP_REST":
            case "TK_NOT_OP":
            case "TK_BOOLEAN_TRUE":
            case "TK_BOOLEAN_FALSE":
            case "TK_NUMBER":
                factor();
                expression_mult_1();
                break;
            default:
                error("una expresión");
                break;
        }
    }

    protected void expression_mult_1() {
        switch (preanalisis.getNombre()) {
            case "TK_MULT_OP_POR":
            case "TK_MULT_OP_DIV":
                multiplication_operator();
                factor();
                expression_mult_1();
                break;
        }
    }

    protected void factor() {
        switch (preanalisis.getNombre()) {
            case "TK_ID":
                identifier();
                factor_1();
                break;
            case "TK_OPAR":
                match("TK_OPAR");
                expression_or();
                match("TK_CPAR");
                break;
            case "TK_ADD_OP_REST":
            case "TK_NOT_OP":
                unary_operator();
                factor();
                break;
            case "TK_BOOLEAN_TRUE":
            case "TK_BOOLEAN_FALSE":
            case "TK_NUMBER":
                literal();
                break;
            default:
                error("un factor");
                break;
        }
    }

    protected void factor_1() {
        if (preanalisis.getNombre().equals("TK_OPAR")) {
            call_procedure_or_function();
        }
    }

    protected void relational_operator() {
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
                error("un operador relacional");
                break;
        }
    }

    protected void unary_operator() {
        switch (preanalisis.getNombre()) {
            case "TK_ADD_OP_REST":
                match("TK_ADD_OP_REST");
                break;
            case "TK_NOT_OP":
                match("TK_NOT_OP");
                break;
            default:
                error("TK_ADD_OP_REST o TK_NOT_OP");
                break;
        }
    }

    protected void addition_operator() {
        switch (preanalisis.getNombre()) {
            case "TK_ADD_OP_SUM":
                match("TK_ADD_OP_SUM");
                break;
            case "TK_ADD_OP_REST":
                match("TK_ADD_OP_REST");
                break;
            default:
                error("TK_ADD_OP_SUM o TK_ADD_OP_REST");
                break;
        }
    }

    protected void multiplication_operator() {
        switch (preanalisis.getNombre()) {
            case "TK_MULT_OP_POR":
                match("TK_MULT_OP_POR");
                break;
            case "TK_MULT_OP_DIV":
                match("TK_MULT_OP_DIV");
                break;
            default:
                error("TK_MULT_OP_POR o TK_MULT_OP_DIV");
                break;
        }
    }

    protected void type() {
        switch (preanalisis.getNombre()) {
            case "TK_TYPE_INT":
                match("TK_TYPE_INT");
                break;
            case "TK_TYPE_BOOL":
                match("TK_TYPE_BOOL");
                break;
            default:
                error("un tipo de dato");
                break;
        }
    }

    protected void identifier_list() {
        if (preanalisis.getNombre().equals("TK_ID")) {
            identifier();
            identifier_list_1();
        } else {
            error("TK_ID");
        }
    }

    protected void identifier_list_1() {
        if (preanalisis.getNombre().equals("TK_COMMA")) {
            match("TK_COMMA");
            identifier_list();
        }
    }

    protected void identifier() {
        if (preanalisis.getNombre().equals("TK_ID")) {
            match("TK_ID");
        } else {
            error("TK_ID");
        }
    }

    //protected void identifier_1() {}
    protected void literal() {
        switch (preanalisis.getNombre()) {
            case "TK_BOOLEAN_TRUE":
            case "TK_BOOLEAN_FALSE":
                bool();
                break;
            case "TK_NUMBER":
                number();
                break;
            default:
                error("TK_BOOLEAN_TRUE o TK_BOOLEAN_FALSE");
                break;
        }
    }

    protected void number() {
        if (preanalisis.getNombre().equals("TK_NUMBER")) {
            match("TK_NUMBER");
        } else {
            error("TK_NUMBER");
        }
    }

    //protected void number_1() {}
    //protected void word() {}
    //protected void word_1() {}
    //protected void letter() {}
    //protected void digit() {}
    protected void bool() {
        switch (preanalisis.getNombre()) {
            case "TK_BOOLEAN_TRUE":
                match("TK_BOOLEAN_TRUE");
                break;
            case "TK_BOOLEAN_FALSE":
                match("TK_BOOLEAN_FALSE");
                break;
            default:
                error("TK_BOOLEAN_TRUE o TK_BOOLEAN_FALSE");
                break;
        }
    }

    /**
     * Lanza un RuntimeException("sintactico", Causa).
     */
    protected void error() {
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

    /**
     * Lanza un RuntimeException("sintactico", Causa). Recibe un string que es
     * el símbolo que esperaba encontrar, para lanzar un error mas especifico.
     * Se podria generalizar recibiendo el conjunto primeros para lanzar un
     * error mas significativo.
     */
    protected void error(String terminal) {
        throw new RuntimeException("sintactico", new Throwable("\nError sintactico: linea " + lexico.getNroLinea()
                + " posicion " + (lexico.getPos() + 1) + ".\nSimbolo de preanalisis " + preanalisis.getNombre()
                + " no esperado. Se esperaba " + terminal));
    }

}
