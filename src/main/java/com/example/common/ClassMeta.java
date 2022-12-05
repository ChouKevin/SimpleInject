package com.example.common;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.exception.ExceptionUtils;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Accessors(fluent = true)
public class ClassMeta {

    @Getter
    private final Class<?> clazz;
    private final TypeMeta type;
    private final List<FieldMeta> autowired;
    @Getter
    private final Set<Class<?>> superClasses;

    public ClassMeta(Class<?> clazz) {
        this.clazz = clazz;
        this.type = new TypeMeta(clazz);
        this.autowired = FieldMeta.build(clazz);
        this.superClasses = this.resolveSuperClass(clazz);
        log.info("{}, {}", clazz, this.superClasses);
    }

    public Object newInstance() {
        for (Constructor<?> constructor : this.clazz.getConstructors()) {
            if (constructor.getParameterCount() > 0)
                continue;
            try {
                return constructor.newInstance();
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException e) {
                log.info(ExceptionUtils.getMessage(e));
                throw new RuntimeException(String.format("create component %s fail", this.clazz.getSimpleName()));
            }
        }
        throw new NotImplementedException(String.format("not support %s constructor that have args", this.clazz));
    }

    public void injectContext(Object obj, Map<Class<?>, Set<ClassMeta>> classTree, Context ctx)
            throws IllegalAccessException {
        for (FieldMeta field : autowired) {
            field.inject(obj, classTree, ctx);
        }
    }

    private Set<Class<?>> resolveSuperClass(Class<?> clazz) {
        if (Object.class.equals(clazz) || clazz == null)
            return Collections.emptySet();
        Set<Class<?>> ans = new HashSet<>();
        ans.add(clazz);
        ans.addAll(ClassUtils.getAllInterfaces(clazz));
        return ans;
    }
}
