package it.unipd.dei.esp1415;

import java.util.Date;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
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
	private boolean stored;
	private int sampleRate;
	private Object lock = new Object();
	private Fall newFall;
	private Context context;
	private int fallNumber;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
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
		stored = true;
		sm = (SensorManager)getSystemService(SENSOR_SERVICE);
		measuredData = new AccelerometerData(0,0,0,0);
		Sensor Accel = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		//Imposto il sensor delay
		sensorDelay = SensorManager.SENSOR_DELAY_UI;
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
		context = this.getApplicationContext();
//		Timer FallUpdateTimer = new Timer("FallTimer");
//		FallUpdateTimer.scheduleAtFixedRate(new TimerTask() {
//			public void run() {
//				
//			}
//		},0,1000);
		return Service.START_STICKY;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		Toast.makeText(getApplicationContext(), "service ucciso",
				Toast.LENGTH_LONG).show();
		 sm.unregisterListener(this);
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
				//se sono passati 500 ms dall'ultima caduta la segnalo e scrivo i dati sul db
				 
				if (timestamp-lastFallNano>500000000&&!stored){
					stored=true;
					//scrivo la caduta nel database
					Toast.makeText(getApplicationContext(), "scrittura nel db",
							Toast.LENGTH_LONG).show();
					//fallNumber =(db.getAllFalls(currentSession.getSessionBegin()).size());
					fallNumber ++;
					newFall = db.createFall(new Date(lastFall),fallNumber, 1, 1, samples, currentSession.getSessionBegin());


					Intent intent=new Intent("Fall");
					intent.putExtra("IDFall",newFall.getFallTimestamp().getTime());
					newFall = null;
					LocalBroadcastManager.getInstance(context).sendBroadcast(intent);


				}
				
				
				timePassed = System.currentTimeMillis()-startDate.getTime();
				intent=new Intent("AccData");
				intent.putExtra("xValue",measuredData.getX());
				intent.putExtra("yValue",measuredData.getY());
				intent.putExtra("zValue",measuredData.getZ());
				intent.putExtra("duration",duration+timePassed);
				LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
				float a = Math.round(Math.sqrt(Math.pow(measuredData.getX(),2)+Math.pow(measuredData.getY(),2)+ Math.pow(measuredData.getZ(),2)));
				currentAcceleration = Math.abs(a-CALIBRATION);
				if ((currentAcceleration > 10) && 
						(stored))
					{
					
					//memorizzo il timestamp della caduta
					lastFallNano = timestamp;
					lastFall = System.currentTimeMillis();
					stored = false;
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


}
