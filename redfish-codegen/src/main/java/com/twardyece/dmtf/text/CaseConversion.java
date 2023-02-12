package com.twardyece.dmtf.text;

public class CaseConversion {
    public static PascalCaseName toPascalCase(String name) {
        try {
            return new PascalCaseName(name);
        } catch (ICaseConvertible.CaseConversionError e) {
            SnakeCaseName snakeCaseName = new SnakeCaseName(name);
            return new PascalCaseName(snakeCaseName);
        }
    }

    public static SnakeCaseName toSnakeCase(String name) {
        try {
            return new SnakeCaseName(name);
        } catch (ICaseConvertible.CaseConversionError e) {
            PascalCaseName pascalCaseName = new PascalCaseName(name);
            return new SnakeCaseName(pascalCaseName);
        }
    }
}
