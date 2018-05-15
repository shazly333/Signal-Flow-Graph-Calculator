package sample;

import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Created by M.Sharaf on 16/04/2018.
 */
public class GainMassage {

    public TextField gainText;
    public static String gain;
    private static Stage gainStage;

    public void initialize (){
        gainText.setAlignment(Pos.CENTER);
        gainText.getStyleClass().add("node-txt");
    }

    public static void display (Stage stage){
        gainStage = stage;
        gainStage.setOnCloseRequest(event -> {
            gain = "error";
        });
        stage.showAndWait();
    }

    public void onEnterGain (){
        if (!gainText.getText().equals("")){
            gain = gainText.getText();
            gainStage.close();
        }
    }
}
