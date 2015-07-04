package it.unipd.dei.esp1415;

import java.util.Date;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

/**
 * Service che elabora i dati dell'accelerometro in background.
 * 
 * Una volta creato avvia la registrazione e l'elaborazione dei dati
 * dell'accelerometro, quando viene terminato salva la durata della sessione.
 * Una volta che riconosce una caduta, in questa specifica e semplice
 * implementazione attraverso una sogliatura dell'accelerazione del dispositivo,
 * prova a determinare una posizione utilizzando il gps o la rete dati del
 * dispositivo. Terminata quella fase tenterà di notificare la caduta via mail
 * alla lista di contatti specificata nelle opzioni.
 * 
 * Terminato l'invio della mail, sia che abbia successo che fallisca, la caduta
 * viene registrata nel db e viene notificata via broadcast receiver al
 * dettaglio sessione corrente, che aggiornerà la lista delle cadute.
 * 
 * È previsto anche un controllo sulla durata massima: se la durata dovesse
 * sforare la durata massima il service terminerà l'esecuzione e se l'activity
 * del dettaglio sessione corrente è in foreground le dirà di simulare la
 * pressione del tasto stop, altrimenti imposterà da solo il campo active di
 * sessione a falso e lancerà una notifica dove avverte della durata massima
 * raggiunta.
 * 
 * Tra una caduta e l'altra devono passare almeno 30 secondi, che coincidono con
 * il tempo massimo dato al GPS per trovare una posizione.
 * 
 * Nell'Archos 70 il sample rate corrispondente a SENSOR_DELAY_UI non funziona
 * correttamente in quanto non registra cadute, ma avendo testato anche su altri
 * dispositivi dove esso funziona possiamo concludere che si debba trattare di
 * un problema dovuto dall'Archos 70.
 *
 * Un'ottimizzazione fatta per migliorare la durata della batteria è quella di
 * non richiedere la posizione del gps in modo continuativo ma solo quando una
 * caduta viene effettivamente rilevata.
 */

public class WatcherService extends Service implements SensorEventListener {
	// DICHIARAZIONE COSTANTI
	private static final int FASTEST = 0;
	private static final int FAST = 20000000;
	private static final int NORMAL = 60000000;
	private static final int LOW = 200000000;
	private static final int MAX_TIME_OUT = 30000;
	private static final String TAG = "AccLogger";
	private static final long SECOND_IN_NANO = 1000000000;
	private final int MAXHOURS = 8;
	private final int MILLISPERHOUR = 60 * 60 * 1000;

	// DICHIARAZIONE VARIABILI
	private SensorManager mSm = null;
	private AccelerometerData mMeasuredData;
	private float mCurrentAcceleration;
	private long mDuration;
	private long mStartDate;
	private long mLastFall;
	private long mLastFallNano;
	private static DBManager sDb;
	private static SharedPreferences sPreferences;
	private Session mCurrentSession;
	private long mTimePassed;
	private int mSensorDelay;
	private LinkedList<AccelerometerData> mSamples;
	private LinkedList<AccelerometerData> mFallSamples;
	private Intent mIntent;
	private boolean mTaskRunning;
	private int mSampleRate;
	private Fall mNewFall;
	private Context mContext;
	private int mFallNumber;
	private LocationManager mLocationManager;
	private LocationListener mLocationListener;
	private double mLatitude;
	private double mLongitude;
	private boolean mGotLocation;
	private boolean mStartTask;
	private boolean maxDurationReached;
	private long mEventTimestamp;
	private Context context;
	private Timer sTimer;
	private static boolean sPressedStop;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent.getBooleanExtra("Stop", false)) {
			// Se è stato premuto stop termino il service
			sPressedStop = true;
			stopSelf();
			return 0;
		}
		sPressedStop = false;
		context = this;
		sDb = new DBManager(getBaseContext());
		sDb.open();
		mCurrentSession = sDb.getActiveSession();
		mStartDate = System.currentTimeMillis();
		mDuration = mCurrentSession.getDuration();
		// Contatore delle cadute richiesto per migliorare le prestazioni,
		// sarebbe troppo costoso leggere ogni volta che registro una caduta nel
		// db il numero di cadute
		mFallNumber = (sDb.getAllFalls(mCurrentSession.getSessionBegin())
				.size());
		sDb.close();
		mLastFall = 0;
		mLastFallNano = 0;
		mTaskRunning = false;
		mStartTask = false;
		mSm = (SensorManager) getSystemService(SENSOR_SERVICE);
		mMeasuredData = new AccelerometerData(0, 0, 0, 0);
		Sensor Accel = mSm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		// Imposta il sensor delay in accordo con quanto deciso nelle opzioni.
		sPreferences = getApplicationContext().getSharedPreferences("MyPref",
				Context.MODE_PRIVATE);
		mSensorDelay = sPreferences.getInt("sensorDelay",
				SensorManager.SENSOR_DELAY_GAME);
		switch (mSensorDelay) {
		case 0:
			mSampleRate = FASTEST;
			break;
		case 1:
			mSampleRate = FAST; // 20000000
			break;
		case 2:
			mSampleRate = NORMAL; // 60000000
			break;
		case 3:
			mSampleRate = LOW; // 60000000
			break;
		}

		// Registro la classe come listener dell'accelerometro
		mSm.registerListener((SensorEventListener) this, Accel, mSensorDelay);
		mLocationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
		mLocationListener = new LocationListener() {

			public void onLocationChanged(Location location) {
				// Registrazione della posizione rilevata
				mLatitude = location.getLatitude();
				mLongitude = location.getLongitude();
				mGotLocation = true;
			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}

			public void onProviderEnabled(String provider) {
			}

			public void onProviderDisabled(String provider) {
			}
		};

		mSamples = new LinkedList<AccelerometerData>();
		mFallSamples = new LinkedList<AccelerometerData>();
		// Imposta la notifica persistente per evitare chiusure non intenzionali
		// del service
		runAsForeground();
		setDurationTimer();
		return Service.START_STICKY;
	}

	public void setDurationTimer() {
		sTimer = new Timer();
		sTimer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				mTimePassed = System.currentTimeMillis() - mStartDate;
				Intent mIntent = new Intent("updateDuration");
				mIntent.putExtra("duration", mDuration + mTimePassed);
				mIntent.putExtra("maxDurationReached", false);
				// Controlla se la durata massima è stata superata
				if ((mDuration + mTimePassed) > sPreferences.getInt(
						"maxDuration", MAXHOURS) * MILLISPERHOUR) {

					mIntent.putExtra("maxDurationReached", true);
					LocalBroadcastManager.getInstance(context).sendBroadcast(
							mIntent);
					maxDurationReached = true;
					if (sPreferences.getBoolean("CurrentSessionOnBackground",
							true)) {
						stopSelf();
					}
					return;
				}
				LocalBroadcastManager.getInstance(context).sendBroadcast(
						mIntent);
			}
		}, 0, 1000);

	}

	/**
	 * Imposta una notifica persistente per evitare chiusure non intenzionali
	 * del service.
	 */
	private void runAsForeground() {

		Intent notificationIntent = new Intent(this,
				CurrentSessionDetailsActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, Intent.FLAG_ACTIVITY_NEW_TASK);

		Notification persistentNotification = new NotificationCompat.Builder(
				this)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("Esp1415")
				.setContentText(
						"Controllando i dati dell'accelerometro per la rilevazione di cadute.")
				.setContentIntent(pendingIntent).build();

		startForeground(Utilities.PERSISTENT_NOTIFICATION_ID,
				persistentNotification);

	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		sTimer.cancel();
		stopForeground(true);
		// Rimuove il listener dell'accelerometro
		mSm.unregisterListener(this);
		// Controlla se esiste una sessione attiva, se non esiste vuol dire che
		// il service è stato fermato perchè è stata cancellata la sessione
		// attiva
		sDb.open();
		if (sDb.hasActiveSession()) {
			// Registra la durata finale nel db
			mTimePassed = System.currentTimeMillis() - mStartDate;
			mCurrentSession.setDuration(((Long) (mDuration + mTimePassed))
					.intValue());
			mCurrentSession = sDb.updateDuration(mCurrentSession);
			// Se il service si è terminato da solo a causa della pressione del
			// tasto stop disattivo la sessione
			if (sPressedStop) {
				mCurrentSession.setActive(false);
				sDb.setActiveSession(mCurrentSession);
			}
			// Caso in cui l'applicazione sia in background, o che l'utente
			// abbia cambiato activity
			if (sPreferences.getBoolean("CurrentSessionOnBackground", true)
					&& maxDurationReached) {
				mCurrentSession.setActive(false);
				sDb.setActiveSession(mCurrentSession);
				// Lancia una notifica per comunicare all'utente che la sessione
				// ha raggiunto la durata massima.
				setMaxDurationNotification();
			}
		}
		sDb.close();
		super.onDestroy();
	}

	/**
	 * Imposta una notifica che avverte l'utente che la sessione è stata
	 * terminata automaticamente a causa del raggiungimento della durata massima
	 * impostata nelle opzioni. Se l'utente tappa sulla notifica viene aperta il
	 * dettaglio della sessione passata riguardante la sessione terminata.
	 */
	private void setMaxDurationNotification() {
		Intent notificationIntent = new Intent(this,
				PastSessionDetailsActivity.class);
		notificationIntent.putExtra("IDSessione", mCurrentSession
				.getSessionBegin().getTime());
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		// Si configura la notifica con un messaggio di avviso all'utente
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("Esp1415")
				.setContentText(
						"La sessione ha raggiunto la durata massima ed è stata terminata automaticamente.")
				.setContentIntent(contentIntent).setAutoCancel(true)
				.setVibrate(Utilities.VIBRATION_PATTERN);

		// La notifica viene lanciata
		NotificationManager mNotificationManager = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(
				Utilities.MAX_DURATION_REACHED_NOTIFICATION_ID,
				mBuilder.build());
	}

	@SuppressWarnings("unchecked")
	public void onSensorChanged(SensorEvent event) {
		synchronized (this) {
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				mEventTimestamp = event.timestamp;
				mMeasuredData = new AccelerometerData(mEventTimestamp,
						event.values[0], event.values[1], event.values[2]);

				// Controlla che sia rispettato il sample rate impostato
				if (mSamples.size() > 0) {
					if (mEventTimestamp - mSamples.getLast().getTimestamp() < mSampleRate) {
						return;
					}
				}
				mSamples.add(mMeasuredData);
				// Se il dato in testa alla coda è piu vecchio di un secondo lo
				// scarta
				if (mEventTimestamp - mSamples.getFirst().getTimestamp() > SECOND_IN_NANO
						&& mSamples.getFirst() != null) {
					mSamples.remove();
				}

				// Dopo aver raccolto i 500ms di dati accelerometro dopo la
				// caduta la segnala
				if (mStartTask
						&& (mEventTimestamp - mLastFallNano > 500000000L)) {
					// Copia i dati dell'accelerometro relativi alla caduta in
					// una lista apposita
					Object tempSamples = mSamples.clone();
					if (tempSamples instanceof LinkedList<?>) {
						mFallSamples = (LinkedList<AccelerometerData>) tempSamples;
					}
					// Lancia la task per recuperare la posizione, mandare la
					// mail e registrare la caduta nel db
					new ProcessFallTask().execute("NEW FALL EVENT");
					mStartTask = false;
					mTaskRunning = true;

				}

				// Comunica i valori dell'accelerometro aggiornati al dettaglio
				// sessione corrente
				mIntent = new Intent("AccData");
				mIntent.putExtra("xValue", mMeasuredData.getX());
				mIntent.putExtra("yValue", mMeasuredData.getY());
				mIntent.putExtra("zValue", mMeasuredData.getZ());
				LocalBroadcastManager.getInstance(this).sendBroadcast(mIntent);

				if (fallDetection()
						&& (!mTaskRunning)
						&& !mStartTask
						&& (mEventTimestamp - mLastFallNano > MAX_TIME_OUT * 1000 * 1000)) {
					// Memorizza il timestamp della caduta
					mLastFallNano = mEventTimestamp;
					mLastFall = System.currentTimeMillis();
					mStartTask = true;
					Toast.makeText(getApplicationContext(),
							"caduta con accelerazione " + mCurrentAcceleration,
							Toast.LENGTH_LONG).show();
				}
			}
		}
	}

	/**
	 * Rileva una caduta in base ai dati dell'accelerometro.
	 * 
	 * @return vero se l'ha rilevata, falso altrimenti
	 */
	private Boolean fallDetection() {
		// Calcola il modulo dell'accelerazione-forza di gravità per
		// stimare una caduta
		float a = Math.round(Math.sqrt(Math.pow(mMeasuredData.getX(), 2)
				+ Math.pow(mMeasuredData.getY(), 2)
				+ Math.pow(mMeasuredData.getZ(), 2)));
		mCurrentAcceleration = Math.abs(a - SensorManager.STANDARD_GRAVITY);
		if (mCurrentAcceleration > 10) {
			return true;
		} else {
			return false;
		}
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		Log.d(TAG, "onAccuracyChanged: " + sensor + ", accuracy: " + accuracy);
	}

	/**
	 * Usa questo task cosi da risparmiare batteria, utilizzando il gps solo
	 * quando viene segnalata una caduta. Questo task è composto da queste fasi:
	 * 
	 * 1) in preExecute cerca di abilitare un listener ad un servizio di
	 * rilevazione della posizione;
	 * 
	 * 2) nella fase di elaborazione in background cerca di ottenere una
	 * posizione;
	 * 
	 * 3) nella fase di postExecute avvia il task che manda la mail di notifica;
	 * 
	 * 4) il task della mail chiamerà notificationUpdate comunicando se la mail
	 * è stata inviata con successo e la caduta viene registrata nel db.
	 */
	private class ProcessFallTask extends AsyncTask<String, Integer, Long>
			implements AsyncInterface {
		private int mSecondsPassed;
		private boolean mGpsEnabled;
		private boolean mNetworkEnabled;
		private boolean mNoPosition;

		protected void onPreExecute() {

			// Registra il listener cosi da avere aggiornamenti sulla posizione
			mLocationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
			mGpsEnabled = mLocationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);
			mNetworkEnabled = mLocationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if (mGpsEnabled) {
				mLocationManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
			} else if (mNetworkEnabled) {
				mLocationManager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER, 0, 0,
						mLocationListener);
			} else {
				// Imposta le variabili in modo da terminare subito il ciclo di
				// attesa di posizione
				mNoPosition = true;
			}
			mSecondsPassed = 0;
		}

		protected Long doInBackground(String... params) {
			Long result = 0L;
			if (mNoPosition) {
				return result;
			}
			// Cicla finché non ottiene una posizione dal gps o scade il timeout
			while (!mGotLocation && mSecondsPassed < MAX_TIME_OUT) {
				try {
					Thread.sleep(1000);
					mSecondsPassed++;

				} catch (InterruptedException e) {
					Thread.interrupted();
				}
			}

			return result;
		}

		protected void onPostExecute(Long result) {
			// Disabilita il listener del gps cosi da risparmiare batteria
			mLocationManager.removeUpdates(mLocationListener);
			// Registra la nuova caduta nel db
			mFallNumber++;
			if (sPreferences.getBoolean("notificationCheck", false)) {
				if (!sPreferences.getString("email", "").equals("")
						&& !sPreferences.getString("password", "").equals("")
						&& !Utilities.sDest.isEmpty()) {
					NotificationSender sender = new NotificationSender(
							sPreferences.getString("email", ""),
							sPreferences.getString("password", ""),
							Utilities.sDest, this);
					String date = (String) DateFormat.format("dd/MM/yy",
							new Date(mLastFall));
					String hours = (String) DateFormat.format("kk:mm:ss",
							new Date(mLastFall));
					String latitude;
					String longitude;
					// Gli if controllano se latitudine e longitudine sono
					// uguali a zero, in tal caso li impostano come N/A
					if (mLatitude == 0) {
						latitude = "N/A";
					} else {
						latitude = Double.toString(mLatitude);
					}
					if (mLongitude == 0) {
						longitude = "N/A";
					} else {
						longitude = Double.toString(mLongitude);
					}
					sender.buildMessage(date, hours, latitude, longitude);
					sender.execute();
				}
			} else {
				notificationUpdate(false);
			}
		}

		@Override
		public void notificationUpdate(Boolean notification) {
			sDb.open();
			// Registra la caduta nel db con i campi notified, latitude e
			// longitude impostati correttamente
			if (!mGotLocation) {
				mNewFall = sDb.createFall(new Date(mLastFall), mFallNumber,
						null, null, mFallSamples,
						mCurrentSession.getSessionBegin());
			} else {
				mNewFall = sDb.createFall(new Date(mLastFall), mFallNumber,
						mLatitude, mLongitude, mFallSamples,
						mCurrentSession.getSessionBegin());
			}
			if (notification) {
				mNewFall.setNotified();
				mNewFall = sDb.setNotified(mNewFall);
			}
			// Ripristina le variabili di controllo sull'ottenimento di una
			// posizione
			mNoPosition = false;
			mGotLocation = false;
			// Segnala a DettaglioSessioneCorrente la nuova caduta
			Intent intent = new Intent("Fall");
			intent.putExtra("IDFall", mNewFall.getFallTimestamp().getTime());
			mNewFall = null;
			LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
			mTaskRunning = false;
			sDb.close();
		}
	}

}
