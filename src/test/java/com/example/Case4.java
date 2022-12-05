package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.common.Container;
import com.example.common.annotation.Autowire;
import com.example.common.annotation.Component;

@Component
public class Case4 {
    public interface State<T> {

    }

    @Component
    static public class A implements State<String> {
        public A() {
        };
    }

    @Component
    static public class B implements State<Integer> {
        public B() {
        };
    }

    @Autowire
    State<String> string;

    @Autowire
    State<Integer> integer;

    @BeforeEach
    public void beforeEach() {
        Container.loadComponent(Case4.class.getCanonicalName());
    }

    @AfterEach
    public void afterEach() {
        Container.remove(0);
    }

    @Test()
    public void test() {
        assertThrows(Exception.class, () -> {
            Case4 case4 = Container.get(0, Case4.class);
        });
    }
}
