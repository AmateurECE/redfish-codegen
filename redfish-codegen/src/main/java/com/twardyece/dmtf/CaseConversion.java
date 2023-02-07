package com.twardyece.dmtf;

public class CaseConversion {
    public static PascalCasedName toPascalCase(String name) {
        try {
            return new PascalCasedName(name);
        } catch (ICaseConvertible.CaseConversionError e) {
            SnakeCaseName snakeCaseName = new SnakeCaseName(name);
            return new PascalCasedName(snakeCaseName);
        }
    }
}
