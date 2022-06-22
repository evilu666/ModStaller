package com.evilu.modstaller.core;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import java.io.IOException;

import com.evilu.modstaller.constant.Language;
import com.evilu.modstaller.model.Config;
import com.evilu.modstaller.ui.util.BindingUtil;
import com.google.inject.Inject;

import org.yaml.snakeyaml.Yaml;

import io.vavr.Tuple2;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringExpression;
import javafx.beans.property.ReadOnlyStringWrapper;

/**
 * TranslationService
 */
public class TranslationService {

    private final Map<String, ReadOnlyStringWrapper> props;
    private final Yaml yaml;

    @Inject
    public TranslationService(final Config config, final Yaml yaml) throws IOException {
        this.yaml = yaml;

        final String propsFilename = String.format("messages-%s.yaml", config.getSettings().getLanguage().name().toLowerCase());
        props = YamlProperties.parse(yaml, getClass().getClassLoader().getResourceAsStream(propsFilename))
            .collect(Collectors.toMap(Tuple2::_1, t -> new ReadOnlyStringWrapper(t._2)));

        config.getSettings().getLanguageProperty().addListener((obs, o, n) -> {
            if (n != null) loadLanguage(n);
        });
    }

    private void loadLanguage(final Language lang) {
        final String propsFilename = String.format("messages-%s.yaml", lang.name().toLowerCase());
        YamlProperties.parse(yaml, getClass().getClassLoader().getResourceAsStream(propsFilename))
            .forEach(t -> props.get(t._1).setValue(t._2));
    }

    @Deprecated(forRemoval = true)
    public String translate(final String key, final Object... values) {
        return translateOnce(key, values);
    }

    public String translateOnce(final String key, final Object... values) {
        if (!props.containsKey(key)) throw new RuntimeException("No message found with key: " + key);

        final List<String> stringValues = Arrays.stream(values).map(Object::toString).collect(Collectors.toList());
        return translate(props.get(key).get(), stringValues);
    }

    public StringExpression translated(final String key, final Object... values) {
        if (!props.containsKey(key)) throw new RuntimeException("No message found with key: " + key);

        final List<String> stringValues = Arrays.stream(values).map(Object::toString).collect(Collectors.toList());
        return BindingUtil.mapString(props.get(key), str -> translate(str, stringValues), "<null>");
    }

    public StringExpression translatedExpression(final String key, final StringExpression... values) {
        if (!props.containsKey(key)) throw new RuntimeException("No message found with key: " + key);

        final List<StringExpression> expressions = Arrays.stream(values).collect(Collectors.toList());
        final StringExpression keyExp = props.get(key);
        final StringExpression[] deps = new StringExpression[values.length+1];
        System.arraycopy(values, 0, deps, 0, values.length);
        deps[values.length] = keyExp;

        return Bindings.createStringBinding(() -> translate(keyExp.get(), expressions.stream().map(StringExpression::getValue).collect(Collectors.toList())), deps);
    }

    private String translate(final String msg, final List<String> values) {
        String result = msg;
        for (int i = 0; i < values.size(); ++i) {
            result = result.replaceAll(Pattern.quote("${" + i + "}"), Matcher.quoteReplacement(values.get(i)));
        }

        return result;
    }

    
}
