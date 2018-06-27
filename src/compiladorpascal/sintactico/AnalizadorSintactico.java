package compiladorpascal.sintactico;

import compiladorpascal.lexico.*;

/**
 *
 * @author Martin Bermudez y Giuliano Marinelli
 */
public class AnalizadorSintactico {

    private Token preanalisis;
    private AnalizadorLexico lexico;

    public AnalizadorSintactico(AnalizadorLexico lexico) {
        this.lexico = lexico;
    }

    /**
     * Comienza el analisis sintactico desde el inicio del programa. Se encarga
     * de capturar los posibles errores lexicos y sintacticos.
     */
    public void analisisSintactico() {
        try {
            preanalisis = lexico.tokenSiguiente();
            program();
        } catch (RuntimeException ex) {
            //los errores lexicos y sintacticos son capturados aca.
            System.out.println(ex.getCause().getMessage());
        }
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
            System.out.print("<" + preanalisis.getNombre() + ">");
            preanalisis = lexico.tokenSiguiente();
        } else {
            error();
        }
    }

    private void program() {
        program_heading();
        block();
        match("TK_POINT");
    }

    private void program_heading() {
        match("TK_PROGRAM");
        identifier();
        match("TK_ENDSTNC");
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
                error();
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
                error();
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
        match("TK_VAR");
        variable_declaration_list();
    }

    private void variable_declaration_list() {
        variable_declaration();
        match("TK_ENDSTNC");
        variable_declaration_list_1();
    }

    private void variable_declaration_list_1() {
        if (preanalisis.getNombre().equals("TK_ID")) {
            variable_declaration_list();
        }
    }

    private void variable_declaration() {
        identifier_list();
        match("TK_TPOINTS");
        type();
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
                error();
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
        procedure_heading();
        match("TK_ENDSTNC");
        block();
    }

    private void procedure_heading() {
        match("TK_PROCEDURE");
        identifier();
        parameters();
    }

    private void function_declaration() {
        function_heading();
        match("TK_ENDSTNC");
        block();
    }

    private void function_heading() {
        match("TK_FUNCTION");
        identifier();
        parameters();
        match("TK_TPOINTS");
        type();
    }

    private void parameters() {
        if (preanalisis.getNombre().equals("TK_OPAR")) {
            match("TK_OPAR");
            parameters_2();
            match("TK_CPAR");
        }
    }

    private void parameters_2() {
        if (preanalisis.getNombre().equals("TK_ID")) {
            parameter_declaration_list();
        }
    }

    private void parameter_declaration_list() {
        parameter_declaration();
        parameter_declaration_list_1();
    }

    private void parameter_declaration_list_1() {
        if (preanalisis.getNombre().equals("TK_COMMA")) {
            match("TK_COMMA");
            parameter_declaration_list();
        }
    }

    private void parameter_declaration() {
        identifier_list();
        match("TK_TPOINTS");
        type();
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
            default: error();
        }
    }

    private void multiple_statement() {
        match("TK_BEGIN");
        statement_list();
        match("TK_END");
    }

    private void statement_list() {
        statement();
        statement_list_1();
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
                error();
                break;
        }
    }

    private void simple_statement() {
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
                error();
                break;
        }
    }

    private void simple_statement_1() {
        switch (preanalisis.getNombre()) {
            case "TK_ASSIGN":
                assignment_statement();
                break;
            case "TK_OPAR":
                call_procedure_or_function();
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
                error();
                break;
        }
    }

    private void assignment_statement() {
        match("TK_ASSIGN");
        expression_or();
    }

    private void call_procedure_or_function() {
        match("TK_OPAR");
        call_procedure_or_function_1();
        match("TK_CPAR");
    }

    private void call_procedure_or_function_1() {
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

    private void conditional_statement() {
        match("TK_IF");
        expression_or();
        match("TK_THEN");
        statement_block();
        else_statement();
    }

    private void else_statement() {
        if (preanalisis.getNombre().equals("TK_ELSE")) {
            match("TK_ELSE");
            statement_block();
        }
    }

    private void repetitive_statement() {
        match("TK_WHILE");
        expression_or();
        match("TK_DO");
        statement_block();
    }

    private void expression_list() {
        expression_or();
        expression_list_1();
    }

    private void expression_list_1() {
        if (preanalisis.getNombre().equals("TK_COMMA")) {
            match("TK_COMMA");
            expression_list();
        }
    }

    private void expression_or() {
        expression_and();
        expression_or_1();
    }

    private void expression_or_1() {
        if (preanalisis.getNombre().equals("TK_OR")) {
            match("TK_OR");
            expression_and();
            expression_or_1();
        }
    }

    private void expression_and() {
        expression_rel();
        expression_and_1();
    }

    private void expression_and_1() {
        if (preanalisis.getNombre().equals("TK_AND")) {
            match("TK_AND");
            expression_rel();
            expression_and_1();
        }
    }

    private void expression_rel() {
        expression_add();
        expression_rel_1();
    }

    private void expression_rel_1() {
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

    private void expression_add() {
        expression_mult();
        expression_add_1();
    }

    private void expression_add_1() {
        switch (preanalisis.getNombre()) {
            case "TK_ADD_OP_SUM":
            case "TK_ADD_OP_REST":
                addition_operator();
                expression_mult();
                expression_add_1();
                break;
        }
    }

    private void expression_mult() {
        factor();
        expression_mult_1();
    }

    private void expression_mult_1() {
        switch (preanalisis.getNombre()) {
            case "TK_MULT_OP_POR":
            case "TK_MULT_OP_DIV":
                multiplication_operator();
                factor();
                expression_mult_1();
                break;
        }
    }

    private void factor() {
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
                error();
                break;
        }
    }

    private void factor_1() {
        if (preanalisis.getNombre().equals("TK_OPAR")) {
            call_procedure_or_function();
        }
    }

    private void relational_operator() {
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
                error();
                break;
        }
    }

    private void unary_operator() {
        switch (preanalisis.getNombre()) {
            case "TK_ADD_OP_REST":
                match("TK_ADD_OP_REST");
                break;
            case "TK_NOT_OP":
                match("TK_NOT_OP");
                break;
            default:
                error();
                break;
        }
    }

    private void addition_operator() {
        switch (preanalisis.getNombre()) {
            case "TK_ADD_OP_SUM":
                match("TK_ADD_OP_SUM");
                break;
            case "TK_ADD_OP_REST":
                match("TK_ADD_OP_REST");
                break;
            default:
                error();
                break;
        }
    }

    private void multiplication_operator() {
        switch (preanalisis.getNombre()) {
            case "TK_MULT_OP_POR":
                match("TK_MULT_OP_POR");
                break;
            case "TK_MULT_OP_DIV":
                match("TK_MULT_OP_DIV");
                break;
            default:
                error();
                break;
        }
    }

    private void type() {
        switch (preanalisis.getNombre()) {
            case "TK_TYPE_INT":
                match("TK_TYPE_INT");
                break;
            case "TK_TYPE_BOOL":
                match("TK_TYPE_BOOL");
                break;
            default:
                error();
                break;
        }
    }

    private void identifier_list() {
        identifier();
        identifier_list_1();
    }

    private void identifier_list_1() {
        if (preanalisis.getNombre().equals("TK_COMMA")) {
            match("TK_COMMA");
            identifier_list();
        }
    }

    private void identifier() {
        switch (preanalisis.getNombre()) {
            case "TK_ID":
                match("TK_ID");
                break;
            default:
                error();
                break;
        }
    }

    //private void identifier_1() {}
    private void literal() {
        switch (preanalisis.getNombre()) {
            case "TK_BOOLEAN_TRUE":
            case "TK_BOOLEAN_FALSE":
                bool();
                break;
            case "TK_NUMBER":
                number();
                break;
            default:
                error();
                break;
        }
    }

    private void number() {
        switch (preanalisis.getNombre()) {
            case "TK_NUMBER":
                match("TK_NUMBER");
                break;
            default:
                error();
                break;
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
                error();
                break;
        }
    }

    /**
     * Lanza un RuntimeException("sintactico", Causa).
     */
    private void error() {
        throw new RuntimeException("sintactico", new Throwable("\nError sintactico: linea " + lexico.getNroLinea()
                + " posicion " + (lexico.getPos() + 1) + ".\nSimbolo de preanalisis " + preanalisis.getNombre()
                + " no esperado."));
    }

}
