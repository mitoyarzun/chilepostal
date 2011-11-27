package net.lapalta.android.chilepostal;

import java.util.HashMap;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;
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
        menu.setHeaderTitle("Opciones");
        menu.add(Menu.NONE, 0, 0, "Copiar código postal");
        menu.add(Menu.NONE, 1, 1, "Copiar dirección");
        menu.add(Menu.NONE, 2, 2, "Borrar ítem");
        
    }

	
    @Override
    public boolean onContextItemSelected(MenuItem item) {
	    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
	    int menuItemIndex = item.getItemId();
	    
	    ClipboardManager cM = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
	    if (menuItemIndex==1) {
	    	showNotif("Copiada dirección: "+item_direccion.get(info.position));
	    	cM.setText(item_direccion.get(info.position));
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
	}
	
	public void deleteFromDatabase(String direccion, int postal) {
		initDB();
		if (db!=null) {
			// TODO: make safe input
			db.execSQL("DELETE FROM codigos WHERE direccion = '"+direccion+ "' AND codigo_postal = '"+postal+"';");
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
		if (c!=null&&c.getCount()>0) {
			do {
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
	}
	
	public void showNotif(String msg) {
        Toast.makeText(PostalHistory.this, msg,
                Toast.LENGTH_SHORT).show();
	}
	
}
