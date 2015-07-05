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

		// Se non c'è già una sessione attiva, se il giorno corrente è quello in
		// cui visualizzare la prossima notifica, allora lancia la notifica.
		// La terza condizione serve per impedire che una notifica configurata
		// per il giorno corrente venga visualizzata anche nel caso si
		// riavviasse/accendesse il telefono prima dell'ora di visualizzazione
		// della notifica. Senza questo controllo si avrebbe una doppia
		// visualizzazione della notifica nello stesso giorno: subito dopo il
		// riavvio (se precendente all'ora prestabilita), e all'ora prestabilita
		if (!db.hasActiveSession()
				&& preferences.getInt("day", 0) == currentTime
						.get(Calendar.DAY_OF_MONTH)
				&& System.currentTimeMillis()
						- notificationTime.getTimeInMillis() >= 0) {
			Intent notificationIntent = new Intent(context,
					SessionListActivity.class);
			notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_CLEAR_TOP);
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
					notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

			// Si configura la notifica con un messaggio di avviso all'utente
			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
					context)
					.setSmallIcon(R.drawable.ic_launcher)
					.setContentTitle(
							context.getString(R.string.title_session_list_activity))
					.setContentText(
							context.getString(R.string.session_reminder))
					.setContentIntent(contentIntent).setAutoCancel(true)
					.setVibrate(Utilities.VIBRATION_PATTERN);

			// La notifica viene lanciata
			NotificationManager mNotificationManager = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.notify(Utilities.ALARM_NOTIFICATION_ID,
					mBuilder.build());
		}
		// Ad ogni (ri)avvio o visualizzazione della notifica, la notifica
		// stessa viene riconfigurata secondo l'orario scelto dall'utente.
		// Questa operazione è necessaria per fare in modo che la notifica venga
		// visualizzata al più una volta al giorno, e solo all'ora scelta
		Utilities.fireAlarm(context);
		db.close();
	}
}