package it.unipd.dei.esp1415;


import java.util.ArrayList;
import java.util.Date;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

/**
 * Questa activity conterrà il dettaglio sulla sessione corrente.
 * Descrizione di come interagisce col WatcherService:
 * 1)creo una nuova sessione e imposto la sessione come attiva nel db o ripristino la sessione in corso se è già presente una sessione attiva nel db;
 * 2)controllo se il service è già attivo, se è attivo mostro pausa, se non è attivo mostro il tasto play
 * 3)premuto play il service viene avviato;
 * 4)premuto pausa uccido il service;
 * 5)premuto stop imposto la sessione come non attiva nel db e uccido il service;
 * 6)uso un broadcast receiver per tenere aggiornata l'UI mentre l'app è in foreground;
 */
public class DettaglioSessioneCorrenteActivity extends ActionBarActivity {

	private static Session currentSession;
	private static DBManager db;
	private static Boolean serviceRunning;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dettaglio_sessione_corrente);
		if (savedInstanceState == null) {
			FragmentTransaction  fm = getSupportFragmentManager().beginTransaction();
			fm.add(R.id.dettaglio_sessione_corrente_fragment, new DettagliSessioneFragment());
			fm.add(R.id.lista_cadute_fragment, new MyListFragment());
			fm.commit();
		}
		serviceRunning = isMyServiceRunning(WatcherService.class);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.dettaglio_sessione_corrente, menu);
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
	public static class DettagliSessioneFragment extends Fragment {

		private TextView xValue;
		private TextView yValue;
		private TextView zValue;

		public DettagliSessioneFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(
					R.layout.fragment_dettaglio_sessione_corrente, container,
					false);
			//imposto la connessione al db
			db = new DBManager(getActivity().getBaseContext());
			db.open();	
			//se non sono presenti sessioni attive creo una nuova sessione
			if(db.hasActiveSession()){
				//è presente se l'app è andata in background mentre c'era una sessione attiva
				currentSession = db.getActiveSession();
			} 
			else {
				currentSession = db.createSession("Nuova Sessione");
				currentSession.setActive(true);
			}
			this.getActivity().setTitle("Sessione attiva");
			//TextView timeStampSessioneTextView = (TextView) rootView.findViewById(R.id.timestampsessione);
			EditText nomeSessione = (EditText) rootView.findViewById(R.id.nomeSessione);			
			xValue = (TextView) rootView.findViewById(R.id.xValue);
			yValue = (TextView) rootView.findViewById(R.id.yValue);
			zValue = (TextView) rootView.findViewById(R.id.zValue);
			TextView durataSessioneTextView = (TextView) rootView.findViewById(R.id.durataSessione);
			ImageButton playPauseButton = (ImageButton) rootView.findViewById(R.id.playPauseButton);
			ImageButton stopButton = (ImageButton) rootView.findViewById(R.id.stopButton);
			//String timestamp = (String) DateFormat.format("dd/MM/yy - kk:mm", currentSession.getSessionBegin());
			//timeStampSessioneTextView.setText(timestamp);
			//durataSessioneTextView.setText(conver_ore_minuti(currentSession.getDuration()));
			//imposto in modo che una volta modificato il nome della sessione la tastiera sparisca
			nomeSessione.setImeOptions(EditorInfo.IME_ACTION_DONE);
			nomeSessione.setOnEditorActionListener(new OnEditorActionListener() {
			    @Override
			    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			    	boolean handled = false;
			    	if (actionId == EditorInfo.IME_ACTION_DONE) {
			    		InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
			    		imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
			    		v.clearFocus();
			    		currentSession.setName(v.getText().toString());
			    		db.renameSession(currentSession);
			    		handled = true;
			    	}
			    	return handled;
			    }
			});
			//imposto i valori iniziali dei campi del layout
			nomeSessione.setText(currentSession.getName());
			durataSessioneTextView.setText(conver_ore_minuti(currentSession.getDuration()));

			//l'immagine e il comportamento del tasto play cambiano in base al servizio se sta andando o no
			playPauseButton.setImageResource(R.drawable.ic_play_button_256);
			if(serviceRunning){
				playPauseButton.setImageResource(R.drawable.ic_pause_button_256);
			}
			playPauseButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if(serviceRunning){
						//uccido il service però gli dico di non disattivare la sessione
						Intent i= new Intent(getActivity(), WatcherService.class);
						i.putExtra("Active", true);
						getActivity().stopService(i);
						((ImageButton)arg0).setImageResource(R.drawable.ic_play_button_256);
						serviceRunning = false;
					}
					else{
						// Avvio il service
						Intent i= new Intent(getActivity(), WatcherService.class);
						// Passo al service le informazioni sulla sessione attiva
						i.putExtra("IDSessione", currentSession.getSessionBegin());
						PendingIntent.getBroadcast(getActivity(), PendingIntent.FLAG_UPDATE_CURRENT, i, PendingIntent.FLAG_UPDATE_CURRENT);
						getActivity().startService(i); 
						((ImageButton)arg0).setImageResource(R.drawable.ic_pause_button_256);
						serviceRunning = true;
					}
				}
			});	
			stopButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					
						//uccido il service e gli dico di disattivare la sessione
						Intent i= new Intent(getActivity(), WatcherService.class);
						i.putExtra("Active", false);
						getActivity().stopService(i);
						serviceRunning=false;
						Intent s= new Intent(getActivity(), DettaglioSessionePassataActivity.class);
						s.putExtra("IDSessione", currentSession.getSessionBegin());
						startActivity(s);
					
				}
			});



			return rootView;
		}
		
		// handler for received Intents for the "my-event" event 
		private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		  @Override
		  public void onReceive(Context context, Intent intent) {
		    // Extract data included in the Intent
			  Float x = intent.getFloatExtra("xValue",0f);
			  Float y = intent.getFloatExtra("yValue",0f);
			  Float z = intent.getFloatExtra("zValue",0f);
		    xValue.setText(x.toString());
		    yValue.setText(y.toString());
		    zValue.setText(z.toString());
		  }
		};
		
		@Override
		public void onResume() {
		  super.onResume();

		  // Register mMessageReceiver to receive messages.
		  LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(mMessageReceiver,
		      new IntentFilter("AccData"));
		}

		

		@Override
		public void onPause() {
		  // Unregister since the activity is not visible
		  LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(mMessageReceiver);
		  super.onPause();
		} 

	}
	
	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class MyListFragment extends ListFragment {
		FallAdapter adapter;
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
//			ArrayList<Session> sessions = (ArrayList<Session>)db.getAllSessions();
//			Session currentSession = sessions.get(2);
			currentSession.setFallList(new ArrayList<Fall>());
			adapter = new FallAdapter(getActivity().getBaseContext(), currentSession.getFallList());
			setListAdapter(adapter);

		}

		// handler for received Intents for the "my-event" event 
		private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				// Extract data included in the Intent
				Float x = intent.getFloatExtra("xValue",0f);
				Float y = intent.getFloatExtra("yValue",0f);
				Float z = intent.getFloatExtra("zValue",0f);
				currentSession.getFallList().add(new Fall.FallBuilder(new Date())
				.session(currentSession.getSessionBegin())
				.fallNumber(currentSession.getFallList().size())
				.notified(false)
				.latitude(0.0)
				.longitude(0.0)
				.build());
				adapter.notifyDataSetChanged();
			}
		};

		@Override
		public void onResume() {
			super.onResume();

			// Register mMessageReceiver to receive messages.
			LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(mMessageReceiver,
					new IntentFilter("Fall"));
		}



		@Override
		public void onPause() {
			// Unregister since the activity is not visible
			LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(mMessageReceiver);
			super.onPause();
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
	
	/**
	 * controllo se il service è già stato avviato
	 * @param serviceClass
	 * @return
	 */
	private boolean isMyServiceRunning(Class<?> serviceClass) {
	    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (serviceClass.getName().equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}

	
}
