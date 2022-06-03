package com.kport.langueg.codeGen.mcDataCodeGen.op;

import com.kport.langueg.codeGen.mcDataCodeGen.MCDataCodeGenerator;
import com.kport.langueg.lex.TokenType;
import com.kport.langueg.typeCheck.types.Type;

import java.util.Arrays;
import java.util.EnumMap;

import static com.kport.langueg.lex.TokenType.*;
import static com.kport.langueg.codeGen.mcDataCodeGen.MCDataCodeGenerator.*;

public enum MCDataDefaultBinOpGenerators implements BinOpGeneratorSupplier{

    PLUS_INT((ast, output) -> {
        loadArithRegsFromStack(output);
        scoreboardOpArith(Plus, Int, output);
        pushStackVar(tmpVar1, output);
    }, Plus, Int),

    MINUS_INT((ast, output) -> {
        loadArithRegsFromStack(output);
        scoreboardOpArith(Minus, Int, output);
        pushStackVar(tmpVar1, output);
    }, Minus, Int),

    MUL_INT((ast, output) -> {
        loadArithRegsFromStack(output);
        scoreboardOpArith(Mul, Int, output);
        pushStackVar(tmpVar1, output);
    }, Mul, Int),

    DIV_INT((ast, output) -> {
        loadArithRegsFromStack(output);
        scoreboardOpArith(Div, Int, output);
        pushStackVar(tmpVar1, output);
    }, Div, Int),

    MOD_INT((ast, output) -> {
        loadArithRegsFromStack(output);
        scoreboardOpArith(Mod, Int, output);
        pushStackVar(tmpVar1, output);
    }, Mod, Int),

    PLUS_LONG((ast, output) -> {
        loadArithRegsFromStack(output);
        scoreboardOpArith(Plus, Long, output);
        pushStackVar(tmpVar1, output);
    }, Plus, Long),

    MINUS_LONG((ast, output) -> {
        loadArithRegsFromStack(output);
        scoreboardOpArith(Minus, Long, output);
        pushStackVar(tmpVar1, output);
    }, Minus, Long),

    MUL_LONG((ast, output) -> {
        loadArithRegsFromStack(output);
        scoreboardOpArith(Mul, Long, output);
        pushStackVar(tmpVar1, output);
    }, Mul, Long),

    DIV_LONG((ast, output) -> {
        loadArithRegsFromStack(output);
        scoreboardOpArith(Div, Long, output);
        pushStackVar(tmpVar1, output);
    }, Div, Long),

    MOD_LONG((ast, output) -> {
        loadArithRegsFromStack(output);
        scoreboardOpArith(Mod, Long, output);
        pushStackVar(tmpVar1, output);
    }, Mod, Long),

    PLUS_BYTE((ast, output) -> {
        loadArithRegsFromStack(output);
        scoreboardOpArith(Plus, Byte, output);
        pushStackVar(tmpVar1, output);
    }, Plus, Byte),

    MINUS_BYTE((ast, output) -> {
        loadArithRegsFromStack(output);
        scoreboardOpArith(Minus, Byte, output);
        pushStackVar(tmpVar1, output);
    }, Minus, Byte),

    MUL_BYTE((ast, output) -> {
        loadArithRegsFromStack(output);
        scoreboardOpArith(Mul, Byte, output);
        pushStackVar(tmpVar1, output);
    }, Mul, Byte),

    DIV_BYTE((ast, output) -> {
        loadArithRegsFromStack(output);
        scoreboardOpArith(Div, Byte, output);
        pushStackVar(tmpVar1, output);
    }, Div, Byte),

    MOD_BYTE((ast, output) -> {
        loadArithRegsFromStack(output);
        scoreboardOpArith(Mod, Byte, output);
        pushStackVar(tmpVar1, output);
    }, Mod, Byte),

    PLUS_FLOAT((ast, output) -> {
        storeStackIntoVar(tmpVar1, output);
        storeStackIntoVar(tmpVar2, output);

        output.append("execute store result score ").append(arithmeticReg1).append(" ").append(globalScoreboard).append(" run data get storage ").append(tmpVar1).append(" val.e\n");
        output.append("execute store result score ").append(arithmeticReg2).append(" ").append(globalScoreboard).append(" run data get storage ").append(tmpVar2).append(" val.e\n");

        //ARITH_REG_3 0 = tmp1.e > tmp2.e     1 = tmp2.e >= tmp1.e
        output.append("scoreboard players set ").append(arithmeticReg3).append(" ").append(globalScoreboard).append(" 0\n");
        output.append("execute if score ").append(arithmeticReg2).append(" ").append(globalScoreboard).append(" >= ").append(arithmeticReg1).append(" ").append(globalScoreboard)
                .append(" run scoreboard players set ").append(arithmeticReg3).append(" ").append(globalScoreboard).append(" 1\n");

        //ARITH_REG_4 = diff tmp1.e tmp2.e
        output.append("scoreboard players operation ").append(arithmeticReg4).append(" ").append(globalScoreboard).append(" = ").append(arithmeticReg1).append(" ").append(globalScoreboard).append("\n");
        output.append("scoreboard players operation ").append(arithmeticReg4).append(" ").append(globalScoreboard).append(" -= ").append(arithmeticReg2).append(" ").append(globalScoreboard).append("\n");
        output.append("execute if score ").append(arithmeticReg4).append(" ").append(globalScoreboard).append(" matches ..0 " +
                "run scoreboard players operation ").append(arithmeticReg4).append(" ").append(globalScoreboard).append(" *= ").append(constPrefix + "-1 ").append(globalScoreboard).append("\n");

        //set tmp1.e to largest e
        output.append("execute if score ").append(arithmeticReg3).append(" ").append(globalScoreboard).append(" matches 1 run ")
                .append("data modify storage ").append(tmpVar1).append(" val.e set from storage ").append(tmpVar2).append(" val.e\n");

        //STORE correct m into a1
        output.append("execute if score ").append(arithmeticReg3).append(" ").append(globalScoreboard).append(" matches 0 run ")
                .append("execute store result score ").append(arithmeticReg1).append(" ").append(globalScoreboard).append(" run data get storage ").append(tmpVar2).append(" val.m 1\n");
        output.append("execute if score ").append(arithmeticReg3).append(" ").append(globalScoreboard).append(" matches 1 run ")
                .append("execute store result score ").append(arithmeticReg1).append(" ").append(globalScoreboard).append(" run data get storage ").append(tmpVar1).append(" val.m 1\n");

        //SHIFT m by diff
        output.append("scoreboard players operation ").append(arithmeticReg2).append(" ").append(globalScoreboard).append(" = ").append(arithmeticReg4).append(" ").append(globalScoreboard).append("\n");
        output.append("function ").append(languegMcCoreDatapack).append("core-libs/operators/right-bitshift\n");

        //STORE tmp1.m / tmp2.m into a2
        output.append("execute if score ").append(arithmeticReg3).append(" ").append(globalScoreboard).append(" matches 0 run ")
                .append("execute store result score ").append(arithmeticReg2).append(" ").append(globalScoreboard).append(" run data get storage ").append(tmpVar1).append(" val.m 1\n");
        output.append("execute if score ").append(arithmeticReg3).append(" ").append(globalScoreboard).append(" matches 1 run ")
                .append("execute store result score ").append(arithmeticReg2).append(" ").append(globalScoreboard).append(" run data get storage ").append(tmpVar2).append(" val.m 1\n");

        //ADD a1 and a2
        output.append("scoreboard players operation ").append(arithmeticReg1).append(" ").append(globalScoreboard).append(" += ").append(arithmeticReg2).append(" ").append(globalScoreboard).append("\n");

        //Normalize m
        output.append("scoreboard players set ").append(arithmeticReg4).append(" ").append(globalScoreboard).append(" 0\n");
        output.append("execute if score ").append(arithmeticReg1).append(" ").append(globalScoreboard).append(" matches ..8388607 ")
                .append("run scoreboard players set ").append(arithmeticReg4).append(" ").append(globalScoreboard).append(" -1\n");
        output.append("execute if score ").append(arithmeticReg1).append(" ").append(globalScoreboard).append(" matches 16777216.. ")
                .append("run scoreboard players set ").append(arithmeticReg4).append(" ").append(globalScoreboard).append(" 1\n");

        output.append("execute if score ").append(arithmeticReg4).append(" ").append(globalScoreboard).append(" matches -1 ")
                .append("run scoreboard players operation ").append(arithmeticReg1).append(" ").append(globalScoreboard).append(" *= ").append(constPrefix).append("2 ").append(globalScoreboard).append("\n");
        output.append("execute if score ").append(arithmeticReg4).append(" ").append(globalScoreboard).append(" matches 1 ")
                .append("run scoreboard players operation ").append(arithmeticReg1).append(" ").append(globalScoreboard).append(" /= ").append(constPrefix).append("2 ").append(globalScoreboard).append("\n");

        ///execute store result score #ARITHMETIC_REG_2 a run data get storage languegmc:tmp1 val.e 1
        output.append("execute store result score ").append(arithmeticReg2).append(" ").append(globalScoreboard)
                .append(" run data get storage ").append(tmpVar1).append(" val.e 1\n");
        output.append("scoreboard players operation ").append(arithmeticReg2).append(" ").append(globalScoreboard).append(" += ").append(arithmeticReg4).append(" ").append(globalScoreboard).append("\n");
        output.append("execute store result storage ").append(tmpVar1).append(" val.e byte 1 run scoreboard players get ").append(arithmeticReg2).append(" ").append(globalScoreboard).append("\n");


        //STORE a1 into tmp1.m
        output.append("execute store result storage ").append(tmpVar1).append(" val.m int 1 run scoreboard players get ").append(arithmeticReg1).append(" ").append(globalScoreboard).append("\n");
        pushStackVar(tmpVar1, output);

        //STORE a1 into tmp1.m / tmp2.m
        /*output.append("execute if score ").append(arithmeticReg3).append(" ").append(globalScoreboard).append(" matches 0 run ")
                .append("execute store result storage ").append(tmpVar2).append(" val.m byte 1 run scoreboard players get ").append(arithmeticReg1).append(" ").append(globalScoreboard).append("\n");
        output.append("execute if score ").append(arithmeticReg3).append(" ").append(globalScoreboard).append(" matches 1 run ")
                .append("execute store result storage ").append(tmpVar1).append(" val.m byte 1 run scoreboard players get ").append(arithmeticReg1).append(" ").append(globalScoreboard).append("\n");*/



    }, Plus, Float),

    ;


    private final BinOpGenerator generator;
    private final TokenType op;
    private final TokenType type;

    MCDataDefaultBinOpGenerators(BinOpGenerator generator_, TokenType op_, TokenType type_){
        generator = generator_;
        op = op_;
        type = type_;
    }

    private static final EnumMap<TokenType, EnumMap<TokenType, BinOpGenerator>> opToGenerator = new EnumMap<>(TokenType.class);
    static {
        Arrays.stream(values()).forEach((g) -> {
            opToGenerator.putIfAbsent(g.op, new EnumMap<>(TokenType.class));
            opToGenerator.get(g.op).put(g.type, g.generator);
        });
    }
    @Override
    public BinOpGenerator getFromOp(TokenType op, TokenType type) {
        return opToGenerator.get(op).get(type);
    }
}
