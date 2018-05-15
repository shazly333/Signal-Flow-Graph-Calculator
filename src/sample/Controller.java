package sample;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sfg.Edge;
import sfg.SfgLine;
import sfg.SfgPath;
import sfg.SignalFlow;

import javax.imageio.ImageIO;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

public class Controller {

    public TextField nodesNumText, gainText;
    public ComboBox<String> fromComboBox, toComboBox;
    public VBox customiserVBox;
    public ScrollPane scrollPane;
    public Pane mainPane;
    public TilePane pathTitlePane, cycleTitlePane, deltaTitlePane;
    public Tab cycleBtn;

    private Color nodeColor, edgeColor, gainColor, pressColor;
    private Circle sourceCircle;

    private final int nodeRadius = 15;
    private final int nodeSpacing = 150;

    public static Stage stage;
    public static LinkedList<LinkedList<Integer>> allNonTouched = new LinkedList<>();
    public static LinkedList<ArrayList<SfgPath>> cyclesAfterPaths = new LinkedList<>();

    private int nodesNum;
    private Dimension screenSize;
    private SignalFlow signalFlow;
    private boolean[] straightPath;
    private boolean isPressed, nonTouchedInserted;

    public BorderPane bot;

    public void initialize() {
        screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        mainPane.setPrefWidth(screenSize.getWidth());
        mainPane.setPrefHeight(screenSize.getHeight());

        edgeColor = new Color(.34, .57, .76, 1);
        nodeColor = new Color(0.3137, 0.4863, 0.651, 1);
        gainColor = new Color(0.051, 0.651, 0.4431, 1);
        pressColor = new Color(0.651, 0.0431, 0, 1);

        mainPane.setOnMouseReleased(event -> {
            if (sourceCircle != null) {
                sourceCircle.setStroke(nodeColor);
                sourceCircle = null;
                isPressed = false;
            }
        });

        mainPane.setOnScroll(event -> {
            double zoomFactor = 1.05;
            double deltaY = event.getDeltaY();
            if (deltaY < 0) {
                zoomFactor = 2.0 - zoomFactor;
            }

            mainPane.setScaleX(mainPane.getScaleX() * zoomFactor);
            mainPane.setScaleY(mainPane.getScaleY() * zoomFactor);
        });

        //onTestInput();
    }

    public void onForwardPaths() {

    }

    public void onCycles() {
        allNonTouched.clear();
        cyclesAfterPaths.clear();
        if (!cycleBtn.isSelected())
            return;

        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("JavaScript");
        try {
            deltaTitlePane.getChildren().clear();
            String[] delta = signalFlow.getDelta();


            for (int i = 0; i < delta.length; i++) {
                Label name = new Label("Δ" + String.valueOf(i));

                if (i == 0)
                    name.setText("Δ ");

                name.getStyleClass().add("path-name-lbl");

                Text deltaValue = new Text("= " + delta[i]);
                deltaValue.getStyleClass().add("path-name-lbl");
                deltaValue.setFill(edgeColor);

                VBox block = new VBox(5);
                block.getStyleClass().add("path-block");
                block.setPrefWidth(deltaValue.getLayoutBounds().getWidth() + 4);
                block.getChildren().addAll(name, deltaValue);

                if (delta[i].length() > 1) {
                    Text equalValue = new Text("= " + String.valueOf(engine.eval(delta[i])));
                    equalValue.getStyleClass().add("path-name-lbl");
                    equalValue.setFill(edgeColor);
                    block.getChildren().add(equalValue);
                }

                deltaTitlePane.getChildren().add(block);
            }

            //Adding Transfer Function
            Text tf = new Text("Transfer Function = " + signalFlow.calculateTransferFunction());
            tf.getStyleClass().add("path-name-lbl");
            tf.setFill(edgeColor);
            deltaTitlePane.getChildren().add(tf);

        } catch (Exception e) {

        }

        if (!nonTouchedInserted)
            showNonTouching();
    }

    public void onEnterPress() {
        try {
            nodesNum = Integer.valueOf(nodesNumText.getText());
        } catch (Exception e) {
            //TODO nan error
        }


        if (customiserVBox.isDisable())
            customiserVBox.setDisable(false);

        clearAll();

        if ((nodesNum + 1) * nodeSpacing > mainPane.getPrefWidth())
            mainPane.setPrefWidth((nodesNum + 1) * nodeSpacing);

        signalFlow = new SignalFlow(nodesNum);
        straightPath = new boolean[nodesNum];
        drawNodes(nodesNum);

        for (int i = 1; i <= nodesNum; i++) {
            fromComboBox.getItems().add(String.valueOf(i));
            toComboBox.getItems().add(String.valueOf(i));
        }

    }

    public void onInsertPress() {
        int source = Integer.valueOf(fromComboBox.getValue());
        int destination = Integer.valueOf(toComboBox.getValue());

        if (source < 1 || source > nodesNum || destination < 1 || destination > nodesNum)
            return;

        SfgLine sfgLine = null;
        int index = signalFlow.findEdge(source, destination);
        if (source == destination) {
            if (index != -1)
                calculateParallelGain(source, destination, index);

            sfgLine = drawSelfLoop(source, gainText.getText());
        } else if (destination == source + 1) {
            if (straightPath[source]) {
                calculateParallelGain(source, destination, index);
                sfgLine = drawStraightLine(source, gainText.getText());
                straightPath[source] = true;
            } else {
                straightPath[source] = true;
                sfgLine = drawStraightLine(source, gainText.getText());
            }
        } else {
            if (index != -1)
                calculateParallelGain(source, destination, index);

            sfgLine = drawEdge(source, destination, gainText.getText());
        }
        signalFlow.addGain(source, destination, gainText.getText(), sfgLine);

        addPaths(pathTitlePane, signalFlow.getForwardPaths(), false);
        addPaths(cycleTitlePane, signalFlow.getCycles(), true);
        nonTouchedInserted = false;

    }

    private void calculateParallelGain(int source, int destination, int index) {
        int gain = Integer.valueOf(signalFlow.getGraph()[source - 1].get(index).getGain()) +
                Integer.valueOf(gainText.getText());

        gainText.setText(String.valueOf(gain));
        signalFlow.removeGain(source, destination, mainPane);
    }

    public void onRemovePress() {
        int source = Integer.valueOf(fromComboBox.getValue());
        int destination = Integer.valueOf(toComboBox.getValue());
        if (source < 1 || source > nodesNum || destination < 1 || destination > nodesNum)
            return;

        if (destination == source + 1)
            straightPath[source] = false;

        signalFlow.removeGain(source, destination, mainPane);
        addPaths(pathTitlePane, signalFlow.getForwardPaths(), false);
        addPaths(cycleTitlePane, signalFlow.getCycles(), true);
        nonTouchedInserted = false;
    }

    private void drawNodes(int num) {
        for (int i = 1; i <= num; i++) {
            Circle node = new Circle(nodeRadius);
            node.setLayoutY(screenSize.getHeight() / 2);
            node.setLayoutX(nodeSpacing * i);
            node.setFill(Color.TRANSPARENT);
            node.setStroke(nodeColor);
            node.setStrokeWidth(2);
            node.setCursor(Cursor.HAND);

            int finalI = i;
            node.setOnMousePressed(event -> {
                isPressed = true;
                node.setStroke(pressColor);
                sourceCircle = node;
                fromComboBox.setValue(String.valueOf(finalI));
            });

            node.setOnDragDetected(event -> {
                node.startFullDrag();
            });

            node.setOnMouseDragEntered(event -> {
                if (isPressed) {
                    node.setStroke(pressColor);
                    toComboBox.setValue(String.valueOf(finalI));
                }
            });

            node.setOnMouseDragExited(event -> {
                if (sourceCircle != node)
                    node.setStroke(nodeColor);
            });

            node.setOnMouseExited(event -> {
                if (sourceCircle != node) {
                    node.setStroke(nodeColor);
                }
            });

            node.setOnMouseReleased(event -> {
                String gain = null;
                try {
                    gain = getGainFromPopUp();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (!gain.equals("error")) {
                    gainText.setText(gain);
                    onInsertPress();
                }
                sourceCircle.setStroke(nodeColor);
                sourceCircle = null;
                isPressed = false;
            });

            Text text = new Text(String.valueOf(i));
            text.setLayoutY(screenSize.getHeight() / 2 + nodeRadius / 2);
            text.setLayoutX(nodeSpacing * i - nodeRadius / 2);
            text.getStyleClass().add("node-txt");
            text.setFill(nodeColor);

            mainPane.getChildren().addAll(text, node);
        }
    }

    private SfgLine drawSelfLoop(int source, String gain) {
        Arc arc = new Arc(
                source * nodeSpacing, screenSize.getHeight() / 2 - 2 * nodeRadius,
                1.5 * nodeRadius - 2, 1.5 * nodeRadius, -40, 260
        );
        arc.setFill(Color.TRANSPARENT);
        arc.setStrokeWidth(2);
        arc.setStroke(edgeColor);

        Label label = new Label(gain);
        label.getStyleClass().add("gain-txt");
        label.setPadding(new Insets(0, 5, 0, 5));
        label.setAlignment(Pos.CENTER);
        label.setLayoutX(source * nodeSpacing - nodeRadius + 6 - gain.length() * 4);
        label.setLayoutY(screenSize.getHeight() / 2 - 4.5 * nodeRadius);

        mainPane.getChildren().addAll(arc, label);
        Shape arrow = makeArrowHead(source, true, true);
        return new SfgLine(arc, arrow, label);
    }

    private SfgLine drawStraightLine(int source, String gain) {
        double nodeMrgine = nodeRadius * 1.414;
        Line line = new Line(
                source * nodeSpacing + nodeMrgine, screenSize.getHeight() / 2,
                (source + 1) * nodeSpacing - nodeMrgine, screenSize.getHeight() / 2
        );
        line.setFill(Color.TRANSPARENT);
        line.setStrokeWidth(2);
        line.setStroke(edgeColor);

        Label label = new Label(gain);
        label.getStyleClass().add("gain-txt");
        label.setPadding(new Insets(0, 5, 0, 5));
        label.setAlignment(Pos.CENTER);
        label.setLayoutX((source + .5) * nodeSpacing - gain.length() * 10);
        label.setLayoutY(screenSize.getHeight() / 2 - nodeRadius);

        mainPane.getChildren().addAll(line, label);
        Shape arrow = makeArrowHead(source + 1, false, true);

        return new SfgLine(line, arrow, label);
    }

    private SfgLine drawEdge(int source, int destination, String gain) {
        boolean feedBackEdge = false;
        double middleHeight, gainHeight;
        int nodeMargin = nodeRadius, arrowDes = destination;

        if (destination < source) {
            int temp = source;
            source = destination;
            destination = temp;
            feedBackEdge = true;
        }

        double middleX = (destination + source) / 2.0;
        double leftX = (middleX + source) / 2.0;
        double rightX = (middleX + destination) / 2.0;

        if (feedBackEdge) {
            middleHeight = screenSize.getHeight() / 2 - (destination - source) * -50;
            nodeMargin *= -1;
        } else {
            middleHeight = screenSize.getHeight() / 2 - (destination - source) * 50;
        }


        CubicCurve curve = new CubicCurve(
                source * nodeSpacing + nodeRadius, screenSize.getHeight() / 2 - nodeMargin,
                leftX * nodeSpacing, middleHeight,
                rightX * nodeSpacing, middleHeight,
                destination * nodeSpacing - nodeRadius, screenSize.getHeight() / 2 - nodeMargin
        );
        curve.setFill(Color.TRANSPARENT);
        curve.setStrokeWidth(2);
        curve.setStroke(edgeColor);

        if (feedBackEdge) {
            gainHeight = screenSize.getHeight() / 2 + curve.getBoundsInParent().getHeight() - 3;
        } else {
            gainHeight = screenSize.getHeight() / 2 - curve.getBoundsInParent().getHeight() - 2 * nodeRadius + 3;
        }


        Label label = new Label(gain);
        label.getStyleClass().add("gain-txt");
        label.setPadding(new Insets(0, 5, 0, 5));
        label.setAlignment(Pos.CENTER);
        label.setLayoutX(middleX * nodeSpacing - gain.length() * 10);
        label.setLayoutY(gainHeight);

        mainPane.getChildren().addAll(curve, label);
        Shape arrow = makeArrowHead(arrowDes, feedBackEdge, false);

        return new SfgLine(curve, arrow, label);
    }

    private Shape makeArrowHead(int destination, boolean feedBack, boolean line) {
        int margin = 4;
        SVGPath arrow = new SVGPath();
        arrow.setContent("M 0 0 L 5 2 L 10 0 L 5 10");
        arrow.setFill(edgeColor);
        if (line && feedBack) {//selfLoopCase
            arrow.setLayoutX(destination * nodeSpacing - nodeRadius - margin - 1);
            arrow.setLayoutY(screenSize.getHeight() / 2 - nodeRadius - margin - 2);
            arrow.setRotate(-40);
        } else if (line) {
            arrow.setLayoutX(destination * nodeSpacing - 1.8 * nodeRadius);
            arrow.setLayoutY(screenSize.getHeight() / 2 - margin - 1);
            arrow.setRotate(-90);
        } else if (feedBack) {
            arrow.setLayoutX(destination * nodeSpacing + nodeRadius - margin);
            arrow.setLayoutY(screenSize.getHeight() / 2 + nodeRadius - margin);
            arrow.setRotate(135);
        } else {
            arrow.setLayoutX(destination * nodeSpacing - nodeRadius - margin - 2);
            arrow.setLayoutY(screenSize.getHeight() / 2 - nodeRadius - margin - 2);
            arrow.setRotate(-45);
        }
        mainPane.getChildren().add(arrow);
        return arrow;
    }

    private String getGainFromPopUp() throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("gain-massage.fxml"));
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);

        stage.maximizedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue)
                stage.setMaximized(false);
        });

        String css = this.getClass().getResource("style.css").toExternalForm();
        root.getStylesheets().add(css);
        stage.setScene(new Scene(root));
        GainMassage.display(stage);
        return GainMassage.gain;
    }

    private void addPaths(TilePane pathTitlePane, ArrayList<SfgPath> paths, boolean cycle) {
        pathTitlePane.getChildren().clear();
        StringBuilder gain = new StringBuilder(""), nodes = new StringBuilder("");
        ArrayList<Edge> path;
        for (int i = 0; i < paths.size(); i++) {
            nodes.setLength(0);
            gain.setLength(0);//clearing stringBuilder

            Label name = new Label("Path " + String.valueOf(i + 1));
            name.getStyleClass().add("path-name-lbl");

            if (cycle)
                name.setText("Cycle " + String.valueOf(i + 1));

            path = paths.get(i).getPath();

            for (int j = 0; j < path.size(); j++) {
                Edge node = path.get(j);
                nodes.append(node.getSource() + 1 + "  ");
                gain.append("(" + node.getGain() + ") ");
            }
            if (cycle) {
                nodes.append(path.get(0).getSource() + 1);//first node in cycle
            } else {
                nodes.append(nodesNum);//lastNode
            }

            Text nodesTxt = new Text(nodes.toString());
            nodesTxt.getStyleClass().add("path-name-lbl");
            nodesTxt.setFill(edgeColor);

            Text gainTxt = new Text("GAIN: " + gain);
            gainTxt.getStyleClass().add("path-name-lbl");
            gainTxt.setFill(gainColor);

            VBox block = new VBox(5);
            block.getStyleClass().add("path-block");
            pathTitlePane.setMargin(block, new Insets(0, 0, 0, 10));
            block.setCursor(Cursor.HAND);
            block.setPrefWidth(gainTxt.getLayoutBounds().getWidth() + 4);

            ArrayList<Edge> pathFinal = path;
            block.setOnMouseEntered(event -> {
                for (Edge edge : pathFinal) {
                    edge.getLine().getLine().setStroke(pressColor);
                    edge.getLine().getArrow().setFill(pressColor);
                }
            });

            block.setOnMouseExited(event -> {
                for (Edge edge : pathFinal) {
                    edge.getLine().getLine().setStroke(edgeColor);
                    edge.getLine().getArrow().setFill(edgeColor);
                }
            });

            block.getChildren().addAll(name, nodesTxt, gainTxt);
            pathTitlePane.getChildren().add(block);
        }
    }

    private void showNonTouching() {
        nonTouchedInserted = true;
        int currentLength = 1;
        int pathNum = 0;
        for (int i = 0; i < allNonTouched.size() && pathNum < signalFlow.forwardPathsNum() + 1; i++) {
            if (allNonTouched.get(i).size() == 1) {
                if (currentLength > 1) {
                    pathNum++;
                    currentLength = 1;
                }
                continue;
            }

            if (cyclesAfterPaths.get(pathNum).size() == 0){
                pathNum++;
                i--;
                continue;
            }

            if (allNonTouched.get(i).size() > currentLength) {
                currentLength++;
                Label label = new Label("All " + currentLength + " NonTouching Loops From Path " + pathNum);
                label.getStyleClass().add("path-name-lbl");
                cycleTitlePane.getChildren().add(label);
            }

            HBox container = new HBox(15);
            container.setAlignment(Pos.CENTER);
            container.getStyleClass().add("path-block");

            for (int j = 0; j < allNonTouched.get(i).size(); j++) {
                Text cycle = new Text("");
                cycle.getStyleClass().add("path-node");
                cycle.setFill(edgeColor);
                cycle.setCursor(Cursor.HAND);
                cycle.setText(cycle.getText() + (allNonTouched.get(i).get(j) + 1));
                container.getChildren().add(cycle);

                ArrayList<Edge> pathFinal = cyclesAfterPaths.get(pathNum).get(allNonTouched.get(i).get(j)).getPath();
                cycle.setOnMouseEntered(event -> {
                    for (Edge edge : pathFinal) {
                        edge.getLine().getLine().setStroke(pressColor);
                        edge.getLine().getArrow().setFill(pressColor);
                    }
                });

                cycle.setOnMouseExited(event -> {
                    for (Edge edge : pathFinal) {
                        edge.getLine().getLine().setStroke(edgeColor);
                        edge.getLine().getArrow().setFill(edgeColor);
                    }
                });

            }
            cycleTitlePane.getChildren().add(container);
        }
    }

    private void clearAll() {
        nonTouchedInserted = false;
        mainPane.getChildren().clear();
        mainPane.setPrefWidth(screenSize.getWidth());
        pathTitlePane.getChildren().clear();
        fromComboBox.getItems().clear();
        toComboBox.getItems().clear();
    }

    public void print() {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter =
                new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png");
        fileChooser.getExtensionFilters().add(extFilter);
        WritableImage image = mainPane.snapshot(new SnapshotParameters(), null);
        File file = fileChooser.showSaveDialog(stage);
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
        } catch (IOException e) {

        }
    }

    public void onTestInput() {
//        nodesNumText.setText("6");
//        onEnterPress();
//        input("1", "2", "a");
//        input("2", "3", "b");
//        input("3", "4", "c");
//        input("4", "5", "d");
//        input("5", "6", "e");
//        input("3", "5", "g");
//        input("5", "5", "f");
//        input("5", "4", "i");
//        input("5", "3", "h");
//        input("3", "2", "j");

//        test 2
//        nodesNumText.setText("4");
//        onEnterPress();
//        input("1", "2", "a");
//        input("2", "3", "b");
//        input("3", "4", "c");
//        input("2", "4", "d");
//        input("2", "2", "e");
//        input("4", "3", "f");
//        input("3", "2", "g");
//
//       test 3
//        nodesNumText.setText("8");
//        onEnterPress();
//        input("1", "2", "1");
//        input("2", "3", "G1");
//        input("3", "4", "G2");
//        input("4", "5", "G3");
//        input("5", "6", "G4");
//        input("6", "7", "G5");
//        input("7", "8", "G6");
//        input("4", "7", "G7");
//        input("6", "8", "G8");
//        input("6", "5", "-H4");
//        input("8", "6", "-H1");
//        input("7", "3", "-H2");
//        input("8", "2", "-H3");

        nodesNumText.setText("7");
        onEnterPress();
        input("1", "2", "1");
        input("2", "3", "1");
        input("3", "4", "1");
        input("4", "6", "1");
        input("5", "6", "1");
        input("6", "7", "1");
        input("2", "5", "1");

        input("4", "3", "-1");
        input("5", "5", "-1");
        input("6", "4", "-1");
        input("6", "2", "-1");

    }

    private void input(String source, String destination, String gain) {
        fromComboBox.setValue(source);
        toComboBox.setValue(destination);
        gainText.setText(gain);
        onInsertPress();
    }

}
