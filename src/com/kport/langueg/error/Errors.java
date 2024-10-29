package com.kport.langueg.error;

public enum Errors {

    PARSE_BLOCK_NOT_CLOSED(                     "Block opened on line %1$d isn't ever closed!",
                                                "Add a closing curly bracket or remove the opening one"),

    PARSE_ATOM_REACHED_EOF(                     "Reached EOF whilst parsing atom!",
                                                ""),
    PARSE_ATOM_UNEXPECTED_TOKEN(                "Unexpected token \"%1$s\"",
                                                ""),

    PARSE_DELIM_EXPECTED_START(                 "Expected token \"%1$s\"",
                                                ""),
    PARSE_DELIM_EXPECTED_SEPARATOR(             "Expected token \"%1$s\"",
                                                ""),


    CHECK_SYNTHESIZE_UNION(                     "Cannot synthesize type of union constructor",
                                                "Use this in a context, where a type is expected."),
    CHECK_SYNTHESIZE_NUM_INFER(                 "Cannot synthesize type of number, without explicit annotation",
                                                "Annotate the type of this number or use it in a context, where a type is expected"),
    CHECK_SYNTHESIZE_VAR_INIT(                  "Cannot synthesize type of expression used to initialize variable",
                                                "Annotate the type of this variable or make the type of expression synthesizeable"),
    CHECK_SYNTHESIZE_IF_ELSE_FIRST_IF(          "If-else expression's if branch's type cannot be synthesized",
                                                ""),
    CHECK_SYNTHESIZE_TUPLE_MULTI_INIT(          "Element of tuple is initialized multiple times",
                                                ""),
    CHECK_SYNTHESIZE_TUPLE_ELEM(                "Cannot synthesize type of tuple element",
                                                ""),
    CHECK_SYNTHESIZE_EXPR_STMNT(                "Cannot synthesize type of expression statement",
                                                ""),
    CHECK_SYNTHESIZE_BLOCK_VAL(                 "Cannot synthesize type of yielding code block",
                                                ""),
    CHECK_SYNTHESIZE_CALLED(                    "Cannot synthesize type of called expression",
                                                ""),
    CHECK_SYNTHESIZE_MATCHED_VAL(               "Cannot synthesize type of matched expression",
                                                ""),
    CHECK_SYNTHESIZE_MATCH_BRANCH(              "Cannot synthesize type of first match branch",
                                                ""),
    CHECK_SYNTHESIZE_ASSIGN_LEFT(               "Cannot synthesize type of assigned to location",
                                                ""),
    CHECK_SYNTHESIZE_REF_REFERENT(              "Cannot synthesize type of referent",
                                                ""),
    CHECK_SYNTHESIZE_DEREF_REF(                 "Cannot synthesize type of dereferenced expression",
                                                ""),


    CHECK_CHECK_GENERIC(                        "Expression can't be made to have type %1$s, because synthesized type %2$s doesn't match",
                                                ""),

    CHECK_CHECK_TUPLE(                          "Tuple can't be made to have type %1$s",
                                                ""),
    CHECK_CHECK_TUPLE_NO_INDEX(                 "Element with index %1$s doesn't exist in type expected of this tuple constructor",
                                                ""),
    CHECK_CHECK_TUPLE_NO_NAME(                  "Element with name %1$s doesn't exist in type expected of this tuple constructor",
                                                ""),
    CHECK_CHECK_TUPLE_ALREADY_INIT(             "Element is initialized multiple times in tuple constructor",
                                                ""),

    CHECK_CHECK_UNION(                          "Union can't be made to have type %1$s",
                                                ""),
    CHECK_CHECK_UNION_NO_INDEX(                 "Element with index %1$s doesn't exist in type expected of this union constructor",
                                                ""),
    CHECK_CHECK_UNION_NO_NAME(                  "Element with name %1$s doesn't exist in type expected of this union constructor",
                                                ""),

    CHECK_CHECK_IF_COND(                        "Condition of if expression can't be made to have type boolean",
                                                ""),

    CHECK_CHECK_IF_ELSE_COND(                   "Condition of if-else expression can't be made to have type boolean",
                                                ""),
    CHECK_CHECK_IF_ELSE_IF(                     "If-else expression's if branch can't be made to have type %1$s",
                                                ""),
    CHECK_CHECK_IF_ELSE_ELSE(                   "If-else expression's else branch can't be made to have type %1$s",
                                                ""),
    CHECK_CHECK_IF_ELSE_ELSE_SYN_FROM_IF(       "If-else expression's else branch can't be made to have type %1$s, expected by if branch",
                                                ""),

    CHECK_CHECK_WHILE_COND(                     "Condition of while loop can't be made to have type boolean",
                                                ""),
    CHECK_CHECK_WHILE_BODY(                     "Body of while loop can't be made to have unit type",
                                                ""),

    CHECK_CHECK_MATCH(                          "Branch of match expression cant be made to have type %1$s, expected by first branch",
                                                ""),

    CHECK_CHECK_FN_ARG(                         "Function argument can't be made to have type %1$s",
                                                ""),

    CHECK_CHECK_RETURN(                         "Returned value can't be made to have type %1$s",
                                                ""),

    CHECK_CHECK_ASSIGN(                         "Cannot assign to location expecting %1$s",
                                                ""),

    CHECK_CHECK_REF(                            "Ref can't be made to have type %1$s",
                                                ""),

    CHECK_CHECK_NUM_INFER(                      "Number can't be made to have type %1$s",
                                                ""),

    CHECK_CHECK_VAR_INIT(                       "Variable initializer can't be made to have type %1$s",
                                                ""),


    PLACEHOLDER(                                "Fix this error",
                                                "pls");

    public final String format;
    public final String suggestion;

    Errors(String format_, String suggestion_) {
        format = format_;
        suggestion = suggestion_;
    }

    public static String formatError(String formatStr, CharSequence source, int offset, Object... args) {
        String errorString = String.format(formatStr, args);

        //%%CL Code line
        int indexOfPrevNewline = offset;
        while (source.charAt(indexOfPrevNewline) != '\n' && indexOfPrevNewline > 0) indexOfPrevNewline--;
        int indexOfNextNewline = offset;
        while (source.charAt(indexOfNextNewline) != '\n' && indexOfNextNewline < source.length()) indexOfNextNewline++;
        String line = source.subSequence(indexOfPrevNewline + 1, indexOfNextNewline).toString();

        errorString = errorString.replaceAll("%CL", line);
        return errorString;
    }
}
