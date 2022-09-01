package com.kport.langueg.error;

public enum Errors {

    PARSE_IF_CONDITION_EMPTY(           "Condition of if statement cannot be empty! Line: %d",
                                        "Add an expression between the parentheses"),
    PARSE_IF_CONDITION_TUPLE(           "Condition of if statement cannot be a tuple! Line: %d",
                                        "Remove all top level commas"),

    PARSE_WHILE_CONDITION_EMPTY(        "Condition of while loop cannot be empty! Line: %d",
                                        "Add an expression between the parentheses"),
    PARSE_WHILE_CONDITION_TUPLE(        "Condition of while loop cannot be a tuple! Line: %d",
                                        "Remove all top level commas"),

    PARSE_FOR_HEAD_EMPTY(               "Head of for loop cannot be empty! Line: %d",
                                        "Add three statements as initialization, loop condition and increment, all separated by semicolons"),
    PARSE_FOR_HEAD_MALFORMED(           "Head of for loop doesn't contain exactly 3 statements! Line: %d",
                                        "Make sure you have three statements for initialization, loop condition and increment, all separated by semicolons"),

    PARSE_FN_PARAMETER_MALFORMED(       "Parameter of function \"%2$s\" is malformed! Line: %1$d",
                                        "Every parameter must be a type followed by the parameter name. E.g. (int x, String str)"),
    PARSE_FN_PARAMETER_VAR(             "Parameter \"%3$s\" of function \"%2$s\" must explicitly declare its type! Line: %1$d",
                                        "Replace \"var\" by the type of this parameter"),
    PARSE_ANON_FN_PARAMETER_MALFORMED(  "Parameter of anonymous function is malformed! Line: %d",
                                        "Every parameter must be a type followed by the parameter name. E.g. (int x, String str)"),
    PARSE_ANON_FN_PARAMETER_VAR(        "Parameter \"%2$s\" of anonymous function must explicitly declare its type! Line: %1$d",
                                        "Replace \"var\" by the type of this parameter"),

    PARSE_FNTYPE_EXPECTED_LBRACK(       "Expected left bracket after \"Fn\"! Line: %d",
                                        "Function types must follow the pattern: Fn[(param1, param2, ...) -> return]"),
    PARSE_FNTYPE_EXPECTED_ARROW(        "Expected -> after parameter list of function type! Line: %d",
                                        "Function types must follow the pattern: Fn[(param1, param2, ...) -> return]"),
    PARSE_FNTYPE_EXPECTED_RBRACK(       "Expected right bracket after return type of function type! Line: %d",
                                        "Function types must follow the pattern: Fn[(param1, param2, ...) -> return]"),

    PARSE_BLOCK_EXPECTED_SEMICOLON(     "Expected semicolon after statement! Line: %d",
                                        ""),
    PARSE_BLOCK_NOT_CLOSED(             "Block opened on line %1$d isn't ever closed!",
                                        "Add a closing curly bracket or remove the opening one"),

    PARSE_VAR_EXPECTED_IDENTIFIER(      "Expected a name for the variable being declared! Line: %d",
                                        "Variable declarations must follow the pattern: var/type name"),

    PARSE_NUM_INVALID_HEX(              "\"%2$s\" is not a valid hexadecimal! Line: %1$d",
                                        "Hexadecimal numbers may only be integers and include the symbols 0123456789abcdefABCDEF"),
    PARSE_NUM_INVALID(                  "\"%s$s\" is neither a valid integer nor float! Line: %1$d",
                                        ""),

    PARSE_ATOM_REACHED_EOF(             "Reached EOF whilst parsing atom!",
                                        ""),
    PARSE_ATOM_UNEXPECTED_TOKEN(        "Unexpected token \"%2$s\"! Line: %1$d",
                                        ""),

    PARSE_DELIM_EXPECTED_START(         "Expected token \"%2$s\"! Line: %1$d",
                                        ""),
    PARSE_DELIM_EXPECTED_SEPARATOR(     "Expected token \"%2$s\"! Line: %1$d",
                                        ""),

    PARSE_TYPE_INVALID(                 "\"%2$s\" is not a valid type! Line: %1$d",
                                        "");

    public String format;
    public String suggestion;

    Errors(String format_, String suggestion_){
        format = format_;
        suggestion = suggestion_;
    }
}
