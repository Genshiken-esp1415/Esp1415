package it.unipd.dei.esp1415;

import it.unipd.dei.esp1415.R;

import java.util.ArrayList;
import java.util.Date;
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
public class ListaSessioniActivity extends ActionBarActivity implements renameDialog.renameDialogListener{
	
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
			getSupportFragmentManager().beginTransaction().add(R.id.container, new ListaSessioniFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// inserisce il menù, aggiungendo elementi all'action bar se presenti
		getMenuInflater().inflate(R.menu.lista_sessioni, menu);
		return true;
	}

	// metodo che gestisce i click nell'action bar
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		// se viene premuto il pulsante impostazioni
		if (id == R.id.settings) {
			Intent settings = new Intent(ListaSessioniActivity.this, OpzioniActivity.class);
			// attivazione dell'activity SettingsActivity.java
			startActivity(settings);
			return true;
		} 
		// se viene premuto il pulsante nuova sessione
		else if (id == R.id.newsession) {
			// se non ci sono sessioni attive
			if (!(sDb.hasActiveSession())) {
			Intent newSession = new Intent(ListaSessioniActivity.this, DettaglioSessioneCorrenteActivity.class);
			// attivazione dell'activity DettaglioSessioneCorrenteActivity.java
			startActivity(newSession);
			}
			// notifica che c'è già una sessione attiva
			else {
			Toast.makeText(this, R.string.errore_sessione_attiva, Toast.LENGTH_SHORT).show();	
			}
			
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onDialogPositiveClick(renameDialog dialog) {
		String name = sDb.getSession(sSelectedSession.getSessionBegin()).getName();
		//rinominazione nell'adapter
		ListaSessioniFragment.sAdapter.mSessions.get(sPosition).setName(name); 
		// notifico all'adapter i cambiamenti
		ListaSessioniFragment.sAdapter.notifyDataSetChanged(); 
	}

	@Override
	public void onDialogNegativeClick(renameDialog dialog) {

	}
	
	public static class ListaSessioniFragment extends ListFragment {
	
		static MyAdapter sAdapter;
		
		public ListaSessioniFragment() {
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			// prende tutte le sessioni dal database
			sDb = new DBManager(getActivity().getBaseContext());
			sDb.open();
			// carico l'adapter
			sAdapter = new MyAdapter(getActivity().getBaseContext(), R.layout.adapter_lista_sessioni, (ArrayList<Session>)sDb.getAllSessions());
			setListAdapter(sAdapter);
			ListView listView = getListView();
			registerForContextMenu(listView);
		
			}	

		// metodo che gestisce il tocco prolungato
		@Override
		public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) { 
			super.onCreateContextMenu(menu, v, menuInfo);
			if (v.getId() == android.R.id.list) {
				AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
				sPosition = info.position;
				sSelectedSession = (Session) getListAdapter().getItem(sPosition);
				// impostazione del titolo del menù
				menu.setHeaderTitle(sSelectedSession.getName()); 
				MenuInflater inflater = getActivity().getMenuInflater();
				// aggiunta di opzioni al menù
				inflater.inflate(R.menu.click_lungo_lista_sessioni, menu);
			}
		}
		
		// metodo che gestisce il menù del tocco prolungato
		@Override
		public boolean onContextItemSelected(MenuItem item) {
			{
				int menuItemIndex = item.getItemId();
				// se viene premuto il tasto rinomina
				if (menuItemIndex == R.id.rename) {
					renameDialog newFragment = new renameDialog();
					Bundle args = new Bundle();
					args.putLong("id", sSelectedSession.getSessionBegin().getTime());
					newFragment.setArguments(args);
					newFragment.show(getFragmentManager(), "rename");
					return true;
				}
				// se viene premuto il tasto elimina
				else if (menuItemIndex == R.id.delete) {
					// rimozione dal database
 					sDb.deleteSession(sSelectedSession);	
 					// rimozione dalla lista
					sAdapter.remove(sSelectedSession); 
					// notifico all'adapter i cambiamenti
					sAdapter.notifyDataSetChanged(); 
					// notifica di avvenuta cancellazione
					Toast.makeText(getActivity(), sSelectedSession.getName() + getActivity().getString(R.string.sessione_rimossa), Toast.LENGTH_SHORT).show(); 
					return true;
				}
				return super.onContextItemSelected(item);
			}
		}
		     
		
		// metodo per gestire i click su elementi della lista
		@Override
		public void onListItemClick(ListView l, View v, int position, long id) {
			super.onListItemClick(l, v, position, id);
			sSelectedSession = (Session) getListAdapter().getItem(position);
			// se viene cliccata una sessione attiva  
			if (sSelectedSession.isActive()) { 
				Intent currentSession = new Intent(getActivity().getApplicationContext(), DettaglioSessioneCorrenteActivity.class);
				startActivity(currentSession);
			} 
			//se viene cliccata una sessione passata
			else { 
				Intent pastSession = new Intent(getActivity().getApplicationContext(), DettaglioSessionePassataActivity.class);
				Date sessionId = sAdapter.getItem(position).getSessionBegin();
				pastSession.putExtra("IDSessione", sessionId.getTime());
				startActivity(pastSession);
			}
		}

	}

	// classe per l'adapter
	public static class MyAdapter extends ArrayAdapter<Session> {
		ArrayList<Session> mSessions;
		static Context sContext;

		public MyAdapter(Context context, int textVewResourceId, ArrayList<Session> sessions) {
			super(context, textVewResourceId, sessions);
			MyAdapter.sContext = context;
			this.mSessions = sessions;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) sContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = convertView;
			if (rowView == null) {
				Holder holder = new Holder();
				rowView = inflater.inflate(R.layout.adapter_lista_sessioni, parent, false);
				holder.secondLine = (TextView) rowView.findViewById(R.id.secondLine);
				holder.imageView = (ImageView) rowView.findViewById(R.id.thumbnail);
				holder.firstLine = (TextView) rowView.findViewById(R.id.firstLine);
				holder.thirdLine = (TextView) rowView.findViewById(R.id.thirdLine);
				rowView.setTag(holder);
			}
			Holder holder = (Holder) rowView.getTag();
			// imposta i campi della prima e della seconda riga
			Session session = mSessions.get(position);
			holder.firstLine.setText(session.getName());
			String date = (String) DateFormat.format("dd/MM/yy", session.getSessionBegin());
			String hour = (String) DateFormat.format("kk:mm", session.getSessionBegin());
			String secondLine = getContext().getString(R.string.data_e_ora) + date + " " + hour;
			holder.secondLine.setText(secondLine);
			String thirdLine = getContext().getString(R.string.durata_2) + 
					conversionFromMilliseconds(session.getDuration()) + " - " + session.getNumberOfFalls();

			if (session.getNumberOfFalls() == 1){
				holder.thirdLine.setText(thirdLine + " " + getContext().getString(R.string.caduta));
				}
			else{
				holder.thirdLine.setText(thirdLine + " " + getContext().getString(R.string.cadute_min));
				}
			// per la sessione attiva cambio determinati parametri
			boolean active = session.isActive();
			if (active){
				// il 60 davanti al numero esadecimale decide la trasparenza
				rowView.setBackgroundColor(Color.parseColor("#60FFFFFF")); 
				holder.firstLine.setTypeface(null, Typeface.BOLD);
				holder.secondLine.setTypeface(null,Typeface.BOLD);
				// carattere grassetto
				holder.thirdLine.setTypeface(null,Typeface.BOLD); 
				// colore rosso del testo della prima riga (nome della sessione)
				holder.firstLine.setTextColor(Color.RED); 
				} 
			
			else{
				// sfondo trasparente
				rowView.setBackgroundColor(Color.parseColor("#00000000")); 
				holder.firstLine.setTypeface(null, Typeface.NORMAL);
				holder.secondLine.setTypeface(null,Typeface.NORMAL);
				holder.thirdLine.setTypeface(null,Typeface.NORMAL);
				holder.firstLine.setTextColor(Color.BLACK);
				}
			
			//TODO solo per test imposto tutte le thumbnail delle sessioni passate
			Date newSessionBegin = session.getSessionBegin();
			Bitmap thumnailGen = ThumbnailGenerator.createThumbnail(newSessionBegin); 
			// conversione della data in stringa
			String name = DBManager.dateToSqlDate(newSessionBegin); 
			// salvataggio in memoria della thumbnail
			if (SettingValues.saveToInternalStorage(thumnailGen, name, sContext)) { 
				// setto la thumbnail nella sessione
				session.setThumbnail(name); 
				}
			
			// impostazione della thumbnail
			name = session.getThumbnail();
			Bitmap thumbnail = SettingValues.loadImageFromStorage(name, sContext);
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

