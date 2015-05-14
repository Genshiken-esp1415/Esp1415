package it.unipd.dei.esp1415;

import it.unipd.dei.esp1415.R.drawable;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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
		  public void onActivityCreated(Bundle savedInstanceState) {
		    super.onActivityCreated(savedInstanceState);
//		    String[] values = new String[] { "Android", "iPhone", "WindowsMobile",
//		        "Blackberry", "WebOS", "Ubuntu", "Windows7", "Max OS X",
//		        "Linux", "OS/2" };
//		    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
//		        android.R.layout.simple_list_item_1, values);
			ArrayList<Session> provaRandomizer = Randomizer.randomSession(30);
		    MyAdapter adapter = new MyAdapter(getActivity().getBaseContext(), R.layout.adapter_lista_sessioni, 
					provaRandomizer);
		    setListAdapter(adapter);
		  }

		
//		@Override
//		public View onCreateView(LayoutInflater inflater, ViewGroup container,
//				Bundle savedInstanceState) {
//			View rootView = inflater.inflate(R.layout.fragment_lista_sessioni,
//					container, false);
//			
//			//ListView lista = (ListView) rootView.findViewById(R.id.listMode);
//			//ListView lista = getListView();
//			// questo mi restituisce un arraylist di sessioni con dati casuali
//			// per testing
////			ArrayList<Session> provaRandomizer = Randomizer.randomSession(30);
//
//
//			/* SimpleCursorAdapter da usare al posto di ArrayAdapter per gestire
//			 * dati del database
//			 */
////			MyAdapter adapter = new MyAdapter(getActivity().getBaseContext(), R.layout.adapter_lista_sessioni, 
////												provaRandomizer);
////			setListAdapter(adapter);
//
//			return rootView;
//
//		}
		
	}

	// classe per l'adapter
	public static class MyAdapter extends ArrayAdapter<Session> {
		//inizializzo a null altrimenti dice che le variabili potrebbe non essere inizializzate per via del 
		//ciclo for
		private final String[] session_name;
		private final Date[] session_begin;
		private final int[] duration;
		private final int[] number_of_falls;
		private final ImageView[] thumbnail;
		Context context;
		

		public MyAdapter(Context context, int textVewResourceId, ArrayList<Session> provaRandomizer)
		{
			super(context, textVewResourceId, provaRandomizer);
			this.context = context;
			int size = provaRandomizer.size();
			session_name = new String[size];
			session_begin = new Date[size];
			duration = new int[size];
			number_of_falls = new int [size];
			thumbnail = new ImageView[size];
			
			for(int i=0; i<provaRandomizer.size(); i++)
				{
				this.session_name[i] = provaRandomizer.get(i).getName();
				this.session_begin[i] = provaRandomizer.get(i).getSessionBegin();
				this.duration[i] = provaRandomizer.get(i).getDuration();
				this.number_of_falls[i] = provaRandomizer.get(i).getNumberOfFalls();
				//this.thumbnail[i] = sessioni[i].getQualcosa(); manca thumbail sessione
				//this.thumbnail[i].setImageResource(R.drawable.thumbnail_placeholder);
				}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		    View rowView = convertView;
		    if(rowView == null)
		    {
		    	Holder holder = new Holder();
		    	rowView = inflater.inflate(R.layout.adapter_lista_sessioni, parent,false );
		    	holder.textView = (TextView) rowView.findViewById(R.id.secondLine);
		    	holder.imageView = (ImageView) rowView.findViewById(R.id.thumbnail);
		    	holder.textView2 = (TextView) rowView.findViewById(R.id.firstLine);
		    	rowView.setTag(holder);
		    }
		    Holder holder = (Holder) rowView.getTag();
		    //imposta i campi della prima e della seconda riga
		    holder.textView.setText(session_name[position]);
		    holder.textView2.setText(session_begin[position] + " - " + duration[position] + " - " + 
		    					number_of_falls[position] + " cadute");
		    return rowView;
		  	}
		
		static class Holder {//holder serve a migliorare le prestazioni nello scrolling,
			// vedi: http://developer.android.com/training/improving-layouts/smooth-scrolling.html#ViewHolder
				public TextView textView, textView2;
				public ImageView imageView;
			}
}
}
