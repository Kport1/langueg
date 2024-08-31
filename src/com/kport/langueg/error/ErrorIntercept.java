package com.kport.langueg.error;

public interface ErrorIntercept {
    //returns true to prevent error
    boolean intercept(Errors error, Object... additional);
}
