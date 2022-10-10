import javafx.scene.shape.Line;

public class SmartLine extends Line {

  private double angle;
  private double multi;

  SmartLine(double sX, double sY, double eX, double eY, double multiIn) {
    super(sX, sY, eX, eY);
    multi = multiIn;
    angle = findAngle(sX, sY, eX, eY);
  }

  SmartLine(double sX, double sY, double eX, double eY, double angleIn, double multiIn) {
    super(sX, sY, eX, eY);
    angle = angleIn;
    multi = multiIn;
    angle = findAngle(sX, sY, eX, eY);
  }

  private double findAngle(double sX, double sY, double eX, double eY) {
    eX -= sX;
    eY -= sY;
    double inRads = Math.atan2(-eY, eX);

    if (inRads < 0)
      inRads = -inRads;
    else
      inRads = 2 * Math.PI - inRads;
    if (inRads >= 2 * Math.PI) {
      inRads %= 2 * Math.PI;
    }
    return inRads;
  }

  public double getAngle() {
    return angle;
  }

  public void setAngle(double angleIn) {
    angle = angleIn;
  }

  public double getMulti() {
    return multi;
  }

  public void setMulti(double multiIn) {
    multi = multiIn;
  }

  public double getLength() {
    return Math.sqrt(Math.pow(getStartX() - getEndX(), 2) + Math.pow(getStartY() - getEndY(), 2));
  }
}