package com.example;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.common.Container;
import com.example.common.annotation.Autowire;
import com.example.common.annotation.Component;

import static org.assertj.core.api.Assertions.assertThat;

@Component
public class Case1 {
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
    List<State> states;

    @Autowire
    Set<State> states2;

    @BeforeEach
    public void beforeEach() {
        Container.loadComponent(Case1.class.getCanonicalName());
    }

    @AfterEach
    public void afterEach() {
        Container.remove(0);
    }

    @Test
    void testName() {
        Case1 case1 = Container.get(0, Case1.class);
        A a = Container.get(0, A.class);
        B b = Container.get(0, B.class);
        assertThat(case1.states).hasSize(2).containsAll(Arrays.asList(a, b));
        assertThat(case1.states2).hasSize(2).containsAll(Arrays.asList(a, b));
    }
}
