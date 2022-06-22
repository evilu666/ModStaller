package com.evilu.modstaller.core;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import java.io.InputStream;


import org.yaml.snakeyaml.Yaml;

import io.vavr.Tuple2;

/**
 * YamlProperties
 */
public class YamlProperties {

    @SuppressWarnings("unchecked")
    public static Stream<Tuple2<String, String>> parse(final Yaml yaml, final InputStream is) {
        return StreamSupport.stream(yaml.loadAll(is).spliterator(), false)
            .flatMap(o -> parseEntries(null, (Map<String, Object>) o));
    }


    @SuppressWarnings("unchecked")
    private static Stream<Tuple2<String, String>> parseEntries(final String currentPrefix, final Map<String, Object> map) {
        return map.entrySet().stream()
            .flatMap(e -> {
                final String newPrefix = Optional.ofNullable(currentPrefix)
                    .map(p -> String.format("%s.%s", p, e.getKey()))
                    .orElseGet(e::getKey);

                if (e.getValue() instanceof String) {
                    return Stream.of(new Tuple2<>(newPrefix, (String) e.getValue()));
                } else {
                    return parseEntries(newPrefix, (Map<String, Object>) e.getValue());
                }
            });
    }
    
}
