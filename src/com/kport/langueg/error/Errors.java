package com.kport.langueg.error;

public enum Errors {

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

    PARSE_BLOCK_NOT_CLOSED(                     "Block opened on line %1$d isn't ever closed!",
                                                "Add a closing curly bracket or remove the opening one"),

    PARSE_VAR_EXPECTED_IDENTIFIER(              "Expected a name for the variable being declared! Line: %d",
                                                "Variable declarations must follow the pattern: var/type name"),
    PARSE_VAR_DESTRUCT_EXPECTED_IDENTIFIER(     "Expected identifier in destructuring assignment! Line: %d",
                                                "Variable declaration with destructuring assignment must follow the pattern: var (name1, name2, ...) = ..."),
    PARSE_VAR_DESTRUCT_CANNOT_INFER_TYPE(       "Cannot infer type of variables, without assignment after declaration! Line: %d",
                                                "Variable declaration with destructuring assignment must follow the pattern: var (name1, name2, ...) = ..."),

    PARSE_INT_INVALID(                          "\"%2$s\" is not a valid integer! Line: %1$d",
                                                ""),
    PARSE_FLOAT_INVALID(                        "\"%2$s\" is not a valid float! Line: %1$d",
                                                ""),

    PARSE_ATOM_REACHED_EOF(                     "Reached EOF whilst parsing atom!",
                                                ""),
    PARSE_ATOM_UNEXPECTED_TOKEN(                "Unexpected token \"%2$s\"! Line: %1$d",
                                                ""),

    PARSE_DELIM_EXPECTED_START(                 "Expected token \"%2$s\"! Line: %1$d",
                                                ""),
    PARSE_DELIM_EXPECTED_SEPARATOR(             "Expected token \"%2$s\"! Line: %1$d",
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
                                                ""),

    PLACEHOLDER(                                "Fix this error",
                                                "pls");

    public final String format;
    public final String suggestion;

    Errors(String format_, String suggestion_){
        format = format_;
        suggestion = suggestion_;
    }
}
