package com.kport.langueg.parse;

import com.kport.langueg.lex.DefaultLexer;
import com.kport.langueg.lex.TokenType;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.BinOp;
import com.kport.langueg.parse.ast.nodes.*;
import com.kport.langueg.parse.ast.nodes.expr.operators.NBinOp;
import com.kport.langueg.parse.ast.nodes.expr.controlFlow.NCall;
import com.kport.langueg.parse.ast.nodes.expr.assignable.NIdent;
import com.kport.langueg.parse.ast.nodes.expr.dataTypes.number.integer.NInt32;
import com.kport.langueg.parse.ast.nodes.expr.dataTypes.number.integer.NUInt64;
import com.kport.langueg.parse.ast.nodes.expr.NBlock;
import com.kport.langueg.parse.ast.nodes.expr.controlFlow.NIf;
import com.kport.langueg.parse.ast.nodes.expr.controlFlow.NReturn;
import com.kport.langueg.parse.ast.nodes.statement.NVarInit;
import com.kport.langueg.pipeline.LanguegPipeline;
import com.kport.langueg.pipeline.LanguegPipelineBuilder;
import com.kport.langueg.typeCheck.types.FnType;
import com.kport.langueg.typeCheck.types.PrimitiveType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultParserTest {
    private static LanguegPipeline<String, AST> pipeline;

    @BeforeAll
    public static void setup(){
        LanguegPipelineBuilder<AST> pipelineBuilder = new LanguegPipelineBuilder<>();
        pipelineBuilder.addComponent(new DefaultLexer(), (o, p) -> {System.out.println(o);}, (o, p) -> {System.out.println(o);});
        pipelineBuilder.addComponent(new DefaultParser(), (o, p) -> {}, (o, p) -> {System.out.println(o);});
        pipeline = pipelineBuilder.get();
    }

    @Test
    public void test1(){
        AST actual = pipeline.evaluate("var a : i32 = 5i32;");
        AST expect = new NProg(0, new NVarInit(0, PrimitiveType.I32, "a", new NInt32(14, 5)));
        assertEquals(expect, actual);
    }

    @Test
    public void test2(){
        AST actual = pipeline.evaluate("var b:i32 -> f32 = fn((a: i32)-> f32) a \n\n\t\n");
        AST expect = new NProg(0,
                new NVarInit(0,
                        new FnType(PrimitiveType.F32, PrimitiveType.I32),
                        "b",
                        new NAnonFn(19,
                                new FnHeader(new NameTypePair[]{new NameTypePair(PrimitiveType.I32, "a")}, PrimitiveType.F32),
                                new NIdent(37, "a")
                        )
                )
        );
        assertEquals(expect, actual);
    }

    @Test
    public void test3(){
        AST actual = pipeline.evaluate("""
                fn f((i : u64) -> u64){
                    if(i == 0u64) return 1u64
                    return i * f(i - 1u64)
                }
                """);
        AST expect =    new NProg(0,
                            new NNamedFn(0, "f", new FnHeader(new NameTypePair[]{new NameTypePair(PrimitiveType.U64, "i")}, PrimitiveType.U64),
                                new NBlock(22,
                                    new NIf(28,
                                        new NBinOp(33, new NIdent(31, "i"), new NUInt64(36, 0), BinOp.Eq),
                                        new NReturn(42, new NUInt64(49, 1))
                                    ),
                                    new NReturn(59,
                                        new NBinOp(68,
                                            new NIdent(66, "i"),
                                            new NCall(71,
                                                new NIdent(70, "f"),
                                                new NBinOp(74, new NIdent(72, "i"), new NUInt64(76, 1), BinOp.Minus)
                                            ),
                                            BinOp.Mul
                                        )
                                    )
                                )
                            )
                        );
        assertEquals(expect, actual);
    }
}