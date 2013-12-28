package com.example.KGraph;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by yangj on 13-12-13.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "/storage/sdcard1/stocks/stock.db";
    private static final int DATABASE_VERSION = 1;

    public DBHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("create table if not exists Stock(_id INTEGER PRIMARY KEY AUTOINCREMENT,CODE varchar,NAME varchar,INDUSTRY varchar,REGION varchar,OPENDATE text,LASTDATE text)" );
        db.execSQL("create table if not exists StockDay(_id INTEGER PRIMARY KEY AUTOINCREMENT,CODE varchar,NAME varchar,TRANSDATE text,TCLOSE float,HIGH float,LOW float,TOPEN float,LCLOSE float,CHG float,PCHG float,TURNOVER float,VOTURNOVER float,VATURNOVER float,TCAP float,MCAP float)" );
        db.execSQL("create table if not exists StockDayDeal(_id INTEGER PRIMARY KEY AUTOINCREMENT,CODE varchar,TRANSDATE text,DEALTIME text,PRICE float,PRICECHANGE varchar,DEALCOUNT float,DEALAMOUNT float,DEALTYPE varchar)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){
        db.execSQL("alter table StockDay add column other string");
    }
}
