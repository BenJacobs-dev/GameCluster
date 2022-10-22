package patternMaker;

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

public class PatternMaker extends Application {

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

  public static void main(String[] args) {
    Application.launch(args);
  }

  public void start(Stage primaryStage) {

    createColorList();

    stage = primaryStage;
    stage.setTitle("PatternMaker");

    Group nodeGroup = new Group();
    nodeList = nodeGroup.getChildren();

    Rectangle mapRect = new Rectangle(0, 0, displaySizeX, displaySizeY);

    WritableImage img = new WritableImage(displaySizeX, displaySizeY);
    imgWriter = img.getPixelWriter();
    mapRect.setFill(new ImagePattern(img));

    TextField valTextIn = new TextField(multi + "");
    valTextIn.setAlignment(Pos.CENTER);
    valTextIn.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        try {
          multi = Double.parseDouble(newValue);

        } catch (Exception e) {
          multi = 1;
        }
      }
    });

    TextField multiTextIn = new TextField(multiStep + "");
    multiTextIn.setAlignment(Pos.CENTER);
    multiTextIn.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        try {
          multiStep = Double.parseDouble(newValue);

        } catch (Exception e) {
          multiStep = 0.00001;
        }
      }
    });

    Slider valSliderIn = new Slider(0, 9, 5);
    valSliderIn.setMajorTickUnit(1);
    valSliderIn.setMinorTickCount(0);
    valSliderIn.setShowTickMarks(true);
    valSliderIn.setShowTickLabels(true);
    valSliderIn.setPrefWidth(250);
    valSliderIn.valueProperty().addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observableValue, Number wasChanging, Number changing) {
        try {
          multi = Double.parseDouble(valTextIn.getText());

        } catch (Exception e) {
          multi = 1;
        }
          multi += valSliderIn.getValue();
      }
    });

    curMultiVal = new Text("Multi: " + multi);

    VBox inputBox = new VBox(valTextIn, valSliderIn, curMultiVal, multiTextIn);
    inputBox.setAlignment(Pos.BOTTOM_LEFT);
    inputBox.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
    inputBox.setAlignment(Pos.CENTER);

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

    new Thread(task).start();

  }

  public void updateDisplay() {
    curMultiVal.setText("Multi: " + round(multi, 10));
    for (int i = 0, j, hsx = sizeX/2, hsy = sizeY/2; i < sizeX; i++) {
      for (j = 0; j < sizeY; j++) {
        setSquareColor((int) i, (int) j, colorList.get((int) (((Math.abs(i-hsx)+1) * (Math.abs(j-hsy)+1) * multi * multi) / 10 % modNum)));
        //setSquareColor((int) i, (int) j, colorList.get((int) (i * j * multi * multi) / 10 % modNum));
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
