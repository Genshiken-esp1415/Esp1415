package it.unipd.dei.esp1415;

import java.util.ArrayList;
import java.util.Date;

import android.app.Service;
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
	private static DBManager db;
	private Session currentSession;

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
		lastFall = 0;
		sm = (SensorManager)getSystemService(SENSOR_SERVICE);
		measuredData = new AccelerometerData(0,0,0);
		Sensor Accel = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		
		// register this class as a listener for the accelerometer sensor
		sm.registerListener((SensorEventListener) this, Accel, SensorManager.SENSOR_DELAY_UI);
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
		 currentSession.setDuration(((Long)duration).intValue());
		 db.updateDuration(currentSession);
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
				
				measuredData.setX(event.values[0]);
				measuredData.setY(event.values[1]);
				measuredData.setZ(event.values[2]);
				Intent intent=new Intent("AccData");
				intent.putExtra("xValue",measuredData.getX());
				intent.putExtra("yValue",measuredData.getY());
				intent.putExtra("zValue",measuredData.getZ());
				intent.putExtra("duration",duration+((new Date()).getTime()-startDate.getTime()));
				LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
				float a = Math.round(Math.sqrt(Math.pow(measuredData.getX(),2)+Math.pow(measuredData.getY(),2)+ Math.pow(measuredData.getZ(),2)));
				currentAcceleration = Math.abs(a-CALIBRATION);
				if ((currentAcceleration > 10) && 
						(-lastFall+(new Date()).getTime()>1000))
					{
					lastFall = (new Date()).getTime();
					Toast.makeText(getApplicationContext(), "caduta con accelerazione "+currentAcceleration,
							Toast.LENGTH_LONG).show();
					//scrivo la caduta nel database
					int fallNumber =(db.getAllFalls(currentSession.getSessionBegin()).size());
					Fall fall = db.createFall(fallNumber, 1, 1, new ArrayList<AccelerometerData>(), currentSession.getSessionBegin());
					intent=new Intent("Fall");
					intent.putExtra("IDFall",fall.getFallTimestamp().getTime());
					LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
				}

			}

		}
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{
		Log.d(tag,"onAccuracyChanged: " + sensor + ", accuracy: " + accuracy);
	}


}
