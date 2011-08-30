package net.lapalta.android.chilepostal;

import java.util.Arrays;

import net.lapalta.android.chilepostal.PostalGetter;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.ClipboardManager;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ChilePostal extends Activity implements Runnable {
	
	private EditText edit_calle = null;
	private EditText edit_numero = null;
	private AutoCompleteTextView edit_comuna = null;
	private Button btn_buscar = null;
	private Button btn_historial = null;
	private TextView text1 = null;
	private TextView text2 = null;
	private TextView label_about = null;
	private TextView label_last = null;
	private TextView label_appname = null;
	
	SQLiteDatabase db = null;
	private String DB_NAME = "chilepostal.db";
	private String DB_TABLE = "codigos";
	
	private ProgressDialog dialog = null;
	
	int current_postal;
	String direccion;
	
	public ProgressDialog createDialog(String str) {
        this.dialog = new ProgressDialog(this);
        this.dialog.setMessage(str);
        this.dialog.setIndeterminate(true);
        this.dialog.setCancelable(false);
        return dialog;
	}
	
	public void showNotif(String msg) {
        Toast.makeText(ChilePostal.this, msg,
                Toast.LENGTH_SHORT).show();
	}
	
	public boolean checkFields()
	{
		if (edit_calle.getText().toString().equals("")) {
			showNotif("Ingrese una calle...");
			return false;
		}
		if (edit_numero.getText().toString().equals("")) {
			showNotif("Ingrese un número...");
			return false;
		}
		if (edit_comuna.getText().toString().equals("")) {
			return false;
		} else {
			if (validarComuna(edit_comuna.getText().toString())==false) {
				showNotif("Comuna no válida");
				return false;
			}
		}
		return true;
	}
	public boolean validarComuna(String comuna) {
		String tmp_comuna = comuna.toUpperCase();
		String[] comunas = getBaseContext().getResources().getStringArray(R.array.comunas);
		if (Arrays.asList(comunas).contains(tmp_comuna)) {
			return true;
		}
		return false;
	}
	public void loadPostal() {
		createDialog("Espere...");
		dialog.show();
		normalizeFields();
        Thread thread = new Thread(this);
        thread.start();
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
	
	public void savePostal() {
		if (this.db==null)
			return;
		
        ContentValues cv = new ContentValues();
        cv.put("direccion", this.direccion);
        cv.put("codigo_postal", this.current_postal);
        
        db.insert(this.DB_TABLE, null, cv);
        setInitialTextFields(this.text1, this.text2);
	}
	public void setInitialTextFields(TextView text1, TextView text2)
	{
		label_last.setVisibility(View.VISIBLE);
		text1.setVisibility(View.VISIBLE);
		text2.setVisibility(View.VISIBLE);
		if (db==null) {
			text1.setText("(vacío)");
			text2.setText("");
			return;
		}
		Cursor c;
		try {
			c = db.query(this.DB_TABLE, new String[] {"_id", "direccion", "codigo_postal"} , null, null, null, null, "_id DESC");
			startManagingCursor(c);
			c.moveToFirst();
		} catch (SQLException e) {
			showNotif(e.toString());
			return;
		}
        if (c.getCount()>0) {
        	if (text1!=null) {
        		text1.setText(c.getString(1));
        	}
        	if (text2!=null) {
        		text2.setText(String.valueOf(c.getInt(2)));
        	}
        } else {
			text1.setText("(vacío)");
			text2.setText("");
        }
        c.close();
	}
	
	public void run() {
		PostalGetter pg = new PostalGetter();
		try {
			//showNotif("Comuna: "+edit_comuna.getText().toString());
			current_postal = pg.getPostal(
					edit_comuna.getText().toString(),
					edit_calle.getText().toString(),
					Integer.parseInt(edit_numero.getText().toString()));
			direccion = edit_calle.getText().toString() + " " + edit_numero.getText().toString() + ", " + edit_comuna.getText().toString();
			handler.sendEmptyMessage(0);
		} catch (Exception e ) {
			showNotif("Error: "+e.toString());
		}
	}
	
	public Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (dialog!=null) {
				dialog.dismiss();
			}
			//createDialog("Codigo: "+currentPostal).show();
			if (current_postal!=0) {
				//showNotif("Codigo encontrado. Se ha guardado en la lista de búsquedas recientes.");
				showNotif("Código encontrado");
				savePostal();
				//savePostal();
			} else {
				showNotif("No se ha encontrado el código. Por favor busque nuevamente.");
			}
		}
	};
	public void closeKeyboard()
	{
		if (btn_buscar!=null) {
			InputMethodManager inputManager = (InputMethodManager) getBaseContext().getSystemService(Context.INPUT_METHOD_SERVICE); 
			inputManager.hideSoftInputFromWindow(btn_buscar.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}
	public void normalizeFields() {
		if (this.edit_calle!=null) {
			this.edit_calle.setText(this.edit_calle.getText().toString().toUpperCase());
		}
		// TODO check if comuna is in array
		if (this.edit_comuna!=null) {
			this.edit_comuna.setText(this.edit_comuna.getText().toString().toUpperCase());
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		closeKeyboard();
		setInitialTextFields(text1,text2);
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
    	edit_calle = (EditText) findViewById(R.id.edit_calle);
    	edit_numero = (EditText) findViewById(R.id.edit_numero);
    	edit_comuna = (AutoCompleteTextView) findViewById(R.id.edit_comuna);
    	btn_buscar = (Button) findViewById(R.id.btn_buscar);
    	text1 = (TextView) findViewById(R.id.text1);
    	text2 = (TextView) findViewById(R.id.text2);
    	label_about = (TextView) findViewById(R.id.label_about);
       	label_last = (TextView) findViewById(R.id.label_last);
       	label_appname = (TextView) findViewById(R.id.text_appname);
        btn_historial = (Button) findViewById(R.id.btn_historial);
    	
        
    	text1.setText("");
    	text2.setText("");
    	
    	text1.setVisibility(View.GONE);
    	text2.setVisibility(View.GONE);
    	label_last.setVisibility(View.GONE);
    	try {
			label_appname.setText("Códigos Postales Chile v"+getBaseContext().getPackageManager().getPackageInfo(getBaseContext().getPackageName(), 0).versionName.toString());
		} catch (NameNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
    	
        initDB();
        setInitialTextFields(text1, text2);
    	
        try{
        	this.btn_buscar.setOnClickListener(new OnClickListener() {
        		@Override
        		public void onClick(View v) {
        			closeKeyboard();
        			normalizeFields();
        			if (checkFields()) {
        				loadPostal();
        			}
        		}
        	});
        } catch (Exception e) {
        	showNotif(e.toString());
        }
        this.text1.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		try {
        			Intent myIntent = new Intent(getBaseContext(), PostalHistory.class);
        			startActivity(myIntent);
        		} catch (Exception e) {
        			showNotif(e.toString());
        		}
        	}
        });
        this.btn_historial.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		try {
        			Intent myIntent = new Intent(getBaseContext(), PostalHistory.class);
        			startActivity(myIntent);
        		} catch (Exception e) {
        			showNotif(e.toString());
        		}
        	}
        });
        /*text2.setOnLongClickListener(new OnLongClickListener() {
        	@Override
        	public boolean onLongClick(View v) {
        		
        		return true;
        	}
        });*/
        registerForContextMenu(text2);
        registerForContextMenu(text1);
        
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.comunas, android.R.layout.simple_dropdown_item_1line);
        try {
        //showNotif(adapter.getItem(2).toString());
        } catch (Exception e) {
        	showNotif(e.toString());
        }
        AutoCompleteTextView textView = (AutoCompleteTextView)
                findViewById(R.id.edit_comuna);
        textView.setAdapter(adapter);
        
        closeKeyboard();
        
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
        ContextMenuInfo menuInfo) {
      if (v.getId()==R.id.text2||v.getId()==R.id.text1) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
        //menu.setHeaderTitle(Countries[info.position]);
        //String[] menuItems = getResources().getStringArray(R.array.menu);
        //for (int i = 0; i<menuItems.length; i++) {
        //  menu.add(Menu.NONE, i, i, menuItems[i]);
        //}
        menu.setHeaderTitle("Opciones");
        menu.add(Menu.NONE, 0, 0, "Copiar código postal");
        menu.add(Menu.NONE, 1, 1, "Copiar dirección");
        
      }
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
	    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
	    int menuItemIndex = item.getItemId();
	    ClipboardManager cM = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

	    if (menuItemIndex==0) {
	    	if (text2!=null)
	    		cM.setText(text2.getText().toString());
	    		showNotif("Copiado código postal: "+text2.getText().toString());
	    }
	    if (menuItemIndex==1) {
	    	if (text1!=null) {
	    		cM.setText(text1.getText().toString());
	    		showNotif("Copiada dirección: "+text1.getText().toString());
	    	}
	    }
		//String[] menuItems = getResources().getStringArray(R.array.menu);
		//String menuItemName = menuItems[menuItemIndex];
	    //String listItemName = Countries[info.position];

	    //TextView text = (TextView)findViewById(R.id.footer);
	    //text.setText(String.format("Selected %s for item %s", menuItemName, listItemName));
    	return true;
    }
}