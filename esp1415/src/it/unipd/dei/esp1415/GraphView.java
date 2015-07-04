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
 * Classe per la stampa del grafico di una funzione basata sui dati ricevuti
 * dall'accelerometro.
 */
public class GraphView extends View {

	// Costanti per la dimensione del grafico e la posizione centrale in
	// larghezza ed altezza
	private final static int GRAPH_WIDTH = 400;
	private final static int GRAPH_HEIGHT = 76;
	private final static int GRAPH_HEIGHT_CENTER = GRAPH_HEIGHT / 2;
	private final static int GRAPH_WIDTH_CENTER = GRAPH_WIDTH / 2;

	// Costanti per identificare se i dati dell'accelerometro riguardano l'asse
	// X o Y (Z per esclusione)
	private final static int X = 0;
	private final static int Y = 1;

	private Paint mPaint;
	private Paint mPaintLine;
	private int mAxis;
	private int mSamples;
	private ArrayList<AccelerometerData> mAccData;

	public GraphView(Context context, AttributeSet attrs) {

		super(context, attrs);

		// Paint per il disegno dello scheletro del grafico
		mPaintLine = new Paint();
		mPaintLine.setStyle(Style.STROKE);
		mPaintLine.setStrokeWidth(1);
		mPaintLine.setColor(Color.BLACK);

		// Cornice del grafico
		setBackgroundResource(R.drawable.box);

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		setMeasuredDimension(GRAPH_WIDTH, GRAPH_HEIGHT);
	}

	public void setGraphParameters(ArrayList<AccelerometerData> accData,
			int samples, int axis, int color) {
		this.mAccData = accData;

		// Numero di campioni da graficare
		this.mSamples = samples;

		// Asse (x, y o z) che riguarda i dati forniti
		this.mAxis = axis;

		// Paint per il disegno della funzione graficata
		mPaint = new Paint();
		mPaint.setStyle(Style.STROKE);
		mPaint.setStrokeWidth(1);
		mPaint.setColor(color);
	}

	protected void onDraw(Canvas c) {
		if (mAccData != null) {
			super.onDraw(c);

			// Disegna una linea che rappresenta l'asse delle ascisse nel
			// grafico
			c.drawLine(0, GRAPH_HEIGHT_CENTER, GRAPH_WIDTH,
					GRAPH_HEIGHT_CENTER, mPaintLine);

			// Disegna una linea che rappresenta l'asse delle ordinate nel
			// grafico
			c.drawLine(GRAPH_WIDTH_CENTER, 0, GRAPH_WIDTH_CENTER, GRAPH_HEIGHT,
					mPaintLine);

			// In base al numero di campioni ricevuti, calcola la distanza tra
			// un punto e il successivo nel grafico
			int offset = (int) Math.round(GRAPH_WIDTH / (mSamples - 1) + 0.5);

			// Grafica la funzione ottenuta dai dati dell'accelerometro,
			// collegando ogni campione al successivo. Si
			// usa l'offset per fare in modo che l'asse y = 0 corrisponda con la
			// metà in (altezza) del grafico
			if (mAxis == X) {
				c.drawPoint(1, (mAccData.get(0).getX() + GRAPH_HEIGHT_CENTER),
						mPaint);
				for (int i = 1; i < mSamples; i++) {
					c.drawLine(1 + offset * (i - 1), (mAccData.get(i - 1)
							.getX() + GRAPH_HEIGHT_CENTER), 1 + offset * i,
							(mAccData.get(i).getX() + GRAPH_HEIGHT_CENTER),
							mPaint);
				}
			} else if (mAxis == Y) {
				c.drawPoint(1, (mAccData.get(0).getY() + GRAPH_HEIGHT_CENTER),
						mPaint);
				for (int i = 1; i < mSamples; i++) {
					c.drawLine(1 + offset * (i - 1), (mAccData.get(i - 1)
							.getY() + GRAPH_HEIGHT_CENTER), 1 + offset * i,
							(mAccData.get(i).getY() + GRAPH_HEIGHT_CENTER),
							mPaint);
				}
			} else {
				c.drawPoint(1, (mAccData.get(0).getZ() + GRAPH_HEIGHT_CENTER),
						mPaint);
				for (int i = 1; i < mSamples; i++) {
					c.drawLine(1 + offset * (i - 1), (mAccData.get(i - 1)
							.getZ() + GRAPH_HEIGHT_CENTER), 1 + offset * i,
							(mAccData.get(i).getZ() + GRAPH_HEIGHT_CENTER),
							mPaint);
				}
			}

			// Ridisegna il bordo destro, inferiore e superiore della cornice
			// per eliminare il trasbordo della funzione graficata sulla cornice
			// stessa
			c.drawLine(GRAPH_WIDTH - 1, 0, GRAPH_WIDTH - 1, GRAPH_HEIGHT,
					mPaintLine);
			c.drawLine(0, GRAPH_HEIGHT - 1, GRAPH_WIDTH, GRAPH_HEIGHT - 1,
					mPaintLine);
			c.drawLine(0, 0, GRAPH_WIDTH, 0, mPaintLine);
		}
	}
}
