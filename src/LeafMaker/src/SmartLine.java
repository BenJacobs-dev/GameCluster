import javafx.scene.shape.Line;

public class SmartLine extends Line{

	private double angle;
	
	SmartLine(double sX, double sY, double eX, double eY){
		super(sX,sY,eX,eY);
		angle = findAngle(sX,sY,eX,eY);
	}
	
	SmartLine(double sX, double sY, double eX, double eY, double angleIn){
		super(sX,sY,eX,eY);
		angle = angleIn;
		angle = findAngle(sX,sY,eX,eY);
	}
	
	private double findAngle(double sX, double sY, double eX, double eY) {
		eX -= sX;
		eY -= sY;
		double inRads = Math.atan2(-eY, eX);

	    if (inRads < 0)
	        inRads = -inRads;
	    else
	        inRads = 2 * Math.PI - inRads;
	    if(inRads >= 2*Math.PI) {
	    	inRads %= 2*Math.PI;
	    }
	    return inRads;
	}
	
	public double getAngle(){
		return angle;
	}

	public void setAngle(double angleIn){
		angle = angleIn;
	}
}