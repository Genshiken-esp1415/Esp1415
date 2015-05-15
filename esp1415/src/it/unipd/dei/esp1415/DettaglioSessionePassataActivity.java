package it.unipd.dei.esp1415;


import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Questa activity conterrà il dettaglio su una sessione passata
 */
public class DettaglioSessionePassataActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dettaglio_sessione_passata);
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.dettaglio_sessione_passata_fragment, new PlaceholderFragment())
					.add(R.id.lista_cadute_fragment, new MyListFragment())
					.commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.dettaglio_sessione_passata, menu);
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
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(
					R.layout.fragment_dettaglio_sessione_passata, container,
					false);
			//solo per testing prendo tutte le sessioni dal db
			DBManager db = new DBManager(getActivity().getBaseContext());
			db.open();
			ArrayList<Session> sessions = (ArrayList<Session>)db.getAllSessions();
			Session currentSession = sessions.get(2);
			currentSession.setFallList((ArrayList<Fall>)db.getAllFalls(currentSession.getSessionBegin()));
			TextView timeStampSessioneTextView = (TextView) rootView.findViewById(R.id.timestampsessione);
			TextView durataSessioneTextView = (TextView) rootView.findViewById(R.id.durataSessione);
			timeStampSessioneTextView.setText(currentSession.getSessionBegin().toString());
			durataSessioneTextView.setText(currentSession.getDuration());
			return rootView;
		}
	}
	
	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class MyListFragment extends ListFragment {

		public MyListFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, 
				ViewGroup container, Bundle savedInstanceState) {

			return inflater.inflate(R.layout.fragment_lista_cadute, container, false);
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			//solo per testing prendo tutte le sessioni dal db
			DBManager db = new DBManager(getActivity().getBaseContext());
			db.open();
			ArrayList<Session> sessions = (ArrayList<Session>)db.getAllSessions();
			Session currentSession = sessions.get(2);
			currentSession.setFallList((ArrayList<Fall>)db.getAllFalls(currentSession.getSessionBegin()));
			FallAdapter adapter = new FallAdapter(getActivity().getBaseContext(), currentSession.getFallList());
			setListAdapter(adapter);

		}
	}

	
	
	public class FallAdapter extends ArrayAdapter<Fall> {
		  private final Context context;
		  private final ArrayList<Fall> falls;

		  public FallAdapter(Context context, ArrayList<Fall> values) {
		    super(context, R.layout.row_fall, values);
		    this.context = context;
		    this.falls = values;
		  }

		  @Override
		  public View getView(int position, View convertView, ViewGroup parent) {
		    LayoutInflater inflater = (LayoutInflater) context
		        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		    View rowFallView = inflater.inflate(R.layout.row_fall, parent, false);
		    TextView fallNumberTextView = (TextView) rowFallView.findViewById(R.id.numeroCaduta);
		    TextView timestampFallTextView = (TextView) rowFallView.findViewById(R.id.timestampCaduta);
		    ImageView notifiedImageView = (ImageView) rowFallView.findViewById(R.id.icon);
		    fallNumberTextView.setText(falls.get(position).getFallNumber());
		    timestampFallTextView.setText(falls.get(position).getFallTimestamp().toString());
		    
		    if (falls.get(position).isNotified()) {
		    	notifiedImageView.setImageResource(R.drawable.cross);
		    } else {
		    	notifiedImageView.setImageResource(R.drawable.tick);
		    }

		    return rowFallView;
		  }
		} 

}
