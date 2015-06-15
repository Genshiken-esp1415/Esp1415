package it.unipd.dei.esp1415;

import it.unipd.dei.esp1415.R;

import java.util.ArrayList;
import android.content.Context;
import android.graphics.Color;
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
public class ListaSessioniActivity extends ActionBarActivity implements renameDialog.renameDialogListener{
	
	private static DBManager db;
	private static Session sessione_scelta;
	private static int pos;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lista_sessioni);
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.lista_sessioni, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.settings) // se viene premuto il pulsante impostazioni
		{
			Intent impostazioni = new Intent(ListaSessioniActivity.this,
					SettingsActivity.class);
			// attivazione dell'activity SettingsActivity.java
			startActivity(impostazioni);
			return true;
		} else if (id == R.id.newsession) // se viene premuto il pulsante nuova
											// sessione
		{
			if(!(db.hasActiveSession())) //se non ci sono sessioni attive
			{
			Intent nuovaSessione = new Intent(ListaSessioniActivity.this,
					DettaglioSessioneCorrenteActivity.class);
			// attivazione dell'activity DettaglioSessioneCorrenteActivity.java
			startActivity(nuovaSessione);
			}
			else //notifico che c'è già una sessione attiva
			{
			Toast.makeText(this, R.string.errore_sessione_attiva, Toast.LENGTH_SHORT).show();	
			}
			
			return true;
		}
		return super.onOptionsItemSelected(item);

	}
	
	@Override
	public void onDialogPositiveClick(renameDialog dialog) {
		String name = db.getSession(sessione_scelta.getSessionBegin()).getName();
		PlaceholderFragment.adapter.sessioni.get(pos).setName(name); //rinomino nell'adapter
		PlaceholderFragment.adapter.notifyDataSetChanged(); //aggiorno la lista
	}

	@Override
	public void onDialogNegativeClick(renameDialog dialog) {

	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends ListFragment {
	
		//public static ArrayList<Session> sessions;
		static MyAdapter adapter;
		
		public PlaceholderFragment() {
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			//prendo tutte le sessioni dal db
			db = new DBManager(getActivity().getBaseContext());
			db.open();
			adapter = new MyAdapter(getActivity().getBaseContext(), R.layout.adapter_lista_sessioni, (ArrayList<Session>)db.getAllSessions());
			setListAdapter(adapter);
			ListView list_view = getListView();
			registerForContextMenu(list_view);

			}	

		@Override
		public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) { // gestisce tocco prolungato
			super.onCreateContextMenu(menu, v, menuInfo);
			if (v.getId() == android.R.id.list) {
				AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
				pos = info.position;
				sessione_scelta = (Session) getListAdapter().getItem(pos);
				menu.setHeaderTitle(sessione_scelta.getName()); // imposto titolo menù
				MenuInflater inflater = getActivity().getMenuInflater();
				inflater.inflate(R.menu.click_lungo_lista_sessioni, menu); // aggiungo opzioni al menù
			}
		}
		
		@Override
		public boolean onContextItemSelected(MenuItem item) {// gestisce menù tocco prolungato
			{
				int menuItemIndex = item.getItemId();

				if (menuItemIndex == R.id.rename) // se viene premuto il tasto rinomina
				{
					renameDialog newFragment = new renameDialog();
					Bundle args = new Bundle();
					args.putLong("id", sessione_scelta.getSessionBegin().getTime());
					newFragment.setArguments(args);
					newFragment.show(getFragmentManager(), "rename");
					return true;
				}

				else if (menuItemIndex == R.id.delete) // se viene premuto il tasto elimina
				{
 					db.deleteSession(sessione_scelta);	//rimuovo dal database
					adapter.remove(sessione_scelta); //rimuovo dalla lista
					adapter.notifyDataSetChanged(); //aggiorno la lista
					Toast.makeText(getActivity(), sessione_scelta.getName() + getActivity().getString(R.string.session_removed), Toast.LENGTH_SHORT).show(); // notifica di avvenuta cancellazione
					return true;
				}
				return super.onContextItemSelected(item);
			}
		}
		     
		@Override
		public void onListItemClick(ListView l, View v, int position, long id) {//gestisce click su elementi della lista
			super.onListItemClick(l, v, position, id);
			sessione_scelta = (Session) getListAdapter().getItem(position);
			if (sessione_scelta.isActive()) { //se clicco una sessione attiva 
				Intent sessioneCorrente = new Intent(getActivity().getApplicationContext(), DettaglioSessioneCorrenteActivity.class);
				startActivity(sessioneCorrente);
			} 
			
			else { //se clicco una sessione passata
				Intent sessione_passata = new Intent(getActivity().getApplicationContext(), DettaglioSessionePassataActivity.class);
				startActivity(sessione_passata);
			}
		}

	}

	// classe per l'adapter
	public static class MyAdapter extends ArrayAdapter<Session> {
		ArrayList<Session> sessioni;
		Context context;

		public MyAdapter(Context context, int textVewResourceId, ArrayList<Session> sessioni) {
			super(context, textVewResourceId, sessioni);
			this.context = context;
			this.sessioni = sessioni;
			//metto una sessione attiva per test
			sessioni.get(1).setActive(true);
		}

		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = convertView;
			if (rowView == null) {
				Holder holder = new Holder();
				rowView = inflater.inflate(R.layout.adapter_lista_sessioni, parent, false);
				holder.secondaLinea = (TextView) rowView.findViewById(R.id.secondLine);
				holder.imageView = (ImageView) rowView.findViewById(R.id.thumbnail);
				holder.primaLinea = (TextView) rowView.findViewById(R.id.firstLine);
				rowView.setTag(holder);
			}
			Holder holder = (Holder) rowView.getTag();
			// imposta i campi della prima e della seconda riga
			Session sessione = sessioni.get(position);
			holder.primaLinea.setText(sessione.getName());
			String data = (String) DateFormat.format("dd/MM/yy", sessione.getSessionBegin());
			String ora = (String) DateFormat.format("kk:mm", sessione.getSessionBegin());
			String seconda_riga = "Data e ora inizio: " + data + " " + ora + " - Durata: " + conver_ore_minuti(sessione.getDuration())
					+ " - " + sessione.getNumberOfFalls();

			if (sessione.getNumberOfFalls() == 1)
				holder.secondaLinea.setText(seconda_riga + " caduta");
			else
				holder.secondaLinea.setText(seconda_riga + " cadute");
			// per sessione attiva cambio colore di sfondo
			boolean attiva = sessione.isActive();
			if (attiva)
				rowView.setBackgroundColor(Color.parseColor("#C6C6FF"));
			else
				rowView.setBackgroundColor(Color.parseColor("#EFEFEF"));
			return rowView;
		}

		public String conver_ore_minuti(int millisecondi) {
			String ore_minuti = "";
			int secondi = millisecondi / 1000;
			int minuti = secondi / 60;
			int ore = minuti / 60;
			minuti = minuti % 60;
			ore_minuti = ore + "h " + minuti + "m";
			return ore_minuti;
		}

		static class Holder {// holder serve a migliorare le prestazioni nello scrolling,
							// vedi: http://developer.android.com/training/improving-layouts/smooth-scrolling.html#ViewHolder
			public TextView secondaLinea, primaLinea;
			public ImageView imageView;
			public RelativeLayout sfondo;
		}
	}// chiusura classe Adapter

}// chiusura lista sessioni activity

