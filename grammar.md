```
Prog = { Statement } 

Statement = ";"
            | Fn
            | Var, ";"
            | If
            | Return, ";"
            | Break, ";"
            | Continue, ";"
            | Switch
            | While
            | For
            | Block
            | Expr, ";" ;

Fn = "fn", Type, Id, "(", { FnParam }, ")", Statement ;

FnParam = Type, Id ;

Var = ( Type | "var" ), Id, [ "=", Expr ] ;

If = "if", "(", Expr, ")", Statement, [ "else", Statement ] ;

Return = "return", [ Expr ] ;

While = "while", "(", Expr, ")", Statement ;

Block = "{", { Statement }, "}" ;

Expr =  UOpPre, Expr
        | Expr, UOpPost
        | Expr, BOp, Expr
        | Expr, "(", [ Expr, { ",", Expr } ], ")"
        | Atom ;

Atom =  Tuple
        | String
        | Number
        | Identifier
        | "true" | "false"
        | AnonFn ;
        
Tuple = "(", [ Expr, { ",", Expr } ], ")" ;

Type = TypeAtom | "(", TypeAtom, ")" ;

TypeAtom =  "boolean" | "byte" | "char" | "short" | "int" | "long" | "float" | "double" | "void"
            | TupleType
            | FnType
            | CustomType ;
        
TupleType = "(", [ Type, { ",", Type } ], ")" ;

FnType = "(", [ Type, { ",", Type } ], ")", "->", Type ;

CustomType = Identifier ;
```