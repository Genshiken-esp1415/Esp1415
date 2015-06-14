package it.unipd.dei.esp1415;

import it.unipd.dei.esp1415.R;

import java.util.ArrayList;
import java.util.Date;
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
public class ListaSessioniActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lista_sessioni);
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
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
			Intent nuovaSessione = new Intent(ListaSessioniActivity.this,
					DettaglioSessioneCorrenteActivity.class);
			// attivazione dell'activity DettaglioSessioneCorrenteActivity.java
			startActivity(nuovaSessione);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends ListFragment {

		public PlaceholderFragment() {
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			// questo mi restituisce un arraylist di sessioni con dati casuali
			// per testing
			ArrayList<Session> provaRandomizer = Randomizer.randomSession(30);

			/*
			 * SimpleCursorAdapter da usare al posto di ArrayAdapter per gestire
			 * dati del database
			 */
			MyAdapter adapter = new MyAdapter(getActivity().getBaseContext(), R.layout.adapter_lista_sessioni, provaRandomizer);
			setListAdapter(adapter);
			ListView list_view = getListView();
			registerForContextMenu(list_view);
			}	

			@Override
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) { //gestisce tocco prolungato
			    super.onCreateContextMenu(menu, v, menuInfo);
			      if (v.getId()==android.R.id.list) {	
			      AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			      Session sessioneSel = (Session) getListAdapter().getItem(info.position);
			      menu.setHeaderTitle(sessioneSel.getName()); //imposto titolo menù
		          MenuInflater inflater = getActivity().getMenuInflater();
		          inflater.inflate(R.menu.click_lungo_lista_sessioni, menu); //aggiungo opzioni al menù
			      }
			}
			      
			
			@Override
	        public boolean onContextItemSelected(MenuItem item) {//gestisce menù tocco prolungato
	            {
	            	  int menuItemIndex = item.getItemId();
	            	  AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
				      Session sessioneSel = (Session) getListAdapter().getItem(info.position);
	            	  if(menuItemIndex == R.id.rename) //se viene premuto il tasto rinomina
	            	  {

	            		  return true;
	            	  }
	            	  
	            	  else if(menuItemIndex == R.id.delete) //se viene premuto il tasto elimina
	            	  {

	            		  //DbManager.deleteSession(sessioneSel);
	            		  Toast.makeText(getActivity(), sessioneSel.getName() + " eliminata", Toast.LENGTH_SHORT).show(); //notifica di avvenuta cancellazione
	            		  return true;
	            	  }
	               	  return super.onContextItemSelected(item);
	        }}
	            
		@Override
		public void onListItemClick(ListView l, View v, int position, long id) {//gestisce click su elementi della lista
			super.onListItemClick(l, v, position, id);
			Session sessione_selezionata = (Session) this.getListAdapter().getItem(position);
			if (sessione_selezionata.isActive()) { //se clicco una sessione attiva 
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
		private final String[] session_name;
		private final Date[] session_begin;
		private final int[] duration;
		private final int[] number_of_falls;
		private final ImageView[] thumbnail;
		private final boolean[] active;
		Context context;

		public MyAdapter(Context context, int textVewResourceId,
				ArrayList<Session> provaRandomizer) {
			super(context, textVewResourceId, provaRandomizer);
			this.context = context;
			int size = provaRandomizer.size();
			session_name = new String[size];
			session_begin = new Date[size];
			duration = new int[size];
			number_of_falls = new int[size];
			thumbnail = new ImageView[size];
			active = new boolean[size];
			// solo per test, per avere una sessione attiva
			provaRandomizer.get(0).setActive(true);
			for (int i = 0; i < provaRandomizer.size(); i++) {
				this.session_name[i] = provaRandomizer.get(i).getName();
				this.session_begin[i] = provaRandomizer.get(i)
						.getSessionBegin();
				this.duration[i] = provaRandomizer.get(i).getDuration();
				this.number_of_falls[i] = provaRandomizer.get(i)
						.getNumberOfFalls();
				// this.thumbnail[i] = sessioni[i].getQualcosa(); manca
				// thumbnail sessione
				// this.thumbnail[i].setImageResource(R.drawable.thumbnail_sessione_placeholder);
				// this.thumbnail[i].setImageResource(R.drawable.ic_launcher);
				this.active[i] = provaRandomizer.get(i).isActive();

			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = convertView;
			if (rowView == null) {
				Holder holder = new Holder();
				rowView = inflater.inflate(R.layout.adapter_lista_sessioni,
						parent, false);
				holder.secondaLinea = (TextView) rowView
						.findViewById(R.id.secondLine);
				holder.imageView = (ImageView) rowView
						.findViewById(R.id.thumbnail);
				holder.primaLinea = (TextView) rowView
						.findViewById(R.id.firstLine);
				rowView.setTag(holder);
			}
			Holder holder = (Holder) rowView.getTag();
			// imposta i campi della prima e della seconda riga
			holder.primaLinea.setText(session_name[position]);
			String data = (String) DateFormat.format("dd/MM/yy",
					session_begin[position]);
			String ora = (String) DateFormat.format("kk:mm",
					session_begin[position]);
			String seconda_riga = "Data e ora inizio: " + data + " " + ora
					+ " - Durata: " + conver_ore_minuti(duration[position])
					+ " - " + number_of_falls[position];

			if (number_of_falls[position] == 1)
				holder.secondaLinea.setText(seconda_riga + " caduta");
			else
				holder.secondaLinea.setText(seconda_riga + " cadute");
			// per sessione attiva cambio colore di sfondo
			boolean attiva = active[position];
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

		static class Holder {// holder serve a migliorare le prestazioni nello
								// scrolling,
								// vedi:
								// http://developer.android.com/training/improving-layouts/smooth-scrolling.html#ViewHolder
			public TextView secondaLinea, primaLinea;
			public ImageView imageView;
			public RelativeLayout sfondo;
		}
	}// chiusura classe Adapter

}// chiusura lista sessioni activity

