import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.application.*;
import javafx.collections.*;
import javafx.concurrent.*;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.input.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.image.*;
import javafx.stage.*;

public class MazeViewer extends Application {

  int[][] grid, prev;
  int size = 300, mapSize = 1000 / size, displaySize = size * mapSize, delayTime = 0, searchDelayAddition = 0,
      xCreationStart, yCreationStart, xOnGridStart, yOnGridStart, xOnGridEnd, yOnGridEnd;
  LinkedList<int[]> pos;
  ArrayList<Integer> possibleSides;
  Stage stage;
  int[] curPos, next;
  ObservableList<Node> nodeList;
  boolean start, searching, found;
  final Semaphore creationSemaphore = new Semaphore(0), searchSemaphore = new Semaphore(0);
  final AtomicBoolean runningCreation = new AtomicBoolean(false), runningSearch = new AtomicBoolean(false);
  PixelWriter imgWriter;
  HashMap<Integer, Color> colorList;

  public static void main(String[] args) {
    Application.launch(args);
  }

  public void start(Stage primaryStage) {

    start = true;
    searching = false;

    createColorList();

    if ((size & 1) == 0) {
      size++;
      displaySize += mapSize;
    }

    grid = new int[size][size];
    restartGrid();

    stage = primaryStage;
    stage.setTitle("Maze Fun");

    Rectangle mapRect = new Rectangle(0, 0, mapSize * size, mapSize * size);

    WritableImage img = new WritableImage(mapSize * size, mapSize * size);
    imgWriter = img.getPixelWriter();
    mapRect.setFill(new ImagePattern(img));
    Group group = new Group();
    nodeList = group.getChildren();

    nodeList.add(mapRect);

    updateMapGrid();

    Scene scene = new Scene(group, displaySize, displaySize);

    stage.setScene(scene);
    stage.show();

    Task<Void> creationTask = new Task<Void>() {
      @Override
      protected Void call() throws Exception {
        try {
          runningCreation.set(true);
          while (runningCreation.get()) {
            Thread.sleep(delayTime);
            creationSemaphore.acquire(1);
            Platform.runLater(() -> {
              addMazeRoute();
            });
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
        return null;
      }
    };

    Task<Void> searchTask = new Task<Void>() {
      @Override
      protected Void call() throws Exception {
        try {
          runningSearch.set(true);
          while (runningSearch.get()) {
            Thread.sleep(delayTime + searchDelayAddition);
            searchSemaphore.acquire(1);
            Platform.runLater(() -> {
              addSearchRoute();
            });
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
        return null;
      }
    };

    scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent key) {
        if (!searching) {
          if (key.getCode() == KeyCode.SPACE) {
            if (start) {
              pos.add(new int[] { xCreationStart, yCreationStart, (int) Math.random() * 4 });
              prev = new int[][] { { xCreationStart, yCreationStart }, { xCreationStart, yCreationStart } };
              creationSemaphore.release();
            } else {
              restartGrid();
              start = true;
              found = false;
              updateMapGrid();
            }
          } else if (key.getCode() == KeyCode.ENTER) {
            if (!start && !found) {
              searching = true;
              pos.add(new int[] { xOnGridStart, yOnGridStart });
              searchSemaphore.release();
            }
          }
        }

      }
    });

    scene.setOnMousePressed(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent mouse) {
        if (!found) {
          if (start) {
            // found = true;
            grid[xCreationStart][yCreationStart] = 1;
            setSquareColor(xCreationStart, yCreationStart, colorList.get(grid[xCreationStart][yCreationStart]));
            xCreationStart = (int) (mouse.getSceneX() / mapSize);
            if ((xCreationStart & 1) == 0)
              xCreationStart = xCreationStart == 0 ? 1 : xCreationStart - 1;
            yCreationStart = (int) (mouse.getSceneY() / mapSize);
            if ((yCreationStart & 1) == 0)
              yCreationStart = yCreationStart == 0 ? 1 : yCreationStart - 1;
            grid[xCreationStart][yCreationStart] = 0;
            setSquareColor(xCreationStart, yCreationStart, colorList.get(-1));
          } else if (!start && grid[(int) (mouse.getSceneX() / mapSize)][(int) (mouse.getSceneY() / mapSize)] == 0) {
            if (mouse.getButton() == MouseButton.PRIMARY) {
              grid[xOnGridStart][yOnGridStart] = 0;
              setSquareColor(xOnGridStart, yOnGridStart, colorList.get(grid[xOnGridStart][yOnGridStart]));
              xOnGridStart = (int) (mouse.getSceneX() / mapSize);
              if ((xOnGridStart & 1) == 0)
                xOnGridStart--;
              yOnGridStart = (int) (mouse.getSceneY() / mapSize);
              if ((yOnGridStart & 1) == 0)
                yOnGridStart--;
              grid[xOnGridStart][yOnGridStart] = 2;
              setSquareColor(xOnGridStart, yOnGridStart, colorList.get(grid[xOnGridStart][yOnGridStart]));
            } else if (mouse.getButton() == MouseButton.SECONDARY) {
              grid[xOnGridEnd][yOnGridEnd] = 0;
              setSquareColor(xOnGridEnd, yOnGridEnd, colorList.get(grid[xOnGridEnd][yOnGridEnd]));
              xOnGridEnd = (int) (mouse.getSceneX() / mapSize);
              if ((xOnGridEnd & 1) == 0)
                xOnGridEnd--;
              yOnGridEnd = (int) (mouse.getSceneY() / mapSize);
              if ((yOnGridEnd & 1) == 0)
                yOnGridEnd--;
              grid[xOnGridEnd][yOnGridEnd] = 3;
              setSquareColor(xOnGridEnd, yOnGridEnd, colorList.get(grid[xOnGridEnd][yOnGridEnd]));
            }
          }
        }
      }
    });

    new Thread(creationTask).start();
    new Thread(searchTask).start();
  }

  public void addMazeRoute() {
    if (pos.size() == 0) {
      start = false;
      found = false;
      setSquareColor(prev[0][0], prev[0][1], colorList.get(0));
      setSquareColor(prev[1][0], prev[1][1], colorList.get(0));
      grid[xOnGridStart][yOnGridStart] = 2;
      setSquareColor(xOnGridStart, yOnGridStart, colorList.get(grid[xOnGridStart][yOnGridStart]));
      grid[xOnGridEnd][yOnGridEnd] = 3;
      setSquareColor(xOnGridEnd, yOnGridEnd, colorList.get(grid[xOnGridEnd][yOnGridEnd]));
      return;
    }
    curPos = pos.remove();
    if (curPos[0] - 2 > 0 && grid[curPos[0] - 2][curPos[1]] != 0) {
      possibleSides.add(0);
      if (curPos[2] == 0 && Math.random() < 0.25) {
        possibleSides.add(0);
        possibleSides.add(0);
      }
    }
    if (curPos[1] - 2 > 0 && grid[curPos[0]][curPos[1] - 2] != 0) {
      possibleSides.add(1);
      if (curPos[2] == 1 && Math.random() < 0.25) {
        possibleSides.add(1);
        possibleSides.add(1);
      }
    }
    if (curPos[0] + 2 < size && grid[curPos[0] + 2][curPos[1]] != 0) {
      possibleSides.add(2);
      if (curPos[2] == 0 && Math.random() < 0.25) {
        possibleSides.add(2);
        possibleSides.add(2);
      }
    }
    if (curPos[1] + 2 < size && grid[curPos[0]][curPos[1] + 2] != 0) {
      possibleSides.add(3);
      if (curPos[2] == 1 && Math.random() < 0.25) {
        possibleSides.add(3);
        possibleSides.add(3);
      }
    }
    if ((possibleSides.size() == 0)) {
      addMazeRoute();
      return;
    }
    int dir = possibleSides.get((int) (Math.random() * possibleSides.size()));
    setSquareColor(prev[0][0], prev[0][1], colorList.get(0));
    setSquareColor(prev[1][0], prev[1][1], colorList.get(0));

    if (dir == 0) {
      grid[curPos[0] - 1][curPos[1]] = 0;
      grid[curPos[0] - 2][curPos[1]] = 0;
      prev[0][0] = curPos[0] - 1;
      prev[0][1] = curPos[1];
      prev[1][0] = curPos[0] - 2;
      prev[1][1] = curPos[1];
      next = new int[] { curPos[0] - 2, curPos[1], 1 };
    } else if (dir == 1) {
      grid[curPos[0]][curPos[1] - 1] = 0;
      grid[curPos[0]][curPos[1] - 2] = 0;
      prev[0][0] = curPos[0];
      prev[0][1] = curPos[1] - 1;
      prev[1][0] = curPos[0];
      prev[1][1] = curPos[1] - 2;
      next = new int[] { curPos[0], curPos[1] - 2, 0 };
    } else if (dir == 2) {
      grid[curPos[0] + 1][curPos[1]] = 0;
      grid[curPos[0] + 2][curPos[1]] = 0;
      prev[0][0] = curPos[0] + 1;
      prev[0][1] = curPos[1];
      prev[1][0] = curPos[0] + 2;
      prev[1][1] = curPos[1];
      next = new int[] { curPos[0] + 2, curPos[1], 1 };
    } else {
      grid[curPos[0]][curPos[1] + 1] = 0;
      grid[curPos[0]][curPos[1] + 2] = 0;
      prev[0][0] = curPos[0];
      prev[0][1] = curPos[1] + 1;
      prev[1][0] = curPos[0];
      prev[1][1] = curPos[1] + 2;
      next = new int[] { curPos[0], curPos[1] + 2, 0 };
    }
    if (Math.random() <= .875) {
      pos.add(0, next);// ((int)(Math.random()*pos.size()*.1), next);
    } else {
      pos.add(next);
    }
    pos.add((int) ((Math.random() * .5 + .5) * pos.size()), curPos);
    setSquareColor(prev[0][0], prev[0][1], Color.RED);
    setSquareColor(prev[1][0], prev[1][1], Color.RED);
    possibleSides.clear();
    creationSemaphore.release();
  }

  public void addSearchRoute() {
    if (pos.size() == 0) {
      cleanGrid();
      return;
    }
    curPos = pos.peek();
    if (grid[curPos[0] - 1][curPos[1]] != 0 && grid[curPos[0] + 1][curPos[1]] != 0
        && grid[curPos[0]][curPos[1] - 1] != 0 && grid[curPos[0]][curPos[1] + 1] != 0) {
      curPos = pos.remove();
      grid[curPos[0]][curPos[1]] = 4;
      setSquareColor(curPos[0], curPos[1], colorList.get(grid[curPos[0]][curPos[1]]));
      if (grid[curPos[0] - 1][curPos[1]] == 2) {
        grid[curPos[0] - 1][curPos[1]] = 4;
        setSquareColor(curPos[0] - 1, curPos[1], colorList.get(grid[curPos[0] - 1][curPos[1]]));
      }
      if (grid[curPos[0] + 1][curPos[1]] == 2) {
        grid[curPos[0] + 1][curPos[1]] = 4;
        setSquareColor(curPos[0] + 1, curPos[1], colorList.get(grid[curPos[0] + 1][curPos[1]]));
      }
      if (grid[curPos[0]][curPos[1] - 1] == 2) {
        grid[curPos[0]][curPos[1] - 1] = 4;
        setSquareColor(curPos[0], curPos[1] - 1, colorList.get(grid[curPos[0]][curPos[1] - 1]));
      }
      if (grid[curPos[0]][curPos[1] + 1] == 2) {
        grid[curPos[0]][curPos[1] + 1] = 4;
        setSquareColor(curPos[0], curPos[1] + 1, colorList.get(grid[curPos[0]][curPos[1] + 1]));
      }
      searchSemaphore.release();
      // addSearchRoute();
      return;
    }

    if (grid[curPos[0] + 1][curPos[1]] == 0) {
      if (grid[curPos[0] + 2][curPos[1]] == 3) {
        grid[curPos[0] + 1][curPos[1]] = 2;
        setSquareColor(curPos[0] + 1, curPos[1], colorList.get(grid[curPos[0] + 1][curPos[1]]));
        searching = false;
        found = true;
        pos.clear();
        return;
      }
      grid[curPos[0] + 1][curPos[1]] = 2;
      grid[curPos[0] + 2][curPos[1]] = 2;
      setSquareColor(curPos[0] + 1, curPos[1], colorList.get(grid[curPos[0] + 1][curPos[1]]));
      setSquareColor(curPos[0] + 2, curPos[1], colorList.get(grid[curPos[0] + 2][curPos[1]]));
      pos.addFirst(new int[] { curPos[0] + 2, curPos[1] });
    } else if (grid[curPos[0]][curPos[1] + 1] == 0) {
      if (grid[curPos[0]][curPos[1] + 2] == 3) {
        grid[curPos[0]][curPos[1] + 1] = 2;
        setSquareColor(curPos[0], curPos[1] + 1, colorList.get(grid[curPos[0]][curPos[1] + 1]));
        searching = false;
        found = true;
        pos.clear();
        return;
      }
      grid[curPos[0]][curPos[1] + 1] = 2;
      grid[curPos[0]][curPos[1] + 2] = 2;
      setSquareColor(curPos[0], curPos[1] + 1, colorList.get(grid[curPos[0]][curPos[1] + 1]));
      setSquareColor(curPos[0], curPos[1] + 2, colorList.get(grid[curPos[0]][curPos[1] + 2]));
      pos.addFirst(new int[] { curPos[0], curPos[1] + 2 });
    } else if (grid[curPos[0] - 1][curPos[1]] == 0) {
      if (grid[curPos[0] - 2][curPos[1]] == 3) {
        grid[curPos[0] - 1][curPos[1]] = 2;
        setSquareColor(curPos[0] - 1, curPos[1], colorList.get(grid[curPos[0] - 1][curPos[1]]));
        searching = false;
        found = true;
        pos.clear();
        return;
      }
      grid[curPos[0] - 1][curPos[1]] = 2;
      grid[curPos[0] - 2][curPos[1]] = 2;
      setSquareColor(curPos[0] - 1, curPos[1], colorList.get(grid[curPos[0] - 1][curPos[1]]));
      setSquareColor(curPos[0] - 2, curPos[1], colorList.get(grid[curPos[0] - 2][curPos[1]]));
      pos.addFirst(new int[] { curPos[0] - 2, curPos[1] });
    } else if (grid[curPos[0]][curPos[1] - 1] == 0) {
      if (grid[curPos[0]][curPos[1] - 2] == 3) {
        grid[curPos[0]][curPos[1] - 1] = 2;
        setSquareColor(curPos[0], curPos[1] - 1, colorList.get(grid[curPos[0]][curPos[1] - 1]));
        searching = false;
        found = true;
        pos.clear();
        return;
      }
      grid[curPos[0]][curPos[1] - 1] = 2;
      grid[curPos[0]][curPos[1] - 2] = 2;
      setSquareColor(curPos[0], curPos[1] - 1, colorList.get(grid[curPos[0]][curPos[1] - 1]));
      setSquareColor(curPos[0], curPos[1] - 2, colorList.get(grid[curPos[0]][curPos[1] - 2]));
      pos.addFirst(new int[] { curPos[0], curPos[1] - 2 });
    }
    searchSemaphore.release();
  }

  public void updateMapGrid() {
    for (int i, j = 0; j < size; j++) {
      for (i = 0; i < size; i++) {
        setSquareColor(i, j, colorList.get(grid[i][j]));
      }
    }
    setSquareColor(xCreationStart, yCreationStart, colorList.get(-1));
  }

  public void restartGrid() {
    for (int i = 0, j; i < size; i++) {
      for (j = 0; j < size; j++) {
        grid[i][j] = 1;
      }
    }
    xCreationStart = 1;
    yCreationStart = 1;
    grid[xCreationStart][yCreationStart] = 0;
    pos = new LinkedList<>();
    possibleSides = new ArrayList<>();
    xOnGridStart = xCreationStart;
    yOnGridStart = yCreationStart;
    xOnGridEnd = size - 2;
    yOnGridEnd = size - 2;
  }

  public void cleanGrid() {
    for (int i = 0, j; i < size; i++) {
      for (j = 0; j < size; j++) {
        if (grid[i][j] == 2) {
          grid[i][j] = 0;
        }
      }
    }
    grid[xOnGridStart][yOnGridStart] = 2;
    grid[xOnGridEnd][yOnGridEnd] = 3;
  }

  public void setSquareColor(int x, int y, Color color) {
    for (int i = 0, j, xMap = x * mapSize, yMap = y * mapSize; i < mapSize; i++) {
      for (j = 0; j < mapSize; j++) {
        imgWriter.setColor(xMap + i, yMap + j, color);
      }
    }
  }

  public void createColorList() {
    colorList = new HashMap<>();
    colorList.put(0, Color.DARKSLATEGRAY);
    colorList.put(1, Color.BLACK);
    colorList.put(2, Color.GREEN);
    colorList.put(3, Color.RED);
    colorList.put(4, Color.PINK);
    colorList.put(-1, Color.YELLOW);

    colorList.put(4, Color.DARKSLATEGRAY);
  }

  public void stop() {
    runningCreation.set(false);
    runningSearch.set(false);
    searchSemaphore.release(100);
    creationSemaphore.release(100);
  }
}