package pong;

import java.util.*;
import java.util.concurrent.*;
import javafx.application.*;
import javafx.concurrent.*;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.input.*;
import javafx.scene.shape.*;
import javafx.scene.paint.*;
import javafx.stage.*;

public class PongMain extends Application {

  Stage stage;
  private HashMap<KeyCode, Runnable> keyActions;
  private LinkedList<KeyCode> keysPressed;
  final Semaphore semaphore = new Semaphore(1);

  public static void main(String[] args) {
    Application.launch(args);
  }

  public void start(Stage primaryStage) {

    createKeyActions();
    // createPlayerPaddle

    stage = primaryStage;
    stage.setTitle("Maze Fun");

    Scene scene = new Scene(new VBox(new Rectangle(500, 500, Color.BLUE)), 1500, 1000);

    stage.setScene(scene);
    stage.show();

    scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent key) {
        if (!keysPressed.contains(key.getCode())) {
          keysPressed.add(key.getCode());
        }
      }
    });

    scene.setOnKeyReleased(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent key) {
        keysPressed.remove(key.getCode());
      }
    });

    Task<Void> task = new Task<Void>() {

      long time;

      @Override
      protected Void call() throws Exception {
        try {
          time = System.currentTimeMillis();
          while (true) {
            semaphore.acquire(1);
            Thread.sleep(Math.max(16 + time - System.currentTimeMillis(), 0));
            time = System.currentTimeMillis();
            Platform.runLater(() -> {
              updateDisplay();
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
    for (KeyCode code : keysPressed) {
      if (keyActions.containsKey(code))
        keyActions.get(code).run();
    }

    semaphore.release();
  }

  public void createKeyActions() {
    keysPressed = new LinkedList<>();
    keyActions = new HashMap<>();
    keyActions.put(KeyCode.UP, () -> {
      System.out.println("UP");
    });
    keyActions.put(KeyCode.DOWN, () -> {
      System.out.println("DOWN");
    });
    keyActions.put(KeyCode.SPACE, () -> {
      System.out.println("SPACE");
    });
    keyActions.put(KeyCode.ESCAPE, () -> {
      System.out.println("ESCAPE");
    });
  }
}
