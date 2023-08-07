package com.kport.langueg.desugar;

import com.kport.langueg.error.ErrorHandler;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.parse.ast.nodes.expr.NAnonFn;
import com.kport.langueg.parse.ast.nodes.expr.NAssign;
import com.kport.langueg.parse.ast.nodes.expr.NAssignable;
import com.kport.langueg.parse.ast.nodes.expr.NBinOp;
import com.kport.langueg.parse.ast.nodes.statement.NNamedFn;
import com.kport.langueg.parse.ast.nodes.statement.NReturn;
import com.kport.langueg.pipeline.LanguegPipeline;
import com.sun.jdi.InvalidTypeException;

public class DefaultDesugarer implements Desugarer {

    private ErrorHandler errorHandler;
    @Override
    public AST process(Object ast_, LanguegPipeline<?, ?> pipeline) {
        AST ast = (AST) ast_;
        errorHandler = pipeline.getErrorHandler();

        ast.accept(new ASTVisitor() {
            @Override
            public void visit(NBinOp binOp, VisitorContext context) {
                if(!binOp.op.isOpAssign()) return;

                for(int i = 0; i < binOp.parent.getChildren().length; i++){
                    if(binOp.parent.getChildren()[i] == binOp){
                        binOp.op = binOp.op.getOpOfOpAssign();

                        if(!(binOp.left instanceof NAssignable assignable)) throw new Error();
                        try {
                            binOp.parent.setChild(i, new NAssign(binOp.line, binOp.column, assignable, binOp));
                        } catch (InvalidTypeException e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            }

            @Override
            public void visit(NNamedFn fn, VisitorContext context) {
                if(!(fn.block instanceof NExpr expr)) return;
                fn.block = new NReturn(fn.block.line, fn.block.column, expr);
            }

            @Override
            public void visit(NAnonFn fn, VisitorContext context) {
                if(!(fn.block instanceof NExpr expr)) return;
                fn.block = new NReturn(fn.block.line, fn.block.column, expr);
            }
        }, null);

        return ast;
    }
}
