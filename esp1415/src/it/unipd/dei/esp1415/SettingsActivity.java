package it.unipd.dei.esp1415;
import java.util.ArrayList;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class SettingsActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new OpzioniFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class OpzioniFragment extends ListFragment {

		static MyAdapter adapter;
		ArrayList<String> nomi_opzioni;
		
		public OpzioniFragment() {
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			nomi_opzioni = new ArrayList<String>(4);
			String frequenza = getString(R.string.frequenza_di_campionamento);
			String durata = getString(R.string.durata_massima);
			String contatti = getString(R.string.contatti);
			String sveglia = getString(R.string.sveglia);
			//inserisco i valori nell'ArrayList
			nomi_opzioni.add(0, frequenza);
			nomi_opzioni.add(1, durata);
			nomi_opzioni.add(2, contatti);
			nomi_opzioni.add(3, sveglia);
			//inizializzo l'adapter
			adapter = new MyAdapter(getActivity().getBaseContext(), R.layout.adapter_settings, nomi_opzioni);
			setListAdapter(adapter);
			ListView list_view = getListView();
			registerForContextMenu(list_view);

			}	
		
		     
		/*@Override
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
		}*/

	}
	
	// classe per l'adapter
	public static class MyAdapter extends ArrayAdapter<String> {
		ArrayList<String> opzioni;
		Context context;

		public MyAdapter(Context context, int textVewResourceId, ArrayList<String> opzioni) {
			super(context, textVewResourceId, opzioni);
			this.context = context;
			this.opzioni = opzioni;
		}

		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = convertView;
			if (rowView == null) {
				Holder holder = new Holder();
				rowView = inflater.inflate(R.layout.adapter_settings, parent, false);
				holder.opzione = (TextView) rowView.findViewById(R.id.nomeOpzione);
				rowView.setTag(holder);
			}
			Holder holder = (Holder) rowView.getTag();
			// imposta i campi della riga
			String testo_opzione = opzioni.get(position);
			holder.opzione.setText(testo_opzione);
			return rowView;
		}


		static class Holder {// holder serve a migliorare le prestazioni nello scrolling
			public TextView opzione;
		}
	}// chiusura classe Adapter
}
