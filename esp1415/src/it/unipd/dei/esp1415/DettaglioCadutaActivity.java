package it.unipd.dei.esp1415;


import java.util.ArrayList;
import java.util.Date;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * Questa activity conterr� il dettaglio su una caduta
 */
public class DettaglioCadutaActivity extends ActionBarActivity {

	private final static int X = 0;
	private final static int Y = 1;
	private final static int Z = 2;
	private DBManager db;
	private static Fall currentFall;
	private static String sessionName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dettaglio_caduta);
		Intent intent = getIntent();
		db = new DBManager(this);
		db.open();
		currentFall = db.getFall((Date) new Date(intent.getLongExtra("IDCaduta",0L)));
	    currentFall.setFallData((ArrayList<AccelerometerData>)db.getAccData(currentFall.getFallTimestamp()));
	    sessionName = intent.getStringExtra("NomeSessione");
		db.close();
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

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK)
			finish();
		return true;
	}
	
	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(
					R.layout.fragment_dettaglio_caduta, container, false);
			
			/* Crea una o pi� sessioni con una o pi� cadute casuali ciascuna e stampa i valori generati nei rispettivi campi */

//			Random randNumber = new Random();
//			ArrayList<Session> randomSession = Randomizer.randomSession(6);
//			int n = randNumber.nextInt(randomSession.size());
//			Session session = randomSession.get(n);
//			ArrayList<Fall> falls = session.getFallList();
//			int fallSize = falls.size();
//			int randFall = randNumber.nextInt(fallSize);
			int numeroCaduta = currentFall.getFallNumber();
			Date dataCaduta = currentFall.getFallTimestamp();
			ArrayList<AccelerometerData> accData = currentFall.getFallData();
			
			getActivity().setTitle("Caduta #"+numeroCaduta+" della sessione "+sessionName);   
			TextView data = (TextView) rootView.findViewById(R.id.data);
			TextView ora = (TextView) rootView.findViewById(R.id.ora);
			TextView notifica = (TextView) rootView.findViewById(R.id.notifica);
			TextView latitudine = (TextView) rootView.findViewById(R.id.latitudine);
			TextView longitudine = (TextView) rootView.findViewById(R.id.longitudine);
			ImageView thumbnailImageView = (ImageView) rootView.findViewById(R.id.thumbnail_label);
			Date currentSession = currentFall.getSession();
			String thumbnailName = DBManager.dateToSqlDate(currentSession);	
			Bitmap thumbnail = SettingValues.loadImageFromStorage(thumbnailName, getActivity().getApplicationContext());
			thumbnailImageView.setImageBitmap(thumbnail);		
			data.setText(DateFormat.format("dd/MM/yy",dataCaduta));
			ora.setText(DateFormat.format("kk:mm:ss",dataCaduta));
			latitudine.setText(Double.toString(currentFall.getLatitude()));
			longitudine.setText(Double.toString(currentFall.getLongitude()));
			if(currentFall.isNotified())
				notifica.setText("inviata");
			else
				notifica.setText("non inviata");
			
			GraphView graphX = (GraphView) rootView.findViewById(R.id.grafico_x);
			GraphView graphY = (GraphView) rootView.findViewById(R.id.grafico_y);
			GraphView graphZ = (GraphView) rootView.findViewById(R.id.grafico_z);
			graphX.setGraphParameters(accData,accData.size(),X,Color.RED);
			graphY.setGraphParameters(accData,accData.size(),Y,Color.BLUE);
			graphZ.setGraphParameters(accData,accData.size(),Z,Color.GREEN);
					
//			Intent emailIntent = new Intent();
//			emailIntent.setAction(Intent.ACTION_SEND);
//			emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"marco@speronello.com","andrea.deagnoi@studenti.unipd.it","laura.quartesan1@studenti.unipd.it"});
//			emailIntent.putExtra(Intent.EXTRA_SUBJECT,"caduta");
//			emailIntent.putExtra(Intent.EXTRA_TEXT, "Ciao, mail di prova");
//			emailIntent.setType("message/rfc822"); // "text/plain" MIME type
//			ContextWrapper context = new ContextWrapper(getActivity());
//			if (emailIntent.resolveActivity(context.getPackageManager()) != null) 
//				startActivity(emailIntent);
			
			return rootView;
		}
	}
}