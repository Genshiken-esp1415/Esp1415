package it.unipd.dei.esp1415;

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

/**
 * Activity per la scelta delle opzioni e dei contatti a cui spedire le
 * notifiche
 */
public class SettingsActivity extends ActionBarActivity {

	// Dichiarazione variabili delle view dell'activity, i nomi sono
	// autoesplicativi
	private static Button sAlarmButton;
	private static Button sSampleRateButton;
	private static TextView sMaxDuration;
	private static CheckBox sAlarm;
	private static CheckBox sNotification;
	private static EditText sEmail;
	private static EditText sPassword;

	private static Context sContext;
	private static ArrayAdapter<String> sArrayAdapter;

	private static SharedPreferences sPreferences;
	private static SharedPreferences.Editor sEditor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_activity);
		sContext = this;
		sPreferences = getSharedPreferences("MyPref", Context.MODE_APPEND);
		sEditor = sPreferences.edit();
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new SettingsFragment()).commit();
		}
	}

	/**
	 * Gestisce la azioni da compiere quando cambia lo stato della checkbox
	 * della sveglia. Viene eseguito il metodo per l'impostazione (o la
	 * rimozione, a seconda) della notification di sistema all'ora specificata
	 * dall'utente.
	 */
	public void onCheckboxClicked(View view) {
		switch (view.getId()) {
		case R.id.alarm_checkbox:
			if (((CheckBox) view).isChecked()) {
				Utilities.fireAlarm(sContext);
			} else {
				Utilities.eraseAlarm(sContext);
			}
			break;
		}
	}

	public static class SettingsFragment extends Fragment {

		public SettingsFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.settings_fragment,
					container, false);

			sEmail = (EditText) rootView.findViewById(R.id.email);
			sPassword = (EditText) rootView.findViewById(R.id.password);

			sNotification = (CheckBox) rootView
					.findViewById(R.id.notification_checkbox);

			// Configura le view relative alla scelta dell'orario per la
			// visualizzazione della notifica
			sAlarm = (CheckBox) rootView.findViewById(R.id.alarm_checkbox);
			sAlarmButton = (Button) rootView.findViewById(R.id.alarm);
			sAlarmButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					android.support.v4.app.DialogFragment newFragment = new TimePickerFragment();
					newFragment.show(getActivity().getSupportFragmentManager(),
							"timePicker");
				}
			});

			// Configura le view relative alla scelta della durata massima di
			// una sessione
			sMaxDuration = (TextView) rootView.findViewById(R.id.duration);
			final TextView hours = (TextView) rootView
					.findViewById(R.id.hours_label);
			Button plusButton = (Button) rootView.findViewById(R.id.plus);
			plusButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					int duration = Integer.parseInt(sMaxDuration.getText()
							.toString());
					if (++duration < 25) {
						sMaxDuration.setText("" + duration);
					}
					hours.setText("ore");
				}
			});
			Button minusButton = (Button) rootView.findViewById(R.id.minus);
			minusButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					int duration = Integer.parseInt(sMaxDuration.getText()
							.toString());
					if (--duration > 0) {
						sMaxDuration.setText("" + duration);
					}
					if (duration < 2) {
						hours.setText("ora");
					} else {
						hours.setText("ore");
					}
				}
			});

			// Configura le view relative alla (modifica della) lista di
			// contatti a cui inviare le e-mail di notifica
			ArrayList<String> contacts = readSelectedContacts();
			final ListView contactList = (ListView) rootView
					.findViewById(R.id.contacts);
			if (contacts != null) {
				sArrayAdapter = new ArrayAdapter<String>(getActivity(),
						R.layout.selected_contacts_view, contacts);
				contactList.setAdapter(sArrayAdapter);
			}
			Button contactListButton = (Button) rootView
					.findViewById(R.id.contacts_button);
			contactListButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent(getActivity(),
							ContactListActivity.class);
					startActivityForResult(intent, 1);
					onActivityResult(0, 0, null);
				}
			});

			// Configura il pulsante relativo alla scelta della frequenza di
			// campionamento
			sSampleRateButton = (Button) rootView
					.findViewById(R.id.sampling);
			sSampleRateButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					PopupMenu popupMenu = new PopupMenu(getActivity(), v);
					popupMenu.inflate(R.menu.settings);
					popupMenu
							.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
								public boolean onMenuItemClick(MenuItem item) {

									switch (item.getItemId()) {

									case R.id.fastest:
										sSampleRateButton.setText("Molto alta");
										return true;

									case R.id.fast:
										sSampleRateButton.setText("Alta");
										return true;

									case R.id.normal:
										sSampleRateButton.setText("Normale");
										return true;

									case R.id.slow:
										sSampleRateButton.setText("Bassa");
										return true;
									}
									return false;
								}
							});
					popupMenu.show();
				}
			});

			// Legge le opzioni precedentemente scelte da file di testo
			setText();

			// Configura il pulsante per il salvataggio delle opzioni su file di
			// testo
			Button salva = (Button) rootView.findViewById(R.id.save_button);
			salva.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					writeSettings();
					getActivity().finish();
				}
			});

			return rootView;
		}
	}

	/**
	 * Classe per la gestione del timepicker usato dall'utente per scegliere
	 * quando visualizzare la notification
	 */
	private static class TimePickerFragment extends
			android.support.v4.app.DialogFragment implements
			TimePickerDialog.OnTimeSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final Calendar c = Calendar.getInstance();
			int hour = c.get(Calendar.HOUR_OF_DAY);
			int minute = c.get(Calendar.MINUTE);

			return new TimePickerDialog(getActivity(), this, hour, minute,
					DateFormat.is24HourFormat(getActivity()));
		}

		/**
		 * Ricava l'orario scelto dall'utente per la visualizzazione della
		 * notifica di sistema e lo passa alla funzione fireAlarm, per la
		 * configurazione della notifica stessa.
		 */
		public void onTimeSet(TimePicker view, int hourOfDay, int minuteOfHour) {
			String hour = Integer.toString(hourOfDay);
			String minute = Integer.toString(minuteOfHour);
			if (hourOfDay < 10) {
				hour = "0" + hour;
			}
			if (minuteOfHour < 10) {
				minute = "0" + minute;
			}
			sAlarmButton.setText(hour + ":" + minute);

			sEditor.putInt("hour", hourOfDay);
			sEditor.putInt("minute", minuteOfHour);
			sEditor.commit();
			if (sAlarm.isChecked()) {
				Utilities.fireAlarm(sContext);
			}
		}
	}

	/**
	 * Legge la lista di contatti precedentemente scelti per l'invio delle
	 * e-mail di notification
	 * 
	 * @return
	 */
	private static ArrayList<String> readSelectedContacts() {
		ArrayList<String> selectedContacts = new ArrayList<String>();
		for (int i = 0; i < Utilities.sDest.size(); i++) {
			selectedContacts.add(Utilities.sName.get(i) + ": "
					+ Utilities.sDest.get(i));
		}
		return selectedContacts;
	}

	/**
	 * Aggiorna la listView dei contatti scelti per l'invio delle e-mail di
	 * notification
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		sArrayAdapter.clear();
		for (int i = 0; i < Utilities.sDest.size(); i++) {
			sArrayAdapter.add(Utilities.sName.get(i) + ": "
					+ Utilities.sDest.get(i));
			
		}
		sArrayAdapter.notifyDataSetChanged();
	}

	/**
	 * Memorizza le impostazioni
	 */
	private static void writeSettings() {
		sEditor.putInt("sensorDelay", Utilities
				.getSensorDelay(sSampleRateButton.getText().toString()));
		sEditor.putInt("maxDuration",
				Integer.parseInt((String) sMaxDuration.getText()));
		sEditor.putBoolean("alarmCheck", sAlarm.isChecked());
		sEditor.putBoolean("notificationCheck", sNotification.isChecked());
		if ((!sEmail.getText().toString().equals("") && !sPassword.getText()
				.toString().equals(""))) {
			sEditor.putString("email", sEmail.getText().toString());
			sEditor.putString("password", sPassword.getText().toString());
		}
		sEditor.commit();
	}

	/**
	 * Imposta il testo delle varie view in base alle impostazioni scelte (o di
	 * default)
	 */
	private static void setText() {
		String hour = Integer.toString(sPreferences.getInt("hour", 8));
		String minute = Integer.toString(sPreferences.getInt("minute", 0));
		if (sPreferences.getInt("hour", 8) < 10) {
			hour = "0" + hour;
		}
		if (sPreferences.getInt("minute", 0) < 10) {
			minute = "0" + minute;
		}
		sAlarmButton.setText(hour + ":" + minute);
		sMaxDuration.setText(((Integer) sPreferences.getInt("maxDuration", 8))
				.toString());
		sAlarm.setChecked(sPreferences.getBoolean("alarmCheck", false));
		sNotification.setChecked(sPreferences.getBoolean("notificationCheck",
				false));
		sEmail.setText(sPreferences.getString("email", ""));
		sPassword.setText(sPreferences.getString("password", ""));
		switch (sPreferences.getInt("sensorDelay",
				SensorManager.SENSOR_DELAY_GAME)) {

		case SensorManager.SENSOR_DELAY_FASTEST:
			sSampleRateButton.setText("Molto alta");
			break;

		case SensorManager.SENSOR_DELAY_GAME:
			sSampleRateButton.setText("Alta");
			break;

		case SensorManager.SENSOR_DELAY_NORMAL:
			sSampleRateButton.setText("Normale");
			break;

		case SensorManager.SENSOR_DELAY_UI:
			sSampleRateButton.setText("Bassa");
			break;

		}
	}
}
