package com.twardyece.dmtf.component.name;

import com.twardyece.dmtf.text.SnakeCaseName;

public interface INameMapper {
    SnakeCaseName matchComponent(String name);
}
