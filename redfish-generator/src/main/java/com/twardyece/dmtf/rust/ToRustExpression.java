package com.twardyece.dmtf.rust;

public interface ToRustExpression {
    RustExpression toRustExpression();
    record RustExpression(String expression) {}
}
