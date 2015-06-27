package it.unipd.dei.esp1415;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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
		//Controllo necessario per rendere persistente la notifica dopo un riavvio del dispositivo
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
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
			
			NotificationManager mNotificationManager =
					(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.notify(0, mBuilder.build());
			Toast.makeText(context, "Alarm received!", Toast.LENGTH_LONG).show();
		}
	}
}
