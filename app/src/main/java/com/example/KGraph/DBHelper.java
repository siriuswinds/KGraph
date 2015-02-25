package com.example.KGraph;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.sql.SQLException;

/**
 * Created by yangj on 13-12-13.
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "/storage/sdcard0/stocks/stock.db";
    private static final int DATABASE_VERSION = 1;

    public DBHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        try {
            //股票列表
            db.execSQL("create table if not exists Stock(_id INTEGER PRIMARY KEY AUTOINCREMENT,CODE varchar,NAME varchar,INDUSTRY varchar,REGION varchar,OPENDATE text,LASTDATE text)");
            //日线表
            db.execSQL("create table if not exists StockDay(_id INTEGER PRIMARY KEY AUTOINCREMENT,CODE varchar,NAME varchar,TRANSDATE text,TCLOSE float,HIGH float,LOW float,TOPEN float,LCLOSE float,CHG float,PCHG float,TURNOVER float,VOTURNOVER float,VATURNOVER float,TCAP float,MCAP float)");
            //分笔表
            db.execSQL("create table if not exists StockDayDeal(_id INTEGER PRIMARY KEY AUTOINCREMENT,CODE varchar,TRANSDATE text,DEALTIME text,PRICE float,PRICECHANGE varchar,DEALCOUNT float,DEALAMOUNT float,DEALTYPE varchar)");
            //交易表
            //db.execSQL("drop table if exists TRADERECORD");
            db.execSQL("create table if not exists TRADERECORD(_id INTEGER PRIMARY KEY AUTOINCREMENT,CODE varchar,TRADETIME text,PRICE float,TURNOVER float,TURNVOLUMN float,TRADETYPE varchar)");
            //账户表
            //db.execSQL("drop table StockAccount");
            db.execSQL("create table if not exists StockAccount(_id INTEGER PRIMARY KEY AUTOINCREMENT, ASSETS float, CAPITALBALANCE float, AVAILABLEBALANCE float, MARKETVALUE float, SHARES float)");
            //自选股
            db.execSQL("create table if not exists FavoriteStock(_id INTEGER PRIMARY KEY AUTOINCREMENT,CODE varchar)");
        }catch(Exception err){
            Log.e("创建数据表",err.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion){
        db.execSQL("alter table StockDay add column other string");
    }
}
