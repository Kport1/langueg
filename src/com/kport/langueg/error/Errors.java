package com.kport.langueg.error;

public enum Errors {

    PARSE_BIN_OP_LEFT_NOT_EXPR(                 "Left side of operator \"%2$s\" is a statement! Line %1$d",
                                                "Replace this with a valid expression"),
    PARSE_BIN_OP_RIGHT_NOT_EXPR(                "Right side of operator \"%2$s\" is a statement! Line %1$d",
                                                "Replace this with a valid expression"),

    PARSE_CALL_CALLE_NOT_EXPR(                  "Called value is a statement! Line %1$d",
                                                "Replace this with a valid expression"),

    PARSE_IF_CONDITION_EMPTY(                   "Condition of if statement cannot be empty! Line: %d",
                                                "Add an expression between the parentheses"),
    PARSE_IF_CONDITION_TUPLE(                   "Condition of if statement cannot be a tuple! Line: %d",
                                                "Remove all top level commas"),

    PARSE_WHILE_CONDITION_EMPTY(                "Condition of while loop cannot be empty! Line: %d",
                                                "Add an expression between the parentheses"),
    PARSE_WHILE_CONDITION_TUPLE(                "Condition of while loop cannot be a tuple! Line: %d",
                                                "Remove all top level commas"),

    PARSE_FOR_HEAD_EMPTY(                       "Head of for loop cannot be empty! Line: %d",
                                                "Add three statements as initialization, loop condition and increment, all separated by semicolons"),
    PARSE_FOR_HEAD_MALFORMED(                   "Head of for loop doesn't contain exactly 3 statements! Line: %d",
                                                "Make sure you have three statements for initialization, loop condition and increment, all separated by semicolons"),

    PARSE_FN_RETURN_INVALID_TYPE(               "Return type of function is invalid! Line: %1$d\n%%C%1$d.%2$d.",
                                                "Replace this with a valid type identifier"),
    PARSE_FN_PARAMETER_MALFORMED(               "Parameter of function \"%2$s\" is malformed! Line: %1$d",
                                                "Every parameter must be a type followed by the parameter name. E.g. (int x, String str)"),
    PARSE_FN_PARAMETER_VAR(                     "Parameter \"%3$s\" of function \"%2$s\" must explicitly declare its type! Line: %1$d",
                                                "Replace \"var\" with the type of this parameter"),
    PARSE_ANON_FN_PARAMETER_MALFORMED(          "Parameter of anonymous function is malformed! Line: %d",
                                                "Every parameter must be a type followed by the parameter name. E.g. (int x, String str)"),
    PARSE_ANON_FN_PARAMETER_VAR(                "Parameter \"%2$s\" of anonymous function must explicitly declare its type! Line: %1$d",
                                                "Replace \"var\" with the type of this parameter"),

    PARSE_FNTYPE_PARAM_INVALID_TYPE(            "Parameter of function type is invalid! Line: %1$d\n%%C%1$d.%2$d.",
                                                "Replace this with a valid type identifier"),
    PARSE_FNTYPE_RETURN_INVALID_TYPE(           "Return of function type is invalid! Line: %1$d\n%%C%1$d.%2$d.",
                                                "Replace this with a valid type identifier"),
    PARSE_FNTYPE_EXPECTED_LBRACK(               "Expected left bracket after \"Fn\"! Line: %d",
                                                "Function types must follow the pattern: Fn[(param1, param2, ...) -> return]"),
    PARSE_FNTYPE_EXPECTED_ARROW(                "Expected -> after parameter list of function type! Line: %d",
                                                "Function types must follow the pattern: Fn[(param1, param2, ...) -> return]"),
    PARSE_FNTYPE_EXPECTED_RBRACK(               "Expected right bracket after return type of function type! Line: %d",
                                                "Function types must follow the pattern: Fn[(param1, param2, ...) -> return]"),

    PARSE_TUPLETYPE_INVALID_TYPE(               "Element of tuple type is invalid! Line: %1$d\n%%C%1$d.%2$d.",
                                                "Replace this with a valid type identifier"),

    PARSE_CAST_INVALID_TYPE(                    "Type of cast is invalid! Line: %1$d\n%%C%1$d.%2$d.",
                                                "Replace this with a valid type identifier"),

    PARSE_BLOCK_EXPECTED_SEMICOLON(             "Expected semicolon after statement! Line: %d",
                                                ""),
    PARSE_BLOCK_NOT_CLOSED(                     "Block opened on line %1$d isn't ever closed!",
                                                "Add a closing curly bracket or remove the opening one"),

    PARSE_VAR_EXPECTED_IDENTIFIER(              "Expected a name for the variable being declared! Line: %d",
                                                "Variable declarations must follow the pattern: var/type name"),
    PARSE_VAR_INIT_NOT_EXPR(                    "Variable %2$s is initialized with a statement! Line: %1$d",
                                                "Replace this with a valid expression"),
    PARSE_VAR_DESTRUCT_EXPECTED_IDENTIFIER(     "Expected identifier in destructuring assignment! Line: %d",
                                                "Variable declaration with destructuring assignment must follow the pattern: var (name1, name2, ...) = ..."),
    PARSE_VAR_DESTRUCT_CANNOT_INFER_TYPE(       "Cannot infer type of variables, without assignment after declaration! Line: %d",
                                                "Variable declaration with destructuring assignment must follow the pattern: var (name1, name2, ...) = ..."),

    PARSE_NUM_INVALID_HEX(                      "\"%2$s\" is not a valid hexadecimal! Line: %1$d",
                                                "Hexadecimal numbers may only be integers and include the symbols 0123456789abcdefABCDEF"),
    PARSE_NUM_INVALID(                          "\"%s$s\" is neither a valid integer nor float! Line: %1$d",
                                                ""),

    PARSE_RETURN_NOT_EXPR(                      "Return value is a statement! Line: %1$d",
                                                "Replace this with a valid expression"),

    PARSE_ATOM_REACHED_EOF(                     "Reached EOF whilst parsing atom!",
                                                ""),
    PARSE_ATOM_UNEXPECTED_TOKEN(                "Unexpected token \"%2$s\"! Line: %1$d",
                                                ""),

    PARSE_DELIM_EXPECTED_START(                 "Expected token \"%2$s\"! Line: %1$d",
                                                ""),
    PARSE_DELIM_EXPECTED_SEPARATOR(             "Expected token \"%2$s\"! Line: %1$d",
                                                ""),

    PARSE_TYPE_INVALID(                         "\"%2$s\" is not a valid type! Line: %1$d",
                                                ""),



    CHECK_FN_DOESNT_RETURN_ON_ALL_PATHS(        "Function \"%2$s\" doesn't return a value on all paths! Line: %1$d",
                                                ""),
    CHECK_FN_DOESNT_RETURN_ON_ALL_PATHS_ANON(   "Anonymous function doesn't return a value on all paths! Line: %d",
                                                ""),
    CHECK_FN_RETURN_TYPE_MISMATCH(              "Function \"%2$s\" should return \"%3$s\", but returns \"%4$s\" instead! Line: %1$d",
                                                ""),
    CHECK_FN_RETURN_TYPE_MISMATCH_VOID(         "Function \"%2$s\" should return \"%3$s\", but returns nothing instead! Line: %1$d",
                                                ""),
    CHECK_FN_RETURN_TYPE_MISMATCH_ANON(         "Anonymous function should return \"%2$s\", but returns \"%3$s\" instead! Line: %1$d",
                                                ""),
    CHECK_FN_RETURN_TYPE_MISMATCH_VOID_ANON(    "Anonymous function should return \"%2$s\", but returns nothing instead! Line: %1$d",
                                                "");

    public String format;
    public String suggestion;

    Errors(String format_, String suggestion_){
        format = format_;
        suggestion = suggestion_;
    }
}
