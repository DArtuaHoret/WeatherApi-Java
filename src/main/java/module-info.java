module org.example {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing; // ← DODAJ TO
    requires org.jfree.jfreechart;
    requires com.google.gson;
    requires redis.clients.jedis;
    requires java.net.http;

    // Otwarcie pakietów dla FXML i refleksji
    opens org.example to javafx.fxml;
    opens org.example.controllers to javafx.fxml;  // Ważne dla kontrolerów FXML
    opens org.example.models to com.google.gson;   // Wymagane dla Gson serializacji

    // Eksporty
    exports org.example;
    exports org.example.controllers;  // Jeśli inne moduły mają używać kontrolerów
    exports org.example.services;
    exports org.example.models;
    exports org.example.utilities;    // Jeśli masz klasy narzędziowe
}