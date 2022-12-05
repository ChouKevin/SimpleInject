package com.example.common;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Context {
    private final long id;
    private final Dispatcher dispatcher;
    private final Map<Class<?>, Object> components = new HashMap<>();
    private final Map<Class<?>, Set<ClassMeta>> classTree = new HashMap<>();

    public Context(long id) {
        this.id = id;
        this.dispatcher = new Dispatcher();
    }

    public void init() throws InstantiationException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException, SecurityException {
        this.createObj();
        this.inject();
        this.register();
    }

    private void inject() throws IllegalAccessException {
        for (ClassMeta meta : Container.classMetaSet) {
            Object autowriedObject = components.get(meta.clazz());
            meta.injectContext(autowriedObject, classTree, this);
        }
    }

    private void createObj() {
        for (ClassMeta meta : Container.classMetaSet) {
            this.components.put(meta.clazz(), meta.newInstance());
            for (Class<?> superClazz : meta.superClasses()) {
                this.classTree.putIfAbsent(superClazz, new HashSet<>());
                this.classTree.get(superClazz).add(meta);
            }
        }
    }

    public void inject(Object object) throws IllegalAccessException {
        Class<?> clazz = object.getClass();
        ClassMeta meta = new ClassMeta(clazz);
        meta.injectContext(object, classTree, this);
    }

    public Object get(Class<?> clazz) {
        return this.components.get(clazz);
    }

    private void register() {
        for (Object obj : this.components.values()) {
            this.dispatcher.register(obj);
        }
    }

}
