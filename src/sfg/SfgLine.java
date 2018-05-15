package sfg;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Shape;

/**
 * Created by M.Sharaf on 19/04/2018.
 */
public class SfgLine {
    private Shape line;
    private Shape arrow;
    private Label gain;

    public Shape getLine() {
        return line;
    }

    public Shape getArrow() {
        return arrow;
    }

    public SfgLine(Shape line, Shape arrow, Label gain) {
        this.line = line;
        this.arrow = arrow;
        this.gain = gain;
    }

    public void removeLine(Pane parent){
        parent.getChildren().remove(line);
        parent.getChildren().remove(arrow);
        parent.getChildren().remove(gain);
    }

}
