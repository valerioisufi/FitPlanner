package com.example.fitplannerclient.util;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

// Utility Class (o Helper Class).
// È una classe che serve solo come "contenitore" di funzioni globali
// e non è pensata per rappresentare un oggetto reale.
public final class I18n {

    private static final ObjectProperty<ResourceBundle> currentBundle = new SimpleObjectProperty<>();

    private I18n() {}


    public static void setLocale(Locale locale) {
        currentBundle.set(ResourceBundle.getBundle("i18n/messages", locale));
    }

    public static StringBinding createStringBinding(String key) {
        return Bindings.createStringBinding(
                () -> currentBundle.get().getString(key),
                currentBundle
        );
    }

    public static StringBinding createStringBinding(String key, Object... args) {
        return Bindings.createStringBinding(
                () -> MessageFormat.format(currentBundle.get().getString(key), args),
                currentBundle
        );
    }

    public static String get(String key) {
        return currentBundle.get().getString(key);
    }
}