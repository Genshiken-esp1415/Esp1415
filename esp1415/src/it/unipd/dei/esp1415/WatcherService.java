package it.unipd.dei.esp1415;
import java.util.Date;
import java.util.LinkedList;

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
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

/**
 * descrivi sta cazzo di classe
 * Saluti,
 * 
 * Marco
 *
 */
public class WatcherService extends Service implements SensorEventListener {
	// TODO sistema le variabili con private davanti o quel che è, se le metti
	// public togli la m davanti al nome
	final float CALIBRATION = SensorManager.STANDARD_GRAVITY;
	final String mTag = "AccLogger";
	SensorManager mSm = null;
	AccelerometerData mMeasuredData;
	private float mCurrentAcceleration;
	private long mDuration;
	private Date mStartDate;
	private long mLastFall;
	private long mLastFallNano;
	private static DBManager sDb;
	private static SharedPreferences sPreferences;
	private Session mCurrentSession;
	long mTimePassed;
	private int mSensorDelay;
	private LinkedList<AccelerometerData> mSamples;
	private LinkedList<AccelerometerData> mFallSamples;
	int mSampleMaxSize;
	private Intent mIntent;
	private boolean mTaskRunning;
	private int mSampleRate;
	private Fall mNewFall;
	private Context mContext;
	private int mFallNumber;
	private LocationManager mLocationManager;
	private LocationListener mLocationListener;
	protected double mLatitude;
	protected double mLongitude;
	protected boolean mGotLocation;
	private boolean mStartTask;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(getApplicationContext(), "service avviato",
				Toast.LENGTH_LONG).show();
		sDb = new DBManager(getBaseContext());
		sDb.open();
		mCurrentSession = sDb.getActiveSession();
		// inizializzo startTime con la data di inizio sessione + la durata già
		// trascorsa (necessario se è stata messa in pausa la sessione)
		mStartDate = new Date();
		mDuration = mCurrentSession.getDuration();
		mFallNumber = (sDb.getAllFalls(mCurrentSession.getSessionBegin())
				.size());
		mLastFall = 0;
		mLastFallNano = 0;
		mTaskRunning = false;
		mStartTask = false;
		mSm = (SensorManager) getSystemService(SENSOR_SERVICE);
		mMeasuredData = new AccelerometerData(0, 0, 0, 0);
		Sensor Accel = mSm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		// Imposto il sensor delay
		sPreferences = getApplicationContext().getSharedPreferences("MyPref",
				Context.MODE_PRIVATE);
		mSensorDelay = sPreferences.getInt("sensorDelay",
				SensorManager.SENSOR_DELAY_GAME);
		// Imposto il samplerate a seguito del sensor delay scelto
		switch (mSensorDelay) {
		case 0:
			mSampleRate = 0;
			break;
		case 1:
			mSampleRate = 20000000;
			break;
		case 2:
			mSampleRate = 60000000;
			break;
		case 3:
			mSampleRate = 200000000;
			break;
		}
		mSamples = new LinkedList<AccelerometerData>();
		mFallSamples = new LinkedList<AccelerometerData>();
		mSampleMaxSize = 1000000 / ((mSensorDelay + 1) * 2);
		// Registro la classe come listener dell'accelerometro
		mSm.registerListener((SensorEventListener) this, Accel, mSensorDelay);
		// Acquire a reference to the system Location Manager
		mLocationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
		mLocationListener = new LocationListener() {

			public void onLocationChanged(Location location) {
				// Called when a new location is found by the gps.
				mLatitude = location.getLatitude();
				mLongitude = location.getLongitude();
				// Imposto la variabile di controllo sull'ottenimento della
				// posizione
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
		return Service.START_STICKY;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		Toast.makeText(getApplicationContext(), "service ucciso",
				Toast.LENGTH_LONG).show();
		// Rimuovo i listener
		mSm.unregisterListener(this);
		// Registro la durata finale nel db
		mTimePassed = System.currentTimeMillis() - mStartDate.getTime();
		mCurrentSession.setDuration(((Long) (mDuration + mTimePassed))
				.intValue());
		mCurrentSession = sDb.updateDuration(mCurrentSession);
		sDb.close();
		super.onDestroy();
	}

	public void onSensorChanged(SensorEvent event) {
		// Java's synchronized keyword is used to ensure mutually exclusive
		// access to the sensor. See also
		// http://download.oracle.com/javase/tutorial/essential/concurrency/locksync.html
		synchronized (this) {
			// The SensorEvent object holds informations such as
			// the sensor's type, the time-stamp, accuracy and of course
			// the sensor's data.
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				Long timestamp = event.timestamp;
				// Controllo che sia rispettato il sample rate impostato
				if (mSamples.size() > 0) {
					if (timestamp - mSamples.getLast().getTimestamp() < mSampleRate) {
						return;
					}
				}
				// Gestisco la memorizzazione dei dati su coda
				mMeasuredData = new AccelerometerData(timestamp,
						event.values[0], event.values[1], event.values[2]);
				mSamples.add(mMeasuredData);
				// Se il dato in testa alla coda � piu vecchio di un secondo lo
				// scarto
				if (timestamp - mSamples.getFirst().getTimestamp() > 1000000000
						&& mSamples.getFirst() != null) {
					mSamples.remove();
				}
				// Se sono passati almeno 5 secondi dall'ultima caduta la
				// segnalo
				if (mStartTask && (timestamp - mLastFallNano > 5000000000L)) {
					// Copio i dati dell'accelerometro relativi alla caduta in
					// una lista apposita
					if(mSamples instanceof LinkedList<?>) {
								mFallSamples = (LinkedList<AccelerometerData>) mSamples.clone();
					}
					// Lancio la task per recuperare la posizione, mandare la
					// mail e registrare la caduta nel db
					new ProcessFallTask().execute("NEW FALL EVENT");
					mStartTask = false;
					mTaskRunning = true;

				}
				mTimePassed = System.currentTimeMillis() - mStartDate.getTime();
				// Mando i valori dell'accelerometro aggiornati al dettaglio
				// sessione corrente
				mIntent = new Intent("AccData");
				mIntent.putExtra("xValue", mMeasuredData.getX());
				mIntent.putExtra("yValue", mMeasuredData.getY());
				mIntent.putExtra("zValue", mMeasuredData.getZ());
				mIntent.putExtra("duration", mDuration + mTimePassed);
				LocalBroadcastManager.getInstance(this).sendBroadcast(mIntent);
				// Calcolo il modulo dell'accelerazione-forza di gravit� per
				// stimare una caduta
				float a = Math
						.round(Math.sqrt(Math.pow(mMeasuredData.getX(), 2)
								+ Math.pow(mMeasuredData.getY(), 2)
								+ Math.pow(mMeasuredData.getZ(), 2)));
				mCurrentAcceleration = Math.abs(a - CALIBRATION);
				if ((mCurrentAcceleration > 10) && (!mTaskRunning)
						&& !mStartTask) {
					// Memorizzo il timestamp della caduta
					mLastFallNano = timestamp;
					mLastFall = System.currentTimeMillis();
					mStartTask = true;
					Toast.makeText(getApplicationContext(),
							"caduta con accelerazione " + mCurrentAcceleration,
							Toast.LENGTH_LONG).show();
				}
			}
		}
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		Log.d(mTag, "onAccuracyChanged: " + sensor + ", accuracy: " + accuracy);
	}

	// Uso questo task cosi da risparmiare batteria, utilizzando il gps solo
	// quando viene segnalata una caduta
	private class ProcessFallTask extends AsyncTask<String, Integer, Long>
			implements AsyncInterface {
		private final int MAX_LOCATION_WAIT = 20;

		private int mSecondsPassed;
		private boolean mGpsEnabled;
		private boolean mNetworkEnabled;
		private boolean mNoPosition;

		protected void onPreExecute() {

			// Registro il listener cosi da avere aggiornamenti sulla posizione
			mLocationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
			mGpsEnabled = mLocationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);
			mNetworkEnabled = mLocationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if (!mGpsEnabled && !mNetworkEnabled) {
				Context context = getApplicationContext();
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(context, "nothing is enabled",
						duration);
				toast.show();

			}

			if (mGpsEnabled) {
				mLocationManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
			} else if (mNetworkEnabled) {
				mLocationManager.requestLocationUpdates(
						LocationManager.NETWORK_PROVIDER, 0, 0,
						mLocationListener);
			} else {
				mSecondsPassed = MAX_LOCATION_WAIT;
				mNoPosition = true;
			}
			mSecondsPassed = 0;
		}

		protected Long doInBackground(String... params) {
			Long result = 0L;
			if (mNoPosition) {
				return result;
			}
			;
			// Ciclo finché non ottengo una posizione dal gps
			while (!mGotLocation && mSecondsPassed < 120) {
				try {
					Thread.sleep(1000);
					mSecondsPassed++;

				} catch (InterruptedException e) {
					Thread.interrupted();
				}
			}

			return result;
		}

		// The onPostexecute method receives the return type of doInBackGround()
		protected void onPostExecute(Long result) {
			// Tolgo il listener cosi da risparmiare batteria
			mLocationManager.removeUpdates(mLocationListener);
			// Ripristino la variabile di controllo sull'ottenimento di una
			// posizione
			// Registro la nuova caduta nel db
			mFallNumber++;
			if (sPreferences.getBoolean("notificationCheck", false)) {
				if (!sPreferences.getString("email", "").equals("")
						&& !sPreferences.getString("password", "").equals("")
						&& !Utilities.sDest.isEmpty()) {
					NotificationSender sender = new NotificationSender(
							sPreferences.getString("email", ""),
							sPreferences.getString("password", ""),
							Utilities.sDest, this);
					sender.buildMessage(
							DBManager.dateToSqlDate(new Date(mLastFall)),
							"14:45:05", Double.toString(mLatitude),
							Double.toString(mLongitude));
					sender.execute();
				}
			} else {
				notificationUpdate(false);
			}
		}

		@Override
		public void notificationUpdate(Boolean notification) {

			if (!mGotLocation) {
				mNewFall = sDb.createFall(new Date(mLastFall), mFallNumber,
						null, null, mFallSamples,
						mCurrentSession.getSessionBegin());
				// TODO cercare di sostituire 0,0 in lat e long con n/a nei vari
				// oggetti
			} else {
				mNewFall = sDb.createFall(new Date(mLastFall), mFallNumber,
						mLatitude, mLongitude, mFallSamples,
						mCurrentSession.getSessionBegin());
			}
			if (notification) {
				mNewFall.setNotified();
				mNewFall = sDb.setNotified(mNewFall);
			}
			mNoPosition = false;
			mGotLocation = false;
			// Segnalo a DettaglioSessioneCorrente la nuova caduta
			Intent intent = new Intent("Fall");
			intent.putExtra("IDFall", mNewFall.getFallTimestamp().getTime());
			mNewFall = null;
			LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
			mTaskRunning = false;
		}
	}

}
