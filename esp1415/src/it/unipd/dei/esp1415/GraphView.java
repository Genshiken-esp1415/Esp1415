package it.unipd.dei.esp1415;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class GraphView extends View {

	private Paint paintX;
	private Paint paintY;
	private Paint paintZ;
	private Paint paintLine;
	private int axis;
	private ArrayList<AccelerometerData> accData;
	
	public GraphView(Context context, AttributeSet attrs) {
		
		super(context,attrs);
		
		paintX = new Paint();
		paintX.setStyle(Style.STROKE);
	    paintX.setStrokeWidth(1);
	    paintX.setColor(Color.RED);
	    
	    paintY = new Paint();
		paintY.setStyle(Style.STROKE);
	    paintY.setStrokeWidth(1);
	    paintY.setColor(Color.GREEN);
	    
	    paintZ = new Paint();
		paintZ.setStyle(Style.STROKE);
	    paintZ.setStrokeWidth(1);
	    paintZ.setColor(Color.BLUE);
		
	    paintLine = new Paint();
		paintLine.setStyle(Style.STROKE);
	    paintLine.setStrokeWidth(1);
	    paintLine.setColor(Color.CYAN);
	    
	    setBackgroundResource(R.drawable.box);
	    
	}
	
	@Override
	protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec){
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		int width = getMeasuredWidth();
	    int height = getMeasuredHeight();
		setMeasuredDimension(448,75);
	}
	
	public void setAccelerometerData(ArrayList<AccelerometerData> accData){
		this.accData=accData;
	}
	
	public void setAxis(int axis){
		this.axis=axis;
	}

	protected void onDraw(Canvas c){
		if(accData != null){
			super.onDraw(c);
			
			c.drawLine(0,37,480,37,paintLine);
			
			if(axis==0){
		    	c.drawPoint(1,(accData.get(0).getX()+37)/2,paintX);
			    for(int i=5;i<1000;i=i+4)
			    	c.drawLine(i-4,(accData.get(i-4).getX()+37)/2,i,(accData.get(i).getX()+37)/2,paintX);
			}else if(axis==1){
				c.drawPoint(1,(accData.get(0).getY()+37)/2,paintY);
			    for(int i=5;i<1000;i=i+4)
			    	c.drawLine(i-4,(accData.get(i-4).getY()+37)/2,i,(accData.get(i).getY()+37)/2,paintY);
			}else{
				c.drawPoint(1,(accData.get(0).getZ()+37)/2,paintZ);
				for(int i=5;i<1000;i=i+4)
					c.drawLine(i-4,(accData.get(i-4).getZ()+37)/2,i,(accData.get(i).getZ()+37)/2,paintZ);
			}
		}else
			Log.v("GraphView","Canvas set to NULL");
	}
}
