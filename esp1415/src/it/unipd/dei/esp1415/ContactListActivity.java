package it.unipd.dei.esp1415;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ContactListActivity extends Activity {

	private ArrayList<String> presentContacts = new ArrayList<String>();
	private ArrayList<ContactData> contacts = new ArrayList<ContactData>();
	private Map<Integer, String> dest = new HashMap<Integer, String>();
	
	private String name;
	private String address;

	@SuppressLint("InlinedApi")
	private static final String[] PROJECTION = new String[] {
		Email.CONTACT_ID,
		Phone.DISPLAY_NAME,
		Email.ADDRESS
	};

	@SuppressLint("InlinedApi")
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contactlist);
		final ListView lv = (ListView) findViewById(R.id.lv);
		Button bn = (Button) findViewById(R.id.button);
		Button bnread = (Button) findViewById(R.id.buttonread);
		presentContacts = readSelectedContacts();
		Cursor cursor = this.getContentResolver().query(Email.CONTENT_URI, PROJECTION, null,
				null, null);
		while(cursor.moveToNext()){
			name = cursor.getString(cursor.getColumnIndex(Phone.DISPLAY_NAME));
			address = cursor.getString(cursor.getColumnIndex(Email.ADDRESS));
			if(!name.equals(address)){ 
				if(presentContacts.contains(address))
					contacts.add(new ContactData(name,address,true));
				else
					contacts.add(new ContactData(name,address,false));
			}
		}
		cursor.close();

		final ContactListArrayAdapter arrayAdapter = 
				new ContactListArrayAdapter(this, R.layout.contactlistview_row, contacts);
		lv.setAdapter(arrayAdapter);
		lv.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> arg0, View v,int position, long arg3)
			{
				String selectedAddr=arrayAdapter.items.get(position).getAddress();
				if(!arrayAdapter.items.get(position).getAdded()){
					dest.put(position, selectedAddr);
					Toast.makeText(getApplicationContext(), "Mail aggiunta: "+ selectedAddr, Toast.LENGTH_SHORT).show();
					arrayAdapter.items.get(position).setAdded(true);
				}else{
					dest.remove(position);
					Toast.makeText(getApplicationContext(), "Mail rimossa: "+ selectedAddr, Toast.LENGTH_SHORT).show();
					arrayAdapter.items.get(position).setAdded(false);
			    }
				arrayAdapter.notifyDataSetChanged();
			}
		});
		bn.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				writeSelectedContacts(dest);
			}
		});
		bnread.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				readSelectedContacts();
			}
		});

	}

	private void writeSelectedContacts(Map<Integer, String> dest){
		try {
			FileOutputStream output = openFileOutput("contactlist.txt", MODE_PRIVATE);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(output));
			Iterator<Map.Entry<Integer, String>> i = dest.entrySet().iterator();
			while(i.hasNext()){
				Map.Entry<Integer, String> entry = i.next();
				bw.append(entry.getValue()+"\r\n");
			}
			bw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private ArrayList<String> readSelectedContacts(){
		ArrayList<String> selectedContacts = new ArrayList<String>();
		TextView test = (TextView) findViewById(R.id.test);
		String str="";
		try {
			FileInputStream input = openFileInput("contactlist.txt");
			BufferedReader br = new BufferedReader(new InputStreamReader(input));

			String line;
			while((line = br.readLine()) != null){
				selectedContacts.add(line);
				str = str + line +"\r\n";
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		test.setText(str);
		return selectedContacts;
	}



	private class ContactListArrayAdapter extends ArrayAdapter<ContactData>{

		Context context;
		int resource;
		ArrayList<ContactData> items;
		
		public ContactListArrayAdapter(Context context, int resource,
				ArrayList<ContactData> items) {
			super(context, resource, items);
			this.context = context;
			this.resource = resource;
			this.items = items;
		}

		public View getView(int position, View convertView, ViewGroup parent){
			LayoutInflater inflater = getLayoutInflater();
			convertView = inflater.inflate(R.layout.contactlistview_row, parent, false);
			TextView tv = (TextView) convertView.findViewById(R.id.tv);
			tv.setText(items.get(position).getName() + ": " + items.get(position).getAddress());
			if(items.get(position).getAdded())
				tv.setBackgroundColor(Color.GRAY);
			else
				tv.setBackgroundColor(Color.WHITE);
			return convertView;
		}
	}

	private class ContactData{

		private String name;
		private String address;
		private boolean added;

		public ContactData(String name, String address, boolean added){
			this.name = name;
			this.address = address;
			this.added = added;
		}

		public String getName() {
			return name;
		}

		public String getAddress() {
			return address;
		}


		public boolean getAdded() {
			return added;
		}

		public void setAdded(boolean added) {
			this.added = added;
		}
	}
}