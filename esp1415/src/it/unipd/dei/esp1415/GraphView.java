package it.unipd.dei.esp1415;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

/**
 * Classe per la stampa del grafico di una funzione basata sui dati ricevuti dall'accelerometro
 * @author Marco
 */
public class GraphView extends View {

	private final int GRAPH_WIDTH = 448;
	private final int GRAPH_HEIGHT = 76;
	private final int GRAPH_CENTER = GRAPH_HEIGHT/2;
	
	private final int X = 0;
	private final int Y = 1;
	//private final int Z = 2;

	private Paint paint;
	private Paint paintLine;
	private int axis;
	private int samples;
	private ArrayList<AccelerometerData> accData;

	public GraphView(Context context, AttributeSet attrs) {

		super(context,attrs);

		//Paint per il disegno dello scheletro del grafico
		paintLine = new Paint();
		paintLine.setStyle(Style.STROKE);
		paintLine.setStrokeWidth(1);
		paintLine.setColor(Color.CYAN);

		//cornice del grafico
		setBackgroundResource(R.drawable.box);

	}

	@Override
	protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec){
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		setMeasuredDimension(GRAPH_WIDTH,GRAPH_HEIGHT);
	}

	public void setGraphParameters(ArrayList<AccelerometerData> accData, int samples, int axis, int color){
		this.accData = accData;
		
		//numero di campioni da graficare
		this.samples = samples;
		
		//asse (x, y o z) che riguarda i dati forniti
		this.axis = axis;
		
		//Paint per il disegno della funzione graficata
		paint = new Paint();
		paint.setStyle(Style.STROKE);
		paint.setStrokeWidth(1);
		paint.setColor(color);
	}

	protected void onDraw(Canvas c){
		if(accData != null){
			super.onDraw(c);
			
			//disegna una linea che rappresenta l'asse delle ascisse nel grafico
			c.drawLine(0,GRAPH_CENTER,GRAPH_WIDTH,GRAPH_CENTER,paintLine);
			
			//in base al numero di campioni ricevuti, calcola la distanza tra un punto e il successivo nel grafico
			int offset = (int) Math.round(GRAPH_WIDTH/(samples-1)+0.5);
			
			//grafica la funzione collegando ogni campione al successivo
			if(axis==X){
				c.drawPoint(1,(accData.get(0).getX()+GRAPH_CENTER),paint);
				for(int i=1;i<samples;i++)
					c.drawLine(1+offset*(i-1),(accData.get(i-1).getX()+GRAPH_CENTER),1+offset*i,(accData.get(i).getX()+GRAPH_CENTER),paint);
			}else if(axis==Y){
				c.drawPoint(1,(accData.get(0).getY()+GRAPH_CENTER),paint);
				for(int i=1;i<samples;i++)
					c.drawLine(1+offset*(i-1),(accData.get(i-1).getY()+GRAPH_CENTER),1+offset*i,(accData.get(i).getY()+GRAPH_CENTER),paint);
			}else{
				c.drawPoint(1,(accData.get(0).getZ()+GRAPH_CENTER),paint);
				for(int i=1;i<samples;i++)
					c.drawLine(1+offset*(i-1),(accData.get(i-1).getZ()+GRAPH_CENTER),1+offset*i,(accData.get(i).getZ()+GRAPH_CENTER),paint);
			}
			
			//ridisegna il bordo destro della cornice per eliminare il trasbordo della funzione graficata sulla cornice
			c.drawLine(GRAPH_WIDTH-1, 0, GRAPH_WIDTH-1, GRAPH_HEIGHT, paintLine);
		}
	}
}
