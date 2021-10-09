package blockBreaker;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import javafx.application.*;
import javafx.collections.*;
import javafx.concurrent.*;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.text.*;
import javafx.scene.layout.*;
import javafx.scene.input.*;
import javafx.scene.shape.*;
import javafx.scene.paint.*;
import javafx.scene.image.*;
import javafx.stage.*;
public class BlockBreakerMain extends Application{

	int fps = 1000/60, sizeX = 40, sizeY = (int)(sizeX*1.33), mapSizeX = 1500/sizeX, mapSizeY = 1000/sizeY, displaySizeX = sizeX*mapSizeX,  displaySizeY = sizeY*mapSizeY;

	Stage stage;
	int[][] grid;
	Rectangle player;
	HashMap<KeyCode, Runnable> keyActions;
	LinkedList<KeyCode> keysPressed;
	ObservableList<Node> nodeList;
	HashMap<Integer, Color> colorList;
	PixelWriter imgWriter;
	final Semaphore semaphore = new Semaphore(1);
	final AtomicBoolean running = new AtomicBoolean(false);
	boolean playing;
	LinkedList<Ball> ballList, ballRemoveList;
	Text fpsCounterText;
	int ballAddCounter;

	public static void main(String[] args) {
		Application.launch(args);
	}

	public void start(Stage primaryStage){

		createKeyActions();
		createColorList();
		ballList = new LinkedList<>();
		ballRemoveList = new LinkedList<>();
		ballAddCounter = 0;

		grid = new int[sizeX][sizeY];
		restartGrid();

		stage = primaryStage;
		stage.setTitle("Block Breaker");
		
		Group nodeGroup = new Group();
		nodeList = nodeGroup.getChildren();

		Rectangle mapRect = new Rectangle(0, 0, displaySizeX, displaySizeY);

		WritableImage img = new WritableImage(displaySizeX, displaySizeY);
		imgWriter = img.getPixelWriter();
		mapRect.setFill(new ImagePattern(img));
		updateBlockGrid();
		
		fpsCounterText = new Text(1400, 40, "60");
		fpsCounterText.setFont(new Font("Courier New", 32));
		fpsCounterText.setFill(Color.HOTPINK);
		
		nodeList.add(mapRect);
		nodeList.add(fpsCounterText);

		Scene scene = new Scene(nodeGroup, displaySizeX, displaySizeY);

		createPlayerPaddle();

		stage.setScene(scene);
		stage.show();

		playing = false;

		scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent key) {
				if(!keysPressed.contains(key.getCode())){
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

//		scene.setOnMousePressed(new EventHandler<MouseEvent>() {
//			@Override
//			public void handle(MouseEvent mouse) {
//				setSquareColor((int)(mouse.getSceneX()/(mapSizeX)), (int)(mouse.getSceneY()/(mapSizeY)), colorList.get(-1));
//			}
//		});
		
		Task<Void> task = new Task<Void>() {

			long time;

			@Override
			protected Void call() throws Exception {
				try {
					time = System.currentTimeMillis();
					running.set(true);
					while(running.get()) {
						semaphore.acquire(1);
						Thread.sleep(Math.max(fps+time-System.currentTimeMillis(), 0));
						time = System.currentTimeMillis();
						Platform.runLater(() -> {
							updateDisplay();
						});
					}
				}catch(Exception e){e.printStackTrace();}
				return null;
			}
		};

		new Thread(task).start();
	}

	public void createPlayerPaddle() {
		player = new Rectangle(displaySizeX/2-50, displaySizeY*0.9, 100, 15);
		player.setFill(Color.BROWN);

		nodeList.add(player);
	}

	public void updateDisplay() {
		for(KeyCode code : keysPressed) {
			if(keyActions.containsKey(code)) keyActions.get(code).run();
		}
		for(Ball ball : ballList) {
			moveBall(ball);
		}
		for(Ball ball : ballRemoveList) {
			ballList.remove(ball);
			nodeList.remove(ball);
		}
		for(;ballAddCounter > 0; ballAddCounter--) {
			releaseBall();
		}
		ballRemoveList.clear();
    	fpsCounterText.setText(String.valueOf(ballList.size()));
		semaphore.release();
	}

	public void restartGrid() {
		for(int i = 0, j; i < sizeX; i++) {
			for(j = 0; j < sizeY*0.6; j++){
				grid[i][j] = (int)(Math.random()*4)+1;
			}
		}
	}

	public void updateBlockGrid() {
		for(int i = 0, j; i < sizeX; i++) {
			for(j = 0; j < sizeY; j++) {
				setSquareColor(i, j, colorList.get(grid[i][j]));
			}
		}
	}

	public void setSquareColor(int x, int y, Color color) {
		for(int i = 0, j, xMap = x*mapSizeX, yMap = y*mapSizeY; i < mapSizeX; i++) {
			for(j = 0; j < mapSizeY; j++) {
				if(i == 0 || j == 0 || i == mapSizeX-1 || j == mapSizeY-1) {
					imgWriter.setColor(xMap+i, yMap+j, colorList.get(0)); 
				}
				else {
					imgWriter.setColor(xMap+i, yMap+j, color);
				}
			}
		}
	}
	
	public void releaseBall() {
		playing = true;
		ballList.add(new Ball(player.getX() + 50, player.getY() - mapSizeY, mapSizeY>>2, Color.WHITE));
		nodeList.add(ballList.getLast());
	}
	
	public void moveBall(Ball ball) {
		
		ball.setCenterY(ball.getCenterY()+ball.yChange*5);
		if(ball.getCenterY() >= displaySizeY) {
			ballRemoveList.add(ball);
			return;
		}
		else if(ball.getCenterY() < 0) {
			ball.yChange *= -1;
			ball.setCenterY(ball.getCenterY()+ball.yChange*5);
		}
		int xPos = (int)(ball.getCenterX()/mapSizeX);
		int yPos = (int)(ball.getCenterY()/mapSizeY);
		if(grid[xPos][yPos] != 0) {
			if(grid[xPos][yPos] > 0) {
				grid[xPos][yPos] = 0;
				setSquareColor(xPos, yPos, colorList.get(0));
				//ballAddCounter++;
			}
			ball.yChange *= -1;
			ball.setCenterY(ball.getCenterY()+ball.yChange*5);
		}
		
		ball.setCenterX(ball.getCenterX()+ball.xChange*5);
		if(ball.getCenterX() < 0 || ball.getCenterX() >= displaySizeX) {
			ball.xChange *= -1;
			ball.setCenterX(ball.getCenterX()+ball.xChange*5);
		}
		xPos = (int)(ball.getCenterX()/mapSizeX);
		yPos = (int)(ball.getCenterY()/mapSizeY);
		
		if(grid[xPos][yPos] != 0) {
			if(grid[xPos][yPos] > 0) {
				grid[xPos][yPos] = 0;
				setSquareColor(xPos, yPos, colorList.get(0));
				//ballAddCounter++;
			}
			ball.xChange *= -1;
			ball.setCenterX(ball.getCenterX()+ball.xChange*5);
		}
		if(ball.getCenterY() >= displaySizeY*0.9 && ball.getCenterY() <= displaySizeY*0.9+15) {
			if(ball.getCenterX() >= player.getX() && ball.getCenterX() <= player.getX()+100) {
				ball.xChange = (ball.getCenterX()-(player.getX()+50))/50;
				ball.yChange = -Math.sqrt(1-ball.xChange*ball.xChange);
				ball.setCenterY(ball.getCenterY()+ball.yChange*5);
			}
		}
	}

	public void createKeyActions() {
		keysPressed = new LinkedList<>();
		keyActions = new HashMap<>();
		keyActions.put(KeyCode.LEFT, () -> {player.setX(Math.max(player.getX()-10, 0));});
		keyActions.put(KeyCode.RIGHT, () -> {player.setX(Math.min(player.getX()+10, 1400));});
		keyActions.put(KeyCode.SPACE, () -> {if(!playing)releaseBall();});
		keyActions.put(KeyCode.ESCAPE, () -> {System.out.println("ESCAPE");});
	}

	public void createColorList(){
		colorList = new HashMap<>();
		colorList.put(0, Color.BLACK);
		colorList.put(1, Color.BLUE);
		colorList.put(2, Color.GREEN);
		colorList.put(3, Color.RED);
		colorList.put(4, Color.PINK);
		colorList.put(-1, Color.YELLOW);
	}
	
	public void stop() {
		running.set(false);
		semaphore.release(100);
	}
}
