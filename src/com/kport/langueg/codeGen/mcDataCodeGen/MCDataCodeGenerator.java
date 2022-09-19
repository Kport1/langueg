package com.kport.langueg.codeGen.mcDataCodeGen;

import com.kport.langueg.codeGen.CodeGenerator;
import com.kport.langueg.codeGen.mcDataCodeGen.op.BinOpGeneratorSupplier;
import com.kport.langueg.codeGen.mcDataCodeGen.op.MCDataDefaultBinOpGenerators;
import com.kport.langueg.lex.TokenType;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.pipeline.LanguegPipeline;
import com.kport.langueg.typeCheck.types.Type;

public class MCDataCodeGenerator implements CodeGenerator {
    private final StringBuilder output = new StringBuilder();
    public static final String stack = "languegmc:stack";

    public static final String tmpVar1 = "languegmc:tmp1";
    public static final String tmpVar2 = "languegmc:tmp2";

    public static final String tmpVarSign = "languegmc:tmpSign";
    public static final String tmpVarExp = "languegmc:tmpExp";
    public static final String tmpVarMant = "languegmc:tmpMant";

    public static final String globalScoreboard = "a";
    public static final String arithmeticReg1 = "#ARITHMETIC_REG_1";
    public static final String arithmeticReg2 = "#ARITHMETIC_REG_2";
    public static final String arithmeticReg3 = "#ARITHMETIC_REG_3";
    public static final String arithmeticReg4 = "#ARITHMETIC_REG_4";

    public static final String languegMcCoreDatapack = "languegmc-core:";
    public static final String constPrefix = "#CONST_";

    BinOpGeneratorSupplier binOpGeneratorSupplier = MCDataDefaultBinOpGenerators.PLUS_INT;



    @Override
    public byte[] process(Object ast_, LanguegPipeline<?, ?> pipeline) {
        AST ast = (AST) ast_;

        //gen libs

        gen(ast, 0, 0, output);

        //return output.toString();
        return null;
    }




    private void gen(AST ast, int depth, int count, StringBuilder output) {

        switch (ast.type){
            case Prog -> {
                for(AST expr : ast.children){
                    gen(expr, depth, count, output);
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
                long doubleBits = Double.doubleToLongBits(ast.val.getDub());

                long sign = (doubleBits & 0x8000000000000000L) >> 63;
                long exponent = ((doubleBits & 0x7ff0000000000000L) >> 52) - 1023;
                long mantissa = (doubleBits & 0x000fffffffffffffL);

                String doubleStr = "{s : " + sign + "b, e : " + exponent + ", m : " + mantissa + "l}";

                pushStackValue(doubleStr, output);
            }
            case Float -> {
                int floatBits = Float.floatToIntBits(ast.val.getFloat());

                int sign = (floatBits & 0x80000000) >> 31;
                int exponent = ((floatBits & 0x7f800000) >> 23) - 127;
                int mantissa = (floatBits & 0x007fffff);

                String floatStr = "{s : " + sign + "b, e : " + exponent + "b, m : " + mantissa + "}";

                pushStackValue(floatStr, output);
            }
            case Int -> {
                pushStackValue(ast.val.getInt() + "", output);
            }
            case Byte -> {
                pushStackValue(ast.val.getByte() + "b", output);
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
                String name = getVarName(ast.children[0].val.getStr(), depth, count);
                if (ast.children.length > 1){
                    gen(ast.children[1], depth, count, output);
                    storeStackIntoVar(name, output);
                }
            }
            case BinOp -> {
                TokenType op = ast.val.getTok();
                Type type = ast.returnType;

                if(op == TokenType.Assign){
                    String name = getVarName(ast.children[0].val.getStr(), depth, count);
                    gen(ast.children[1], depth, count, output);
                    storeStackIntoVar(name, output);
                    return;
                }

                gen(ast.children[1], 0, 0, output);
                gen(ast.children[0], 0, 0, output);

                if(!type.isPrimitive()){
                    throw new Error("ono");
                }

                binOpGeneratorSupplier.getFromOp(op, type.primitive()).gen(ast, output);
            }
            case UnaryOpBefore -> {
            }
            case UnaryOpAfter -> {
            }
            case Modifier -> {
            }
            case Identifier -> {
                String name = getVarName(ast.val.getStr(), depth, count);
                //STORE
                pushStackVar(name, output);
            }
        }

    }

    public static void pushStackValue(String val, StringBuilder output){
        output.append("data modify storage ").append(stack).append(" val prepend value ").append(val).append("\n");
    }

    public static void pushStackVar(String varName, StringBuilder output){
        output.append("data modify storage ").append(stack).append(" val prepend from storage ").append(varName).append(" val\n");
    }

    public static void storeStackIntoVar(String varName, StringBuilder output){
        output.append("data modify storage ").append(varName).append(" val set from storage ").append(stack).append(" val[0]\n");
        popStack(output);
    }

    public static void popStack(StringBuilder output) {
        output.append("data remove storage ").append(stack).append(" val[0]\n");
    }

    public static void storeVarIntoVar(String var1, String var2, StringBuilder output){
        output.append("data modify storage ").append(var2).append(" val set from storage ").append(var1).append(" val\n");
    }

    public static void loadArithRegsFromStack(StringBuilder output){
        output.append("execute store result score ").append(arithmeticReg1).append(" ").append(globalScoreboard).append(" run data get storage ").append(stack).append(" val[0]\n");
        popStack(output);
        output.append("execute store result score ").append(arithmeticReg2).append(" ").append(globalScoreboard).append(" run data get storage ").append(stack).append(" val[0]\n");
        popStack(output);
    }

    public static void scoreboardOpArith(TokenType op, TokenType type, StringBuilder output){
        output.append("scoreboard players operation ").append(arithmeticReg1).append(" ").append(globalScoreboard).append(" ").append(opToString(op)).append(" ").append(arithmeticReg2).append(" ").append(globalScoreboard).append("\n");
        output.append("execute store result storage ").append(tmpVar1).append(" val ").append(typeToStr(type)).append(" 1 run scoreboard players get ").append(arithmeticReg1).append(" ").append(globalScoreboard).append("\n");
    }

    public static String typeToStr(TokenType t){
        return switch (t){
            case Int -> "int";
            case Byte -> "byte";
            case Long -> "long";
            case Double -> "double";
            case Float -> "float";

            default -> "int";
        };
    }

    public static String opToString(TokenType op){
        return switch (op){
            case Plus: yield "+=";
            case Minus: yield "-=";
            case Mul: yield  "*=";
            case Div: yield "/=";
            case Mod: yield "%=";

            default: throw new Error("nooo");
        };
    }

    public static String getVarName(String var, int depth, int count){
        return "languegmc:var_" + var + "_" + depth + "_" + count;
    }




}
