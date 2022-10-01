package fractal;

import javafx.scene.shape.Line;

public class SmartLine extends Line {

  private double length, angle;

  SmartLine(double sX, double sY, double eX, double eY, double angleIn) {
    super(sX, sY, eX, eY);
    angle = angleIn;
  }

  public double getAngle() {
    return angle;
  }

  public void setAngle(double angleIn) {
    angle = angleIn;
  }
}