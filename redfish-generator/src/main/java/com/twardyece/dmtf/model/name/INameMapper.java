package com.twardyece.dmtf.model.name;

import com.twardyece.dmtf.text.SnakeCaseName;

public interface INameMapper {
    SnakeCaseName matchComponent(String name);
}
