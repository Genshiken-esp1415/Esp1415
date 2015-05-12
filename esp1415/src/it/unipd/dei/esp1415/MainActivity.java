package it.unipd.dei.esp1415;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

/**
 * La main Activity contiene un redirect alla lista delle sessioni per il
 * momento. Una volta deciso quale activity fungerà da main verra incorporata in
 * questa. In ogni caso non è un problema, visto che verrà gestito tutto con i
 * fragment. Guardate qua per la descrizione di fragment: 
 * http://developer.android.com/guide/components/fragments.html
 * Per il momento frega, sviluppiamo le activity indipendentemente e poi per la visuale su tablet le uniremo in futuro.
 * Basta inserire il codice del layout nel fragment relativo all'activity, invece che nel layout.
 *
 */
public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
        
        ArrayList<Session> randomSessions = Randomizer.randomSession(10);
        DBManager db = new DBManager(this);
        db.open();
        for(int i = 0; i<10; i++){
        	db.createSession(randomSessions.get(i).getName());
        	ArrayList<Fall> falls = randomSessions.get(i).getFallList();
        	for(Fall fall : falls){
        		db.createFall(fall.getFallNumber(), fall.getLatitude(), fall.getLongitude(), fall.getFallData());
        	}
        }
        
        Intent openListaSessioni = new Intent(this, ListaSessioniActivity.class);
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
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main,
					container, false);
			return rootView;
		}
	}
}
