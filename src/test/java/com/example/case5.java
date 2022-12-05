package com.example;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class case5 {
    public interface State<T> {

    }

    static public abstract class absState<T> {

    }

    static public class A implements State<String> {
    }

    static public class B extends absState<Integer> {
    }

    static public class C extends absState<Integer> implements State<Integer> {

    }

    static public class CC implements State<Integer> {
    }

    public class D {
    }

    public class E extends D implements State<C> {

    }

    public class EE extends A {

    }

    @Test
    public void testtt() throws ClassNotFoundException {
        Map<Integer, Integer> map;

        log.info("{}", Class.forName("map"));
    }

    @Test
    public void test() {
        // class java.lang.Object
        log.info("{}", A.class.getGenericSuperclass());
        // State<java.lang.String>
        log.info("{}", A.class.getGenericInterfaces());
        // absState<java.lang.Integer>
        log.info("{}", B.class.getGenericSuperclass());
        // {}
        log.info("{}", B.class.getGenericInterfaces());

        // absState<java.lang.Integer>
        log.info("{}", C.class.getGenericSuperclass());
        // State<java.lang.Integer>
        log.info("{}", C.class.getGenericInterfaces());

        // class java.lang.Object
        log.info("{}", D.class.getGenericSuperclass());
        // {}
        log.info("{}", D.class.getGenericInterfaces());

        // D
        log.info("{}", E.class.getGenericSuperclass());
        // false
        log.info("{}", E.class.getGenericSuperclass() instanceof ParameterizedType);
        // State<com.example.case5$C>
        log.info("{}", E.class.getGenericInterfaces());

        log.info("{}, {}, {}", CC.class.getGenericInterfaces()[0], C.class.getGenericInterfaces()[0],
                C.class.getGenericInterfaces()[0].equals(CC.class.getGenericInterfaces()[0]));
    }

    @Test
    public void test2() {
        // mappingRegistrar(A.class);
        mappingRegistrar(C.class);
    }

    protected static void mappingRegistrar(Class<?> cls) {
        if (cls.getGenericSuperclass() == Object.class)
            return;
        Type[] type = ((ParameterizedType) cls.getGenericSuperclass()).getActualTypeArguments();
        innerSeeIt(type[0]);
    }

    protected static void innerSeeIt(Type type) {
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

        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            String rawType = parameterizedType.getRawType().getTypeName();
            System.out.print(rawType + "<");
            Type[] arguments = parameterizedType.getActualTypeArguments();

            for (int i = 0; i < arguments.length; ++i) {
                innerSeeIt(arguments[i]);
                if (i != arguments.length - 1) {
                    System.out.print(", ");
                }

            }

            System.out.print(">");
            // System.out.println(Arrays.toString(arguments));
        }

        if (type instanceof GenericArrayType) {
            // you need to handle this one too
        }

        if (type instanceof WildcardType) {
            // you need to handle this one too, but it isn't trivial
        }
    }
}
