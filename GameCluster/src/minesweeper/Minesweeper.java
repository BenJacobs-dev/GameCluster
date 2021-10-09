package minesweeper;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.*;
import javafx.scene.layout.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.geometry.Pos;
import javafx.event.*;



public class Minesweeper extends Application{
	
	/////////////////////////////////////////////////////////////////////
	///////////////////////////Code for the UI///////////////////////////
	/////////////////////////////////////////////////////////////////////
	
	private Stage stage;
	private Font font = new Font("Courier New", 12);
	private Font bigFont = new Font("Courier New", 16);
	
	private IntField sizeInput; 
	private IntField bombInput;
	
	private Button flagOn = new Button("Flagging");
	private Button flagOff = new Button("Not Flagging");
	private Button restartButton = new Button();
	private Button flagButton;
	
	private GridPane map;
	
	public static void main(String[] args) {
		Application.launch(args);
	}
	
	public void start(Stage primaryStage) {
		
		flagOn.setPrefWidth(120);
		flagOn.setPrefHeight(40);
		flagOff.setPrefWidth(120);
		flagOff.setPrefHeight(40);
		flagOn.setOnAction(this::toggleFlag);
		flagOff.setOnAction(this::toggleFlag);
		
		restartButton.setPrefWidth(100);
		restartButton.setPrefHeight(40);
		restartButton.setOnAction(this::makeStart);
		
		sizeInput = new IntField(0, 99, 20);
		sizeInput.setPrefWidth(50);
		sizeInput.setMaxWidth(50);
		
		bombInput = new IntField(0, 99, 20);
		bombInput.setPrefWidth(50);
		bombInput.setMaxWidth(50);
		
		stage = primaryStage;
		stage.setTitle("Minesweeper");
		
		Text mainMenu1 = new Text("Welcome to");
		mainMenu1.setFont(font);
		
		Text mainMenu2 = new Text("Minesweeper");
		mainMenu2.setFont(bigFont);
		
		Button menuButton = new Button("New Game");
		menuButton.setPrefWidth(80);
		menuButton.setPrefHeight(40);
		menuButton.setOnAction(this::makeStart);
		
		VBox menuPane = new VBox(mainMenu1, mainMenu2, menuButton);
		menuPane.setSpacing(20);
		menuPane.setAlignment(Pos.CENTER);
		
		stage.setScene(new Scene(menuPane, 900, 900));
		stage.show();
		
	}
	
	public void makeStart(ActionEvent event){
		
		flagButton = flagOff;
		flagging = false;
		gameOver = false;
		map = null;
		first = true;
		
		Text sizeText = new Text("Map Size (5-30)");
		Text bombText = new Text("Percentage of bombs (10-40)");
		
		Button createButton = new Button("Create");
		createButton.setPrefWidth(80);
		createButton.setPrefHeight(40);
		createButton.setOnAction(this::makeGame);
		
		VBox startPane = new VBox(sizeText, sizeInput, bombText, bombInput, createButton);
		startPane.setSpacing(20);
		startPane.setAlignment(Pos.CENTER);
		
		stage.setScene(new Scene(startPane, 900, 900));
		stage.show();	
	}
	
	public void makeGame(ActionEvent event){
		
		if(map == null){
			createTileGrid(sizeInput.getValue(), bombInput.getValue());
		
			map = new GridPane();
			map.setAlignment(Pos.CENTER);
		
			for(int i = 0; i < mapSize; i++){
				for(int j = 0; j < mapSize; j++){
					map.add(tileGrid[i][j], i, j);
				}
			}
		}
		
		
		Text bombsLeft = new Text("# Bombs Left: " + (numBombs - numFlagged));
		bombsLeft.setFont(bigFont);
		
		Text tilesLeft = new Text("# Tiles Left: " + (numTilesLeft - numFlagged));
		tilesLeft.setFont(bigFont);
		
		HBox header = new HBox(bombsLeft, flagButton, tilesLeft);
		header.setSpacing(30);
		header.setAlignment(Pos.CENTER);
		
		VBox mapPane = new VBox(header, map);
		mapPane.setSpacing(30);
		mapPane.setAlignment(Pos.CENTER);
		
		stage.setScene(new Scene(mapPane, 900, 900));
		stage.show();	
	
	}
	
	//////////////////////////////////////////////////////////////////////
	//////////////////////Code to make the game work//////////////////////
	//////////////////////////////////////////////////////////////////////
	
	
	private Tile[][] tileGrid;
	private int mapSize;
	private boolean flagging;
	private boolean gameOver;
	private int numBombs;
	private int numTilesLeft;
	private int numFlagged;
	private int numTilesShown;
	private boolean first;
	
	public void createTileGrid(int sizeIn, int bombPercentIn){
		
		int bombPercent;
		
		if(sizeIn > 30){mapSize = 50;}
		else if(sizeIn < 5){mapSize = 5;}
		else{mapSize = sizeIn;}
		
		if(bombPercentIn > 40){bombPercent = 40;}
		else if(bombPercentIn < 10){bombPercent = 10;}
		else{bombPercent = bombPercentIn;}
		
		tileGrid = new Tile[mapSize][mapSize];
		for(int i = 0; i < mapSize; i++){
			for(int j = 0; j < mapSize; j++){
				tileGrid[i][j] = new Tile(i, j);
				tileGrid[i][j].setOnAction(this::tileClicked);
			}
		}
		
		numTilesLeft = mapSize * mapSize; 
		numBombs = (numTilesLeft * bombPercent)/100;
		numFlagged = 0;
		
		
	}
	
	public void placeBombs(){
		for(int i = 0; i < numBombs; i++){
			int w = (int)((mapSize) * Math.random());
			int h = (int)((mapSize) * Math.random());
			
			if(!tileGrid[w][h].getIsBomb() && !tileGrid[w][h].getStartTile()){
				setTileBomb(w, h);
			}
			else{
				i--;
			}
		}
	}
	
	public void setTileBomb(int w, int h){
		tileGrid[w][h].setBomb(true);
		if(w > 0 && h > 0){tileGrid[w-1][h-1].isClose();}
		if(w > 0){tileGrid[w-1][h].isClose();}
		if(w > 0 && h < mapSize-1){tileGrid[w-1][h+1].isClose();}
		if(h < mapSize-1){tileGrid[w][h+1].isClose();}
		if(w < mapSize-1 && h < mapSize-1){tileGrid[w+1][h+1].isClose();}
		if(w < mapSize-1){tileGrid[w+1][h].isClose();}
		if(w < mapSize-1 && h > 0){tileGrid[w+1][h-1].isClose();}
		if(h > 0){tileGrid[w][h-1].isClose();}
	}
	
	public void tileClicked(ActionEvent event){
		
		Tile curTile = (Tile)event.getSource();
		if(first){
			first = false;
			curTile.setStartTile(true);
			int h = curTile.getH(), w = curTile.getW();
			if(w > 0 && h > 0){tileGrid[w-1][h-1].setStartTile(true);}
			if(w > 0){tileGrid[w-1][h].setStartTile(true);}
			if(w > 0 && h < mapSize-1){tileGrid[w-1][h+1].setStartTile(true);}
			if(h < mapSize-1){tileGrid[w][h+1].setStartTile(true);}
			if(w < mapSize-1 && h < mapSize-1){tileGrid[w+1][h+1].setStartTile(true);}
			if(w < mapSize-1){tileGrid[w+1][h].setStartTile(true);}
			if(w < mapSize-1 && h > 0){tileGrid[w+1][h-1].setStartTile(true);}
			if(h > 0){tileGrid[w][h-1].setStartTile(true);}
			placeBombs();
			tileClickedExtended(curTile);
		}
		else{
			if(gameOver){
				return;
			}
			else if(curTile.getIsVisible()){
				if(curTile.getNumCloseBombs() != 0){
					tileClickedVisible(curTile);
				}
			}
			else{
				tileClickedExtended(curTile);
			}
			makeGame(new ActionEvent());
		}
	}
	
	public void tileClickedExtended(Tile curTile){

		if(numBombs == numTilesLeft){
			restartButton.setText("You Won");
			flagButton = restartButton;
			gameOver = true;
			makeGame(new ActionEvent());
			return;
		}
		else if(flagging){
			numFlagged += curTile.flag();
		}
		else{
			if(curTile.getIsFlagged()){
				return;
			}
			else if(curTile.getIsBomb()){

				curTile.showTile();
			
				for(int i = 0; i < mapSize; i++){
					for(int j = 0; j < mapSize; j++){
						if(tileGrid[i][j].getIsBomb()){
							tileGrid[i][j].showBomb();
						}
					}
				}
				restartButton.setText("You Lost");
				flagButton = restartButton;
				gameOver = true;
				makeGame(new ActionEvent());
				return;

			}
			else if(curTile.getNumCloseBombs() == 0){
				
				numTilesLeft--;
				int w = curTile.getW();
				int h = curTile.getH();
				curTile.showTile();
			
				if(w > 0 && h > 0){
					if(!tileGrid[w-1][h-1].getIsVisible())
						tileClickedExtended(tileGrid[w-1][h-1]);
				}
				if(w > 0){
					if(!tileGrid[w-1][h].getIsVisible())
						tileClickedExtended(tileGrid[w-1][h]);
				}
				if(w > 0 && h < mapSize-1){
					if(!tileGrid[w-1][h+1].getIsVisible())
						tileClickedExtended(tileGrid[w-1][h+1]);
				}
				if(h < mapSize-1){
					if(!tileGrid[w][h+1].getIsVisible())
						tileClickedExtended(tileGrid[w][h+1]);
				}
				if(w < mapSize-1 && h < mapSize-1){
					if(!tileGrid[w+1][h+1].getIsVisible())
						tileClickedExtended(tileGrid[w+1][h+1]);
				}
				if(w < mapSize-1){
					if(!tileGrid[w+1][h].getIsVisible())
						tileClickedExtended(tileGrid[w+1][h]);
				}
				if(w < mapSize-1 && h > 0){
					if(!tileGrid[w+1][h-1].getIsVisible())
						tileClickedExtended(tileGrid[w+1][h-1]);
				}
				if(h > 0){
					if(!tileGrid[w][h-1].getIsVisible())
						tileClickedExtended(tileGrid[w][h-1]);
				}
			
			}
			else{
				numTilesLeft--;
				curTile.showTile();
			}
		}
	}
	
	public void tileClickedVisible(Tile curTile){
		int nearFlagged = 0;
		int w = curTile.getW();
		int h = curTile.getH();
		
		if(w > 0 && h > 0){
			if(tileGrid[w-1][h-1].getIsFlagged())
				nearFlagged++;
		}
		if(w > 0){
			if(tileGrid[w-1][h].getIsFlagged())
				nearFlagged++;
		}
		if(w > 0 && h < mapSize-1){
			if(tileGrid[w-1][h+1].getIsFlagged())
				nearFlagged++;
		}
		if(h < mapSize-1){
			if(tileGrid[w][h+1].getIsFlagged())
				nearFlagged++;
		}
		if(w < mapSize-1 && h < mapSize-1){
			if(tileGrid[w+1][h+1].getIsFlagged())
				nearFlagged++;
		}
		if(w < mapSize-1){
			if(tileGrid[w+1][h].getIsFlagged())
				nearFlagged++;
		}
		if(w < mapSize-1 && h > 0){
			if(tileGrid[w+1][h-1].getIsFlagged())
				nearFlagged++;
		}
		if(h > 0){
			if(tileGrid[w][h-1].getIsFlagged())
				nearFlagged++;
		}
		
		if(nearFlagged >= curTile.getNumCloseBombs()){
			boolean flaggingBefore = flagging;
			flagging = false;
			
			if(w > 0 && h > 0){
				if(!tileGrid[w-1][h-1].getIsVisible())
					tileClickedExtended(tileGrid[w-1][h-1]);
			}
			if(w > 0){
				if(!tileGrid[w-1][h].getIsVisible())
					tileClickedExtended(tileGrid[w-1][h]);
			}
			if(w > 0 && h < mapSize-1){
				if(!tileGrid[w-1][h+1].getIsVisible())
					tileClickedExtended(tileGrid[w-1][h+1]);
			}
			if(h < mapSize-1){
				if(!tileGrid[w][h+1].getIsVisible())
					tileClickedExtended(tileGrid[w][h+1]);
			}
			if(w < mapSize-1 && h < mapSize-1){
				if(!tileGrid[w+1][h+1].getIsVisible())
					tileClickedExtended(tileGrid[w+1][h+1]);
			}
			if(w < mapSize-1){
				if(!tileGrid[w+1][h].getIsVisible())
					tileClickedExtended(tileGrid[w+1][h]);
			}
			if(w < mapSize-1 && h > 0){
				if(!tileGrid[w+1][h-1].getIsVisible())
					tileClickedExtended(tileGrid[w+1][h-1]);
			}
			if(h > 0){
				if(!tileGrid[w][h-1].getIsVisible())
					tileClickedExtended(tileGrid[w][h-1]);
			}
			
			flagging = flaggingBefore;
		}
		
	}
	
	public void toggleFlag(ActionEvent event){
		if(flagging){
			flagButton = flagOff;
		}
		else{
			flagButton = flagOn;
		}
		flagging = !flagging;
		makeGame(new ActionEvent());
	}
}