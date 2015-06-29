package it.unipd.dei.esp1415;


import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateFormat;
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
 * Questa activity conterrà il dettaglio su una sessione passata
 */
public class DettaglioSessionePassataActivity extends ActionBarActivity
												implements renameDialog.renameDialogListener{

	private static Session currentSession;
	private static DBManager db;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		db = new DBManager(this);
		db.open();
		
		currentSession = db.getSession(new Date(intent.getLongExtra("IDSessione",0L)));
		setContentView(R.layout.activity_dettaglio_sessione_passata);
		if (savedInstanceState == null) {
			FragmentTransaction  fm = getSupportFragmentManager().beginTransaction();
			fm.add(R.id.dettaglio_sessione_passata_fragment, new DettagliSessioneFragment());
			fm.add(R.id.lista_cadute_fragment, new MyListFragment());
			fm.commit();
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
		if (id == R.id.action_rename) {
			showRenameDialog();
		    
		}
		return super.onOptionsItemSelected(item);
	}

	 public void showRenameDialog() {
	        // Create an instance of the dialog fragment and show it
		 	renameDialog newFragment = new renameDialog();
			Bundle args = new Bundle();
			args.putLong("id", currentSession.getSessionBegin().getTime());
			newFragment.setArguments(args);
		    newFragment.show(getSupportFragmentManager(), "rename");
	    }
	
	@Override
	public void onDialogPositiveClick(renameDialog dialog) {
		currentSession = db.getSession(currentSession.getSessionBegin());
		setTitle(currentSession.getName());
	}

	@Override
	public void onDialogNegativeClick(renameDialog dialog) {
		
	}


	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class DettagliSessioneFragment extends Fragment {

		public DettagliSessioneFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(
					R.layout.fragment_dettaglio_sessione_passata, container,
					false);
			//solo per testing prendo tutte le sessioni dal db
			
//			ArrayList<Session> sessions = (ArrayList<Session>)db.getAllSessions();
//			currentSession = sessions.get(2);
			currentSession.setFallList((ArrayList<Fall>)db.getAllFalls(currentSession.getSessionBegin()));
			// caricamento thumbnail
			String thumbnailName = currentSession.getThumbnail();
			Bitmap thumbnail = SettingValues.loadImageFromStorage(thumbnailName, getActivity().getApplicationContext());
			ImageView thumbnailImageView = (ImageView) rootView.findViewById(R.id.thumbnailSessione);
			thumbnailImageView.setImageBitmap(thumbnail);		
			TextView timeStampSessioneTextView = (TextView) rootView.findViewById(R.id.timestampsessione);
			TextView durataSessioneTextView = (TextView) rootView.findViewById(R.id.durataSessione);
			String timestamp = (String) DateFormat.format("dd/MM/yy - kk:mm", currentSession.getSessionBegin());
			timeStampSessioneTextView.setText(timestamp);
			durataSessioneTextView.setText(conver_ore_minuti(currentSession.getDuration()));
			this.getActivity().setTitle(currentSession.getName());
			return rootView;
		}
	}
	
	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class MyListFragment extends ListFragment {
		private Fall caduta_scelta;
		private FallAdapter adapter;

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
			currentSession.setFallList((ArrayList<Fall>)db.getAllFalls(currentSession.getSessionBegin()));
			adapter = new FallAdapter(getActivity().getBaseContext(), currentSession.getFallList());
			setListAdapter(adapter);
		}
		
		@Override
		public void onListItemClick(ListView l, View v, int position, long id) {//gestisce click su elementi della lista
			super.onListItemClick(l, v, position, id);
			caduta_scelta = (Fall) getListAdapter().getItem(position);
			Intent dettaglio_caduta = new Intent(getActivity().getApplicationContext(), DettaglioCadutaActivity.class);
			Date idSessione = adapter.getItem(position).getFallTimestamp();
			dettaglio_caduta.putExtra("IDCaduta", idSessione.getTime());
			dettaglio_caduta.putExtra("NomeSessione", currentSession.getName());
			dettaglio_caduta.putExtra("timestampSessione", currentSession.getSessionBegin().getTime());
			startActivity(dettaglio_caduta);

		}
	}

	
	
	public static class FallAdapter extends ArrayAdapter<Fall> {
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
		    ImageView notifiedImageView = (ImageView) rowFallView.findViewById(R.id.notificato);
		    fallNumberTextView.setText(String.valueOf(falls.get(position).getFallNumber()));
		    String timestamp = (String) DateFormat.format("dd/MM/yy - kk:mm", falls.get(position).getFallTimestamp());
		    timestampFallTextView.setText(timestamp);
		    
		    if (falls.get(position).isNotified()) {
		    	notifiedImageView.setImageResource(R.drawable.cross);
		    } else {
		    	notifiedImageView.setImageResource(R.drawable.tick);
		    }

		    return rowFallView;
		  
		} 
	}
	
	
	public static String conver_ore_minuti (int millisecondi)
		{String ore_minuti = "";
		 int secondi = millisecondi/1000;
		 int minuti = secondi/60;
		 int ore = minuti/60;
		 minuti = minuti%60;
		 ore_minuti = ore + "h " + minuti + "m";
		 return ore_minuti;			
		}

}
