package com.twardyece.dmtf.text;

public class CaseConversion {
    public static PascalCaseName toPascalCase(String name) {
        try {
            return new PascalCaseName(name);
        } catch (ICaseConvertible.CaseConversionError e) {
            try {
                SnakeCaseName snakeCaseName = new SnakeCaseName(name);
                return new PascalCaseName(snakeCaseName);
            } catch (ICaseConvertible.CaseConversionError f) {
                CamelCaseName camelCaseName = new CamelCaseName(name);
                return new PascalCaseName(camelCaseName);
            }
        }
    }

    public static SnakeCaseName toSnakeCase(String name) {
        try {
            return new SnakeCaseName(name);
        } catch (ICaseConvertible.CaseConversionError e) {
            try {
                PascalCaseName pascalCaseName = new PascalCaseName(name);
                return new SnakeCaseName(pascalCaseName);
            } catch (ICaseConvertible.CaseConversionError f) {
                CamelCaseName camelCaseName = new CamelCaseName(name);
                return new SnakeCaseName(camelCaseName);
            }
        }
    }
}
