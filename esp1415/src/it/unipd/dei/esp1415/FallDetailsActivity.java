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
 * Activity che visualizza i dettagli relativi ad una caduta: data, ora,
 * longitudine, latitudine, se le e-mail di notifica sono state inviate con
 * successo, dei grafici riguardanti i dati dell'accelerometro e una thumbnail
 * corrispondente alla sessione a cui appartiene la caduta.
 */
public class FallDetailsActivity extends ActionBarActivity {

	private final static int X = 0;
	private final static int Y = 1;
	private final static int Z = 2;
	private DBManager mDb;
	private static Fall sCurrentFall;
	private static String sSessionName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dettaglio_caduta);
		Intent intent = getIntent();
		mDb = new DBManager(this);
		mDb.open();

		// Recupera i dati di una caduta ed il nome delal sessione relativa
		sCurrentFall = mDb.getFall((Date) new Date(intent.getLongExtra(
				"IDCaduta", 0L)));
		sCurrentFall.setFallData((ArrayList<AccelerometerData>) mDb
				.getAccData(sCurrentFall.getFallTimestamp()));
		sSessionName = intent.getStringExtra("NomeSessione");
		mDb.close();
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new FallDetailsFragment()).commit();
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
	 * Fragment per la visualizzazione del layout dell'activity
	 */
	public static class FallDetailsFragment extends Fragment {

		public FallDetailsFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(
					R.layout.fragment_dettaglio_caduta, container, false);

			// Configura le varie view coi dati della caduta corrispondenti
			int fallNumber = sCurrentFall.getFallNumber();
			Date fallDate = sCurrentFall.getFallTimestamp();
			ArrayList<AccelerometerData> accData = sCurrentFall.getFallData();

			getActivity()
					.setTitle(
							"Caduta #" + fallNumber + " della sessione "
									+ sSessionName);
			TextView date = (TextView) rootView.findViewById(R.id.data);
			TextView time = (TextView) rootView.findViewById(R.id.ora);
			TextView notification = (TextView) rootView
					.findViewById(R.id.notifica);
			TextView latitude = (TextView) rootView
					.findViewById(R.id.latitudine);
			TextView longitude = (TextView) rootView
					.findViewById(R.id.longitudine);
			ImageView thumbnailImageView = (ImageView) rootView
					.findViewById(R.id.thumbnail_label);
			Date currentSession = sCurrentFall.getSession();
			String thumbnailName = DBManager.dateToSqlDate(currentSession);
			Bitmap thumbnail = Utilities.loadImageFromStorage(thumbnailName,
					getActivity().getApplicationContext());
			thumbnailImageView.setImageBitmap(thumbnail);
			date.setText(DateFormat.format("dd/MM/yy", fallDate));
			time.setText(DateFormat.format("kk:mm:ss", fallDate));
			latitude.setText(Double.toString(sCurrentFall.getLatitude()));
			longitude.setText(Double.toString(sCurrentFall.getLongitude()));
			if (sCurrentFall.isNotified())
				notification.setText("inviata");
			else
				notification.setText("non inviata");

			// Costruisce i grafici
			GraphView graphX = (GraphView) rootView
					.findViewById(R.id.grafico_x);
			GraphView graphY = (GraphView) rootView
					.findViewById(R.id.grafico_y);
			GraphView graphZ = (GraphView) rootView
					.findViewById(R.id.grafico_z);
			graphX.setGraphParameters(accData, accData.size(), X, Color.RED);
			graphY.setGraphParameters(accData, accData.size(), Y, Color.BLUE);
			graphZ.setGraphParameters(accData, accData.size(), Z, Color.GREEN);

			return rootView;
		}
	}
}