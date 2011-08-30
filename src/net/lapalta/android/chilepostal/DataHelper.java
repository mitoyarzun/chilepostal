package net.lapalta.android.chilepostal;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.database.sqlite.SQLiteDatabase.CursorFactory;



public class DataHelper {
	private String DB_NAME = "chilepostal.db";
	private String DB_TABLE = "codigos";
	CursorFactory cf = null;
	SQLiteDatabase db = null;
	
	public DataHelper() {
		//this.db = new SQLiteDatabase();
		
		this.db = SQLiteDatabase.openOrCreateDatabase(
				this.DB_NAME, cf);
		/*this.db = SQLiteDatabase.openOrCreateDatabase(
            	"codigos2.db",
            	SQLiteDatabase.CREATE_IF_NECESSARY,
            	null
        );*/
	}
}
