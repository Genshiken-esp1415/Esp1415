package it.unipd.dei.esp1415;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.PopupMenu;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

/**
 * Activity per la scelta delle opzioni e dei contatti a cui spedire le notifiche
 * @author Marco
 */
public class OpzioniActivity extends ActionBarActivity {

	//Dichiarazione variabili delle view dell'activity, i nomi sono autoesplicativi
	private static Button alarmButton;
	private static Button sampleRateButton;
	private static TextView maxDuration;
	private static PendingIntent alarmIntent;
	private static CheckBox alarm;
	private static CheckBox notification;
	private static EditText email;
	private static EditText password;

	private static Context context;
	private static ArrayAdapter<String> arrayAdapter;
	private static AlarmManager alarmMgr;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_opzioni);
		context = this;
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
			.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.opzioni, menu);
		return true;
	}

	/**
	 * Gestisce la azioni da compiere quando cambia lo stato della checkbox della sveglia. Viene eseguito
	 * il metodo per l'impostazione (o la rimozione, a seconda) della notification di sistema all'ora specificata
	 * dall'utente.
	 */
	public void onCheckboxClicked(View view) {
		boolean checked = ((CheckBox) view).isChecked();

		switch(view.getId()) {
		case R.id.sveglia_checkbox:
			if (checked)
				fireAlarm((String) alarmButton.getText());
			else
				eraseAlarm();
			break;
			/*case R.id.notification_checkbox:
	            if (checked)
	            else
	            break;*/
		}
	}


	/**
	 * Configura ed imposta una notification di sistema all'orario specificato dall'utente.
	 * @param time
	 */
	private static void fireAlarm(String time){
		Toast.makeText(context, "Allarme aggiunto", Toast.LENGTH_SHORT).show();
		
		//Inizializza l'AlarmManager e chiama la classe d'appoggio per la configurazione della notifica
		alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(context, AlarmReceiver.class);
		alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		
		//Se una notifica era già stata impostata in precedenza, viene cancellata
		if (alarmMgr!= null)
			eraseAlarm();
		
		Calendar calendar = Calendar.getInstance();

		//Si costruisce la stringa corrispondente all'orario a cui visualizzare la notifica, nel formato hh:mm:ss
		String buf = "";
		int i=0;
		for(;time.charAt(i)!=':';i++)
			buf += time.charAt(i);
		calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(buf));
		i++;
		buf = "";
		for(;i<time.length();i++)
			buf += time.charAt(i);
		calendar.set(Calendar.MINUTE, Integer.parseInt(buf));
		calendar.set(Calendar.SECOND, 0);
		
		/* Se l'orario scelto è successivo all'orario attuale all'interno della giornata, si incrementa il giorno di uno
		 * per impedire che la notifica venga lanciata immediatamente.
		 */
		if(System.currentTimeMillis()-calendar.getTimeInMillis()>0)
			calendar.add(Calendar.DAY_OF_MONTH, 1);

		Toast.makeText(context, DateFormat.format("dd/MM/yy kk:mm:ss",calendar.getTime()), Toast.LENGTH_SHORT).show();
		
		//L'AlarmManager setta la notifica, che deve comparire ogni giorno all'orario appena stabilito
		alarmMgr.set(AlarmManager.RTC, calendar.getTimeInMillis(), alarmIntent);
		alarmMgr.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
				AlarmManager.INTERVAL_DAY, alarmIntent);

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
	private static void eraseAlarm(){
		if(alarmMgr!=null){
			alarmMgr.cancel(alarmIntent);

			ComponentName receiver = new ComponentName(context, AlarmReceiver.class);
			PackageManager pm = context.getPackageManager();
			pm.setComponentEnabledSetting(receiver,
					PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
					PackageManager.DONT_KILL_APP);
		}
	}


	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(
					R.layout.fragment_opzioni, container, false);

			email = (EditText) rootView.findViewById(R.id.email);
			password = (EditText) rootView.findViewById(R.id.password);

			notification = (CheckBox) rootView.findViewById(R.id.notifica_checkbox);

			//Configura le view relative alla scelta dell'orario per la visualizzazione della notifica
			alarm = (CheckBox) rootView.findViewById(R.id.sveglia_checkbox);
			alarmButton = (Button) rootView.findViewById(R.id.sveglia);
			alarmButton.setOnClickListener(new View.OnClickListener(){
				public void onClick(View v){
					android.support.v4.app.DialogFragment newFragment = new TimePickerFragment();
					newFragment.show(getActivity().getSupportFragmentManager(), "timePicker");
				}
			});

			//Configura le view relative alla scelta della durata massima di una sessione
			maxDuration = (TextView) rootView.findViewById(R.id.max_durata);
			final TextView hours = (TextView) rootView.findViewById(R.id.ore_label);
			Button plusButton = (Button) rootView.findViewById(R.id.plus);
			plusButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					int duration = Integer.parseInt(maxDuration.getText().toString());
					if(++duration<25)
						maxDuration.setText(""+duration);
					hours.setText("hours");
				}
			});
			Button minusButton = (Button) rootView.findViewById(R.id.minus);
			minusButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					int duration = Integer.parseInt(maxDuration.getText().toString());
					if(--duration>0)
						maxDuration.setText(""+duration);
					if(duration<2)
						hours.setText("ora");
					else
						hours.setText("hours");
				}
			});

			//Configura le view relative alla (modifica della) lista di contatti a cui inviare le e-mail di notifica
			ArrayList<String> contacts = readSelectedContacts();
			final ListView lv = (ListView) rootView.findViewById(R.id.contatti);
			if(contacts!=null){
				arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, contacts);
				lv.setAdapter(arrayAdapter);
			}
			Button contactListButton = (Button) rootView.findViewById(R.id.contatti_button);
			contactListButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent(getActivity(), ContactListActivity.class);
					startActivityForResult(intent,1);
					onActivityResult(0,0,null);
				}
			});

			//Configura il pulsante relativo alla scelta della frequenza di campionamento
			sampleRateButton = (Button) rootView.findViewById(R.id.campionamento);
			sampleRateButton.setOnClickListener(new View.OnClickListener(){
				public void onClick(View v){
					PopupMenu popupMenu = new PopupMenu(getActivity(), v);
					popupMenu.inflate(R.menu.opzioni);
					popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener(){
						public boolean onMenuItemClick(MenuItem item) {

							switch (item.getItemId()) {

							case R.id.fastest:
								sampleRateButton.setText("Molto alta");
								return true;

							case R.id.fast:
								sampleRateButton.setText("Alta");
								return true;

							case R.id.normal:
								sampleRateButton.setText("Normale");
								return true;

							case R.id.slow:
								sampleRateButton.setText("Bassa");
								return true;
							}
							return false;
						}
					});
					popupMenu.show();
				}
			});
			
			//Legge le opzioni precedentemente scelte da file di testo
			readSettings();	

			//Configura il pulsante per il salvataggio delle opzioni su file di testo 
			Button salva = (Button) rootView.findViewById(R.id.salva_button);
			salva.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					writeSettings();
				}
			});
			/*Button carica = (Button) rootView.findViewById(R.id.carica_button);
			carica.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					readSettings();
				}
			});*/
			return rootView;
		}



	}

	/**
	 * Classe per la gestione del timepicker usato dall'utente per scegliere quando visualizzare la notification
	 */
	private static class TimePickerFragment extends android.support.v4.app.DialogFragment
	implements TimePickerDialog.OnTimeSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final Calendar c = Calendar.getInstance();
			int hour = c.get(Calendar.HOUR_OF_DAY);
			int minute = c.get(Calendar.MINUTE);

			return new TimePickerDialog(getActivity(), this, hour, minute,
					DateFormat.is24HourFormat(getActivity()));
		}
		
		/**
		 * Ricava l'orario scelto dall'utente per la visualizzazione della notifica di sistema e lo passa alla funzione
		 * fireAlarm, per la configurazione della notifica stessa.
		 */
		public void onTimeSet(TimePicker view, int hourOfDay, int minuteOfHour) {
			String hour = Integer.toString(hourOfDay);
			String minute = Integer.toString(minuteOfHour);
			if(hourOfDay<10)
				hour = "0" + hour;
			if(minuteOfHour<10)
				minute = "0" + minute;
			alarmButton.setText(hour+":"+minute);
			if(alarm.isChecked())
				fireAlarm(hour+":"+minute);
		}
	}

	/**
	 * Legge la lista di contatti precedentemente scelti per l'invio delle e-mail di notification
	 * @return
	 */
	private static ArrayList<String> readSelectedContacts(){
		ArrayList<String> selectedContacts = new ArrayList<String>();
		try {
			FileInputStream input = context.openFileInput("contactlist.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(input));
			String line;
			while((line = br.readLine()) != null)
				selectedContacts.add(line);
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return selectedContacts;
	}

	/**
	 * Aggiorna la listView dei contatti scelti per l'invio delle e-mail di notification
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		arrayAdapter.clear();
		ArrayList<String> contacts = readSelectedContacts();
		for(int i=0;i<contacts.size();i++)
			arrayAdapter.add(contacts.get(i));
		arrayAdapter.notifyDataSetChanged();
	}

	/**
	 * Memorizza le impostazioni su un file di testo
	 */
	private static void writeSettings(){
		try {
			FileOutputStream output = context.openFileOutput("settings.txt", MODE_PRIVATE);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(output));
			bw.write(sampleRateButton.getText() + "\r\n");
			bw.write(maxDuration.getText() + "\r\n");
			bw.write(alarmButton.getText() + "\r\n");
			if(alarm.isChecked())
				bw.write(SettingValues.ENABLED + "\r\n");
			else
				bw.write(SettingValues.DISABLED + "\r\n");
			if(notification.isChecked())
				bw.write(SettingValues.ENABLED + "\r\n");
			else
				bw.write(SettingValues.DISABLED + "\r\n");
			if((!email.getText().toString().equals("") && !password.getText().toString().equals(""))){
				bw.write(email.getText() + "\r\n");
				bw.write(password.getText().toString());
			}
			bw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Recupera le impostazione dal file di testo relativo e configura i campi dell'activity in base ad esse.
	 */
	private static void readSettings(){
		try {
			FileInputStream input = context.openFileInput("settings.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(input));
			String line;
			if((line = br.readLine()) == null)
				return;
			else
				sampleRateButton.setText(line);
			maxDuration.setText(br.readLine());
			alarmButton.setText(br.readLine());
			if( (line = br.readLine()).equals("tick sveglia"))
				alarm.setChecked(true);
			else
				alarm.setChecked(false);
			if( (line = br.readLine()).equals("tick notification"))
				notification.setChecked(true);
			else
				notification.setChecked(false);
			if( (line = br.readLine()) != null){
				email.setText(line);
				password.setText(br.readLine());
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
