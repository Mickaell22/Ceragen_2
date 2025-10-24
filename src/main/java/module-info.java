module com.example.ceragen_2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;

    requires java.sql;
    requires io.github.cdimascio.dotenv.java;
    requires org.slf4j;
    requires ch.qos.logback.classic;

    opens com.example.ceragen_2 to javafx.fxml;
    opens com.example.ceragen_2.controller to javafx.fxml;

    exports com.example.ceragen_2;
    exports com.example.ceragen_2.config;
    exports com.example.ceragen_2.util;
    exports com.example.ceragen_2.controller;
    exports com.example.ceragen_2.service;
}