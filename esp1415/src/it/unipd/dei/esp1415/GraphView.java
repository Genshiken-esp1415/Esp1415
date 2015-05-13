package it.unipd.dei.esp1415;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

@SuppressLint("WrongCall")
public class GraphView extends View {

	private Paint paint;
	
	
	/*public GraphView(Context context) {
		super(context);
		onMeasure(100,100);
		setBackgroundResource(R.drawable.box);
	}*/
	
	public GraphView(Context context, AttributeSet attrs) {
		
		super(context,attrs);
		
		paint = new Paint();
		paint.setStyle(Style.STROKE);
	    paint.setStrokeWidth(4);
	    paint.setColor(Color.RED);
		
		this.onMeasure(480,150);
		setBackgroundResource(R.drawable.box);
	}
	
	/*public GraphView(Context context, AttributeSet attrs, int defStyle){
		super(context);
		onMeasure(100,100);
		setBackgroundResource(R.drawable.box);
	}*/
	
	protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec){
		this.setMeasuredDimension(widthMeasureSpec,heightMeasureSpec);
	}
	
	public void onDraw(Canvas c){
		super.onDraw(c);
		
	    for(int i=0;i<10;i++)
	    	c.drawPoint(400+i, 50, paint);
	}
}
