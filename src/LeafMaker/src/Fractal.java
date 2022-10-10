import java.util.ArrayList;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Fractal extends Application {

  int displaySizeX = 1800, displaySizeY = 900;
  boolean spike = false;

  Group lineGroup;
  ObservableList<Node> lineList;
  ArrayList<SmartLine> topLines;
  double lineLength;
  Stage stage;
  VBox menuPanel;
  Slider leafThicknessSlider, lengthMultiSlider, angleSlider, lengthSlider, quadraticMultiSlider;
  CheckBox randomColorBox, inwardModeBox, oneLineBox, bothSidesBox, dragonCurveBox, angleInvertionBox, isSpikeBox,
      AddCenterBox;
  int dir;
  PixelWriter imgWriter;
  PixelReader imgReader;
  Rectangle mapRect;

  double[] distFrom = new double[1];

  public static void main(String[] args) {
    Application.launch(args);
  }

  public void start(Stage stageIn) {

    Button addLineButton = new Button();
    addLineButton.setOnAction(this::addLineLayer);
    addLineButton.setText("Add Line");

    Button resetButton = new Button();
    resetButton.setOnAction(this::reset);
    resetButton.setText("Reset");

    leafThicknessSlider = new Slider(0, 99, 60);
    leafThicknessSlider.setMajorTickUnit(10);
    leafThicknessSlider.setShowTickMarks(true);
    leafThicknessSlider.setShowTickLabels(true);
    leafThicknessSlider.setPrefWidth(500);
    leafThicknessSlider.valueChangingProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observableValue, Boolean wasChanging, Boolean changing) {
        reset(new ActionEvent());
        addLineLayer(new ActionEvent());
      }
    });

    lengthMultiSlider = new Slider(0, 99, 60);
    lengthMultiSlider.setMajorTickUnit(10);
    lengthMultiSlider.setShowTickMarks(true);
    lengthMultiSlider.setShowTickLabels(true);
    lengthMultiSlider.setPrefWidth(500);
    lengthMultiSlider.valueChangingProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observableValue, Boolean wasChanging, Boolean changing) {
        reset(new ActionEvent());
        addLineLayer(new ActionEvent());
      }
    });

    lengthSlider = new Slider(0, 800, 350);
    lengthSlider.setMajorTickUnit(100);
    lengthSlider.setMinorTickCount(3);
    lengthSlider.setShowTickMarks(true);
    lengthSlider.setShowTickLabels(true);
    lengthSlider.setPrefWidth(500);
    lengthSlider.valueChangingProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observableValue, Boolean wasChanging, Boolean changing) {
        reset(new ActionEvent());
        addLineLayer(new ActionEvent());
      }
    });

    angleSlider = new Slider(0, 180, 135);
    angleSlider.setMajorTickUnit(30);
    angleSlider.setMinorTickCount(1);
    angleSlider.setShowTickMarks(true);
    angleSlider.setShowTickLabels(true);
    angleSlider.setPrefWidth(500);
    angleSlider.valueChangingProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observableValue, Boolean wasChanging, Boolean changing) {
        reset(new ActionEvent());
        addLineLayer(new ActionEvent());
      }
    });

    quadraticMultiSlider = new Slider(0, 1, 1);
    quadraticMultiSlider.setMajorTickUnit(0.1);
    quadraticMultiSlider.setMinorTickCount(1);
    quadraticMultiSlider.setShowTickMarks(true);
    quadraticMultiSlider.setShowTickLabels(true);
    quadraticMultiSlider.setPrefWidth(500);
    quadraticMultiSlider.valueChangingProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observableValue, Boolean wasChanging, Boolean changing) {
        reset(new ActionEvent());
        addLineLayer(new ActionEvent());
      }
    });

    isSpikeBox = new CheckBox("Spike On");
    isSpikeBox.setOnAction(this::reset);
    randomColorBox = new CheckBox("Randomize Colors");
    inwardModeBox = new CheckBox("Inward Mode");
    inwardModeBox.setOnAction(this::changeDirectionMode);
    dragonCurveBox = new CheckBox("Dragon Curve Mode");
    dragonCurveBox.setVisible(false);
    angleInvertionBox = new CheckBox("Angle Invertion");
    angleInvertionBox.setVisible(false);
    oneLineBox = new CheckBox("One Line");
    oneLineBox.setOnAction(this::changeSingleLineCount);
    bothSidesBox = new CheckBox("Both Sides");
    bothSidesBox.setOnAction(this::reset);
    AddCenterBox = new CheckBox("Add Center Line");

    menuPanel = new VBox(addLineButton, resetButton, new Text("Leaf Thickness"), leafThicknessSlider,
        new Text("Length Multiplier"), lengthMultiSlider, new Text("Starting Length"), lengthSlider,
        new Text("Angle"), angleSlider, new Text("Quadratic Multiplier"), quadraticMultiSlider,
        isSpikeBox, randomColorBox, inwardModeBox, oneLineBox, bothSidesBox, dragonCurveBox, angleInvertionBox,
        AddCenterBox);
    menuPanel.setSpacing(10);
    menuPanel.setAlignment(Pos.CENTER);

    lineGroup = new Group();
    lineList = lineGroup.getChildren();
    topLines = new ArrayList<SmartLine>();

    WritableImage img = new WritableImage(displaySizeX, displaySizeY);
    imgWriter = img.getPixelWriter();
    imgReader = img.getPixelReader();

    mapRect = new Rectangle(0, 0, displaySizeX, displaySizeY);
    mapRect.setFill(new ImagePattern(img));

    stage = stageIn;
    stage.setTitle("Fractal");
    reset(new ActionEvent());

    stage.setScene(new Scene(lineGroup, displaySizeX, displaySizeY));
    stage.show();

  }

  public void addLineLayer(ActionEvent event) {
    SmartLine curLine;
    double angleChange = angleSlider.getValue() * Math.PI / 180;
    if (!inwardModeBox.isSelected()) {
      if (!oneLineBox.isSelected()) {
        boolean doOnce = true;
        ArrayList<SmartLine> tempList;
        while (lineList.size() <= 10 || doOnce) {
          tempList = new ArrayList<>();
          for (SmartLine mainLine : topLines) {
            double lineLength = mainLine.getLength() * mainLine.getMulti();
            curLine = new SmartLine(mainLine.getEndX(), mainLine.getEndY(),
                mainLine.getEndX() + (lineLength * Math.cos(mainLine.getAngle() + angleChange)),
                mainLine.getEndY() + (lineLength * Math.sin(mainLine.getAngle() + angleChange)),
                mainLine.getAngle() + angleChange, mainLine.getMulti() * quadraticMultiSlider.getValue());
            if (randomColorBox.isSelected()) {
              curLine.setStroke(new Color(Math.random(), Math.random(), Math.random(), 1));
            }
            tempList.add(curLine);
            lineList.add(curLine);
            curLine = new SmartLine(mainLine.getEndX(), mainLine.getEndY(),
                mainLine.getEndX() + (lineLength * Math.cos(mainLine.getAngle() - angleChange)),
                mainLine.getEndY() + (lineLength * Math.sin(mainLine.getAngle() - angleChange)),
                mainLine.getAngle() - angleChange, mainLine.getMulti() * quadraticMultiSlider.getValue());
            if (randomColorBox.isSelected()) {
              curLine.setStroke(new Color(Math.random(), Math.random(), Math.random(), 1));
            }
            tempList.add(curLine);
            lineList.add(curLine);
            if (AddCenterBox.isSelected()) {
              curLine = new SmartLine(mainLine.getEndX(), mainLine.getEndY(),
                  mainLine.getEndX() + (lineLength * Math.cos(mainLine.getAngle())),
                  mainLine.getEndY() + (lineLength * Math.sin(mainLine.getAngle())), mainLine.getAngle(),
                  mainLine.getMulti());
              if (randomColorBox.isSelected()) {
                curLine.setStroke(new Color(Math.random(), Math.random(), Math.random(), 1));
              }
              tempList.add(curLine);
              lineList.add(curLine);
            }
          }
          doOnce = false;
          topLines = tempList;
        }
      } else {
        SmartLine mainLine = topLines.get(0);
        for (int i = 0; i < 1000; i++) {
          double lineLength = mainLine.getLength() * mainLine.getMulti();
          curLine = new SmartLine(mainLine.getEndX(), mainLine.getEndY(),
              mainLine.getEndX() + (lineLength * Math.cos(mainLine.getAngle() + angleChange)),
              mainLine.getEndY() + (lineLength * Math.sin(mainLine.getAngle() + angleChange)),
              mainLine.getAngle() + angleChange, lengthMultiSlider.getValue() / 100.0);
          if (randomColorBox.isSelected()) {
            curLine.setStroke(new Color(Math.random(), Math.random(), Math.random(), 1));
          }
          lineList.add(curLine);
          mainLine = curLine;
        }
        topLines.clear();
        topLines.add(mainLine);
      }
    } else {
      boolean doOnce = true;
      ArrayList<SmartLine> tempList;
      while (/* lineList.size() <= 10000 || */ doOnce) {
        tempList = new ArrayList<>();
        lineList.remove(2, lineList.size());
        lineLength /= (2 * Math.cos(angleChange));
        for (SmartLine mainLine : topLines) {
          curLine = new SmartLine(mainLine.getStartX(), mainLine.getStartY(),
              mainLine.getStartX() + (lineLength * Math.cos(mainLine.getAngle() - dir * angleChange)),
              mainLine.getStartY() + (lineLength * Math.sin(mainLine.getAngle() - dir * angleChange)),
              mainLine.getAngle() - dir * angleChange);
          if (randomColorBox.isSelected()) {
            curLine.setStroke(new Color(Math.random(), Math.random(), Math.random(), 1));
          }
          tempList.add(curLine);
          lineList.add(curLine);
          curLine = new SmartLine(mainLine.getEndX() - (lineLength * Math.cos(mainLine.getAngle() + dir * angleChange)),
              mainLine.getEndY() - (lineLength * Math.sin(mainLine.getAngle() + dir * angleChange)), mainLine.getEndX(),
              mainLine.getEndY(), mainLine.getAngle() + dir * angleChange);
          if (randomColorBox.isSelected()) {
            curLine.setStroke(new Color(Math.random(), Math.random(), Math.random(), 1));
          }
          tempList.add(curLine);
          lineList.add(curLine);
          if (dragonCurveBox.isSelected()) {
            dir *= -1;
          }
        }
        if (angleInvertionBox.isSelected()) {
          dir *= -1;
        }
        doOnce = false;
        topLines = tempList;
      }
    }
    // setSquareColor();
    colorAll();
  }

  public void reset(ActionEvent event) {

    lineList.clear();
    lineList.add(mapRect);
    lineList.add(menuPanel);
    topLines.clear();
    dir = 1;

    lineLength = lengthSlider.getValue();
    SmartLine prevLine;
    if (!inwardModeBox.isSelected()) {
      if (bothSidesBox.isSelected()) {
        prevLine = new SmartLine(900 + lineLength / 2, 450, 900 - lineLength / 2, 450, Math.PI,
            lengthMultiSlider.getValue() / 100.0);
        lineList.add(prevLine);
        topLines.add(prevLine);
        prevLine = new SmartLine(900 - lineLength / 2, 450, 900 + lineLength / 2, 450, 0,
            lengthMultiSlider.getValue() / 100.0);
      } else {
        prevLine = new SmartLine(900, 800, 900, 800 - lineLength, 3 * Math.PI / 2,
            lengthMultiSlider.getValue() / 100.0);
      }
    } else {
      prevLine = new SmartLine(900 - lineLength / 2, 450, 900 + lineLength / 2, 450, 0,
          lengthMultiSlider.getValue() / 100.0);
    }

    // prevLine = new SmartLine(900, 800, 900, 800-lineLength, -Math.PI/2);

    lineList.add(prevLine);
    topLines.add(prevLine);
    // setSquareColor();

    for (int i = 0, j; i < displaySizeX; i++) {
      for (j = 0; j < displaySizeY; j++) {
        imgWriter.setColor(i, j, Color.WHITE);
      }
    }
    colorAll();
  }

  public boolean checkCollisionWithLine(double ax, double ay, double bx, double by, double angle, double cx, double cy,
      double r) {
    double dist = Math.sqrt(distFromLine(ax, ay, bx, by, cx, cy));
    if (spike) {
      if (dist == Math.sqrt(distSquared(ax, ay, cx, cy))) {
        angle = getAngle(ax, ay, cx, cy, angle);
        angle /= Math.PI / 2;
        r += r * angle * angle;
      } else if (dist == Math.sqrt(distSquared(bx, by, cx, cy))) {
        angle = getAngle(bx, by, cx, cy, angle);
        angle /= Math.PI / 2;
        r += r * angle * angle;

      }
    }
    return r - dist > 0 ? true : false;
  }

  public double getAngle(double x1, double y1, double x2, double y2, double angleIn) {
    x1 -= x2;
    y1 -= y2;

    double angle = Math.atan2(-y1, x1);

    // We need to map to coord system when 0 degree is at 3 O'clock, 270 at 12
    // O'clock
    if (angle < 0)
      angle = Math.abs(angle);
    else
      angle = 2 * Math.PI - angle;
    if (angle >= 2 * Math.PI) {
      angle %= 2 * Math.PI;
    }
    angle -= angleIn;
    angle = angle >= 0 ? angle : 2 * Math.PI + angle;
    if (angle > Math.PI) {
      angle -= Math.PI;
    }
    angle -= Math.PI / 2;
    return angle >= 0 ? angle : -angle;
  }

  double distSquared(double x1, double y1, double x2, double y2) {
    x1 -= x2;
    y1 -= y2;
    return x1 * x1 + y1 * y1;
  }

  double distFromLine(double lx1, double ly1, double lx2, double ly2, double px, double py) {
    double line_dist = distSquared(lx1, ly1, lx2, ly2);
    if (line_dist == 0)
      return distSquared(px, py, lx1, ly1);
    double t = ((px - lx1) * (lx2 - lx1) + (py - ly1) * (ly2 - ly1)) / line_dist;
    t = Math.max(0, Math.min(1, t));
    return distSquared(px, py, lx1 + t * (lx2 - lx1), ly1 + t * (ly2 - ly1));
  }

  public boolean isColliding(double x, double y) {
    double radius = leafThicknessSlider.getValue();
    for (int i = 2, size = lineList.size(); i < size; i++) {
      SmartLine line = (SmartLine) lineList.get(i);
      if (checkCollisionWithLine(line.getEndX(), line.getEndY(), line.getStartX(), line.getStartY(), line.getAngle(), x,
          y, radius)) {
        return true;
      }
    }
    return false;
  }

  public void setSquareColor() {
    for (int i = 0, j; i < displaySizeX; i++) {
      for (j = 0; j < displaySizeY; j++) {
        imgWriter.setColor(i, j, isColliding(i, j) ? Color.LIGHTGREEN : Color.WHITE);
      }
    }
  }

  public void colorAll() {
    if (leafThicknessSlider.getValue() == 0)
      return;
    for (int i = 2, size = lineList.size(); i < size; i++) {
      colorLine((SmartLine) lineList.get(i));
    }
  }

  public double getColor(double ax, double ay, double bx, double by, double angle, double cx, double cy, double r) {
    double dist = Math.sqrt(distFromLine(ax, ay, bx, by, cx, cy));
    if (isSpikeBox.isSelected()) {
      if (dist == Math.sqrt(distSquared(ax, ay, cx, cy))) {
        angle = getAngle(ax, ay, cx, cy, angle);
        angle /= Math.PI / 2;
        r += r * angle * angle;
      } else if (dist == Math.sqrt(distSquared(bx, by, cx, cy))) {
        angle = getAngle(bx, by, cx, cy, angle);
        angle /= Math.PI / 2;
        r += r * angle * angle;

      }
    }
    return Math.max(r - dist, 0) / r;
  }

  public void colorLine(SmartLine line) {
    int radius;
    if (line == lineList.get(2)) {
      radius = (int) leafThicknessSlider.getValue();
    } else {
      radius = (int) (line.getLength() * leafThicknessSlider.getValue() / 100);
    }
    int minX, minY, maxX, maxY;

    if (line.getEndX() > line.getStartX()) {
      maxX = Math.min(displaySizeX - 1, (int) line.getEndX()) + 2 * radius;
      minX = Math.max(0, (int) line.getStartX() - 2 * radius);
    } else {
      maxX = Math.min(displaySizeX - 1, (int) line.getStartX() + 2 * radius);
      minX = Math.max(0, (int) line.getEndX() - 2 * radius);
    }
    if (line.getEndY() > line.getStartY()) {
      maxY = Math.min(displaySizeY, (int) line.getEndY() + 2 * radius);
      minY = Math.max(0, (int) line.getStartY() - 2 * radius);
    } else {
      maxY = Math.min(displaySizeY, (int) line.getStartY() + 2 * radius);
      minY = Math.max(0, (int) line.getEndY() - 2 * radius);
    }

    for (int x = minX; x < maxX; x++) {
      for (int y = minY; y < maxY; y++) {
        double green = getColor(line.getEndX(), line.getEndY(), line.getStartX(), line.getStartY(), line.getAngle(), x,
            y, radius);
        if (x > 0 && x < displaySizeX - 1 && y > 0 && y < displaySizeY - 1 && green > 0) {
          Color col = imgReader.getColor(x, y);
          Color newCol = Color.LIGHTGREEN.interpolate(Color.GREEN, green);
          imgWriter.setColor(x, y,
              new Color(Math.min(newCol.getRed(), col.getRed()), 1, Math.min(newCol.getBlue(), col.getBlue()), 1));
        }
      }
    }
  }

  public void changeDirectionMode(ActionEvent event) {
    if (inwardModeBox.isSelected()) {
      oneLineBox.setVisible(false);
      oneLineBox.setSelected(false);
      bothSidesBox.setSelected(false);
      bothSidesBox.setVisible(false);
      dragonCurveBox.setVisible(true);
      angleInvertionBox.setVisible(true);
    } else {
      oneLineBox.setVisible(true);
      bothSidesBox.setVisible(true);
      dragonCurveBox.setVisible(false);
      dragonCurveBox.setSelected(false);
      angleInvertionBox.setVisible(false);
      angleInvertionBox.setSelected(false);
    }
    reset(new ActionEvent());
  }

  public void changeSingleLineCount(ActionEvent event) {
    if (oneLineBox.isSelected()) {
      bothSidesBox.setSelected(false);
      bothSidesBox.setVisible(false);
    } else {
      bothSidesBox.setVisible(true);
    }
    reset(new ActionEvent());
  }
}