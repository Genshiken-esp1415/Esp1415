package it.unipd.dei.esp1415;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;




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
		if (id == R.id.action_settings) {
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
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_lista_sessioni,
					container, false);
			
			
			
			
			final ListView lista = (ListView) rootView.findViewById(R.id.list);
			
			//questo mi restituisce un arraylist di sessioni con dati casuali per testing
			ArrayList<Session> provaRandomizer = Randomizer.randomSession(30);
			while(true){
				
			}
			
			/* SimpleCursorAdapter da usare al posto di ArrayAdapter per gestire dati del database */
			ArrayAdapter<Session> adapter = new ArrayAdapter<Session>(getActivity(),
					android.R.layout.simple_list_item_1, provaRandomizer);
			
			lista.setListAdapter(adapter);
		

			
			return rootView;
			
		}
	}
}
