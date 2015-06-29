package it.unipd.dei.esp1415;

import java.util.Calendar;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

/**
 * Classe di appoggio per l'impostazione della notifica di sistema. Lancia una notifica all'ora stabilita
 * avvisando l'utente di iniziare la registrazione.
 * @author Marco
 */
public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		DBManager db = new DBManager(context);

		db.open();
		SharedPreferences preferences = context.getSharedPreferences("MyPref", Context.MODE_PRIVATE); 

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, preferences.getInt("hour",8));
		calendar.set(Calendar.MINUTE, preferences.getInt("minute",0));
		calendar.set(Calendar.DAY_OF_MONTH, preferences.getInt("day", 0));
		calendar.set(Calendar.SECOND, 0);

		if (db.hasActiveSession() || (preferences.getInt("day", 0) == SettingValues.sCalendar.get(Calendar.DAY_OF_MONTH)) 
				&& (System.currentTimeMillis()-calendar.getTimeInMillis())>5000) {
			SettingValues.fireAlarm(context);
		} else {
			Intent notificationIntent = new Intent(context, OpzioniActivity.class);
			PendingIntent contentIntent = PendingIntent.getActivity(context,
					0, notificationIntent,
					PendingIntent.FLAG_CANCEL_CURRENT);

			//Si configura la notifica con un messaggio di avviso all'utente
			NotificationCompat.Builder mBuilder =
					new NotificationCompat.Builder(context)
			.setSmallIcon(R.drawable.ic_launcher)
			.setContentTitle("Esp1415")
			.setContentText("Ricordati di iniziare la registrazione")
			.setContentIntent(contentIntent)
			.setAutoCancel(true);

			SharedPreferences.Editor editor = preferences.edit();
			editor.putInt("day", SettingValues.sCalendar.get(Calendar.DAY_OF_MONTH));
			editor.commit();

			NotificationManager mNotificationManager =
					(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.notify(0, mBuilder.build());
		}
	}
}
