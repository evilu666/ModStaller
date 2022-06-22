package com.evilu.modstaller.core;

import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

/**
 * Injector
 */
public class Injector {

    public static @interface Service {

    }


    static {
        final Reflections refs = new Reflections(Scanners.TypesAnnotated);
        final Set<Class<?>> serviceClasses = refs.getTypesAnnotatedWith(Service.class);

    }
    
}
