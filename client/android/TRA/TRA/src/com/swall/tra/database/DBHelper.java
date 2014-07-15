package com.swall.tra.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.webkit.URLUtil;
import com.swall.tra.TRAApplication;

/**
 * Created by pxz on 14-7-13.
 */
public class DBHelper {
    protected class SQLiteOpenHelperImpl extends android.database.sqlite.SQLiteOpenHelper {
        private String databaseName;
        private SQLiteDatabase dbR, dbW;
        private SQLiteDatabase mInnerDb;

        public SQLiteOpenHelperImpl(String name, SQLiteDatabase.CursorFactory factory,
                                    int version) {
            super(TRAApplication.getContext(), name, factory, version); //如需加密，可用自定义 factory
            this.databaseName = name;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            //createDatabase(db);
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            super.onOpen(db);
            mInnerDb = db;
        }

        public void dropAllTable() {
            dropAllTable(mInnerDb);
        }

        private void dropAllTable(SQLiteDatabase db) {
            String[] tables = getAllTableName(db);
            if (tables != null) {
                for (String tb : tables) {
                    if("android_metadata".equals(tb)){
                        continue;
                    }
                    if("sqlite_sequence".equals(tb)){
                        continue;
                    }
                    //db.execSQL(TableBuilder.dropSQLStatement(tb));
                }
            }
            onCreate(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //upgradeDatabase(db, oldVersion, newVersion);
        }



        private String[] getAllTableName(SQLiteDatabase db) {
            String sql = "select distinct tbl_name from Sqlite_master";
            Cursor c = db.rawQuery(sql, null);
            String[] tbs = null;
            int index = 0;
            if (c != null && c.moveToFirst()) {
                tbs = new String[c.getCount()];
                do {
                    tbs[index++] = c.getString(0);
                } while (c.moveToNext());
            }
            if (c != null)
            {
                c.close();
            }

            return tbs;
        }

        @Override
        public void close() {
            try {
                if (dbR != null && dbR.isOpen()) {
                    dbR.close();
                    dbR = null;
                }
                if (dbW != null && dbW.isOpen()) {
                    dbW.close();
                    dbW = null;
                }
//				QLog.i("System.out", QLog.CLR, "[DB]" + databaseName + " closed.");
            } catch (Exception e) {
            }
        }

        @Override
        public synchronized SQLiteDatabase getWritableDatabase() {
            try{
                dbW = super.getWritableDatabase();
                dbW.setLockingEnabled(false);
            } catch(Exception e){
                e.printStackTrace();
            }
            return dbW;
        }

        @Override
        public synchronized SQLiteDatabase getReadableDatabase() {
            try{
                dbR = super.getReadableDatabase();
            } catch(Exception e){}
            return dbR;
        }
    }
}
