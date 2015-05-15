package it.unipd.dei.esp1415;


import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.os.Build;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.*;
import android.graphics.drawable.shapes.RectShape;
/**
 * Questa activity conterr� il dettaglio su una caduta
 */
public class DettaglioCadutaActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dettaglio_caduta);
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.dettaglio_caduta, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@SuppressLint("WrongCall")
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(
					R.layout.fragment_dettaglio_caduta, container, false);
			
			/* Crea una o pi� sessioni con una o pi� cadute casuali ciascuna e stampa i valori generati nei rispettivi campi */

			Random randNumber = new Random();
			ArrayList<Session> randomSession = Randomizer.randomSession(6);
			int n = randNumber.nextInt(randomSession.size());
			Session session = randomSession.get(n);
			ArrayList<Fall> falls = session.getFallList();
			int fallSize = falls.size();
			int randFall = randNumber.nextInt(fallSize);
			Fall fall = falls.get(randFall);
			int numeroCaduta = fall.getFallNumber();
			Date dataCaduta = fall.getFallTimestamp();
			ArrayList<AccelerometerData> accData = fall.getFallData();
			getActivity().setTitle("Caduta #"+numeroCaduta+" della sessione "+session.getName());   
			TextView data = (TextView) rootView.findViewById(R.id.data);
			TextView ora = (TextView) rootView.findViewById(R.id.ora);
			TextView notifica = (TextView) rootView.findViewById(R.id.notifica);
			TextView latitudine = (TextView) rootView.findViewById(R.id.latitudine);
			TextView longitudine = (TextView) rootView.findViewById(R.id.longitudine);
			data.setText(DateFormat.format("dd/MM/yy",dataCaduta));
			ora.setText(DateFormat.format("kk:mm:ss",dataCaduta));
			latitudine.setText(Double.toString(fall.getLatitude()));
			longitudine.setText(Double.toString(fall.getLongitude()));
			if(fall.isNotified())
				notifica.setText("inviata");
			else
				notifica.setText("non inviata");
			GraphView graphX = (GraphView) rootView.findViewById(R.id.grafico_x);
			GraphView graphY = (GraphView) rootView.findViewById(R.id.grafico_y);
			GraphView graphZ = (GraphView) rootView.findViewById(R.id.grafico_z);
			graphX.setAccelerometerData(accData);
			graphY.setAccelerometerData(accData);
			graphZ.setAccelerometerData(accData);
			graphX.setAxis(0);
			graphY.setAxis(1);
			graphZ.setAxis(2);
			return rootView;
		}
	}
	
	/*public class ThumbnailBox extends View{
		private ShapeDrawable box;
		
		public ThumbnailBox(Context context){
			super(context);
			
			int x=10;
			int y=10;
			int width = 200;
			int height = 200;
			
			box = new ShapeDrawable(new RectShape());
			box.getPaint().setColor(0x0);
			box.setBounds(x, y, x+width, y+height);
		}
		
		protected void onDraw(Canvas canvas){
			box.draw(canvas);
		}
	}*/
}
