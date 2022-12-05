package com.example.common;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.NotImplementedException;

import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class TypeMeta {
    private Set<String> parentTypes;

    private boolean isCollection;

    public TypeMeta(Class<?> clazz) {
        this.checkNotImplemented(clazz);
        this.isCollection = Collection.class.isAssignableFrom(clazz);
        this.resolveSuperClass(clazz);
    }

    private void checkNotImplemented(Class<?> clazz) {
        if (Map.class.isAssignableFrom(clazz)
                || Array.class.isAssignableFrom(clazz))
            throw new NotImplementedException("not support map class");
    }

    private void resolveSuperClass(Type type) {
        if (type instanceof Class) {
            Class<?> cls = (Class<?>) type;
            boolean isArray = cls.isArray();
            if (isArray) {
                System.out.print(cls.getComponentType().getSimpleName() + "[]");
                return;
            }
            System.out.print(cls.getSimpleName());

        }

        if (type instanceof TypeVariable) {
            Type[] bounds = ((TypeVariable<?>) type).getBounds();
            String s = Arrays.stream(bounds).map(Type::getTypeName).collect(Collectors.joining(", ", "[", "]"));
            System.out.print(s);
        }

        // if (type instanceof ParameterizedType) {
        // ParameterizedType parameterizedType = (ParameterizedType) type;
        // String rawType = parameterizedType.getRawType().getTypeName();
        // System.out.print(rawType + "<");
        // Type[] arguments = parameterizedType.getActualTypeArguments();

        // for (int i = 0; i < arguments.length; ++i) {
        // innerSeeIt(arguments[i]);
        // if (i != arguments.length - 1) {
        // System.out.print(", ");
        // }

        // }

        // System.out.print(">");
        // // System.out.println(Arrays.toString(arguments));
        // }

        // if (type instanceof GenericArrayType) {
        // // you need to handle this one too
        // }

        // if (type instanceof WildcardType) {
        // // you need to handle this one too, but it isn't trivial
        // }
    }

}
