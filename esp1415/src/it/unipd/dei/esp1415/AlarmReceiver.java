package it.unipd.dei.esp1415;

import java.util.Calendar;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;

/**
 * Classe di appoggio per l'impostazione della notifica di sistema. Lancia una
 * notifica all'ora stabilita, avvisando l'utente di iniziare la registrazione.
 * La notifica si ripete ogni 24 ore.
 */
public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		DBManager db = new DBManager(context);

		db.open();
		SharedPreferences preferences = context.getSharedPreferences("MyPref",
				Context.MODE_PRIVATE);
		
		// Orario corrente
		Calendar currentTime = Calendar.getInstance();
		
		// Orario della notifica
		Calendar notificationTime = Calendar.getInstance();
		notificationTime.set(Calendar.HOUR_OF_DAY,
				preferences.getInt("hour", 8));
		notificationTime.set(Calendar.MINUTE, preferences.getInt("minute", 0));
		notificationTime.set(Calendar.DAY_OF_MONTH,
				preferences.getInt("day", 0));
		notificationTime.set(Calendar.SECOND, 0);

		// Se c'è già una sessione attiva, oppure se l'orario di visualizzazione
		// della notifica è già trascorso ma non è stato possibile inviarla,
		// viene impostata una notifica alla stessa ora del giorno successivo. I
		// 5000ms impostati nel confronto servono per fornire all'applicazione
		// un margine abbondantemente sufficiente per l'invio della notifica.
		// Con un valore di tempo molto più basso (per esempio nullo)
		// rischierebbe di non essere visualizzata ma posticipata alla stessa
		// ora del giorno successivo
		if (db.hasActiveSession()
				|| ((preferences.getInt("day", 0) == currentTime
						.get(Calendar.DAY_OF_MONTH) && (System
						.currentTimeMillis() - notificationTime
						.getTimeInMillis()) > 5000))) {
			Utilities.fireAlarm(context);
		} else {
			Intent notificationIntent = new Intent(context,
					SessionListActivity.class);
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
					notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

			// Si configura la notifica con un messaggio di avviso all'utente
			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
					context).setSmallIcon(R.drawable.ic_launcher)
					.setContentTitle("Esp1415")
					.setContentText("Ricordati di creare una nuova sessione.")
					.setContentIntent(contentIntent).setAutoCancel(true)
					.setVibrate(Utilities.VIBRATION_PATTERN);

			// Aggiorna il giorno in cui è stata visualizzata la notifica,
			// necessario perché l'if di controllo definito sopra funzioni come
			// correttamente così come descritto
			SharedPreferences.Editor editor = preferences.edit();
			editor.putInt("day", currentTime.get(Calendar.DAY_OF_MONTH));
			editor.commit();

			// La notifica viene lanciata
			NotificationManager mNotificationManager = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.notify(Utilities.ALARM_NOTIFICATION_ID,
					mBuilder.build());
		}
		db.close();
	}
}