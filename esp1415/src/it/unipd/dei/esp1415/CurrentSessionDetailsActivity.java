package it.unipd.dei.esp1415;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
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
import android.widget.Toast;

/**
 * Questa activity contiene il dettaglio sulla sessione corrente. Vengono
 * visualizzati la thumbnail della sessione, creata all'avvio della activity per
 * le nuove sessioni, data di inizio sessione, durata della sessione e la lista
 * delle cadute rilevate.
 * 
 * Questa activity permette di avviare, mettere in pausa, riprendere o terminare
 * una sessione previa azione sui relativi tasti. Vengono inoltre visualizzati i
 * dati dell'accelerometro in tempo reale mentre la sessione sta registrando. La
 * lista delle cadute viene anch'essa aggiornata in tempo reale.
 * 
 * Descrizione di come interagisce col WatcherService:
 * 
 * 1)creo una nuova sessione e imposto la sessione come attiva nel db o
 * ripristino la sessione in corso se è già presente una sessione attiva nel db;
 * 2)controllo se il service è già attivo, se è attivo mostro pausa, se non è
 * attivo mostro il tasto play;
 * 3)premuto play il service viene avviato;
 * 4)premuto pausa uccido il service;
 * 5)premuto stop imposto la sessione come non attiva nel db e uccido il
 * service;
 * 6)uso un broadcast receiver per tenere aggiornata l'UI mentre l'app è in
 * foreground.
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
			fm.add(R.id.fall_list_fragment, new FallListFragment());
			fm.commit();
		}
		//Chiamato per controllare se il service è attivo in background
		sServiceRunning = isMyServiceRunning(WatcherService.class);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		sDb.close();
	}

	/**
	 * Questo fragment contiene la view dedicata ai dettagli della sessione,
	 * eccetto la lista di cadute, contenuta in un altro fragment.
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
		private SharedPreferences sPreferences;
		private SharedPreferences.Editor sEditor;

		public SessionDetailsFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater
					.inflate(R.layout.current_session_details_fragment,
							container, false);
			// Apre un accesso a sharedPref
			sPreferences = getActivity().getBaseContext().getSharedPreferences("MyPref",
					Context.MODE_PRIVATE);
			sEditor = sPreferences.edit();
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
				// TODO: fare un check prima di creare una nuova sessione, per
				// vedere se la memoria disponibile è maggiore di un certo
				// numero che è da decidere
				//if (getAvailableInternalMemorySize() >= numeroX) {
				String memory = getAvailableInternalMemorySize();
				Toast.makeText(getActivity(), memory,
						Toast.LENGTH_SHORT).show();
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
			TextView timeStampSessioneTextView = (TextView) rootView
					.findViewById(R.id.session_timestamp);
			// Imposta il pulsante done sulla tastiera se l'utente tappa nel
			// campo di testo per modificare il nome della sessione
			sessionName.setImeOptions(EditorInfo.IME_ACTION_DONE);
			sessionName.setOnEditorActionListener(new OnEditorActionListener() {
				@Override
				public boolean onEditorAction(TextView v, int actionId,
						KeyEvent event) {
					boolean handled = false;
					if (actionId == EditorInfo.IME_ACTION_DONE) {
						// Questa chiamata fa sparire la tastiera una
						// volta finito di modificare il nome della
						// sessione
						InputMethodManager imm = (InputMethodManager) v
								.getContext().getSystemService(
										Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
						// Modifica il nome della sessione e registra il
						// cambiamento nel database
						sCurrentSession.setName(v.getText().toString());
						sDb.renameSession(sCurrentSession);
						handled = true;
					}
					return handled;
				}
			});
			// Impostazione dei valori iniziali dei campi del layout
			String sessionTimestamp = (String) DateFormat.format(
					"dd/MM/yy kk:mm", sCurrentSession.getSessionBegin());
			timeStampSessioneTextView.setText(getActivity().getBaseContext()
					.getString(R.string.date_and_time) + sessionTimestamp);
			sessionName.setText(sCurrentSession.getName());
			mSessionLengthTextView.setText(getActivity().getBaseContext()
					.getString(R.string.session_duration)
					+ Utilities.millisToHourMinuteSecond(
							sCurrentSession.getDuration(), true));
			mXValue.setText("");
			mYValue.setText("");
			mZValue.setText("");

			// L'immagine e il comportamento del tasto play cambiano in base
			// alla presenza del service in background
			mPlayPauseButton.setImageResource(R.drawable.ic_play_button_256);
			if (sServiceRunning) {
				mPlayPauseButton
						.setImageResource(R.drawable.ic_pause_button_256);
			}
			
			mPlayPauseButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if (sServiceRunning) {
						Intent i = new Intent(getActivity(),
								WatcherService.class);
						i.putExtra("Active", true);
						getActivity().stopService(i);
						// Modifica l'immagine del button in accordo con lo
						// stato di service non attivo
						((ImageButton) arg0)
								.setImageResource(R.drawable.ic_play_button_256);
						mXValue.setText("");
						mYValue.setText("");
						mZValue.setText("");
						sServiceRunning = false;
					}
					else {
						// Avvia il service
						mI = new Intent(getActivity(), WatcherService.class);
						// Passa al service le informazioni sulla sessione
						// attiva
						mI.putExtra("IDSessione",
								sCurrentSession.getSessionBegin());
						PendingIntent.getBroadcast(getActivity(),
								PendingIntent.FLAG_UPDATE_CURRENT, mI,
								PendingIntent.FLAG_UPDATE_CURRENT);
						getActivity().startService(mI);
						// Modifica l'immagine del bottone in accordo con lo
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

					// Termina il service e imposta la sessione come non attiva nel db
					mI = new Intent(getActivity(), WatcherService.class);
					mI.putExtra("Active", false);
					getActivity().stopService(mI);
					sCurrentSession.setActive(false);
					sDb.setActiveSession(sCurrentSession);
					sServiceRunning = false;
					// Apre il dettaglio della sessione passata riguardo alla
					// sessione appena conclusa
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
		 * Ritorna quanta memoria è disponibile nella memoria interna.
		 * 
		 * @return una stringa contenente il valore di memoria disponibile
		 */
	    public static String getAvailableInternalMemorySize() {
	        File path = Environment.getDataDirectory();
	        StatFs stat = new StatFs(path.getPath());
	        long blockSize = stat.getBlockSize();
	        long availableBlocks = stat.getAvailableBlocks();
	        return formatSize(availableBlocks * blockSize);
	    }
	    
		/**
		 * Ritorna una stringa formattata con la memoria disponibile in MB o KB.
		 * 
		 * @param availableMemory
		 *            la memoria da inserire nella stringa
		 * @return una stringa contentente la memoria formattata correttamente
		 */
	    public static String formatSize(long availableMemory) {
	        String unitOfMeasure = null;

	        if (availableMemory >= 1024) {
	            unitOfMeasure = "KB";
	            availableMemory /= 1024;
	            if (availableMemory >= 1024) {
	                unitOfMeasure = "MB";
	                availableMemory /= 1024;
	            }
	            if (availableMemory >= 1024) {
	                unitOfMeasure = "GB";
	                availableMemory /= 1024;
	            }
	        }

	        StringBuilder resultBuffer = new StringBuilder(Long.toString(availableMemory));

	        int commaOffset = resultBuffer.length() - 3;
	        while (commaOffset > 0) {
	            resultBuffer.insert(commaOffset, ',');
	            commaOffset -= 3;
	        }

	        if (unitOfMeasure != null) resultBuffer.append(unitOfMeasure);
	        return resultBuffer.toString();
	    }

		/**
		 * Handler per gli intent ricevuti dall'evento "AccData"
		 */
		private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// Controlla per evitare scritture al layout dopo che è stato
				// terminato il service
				if (sServiceRunning) {
					Float x = intent.getFloatExtra("xValue", 0f);
					Float y = intent.getFloatExtra("yValue", 0f);
					Float z = intent.getFloatExtra("zValue", 0f);
					Long duration = intent.getLongExtra("duration", 0);
					mXValue.setText(x.toString());
					mYValue.setText(y.toString());
					mZValue.setText(z.toString());
					mSessionLengthTextView.setText(getActivity()
							.getBaseContext().getString(
									R.string.session_duration)
							+ Utilities
									.millisToHourMinuteSecond(duration, true));
					// Controllo sulla durata massima, se è stata raggiunta
					// simula un clic su stop
					// Viene chiamato solo se l'app è in foreground mentre la
					// durata massima viene raggiunta
					if (intent.getBooleanExtra("maxDurationReached", false)) {
						mStopButton.callOnClick();
					}
				}
			}
		};

		@Override
		public void onResume() {
			sDb.open();
			// Registra mMessageReceiver per il ricevimento di messaggi dal broadcast.
			LocalBroadcastManager.getInstance(this.getActivity())
					.registerReceiver(mMessageReceiver,
							new IntentFilter("AccData"));
			sEditor.putBoolean("CurrentSessionOnBackground", false);
			sEditor.commit();
			// Se l'app viene riportata in foreground dopo che la durata massima
			// per la sessione è già stata raggiunta, non ho piu sessioni attive
			// in db quindi mostro il dettaglio sessione passata riguardante la
			// sessione appena terminata.
			if(!sDb.hasActiveSession()){
				sServiceRunning = false;
				Intent s = new Intent(getActivity(),
						PastSessionDetailsActivity.class);
				s.putExtra("IDSessione", sCurrentSession.getSessionBegin()
						.getTime());
				startActivity(s);
			}
			super.onResume();
		}

		@Override
		public void onPause() {
			// Smette di ascoltare il broadcast poichè l'app perde il foreground
			LocalBroadcastManager.getInstance(this.getActivity())
					.unregisterReceiver(mMessageReceiver);
			sDb.close();
			sEditor.putBoolean("CurrentSessionOnBackground", true);
			sEditor.commit();
			super.onPause();
		}

	}

	/**
	 * Fragment relativo alla lista di cadute registrate in questa sessione.
	 * Viene aggiornato in real time mentre l'activity è in foreground.
	 */
	public static class FallListFragment extends ListFragment {
		FallAdapter mAdapter;
		private ArrayList<Fall> mFalls;

		public FallListFragment() {
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
			// Recupera dal db tutte le cadute passate relative alla sessione
			mFalls = (ArrayList<Fall>) sDb.getAllFalls(sCurrentSession
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

				Long millis = intent.getLongExtra("IDFall", 0);
				if (sServiceRunning) {
					// Aggiornamento della lista di cadute appena mi viene
					// notificata una caduta dal service
					Fall newFall = sDb.getFall(new Date(millis));
					mAdapter.insert(newFall, 0);
					mAdapter.notifyDataSetChanged();
				}

			}
		};

		@Override
		public void onResume() {
			super.onResume();
			// Registra mMessageReceiver per il ricevimento di messaggi dal broadcast
			LocalBroadcastManager.getInstance(this.getActivity())
					.registerReceiver(mMessageReceiver,
							new IntentFilter("Fall"));
			mAdapter.clear();
			// Riprende tutte le cadute dal database
			List<Fall> falls = sDb.getAllFalls(sCurrentSession.getSessionBegin() );
			for (int i = 0; i < falls.size(); i++) {
				mAdapter.add(falls.get(i));
			}
			mAdapter.notifyDataSetChanged();
		}

		@Override
		public void onPause() {
			sDb.close();
			LocalBroadcastManager.getInstance(this.getActivity())
					.unregisterReceiver(mMessageReceiver);
			super.onPause();
		}

		/**
		 * Gestisce click su elementi della lista, aprendo il relativo dettaglio caduta
		 */
		@Override
		public void onListItemClick(ListView l, View v, int position, long id) {
			super.onListItemClick(l, v, position, id);

			Intent fallDetail = new Intent(getActivity()
					.getApplicationContext(), FallDetailsActivity.class);
			Date fallId = mAdapter.getItem(position).getFallTimestamp();
			fallDetail.putExtra("IDCaduta", fallId.getTime());
			fallDetail.putExtra("NomeSessione", sCurrentSession.getName());
			startActivity(fallDetail);
		}
	}

	/**
	 * Adapter relativo alla lista cadute.
	 *
	 */
	public static class FallAdapter extends ArrayAdapter<Fall> {
		private final Context mContext;
		private final ArrayList<Fall> mFalls;
		private View mRowView;
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
			mRowView = convertView;
			if (mRowView == null) {
				Holder holder = new Holder();
				mRowView = inflater.inflate(R.layout.row_fall, parent, false);
				holder.number = (TextView) mRowView
						.findViewById(R.id.fall_number);
				holder.timestamp = (TextView) mRowView
						.findViewById(R.id.fall_timestamp);
				holder.notification = (ImageView) mRowView
						.findViewById(R.id.notified);
				mRowView.setTag(holder);
			}
			Holder holder = (Holder) mRowView.getTag();
			holder.number.setText(String.valueOf(mFalls.get(position)
					.getFallNumber()));
			mTimestamp = (String) DateFormat.format("dd/MM/yy - kk:mm:ss",
					mFalls.get(position).getFallTimestamp());
			holder.timestamp.setText(mTimestamp);

			if (!mFalls.get(position).isNotified()) {
				holder.notification.setImageResource(R.drawable.cross);
			} else {
				holder.notification.setImageResource(R.drawable.tick);
			}
			return mRowView;
		}
	}

	static class Holder {
		public TextView number, timestamp;
		public ImageView notification;
	}

	/**
	 * Controlla se il service è giè stato avviato.
	 * 
	 * @param serviceClass il nome della classe contente il service
	 * @return vero se è stato avviato, falso altrimenti
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
