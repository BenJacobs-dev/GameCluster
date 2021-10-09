package blockBreaker;

import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

public class Ball extends Circle {
	
	double xChange, yChange;
	
	public Ball(double centerX, double centerY, double radius, Paint fill) {
		super(centerX, centerY, radius, fill);
		xChange = 2*Math.random()-1;
		yChange = -Math.sqrt(1-xChange*xChange);
	}

}
