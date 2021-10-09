package fractal;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.text.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.layout.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.geometry.Pos;
import javafx.event.*;
import java.util.*;
import java.io.*;
import javafx.collections.*;
import javafx.beans.value.*;

public class Fractal extends Application{
	
	Group lineGroup;
	ObservableList<Node> lineList;
	ArrayList<SmartLine> topLines;
	double lineLength;
	Stage stage;
	VBox menuPanel;
	//IntField lengthMultiField, angleField, lengthField;
	Slider lengthMultiField, angleField, lengthField;
	CheckBox randomColorBox, inwardModeBox, oneLineBox, bothSidesBox, dragonCurveBox, angleInvertionBox;
	int dir;
	
	public static void main(String[] args) {
		Application.launch(args);
	}
	
	public void start(Stage stageIn) {
		
		Button addLineButton = new Button();
		addLineButton.setOnAction(this::addLineLayer);
		addLineButton.setText("Add Line");
		
		Button resetButton = new Button();
		resetButton.setOnAction(this::reset);
		resetButton.setText("Reset");
		
		/*
		lengthMultiField = new IntField(0, 1000, 50);
		lengthMultiField.setPrefWidth(150);
		lengthMultiField.addEventFilter(KeyEvent.KEY_TYPED, new EventHandler<KeyEvent>() {
			@Override public void handle(KeyEvent keyEvent){
				reset(new ActionEvent());
				addLineLayer(new ActionEvent());
			}
		});
		
		lengthField = new IntField(0, 1000, 350);
		lengthField.setPrefWidth(150);
		lengthField.addEventFilter(KeyEvent.KEY_TYPED, new EventHandler<KeyEvent>() {
			@Override public void handle(KeyEvent keyEvent) {
				reset(new ActionEvent());
				addLineLayer(new ActionEvent());
			}
		});
		
		angleField = new IntField(-999, 999, 45);
		angleField.setPrefWidth(150);
		angleField.setModValue(360);
		angleField.addEventFilter(KeyEvent.KEY_TYPED, new EventHandler<KeyEvent>() {
			@Override public void handle(KeyEvent keyEvent) {
				reset(new ActionEvent());
				addLineLayer(new ActionEvent());
			}
		});
		*/
		
		lengthMultiField = new Slider(0, 99, 60);
		lengthMultiField.setMajorTickUnit(10);
        lengthMultiField.setShowTickMarks(true);
        lengthMultiField.setShowTickLabels(true);
		lengthMultiField.setPrefWidth(250);
		lengthMultiField.valueChangingProperty().addListener(new ChangeListener<Boolean>() {
			@Override public void changed(ObservableValue<? extends Boolean> observableValue, Boolean wasChanging, Boolean changing){
				reset(new ActionEvent());
				addLineLayer(new ActionEvent());
			}
		});
		
		lengthField = new Slider(0, 800, 350);
		lengthField.setMajorTickUnit(100);
        lengthField.setMinorTickCount(3);
        lengthField.setShowTickMarks(true);
        lengthField.setShowTickLabels(true);
		lengthField.setPrefWidth(250);
		lengthField.valueChangingProperty().addListener(new ChangeListener<Boolean>() {
			@Override public void changed(ObservableValue<? extends Boolean> observableValue, Boolean wasChanging, Boolean changing){
				reset(new ActionEvent());
				addLineLayer(new ActionEvent());
			}
		});
		
		angleField = new Slider(0, 180, 60);
		angleField.setMajorTickUnit(30);
        angleField.setMinorTickCount(1);
        angleField.setShowTickMarks(true);
        angleField.setShowTickLabels(true);
		angleField.setPrefWidth(250);
		angleField.valueChangingProperty().addListener(new ChangeListener<Boolean>() {
			@Override public void changed(ObservableValue<? extends Boolean> observableValue, Boolean wasChanging, Boolean changing){
				reset(new ActionEvent());
				addLineLayer(new ActionEvent());
			}
		});
		
		randomColorBox = new CheckBox("Randomize Colors");
		inwardModeBox = new CheckBox("Inward Mode");
		inwardModeBox.setOnAction(this::changeDirectionMode);
		dragonCurveBox = new CheckBox("Dragon Curve Mode");
		dragonCurveBox.setVisible(false);
		angleInvertionBox = new CheckBox("Angle Invertion");
		angleInvertionBox.setVisible(false);
		oneLineBox = new CheckBox("One Line");
		oneLineBox.setOnAction(this::changeSingleLineCount);
		bothSidesBox = new CheckBox("Both Sides");
		bothSidesBox.setOnAction(this::reset);
		
		menuPanel = new VBox(addLineButton, resetButton, lengthMultiField, lengthField, angleField, randomColorBox, inwardModeBox, oneLineBox, bothSidesBox, dragonCurveBox, angleInvertionBox);
		menuPanel.setSpacing(20);
		menuPanel.setAlignment(Pos.CENTER);
		
		lineGroup = new Group();
		lineList = lineGroup.getChildren();
		topLines = new ArrayList<SmartLine>();
		
		stage = stageIn;
		stage.setTitle("Fractal");
		reset(new ActionEvent());
		
		stage.setScene(new Scene (lineGroup, 1800, 900));
		stage.show();
		
	}
	
	public void addLineLayer(ActionEvent event){
		SmartLine curLine;
		double angleChange = angleField.getValue()*Math.PI/180;
		if(!inwardModeBox.isSelected()){
			if(!oneLineBox.isSelected()){
				boolean doOnce = true;
				ArrayList<SmartLine> tempList;
				while(lineList.size() <= 10000 || doOnce){
					tempList = new ArrayList<>();
					lineLength *= lengthMultiField.getValue()/100.0;
					for(SmartLine mainLine : topLines){
						curLine = new SmartLine(mainLine.getEndX(), mainLine.getEndY(), mainLine.getEndX() + (lineLength * Math.cos(mainLine.getAngle() + angleChange)), mainLine.getEndY() + (lineLength * Math.sin(mainLine.getAngle() + angleChange)), mainLine.getAngle() + angleChange);
						if(randomColorBox.isSelected()){
							curLine.setStroke(new Color(Math.random(),Math.random(),Math.random(),1));
						}
						tempList.add(curLine);
						lineList.add(curLine);
						curLine = new SmartLine(mainLine.getEndX(), mainLine.getEndY(), mainLine.getEndX() + (lineLength * Math.cos(mainLine.getAngle() - angleChange)), mainLine.getEndY() + (lineLength * Math.sin(mainLine.getAngle() - angleChange)), mainLine.getAngle() - angleChange);
						if(randomColorBox.isSelected()){
							curLine.setStroke(new Color(Math.random(),Math.random(),Math.random(),1));
						}
						tempList.add(curLine);
						lineList.add(curLine);
					}
					doOnce = false;
					topLines = tempList;
				}
			}
			else{
				SmartLine mainLine = topLines.get(0);
				for(int i = 0; i < 1000; i++){
					lineLength *= lengthMultiField.getValue()/100.0;
					curLine = new SmartLine(mainLine.getEndX(), mainLine.getEndY(), mainLine.getEndX() + (lineLength * Math.cos(mainLine.getAngle() + angleChange)), mainLine.getEndY() + (lineLength * Math.sin(mainLine.getAngle() + angleChange)), mainLine.getAngle() + angleChange);
					if(randomColorBox.isSelected()){
						curLine.setStroke(new Color(Math.random(),Math.random(),Math.random(),1));
					}
					lineList.add(curLine);
					mainLine = curLine;
				}
				topLines.clear();
				topLines.add(mainLine);
			}
		}
		else{
			boolean doOnce = true;
			ArrayList<SmartLine> tempList;
			while(/* lineList.size() <= 10000 || */ doOnce){
				tempList = new ArrayList<>();
				lineList.remove(1, lineList.size());
				lineLength /= (2*Math.cos(angleChange));
				for(SmartLine mainLine : topLines){
					curLine = new SmartLine(mainLine.getStartX(), mainLine.getStartY(), mainLine.getStartX() + (lineLength * Math.cos(mainLine.getAngle() - dir*angleChange)), mainLine.getStartY() + (lineLength * Math.sin(mainLine.getAngle() - dir*angleChange)), mainLine.getAngle() - dir*angleChange);
					if(randomColorBox.isSelected()){
						curLine.setStroke(new Color(Math.random(),Math.random(),Math.random(),1));
					}
					tempList.add(curLine);
					lineList.add(curLine);
					curLine = new SmartLine(mainLine.getEndX() - (lineLength * Math.cos(mainLine.getAngle() + dir*angleChange)), mainLine.getEndY() - (lineLength * Math.sin(mainLine.getAngle() + dir*angleChange)), mainLine.getEndX(), mainLine.getEndY(), mainLine.getAngle() + dir*angleChange);
					if(randomColorBox.isSelected()){
						curLine.setStroke(new Color(Math.random(),Math.random(),Math.random(),1));
					}
					tempList.add(curLine);
					lineList.add(curLine);
					if(dragonCurveBox.isSelected()){
						dir*=-1;
					}
				}
				if(angleInvertionBox.isSelected()){
					dir*=-1;
				}
				doOnce = false;
				topLines = tempList;
				}
		}
	}
	
	public void reset(ActionEvent event){
		
		lineList.clear();
		lineList.add(menuPanel);
		topLines.clear();
		dir = 1;
		
		lineLength = lengthField.getValue();
		SmartLine prevLine;
		if(!inwardModeBox.isSelected()){	
			if(bothSidesBox.isSelected()){
				prevLine = new SmartLine(900+lineLength/2, 450, 900-lineLength/2, 450, Math.PI);
				lineList.add(prevLine);
				topLines.add(prevLine);
				prevLine = new SmartLine(900-lineLength/2, 450, 900+lineLength/2, 450, 0);
			}
			else{
				prevLine = new SmartLine(900, 800, 900, 800-lineLength, -Math.PI/2);
			}
		}
		else{
			prevLine = new SmartLine(900-lineLength/2, 450, 900+lineLength/2, 450, 0);
		}
		lineList.add(prevLine);
		topLines.add(prevLine);
	}
	
	public void changeDirectionMode(ActionEvent event){
		if(inwardModeBox.isSelected()){
			oneLineBox.setVisible(false);
			oneLineBox.setSelected(false);
			bothSidesBox.setSelected(false);
			bothSidesBox.setVisible(false);
			dragonCurveBox.setVisible(true);
			angleInvertionBox.setVisible(true);
		}
		else{
			oneLineBox.setVisible(true);
			bothSidesBox.setVisible(true);
			dragonCurveBox.setVisible(false);
			dragonCurveBox.setSelected(false);
			angleInvertionBox.setVisible(false);
			angleInvertionBox.setSelected(false);
		}
		reset(new ActionEvent());
	}
	
	public void changeSingleLineCount(ActionEvent event){
		if(oneLineBox.isSelected()){
			bothSidesBox.setSelected(false);
			bothSidesBox.setVisible(false);
		}
		else{
			bothSidesBox.setVisible(true);
		}
		reset(new ActionEvent());
	}
}