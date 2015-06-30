package it.unipd.dei.esp1415;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;

/**
 * Classe contentenente varie variabili statiche e metodi di utilità utilizzati
 * dalle altre classi e activity.
 */
/**
 * @author Laura
 *
 */
public class Utilities {

	protected static final boolean ENABLED = true;
	protected static final boolean DISABLED = false;

	public static ArrayList<String> sDest = new ArrayList<String>();
	public static ArrayList<String> sName = new ArrayList<String>();

	protected static AlarmManager sAlarmMgr;
	protected static PendingIntent sAlarmIntent;
	protected static Calendar sCalendar;
	protected static SharedPreferences sPreferences;

	/**
	 * Restituisce l'array con gli indirizzi e-mail, letti da un file di testo,
	 * a cui inviare le notifiche.
	 * 
	 * @param context
	 * @return
	 */
	public static ArrayList<String> setSelectedContacts(Context context) {
		try {
			FileInputStream input = context.openFileInput("contactlist.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(input));

			String line;
			int i;
			while ((line = br.readLine()) != null) {
				for (i = 0; line.charAt(i) != ':'; i++)
					;
				sName.add(line.substring(0, i));
				sDest.add(line.substring(i + 2, line.length()));
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sDest;
	}

	public static int getSensorDelay(String sampleRate) {
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
	 * Configura ed imposta una notification di sistema all'orario specificato
	 * dall'utente.
	 * 
	 * @param time
	 */
	public static void fireAlarm(Context context) {

		Toast.makeText(context, "Allarme aggiunto", Toast.LENGTH_SHORT).show();
		sPreferences = context.getSharedPreferences("MyPref",
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sPreferences.edit();

		// Inizializza l'AlarmManager e chiama la classe d'appoggio per la
		// configurazione della notifica
		sAlarmMgr = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, AlarmReceiver.class);
		sAlarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

		// Se una notifica era già stata impostata in precedenza, viene
		// cancellata
		if (sAlarmMgr != null) {
			eraseAlarm(context);
		}

		sCalendar = Calendar.getInstance();
		sCalendar.set(Calendar.HOUR_OF_DAY, sPreferences.getInt("hour", 8));
		sCalendar.set(Calendar.MINUTE, sPreferences.getInt("minute", 0));
		sCalendar.set(Calendar.SECOND, 0);

		/*
		 * Se l'orario scelto è successivo all'orario attuale all'interno della
		 * giornata, si incrementa il giorno di uno per impedire che la notifica
		 * venga lanciata immediatamente.
		 */
		if (System.currentTimeMillis() - sCalendar.getTimeInMillis() > 0) {
			Toast.makeText(context, "fanculo", Toast.LENGTH_SHORT).show();
			sCalendar.add(Calendar.DAY_OF_MONTH, 1);
		}

		editor.putInt("day", sCalendar.get(Calendar.DAY_OF_MONTH));
		editor.putInt("hour", sCalendar.get(Calendar.HOUR_OF_DAY));
		editor.putInt("minute", sCalendar.get(Calendar.MINUTE));
		editor.commit();

		Toast.makeText(context,
				DateFormat.format("dd/MM/yy kk:mm:ss", sCalendar.getTime()),
				Toast.LENGTH_SHORT).show();

		// L'AlarmManager setta la notifica, che deve comparire ogni giorno
		// all'orario appena stabilito
		sAlarmMgr.set(AlarmManager.RTC, sCalendar.getTimeInMillis(),
				sAlarmIntent);
		sAlarmMgr.setRepeating(AlarmManager.RTC, sCalendar.getTimeInMillis(),
				AlarmManager.INTERVAL_DAY, sAlarmIntent);

		// Necessario perché le impostazioni della notifica persistano al
		// riavvio del dispositivo
		ComponentName receiver = new ComponentName(context, AlarmReceiver.class);
		PackageManager pm = context.getPackageManager();
		pm.setComponentEnabledSetting(receiver,
				PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
				PackageManager.DONT_KILL_APP);
	}

	/**
	 * Cancella la notifica di sistema precedentemente impostata, se presente.
	 * 
	 * @param context
	 */
	public static void eraseAlarm(Context context) {
		if (sAlarmMgr != null) {
			sAlarmMgr.cancel(sAlarmIntent);

			ComponentName receiver = new ComponentName(context,
					AlarmReceiver.class);
			PackageManager pm = context.getPackageManager();
			pm.setComponentEnabledSetting(receiver,
					PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
					PackageManager.DONT_KILL_APP);
		}
	}

	/**
	 * Salva nella memoria interna l'immagine data, associandole il nome scelto.
	 * 
	 * @param image
	 * @param name
	 * @param context
	 * @return
	 */
	public static boolean saveToInternalStorage(Bitmap image, String name,
			Context context) {
		try {
			// Crea la directory nell'archivio interno
			File myDir = context.getDir("Thumbnails", Context.MODE_PRIVATE);
			// Mette il file nella directory
			File fileWithinMyDir = new File(myDir, name);
			// Crea uno stream per scrivere nel file
			FileOutputStream out = new FileOutputStream(fileWithinMyDir);
			// Scrive la bitmap nello stream
			image.compress(Bitmap.CompressFormat.PNG, 100, out);
			out.close();
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Recupera dalla memoria interna il file col nome passatogli come parametro.
	 * 
	 * @param filename
	 * @param context
	 * @return
	 */
	public static Bitmap loadImageFromStorage(String filename, Context context) {
		Bitmap thumbnail = null;
		FileInputStream stream;
		try {
			// Genera il path del file richiesto
			String path = context.getDir("Thumbnails", Context.MODE_PRIVATE)
					+ "/" + filename;
			File file = new File(path);
			// Associa uno stream al file
			stream = new FileInputStream(file);
			// Crea la bitmap dallo stream
			thumbnail = BitmapFactory.decodeStream(stream);
			stream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return thumbnail;
	}

	/**
	 * Dato il timestamp della sessione crea una thumbnail associata.
	 * 
	 * @param sessionBegin
	 * @return
	 */
	public static Bitmap createThumbnail(Date sessionBegin) {
		long timestamp = sessionBegin.getTime();
		Random random = new Random();
		// Prende i quattro byte a sinistra del timestamp e ci aggiunge un numero casuale
		int a = (int) (timestamp >> 32) + random.nextInt();
		// Prende i quattro byte a destra del timestamp
		long rightDigits = timestamp & 0xffffffff;
		int b = (int) rightDigits;
		Bitmap.Config configuration = Bitmap.Config.ARGB_4444;
		// Crea un'immagine e la colora usando l'intero generato prima coi byte sinistri del timestamp
		Bitmap left = Bitmap.createBitmap(35, 70, configuration);
		left.eraseColor(a);
		// Crea un'immagine e la colora usando l'intero generato prima coi byte destri del timestamp
		Bitmap right = Bitmap.createBitmap(35, 70, configuration);
		right.eraseColor(b);
		// Crea un'immagine che sarà la fusione delle due immagini create precedentemente
		Bitmap thumbnail = Bitmap.createBitmap(70, 70, configuration);
		Canvas canvas = new Canvas(thumbnail);
		canvas.drawBitmap(left, null, new Rect(0, 0, canvas.getWidth() / 2,
				canvas.getHeight()), null);
		canvas.drawBitmap(right, null, new Rect(canvas.getWidth() / 2, 0,
				canvas.getWidth(), canvas.getHeight()), null);
		return thumbnail;
	}
	
	/**
	 * Converte da millisecondi a ore, minuti e secondi.
	 * 
	 * @param millis
	 * @param returnSeconds
	 * @return
	 */
	public static String millisToHourMinuteSecond(long millis, boolean returnSeconds) {
		String time = "";
		long seconds = millis / 1000;
		long minutes = seconds / 60;
		long hours = minutes / 60;
		minutes = minutes % 60;
		seconds = seconds - hours * 3600 - minutes * 60;
		if (returnSeconds == true) {
			time = hours + " h " + minutes + " m " + seconds + " s ";
			return time;
			}
		else{
			time = hours + " h " + minutes + " m ";
			return time;
			}
	}
}
