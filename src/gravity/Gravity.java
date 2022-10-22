import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import javafx.application.*;
import javafx.collections.*;
import javafx.concurrent.*;
import javafx.scene.*;
import javafx.scene.input.*;
import javafx.scene.shape.*;
import javafx.scene.paint.*;
import javafx.scene.image.*;
import javafx.stage.*;

public class Gravity extends Application {

  ///////////////////////////////////////////////
  // Edit here //
  ///////////////////////////////////////////////

  int screen = 500, pixelSize = 1, fps = 1000 / 1000, particleCount = 50000;
  int screenWidth = 1600, screenHeight = 900;
  double friction = 0.95, forceMulti = 2, jitter = 0.5;
  int generationMode = 1; // 0: random, 1: circle, 2: outside
  boolean blackHoleMode = false, bhmStopVelocity = true;

  ///////////////////////////////////////////////

  Stage stage;
  HashMap<KeyCode, Runnable> keyActions;
  LinkedList<KeyCode> keysPressed;
  ObservableList<Node> nodeList;
  PixelWriter imgWriter;
  final Semaphore semaphore = new Semaphore(1);
  final AtomicBoolean running = new AtomicBoolean(false);
  boolean playing;
  ArrayList<GravityPoint> points;
  Particle[] particles;

  GravityPoint curPoint;

  double mouseX = 0, mouseY = 0;

  public static void main(String[] args) {
    Application.launch(args);
  }

  public void start(Stage primaryStage) {

    stage = primaryStage;
    stage.setTitle("Gravity");

    points = new ArrayList<GravityPoint>();

    restart();

    Group nodeGroup = new Group();
    nodeList = nodeGroup.getChildren();

    Rectangle mapRect = new Rectangle(0, 0, screenWidth, screenHeight);

    WritableImage img = new WritableImage(screenWidth, screenHeight);
    imgWriter = img.getPixelWriter();
    mapRect.setFill(new ImagePattern(img));

    nodeList.add(mapRect);

    Scene scene = new Scene(nodeGroup, screenWidth, screenHeight);

    scene.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
      boolean found = false;
      for (GravityPoint point : points) {
        double centerX = point.getCenterX();
        double centerY = point.getCenterY();
        double x = event.getX();
        double y = event.getY();
        double radius = Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2));
        if (radius < point.getRadius()) {
          curPoint = point;
          found = true;
          break;
        }
      }
      if (!found) {
        curPoint = new GravityPoint(event.getX(), event.getY(), 30, Color.BLACK);
        points.add(curPoint);
        nodeList.add(curPoint);
      }
      curPoint.setFill(new Color(0, 0, 0, 0.3875));
    });
    scene.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
      if (event.getButton() == MouseButton.PRIMARY) {
        curPoint.setCenterX(event.getX());
        curPoint.setCenterY(event.getY());
        curPoint.x = event.getX();
        curPoint.y = event.getY();
      }
      if (event.getButton() == MouseButton.SECONDARY) {
        double centerX = curPoint.getCenterX();
        double centerY = curPoint.getCenterY();
        double x = event.getX();
        double y = event.getY();
        double radius = Math.sqrt(Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2));
        curPoint.setMass(radius);
      }
    });
    scene.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
      if (event.getButton() == MouseButton.PRIMARY) {
        points.remove(curPoint);
        nodeList.remove(curPoint);
      }
      curPoint.setFill(Color.BLACK);
    });
    scene.addEventFilter(KeyEvent.KEY_TYPED, event -> {
      if (event.getCharacter().equals("r")) {
        restart();
      }
    });

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
              clearScreen();
              update();
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

  public void clearScreen() {
    for (int x = 0; x < screenWidth; x++) {
      for (int y = 0; y < screenHeight; y++) {
        imgWriter.setColor(x, y, Color.BLACK);
      }
    }
  }

  public void restart() {
    curPoint = null;
    points.clear();
    particles = new Particle[particleCount];
    for (int i = 0; i < particleCount; i++) {
      particles[i] = new Particle(0, 0, 0);
      positionParticle(particles[i]);
    }
  }

  public void positionParticle(Particle p) {
    if (generationMode == 0) {
      positionParticleRandom(p);
    } else if (generationMode == 1) {
      positionParticleCircle(p);
    } else {
      positionParticleOutside(p);
    }
    if (bhmStopVelocity) {
      p.vx = 0;
      p.vy = 0;
    }
  }

  public void update() {
    for (int i = 0; i < particleCount; i++) {
      moveParticle(particles[i]);
    }
  }

  public void moveParticle(Particle p) {
    for (GravityPoint gp : points) {
      double dx = gp.x - p.x, dy = gp.y - p.y;
      double dist = Math.sqrt(dx * dx + dy * dy);
      if (blackHoleMode && dist <= 0.9 * gp.mass) {
        positionParticle(p);
        return;
      }
      double force = forceMulti * gp.mass / (dist * dist);
      p.vx += dx * force;
      p.vy += dy * force;
      p.vx += Math.random() * jitter - jitter / 2;
      p.vy += Math.random() * jitter - jitter / 2;
    }
    p.x += p.vx;
    p.y += p.vy;
    p.vx *= friction;
    p.vy *= friction;
    setSquareColor((int) p.x, (int) p.y, pixelSize, generateColor(p.vx, p.vy));
  }

  public Color generateColor(double vx, double vy) {
    double speed = Math.sqrt(vx * vx + vy * vy);
    double hue = speed * 10;
    return Color.hsb(hue, 1, 1);
  }

  public void setSquareColor(int x, int y, int size, Color color) {
    int half = size / 2;
    for (int i = 0, j; i < size; i++) {
      for (j = 0; j < size; j++) {
        x = x + i - half;
        y = y + j - half;
        if (onScreen(x, y)) {
          imgWriter.setColor(x, y, color);
        }
      }
    }
  }

  public boolean onScreen(int x, int y) {
    return x >= 0 && x < screenWidth && y >= 0 && y < screenHeight;
  }

  public void positionParticleCircle(Particle p) {
    double angle = Math.random() * 2 * Math.PI;
    double pointX = Math.cos(angle);
    double pointY = Math.sin(angle);
    double radius = Math.min(screenWidth, screenHeight) / 2.5;
    double randomX = (Math.random() * radius * 2 - radius) * 0.1;
    double randomY = (Math.random() * radius * 2 - radius) * 0.1;
    p.x = pointX * radius + screenWidth / 2 + randomX;
    p.y = pointY * radius + screenHeight / 2 + randomY;
  }

  public void positionParticleOutside(Particle p) {
    int side = (int) (Math.random() * 4);
    if (side < 2) {
      double rand = Math.random() * screenWidth;
      if (side == 0) {
        p.x = rand;
        p.y = 0;
      } else {
        p.x = rand;
        p.y = screenHeight;
      }
    } else {
      double rand = Math.random() * screenHeight;
      if (side == 2) {
        p.x = 0;
        p.y = rand;
      } else {
        p.x = screenWidth;
        p.y = rand;
      }
    }
  }

  public void positionParticleRandom(Particle p) {
    p.x = Math.random() * screenWidth;
    p.y = Math.random() * screenHeight;
  }

  @Override
  public void stop() {
    System.out.println("Stop");
    running.set(false);
    semaphore.release(100);
  }
}
