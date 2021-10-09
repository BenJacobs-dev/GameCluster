package minesweeper;

import javafx.scene.control.Button;
import javafx.scene.text.Font;

public class Tile extends Button{

	private Font font = new Font("Courier New", 11);

	private boolean isBomb;
	private int numCloseBombs;
	private int w;
	private int h;
	private boolean visible;
	private boolean flagged;
	private boolean startTile;
	
	public Tile(int wIn, int hIn){
		w = wIn;
		h = hIn;
		isBomb = false;
		numCloseBombs = 0;
		setText(" ");
		setFont(font);
		visible = false;
		flagged = false;
	}
	
	public void setBomb(boolean bombIn){
		isBomb = bombIn;
	}
	
	public void isClose(){
		numCloseBombs++;
	}
	
	public void setStartTile(boolean startIn){
		startTile = startIn;
	}
	
	public boolean getStartTile(){
		return startTile;
	}
	
	public void showTile(){
		visible = true;
		if(isBomb){
			setText("O");
			setStyle("-fx-background-color: #000000; -fx-text-fill: #ff0000; ");
		}else if(numCloseBombs == 0){
			setStyle("-fx-background-color: #c8c8c8; ");
		}
		else{
			setText("" + numCloseBombs);
			switch(numCloseBombs){
				case 1: setStyle("-fx-text-fill: #0000ff; "); break;
				case 2: setStyle("-fx-text-fill: #00cc00; "); break;
				case 3: setStyle("-fx-text-fill: #ff0000; "); break;
				case 4: setStyle("-fx-text-fill: #6600cc; "); break;
				case 5: setStyle("-fx-text-fill: #800000; "); break;
				case 6: setStyle("-fx-text-fill: #0099cc; "); break;
				case 7: setStyle("-fx-text-fill: #ff6600; "); break;
				case 8: setStyle("-fx-text-fill: #999966; "); break;
				default: 
			}
		}
	}
	
	public void showBomb(){
		visible = true;
		setText("O");
		setStyle("-fx-background-color: #202020; -fx-text-fill: #ff0000; ");
	}
	
	public boolean getIsBomb(){
		return isBomb;
	}
	
	public int getNumCloseBombs(){
		return numCloseBombs;
	}
	
	public int getW(){
		return w;
	}
	
	public int getH(){
		return h;
	}
	
	public boolean getIsVisible(){
		return visible;
	}
	
	public boolean getIsFlagged(){
		return flagged;
	}
	
	public int flag(){
		if(!visible){
			flagged = !flagged;
			if(flagged){
				setText("F");
				return 1;
			}
			else{
				setText(" ");
				return -1;
			}
		}
		return 0;
	}
	
}