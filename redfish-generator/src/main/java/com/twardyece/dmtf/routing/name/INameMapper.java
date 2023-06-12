package com.twardyece.dmtf.routing.name;

import com.twardyece.dmtf.text.SnakeCaseName;

public interface INameMapper {
    SnakeCaseName matchComponent(String name);
}
