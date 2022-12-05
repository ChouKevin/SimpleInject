package com.example.common;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.example.common.annotation.Component;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;

import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Accessors(fluent = true)
public class Container {
    protected static Set<ClassMeta> classMetaSet;
    private static Map<Long, Context> ctxMapper = new ConcurrentHashMap<>();

    public static <T> T get(long id, Class<T> clazz) {
        Context context = find(id);
        Object obj = context.get(clazz);
        return (T) obj;
    }

    public static Set<Long> all() {
        return ImmutableSet.copyOf(ctxMapper.keySet());
    }

    public static void inject(long id, Object object) throws IllegalAccessException {
        Context context = find(id);
        context.inject(object);
    }

    public static void remove(long id) {
        ctxMapper.remove(id);
    }

    public static Context find(long id) {
        ctxMapper.computeIfAbsent(id, (__) -> {
            Context context = new Context(id);
            try {
                context.init();
            } catch (Exception e) {
                log.info(ExceptionUtils.getMessage(e));
                throw new RuntimeException("context init error");
            }
            return context;
        });
        return ctxMapper.get(id);
    }

    public static void loadComponent(Class<?> app) {
        final String packageName = app.getPackageName();
        loadComponent(packageName);
    }

    public static void loadComponent(String packageName) {
        log.info("load class from: {}", packageName);
        try {
            classMetaSet = ClassPath.from(ClassLoader.getSystemClassLoader())
                    .getAllClasses()
                    .stream()
                    .filter(clazz -> clazz.getName().startsWith(packageName))
                    .map(clazz -> clazz.load())
                    .filter(clazz -> clazz.isAnnotationPresent(Component.class))
                    .map(ClassMeta::new)
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            log.info(ExceptionUtils.getMessage(e));
            throw new RuntimeException("load class error");
        }
    }
}
