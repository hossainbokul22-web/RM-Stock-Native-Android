package com.rmstock.nativeapp;

import android.content.*;import android.database.*;import android.database.sqlite.*;import org.json.*;import java.io.*;import java.util.*;

public class StockDb extends SQLiteOpenHelper {
    private static final String DB="rmstock_native.db"; private static final int VER=4; private final Context ctx;
    StockDb(Context c){super(c,DB,null,VER);ctx=c;}
    public void onCreate(SQLiteDatabase d){
        d.execSQL("CREATE TABLE rms(code TEXT PRIMARY KEY,name TEXT NOT NULL,uom TEXT NOT NULL,opening REAL NOT NULL DEFAULT 0)");
        d.execSQL("CREATE TABLE products(code INTEGER PRIMARY KEY,name TEXT NOT NULL)");
        d.execSQL("CREATE TABLE recipes(rm_code TEXT NOT NULL,product_code INTEGER NOT NULL,qty REAL NOT NULL DEFAULT 0,PRIMARY KEY(rm_code,product_code))");
        d.execSQL("CREATE TABLE batches(id INTEGER PRIMARY KEY AUTOINCREMENT,date TEXT,product_code INTEGER,shift INTEGER,qty REAL,saved_at TEXT)");
        d.execSQL("CREATE TABLE receives(id INTEGER PRIMARY KEY AUTOINCREMENT,date TEXT,rm_code TEXT,qty REAL,note TEXT,voucher_no TEXT,source TEXT)");
        d.execSQL("CREATE TABLE approvals(id INTEGER PRIMARY KEY AUTOINCREMENT,type TEXT,old_value TEXT,new_value TEXT,meta TEXT,status TEXT,user TEXT,created_at TEXT)");
        d.execSQL("CREATE TABLE custom_sheets(id TEXT PRIMARY KEY,name TEXT,description TEXT,access TEXT)");
        d.execSQL("CREATE TABLE custom_cols(id INTEGER PRIMARY KEY AUTOINCREMENT,sheet_id TEXT,name TEXT,type TEXT,formula TEXT,position INTEGER)");
        d.execSQL("CREATE TABLE custom_rows(id INTEGER PRIMARY KEY AUTOINCREMENT,sheet_id TEXT,row_json TEXT)");
        d.execSQL("CREATE TABLE settings(k TEXT PRIMARY KEY,v TEXT)");
        d.execSQL("CREATE TABLE mixing2_batch_log(id INTEGER PRIMARY KEY AUTOINCREMENT,batch_id INTEGER NOT NULL,date TEXT,product_code INTEGER,shift INTEGER,rm_code TEXT,recipe_qty REAL,batch_count REAL,consumption REAL,created_at TEXT,UNIQUE(batch_id,rm_code))");
        d.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_batches_shift ON batches(date,product_code,shift)");
        seed(d);
    }
    private void seed(SQLiteDatabase d){try{
        JSONObject data=new JSONObject(readAsset("data.json")); String company=data.optString("company","RM Stock"); putSetting(d,"company",company); putSetting(d,"role","Admin");
        JSONArray rms=data.getJSONArray("rms"); for(int i=0;i<rms.length();i++){JSONObject x=rms.getJSONObject(i);ContentValues v=new ContentValues();v.put("code",String.valueOf(x.get("code")));v.put("name",x.getString("name"));v.put("uom",x.optString("uom","kg"));v.put("opening",x.optDouble("opening",0));d.insert("rms",null,v);}
        JSONArray ps=data.getJSONArray("products");for(int i=0;i<ps.length();i++){JSONObject x=ps.getJSONObject(i);ContentValues v=new ContentValues();v.put("code",x.getInt("code"));v.put("name",x.getString("name"));d.insert("products",null,v);}
        JSONObject rec=data.getJSONObject("recipe");Iterator<String> keys=rec.keys();while(keys.hasNext()){String rm=keys.next();JSONArray a=rec.getJSONArray(rm);for(int i=0;i<a.length();i++){if(i>=ps.length())break;ContentValues v=new ContentValues();v.put("rm_code",rm);v.put("product_code",ps.getJSONObject(i).getInt("code"));v.put("qty",a.optDouble(i,0));d.insert("recipes",null,v);}}
    }catch(Exception e){throw new RuntimeException(e);}}
    private String readAsset(String n)throws Exception{InputStream in=ctx.getAssets().open(n);ByteArrayOutputStream b=new ByteArrayOutputStream();byte[] buf=new byte[8192];int k;while((k=in.read(buf))>0)b.write(buf,0,k);return b.toString("UTF-8");}
    private void putSetting(SQLiteDatabase d,String k,String v){ContentValues x=new ContentValues();x.put("k",k);x.put("v",v);d.insertWithOnConflict("settings",null,x,SQLiteDatabase.CONFLICT_REPLACE);}
    String setting(String k,String def){Cursor c=getReadableDatabase().rawQuery("SELECT v FROM settings WHERE k=?",new String[]{k});try{return c.moveToFirst()?c.getString(0):def;}finally{c.close();}}
    void setSetting(String k,String v){putSetting(getWritableDatabase(),k,v);}
    public void onUpgrade(SQLiteDatabase d,int o,int n){
        if(o<2){
            // Batch count is one editable value per date + product + shift.
            // Merge accidental duplicate rows before enforcing uniqueness.
            d.execSQL("CREATE TABLE batches_mig(id INTEGER PRIMARY KEY AUTOINCREMENT,date TEXT,product_code INTEGER,shift INTEGER,qty REAL,saved_at TEXT)");
            d.execSQL("INSERT INTO batches_mig(date,product_code,shift,qty,saved_at) SELECT date,product_code,shift,SUM(qty),MAX(saved_at) FROM batches GROUP BY date,product_code,shift");
            d.execSQL("DROP TABLE batches");
            d.execSQL("ALTER TABLE batches_mig RENAME TO batches");
            d.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_batches_shift ON batches(date,product_code,shift)");
        }
        if(o<3){ d.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_batches_shift ON batches(date,product_code,shift)"); }
        if(o<4){
            d.execSQL("CREATE TABLE IF NOT EXISTS mixing2_batch_log(id INTEGER PRIMARY KEY AUTOINCREMENT,batch_id INTEGER NOT NULL,date TEXT,product_code INTEGER,shift INTEGER,rm_code TEXT,recipe_qty REAL,batch_count REAL,consumption REAL,created_at TEXT,UNIQUE(batch_id,rm_code))");
            d.execSQL("INSERT OR IGNORE INTO mixing2_batch_log(batch_id,date,product_code,shift,rm_code,recipe_qty,batch_count,consumption,created_at) SELECT b.id,b.date,b.product_code,b.shift,r.rm_code,r.qty,b.qty,b.qty*r.qty,b.saved_at FROM batches b JOIN recipes r ON r.product_code=b.product_code AND r.qty<>0");
        }
    }
    List<JSONObject> rows(String sql,String[] args)throws Exception{Cursor c=getReadableDatabase().rawQuery(sql,args);List<JSONObject> out=new ArrayList<>();try{while(c.moveToNext()){JSONObject x=new JSONObject();for(int i=0;i<c.getColumnCount();i++)x.put(c.getColumnName(i),c.getString(i));out.add(x);}}finally{c.close();}return out;}
    JSONObject one(String sql,String[] args)throws Exception{List<JSONObject> a=rows(sql,args);return a.isEmpty()?null:a.get(0);}
    long insert(String table,ContentValues v){return getWritableDatabase().insertOrThrow(table,null,v);}
    int update(String table,ContentValues v,String where,String[] args){return getWritableDatabase().update(table,v,where,args);}
    void delete(String table,String where,String[] args){getWritableDatabase().delete(table,where,args);}
}
