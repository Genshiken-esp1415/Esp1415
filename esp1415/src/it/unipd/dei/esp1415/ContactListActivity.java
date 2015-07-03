package it.unipd.dei.esp1415;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity per la scelta degli indirizzi e-mail, tra quelli memorizzati nella
 * rubrica, a cui inviare le notifiche di caduta.
 */
public class ContactListActivity extends Activity {

	// Parametri da ricavare tramite query (nome e indirizzo e-mail
	// associato)
	@SuppressLint("InlinedApi")
	private static final String[] PROJECTION = new String[] { Email.CONTACT_ID,
			Phone.DISPLAY_NAME, Email.ADDRESS };
	static ContactListArrayAdapter sArrayAdapter;

	/*
	 * Alla creazione dell'activity viene riempito l'array di contatti contacts
	 * con tutti gli indirizzi e-mail, e relativo nome associato presenti nella
	 * rubrica. Si fa uso di un flag per capire se un dato indirizzo è già
	 * presente tra quelli da inviare.
	 */
	@SuppressLint("InlinedApi")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contactlist_activity);

		final ListView contactList = (ListView) findViewById(R.id.contact_list);
		Button doneButton = (Button) findViewById(R.id.done);
		String name;
		String address;
		ArrayList<ContactData> contacts = new ArrayList<ContactData>();

		// Cursore che punta alla lista di contatti in rubrica a cui è anche
		// associata una e-mail
		Cursor cursor = this.getContentResolver().query(Email.CONTENT_URI,
				PROJECTION, null, null, null);
		while (cursor.moveToNext()) {
			name = cursor.getString(cursor.getColumnIndex(Phone.DISPLAY_NAME));
			address = cursor.getString(cursor.getColumnIndex(Email.ADDRESS));

			/*
			 * Controllo necessario per eliminare risultati in cui nome e
			 * indirizzo sono uguali, come osservato in fase di testing. Se
			 * l'indirizzo era già stato scelto, l'attributo added viene settato
			 * di conseguenza.
			 */
			if (!name.equals(address)) {
				if (Utilities.sDest.contains(address)) {
					contacts.add(new ContactData(name, address, true));
				} else {
					contacts.add(new ContactData(name, address, false));
				}
			}
		}
		cursor.close();

		/*
		 * Al click di uno degli elementi della lista, lo si aggiunge (o rimuove
		 * a seconda) alla lista degli indirizzi scelti per l'invio delle
		 * notifiche.
		 */
		sArrayAdapter = new ContactListArrayAdapter(this,
				R.layout.contactlistview_row, contacts);
		contactList.setAdapter(sArrayAdapter);
		contactList.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View v, int position,
					long arg3) {
				if (!sArrayAdapter.items.get(position).getAdded()) {
					sArrayAdapter.items.get(position).setAdded(true);
				} else {
					sArrayAdapter.items.get(position).setAdded(false);
				}
				sArrayAdapter.notifyDataSetChanged();
			}
		});

		/*
		 * Premendo il pulsante Fatto vengono scritti i contatti scelti su un
		 * file di testo e si torna all'activity chiamante
		 */
		doneButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				writeSelectedContacts(sArrayAdapter.items);
				finish();
			}
		});
	}

	/*
	 * Premendo il pulsante indietro del dispositivo vengono scritti i contatti
	 * scelti su un file di testo e si torna all'activity chiamante
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			writeSelectedContacts(sArrayAdapter.items);
			setResult(1);
			finish();
		}
		return true;
	}

	/**
	 * 
	 * Scrive su file di testo gli indirizzi scelti e i nomi associati, nel
	 * formato nome: indirizzo, uno per riga. Viene utilizzato alla pressione
	 * del bottone "Fatto".
	 *
	 * @param dest
	 *            lista dei destinatari delle e-mail di notifica
	 */
	private void writeSelectedContacts(ArrayList<ContactData> dest) {
		try {
			FileOutputStream output = openFileOutput("contactlist.txt",
					MODE_PRIVATE);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
					output));

			// Si svuotano gli ArrayList per assicurarsi che non si presentino
			// duplicati di alcun tipo
			Utilities.sDest.clear();
			Utilities.sName.clear();
			for (int i = 0; i < dest.size(); i++) {
				if (dest.get(i).getAdded()) {
					bw.append(dest.get(i).getName() + ": "
							+ dest.get(i).getAddress() + "\r\n");
					Utilities.sName.add(dest.get(i).getName());
					Utilities.sDest.add(dest.get(i).getAddress());
				}
			}
			bw.close();
		} catch (FileNotFoundException e) {
			Toast.makeText(getApplicationContext(),
					"File contatti scelti non trovato", Toast.LENGTH_LONG)
					.show();
		} catch (IOException e) {
			Toast.makeText(getApplicationContext(),
					"Errore scrittura file contatti scelti", Toast.LENGTH_LONG)
					.show();
		}
	}

	/**
	 * ArrayAdapter personalizzato per la gestione delle singole righe della
	 * ListView degli indirizzi
	 */
	private class ContactListArrayAdapter extends ArrayAdapter<ContactData> {

		ArrayList<ContactData> items;

		public ContactListArrayAdapter(Context context, int resource,
				ArrayList<ContactData> items) {
			super(context, resource, items);
			this.items = items;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = getLayoutInflater();
			View rowView = convertView;
			if (rowView == null) {
				Holder holder = new Holder();
				rowView = inflater.inflate(R.layout.contactlistview_row,
						parent, false);
				holder.row = (TextView) rowView.findViewById(R.id.row);
				rowView.setTag(holder);
			}
			Holder holder = (Holder) rowView.getTag();

			// Scrive il testo della riga, nel formato nome: indirizzo
			holder.row.setText(items.get(position).getName() + ": "
					+ items.get(position).getAddress());

			// Se l'indirizzo è stato aggiunto, la riga corrispondente si colora
			// di grigio; altrimenti di bianco
			if (items.get(position).getAdded()) {
				holder.row.setBackgroundColor(Color.GRAY);
			} else {
				holder.row.setBackgroundColor(Color.WHITE);
			}
			return rowView;
		}
	}

	/**
	 * Rappresenta i dati di interesse di ogni contatto: nome, indirizzo, se
	 * l'indirizzo è stato aggiunto o meno tra quelli a cui spedire le
	 * notifiche.
	 */
	private class ContactData {

		private String mName;
		private String mAddress;
		private boolean mAdded;

		public ContactData(String name, String address, boolean added) {
			this.mName = name;
			this.mAddress = address;
			this.mAdded = added;
		}

		public String getName() {
			return mName;
		}

		public String getAddress() {
			return mAddress;
		}

		public boolean getAdded() {
			return mAdded;
		}

		public void setAdded(boolean added) {
			this.mAdded = added;
		}
	}

	/**
	 * Classe usata per implementare il design pattern view holder.
	 */
	private static class Holder {
		public TextView row;
	}
}