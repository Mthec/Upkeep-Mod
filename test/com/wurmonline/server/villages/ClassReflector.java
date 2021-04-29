package com.wurmonline.server.villages;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

class ClassReflector {
    private Class<?> clazz;

    ClassReflector(Class<?> clazz) {
        this.clazz = clazz;
    }

    Field getDeclaredField(String name) throws NoSuchFieldException {
        Field field = clazz.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    Method getDeclaredMethod(String name, Class<?>...parameterTypes) throws NoSuchMethodException {
        Method method = clazz.getDeclaredMethod(name, parameterTypes);
        method.setAccessible(true);
        return method;
    }
}
