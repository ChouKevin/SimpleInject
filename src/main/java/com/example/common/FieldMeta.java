package com.example.common;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.NotImplementedException;

import com.example.common.annotation.Autowire;
import com.example.common.annotation.Qualifier;

public class FieldMeta {
    private final Field field;
    private TypeInfo type;
    private boolean hasQualifier;

    public static List<FieldMeta> build(Class<?> clazz) {
        List<FieldMeta> fields = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (!field.isAnnotationPresent(Autowire.class))
                continue;
            fields.add(new FieldMeta(field));
        }
        return fields;
    }

    private FieldMeta(Field field) {
        this.field = field;
        this.init();
    }

    private void init() {
        this.field.setAccessible(true);
        this.hasQualifier = this.field.isAnnotationPresent(Qualifier.class);
        this.type = TypeInfo.forField(field);
        this.checkNotImplemented();
    }

    private void checkNotImplemented() {
        if (this.type.isArray() || this.type.isGenericArray() || this.type.isGenericValue()) {
            throw new NotImplementedException(String.format("not support to inject some type, happend in %s field", this.field.getDeclaringClass(), this.field.getName()));
        }
    }

    public void inject(Object obj, Map<Class<?>, Set<ClassMeta>> classTree, Context ctx)
            throws IllegalAccessException {
        if (this.type.isGeneric() && Collection.class.isAssignableFrom(this.type.resolve())) {
            this.injectWithMultiObjects(obj, classTree, ctx);
            return;
        }
        Set<ClassMeta> concreteClasses = classTree.getOrDefault(field.getType(), Collections.emptySet());
        if (concreteClasses.size() > 1) {
            this.injectWithQualifier(obj, concreteClasses, ctx);
            return;
        }
        this.injectConcreteClass(obj, concreteClasses, ctx);
    }

    private void injectConcreteClass(Object obj, Set<ClassMeta> concreteClasses, Context ctx)
            throws IllegalAccessException {
        if (concreteClasses.size() == 0) {
            throw new RuntimeException(
                    String.format("can not found component %s ", field.getType()));
        }
        ClassMeta meta = concreteClasses.iterator().next();
        this.set(obj, ctx.get(meta.clazz()));
    }

    private void injectWithQualifier(Object obj, Set<ClassMeta> concreteClasses, Context ctx)
            throws IllegalAccessException {
        if (!this.hasQualifier) {
            StringBuilder sb = new StringBuilder();
            String head = String.format("%s require a single class, but %s were found: ",
                    this.field.getType(), concreteClasses.size());
            sb.append(head).append(System.lineSeparator());
            concreteClasses.forEach(meta -> {
                sb.append(meta.clazz()).append(System.lineSeparator());
            });
            throw new RuntimeException(sb.toString());
        }
        Qualifier qualifier = this.field.getAnnotation(Qualifier.class);
        for (ClassMeta meta : concreteClasses) {
            if (meta.clazz() == qualifier.value()) {
                Object val = ctx.get(meta.clazz());
                this.set(obj, val);
            }
        }
    }

    private void injectWithMultiObjects(Object obj, Map<Class<?>, Set<ClassMeta>> classTree, Context ctx)
            throws IllegalAccessException {
        ParameterizedType type = (ParameterizedType) field.getGenericType();
        Class<?> clazz = (Class<?>) type.getActualTypeArguments()[0];
        Set<ClassMeta> concreteClasses = classTree.getOrDefault(clazz, Collections.emptySet());
        if (Set.class.isAssignableFrom(field.getType())) {
            Set<Object> val = new HashSet<>();
            for (ClassMeta concreteClass : concreteClasses) {
                val.add(ctx.get(concreteClass.clazz()));
            }
            this.set(obj, val);
        } else {
            Collection<Object> val = new ArrayList<>();
            for (ClassMeta concreteClass : concreteClasses) {
                val.add(ctx.get(concreteClass.clazz()));
            }
            this.set(obj, val);
        }
    }

    private void set(Object obj, Object val) throws IllegalAccessException {
        this.field.set(obj, val);
    }
}
