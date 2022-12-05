package com.example.common;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Map.Entry;

import com.example.common.annotation.Subscribe;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Dispatcher {

    @AllArgsConstructor
    static class Subscriber {
        final Method method;
        final Object targetInstance;

        public boolean equals(Object other) {
            if (other instanceof Subscriber) {
                return ((Subscriber) other).targetInstance.equals(this.targetInstance);
            }
            return false;
        }

        public void invoke(Object event) {
            try {
                method.invoke(this.targetInstance, event);
            } catch (IllegalArgumentException e) {
                throw new Error("Method rejected target/argument: " + event, e);
            } catch (IllegalAccessException e) {
                throw new Error("Method became inaccessible: " + event, e);
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof Error) {
                    throw (Error) e.getCause();
                }
            }
        }
    }

    private Map<Class<?>, List<Subscriber>> map = new HashMap<>();
    private Queue<ContextEvent> eventQueue = new LinkedList<>();
    private boolean oneEventIsProcessing = false;

    public void register(Object listener) {
        Multimap<Class<?>, Subscriber> eventMap = eventMap(listener);
        for (Entry<Class<?>, Collection<Subscriber>> en : eventMap.asMap().entrySet()) {
            Class<?> eventClazz = en.getKey();
            if (!ContextEvent.class.isAssignableFrom(eventClazz))
                continue;
            Collection<Subscriber> eventMethods = en.getValue();
            List<Subscriber> listeners = this.map.getOrDefault(eventClazz, new ArrayList<>());
            eventMethods.forEach(method -> {
                if (listeners.contains(method))
                    return;
                listeners.add(method);
            });
            this.map.put(eventClazz, listeners);
        }
    }

    private Multimap<Class<?>, Subscriber> eventMap(Object listener) {
        Multimap<Class<?>, Subscriber> methodsInListener = HashMultimap.create();
        Class<?> clazz = listener.getClass();
        for (Method method : clazz.getDeclaredMethods()) {
            method.setAccessible(true);
            if (!method.isAnnotationPresent(Subscribe.class))
                continue;
            if (method.getParameterCount() != 1)
                continue;
            Class<?> eventType = method.getParameterTypes()[0];
            methodsInListener.put(eventType, new Subscriber(method, listener));
        }
        return methodsInListener;
    }

    public <E extends ContextEvent> void publish(List<E> events) {
        events.forEach(this::publish);
    }

    public <E extends ContextEvent> void publish(E event) {
        if (this.oneEventIsProcessing) {
            this.eventQueue.add(event);
            return;
        }
        this.oneEventIsProcessing = true;
        this.eventQueue.add(event);
        while (!this.eventQueue.isEmpty()) {
            ContextEvent polledEvent = this.eventQueue.poll();
            log.info("publish {}", polledEvent.toString());
            Class<?> eventClazz = polledEvent.getClass();
            List<Subscriber> subscribers = this.map.getOrDefault(eventClazz, Collections.emptyList());
            for (Subscriber subscriber : subscribers) {
                subscriber.invoke(polledEvent);
            }
        }
        this.oneEventIsProcessing = false;
    }
}
