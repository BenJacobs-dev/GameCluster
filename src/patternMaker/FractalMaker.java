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
	
	int modNum = 8, screen = 1000, pixelSize = 1, blackStart = 10, fps = 1000;
	double rightShift = .5, downShift = 0;
	double xMulti = 1.5, multiIn = 0.00001, displayBounds = 1.25;
	boolean autoIterate = false; //true;
	
	///////////////////////////////////////////////
	
	int size = screen/pixelSize, sizeX = (int)(size*xMulti), sizeY = size, mapSizeX = (int)(xMulti*screen/sizeX), mapSizeY = screen/sizeY, displaySizeX = sizeX*mapSizeX,  displaySizeY = sizeY*mapSizeY, fpsMulti = 1000/fps;

	Stage stage;
	double grid[][][];
	Rectangle player;
	HashMap<KeyCode, Runnable> keyActions;
	LinkedList<KeyCode> keysPressed;
	ObservableList<Node> nodeList;
	HashMap<Integer, Color> colorList;
	PixelWriter imgWriter;
	final Semaphore semaphore = new Semaphore(1);
	final AtomicBoolean running = new AtomicBoolean(false);
	boolean playing;
	Text fpsCounterText;
	int ballAddCounter;
	volatile double multi = 1;

	public static void main(String[] args) {
		Application.launch(args);
	}

	public void start(Stage primaryStage){

		createColorList();
		
		stage = primaryStage;
		stage.setTitle("PatternMaker");
		
		Group nodeGroup = new Group();
		nodeList = nodeGroup.getChildren();

		Rectangle mapRect = new Rectangle(0, 0, displaySizeX, displaySizeY);
		
		grid = new double[sizeX][sizeY][6];
		for(int i = 0, j, hSizeX = sizeX>>1, hSizeY = sizeY>>1; i < sizeX; i++) {
			for(j = 0; j < sizeY; j++) {
				grid[i][j][1] = (displayBounds*(i-hSizeX))/hSizeY-rightShift;
				grid[i][j][2] = (displayBounds*(j-hSizeY))/hSizeY+downShift;
				grid[i][j][5] = Math.sqrt(grid[i][j][1]*grid[i][j][1] + grid[i][j][2]*grid[i][j][2]);
			}
		}
		
		WritableImage img = new WritableImage(displaySizeX, displaySizeY);
		imgWriter = img.getPixelWriter();
		mapRect.setFill(new ImagePattern(img));

		Button iterButton = new Button("Iterate");
		iterButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				updateGrid();
				updateDisplay();
			}
		});
		
		/*
		TextField valTextIn = new TextField();
		valTextIn.setAlignment(Pos.CENTER);
		valTextIn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					multi = Double.parseDouble(valTextIn.getText());
					
				}
				catch (Exception e){
					multi = 1;
				}
				//multi *= modNum;
				//updateGrid();
			}
		});
		
		Slider valSliderIn = new Slider(0, 9, 5);
		valSliderIn.setMajorTickUnit(1);
		valSliderIn.setMinorTickCount(0);
        valSliderIn.setShowTickMarks(true);
        valSliderIn.setShowTickLabels(true);
		valSliderIn.setPrefWidth(250);
		valSliderIn.setSnapToTicks(true);
		valSliderIn.valueChangingProperty().addListener(new ChangeListener<Boolean>() {
			@Override 
			public void changed(ObservableValue<? extends Boolean> observableValue, Boolean wasChanging, Boolean changing){
				if(wasChanging) {
					try {
						multi = Double.parseDouble(valTextIn.getText());
						
					}
					catch (Exception e){
						multi = 1;
					}
					String[] splitStrings = valTextIn.getText().split("\\.");
					if(splitStrings.length > 1) {
						multi += Math.pow(10, -1*splitStrings[1].length())*(((int)(valSliderIn.getValue()))/10.0);
					}
					else {
						multi += valSliderIn.getValue();
					}
					
					
					//valTextIn.setText(valTextIn.getText()+((int)(valSliderIn.getValue())));
					//updateGrid();
				}
			}
		});
		
		VBox inputBox = new VBox(valTextIn, valSliderIn);
		inputBox.setAlignment(Pos.BOTTOM_LEFT);
		*/
		
		nodeList.add(mapRect);
		nodeList.add(iterButton);

		Scene scene = new Scene(nodeGroup, displaySizeX, displaySizeY);

		stage.setScene(scene);
		stage.show();
		updateDisplay();
		
		if(autoIterate) {
			Task<Void> task = new Task<Void>() {
	
				long time;
	
				@Override
				protected Void call() throws Exception {
					try {
						time = System.currentTimeMillis();
						running.set(true);
						while(running.get()) {
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
	}

	public void updateGridOld() {
		double xTemp;
		for(int i = 0, j; i < sizeX; i++) {
			for(j = 0; j < sizeY; j++) {
				for(int count = 100; count >= 0; count--)
				if(grid[i][j][5] != Double.POSITIVE_INFINITY ) {
					grid[i][j][0]++;
					xTemp = grid[i][j][3]*grid[i][j][3]-grid[i][j][4]*grid[i][j][4]+grid[i][j][1];
					grid[i][j][4] = 2*grid[i][j][3]*grid[i][j][4]+grid[i][j][2];
					grid[i][j][3] = xTemp;
					grid[i][j][5] = Math.sqrt(grid[i][j][3]*grid[i][j][3] + grid[i][j][4]*grid[i][j][4]);
				}else {
					break;
				}
			}
		}
	}
	
	public void updateGrid() {
		double x2, y2;
		for(int i = 0, j; i < sizeX; i++) {
			for(j = 0; j < sizeY; j++) {
				x2 = grid[i][j][3]*grid[i][j][3];
				y2 = grid[i][j][4]*grid[i][j][4];
				for(int count = 100; count >= 0; count--)
				if(x2+y2 <= 4) {
					grid[i][j][0]++;
					grid[i][j][4] = (grid[i][j][3]+grid[i][j][3])*grid[i][j][4]+grid[i][j][2];
					grid[i][j][3] = x2-y2+grid[i][j][1];
					x2 = grid[i][j][3]*grid[i][j][3];
					y2 = grid[i][j][4]*grid[i][j][4];
				}
				else {
					break;
				}
			}
		}
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
	
	public void createColorList(){
		colorList = new HashMap<>();
		colorList.put(0, Color.BLACK);
		colorList.put(1, Color.RED);
		colorList.put(2, Color.ORANGE);
		colorList.put(3, Color.YELLOW);
		colorList.put(4, Color.GREEN);
		colorList.put(5, Color.BLUE);
		colorList.put(6, Color.PURPLE);
		for(blackStart = Math.min(blackStart, 7); blackStart < modNum; blackStart++) {
			colorList.put(blackStart, Color.BLACK);
		}
	}
	
	@Override
	public void stop() {
		System.out.println("Stop");
		running.set(false);
		semaphore.release(100);
	}
}
