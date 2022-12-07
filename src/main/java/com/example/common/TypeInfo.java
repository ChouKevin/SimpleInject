package com.example.common;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class TypeInfo {

    private Type type;
    private Class<?> resolve;
    private TypeInfo[] genericTypes;

    public static TypeInfo forField(Field field) {
        return new TypeInfo(field);
    }

    public static TypeInfo forType(Type type) {
        return new TypeInfo(type);
    }

    public static TypeInfo forClass(Class<?> clazz) {
        return new TypeInfo(clazz);
    }

    private TypeInfo(Type type) {
        this.type = type;
        this.resolve = resolveClass();
    }

    private TypeInfo(Class<?> clazz) {
        this.type = clazz;
        this.resolve = clazz;
    }

    private TypeInfo(Field field) {
        this(field.getGenericType());
    }

    //  CustumerClass<T>, List<T>, Set<T> or Map<K, V>
    public boolean isGeneric() {
        return this.type instanceof ParameterizedType;
    }
    // CustumerClass<T>[], List<T>[], ....
    public boolean isArray() {
        if (this.type instanceof Class<?> clazz) {
            return clazz.isArray();
        }
        return false;
    }
    // T[]
    public boolean isGenericArray() {
        return this.type instanceof GenericArrayType;
    }
    // T
    public boolean isGenericValue() {
        return this.type instanceof TypeVariable;
    }
    // CustumerClass, int, String ....
    public boolean isClass() {
        return this.resolve == this.type;
    }

    private Class<?> resolveClass() {
        if (this.type instanceof GenericArrayType genericArrayType) {
            // when type == List<T>[], need return List<T>
            Class<?> resolvedComponent = getComponentType().resolve();
            return (resolvedComponent != null ? Array.newInstance(resolvedComponent, 0).getClass() : null);
        }
        if (this.type instanceof ParameterizedType parameterizedType) {
            return forType(parameterizedType.getRawType()).resolve();
        }
        // String, int, long ..., or array type int[], String[] ....
        if (this.type instanceof Class<?> clazz) {
            return clazz;
        }
        if (this.type instanceof TypeVariable<?> typeVariable) {
            return null;
        }
        return null;
    }

    public TypeInfo getComponentType() {
		if (this.type instanceof Class<?> clazz) {
			Class<?> componentType = clazz.getComponentType();
			return forType(componentType);
		}
		if (this.type instanceof GenericArrayType genericArrayType) {
			return forType(genericArrayType.getGenericComponentType());
		}
		return null;
	}

    public TypeInfo[] getGenerics() {
        if (this.type instanceof ParameterizedType parameterizedType) {
            Type[] types = parameterizedType.getActualTypeArguments();
            TypeInfo[] typeInfos = new TypeInfo[types.length];
            for (int i = 0; i < types.length; i++) {
                typeInfos[i] = forType(types[i]);
            }
            this.genericTypes = typeInfos;
        }
        return this.genericTypes;
    }

}
