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

	/*public GraphView(Context context) {
		super(context);
		onMeasure(100,100);
		setBackgroundResource(R.drawable.box);
	}*/
	
	public GraphView(Context context, AttributeSet attrs) {
		super(context,attrs);
		this.onMeasure(1,1);
		setBackgroundResource(R.drawable.box);
		this.onDraw(new Canvas());
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
		Paint paint = new Paint();
	    paint.setStyle(Style.STROKE);
	    paint.setStrokeWidth(4);
	    paint.setColor(Color.RED);
	    for(int i=0;i<10;i++)
	    	c.drawPoint(400+i, 400, paint);
	}
}
