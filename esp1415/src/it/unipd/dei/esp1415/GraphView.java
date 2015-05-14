package it.unipd.dei.esp1415;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

public class GraphView extends View {

	private Paint paintX;
	private Paint paintY;
	private Paint paintZ;
	private ArrayList<AccelerometerData> accData;
	
	public GraphView(Context context, AttributeSet attrs) {
		
		super(context,attrs);
		
		paintX = new Paint();
		paintX.setStyle(Style.STROKE);
	    paintX.setStrokeWidth(4);
	    paintX.setColor(Color.RED);
	    
	    paintY = new Paint();
		paintY.setStyle(Style.STROKE);
	    paintY.setStrokeWidth(4);
	    paintY.setColor(Color.GREEN);
	    
	    paintZ = new Paint();
		paintZ.setStyle(Style.STROKE);
	    paintZ.setStrokeWidth(4);
	    paintZ.setColor(Color.BLUE);
		
		setBackgroundResource(R.drawable.box);
	}
	
	protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec){
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int width = getMeasuredWidth();
	    int height = getMeasuredHeight();
		setMeasuredDimension(width,height);
	}
	
	public void setAccelerometerData(ArrayList<AccelerometerData> accData){
		this.accData=accData;
	}
	
	protected void onDraw(Canvas c){
		super.onDraw(c);
		
	    for(int i=0;i<1000;i=i+2){
	    	c.drawPoint(i+3,accData.get(i).getX()+75,paintX);
	    	c.drawPoint(i+3,accData.get(i).getY()+75,paintY);
	    	c.drawPoint(i+3,accData.get(i).getZ()+75,paintZ);
	    }
	}
}
