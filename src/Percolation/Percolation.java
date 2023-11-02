import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import javafx.application.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.*;
import javafx.concurrent.*;
import javafx.event.*;
import javafx.geometry.Insets;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.*;
import javafx.scene.paint.*;
import javafx.scene.image.*;
import javafx.stage.*;

public class Percolation extends Application {

  ///////////////////////////////////////////////
  // Edit here //
  ///////////////////////////////////////////////

  int screen = 500, pixelSize = 2, fps = 1000 / 1000, particleCount = 50000;
  int screenWidth = 1600, screenHeight = 900;

  ///////////////////////////////////////////////

  Stage stage;
  ObservableList<Node> nodeList;
  PixelWriter imgWriter;
  final Semaphore semaphore = new Semaphore(1);
  final AtomicBoolean running = new AtomicBoolean(false);

  Slider percolationSlider;
  TextField lowerBoundField, upperBoundField, stepField;
  CheckBox autoStepCheckBox, increasingButton;
  Button stepButton;

  double connections[][];
  int grid[][];
  ArrayList<Color> colors = new ArrayList<Color>();

  double percolation = 0.5;

  public static void main(String[] args) {
    Application.launch(args);
  }

  public void start(Stage primaryStage) {

    stage = primaryStage;
    stage.setTitle("Percolation");

    restart();

    Group nodeGroup = new Group();
    nodeList = nodeGroup.getChildren();

    Rectangle mapRect = new Rectangle(0, 0, screenWidth, screenHeight);

    WritableImage img = new WritableImage(screenWidth, screenHeight);
    imgWriter = img.getPixelWriter();
    mapRect.setFill(new ImagePattern(img));

    Button restartButton = new Button("Restart");
    restartButton.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        colors.clear();
        restart();
        update();
        draw();
      }
    });

    percolationSlider = new Slider(0, 1, percolation);
    percolationSlider.setShowTickLabels(true);
    percolationSlider.setShowTickMarks(true);
    percolationSlider.setMajorTickUnit(0.1);
    percolationSlider.setMinorTickCount(10);
    percolationSlider.setBlockIncrement(0.01);
    percolationSlider.valueProperty().addListener(new ChangeListener<Number>() {
      public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
        percolation = new_val.doubleValue();
        update();
        draw();
      }
    });

    lowerBoundField = new TextField();
    lowerBoundField.setText("0");
    lowerBoundField.textProperty().addListener(new ChangeListener<String>() {
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        double val = getDouble(newValue);
        if (val <= percolationSlider.getMax()) {
          percolationSlider.setMin(Math.max(val, 0));
        }
      }
    });
    upperBoundField = new TextField();
    upperBoundField.setText("1");
    upperBoundField.textProperty().addListener(new ChangeListener<String>() {
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        double val = getDouble(newValue);
        if (val >= percolationSlider.getMin()) {
          percolationSlider.setMax(Math.min(val, 1));
        }
      }
    });
    stepField = new TextField();
    stepField.setText("0.01");
    stepField.textProperty().addListener(new ChangeListener<String>() {
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        percolationSlider.setBlockIncrement(getDouble(newValue));
      }
    });
    stepButton = new Button("Step");
    stepButton.setOnAction(this::step);
    autoStepCheckBox = new CheckBox("Auto Step");
    increasingButton = new CheckBox("Decreasing");
    increasingButton.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        if (increasingButton.isSelected()) {
          increasingButton.setText("Increasing");
        } else {
          increasingButton.setText("Decreasing");
        }
      }
    });
    increasingButton.fire();

    VBox vbox = new VBox(restartButton, percolationSlider, lowerBoundField, upperBoundField, stepField, stepButton,
        autoStepCheckBox, increasingButton);
    vbox.setSpacing(10);
    vbox.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
    vbox.setPadding(new Insets(10, 10, 10, 10));

    nodeList.add(mapRect);
    nodeList.add(vbox);

    Scene scene = new Scene(nodeGroup, screenWidth, screenHeight);

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
            Thread.sleep(Math.max(fps + time - System.currentTimeMillis(), 0));
            time = System.currentTimeMillis();

            Platform.runLater(() -> {
              if (autoStepCheckBox.isSelected()) {
                step(null);
              }
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

    restart();
    update();
    draw();
  }

  public void clearScreen() {
    for (int x = 0; x < screenWidth; x++) {
      for (int y = 0; y < screenHeight; y++) {
        imgWriter.setColor(x, y, Color.BLACK);
      }
    }
  }

  public void step(ActionEvent event) {
    double cur = percolationSlider.getValue();
    if (cur >= percolationSlider.getMax() || cur <= percolationSlider.getMin()) {
      increasingButton.fire();
    }
    if (increasingButton.isSelected()) {
      percolationSlider.increment();
    } else {
      percolationSlider.decrement();
    }
  }

  public double getDouble(String text) {
    try {
      return Double.parseDouble(text);
    } catch (Exception e) {
      return 0;
    }
  }

  public void restart() {
    if (connections == null) {
      connections = new double[screenWidth / pixelSize][screenHeight / pixelSize];
      grid = new int[screenWidth / pixelSize][screenHeight / pixelSize];
      screenWidth = screenWidth - screenWidth % pixelSize;
      screenHeight = screenHeight - screenHeight % pixelSize;
    }
    for (int x = 0, xSize = screenWidth / pixelSize, ySize = screenHeight / pixelSize; x < xSize; x++) {
      for (int y = 0; y < ySize; y++) {
        connections[x][y] = Math.random();
        grid[x][y] = 0;
      }
    }
  }

  public void resetGrid() {
    for (int x = 0, xSize = screenWidth / pixelSize, ySize = screenHeight / pixelSize; x < xSize; x++) {
      for (int y = 0; y < ySize; y++) {
        grid[x][y] = 0;
      }
    }
  }

  public void update() {
    for (int x = 0, xSize = screenWidth / pixelSize, ySize = screenHeight / pixelSize, iter = 1; x < xSize; x++) {
      for (int y = 0; y < ySize; y++) {
        if (grid[x][y] == 0) {
          floodFill(x, y, iter++);
          if (iter > colors.size()) {
            colors.add(Color.hsb(Math.random() * 360, 1, 1));
          }
        }
      }
    }
  }

  public void floodFill(int x, int y, int iter) {
    Stack<Integer> stackX = new Stack<Integer>();
    Stack<Integer> stackY = new Stack<Integer>();
    stackX.push(x);
    stackY.push(y);
    while (!stackX.isEmpty()) {
      x = stackX.pop();
      y = stackY.pop();
      if (grid[x][y] == 0) {
        grid[x][y] = iter;
        if (inGrid(x + 1, y) && connections[x + 1][y] < percolation) {
          stackX.push(x + 1);
          stackY.push(y);
        }
        if (inGrid(x - 1, y) && connections[x - 1][y] < percolation) {
          stackX.push(x - 1);
          stackY.push(y);
        }
        if (inGrid(x, y + 1) && connections[x][y + 1] < percolation) {
          stackX.push(x);
          stackY.push(y + 1);
        }
        if (inGrid(x, y - 1) && connections[x][y - 1] < percolation) {
          stackX.push(x);
          stackY.push(y - 1);
        }
      }
    }

  }

  public void draw() {
    for (int x = 0, xSize = screenWidth / pixelSize, ySize = screenHeight / pixelSize; x < xSize; x++) {
      for (int y = 0; y < ySize; y++) {
        if (grid[x][y] != 0) {
          drawPixel(x, y, colors.get(grid[x][y] - 1));
          grid[x][y] = 0;
        }
      }
    }
  }

  public void drawPixel(int x, int y, Color color) {
    for (int i = 0; i < pixelSize; i++) {
      for (int j = 0; j < pixelSize; j++) {
        int xPos = x * pixelSize + i;
        int yPos = y * pixelSize + j;
        if (onScreen(xPos, yPos)) {
          imgWriter.setColor(xPos, yPos, color);
        }
      }
    }
  }

  public boolean onScreen(int x, int y) {
    return x >= 0 && x < screenWidth && y >= 0 && y < screenHeight;
  }

  public boolean inGrid(int x, int y) {
    return x >= 0 && x < screenWidth / pixelSize && y >= 0 && y < screenHeight / pixelSize;
  }

  @Override
  public void stop() {
    System.out.println("Stop");
    running.set(false);
    semaphore.release(100);
  }
}
