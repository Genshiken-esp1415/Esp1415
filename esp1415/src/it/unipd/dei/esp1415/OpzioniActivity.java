package it.unipd.dei.esp1415;


import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
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
	private static CheckBox alarm;
	private static CheckBox notification;
	private static EditText email;
	private static EditText password;

	private static Context context;
	private static ArrayAdapter<String> arrayAdapter;

	private static SharedPreferences preferences; 
	private static SharedPreferences.Editor editor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_opzioni);
		context = this;
		preferences = getSharedPreferences("MyPref", Context.MODE_APPEND);
		editor = preferences.edit();
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
		switch(view.getId()) {
		case R.id.sveglia_checkbox:
			if (((CheckBox) view).isChecked()) {
				//fireAlarm((String) alarmButton.getText());
				SettingValues.fireAlarm(context);
			} else {
				SettingValues.eraseAlarm(context);
			}
			break;
			/*case R.id.notification_checkbox:
	            if (checked)
	            else
	            break;*/
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
			setText();	

			//Configura il pulsante per il salvataggio delle opzioni su file di testo 
			Button salva = (Button) rootView.findViewById(R.id.salva_button);
			salva.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					writeSettings();
				}
			});

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

			editor.putInt("hour", hourOfDay);
			editor.putInt("minute", minuteOfHour);
			editor.commit();
			if(alarm.isChecked())
				SettingValues.fireAlarm(context);
		}
	}

	/**
	 * Legge la lista di contatti precedentemente scelti per l'invio delle e-mail di notification
	 * @return
	 */
	private static ArrayList<String> readSelectedContacts(){
		ArrayList<String> selectedContacts = new ArrayList<String>();
		for (int i=0;i<SettingValues.sDest.size();i++) {
			selectedContacts.add(SettingValues.sName.get(i) + ": " + SettingValues.sDest.get(i));
		}
		return selectedContacts;
	}

	/**
	 * Aggiorna la listView dei contatti scelti per l'invio delle e-mail di notification
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		arrayAdapter.clear();
		for (int i=0;i<SettingValues.sDest.size();i++) {
			arrayAdapter.add(SettingValues.sName.get(i) + ": " + SettingValues.sDest.get(i));
		}
		arrayAdapter.notifyDataSetChanged();
	}

	/**
	 * Memorizza le impostazioni su un file di testo
	 */
	private static void writeSettings(){
		try {
			FileOutputStream output = context.openFileOutput("settings.txt", MODE_PRIVATE);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(output));

			SettingValues.setSensorDelay(sampleRateButton.getText().toString());
			bw.write(sampleRateButton.getText() + "\r\n");
			SettingValues.sMaxDuration = Integer.parseInt((String) maxDuration.getText());
			bw.write(SettingValues.sMaxDuration + "\r\n");
			//setTime((String) alarmButton.getText());
			//bw.write(SettingValues.sAlarmHour + "\r\n");
			//bw.write(SettingValues.sAlarmMinute + "\r\n");
			SettingValues.sAlarmCheck = alarm.isChecked();
			bw.write(SettingValues.sAlarmCheck + "\r\n");
			SettingValues.sNotificationCheck = notification.isChecked();
			bw.write(SettingValues.sNotificationCheck + "\r\n");
			if((!email.getText().toString().equals("") && !password.getText().toString().equals(""))){
				SettingValues.sEmail = email.getText().toString();
				SettingValues.sPassword = password.getText().toString();
				bw.write(SettingValues.sEmail + "\r\n");
				bw.write(SettingValues.sPassword);
			}

			bw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void setText() {
		String hour = Integer.toString(preferences.getInt("hour", 8));
		String minute = Integer.toString(preferences.getInt("minute", 0));
		if (preferences.getInt("hour",8)<10){
			hour = "0" + hour;
			Toast.makeText(context, "" + preferences.getInt("hour",8), Toast.LENGTH_LONG).show();
		}
		if (preferences.getInt("minute",0)<10)
			minute = "0" + minute;
		alarmButton.setText(hour+":"+minute);
		maxDuration.setText(((Integer)SettingValues.sMaxDuration).toString());
		alarm.setChecked(SettingValues.sAlarmCheck);
		notification.setChecked(SettingValues.sNotificationCheck);
		email.setText(SettingValues.sEmail);
		password.setText(SettingValues.sPassword);	
		switch (SettingValues.sSensorDelay) {
		case SensorManager.SENSOR_DELAY_FASTEST:
			sampleRateButton.setText("Molto alta");
			break;
		case SensorManager.SENSOR_DELAY_GAME:
			sampleRateButton.setText("Alta");
			break;
		case SensorManager.SENSOR_DELAY_NORMAL:
			sampleRateButton.setText("Normale");
			break;
		case SensorManager.SENSOR_DELAY_UI:
			sampleRateButton.setText("Bassa");
			break;
		}
	}
}
