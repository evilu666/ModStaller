open module com.evilu.modstaller {
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires io.vavr;
    requires javafaker;
    requires com.google.guice;
    requires snakeyaml;
    requires lombok;
    requires org.reflections;
    requires org.apache.commons.lang3;
    requires org.apache.logging.log4j;
    requires org.apache.commons.io;
    requires toml4j;
    requires org.apache.commons.text;
    requires dev.dirs;
    requires org.antlr.antlr4.runtime;
    requires java.sql;

    requires transitive net.synedra.validatorfx;
    requires transitive FranzXaver;

    exports com.evilu.modstaller.version;
    exports com.evilu.modstaller.model;
    exports com.evilu.modstaller.constant;
}
