package it.unipd.dei.esp1415;

import java.util.ArrayList;
import java.util.Date;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

/**
 * Questa activity conterrà il dettaglio sulla sessione corrente. Descrizione di
 * come interagisce col WatcherService: 1)creo una nuova sessione e imposto la
 * sessione come attiva nel db o ripristino la sessione in corso se è già
 * presente una sessione attiva nel db; 2)controllo se il service è già attivo,
 * se è attivo mostro pausa, se non è attivo mostro il tasto play 3)premuto play
 * il service viene avviato; 4)premuto pausa uccido il service; 5)premuto stop
 * imposto la sessione come non attiva nel db e uccido il service; 6)uso un
 * broadcast receiver per tenere aggiornata l'UI mentre l'app è in foreground;
 */
public class CurrentSessionDetailsActivity extends ActionBarActivity {

	private static Session sCurrentSession;
	private static DBManager sDb;
	private static Boolean sServiceRunning;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.current_session_details_activity);
		if (savedInstanceState == null) {
			FragmentTransaction fm = getSupportFragmentManager()
					.beginTransaction();
			fm.add(R.id.current_session_details_fragment,
					new SessionDetailsFragment());
			fm.add(R.id.fall_list_fragment, new MyListFragment());
			fm.commit();
		}
		sServiceRunning = isMyServiceRunning(WatcherService.class);
	}

	@Override
	protected void onDestroy() {
		sDb.close();

		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.current_session_details, menu);
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
	 * Questo fragment contiene la view dedicata ai dettagli della sessione,
	 * eccetto la lista di cadute
	 */
	public static class SessionDetailsFragment extends Fragment {

		private TextView mXValue;
		private TextView mYValue;
		private TextView mZValue;
		private TextView mSessionLengthTextView;
		private ImageView mThumbnailImageView;
		private Intent mI;
		private ImageButton mPlayPauseButton;
		private ImageButton mStopButton;

		public SessionDetailsFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(
					R.layout.current_session_details_fragment, container,
					false);
			// Imposto la connessione al db
			sDb = new DBManager(getActivity().getBaseContext());
			sDb.open();
			// Se non sono presenti sessioni attive creo una nuova sessione e
			// avvio il service
			if (sDb.hasActiveSession()) {
				// è presente se l'app è andata in background mentre c'era una
				// sessione attiva
				sCurrentSession = sDb.getActiveSession();
			} else {
				sCurrentSession = sDb.createSession("Nuova Sessione");
				sCurrentSession.setActive(true);
				// Generazione e impostazione della thumbnail
				Date newSessionBegin = sCurrentSession.getSessionBegin();
				Bitmap thumbnailGen = Utilities
						.createThumbnail(newSessionBegin);
				// Conversione della data in stringa
				String name = DBManager.dateToSqlDate(newSessionBegin);
				// Salvataggio in memoria della thumbnail
				Utilities.saveToInternalStorage(thumbnailGen, name,
						getActivity().getBaseContext());
				sDb.setActiveSession(sCurrentSession);

			}
			this.getActivity().setTitle("Sessione attiva");
			// TextView timeStampSessioneTextView = (TextView)
			// rootView.findViewById(R.id.timestampsessione);
			EditText sessionName = (EditText) rootView
					.findViewById(R.id.session_name);
			mXValue = (TextView) rootView.findViewById(R.id.xValue);
			mYValue = (TextView) rootView.findViewById(R.id.yValue);
			mZValue = (TextView) rootView.findViewById(R.id.zValue);
			mSessionLengthTextView = (TextView) rootView
					.findViewById(R.id.session_length);
			mPlayPauseButton = (ImageButton) rootView
					.findViewById(R.id.play_pause_button);
			mStopButton = (ImageButton) rootView.findViewById(R.id.stop_button);
			// Caricamento thumbnail
			mThumbnailImageView = (ImageView) rootView
					.findViewById(R.id.session_thumbnail);
			String thumbnailName = sCurrentSession.getThumbnail();
			Bitmap thumbnail = Utilities.loadImageFromStorage(thumbnailName,
					getActivity().getApplicationContext());
			mThumbnailImageView.setImageBitmap(thumbnail);

			sessionName.setImeOptions(EditorInfo.IME_ACTION_DONE);
			sessionName.setOnEditorActionListener(new OnEditorActionListener() {
				@Override
				public boolean onEditorAction(TextView v, int actionId,
						KeyEvent event) {
					boolean handled = false;
					if (actionId == EditorInfo.IME_ACTION_DONE) {
						// Questa chiamata mi fa sparire la tastiera una
						// volta finito di modificare il nome della
						// sessione
						InputMethodManager imm = (InputMethodManager) v
								.getContext().getSystemService(
										Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
						// Modifico il nome della sessione e registro il
						// cambiamento nel database
						sCurrentSession.setName(v.getText().toString());
						sDb.renameSession(sCurrentSession);
						handled = true;
					}
					return handled;
				}
			});
			// Imposto i valori iniziali dei campi del layout
			sessionName.setText(sCurrentSession.getName());
			mSessionLengthTextView
					.setText(millisToHourMinuteSecond(sCurrentSession
							.getDuration()));
			mXValue.setText("");
			mYValue.setText("");
			mZValue.setText("");

			// L'immagine e il comportamento del tasto play cambiano in base al
			// servizio se sta andando o no
			mPlayPauseButton.setImageResource(R.drawable.ic_play_button_256);
			if (sServiceRunning) {
				mPlayPauseButton
						.setImageResource(R.drawable.ic_pause_button_256);
			}
			mPlayPauseButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if (sServiceRunning) {
						// Uccido il service però gli dico di non disattivare la
						// sessione
						Intent i = new Intent(getActivity(),
								WatcherService.class);
						i.putExtra("Active", true);
						getActivity().stopService(i);
						// Modifico l'immagine del bottone in accordo con lo
						// stato di service non attivo
						((ImageButton) arg0)
								.setImageResource(R.drawable.ic_play_button_256);
						mXValue.setText("");
						mYValue.setText("");
						mZValue.setText("");
						sServiceRunning = false;

					} else {
						// Avvio il service
						mI = new Intent(getActivity(), WatcherService.class);
						// Passo al service le informazioni sulla sessione
						// attiva
						mI.putExtra("IDSessione",
								sCurrentSession.getSessionBegin());
						PendingIntent.getBroadcast(getActivity(),
								PendingIntent.FLAG_UPDATE_CURRENT, mI,
								PendingIntent.FLAG_UPDATE_CURRENT);
						getActivity().startService(mI);
						// Modifico l'immagine del bottone in accordo con lo
						// stato di service attivo
						((ImageButton) arg0)
								.setImageResource(R.drawable.ic_pause_button_256);
						sServiceRunning = true;
					}
				}
			});
			mStopButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {

					// Uccido il service e gli dico di disattivare la sessione
					mI = new Intent(getActivity(), WatcherService.class);
					mI.putExtra("Active", false);
					getActivity().stopService(mI);
					sCurrentSession.setActive(false);
					sDb.setActiveSession(sCurrentSession);
					sServiceRunning = false;
					Intent s = new Intent(getActivity(),
							PastSessionDetailsActivity.class);
					s.putExtra("IDSessione", sCurrentSession.getSessionBegin()
							.getTime());
					startActivity(s);

				}
			});

			return rootView;
		}

		/**
		 * Handler per gli intent ricevuti dall'evento "AccData"
		 */
		private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// Controllo per evitare scritture al layout dopo che � stato
				// stoppato il service
				if (sServiceRunning) {
					// Extract data included in the Intent
					Float x = intent.getFloatExtra("xValue", 0f);
					Float y = intent.getFloatExtra("yValue", 0f);
					Float z = intent.getFloatExtra("zValue", 0f);
					Long duration = intent.getLongExtra("duration", 0);
					mXValue.setText(x.toString());
					mYValue.setText(y.toString());
					mZValue.setText(z.toString());
					mSessionLengthTextView
							.setText(millisToHourMinuteSecond(duration));
				}
			}
		};

		@Override
		public void onResume() {
			super.onResume();
			sDb.open();
			// Register mMessageReceiver to receive messages.
			LocalBroadcastManager.getInstance(this.getActivity())
					.registerReceiver(mMessageReceiver,
							new IntentFilter("AccData"));
		}

		@Override
		public void onPause() {
			// Unregister since the activity is not visible
			LocalBroadcastManager.getInstance(this.getActivity())
					.unregisterReceiver(mMessageReceiver);
			sDb.close();
			super.onPause();
		}

	}

	/**
	 * DA COMMENTARE
	 */
	public static class MyListFragment extends ListFragment {
		FallAdapter mAdapter;
		private ArrayList<Fall> mFalls;

		public MyListFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

			return inflater.inflate(R.layout.fall_list_fragment, container,
					false);
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			// Solo per testing prendo tutte le sessioni dal db
			DBManager db = new DBManager(getActivity().getBaseContext());
			db.open();
			mFalls = (ArrayList<Fall>) db.getAllFalls(sCurrentSession
					.getSessionBegin());
			mAdapter = new FallAdapter(getActivity().getBaseContext(), mFalls);
			setListAdapter(mAdapter);
			// LocalBroadcastManager.getInstance(this.getActivity())
			// .registerReceiver(mMessageReceiver,
			// new IntentFilter("Fall"));

		}

		// Handler per gli intent ricevuti dall'evento "Fall"
		private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {

				// Controllo per evitare scritture al layout dopo che è stato
				// stoppato il service o scritture doppie

				Long millis = intent.getLongExtra("IDFall", 0);
				// && falls.get(falls.size()-1).getFallTimestamp()!=(new
				// Date(millis))
				if (sServiceRunning) {
					Fall newFall = sDb.getFall(new Date(millis));
					mAdapter.insert(newFall, 0);
					// adapter.clear();
					// adapter.addAll((ArrayList<Fall>)
					// db.getAllFalls(currentSession.getSessionBegin()));
					ArrayList<AccelerometerData> acc = (ArrayList<AccelerometerData>) sDb
							.getAccData(new Date(millis));
					mAdapter.notifyDataSetChanged();
				}

			}
		};

		@Override
		public void onResume() {
			super.onResume();
			sDb.open();
			// Register mMessageReceiver to receive messages.
			LocalBroadcastManager.getInstance(this.getActivity())
					.registerReceiver(mMessageReceiver,
							new IntentFilter("Fall"));
		}

		@Override
		public void onPause() {
			sDb.close();
			// Unregister since the activity is not visible
			LocalBroadcastManager.getInstance(this.getActivity())
					.unregisterReceiver(mMessageReceiver);
			super.onPause();
		}

		/**
		 * Gestisce click su elementi della lista
		 */
		@Override
		public void onListItemClick(ListView l, View v, int position, long id) {
			super.onListItemClick(l, v, position, id);

			Intent fallDetail = new Intent(getActivity()
					.getApplicationContext(), FallDetailsActivity.class);
			Date sessionId = mAdapter.getItem(position).getFallTimestamp();
			fallDetail.putExtra("IDCaduta", sessionId.getTime());
			startActivity(fallDetail);

		}
	}

	public static class FallAdapter extends ArrayAdapter<Fall> {
		private final Context mContext;
		private final ArrayList<Fall> mFalls;
		private View mFallRowView;
		private TextView mFallNumberTextView;
		private TextView mFallTimestampTextView;
		private ImageView mNotifiedImageView;
		private String mTimestamp;

		public FallAdapter(Context context, ArrayList<Fall> values) {
			super(context, R.layout.row_fall, values);
			this.mContext = context;
			this.mFalls = values;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mFallRowView = inflater.inflate(R.layout.row_fall, parent, false);
			mFallNumberTextView = (TextView) mFallRowView
					.findViewById(R.id.fall_number);
			mFallTimestampTextView = (TextView) mFallRowView
					.findViewById(R.id.fall_timestamp);
			mNotifiedImageView = (ImageView) mFallRowView
					.findViewById(R.id.notified);
			mFallNumberTextView.setText(String.valueOf(mFalls.get(position)
					.getFallNumber()));
			mTimestamp = (String) DateFormat.format("dd/MM/yy - kk:mm", mFalls
					.get(position).getFallTimestamp());
			mFallTimestampTextView.setText(mTimestamp);

			if (!mFalls.get(position).isNotified()) {
				mNotifiedImageView.setImageResource(R.drawable.cross);
			} else {
				mNotifiedImageView.setImageResource(R.drawable.tick);
			}

			return mFallRowView;

		}

	}

	public static String millisToHourMinuteSecond(long millis) {
		String time = "";
		long seconds = millis / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;
		minutes = minutes % 60;
		seconds = seconds - hours * 3600 - minutes * 60;
		time = hours + " h " + minutes + " m " + seconds + " s ";
		return time;
	}

	/**
	 * Controllo se il service è giè stato avviato
	 * 
	 * @param serviceClass
	 * @return
	 */
	private boolean isMyServiceRunning(Class<?> serviceClass) {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager
				.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

}
