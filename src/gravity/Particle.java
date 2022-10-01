import javafx.scene.paint.*;

public class Particle {

  public double x, y, mass;
  public Color color;
  public double vx, vy;

  public Particle(double x, double y, double mass) {
    this.x = x;
    this.y = y;
    this.mass = mass;
    this.color = Color.BLACK;
    this.vx = 0;
    this.vy = 0;
  }
}
