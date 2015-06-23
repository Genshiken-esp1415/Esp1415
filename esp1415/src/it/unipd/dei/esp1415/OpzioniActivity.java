package it.unipd.dei.esp1415;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

public class OpzioniActivity extends ActionBarActivity {

	private static Button alarmButton;
	private static Button sampleRateButton;
	private static ArrayAdapter<String> arrayAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_opzioni);
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

			alarmButton = (Button) rootView.findViewById(R.id.sveglia);
			alarmButton.setOnClickListener(new View.OnClickListener(){
				public void onClick(View v){
					android.support.v4.app.DialogFragment newFragment = new TimePickerFragment();
					newFragment.show(getActivity().getSupportFragmentManager(), "timePicker");
				}
			});

			final TextView maxDurata = (TextView) rootView.findViewById(R.id.max_durata);
			final TextView ore = (TextView) rootView.findViewById(R.id.ore_label);
			Button plusButton = (Button) rootView.findViewById(R.id.plus);
			plusButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					int d = Integer.parseInt(maxDurata.getText().toString());
					if(++d<25)
						maxDurata.setText(""+d);
					ore.setText("ore");
				}
			});
			Button minusButton = (Button) rootView.findViewById(R.id.minus);
			minusButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					int d = Integer.parseInt(maxDurata.getText().toString());
					if(--d>0)
						maxDurata.setText(""+d);
					if(d<2)
						ore.setText("ora");
					else
						ore.setText("ore");
				}
			});

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
			
			return rootView;
		}

		private ArrayList<String> readSelectedContacts(){
			ArrayList<String> selectedContacts = new ArrayList<String>();
			try {
				FileInputStream input = getActivity().getApplication().openFileInput("contactlist.txt");
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

		@Override
		public void onActivityResult(int requestCode, int resultCode, Intent data) {
			arrayAdapter.clear();
			ArrayList<String> contacts = readSelectedContacts();
			for(int i=0;i<contacts.size();i++)
				arrayAdapter.add(contacts.get(i));
			arrayAdapter.notifyDataSetChanged();
		}

	}

	private static class TimePickerFragment extends android.support.v4.app.DialogFragment
	implements TimePickerDialog.OnTimeSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current time as the default values for the picker
			final Calendar c = Calendar.getInstance();
			int hour = c.get(Calendar.HOUR_OF_DAY);
			int minute = c.get(Calendar.MINUTE);

			return new TimePickerDialog(getActivity(), this, hour, minute,
					DateFormat.is24HourFormat(getActivity()));
		}

		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			alarmButton.setText(hourOfDay+":"+minute);
		}
	}
}
