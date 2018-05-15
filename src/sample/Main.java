package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("main.fxml"));
        primaryStage.setMaximized(true);
        primaryStage.setTitle("Signal Flow Graph");
        String css = this.getClass().getResource("style.css").toExternalForm();
        root.getStylesheets().add(css);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        Controller.stage = primaryStage;
    }


    public static void main(String[] args) throws ScriptException {
        launch(args);


    }
}
