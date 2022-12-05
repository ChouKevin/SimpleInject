package com.example.common;

import java.lang.reflect.Type;
import java.util.Map;

public class TypeInfo {

    private Type type;

    public TypeInfo(Class<?> clazz) {
        this.type = clazz;
    }

    // List<T>, Set<T> or Map<K, V>
    public boolean isGeneric() {
        return false;
    }
}
