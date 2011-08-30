package net.lapalta.android.chilepostal;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.Contacts.Phones;
import android.text.ClipboardManager;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class PostalHistory extends ListActivity {
	
	private SQLiteDatabase db = null;
	private String DB_NAME = "chilepostal.db";
	private String DB_TABLE = "codigos";

	private HashMap<Integer, Integer> item_postal = new HashMap<Integer, Integer>();
	private HashMap<Integer, String> item_direccion = new HashMap<Integer, String>();
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this.setContentView(android.R.layout.simple_list_item_1);
        
        //this.setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new String[] {"hola", "chao"}));
        // Get a cursor with all phones
        //Cursor c = getContentResolver().query(Phones.CONTENT_URI, null, null, null, null);
        //startManagingCursor(c);
        
        // Map Cursor columns to views defined in simple_list_item_2.xml
       /* ListAdapter adapter = new SimpleCursorAdapter(this,
                android.R.layout.two_line_list_item, c, 
                        new String[] { Phones.NAME, Phones.NUMBER }, 
                        new int[] { android.R.id.text1, android.R.id.text2 });
        setListAdapter(adapter);*/
        initDB();
        fillList();
        registerForContextMenu(this.getListView());
        this.getListView().setOnItemClickListener(new OnItemClickListener() {
        	@Override
        	public void onItemClick(AdapterView<?> a, View v, int position, long id) {
        		try {
        			Bundle bundle = new Bundle();
        			bundle.putString("address",item_direccion.get(position).toString() );
        			bundle.putString("postal_code",item_postal.get(position).toString() );
        			Intent myIntent = new Intent(getBaseContext(), Mapa.class);
        			myIntent.putExtras(bundle);
        			startActivity(myIntent);
        		} catch (Exception e) {
        			showNotif(e.toString());
        		}
        	}
        });
        
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
        ContextMenuInfo menuInfo) {
 //     if (v.getId()==R.id.text2||v.getId()==R.id.text1) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;

        menu.setHeaderTitle("Opciones");
        menu.add(Menu.NONE, 0, 0, "Copiar código postal");
        menu.add(Menu.NONE, 1, 1, "Copiar dirección");
        menu.add(Menu.NONE, 2, 2, "Borrar ítem");
        
 //     }
    }

	
    @Override
    public boolean onContextItemSelected(MenuItem item) {
	    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
	    int menuItemIndex = item.getItemId();
	    Intent index2 = item.getIntent();
	    
	    ClipboardManager cM = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
	    TextView tv = null;
	    if (menuItemIndex==1) {
	    	//ListAdapter ad = (ListAdapter) this.getListAdapter().getItem(menuItemIndex);
	    	showNotif("Copiada dirección: "+item_direccion.get(info.position));
	    	cM.setText(item_direccion.get(info.position));
	    	//showNotif("Selected item: "+info.position + "\nDireccion: "+ item_direccion.get(info.position) + "\nPostal: "+item_postal.get(info.position));
	    	//item.getItemId();
	    }
	    if (menuItemIndex==0) {
	    	showNotif("Copiado código postal: "+item_postal.get(info.position));
	    	cM.setText(item_postal.get(info.position).toString());
	    }
	    if (menuItemIndex==2) {
	    	deleteFromDatabase(item_direccion.get(info.position), item_postal.get(info.position));
	    }
	    
    	return true;
    }
    
	public void initDB()
	{
		this.db = openOrCreateDatabase(this.DB_NAME,
			SQLiteDatabase.CREATE_IF_NECESSARY,
			null
		);
        try {
        	db.execSQL("CREATE TABLE IF NOT EXISTS "+this.DB_TABLE+" (_id INT AUTO_INCREMENT, direccion TEXT, codigo_postal INT, PRIMARY KEY(_id));");
        } catch (SQLException e) {
        	showNotif(e.toString());
        }
        /*ContentValues cv = new ContentValues();
        cv.put("direccion", "PRUEBA");
        cv.put("codigo_postal", 254243);
		db.insert(this.DB_TABLE, null, cv);*/
	}
	
	public void deleteFromDatabase(String direccion, int postal) {
		initDB();
		if (db!=null) {
			// TODO: make safe input
			//db.rawQuery("DELETE FROM codigos WHERE direccion = '"+direccion+ "' AND codigo_postal = '"+postal+"';",null);
			db.execSQL("DELETE FROM codigos WHERE direccion = '"+direccion+ "' AND codigo_postal = '"+postal+"';");
			//db.delete("codigos", "direccion = '"+direccion+"'", whereArgs)
			fillList();
		}
	}
	
	public void fillList()
	{
		Cursor c = null;
		try {
			c = db.query(this.DB_TABLE, new String[] {"_id", "direccion", "codigo_postal"} , null, null, null, null, "_id DESC");
			startManagingCursor(c);
			c.moveToFirst();	
		} catch (Exception e) {
			showNotif(e.toString());
		}
		int i = 0;
		item_postal.clear();
		item_direccion.clear();
		//c = db.rawQuery("SELECT _id, direccion, codigo_postal FROM codigos ORDER BY _id DESC;", null);
		//c.requery();
		if (c!=null&&c.getCount()>0) {
			//CursorAdapter ca = new SimpleCursorAdapter(this, android.R.layout.two_line_list_item, c, null, null);
			do {
				
				//int curr = c.getInt(2);

				//ca.getItem(c.getPosition());
				item_postal.put(i, c.getInt(c.getColumnIndex("codigo_postal")));
				item_direccion.put(i, c.getString(c.getColumnIndex("direccion")));
				i++;
			} while (c.moveToNext());
		}

		c.moveToFirst();
		ListAdapter adapter = new SimpleCursorAdapter(
				this,
				android.R.layout.two_line_list_item,
				c,
				new String[] { "direccion", "codigo_postal" },
				new int[] { android.R.id.text1, android.R.id.text2 }
		);

		this.setListAdapter(adapter);
		//View vi = (View) adapter.getItem(0);
		//c.close();
	}
	
	public void showNotif(String msg) {
        Toast.makeText(PostalHistory.this, msg,
                Toast.LENGTH_SHORT).show();
	}
	
}
