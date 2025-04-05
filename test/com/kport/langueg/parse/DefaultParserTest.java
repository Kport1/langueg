package com.kport.langueg.parse;

import com.kport.langueg.lex.DefaultLexer;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.BinOp;
import com.kport.langueg.parse.ast.nodes.NAnonFn;
import com.kport.langueg.parse.ast.nodes.NNamedFn;
import com.kport.langueg.parse.ast.nodes.NProg;
import com.kport.langueg.parse.ast.nodes.NameTypePair;
import com.kport.langueg.parse.ast.nodes.expr.NBlock;
import com.kport.langueg.parse.ast.nodes.expr.assignable.NIdent;
import com.kport.langueg.parse.ast.nodes.expr.controlFlow.NCall;
import com.kport.langueg.parse.ast.nodes.expr.controlFlow.NIf;
import com.kport.langueg.parse.ast.nodes.expr.controlFlow.NReturn;
import com.kport.langueg.parse.ast.nodes.expr.dataTypes.NTuple;
import com.kport.langueg.parse.ast.nodes.expr.dataTypes.number.integer.NInt32;
import com.kport.langueg.parse.ast.nodes.expr.dataTypes.number.integer.NUInt64;
import com.kport.langueg.parse.ast.nodes.expr.operators.NBinOp;
import com.kport.langueg.parse.ast.nodes.statement.NTypeDef;
import com.kport.langueg.parse.ast.nodes.statement.NVarInit;
import com.kport.langueg.pipeline.LanguegPipeline;
import com.kport.langueg.pipeline.LanguegPipelineBuilder;
import com.kport.langueg.typeCheck.types.FnType;
import com.kport.langueg.typeCheck.types.NamedType;
import com.kport.langueg.typeCheck.types.PrimitiveType;
import com.kport.langueg.typeCheck.types.TupleType;
import com.kport.langueg.util.Pair;
import com.kport.langueg.util.Span;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultParserTest {
    private static LanguegPipeline<String, AST> pipeline;

    @BeforeAll
    public static void setup() {
        LanguegPipelineBuilder<AST> pipelineBuilder = new LanguegPipelineBuilder<>();
        pipelineBuilder.addComponent(new DefaultLexer(), (o, p) -> {
            System.out.println(o);
        }, (o, p) -> {
            System.out.println(o);
        });
        pipelineBuilder.addComponent(new DefaultParser(), (o, p) -> {
        }, (o, p) -> {
            System.out.println(o);
        });
        pipeline = pipelineBuilder.get();
    }

    @Test
    public void test1() {
        AST actual = pipeline.evaluate("var a : i32 = 5i32");
        AST expect = new NProg(new Span(0, 17), new NVarInit(new Span(0, 17), PrimitiveType.I32, "a", new NInt32(new Span(14, 17), 5)));
        assertEquals(expect, actual);
    }

    @Test
    public void test2() {
        AST actual = pipeline.evaluate("var b:i32 -> f32 = fn (a: i32)-> f32 = a \n\n\t\n");
        AST expect = new NProg(new Span(0, 37),
                new NVarInit(new Span(0, 37),
                        new FnType(PrimitiveType.F32, PrimitiveType.I32),
                        "b",
                        new NAnonFn(new Span(19, 37),
                                new FnType(
                                        PrimitiveType.F32,
                                        new TupleType(new NameTypePair(PrimitiveType.I32, "a"))
                                ),
                                new NIdent(new Span(37, 37), "a")
                        )
                )
        );
        assertEquals(expect, actual);
    }

    @Test
    public void test3() {
        AST actual = pipeline.evaluate("""
                type Zero = ()
                type<T> Succ = (T,)
                var x : Succ<Succ<Zero>> = (((),),)
                """);
        AST expect = new NProg(new Span(0, 69),
                new NTypeDef(new Span(0, 13), "Zero", new TupleType()),
                new NTypeDef(new Span(15, 33), "Succ",
                        new TupleType(new NameTypePair(new NamedType("T"), null)),
                        "T"),
                new NVarInit(new Span(35, 69),
                        new NamedType("Succ", new NamedType("Succ", new NamedType("Zero"))),
                        "x",
                        new NTuple(new Span(62, 69), new Pair<>(null,
                                new NTuple(new Span(63, 67), new Pair<>(null,
                                        new NTuple(new Span(64, 65)))))
                        )
                )
        );
        assertEquals(expect, actual);
    }

    @Test
    public void test4() {
        AST actual = pipeline.evaluate("""
                fn f : (i : u64) -> u64 = {
                    if(i == 0u64) return 1u64
                    return i * f(i - 1u64)
                }
                """);
        AST expect = new NProg(new Span(0, 85),
                new NNamedFn(new Span(0, 85), "f",
                        new FnType(
                                PrimitiveType.U64,
                                new TupleType(new NameTypePair(PrimitiveType.U64, "i"))
                        ),
                        new NBlock(new Span(26, 85),
                                new NIf(new Span(32, 56),
                                        new NBinOp(new Span(35, 43), new NIdent(new Span(35, 35), "i"), new NUInt64(new Span(40, 43), 0), BinOp.Eq),
                                        new NReturn(new Span(46, 56), new NUInt64(new Span(53, 56), 1))
                                ),
                                new NReturn(new Span(62, 83),
                                        new NBinOp(new Span(69, 83),
                                                new NIdent(new Span(69, 69), "i"),
                                                new NCall(new Span(73, 83),
                                                        new NIdent(new Span(73, 73), "f"),
                                                        new NBinOp(new Span(75, 82),
                                                                new NIdent(new Span(75, 75), "i"),
                                                                new NUInt64(new Span(79, 82), 1),
                                                                BinOp.Minus
                                                        )
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