package com.twardyece.dmtf.api.name;

import com.twardyece.dmtf.text.SnakeCaseName;

public interface INameMapper {
    SnakeCaseName matchComponent(String name);
}
