package patternMaker;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import javafx.application.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.*;
import javafx.concurrent.*;
import javafx.event.*;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.text.*;
import javafx.scene.layout.*;
import javafx.scene.input.*;
import javafx.scene.shape.*;
import javafx.scene.paint.*;
import javafx.scene.image.*;
import javafx.stage.*;

public class FractalMaker extends Application{

	///////////////////////////////////////////////
	//                Edit here                  //
	///////////////////////////////////////////////
	
	int modNum = 12, screen = 1000, pixelSize = 1, blackStart = 10, fps = 1000, iterations = 100;
	double screenCenterX = -.5, screenCenterY = 0, verticalBounds = 1.25; // horizontalBound = verticalBounds*xMulti
	double xMulti = 1.5, multiIn = 0.00001;
	
	///////////////////////////////////////////////
	
	int size = screen/pixelSize, sizeX = (int)(size*xMulti), sizeY = size, mapSizeX = (int)(xMulti*screen/sizeX), mapSizeY = screen/sizeY;
	int displaySizeX = sizeX*mapSizeX,  displaySizeY = sizeY*mapSizeY, fpsMulti = 1000/fps;
	int curIter = 0, iterCount = iterations;

	Stage stage;
	double grid[][][];
	Rectangle player;
	HashMap<KeyCode, Runnable> keyActions;
	LinkedList<KeyCode> keysPressed;
	ObservableList<Node> nodeList;
	HashMap<Integer, Color> colorList;
	PixelWriter imgWriter;
	final Semaphore semaphore = new Semaphore(1);
	final AtomicBoolean running = new AtomicBoolean(false), stopped = new AtomicBoolean(true);
	boolean playing;
	Text fpsCounterText;
	int ballAddCounter;
	volatile double multi = 1;
	double rectCenterX = 0, rectCenterY = 0, rectCornerX = 0, rectCornerY = 0, xDif = 1, yDif = 1;
	Line[] lines;

	public static void main(String[] args) {
		Application.launch(args);
	}

	public void start(Stage primaryStage){

		createColorList();
		
		lines = new Line[2000];
		for(int i = 0; i < lines.length; i++) {
			lines[i] = new Line(); 
			lines[i].setStroke(Color.WHITE);
		}
		
		modNum = colorList.size()-1;
		
		stage = primaryStage;
		stage.setTitle("PatternMaker");
		
		Group nodeGroup = new Group();
		nodeList = nodeGroup.getChildren();
		
		Polygon newScreenDimensions = new Polygon(0,0,0,0,0,0,0,0);
		ObservableList<Double> points = newScreenDimensions.getPoints();
		newScreenDimensions.setFill(Color.TRANSPARENT);
		newScreenDimensions.setStroke(Color.WHITE);

		Rectangle mapRect = new Rectangle(0, 0, displaySizeX, displaySizeY);
		mapRect.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
			if(event.getButton() == MouseButton.SECONDARY) {
				calculateLines(event.getSceneX(), event.getSceneY());
			}
			else {
				rectCenterX = event.getSceneX();
				rectCenterY = event.getSceneY();
				rectCornerX = event.getSceneX();
				rectCornerY = event.getSceneY();
				xDif = (rectCornerX-rectCenterX);
				yDif = (rectCornerY-rectCenterY);
				xDif = xDif < 0 ? -xDif : xDif;
				yDif = yDif < 0 ? -yDif : yDif;
				if(xDif < yDif*xMulti) {
					xDif = (int)(yDif*xMulti);
				}
				else {
					yDif = (int)(xDif/xMulti);
				}
				points.setAll(
						xDif+rectCenterX,
						yDif+rectCenterY,
						(-xDif)+rectCenterX,
						yDif+rectCenterY,
						(-xDif)+rectCenterX,
						(-yDif)+rectCenterY,
						xDif+rectCenterX,
						(-yDif)+rectCenterY);
				newScreenDimensions.setVisible(true);
			}
		});
		mapRect.addEventFilter(MouseEvent.MOUSE_DRAGGED, event -> {
			if(event.getButton() == MouseButton.SECONDARY) {
				calculateLines(event.getSceneX(), event.getSceneY());
			}
			else {
				rectCornerX = event.getSceneX();
				rectCornerY = event.getSceneY();
				xDif = (rectCornerX-rectCenterX);
				yDif = (rectCornerY-rectCenterY);
				xDif = xDif < 0 ? -xDif : xDif;
				yDif = yDif < 0 ? -yDif : yDif;
				if(xDif < yDif*xMulti) {
					xDif = (int)(yDif*xMulti);
				}
				else {
					yDif = (int)(xDif/xMulti);
				}
				points.setAll(
						xDif+rectCenterX,
						yDif+rectCenterY,
						(-xDif)+rectCenterX,
						yDif+rectCenterY,
						(-xDif)+rectCenterX,
						(-yDif)+rectCenterY,
						xDif+rectCenterX,
						(-yDif)+rectCenterY);
			}
		});
		mapRect.addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
			if(event.getButton() == MouseButton.SECONDARY) {
				for(int i = 0; i < lines.length; i++) {
					lines[i].setVisible(false);
				}
			}
			else {
				newScreenDimensions.setVisible(false);
				if(yDif >= 3) {
					screenCenterX += verticalBounds*2*(rectCenterX-(displaySizeX>>1))/displaySizeY;
					screenCenterY -= verticalBounds*2*(rectCenterY-(displaySizeY>>1))/displaySizeY;
					verticalBounds*= 2*yDif/displaySizeY;
					curIter = 0;
					initGrid();
					updateGrid();
					updateDisplay();
				}
			}
		});
		
		grid = new double[sizeX][sizeY][5];
		initGrid();
		updateGrid();
		
		WritableImage img = new WritableImage(displaySizeX, displaySizeY);
		imgWriter = img.getPixelWriter();
		mapRect.setFill(new ImagePattern(img));
		
		nodeList.add(mapRect);
		nodeList.add(newScreenDimensions);
		nodeList.addAll(lines);

		Scene scene = new Scene(nodeGroup, displaySizeX, displaySizeY);
		scene.addEventFilter(KeyEvent.KEY_PRESSED , key -> {
			if(key.getCode() == KeyCode.SPACE) {
				updateGrid();
				updateDisplay();
			}
			else if(key.getCode() == KeyCode.ENTER) {
				if(iterCount == 1) {
					iterCount = iterations;
					stopped.set(true);
				}
				else {
					iterCount = 1;
					stopped.set(false);
				}
			}
			else {
				if(key.getCode() == KeyCode.ESCAPE) {
					screenCenterX = -0.5;
					screenCenterY = 0;
					verticalBounds= 1.25;
				}
				else if(key.getCode() == KeyCode.W){
					verticalBounds *= 0.5;
				}
				else if(key.getCode() == KeyCode.S){
					verticalBounds *= 2;
				}
				else if(key.getCode() == KeyCode.UP){
					screenCenterY += verticalBounds*0.5;
				}
				else if(key.getCode() == KeyCode.DOWN){
					screenCenterY -= verticalBounds*0.5;
				}
				else if(key.getCode() == KeyCode.LEFT){
					screenCenterX -= xMulti*verticalBounds*0.5;
				}
				else if(key.getCode() == KeyCode.RIGHT){
					screenCenterX += xMulti*verticalBounds*0.5;
				}
				curIter = 0;
				initGrid();
				updateGrid();
				updateDisplay();
			}
		});
		
		stage.setScene(scene);
		stage.show();
		updateDisplay();
		
		
		Task<Void> task = new Task<Void>() {

			long time;

			@Override
			protected Void call() throws Exception {
				try {
					time = System.currentTimeMillis();
					running.set(true);
					while(running.get()) {
						while(stopped.get());
						semaphore.acquire();
						Thread.sleep(Math.max(fpsMulti+time-System.currentTimeMillis(), 0));
						time = System.currentTimeMillis();
						
						multi += multiIn;
						
						Platform.runLater(() -> {
							updateGrid();
							updateDisplay();
							semaphore.release();
						});
					}
				}catch(Exception e){e.printStackTrace();}
				return null;
			}
		};

		new Thread(task).start();
	}
	
	public void updateGrid() {
		double x2, y2;
		for(int i = 0, j; i < sizeX; i++) {
			for(j = 0; j < sizeY; j++) {
				x2 = grid[i][j][3]*grid[i][j][3];
				y2 = grid[i][j][4]*grid[i][j][4];
				for(int count = iterCount; count > 0; count--)
				if(x2+y2 <= 4) {
					grid[i][j][4] = (grid[i][j][3]+grid[i][j][3])*grid[i][j][4]+grid[i][j][2];
					grid[i][j][3] = x2-y2+grid[i][j][1];
					x2 = grid[i][j][3]*grid[i][j][3];
					y2 = grid[i][j][4]*grid[i][j][4];
				}
				else {
					if(grid[i][j][0] == -1) {
						grid[i][j][0] = curIter+count;
					}
					break;
				}
			}
		}
		curIter += iterCount;
	}
	
	public void updateDisplay() {
		for(int i = 0, j; i < sizeX; i++) {
			for(j = 0; j < sizeY; j++) {
				//setSquareColor((int)i, (int)j, colorList.get((int)((i*j*multi*multi)/10%modNum)));
				//setSquareColor(i, j, colorList.get((int)grid[i][j][0]%modNum));
				setSquareColor(i, j, colorList.get((int)(grid[i][j][0]%modNum)));
			}
		}
	}

	public void setSquareColor(int x, int y, Color color) {
		for(int i = 0, j, xMap = x*mapSizeX, yMap = y*mapSizeY; i < mapSizeX; i++) {
			for(j = 0; j < mapSizeY; j++) {
//				if(i == 0 || j == 0 || i == mapSizeX-1 || j == mapSizeY-1) {
//					imgWriter.setColor(xMap+i, yMap+j, colorList.get(0)); 
//				}
//				else {
					imgWriter.setColor(xMap+i, yMap+j, color);
//				}
			}
		}
	}
	
	public void initGrid() {
		for(int i = 0, j, hSizeX = sizeX>>1, hSizeY = sizeY>>1; i < sizeX; i++) {
			for(j = 0; j < sizeY; j++) {
				grid[i][j][0] = -1;
				grid[i][j][1] = (verticalBounds*(i-hSizeX))/hSizeY+screenCenterX;
				grid[i][j][2] = (verticalBounds*(j-hSizeY))/hSizeY-screenCenterY;
				grid[i][j][3] = 0;
				grid[i][j][4] = 0;
			}
		}
	}
	
	public void createColorList(){
		colorList = new HashMap<>();
		colorList.put(-1, Color.BLACK);
		colorList.put(0, Color.RED);
		colorList.put(1, Color.ORANGERED);
		colorList.put(2, Color.ORANGE);
		colorList.put(3, Color.GOLD);
		colorList.put(4, Color.YELLOW);
		colorList.put(5, Color.YELLOWGREEN);
		colorList.put(6, Color.GREEN);
		colorList.put(7, Color.DARKCYAN);
		colorList.put(8, Color.BLUE);
		colorList.put(9, Color.SLATEBLUE);
		colorList.put(10, Color.PURPLE);
		colorList.put(11, Color.MEDIUMVIOLETRED);
	}
	
	public void calculateLines(double mousePosX, double mousePosY) {
		//				 verticalBounds*2*(rectCenterX-(displaySizeX>>1)) /displaySizeY               ;
		//				(verticalBounds*  (i          -hSizeX		    ))/hSizeY       +screenCenterX;
		
		double relPosX = intoSetSpaceX(mousePosX);
		double relPosY = intoSetSpaceY(mousePosY);
		double x2 = relPosX*relPosX;
		double y2 = relPosY*relPosY;
		lines[0].setVisible(true);
		lines[0].setStartX((displaySizeX>>1)-(screenCenterX*(displaySizeY>>1))/verticalBounds);
		lines[0].setStartY((displaySizeY>>1)+(screenCenterY*(displaySizeY>>1))/verticalBounds);
		lines[0].setEndX(mousePosX);
		lines[0].setEndY(mousePosY);
		for(int i = 1; i < lines.length; i++) {
			lines[i].setVisible(true);
			lines[i].setStartX(lines[i-1].getEndX());
			lines[i].setStartY(lines[i-1].getEndY());
			if(x2+y2 <= 3000) {
				lines[i].setEndY(outSetSpaceY((intoSetSpaceX(lines[i-1].getEndX())+intoSetSpaceX(lines[i-1].getEndX()))*intoSetSpaceY(lines[i-1].getEndY())+relPosY));
				lines[i].setEndX(outSetSpaceX(x2-y2+relPosX));
				x2 = intoSetSpaceX(lines[i].getEndX())*intoSetSpaceX(lines[i].getEndX());
				y2 = intoSetSpaceY(lines[i].getEndY())*intoSetSpaceY(lines[i].getEndY());
			}
			else {
				lines[i].setVisible(false);
			}
		}
	}
	
	public double intoSetSpaceX(double input) {
		return screenCenterX+(verticalBounds*(input-(displaySizeX>>1))/(displaySizeY>>1));
	}
	
	public double intoSetSpaceY(double input) {
		return screenCenterY-(verticalBounds*(input-(displaySizeY>>1))/(displaySizeY>>1));
	}
	
	public double outSetSpaceX(double input) {
		return ((displaySizeY>>1)*(input-screenCenterX)/verticalBounds)+(displaySizeX>>1);
	}
	
	public double outSetSpaceY(double input) {
		return ((displaySizeY>>1)*(-input+screenCenterY)/verticalBounds)+(displaySizeY>>1);
	}
	
	@Override
	public void stop() {
		running.set(false);
		stopped.set(false);
		semaphore.release(100);
	}
}
