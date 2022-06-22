package com.evilu.modstaller;

import com.evilu.modstaller.core.ApplicationContext;
import com.evilu.modstaller.ui.MainScene;

import javafx.application.Application;
import javafx.stage.Stage;


/**
 * JavaFX App
 */
public class App extends Application {

    public static final String APP_NAME = "Modstaller";
    public static final String APP_VERSION = "0.1";


    @Override
    public void start(final Stage stage) {
        stage.setScene(new MainScene());
        stage.show();
    }

    public static void main(final String[] args) {
        ApplicationContext.init();
        launch();
    }

}
