package it.unipd.dei.esp1415;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.text.format.DateFormat;
import android.widget.Toast;

public class SettingValues {


	protected static final boolean ENABLED = true;
	protected static final boolean DISABLED = false;

	protected static ArrayList<String> sDest = new ArrayList<String>();
	protected static ArrayList<String> sName = new ArrayList<String>();

	protected static AlarmManager sAlarmMgr;
	protected static PendingIntent sAlarmIntent;
	protected static Calendar sCalendar;
	protected static SharedPreferences sPreferences;

	protected static ArrayList<String> setSelectedContacts(Context context) {
		try {
			FileInputStream input = context.openFileInput("contactlist.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(input));
			String line;
			int i;
			while ((line = br.readLine()) != null) {
				for(i=0;line.charAt(i)!=':';i++); 
				sName.add(line.substring(0, i));
				sDest.add(line.substring(i+2,line.length()));
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sDest;
	}

	protected static int getSensorDelay(String sampleRate){
		if (sampleRate.equals("Molto alta")) {
			return SensorManager.SENSOR_DELAY_FASTEST;
		} else if (sampleRate.equals("Alta")) {
			return SensorManager.SENSOR_DELAY_GAME;
		} else if (sampleRate.equals("Normale")) {
			return SensorManager.SENSOR_DELAY_NORMAL;
		} else {
			return SensorManager.SENSOR_DELAY_UI;
		}
	}
	/**
	 * Configura ed imposta una notification di sistema all'orario specificato dall'utente.
	 * @param time
	 */
	protected static void fireAlarm(Context context){

		Toast.makeText(context, "Allarme aggiunto", Toast.LENGTH_SHORT).show();
		sPreferences = context.getSharedPreferences("MyPref", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sPreferences.edit();

		//Inizializza l'AlarmManager e chiama la classe d'appoggio per la configurazione della notifica
		sAlarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, AlarmReceiver.class);
		sAlarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

		//Se una notifica era già stata impostata in precedenza, viene cancellata
		if (sAlarmMgr!= null)
			eraseAlarm(context);

		sCalendar = Calendar.getInstance();
		sCalendar.set(Calendar.HOUR_OF_DAY, sPreferences.getInt("hour", 8));
		sCalendar.set(Calendar.MINUTE, sPreferences.getInt("minute", 0));
		sCalendar.set(Calendar.SECOND, 0);

		/* Se l'orario scelto è successivo all'orario attuale all'interno della giornata, si incrementa il giorno di uno
		 * per impedire che la notifica venga lanciata immediatamente.
		 */
		if(System.currentTimeMillis()-sCalendar.getTimeInMillis()>0){
			Toast.makeText(context, "fanculo", Toast.LENGTH_SHORT).show();
			sCalendar.add(Calendar.DAY_OF_MONTH, 1);
		} 

		editor.putInt("day", sCalendar.get(Calendar.DAY_OF_MONTH));
		editor.putInt("hour", sCalendar.get(Calendar.HOUR_OF_DAY));
		editor.putInt("minute", sCalendar.get(Calendar.MINUTE));
		editor.commit();

		Toast.makeText(context, DateFormat.format("dd/MM/yy kk:mm:ss",sCalendar.getTime()), Toast.LENGTH_SHORT).show();

		//L'AlarmManager setta la notifica, che deve comparire ogni giorno all'orario appena stabilito
		sAlarmMgr.set(AlarmManager.RTC, sCalendar.getTimeInMillis(), sAlarmIntent);
		sAlarmMgr.setRepeating(AlarmManager.RTC, sCalendar.getTimeInMillis(),
				AlarmManager.INTERVAL_DAY, sAlarmIntent);

		//Necessario perché le impostazioni della notifica persistano al riavvio del dispositivo
		ComponentName receiver = new ComponentName(context, AlarmReceiver.class);
		PackageManager pm = context.getPackageManager();
		pm.setComponentEnabledSetting(receiver,
				PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
				PackageManager.DONT_KILL_APP);
	}
	
	/**
	 * Cancella la notifica di sistema precedentemente impostata, se presente
	 */
	protected static void eraseAlarm(Context context){
		if(sAlarmMgr!=null){
			sAlarmMgr.cancel(sAlarmIntent);

			ComponentName receiver = new ComponentName(context, AlarmReceiver.class);
			PackageManager pm = context.getPackageManager();
			pm.setComponentEnabledSetting(receiver,
					PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
					PackageManager.DONT_KILL_APP);
		}
	}
}
