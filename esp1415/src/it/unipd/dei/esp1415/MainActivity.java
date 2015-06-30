package it.unipd.dei.esp1415;

import java.io.File;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/**
 * La main Activity contiene un redirect alla lista delle sessioni per il
 * momento. Una volta deciso quale activity fungerà da main verra incorporata in
 * questa. In ogni caso non è un problema, visto che verrà gestito tutto con i
 * fragment. Guardate qua per la descrizione di fragment:
 * http://developer.android.com/guide/components/fragments.html Per il momento
 * frega, sviluppiamo le activity indipendentemente e poi per la visuale su
 * tablet le uniremo in futuro. Basta inserire il codice del layout nel fragment
 * relativo all'activity, invece che nel layout.
 *
 */
public class MainActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new MainFragment()).commit();
		}
		// Creo il db per il primo avvio e faccio inserimenti dummy per testing,
		// se non è già stato popolato il db.
		DBManager db = new DBManager(this);
		db.open();
		/*
		 * if(db.getAllSessions().size()==0){ db.dummyInsert(); }
		 */
		db.close();

		// Se sono stati scelti in passato degli indirizzi e-mail per l'invio
		// delle notifiche, vengono letti dal corrispondente file di testo
		if ((new File(getApplicationContext().getFilesDir().getPath()
				+ "/contactlist.txt")).exists()) {
			Toast.makeText(this, "Contact list trovata", Toast.LENGTH_LONG)
					.show();
			Utilities.setSelectedContacts(this);
		}

		Intent openListaSessioni = new Intent(this, SessionListActivity.class);
		startActivity(openListaSessioni);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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

	public static class MainFragment extends Fragment {
		public MainFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(
					R.layout.fall_details_fragment, container, false);
			return rootView;
		}
	}
}
