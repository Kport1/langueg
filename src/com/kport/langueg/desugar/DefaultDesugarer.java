package com.kport.langueg.desugar;

import com.kport.langueg.error.ErrorHandler;
import com.kport.langueg.error.Errors;
import com.kport.langueg.parse.ast.AST;
import com.kport.langueg.parse.ast.ASTVisitor;
import com.kport.langueg.parse.ast.VisitorContext;
import com.kport.langueg.parse.ast.nodes.NExpr;
import com.kport.langueg.parse.ast.nodes.NAnonFn;
import com.kport.langueg.parse.ast.nodes.expr.NAssign;
import com.kport.langueg.parse.ast.nodes.expr.NBinOp;
import com.kport.langueg.parse.ast.nodes.expr.assignable.NAssignable;
import com.kport.langueg.parse.ast.nodes.NNamedFn;
import com.kport.langueg.parse.ast.nodes.statement.NReturn;
import com.kport.langueg.pipeline.LanguegPipeline;

public class DefaultDesugarer implements Desugarer {

    private ErrorHandler errorHandler;
    @Override
    public AST process(Object ast_, LanguegPipeline<?, ?> pipeline) {
        AST ast = (AST) ast_;
        errorHandler = pipeline.getErrorHandler();

        ast.accept(new ASTVisitor() {
            @Override
            public void visit(NBinOp binOp, VisitorContext context) {
                if(!binOp.op.isCompoundAssign()) return;

                if(!(binOp.left instanceof NAssignable leftAssignable)) { errorHandler.error(Errors.PLACEHOLDER, 0); return; }
                new NAssign(binOp.offset, leftAssignable, new NBinOp(binOp.offset, leftAssignable, binOp.right, binOp.op.getOpOfCompoundAssign()));//TODO
            }

            @Override
            public void visit(NNamedFn fn, VisitorContext context) {
                if(!(fn.body instanceof NExpr expr)) return;
                fn.body = new NReturn(fn.body.offset, expr);
            }

            @Override
            public void visit(NAnonFn fn, VisitorContext context) {
                if(!(fn.body instanceof NExpr expr)) return;
                fn.body = new NReturn(fn.body.offset, expr);
            }
        }, null);

        return ast;
    }
}
