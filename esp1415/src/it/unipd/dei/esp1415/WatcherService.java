package it.unipd.dei.esp1415;

import java.util.Date;
import java.util.LinkedList;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

public class WatcherService extends Service implements SensorEventListener{


	final float CALIBRATION = SensorManager.STANDARD_GRAVITY;
	final String tag = "AccLogger";
	SensorManager sm = null;
	AccelerometerData measuredData;
	private float currentAcceleration;
	private long duration;
	private Date startDate;
	private long lastFall;
	private long lastFallNano;
	private static DBManager db;
	private Session currentSession;
	long timePassed;
	private int sensorDelay;
	private LinkedList<AccelerometerData> samples; 
	int sampleMaxSize;
	private Intent intent;
	private boolean taskRunning;
	private int sampleRate;
	private Fall newFall;
	private Context context;
	private int fallNumber;
	private LocationManager locationManager;
	private LocationListener locationListener;
	protected double latitude;
	protected double longitude;
	protected boolean gotLocation;
	private boolean startTask;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(getApplicationContext(), "service avviato",
				Toast.LENGTH_LONG).show();
		db = new DBManager(getBaseContext());
		db.open();	
		currentSession = db.getActiveSession();
		// inizializzo startTime con la data di inizio sessione + la durata già
		// trascorsa (necessario se è stata messa in pausa la sessione)
		startDate = new Date();
		duration = currentSession.getDuration();
		fallNumber = (db.getAllFalls(currentSession.getSessionBegin()).size());
		lastFall = 0;
		lastFallNano = 0;
		taskRunning = false;
		startTask = false;
		sm = (SensorManager)getSystemService(SENSOR_SERVICE);
		measuredData = new AccelerometerData(0,0,0,0);
		Sensor Accel = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		//Imposto il sensor delay
		sensorDelay = SensorManager.SENSOR_DELAY_FASTEST;
		//imposto il samplerate a seguito del sensor delay scelto
		switch (sensorDelay){
		case 0: sampleRate = 0; break;
		case 1: sampleRate = 20000000; break;
		case 2: sampleRate = 60000000; break;
		case 3: sampleRate = 200000000; break;
		}
		samples = new LinkedList<AccelerometerData>();
		sampleMaxSize = 1000000/((sensorDelay+1)*2);
		// registro la classe come listener dell'accelerometro
		sm.registerListener((SensorEventListener) this, Accel, sensorDelay);
		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		locationListener = new LocationListener() {


	    	public void onLocationChanged(Location location) {
	    		// Called when a new location is found by the gps.
	    		latitude = location.getLatitude();
	    		longitude = location.getLongitude();
	    		//imposto la variabile di controllo sull'ottenimento della posizione
	    		gotLocation = true;
	    	}

	    	public void onStatusChanged(String provider, int status, Bundle extras) {}

	    	public void onProviderEnabled(String provider) {}

	    	public void onProviderDisabled(String provider) {}
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
		sm.unregisterListener(this);		
		//registro la durata finale nel db
		timePassed = System.currentTimeMillis()-startDate.getTime();
		currentSession.setDuration(((Long)(duration + timePassed)).intValue());
		currentSession = db.updateDuration(currentSession);
		db.close();
		super.onDestroy();
	}

	public void onSensorChanged(SensorEvent event)
	{
		// Java's synchronized keyword is used to ensure mutually exclusive
		// access to the sensor. See also
		// http://download.oracle.com/javase/tutorial/essential/concurrency/locksync.html
		synchronized(this)
		{
			// The SensorEvent object holds informations such as
			// the sensor's type, the time-stamp, accuracy and of course
			// the sensor's data.
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
			{
				Long timestamp = event.timestamp;
				//controllo che sia rispettato il sample rate impostato
				if(samples.size()>0){
					if(timestamp-samples.getLast().getTimestamp()<sampleRate){
						return;
					}
				}
				//gestisco la memorizzazione dei dati su coda
				measuredData = new AccelerometerData(timestamp,event.values[0],event.values[1],event.values[2]);
				samples.add(measuredData);
				//se il dato in testa alla coda è piu vecchio di un secondo lo scarto
				if(timestamp-samples.getFirst().getTimestamp()>1000000000 && samples.getFirst()!=null){
					samples.remove();
				}
				//se sono passati almeno 5 secondi dall'ultima caduta la segnalo
				if (startTask){					
					//lancio la task per recuperare la posizione, mandare la mail e registrare la caduta nel db
					new ProcessFallTask().execute("NEW FALL EVENT"); 
					startTask = false;
					taskRunning = true;
					
				}
				timePassed = System.currentTimeMillis()-startDate.getTime();
				//mando i valori dell'accelerometro aggiornati al dettaglio sessione corrente
				intent=new Intent("AccData");
				intent.putExtra("xValue",measuredData.getX());
				intent.putExtra("yValue",measuredData.getY());
				intent.putExtra("zValue",measuredData.getZ());
				intent.putExtra("duration",duration+timePassed);
				LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
				//calcolo il modulo dell'accelerazione-forza di gravità per stimare una caduta
				float a = Math.round(Math.sqrt(Math.pow(measuredData.getX(),2)+Math.pow(measuredData.getY(),2)+ Math.pow(measuredData.getZ(),2)));
				currentAcceleration = Math.abs(a-CALIBRATION);
				if ((currentAcceleration > 10) && 
						(!taskRunning))
				{
					//memorizzo il timestamp della caduta
					lastFallNano = timestamp;
					lastFall = System.currentTimeMillis();
					startTask = true;
					Toast.makeText(getApplicationContext(), "caduta con accelerazione "+currentAcceleration,
							Toast.LENGTH_LONG).show();
				}
			}
		}
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{
		Log.d(tag,"onAccuracyChanged: " + sensor + ", accuracy: " + accuracy);
	}
	
	//uso questo task cosi da risparmiare batteria, utilizzando il gps solo quando viene segnalata una caduta
	private class ProcessFallTask extends AsyncTask<String, Integer, Long> {
		 protected void onPreExecute(){
			 
			 // registro il listener cosi da avere aggiornamenti sulla posizione
			    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0, locationListener);
		 }
		 
		 protected Long doInBackground(String... params) {
		    //ciclo finchè non ottengo una posizione dal gps
		    while(!gotLocation){
		    	try {
		    		Thread.sleep(1000);
		    	} catch (InterruptedException e) {
		    		Thread.interrupted();
		    	}
		    }
		    Long result = 0L;
		    return result;
		 }

		 // the onPostexecute method receives the return type of doInBackGround()
		 protected void onPostExecute(Long result) {
			 //tolgo il listener cosi da risparmiare batteria
			 locationManager.removeUpdates(locationListener);
			 //ripristino la variabile di controllo sull'ottenimento di una posizione
			 gotLocation = false;
			 //registro la nuova caduta nel db
			 fallNumber ++;
			 newFall = db.createFall(new Date(lastFall),fallNumber, latitude, longitude, samples, currentSession.getSessionBegin());
			
			 //segnalo a DettaglioSessioneCorrente la nuova caduta
			 Intent intent=new Intent("Fall");
			 intent.putExtra("IDFall",newFall.getFallTimestamp().getTime());
			 newFall = null;
			 LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
			 taskRunning = false;
		 }
	}

}
