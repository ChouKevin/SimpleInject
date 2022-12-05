package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.common.Container;
import com.example.common.annotation.Autowire;
import com.example.common.annotation.Qualifier;
import com.example.common.annotation.Component;

@Component
public class Case3 {

    public interface State {

    }

    @Component
    static public class A implements State {
        public A() {
        };
    }

    @Component
    static public class B implements State {
        public B() {
        };
    }

    @Autowire
    @Qualifier(A.class)
    State state;

    @BeforeEach
    public void beforeEach() {
        Container.loadComponent(Case3.class.getCanonicalName());
    }

    @AfterEach
    public void afterEach() {
        Container.remove(0);
    }

    @Test
    void testName() {
        Case3 case3 = Container.get(0, Case3.class);
        A a = Container.get(0, A.class);
        assertEquals(case3.state, a);
    }

}
