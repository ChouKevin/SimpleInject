package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.common.Container;
import com.example.common.annotation.Autowire;
import com.example.common.annotation.Component;

public class Case2 {

    @Component
    static public class A {
        @Autowire
        B b;
    }

    @Component
    static public class B {
        @Autowire
        A a;
    }

    @BeforeEach
    public void beforeEach() {
        Container.loadComponent(Case2.class.getCanonicalName());
    }

    @AfterEach
    public void afterEach() {
        Container.remove(0);
    }

    @Test
    public void test_circular_dependcy() {
        A a = Container.get(0, A.class);
        B b = Container.get(0, B.class);
        assertEquals(a.b, b);
        assertEquals(b.a, a);
    }
}
