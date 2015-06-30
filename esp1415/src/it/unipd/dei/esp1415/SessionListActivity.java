package it.unipd.dei.esp1415;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateFormat;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.widget.AdapterView;

/**
 * Questa activity conterrà la lista delle sessioni.
 *
 */
public class SessionListActivity extends ActionBarActivity implements
		RenameDialog.renameDialogListener {

	private static DBManager sDb;
	private static Session sSelectedSession;
	private static int sPosition;

	@Override
	protected void onDestroy() {
		sDb.close();
		super.onDestroy();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lista_sessioni);
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new SessionListFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inserisce il menù, aggiungendo elementi all'action bar se presenti
		getMenuInflater().inflate(R.menu.lista_sessioni, menu);
		return true;
	}

	// Metodo che gestisce i click nell'action bar
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		// Se viene premuto il pulsante impostazioni
		if (id == R.id.settings) {
			Intent settings = new Intent(SessionListActivity.this,
					SettingsActivity.class);
			// Attivazione dell'activity SettingsActivity.java
			startActivity(settings);
			return true;
		}
		// Se viene premuto il pulsante nuova sessione
		else if (id == R.id.newsession) {
			// Se non ci sono sessioni attive
			if (!(sDb.hasActiveSession())) {
				Intent newSession = new Intent(SessionListActivity.this,
						CurrentSessionDetailsActivity.class);
				// Attivazione dell'activity
				// DettaglioSessioneCorrenteActivity.java
				startActivity(newSession);
			}
			// Notifica che c'è già una sessione attiva
			else {
				Toast.makeText(this, R.string.errore_sessione_attiva,
						Toast.LENGTH_SHORT).show();
			}

			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onDialogPositiveClick(RenameDialog dialog) {
		String name = sDb.getSession(sSelectedSession.getSessionBegin())
				.getName();
		// Rinominazione nell'adapter
		SessionListFragment.sAdapter.mSessions.get(sPosition).setName(name);
		// Notifico all'adapter i cambiamenti
		SessionListFragment.sAdapter.notifyDataSetChanged();
	}

	@Override
	public void onDialogNegativeClick(RenameDialog dialog) {

	}

	public static class SessionListFragment extends ListFragment {

		static MyAdapter sAdapter;

		public SessionListFragment() {
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			// Prende tutte le sessioni dal database
			sDb = new DBManager(getActivity().getBaseContext());
			sDb.open();
			// Carico l'adapter
			sAdapter = new MyAdapter(getActivity().getBaseContext(),
					R.layout.adapter_lista_sessioni,
					(ArrayList<Session>) sDb.getAllSessions());
			setListAdapter(sAdapter);
			ListView listView = getListView();
			registerForContextMenu(listView);

		}

		@Override
		public void onResume() {
			super.onResume();
			sAdapter.clear();
			List<Session> dbSessions = sDb.getAllSessions();
			for (int i = 0; i < dbSessions.size(); i++) {
				sAdapter.add(dbSessions.get(i));
			}
			sAdapter.notifyDataSetChanged();
		}

		// Metodo che gestisce il tocco prolungato
		@Override
		public void onCreateContextMenu(ContextMenu menu, View v,
				ContextMenuInfo menuInfo) {
			super.onCreateContextMenu(menu, v, menuInfo);
			if (v.getId() == android.R.id.list) {
				AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
				sPosition = info.position;
				sSelectedSession = (Session) getListAdapter()
						.getItem(sPosition);
				// Impostazione del titolo del menù
				menu.setHeaderTitle(sSelectedSession.getName());
				MenuInflater inflater = getActivity().getMenuInflater();
				// Aggiunta di opzioni al menù
				inflater.inflate(R.menu.click_lungo_lista_sessioni, menu);
			}
		}

		// Metodo che gestisce il menù del tocco prolungato
		@Override
		public boolean onContextItemSelected(MenuItem item) {
			{
				int menuItemIndex = item.getItemId();
				// Se viene premuto il tasto rinomina
				if (menuItemIndex == R.id.rename) {
					RenameDialog newFragment = new RenameDialog();
					Bundle args = new Bundle();
					args.putLong("id", sSelectedSession.getSessionBegin()
							.getTime());
					newFragment.setArguments(args);
					newFragment.show(getFragmentManager(), "rename");
					return true;
				}
				// Se viene premuto il tasto elimina
				else if (menuItemIndex == R.id.delete) {
					// Rimozione dal database
					sDb.deleteSession(sSelectedSession);
					// Rimozione dalla lista
					sAdapter.remove(sSelectedSession);
					// notifico all'adapter i cambiamenti
					sAdapter.notifyDataSetChanged();
					// Notifica di avvenuta cancellazione
					Toast.makeText(
							getActivity(),
							sSelectedSession.getName()
									+ getActivity().getString(
											R.string.sessione_rimossa),
							Toast.LENGTH_SHORT).show();
					return true;
				}
				return super.onContextItemSelected(item);
			}
		}

		// Metodo per gestire i click su elementi della lista
		@Override
		public void onListItemClick(ListView l, View v, int position, long id) {
			super.onListItemClick(l, v, position, id);
			sSelectedSession = (Session) getListAdapter().getItem(position);
			// Se viene cliccata una sessione attiva
			if (sSelectedSession.isActive()) {
				Intent currentSession = new Intent(getActivity()
						.getApplicationContext(),
						CurrentSessionDetailsActivity.class);
				startActivity(currentSession);
			}
			// Se viene cliccata una sessione passata
			else {
				Intent pastSession = new Intent(getActivity()
						.getApplicationContext(),
						PastSessionDetailsActivity.class);
				Date sessionId = sAdapter.getItem(position).getSessionBegin();
				pastSession.putExtra("IDSessione", sessionId.getTime());
				startActivity(pastSession);
			}
		}

	}

	// Classe per l'adapter
	public static class MyAdapter extends ArrayAdapter<Session> {
		ArrayList<Session> mSessions;
		static Context sContext;

		public MyAdapter(Context context, int textVewResourceId,
				ArrayList<Session> sessions) {
			super(context, textVewResourceId, sessions);
			MyAdapter.sContext = context;
			this.mSessions = sessions;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) sContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = convertView;
			if (rowView == null) {
				Holder holder = new Holder();
				rowView = inflater.inflate(R.layout.adapter_lista_sessioni,
						parent, false);
				holder.secondLine = (TextView) rowView
						.findViewById(R.id.secondLine);
				holder.imageView = (ImageView) rowView
						.findViewById(R.id.thumbnail);
				holder.firstLine = (TextView) rowView
						.findViewById(R.id.firstLine);
				holder.thirdLine = (TextView) rowView
						.findViewById(R.id.thirdLine);
				rowView.setTag(holder);
			}
			Holder holder = (Holder) rowView.getTag();
			// Imposta i campi della prima e della seconda riga
			Session session = mSessions.get(position);
			holder.firstLine.setText(session.getName());
			String date = (String) DateFormat.format("dd/MM/yy",
					session.getSessionBegin());
			String hour = (String) DateFormat.format("kk:mm",
					session.getSessionBegin());
			String secondLine = getContext().getString(R.string.data_e_ora)
					+ date + " " + hour;
			holder.secondLine.setText(secondLine);
			String thirdLine = getContext().getString(R.string.durata_2)
					+ conversionFromMilliseconds(session.getDuration()) + " - "
					+ session.getNumberOfFalls();

			if (session.getNumberOfFalls() == 1) {
				holder.thirdLine.setText(thirdLine + " "
						+ getContext().getString(R.string.caduta));
			} else {
				holder.thirdLine.setText(thirdLine + " "
						+ getContext().getString(R.string.cadute_min));
			}
			// Per la sessione attiva cambio determinati parametri
			boolean active = session.isActive();
			if (active) {
				// Il 60 davanti al numero esadecimale decide la trasparenza
				rowView.setBackgroundColor(Color.parseColor("#60FFFFFF"));
				holder.firstLine.setTypeface(null, Typeface.BOLD);
				holder.secondLine.setTypeface(null, Typeface.BOLD);
				// Carattere grassetto
				holder.thirdLine.setTypeface(null, Typeface.BOLD);
				// Colore rosso del testo della prima riga (nome della sessione)
				holder.firstLine.setTextColor(Color.RED);
			}

			else {
				// Sfondo trasparente
				rowView.setBackgroundColor(Color.parseColor("#00000000"));
				holder.firstLine.setTypeface(null, Typeface.NORMAL);
				holder.secondLine.setTypeface(null, Typeface.NORMAL);
				holder.thirdLine.setTypeface(null, Typeface.NORMAL);
				holder.firstLine.setTextColor(Color.BLACK);
			}

			// Impostazione della thumbnail
			String name = session.getThumbnail();
			Bitmap thumbnail = Utilities.loadImageFromStorage(name, sContext);
			holder.imageView.setImageBitmap(thumbnail);
			return rowView;
		}

		public String conversionFromMilliseconds(int milliseconds) {
			String hoursAndMinutes = "";
			int seconds = milliseconds / 1000;
			int minutes = seconds / 60;
			int hours = minutes / 60;
			minutes = minutes % 60;
			hoursAndMinutes = hours + "h " + minutes + "m";
			return hoursAndMinutes;
		}

		static class Holder {
			public TextView firstLine, secondLine, thirdLine;
			public ImageView imageView;
			public RelativeLayout layout;
		}
	}

}
