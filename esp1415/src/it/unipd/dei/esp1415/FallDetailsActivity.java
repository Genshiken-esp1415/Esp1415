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
	protected void onPause() {
		mDb.close();
		super.onPause();
	}

	@Override
	protected void onResume() {
		mDb.open();
		super.onResume();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fall_details_activity);
		Intent intent = getIntent();
		mDb = new DBManager(this);
		mDb.open();

		// Recupera i dati di una caduta ed il nome della sessione relativa
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
		getMenuInflater().inflate(R.menu.fall_details, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
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
	 * Fragment per la visualizzazione del layout dell'activity.
	 */
	public static class FallDetailsFragment extends Fragment {

		public FallDetailsFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fall_details_fragment,
					container, false);

			// Configura le varie view coi dati della caduta corrispondenti
			int fallNumber = sCurrentFall.getFallNumber();
			Date fallDate = sCurrentFall.getFallTimestamp();
			ArrayList<AccelerometerData> accData = sCurrentFall.getFallData();

			getActivity()
					.setTitle(
							"Caduta #" + fallNumber + " di "
									+ sSessionName);
			TextView date = (TextView) rootView.findViewById(R.id.date);
			TextView time = (TextView) rootView.findViewById(R.id.time);
			TextView notification = (TextView) rootView
					.findViewById(R.id.notification);
			TextView latitude = (TextView) rootView.findViewById(R.id.latitude);
			TextView longitude = (TextView) rootView
					.findViewById(R.id.longitude);
			ImageView thumbnailImageView = (ImageView) rootView
					.findViewById(R.id.thumbnail_label);
			Date currentSession = sCurrentFall.getSession();
			String thumbnailName = DBManager.dateToSqlDate(currentSession);
			Bitmap thumbnail = Utilities.loadImageFromStorage(thumbnailName,
					getActivity().getApplicationContext());
			thumbnailImageView.setImageBitmap(thumbnail);
			date.setText(DateFormat.format("dd/MM/yy", fallDate));
			time.setText(DateFormat.format("kk:mm:ss", fallDate));
			Double latitudeValue = sCurrentFall.getLatitude();
			Double longitudeValue = sCurrentFall.getLongitude();
			String latitudeString;
			String longitudeString;
			if (latitudeValue == 0) {
				latitudeString = "N/A";
			} else {
				latitudeString = Double.toString(latitudeValue);
			}
			if (longitudeValue == 0) {
				longitudeString = "N/A";
			} else {
				longitudeString = Double.toString(longitudeValue);
			}
			latitude.setText(latitudeString);
			longitude.setText(longitudeString);
			if (sCurrentFall.isNotified())
				notification.setText("inviata");
			else
				notification.setText("non inviata");

			// Costruisce i grafici
			GraphView graphX = (GraphView) rootView.findViewById(R.id.x_graph);
			GraphView graphY = (GraphView) rootView.findViewById(R.id.y_graph);
			GraphView graphZ = (GraphView) rootView.findViewById(R.id.z_graph);
			graphX.setGraphParameters(accData, accData.size(), X, Color.RED);
			graphY.setGraphParameters(accData, accData.size(), Y, Color.BLUE);
			graphZ.setGraphParameters(accData, accData.size(), Z, Color.GREEN);

			return rootView;
		}
	}
}