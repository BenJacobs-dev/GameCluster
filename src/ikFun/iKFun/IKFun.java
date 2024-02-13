package iKFun;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.stage.Stage;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.layout.*;
import javafx.scene.input.*;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.collections.*;
import javafx.concurrent.Task;

public class IKFun extends Application {

  Stage stage;
  ArrayList<Line> lineList;
  ArrayList<Double> lineLengthsList;
  ObservableList<Node> nodeList;
  Circle start, end;
  List<Node> constantsGroup;
  int sideNum = 0;
  boolean setAnimation = false, animateStart = false, animateEnd = false;
  final double HP = Math.PI / 2, TP = HP * 3;
  final Semaphore semaphore = new Semaphore(1);
  final AtomicBoolean running = new AtomicBoolean(false);
  double[] sAnimationPos, eAnimationPos;
  TextField gravityTextField;
  double gravity = 10;

  int fps = 60;

  public static void main(String[] args) {
    Application.launch(args);
  }

  public void start(Stage stageIn) {

    Button addAnimationButton = new Button("Add Animation");

    start = new Circle(100, 500, 10, Color.BLACK);
    start.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
      start.setCenterX(event.getSceneX());
      start.setCenterY(event.getSceneY());
      // doIK(true, event.getSceneX(), event.getSceneY());
    });
    start.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
      if (setAnimation) {
        addAnimationButton.setText("Click Animation End");
        sideNum = 1;
      }
    });

    end = new Circle(900, 500, 10, Color.BLACK);
    end.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
      end.setCenterX(event.getSceneX());
      end.setCenterY(event.getSceneY());
      // doIK(false, event.getSceneX(), event.getSceneY());
    });
    end.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
      if (setAnimation) {
        sideNum = 2;
      }
    });

    Button splitLineButton = new Button("Split Line");
    splitLineButton.setOnAction(event -> {
      int index = lineList.size() - 1;
      double indexLength = lineLengthsList.get(index), tempLength;
      for (int i = index - 1; i >= 0; i--) {
        tempLength = lineLengthsList.get(i);
        if (indexLength < tempLength) {
          index = i;
          indexLength = tempLength;
        }
      }
      Line removedLine = lineList.get(index);
      double xTemp = (removedLine.getEndX() - removedLine.getStartX()) * .5 + removedLine.getStartX();
      double yTemp = (removedLine.getEndY() - removedLine.getStartY()) * .5 + removedLine.getStartY();
      lineList.add(index + 1, new Line(xTemp, yTemp, removedLine.getEndX(), removedLine.getEndY()));
      removedLine.setEndX(xTemp);
      removedLine.setEndY(yTemp);
      lineLengthsList.set(index, getLength(removedLine));
      lineLengthsList.add(index, getLength(lineList.get(index + 1)));
      nodeList.clear();
      nodeList.addAll(lineList);
      nodeList.addAll(constantsGroup);
    });

    Button removeLineButton = new Button("Remove Line");
    removeLineButton.setOnAction(event -> {
      if (lineList.size() != 1) {
        int index = lineList.size() - 1;
        double indexLength = lineLengthsList.get(index), tempLength;
        for (int i = index - 1; i >= 0; i--) {
          tempLength = lineLengthsList.get(i);
          if (indexLength > tempLength) {
            index = i;
            indexLength = tempLength;
          }
        }
        if (index == lineList.size() - 1)
          index--;
        Line keptLine = lineList.get(index), removedLine = lineList.get(index + 1);
        keptLine.setStartX(removedLine.getStartX());
        keptLine.setStartY(removedLine.getStartY());
        lineLengthsList.set(index, getLength(keptLine));
        lineList.remove(index + 1);
        lineLengthsList.remove(index + 1);
        nodeList.clear();
        nodeList.addAll(lineList);
        nodeList.addAll(constantsGroup);
      }
    });

    Button addLineButton = new Button("Add Line");
    addLineButton.setOnAction(event -> {
      lineList.add(new Line(50, 50, 100, 50));
      lineLengthsList.add(getLength(lineList.get(lineList.size() - 1)));
      nodeList.clear();
      nodeList.addAll(lineList);
      nodeList.addAll(constantsGroup);
    });

    Button smoothLineButton = new Button("Smooth Line");
    smoothLineButton.setOnAction(event -> {
      for (int i = 250 - lineList.size(); i >= 0; i--) {
        splitLineButton.fire();
      }
    });

    Button resetLineButton = new Button("Reset Line");
    resetLineButton.setOnAction(event -> {
      lineList.clear();
      lineLengthsList.clear();
      lineList.add(new Line(start.getCenterX(), start.getCenterY(), end.getCenterX(), end.getCenterY()));
      lineLengthsList.add(getLength(lineList.get(0)));
      nodeList.clear();
      nodeList.addAll(lineList);
      nodeList.addAll(constantsGroup);
    });

    Button centerXLineButton = new Button("500, 500 End");
    centerXLineButton.setOnAction(event -> {
      end.setCenterX(500);
      end.setCenterY(500);
      doIK(false, 500, 500);
    });

    addAnimationButton.setOnAction(event -> {
      addAnimationButton.setText("Click Animating Side");
      setAnimation = true;
    });

    Button removeAnimationButton = new Button("Remove Animation");
    removeAnimationButton.setOnAction(event -> {
      animateStart = false;
      animateEnd = false;
    });
    gravityTextField = new TextField();
    gravityTextField.setText("10");
    gravityTextField.textProperty().addListener(new ChangeListener<String>() {
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        gravity = getDouble(newValue);
      }
    });

    Group nodeGroup = new Group();
    nodeList = nodeGroup.getChildren();
    lineList = new ArrayList<Line>();
    lineLengthsList = new ArrayList<Double>();

    lineList.add(new Line(start.getCenterX(), start.getCenterY(), end.getCenterX(), end.getCenterY()));
    lineLengthsList.add(getLength(lineList.get(0)));

    VBox buttonsBox = new VBox(addLineButton, splitLineButton, removeLineButton, smoothLineButton, resetLineButton,
        centerXLineButton, addAnimationButton, removeAnimationButton, gravityTextField);

    constantsGroup = new ArrayList<>();
    constantsGroup.add(start);
    constantsGroup.add(end);
    constantsGroup.add(buttonsBox);

    nodeList.addAll(lineList);
    nodeList.addAll(constantsGroup);

    stage = stageIn;
    stage.setTitle("IK Fun");

    Scene scene = new Scene(nodeGroup, 1500, 1000);
    scene.addEventFilter(MouseEvent.MOUSE_CLICKED, event -> {
      if (sideNum != 0) {
        addAnimationButton.setText("Add Animation");
        double animationTimes = 60; // Keep as whole number
        if (sideNum == 1) {
          sAnimationPos = new double[4];
          sAnimationPos[0] = (start.getCenterX() - event.getSceneX()) / animationTimes;
          sAnimationPos[1] = (start.getCenterY() - event.getSceneY()) / animationTimes;
          sAnimationPos[2] = 0;
          sAnimationPos[3] = animationTimes;
          animateStart = true;
        } else {
          animationTimes *= 10;
          eAnimationPos = new double[4];
          eAnimationPos[0] = (end.getCenterX() - event.getSceneX()) / animationTimes;
          eAnimationPos[1] = (end.getCenterY() - event.getSceneY()) / animationTimes;
          eAnimationPos[2] = 0;
          eAnimationPos[3] = animationTimes;
          animateEnd = true;
        }
        sideNum = 0;
        setAnimation = false;
      }
    });

    stage.setScene(scene);
    stage.show();

    Task<Void> updateIK = new Task<Void>() {

      long time;
      boolean isStart;
      int counter;

      @Override
      protected Void call() throws Exception {
        try {
          fps = 1000 / fps;
          time = System.currentTimeMillis();
          isStart = true;
          counter = 0;
          running.set(true);
          while (running.get()) {
            semaphore.acquire(1);
            Thread.sleep(Math.max(fps + time - System.currentTimeMillis(), 0));
            time = System.currentTimeMillis();
            Platform.runLater(() -> {
              doAnimation(counter++);
              doIK(isStart, 0, 0);
            });
            // isStart = !isStart;
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
        return null;
      }
    };

    new Thread(updateIK).start();

  }

  public void doIK(boolean isStart, double mX, double mY) {
    int size = lineList.size();
    double sX = isStart ? start.getCenterX() : end.getCenterX(), sY = isStart ? start.getCenterY() : end.getCenterY();
    for (int runs = 10; runs >= 0; runs--) { // I originally thought i would have to loop a few times, but since it
                                             // recalculates every frame, or pixel moved, theres not enough change to
                                             // see
      if (isStart) {
        sX = start.getCenterX();
        sY = start.getCenterY();
        for (int i = size - 1; i >= 0; i--) {
          lineList.get(i).setStartX(sX);
          lineList.get(i).setStartY(sY);
          setLineAnchorStart(i, sX, sY);
          sX = lineList.get(i).getEndX();
          sY = lineList.get(i).getEndY();
        }
      } else {
        sX = end.getCenterX();
        sY = end.getCenterY();
        for (int i = 0; i < size; i++) {
          lineList.get(i).setEndX(sX);
          lineList.get(i).setEndY(sY);
          setLineAnchorEnd(i, sX, sY);
          sX = lineList.get(i).getStartX();
          sY = lineList.get(i).getStartY();
        }
      }
      isStart = !isStart;
    }
    lineList.get(0).setEndX(end.getCenterX());
    lineList.get(0).setEndY(end.getCenterY());
    semaphore.release();
  }

  public void doAnimation(int counter) {
    if (animateStart && sAnimationPos != null) {
      if (sAnimationPos[2] > 0) {
        start.setCenterX(start.getCenterX() - sAnimationPos[0]);
        start.setCenterY(start.getCenterY() - sAnimationPos[1]);
        if (sAnimationPos[2] >= sAnimationPos[3]) {
          sAnimationPos[2] -= sAnimationPos[3] * 2;
        }
      } else {
        start.setCenterX(start.getCenterX() + sAnimationPos[0]);
        start.setCenterY(start.getCenterY() + sAnimationPos[1]);
      }
      sAnimationPos[2] += 1;
    }
    if (animateEnd && eAnimationPos != null) {
      if (eAnimationPos[2] > 0) {
        end.setCenterX(end.getCenterX() - eAnimationPos[0]);
        end.setCenterY(end.getCenterY() - eAnimationPos[1]);
        if (eAnimationPos[2] >= eAnimationPos[3]) {
          eAnimationPos[2] -= eAnimationPos[3] * 2;
        }
      } else {
        end.setCenterX(end.getCenterX() + eAnimationPos[0]);
        end.setCenterY(end.getCenterY() + eAnimationPos[1]);
      }
      eAnimationPos[2] += 1;
    }
  }

  public double getDouble(String text) {
    try {
      return Double.parseDouble(text);
    } catch (Exception e) {
      return 0;
    }
  }

  public double getLength(Line line) {
    double x = line.getStartX() - line.getEndX();
    double y = line.getStartY() - line.getEndY();
    return Math.sqrt(x * x + y * y);
  }

  public double getDist(Line before, Line after) {
    double x = after.getStartX() - before.getEndX();
    double y = after.getStartY() - before.getEndY();
    return Math.sqrt(x * x + y * y);
  }

  public double getAngleAnchorStart(Line line) {
    double x = line.getStartX() - line.getEndX();
    double y = line.getStartY() - line.getEndY() - gravity;
    return x < 0 ? TP + Math.atan(y / x) : Math.atan(y / x) + HP;
  }

  public double getAngleAnchorEnd(Line line) {
    double x = -(line.getStartX() - line.getEndX());
    double y = -(line.getStartY() - line.getEndY() - gravity);
    return x <= 0 ? TP + Math.atan(y / x) : Math.atan(y / x) + HP;
  }

  public double getAngle(double xLine, double yLine, double xPref, double yPref) {
    return 0;
  }

  public double getRelX(double length, double angle) {
    return Math.cos(angle + HP) * length;
  }

  public double getRelY(double length, double angle) {
    return Math.sin(angle + HP) * length;
  }

  public void setLineAnchorStart(int lineIndex, double xPref, double yPref) {
    double angle = getAngleAnchorStart(lineList.get(lineIndex));
    lineList.get(lineIndex)
        .setEndX(lineList.get(lineIndex).getStartX() + getRelX(lineLengthsList.get(lineIndex), angle));
    lineList.get(lineIndex)
        .setEndY(lineList.get(lineIndex).getStartY() + getRelY(lineLengthsList.get(lineIndex), angle));
  }

  public void setLineAnchorEnd(int lineIndex, double xPref, double yPref) {
    double angle = getAngleAnchorEnd(lineList.get(lineIndex));
    lineList.get(lineIndex)
        .setStartX(lineList.get(lineIndex).getEndX() + getRelX(lineLengthsList.get(lineIndex), angle));
    lineList.get(lineIndex)
        .setStartY(lineList.get(lineIndex).getEndY() + getRelY(lineLengthsList.get(lineIndex), angle));
  }

  public void stop() {
    running.set(false);
    semaphore.release(100);
  }
}
