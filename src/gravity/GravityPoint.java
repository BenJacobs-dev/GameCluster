import javafx.scene.input.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.effect.*;

public class GravityPoint extends Circle {

  public double x, y, mass;

  public GravityPoint(double x, double y, double mass, Color color) {
    super(x, y, mass);
    this.x = x;
    this.y = y;
    this.setMass(mass);
    this.setFill(color);
  }

  public void setMass(double mass) {
    this.mass = mass;
    setRadius(mass);
    DropShadow dropShadow = new DropShadow(BlurType.GAUSSIAN, Color.WHITE, mass * 1.125, 0, 0, 0);
    this.setEffect(dropShadow);
  }
}
