package randoFractals;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import javafx.application.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.*;
import javafx.concurrent.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.text.*;
import javafx.scene.layout.*;
import javafx.scene.input.*;
import javafx.scene.shape.*;
import javafx.scene.paint.*;
import javafx.scene.image.*;
import javafx.stage.*;

public class RandoFractals extends Application {

  ///////////////////////////////////////////////
  // Edit here //
  ///////////////////////////////////////////////

  int modNum = 17, screen = 1000, pixelSize = 5, blackStart = 10, fps = 1000;
  double xMulti = 1;

  ///////////////////////////////////////////////

  int size = screen / pixelSize, sizeX = (int) (size * xMulti), sizeY = size,
      mapSizeX = (int) (xMulti * screen / sizeX), mapSizeY = screen / sizeY, displaySizeX = sizeX * mapSizeX,
      displaySizeY = sizeY * mapSizeY, fpsMulti = 1000 / fps;

  Stage stage;
  Rectangle player;
  HashMap<KeyCode, Runnable> keyActions;
  LinkedList<KeyCode> keysPressed;
  ObservableList<Node> nodeList;
  HashMap<Integer, Color> colorList;
  PixelWriter imgWriter;
  final Semaphore semaphore = new Semaphore(1);
  final AtomicBoolean running = new AtomicBoolean(false);
  boolean playing;
  Text fpsCounterText;
  int ballAddCounter;
  double multi = 1, multiStep = 0.00001;
  Text curMultiVal;
  int dotCount = 30000;
  ArrayList<double[]> equationVals = new ArrayList<>();
  double totalRatio = 0;
  double scale = 100;

  public static void main(String[] args) {
    Application.launch(args);
  }

  public void start(Stage primaryStage) {

    equationVals.add(new double[]{10, 0.95, .07, 0.9, -.2});
    equationVals.add(new double[]{3, 0.5, .3, 0.5, 0.6});
    equationVals.add(new double[]{4, -1.1, .3, -1.1, 0.6});

    for(double [] vals : equationVals){
      totalRatio += vals[0];
    }

    createColorList();

    stage = primaryStage;
    stage.setTitle("PatternMaker");

    Group nodeGroup = new Group();
    nodeList = nodeGroup.getChildren();

    Rectangle mapRect = new Rectangle(0, 0, displaySizeX, displaySizeY);

    WritableImage img = new WritableImage(displaySizeX, displaySizeY);
    imgWriter = img.getPixelWriter();
    mapRect.setFill(new ImagePattern(img));

    curMultiVal = new Text("Multi: " + multi);

    VBox inputBox = new VBox();
    inputBox.setAlignment(Pos.BOTTOM_LEFT);
    inputBox.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
    inputBox.setAlignment(Pos.CENTER);

    updateInputBox(inputBox);

    nodeList.add(mapRect);
    nodeList.add(inputBox);

    Scene scene = new Scene(nodeGroup, displaySizeX, displaySizeY);

    stage.setScene(scene);
    stage.show();

    Task<Void> task = new Task<Void>() {

      long time;

      @Override
      protected Void call() throws Exception {
        try {
          time = System.currentTimeMillis();
          running.set(true);
          while (running.get()) {
            semaphore.acquire();
            Thread.sleep(Math.max(fpsMulti + time - System.currentTimeMillis(), 0));
            time = System.currentTimeMillis();

            Platform.runLater(() -> {
              multi += multiStep;
              updateDisplay();
              semaphore.release();
            });
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
        return null;
      }
    };

    //new Thread(task).start();

  }

  public void updateInputBox(VBox inputBox){
    inputBox.getChildren().clear();
    Button drawButton = new Button("Draw");
    drawButton.setOnAction(e -> {
      resetDisplay();
      updateDisplay();
    });
    inputBox.getChildren().add(drawButton);
    TextField countField = new TextField(dotCount + "");
    countField.setOnKeyReleased(e -> {
      try{
        dotCount = Integer.parseInt(countField.getText());
      }
      catch(Exception ex){}
      resetDisplay();
      updateDisplay();
    });
    HBox countBox = new HBox(new Text("Dot Count: "), countField);
    countBox.setAlignment(Pos.CENTER);
    countBox.setSpacing(10);
    countBox.setPadding(new Insets(10, 10, 10, 10));
    inputBox.getChildren().add(countBox);
    TextField scaleField = new TextField(scale + "");
    scaleField.setOnKeyReleased(e -> {
      try{
        scale = Double.parseDouble(scaleField.getText());
      }
      catch(Exception ex){}
      resetDisplay();
      updateDisplay();
    });
    HBox scaleBox = new HBox(new Text("Scale: "), scaleField);
    scaleBox.setAlignment(Pos.CENTER);
    scaleBox.setSpacing(10);
    scaleBox.setPadding(new Insets(10, 10, 10, 10));
    inputBox.getChildren().add(scaleBox);
    for(double[] vals : equationVals){
      inputBox.getChildren().add(makeEquationBox(vals, inputBox));
    }
    Button addButton = new Button("+");
    addButton.setOnAction(e -> {
      equationVals.add(new double[]{1, 0, 0, 0, 0});
      totalRatio += 1;
      updateInputBox(inputBox);
    });
    inputBox.getChildren().add(addButton);
  }

  public HBox makeEquationBox(double[] vals, VBox inputBox){

    TextField ratioField = new TextField(vals[0] + "");
    ratioField.setOnKeyReleased(e -> {
      try{
        totalRatio -= vals[0];
        vals[0] = Double.parseDouble(ratioField.getText());
        totalRatio += vals[0];
      }
      catch(Exception ex){}
      resetDisplay();
      updateDisplay();
    });
    ratioField.setMaxWidth(35);
    TextField xField = new TextField(vals[1] + "");
    xField.setOnKeyReleased(e -> {
      try{
        vals[1] = Double.parseDouble(xField.getText());
      }
      catch(Exception ex){}
      resetDisplay();
      updateDisplay();
    });
    xField.setMaxWidth(35);
    TextField yField = new TextField(vals[3] + "");
    yField.setOnKeyReleased(e -> {
      try{
        vals[3] = Double.parseDouble(yField.getText());
      }
      catch(Exception ex){}
      resetDisplay();
      updateDisplay();
    });
    yField.setMaxWidth(35);
    TextField xAddField = new TextField(vals[2] + "");
    xAddField.setOnKeyReleased(e -> {
      try{
        vals[2] = Double.parseDouble(xAddField.getText());
      }
      catch(Exception ex){}
      resetDisplay();
      updateDisplay();
    });
    xAddField.setMaxWidth(35);
    TextField yAddField = new TextField(vals[4] + "");
    yAddField.setOnKeyReleased(e -> {
      try{
        vals[4] = Double.parseDouble(yAddField.getText());
      }
      catch(Exception ex){}
      resetDisplay();
      updateDisplay();
    });
    yAddField.setMaxWidth(35);

    Button deleteButton = new Button("X");
    deleteButton.setOnAction(e -> {
      totalRatio -= vals[0];
      equationVals.remove(vals);
      resetDisplay();
      updateDisplay();
      updateInputBox(inputBox);
    });
    
    HBox box = new HBox(new Text("Ratio: "), ratioField, new Text("X: "), xField, new Text("XAdd: "), xAddField, new Text("Y: "), yField, new Text("YAdd: "), yAddField, deleteButton);
    box.setAlignment(Pos.CENTER);
    box.setSpacing(10);
    box.setPadding(new Insets(10, 10, 10, 10));
    return box;
  }

  public void resetDisplay(){
    for(int x = 0; x < displaySizeX; x++){
      for(int y = 0; y < displaySizeY; y++){
        imgWriter.setColor(x, y, Color.WHITE);
      }
    }
  }

  public void moveRandom(double[] pos){
    double rand = Math.random()*totalRatio;
    for(int i = 0, total = 0; i < equationVals.size(); i++){
      if(rand < equationVals.get(i)[0] + total){
        pos[0] = pos[0] * equationVals.get(i)[1] + equationVals.get(i)[2];
        pos[1] = pos[1] * equationVals.get(i)[3] + equationVals.get(i)[4];
        break;
      }
      total += equationVals.get(i)[0];
    }
  }

  public void updateDisplay() {
    curMultiVal.setText("Multi: " + round(multi, 10));
    double[] pos = new double[2];
    for (int i = 0; i < dotCount; i++) {
      moveRandom(pos);
      int x = (int) (pos[0]*scale + displaySizeX*0.5);
      int y = (int) (pos[1]*scale + displaySizeY*0.5);
      if (x >= 0 && x < displaySizeX && y >= 0 && y < displaySizeY){
        imgWriter.setColor(x, y, Color.BLACK);
      }
    }
  }

  public void setSquareColor(int x, int y, Color color) {
    for (int i = 0, j, xMap = x * mapSizeX, yMap = y * mapSizeY; i < mapSizeX; i++) {
      for (j = 0; j < mapSizeY; j++) {
        // if(i == 0 || j == 0 || i == mapSizeX-1 || j == mapSizeY-1) {
        // imgWriter.setColor(xMap+i, yMap+j, colorList.get(0));
        // }
        // else {
        imgWriter.setColor(xMap + i, yMap + j, color);
        // }
      }
    }
  }

  public void createColorList() {
    colorList = new HashMap<>();
    colorList.put(0, Color.BLACK);
    colorList.put(1, Color.RED);
    colorList.put(2, Color.ORANGE);
    colorList.put(3, Color.YELLOW);
    colorList.put(4, Color.GREEN);
    colorList.put(5, Color.BLUE);
    colorList.put(6, Color.PURPLE);
    for (blackStart = Math.min(blackStart, 7); blackStart < modNum; blackStart++) {
      colorList.put(blackStart, Color.BLACK);
    }
  }

  public double round(double value, int places) {
    if (places < 0)
      throw new IllegalArgumentException();

    long factor = (long) Math.pow(10, places);
    value = value * factor;
    long tmp = Math.round(value);
    return (double) tmp / factor;
  }

  @Override
  public void stop() {
    System.out.println("Stop");
    running.set(false);
    semaphore.release(100);
  }
}
