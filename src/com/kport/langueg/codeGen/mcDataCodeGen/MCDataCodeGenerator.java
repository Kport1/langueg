package com.kport.langueg.codeGen.mcDataCodeGen;

import com.kport.langueg.lex.TokenType;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.typeCheck.FnIdentifier;
import com.kport.langueg.typeCheck.VarIdentifier;
import com.kport.langueg.typeCheck.types.Type;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class MCDataCodeGenerator {
    private HashMap<FnIdentifier, Type> fnTypes;
    private HashMap<VarIdentifier, Type> varTypes;
    private HashMap<VarIdentifier, Type> fnParamTypes;
    private String outDir;// /functions/outDir/

    public MCDataCodeGenerator(HashMap<FnIdentifier, Type> fnTypes_, HashMap<VarIdentifier, Type> varTypes_, HashMap<VarIdentifier, Type> fnParamTypes_,
                               String file_){
        fnTypes = fnTypes_;
        varTypes = varTypes_;
        fnParamTypes = fnParamTypes_;
        outDir = file_;
        new File(outDir).mkdir();
    }

    public void generate(AST ast) throws IOException {
        gen(ast, 0, 0, new FileOutputStream(outDir + "/main.mcfunction"));
    }

    private static final int floatSize = 10000;
    final String floatSizeStr = String.format("%.15f", 1d / (floatSize * floatSize)).replace(',', '.');
    private void gen(AST ast, int depth, int count, FileOutputStream outFile) throws IOException {

        switch (ast.type){
            case Prog -> {
                for(AST expr : ast.children){
                    gen(expr, depth, count, outFile);
                }
            }
            case Type -> {
            }
            case Cast -> {
            }
            case Fn -> {
            }
            case FnArg -> {
            }
            case Tuple -> {
            }
            case Class -> {
            }
            case Str -> {
            }
            case Double -> {
                //PUSH
                outFile.write(("data modify storage languegmc:stack val prepend value " + ast.val.getDub() + "d\n").getBytes(StandardCharsets.UTF_8));
            }
            case Float -> {
                //PUSH
                outFile.write(("data modify storage languegmc:stack val prepend value " + ast.val.getFloat() + "f\n").getBytes(StandardCharsets.UTF_8));
            }
            case Int -> {
                //PUSH
                outFile.write(("data modify storage languegmc:stack val prepend value " + ast.val.getInt() + "\n").getBytes(StandardCharsets.UTF_8));
            }
            case Byte -> {
                //PUSH
                outFile.write(("data modify storage languegmc:stack val prepend value " + ast.val.getByte() + "b\n").getBytes(StandardCharsets.UTF_8));
            }
            case Long -> {
            }
            case Bool -> {
            }
            case If -> {
            }
            case Switch -> {
            }
            case While -> {
            }
            case For -> {
            }
            case Call -> {
            }
            case Block -> {
            }
            case Return -> {
            }
            case Var -> {
                String name = "languegmc:var_" + ast.children[0].val.getStr() + "_" + depth + "_" + count;
                if (ast.children.length > 1){
                    gen(ast.children[1], depth, count, outFile);
                    outFile.write(
                            ("execute store result storage " + name + " val " + typeToStr(ast.returnType.primitive()) + " " +
                                    (ast.returnType.primitive() == TokenType.Float? floatSizeStr : "1")
                                    + " run data get storage languegmc:stack val[0] 1\n")
                                    .getBytes(StandardCharsets.UTF_8)
                    );
                    popStack(outFile);
                }
            }
            case BinOp -> {
                TokenType op = ast.val.getTok();

                if(op == TokenType.Assign){
                    String name = "languegmc:var_" + ast.children[0].val.getStr() + "_" + depth + "_" + count;
                    gen(ast.children[1], depth, count, outFile);
                    outFile.write(
                            ("execute store result storage " + name + " val " + typeToStr(ast.returnType.primitive()) + " " +
                                    (ast.returnType.primitive() == TokenType.Float? floatSizeStr : "1")
                                    + " run data get storage languegmc:stack val[0] 1\n")
                                    .getBytes(StandardCharsets.UTF_8)
                    );
                    popStack(outFile);
                    return;
                }

                gen(ast.children[1], 0, 0, outFile);
                gen(ast.children[0], 0, 0, outFile);

                if(ast.returnType.equals(new Type(TokenType.Float))){
                    outFile.write(("execute store result score #ARITHMETIC_REG_1 a run data get storage languegmc:stack val[0] " + floatSize + "\n").getBytes(StandardCharsets.UTF_8));
                    popStack(outFile);
                    outFile.write(("execute store result score #ARITHMETIC_REG_2 a run data get storage languegmc:stack val[0] " + floatSize + "\n").getBytes(StandardCharsets.UTF_8));
                    popStack(outFile);
                }
                else {
                    outFile.write("execute store result score #ARITHMETIC_REG_1 a run data get storage languegmc:stack val[0] 1\n".getBytes(StandardCharsets.UTF_8));
                    popStack(outFile);
                    outFile.write("execute store result score #ARITHMETIC_REG_2 a run data get storage languegmc:stack val[0] 1\n".getBytes(StandardCharsets.UTF_8));
                    popStack(outFile);
                }

                //OP
                String opStr = switch (op){
                    case Plus -> "+=";
                    case Minus -> "-=";
                    case Mul -> "*=";
                    case Div -> "/=";
                    case Mod -> "%=";
                    default -> throw new Error("aaaaa");
                };

                outFile.write(("scoreboard players operation #ARITHMETIC_REG_1 a " + opStr + " #ARITHMETIC_REG_2 a\n").getBytes(StandardCharsets.UTF_8));

                //STORE
                if(ast.returnType.equals(new Type(TokenType.Float))) {
                    outFile.write("execute store result storage languegmc:tmp1 val float 1 run scoreboard players get #ARITHMETIC_REG_1 a\n".getBytes(StandardCharsets.UTF_8));
                }
                else {
                    outFile.write("execute store result storage languegmc:tmp1 val int 1 run scoreboard players get #ARITHMETIC_REG_1 a\n".getBytes(StandardCharsets.UTF_8));
                }
                outFile.write(("data modify storage languegmc:stack val prepend from storage languegmc:tmp1 val\n").getBytes(StandardCharsets.UTF_8));
            }
            case UnaryOpBefore -> {
            }
            case UnaryOpAfter -> {
            }
            case Modifier -> {
            }
            case Identifier -> {
                String name = "languegmc:var_" + ast.val.getStr() + "_" + depth + "_" + count;
                //STORE
                outFile.write(("data modify storage languegmc:stack val prepend from storage " + name + " val\n").getBytes(StandardCharsets.UTF_8));
            }
        }

    }

    private void popStack(FileOutputStream outFile) throws IOException {
        outFile.write(("data remove storage languegmc:stack val[0]\n").getBytes(StandardCharsets.UTF_8));
    }

    private String typeToStr(TokenType t){
        return switch (t){
            case Int -> "int";
            case Byte -> "byte";
            case Long -> "long";
            case Double -> "double";
            case Float -> "float";

            default -> "int";
        };
    }



}
