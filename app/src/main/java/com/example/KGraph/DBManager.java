package com.example.KGraph;

import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.util.Log;

import java.text.*;
import java.util.*;

/**
 * Created by yangj on 13-12-13.
 */
public class DBManager {
    private DBHelper helper;
    private SQLiteDatabase db;

    public DBManager(Context context){
        helper = new DBHelper(context);
        db = helper.getWritableDatabase();
        //deleteTradeRecords();
        //helper.onCreate(db);
        //clearData();
    }

    /**
     * 清理数据
     */
    private void clearData(){
        deleteStocks();
        deleteStockDays();
        deleteStockDayDeals();
    }

    public void deleteStocks(){
        db.delete("STOCK","",null);
    }

    public void deleteStockDays(){
        db.delete("STOCKDAY","",null);
    }

    public void deleteStockDayDeals(){db.delete("STOCKDAYDEAL","",null);}

    public void deleteTradeRecords(){db.delete("TRADERECORD","",null);}
    public void deleteAccount() {db.delete("StockAccount","",null); }
    public void deleteOldStockDay(StockDay stock){
        db.delete("STOCKDAY","CODE=? and TRANSDATE=date(?)",new String[]{stock.CODE,stock.TRANSDATE});
    }

    public void deleteStockDayDeals(String code, String date) {
        db.delete("STOCKDAYDEAL","CODE=? and TRANSDATE=date(?)",new String[]{code,date});
    }

    /**
     * 将日内分笔交易数据写入数据库
     * @param deals
     */
	public void addStockDayDeal(List<StockDayDeal> deals){
		db.beginTransaction();
		try{
            int size = deals.size();
            for(int i=0;i<size;i++){
                StockDayDeal deal = deals.get(i);
				db.execSQL("insert into stockdaydeal values(null,?,?,?,?,?,?,?,?)",new Object[]{deal.Code,deal.TransDate,deal.DealTime,deal.Price,deal.PriceChange,deal.DealCount,deal.DealAmount,deal.DealType});
			}
			db.setTransactionSuccessful();
		}catch(Exception ex){
            Log.e("",ex.getMessage());
		}finally{
			db.endTransaction();
		}
	}

    /**
     * 将日K线数据写入数据库
     * @param stocks
     */
    public void addStockDay(List<StockDay> stocks){
        db.beginTransaction();
        try{
            for(StockDay stock:stocks){
                db.execSQL("insert into STOCKDAY values(null,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",new Object[]{stock.CODE,stock.NAME,stock.TRANSDATE,stock.TCLOSE,stock.HIGH,stock.LOW,stock.TOPEN,stock.LCLOSE,stock.CHG,stock.PCHG,stock.TURNOVER,stock.VOTURNOVER,stock.VATURNOVER,stock.TCAP,stock.MCAP});
            }
            db.setTransactionSuccessful();
        }catch (Exception ex){
            Log.e("",ex.getMessage());
        }
        finally {
            db.endTransaction();
        }
    }

    /**
     * 写入证券数据到数据库
     * @param stocks
     */
    public void addStock(List<StockDay> stocks){
        db.beginTransaction();
        try{
            for(StockDay stock:stocks){
                db.execSQL("insert into STOCK values(null,?,?,?,?,null,null)",new Object[]{stock.CODE,stock.NAME, stock.INDUSTRY, stock.REGION});
            }
            db.setTransactionSuccessful();
        }catch (Exception ex){
            Log.e("",ex.getMessage());
        }
        finally {
            db.endTransaction();
        }
    }

    /**
     * 从数据库读取某股某日分笔交易明细
     * @param code
     * @param date
     * @return
     */
	public List<StockDayDeal> queryStockDeals(String code,String date,int index,int count){
		ArrayList<StockDayDeal> deals = new ArrayList<StockDayDeal>();
		Cursor c = queryTheDealCursor(code,date,index,count);
		while(c.moveToNext()){
			StockDayDeal deal = new StockDayDeal();
			deal.DealAmount=c.getFloat(c.getColumnIndex("DEALAMOUNT"));
			deal.DealCount=c.getInt(c.getColumnIndex("DEALCOUNT"));
			deal.DealTime=c.getString(c.getColumnIndex("DEALTIME"));
			deal.DealType=c.getString(c.getColumnIndex("DEALTYPE"));
			deal.Price=c.getFloat(c.getColumnIndex("PRICE"));
			deal.PriceChange=c.getString(c.getColumnIndex("PRICECHANGE"));
			deal.Code=c.getString(c.getColumnIndex("CODE"));
			deal.TransDate=c.getString(c.getColumnIndex("TRANSDATE"));
			deals.add(deal);
		}
		return deals;
	}

    /**
     *
     * @param code
     * @param date
     * @param index
     * @param count
     * @return
     */
    public Cursor queryTheDealCursor(String code,String date,int index,int count){
        Cursor c=db.rawQuery("select * from stockdaydeal where code=? and transdate=date(?) order by dealtime asc limit ?,?",new String[]{code,date,String.valueOf(index),String.valueOf(count)});
        return c;
    }
    /**
     * 查询某日分笔成交记录数
     * @param code
     * @param date
     * @return
     */
    public int queryStockDealsCount(String code,String date){
        int result = 0;

        Cursor c =  db.rawQuery("select count(*) from stockdaydeal where code=? and transdate=date(?)",new String[]{code,date});

        while(c.moveToNext()){
            result = c.getInt(0);
        }
        return result;
    }

    /**
     * 从数据库内读取某股一段时间内的日成交数据
     * @param code
     * @param startdate
     * @param enddate
     * @return
     */
    public List<StockDay> queryStockDay(String code,String startdate,String enddate){
        ArrayList<StockDay> stocks = new ArrayList<StockDay>();
        Cursor c = queryTheCursor(code,startdate,enddate);
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");

        while (c.moveToNext()){
            StockDay stock = new StockDay();

            stock.CODE = c.getString(c.getColumnIndex("CODE"));
            stock.NAME = c.getString(c.getColumnIndex("NAME"));
            stock.TRANSDATE = c.getString(c.getColumnIndex("TRANSDATE"));
            stock.TCLOSE = c.getFloat(c.getColumnIndex("TCLOSE"));
            stock.HIGH = c.getFloat(c.getColumnIndex("HIGH"));
            stock.LOW = c.getFloat(c.getColumnIndex("LOW"));
            stock.TOPEN = c.getFloat(c.getColumnIndex("TOPEN"));
            stock.LCLOSE = c.getFloat(c.getColumnIndex("LCLOSE"));
            stock.CHG = c.getFloat(c.getColumnIndex("CHG"));
            stock.PCHG = c.getFloat(c.getColumnIndex("PCHG"));
            stock.TURNOVER = c.getFloat(c.getColumnIndex("TURNOVER"));
            stock.VOTURNOVER =  c.getFloat(c.getColumnIndex("VOTURNOVER"));
            stock.VATURNOVER =  c.getFloat(c.getColumnIndex("VATURNOVER"));
            stock.TCAP =  c.getFloat(c.getColumnIndex("TCAP"));
            stock.MCAP =  c.getFloat(c.getColumnIndex("MCAP"));
            stocks.add(stock);
        }
        c.close();
        return stocks;
    }

    /**
     * 查询数据库股票数
     * @return
     */
    public int queryStockCount(String cnd){
        int result = 0;
        Cursor c = null;

        if(cnd.length()>0)
            c = db.rawQuery("select count(*) from STOCK where code like ?",new String[]{"%" + cnd + "%"});
        else
            c = db.rawQuery("select count(*) from STOCK",null);

        while(c.moveToNext()){
            result = c.getInt(0);
        }
        return result;
    }

    /**
     * 查询数据库内股票代码
     * @return
     */
    public List<StockDay> queryStock(int index,int count,String cnd){
        ArrayList<StockDay> stocks = new ArrayList<StockDay>();
        Cursor c = queryTheStockCursor(index,count,cnd);
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");

        while (c.moveToNext()){
            StockDay stock = new StockDay();

            stock.CODE = c.getString(c.getColumnIndex("CODE"));
            stock.NAME = c.getString(c.getColumnIndex("NAME"));
            stock.INDUSTRY = c.getString(c.getColumnIndex("INDUSTRY"));
            stock.REGION = c.getString(c.getColumnIndex("REGION"));

            try{
                String sOPEN = c.getString(c.getColumnIndex("OPENDATE"));
                String sLAST = c.getString(c.getColumnIndex("LASTDATE"));

                if(sOPEN!=null)
                    stock.OPENDATE = simpleDateFormat.parse(sOPEN);
                if(sLAST!=null)
                    stock.LASTDATE = simpleDateFormat.parse(sLAST);
            }catch (Exception ex){
                Log.e("日期转换错误",ex.getMessage());
            }

            stocks.add(stock);
        }
        c.close();
        return stocks;
    }

    public Cursor queryTheStockCursor(int index,int count,String cnd){
        Cursor c = null;
        if(cnd.length()>0)
            c = db.rawQuery("select * from STOCK where code like ? limit ?,?",new String[]{"%"+cnd+"%",Integer.toString(index),Integer.toString(count)});
        else
            c = db.rawQuery("select * from STOCK limit ?,?",new String[]{Integer.toString(index),Integer.toString(count)});
        return c;
    }

    public Cursor queryTheCursor(String code,String startdate,String enddate){
        Cursor c = db.rawQuery("select * from STOCKDAY where CODE=? and transdate>=date(?) and transdate<=date(?) order by transdate desc",new String[]{code,startdate,enddate});
        return c;
    }

	public Date getlastStockday(String code){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date dt = null;

        try
        {
            dt = sdf.parse("1990-01-01");
            Cursor c= db.rawQuery("select TRANSDATE from stockday where code=? order by transdate desc limit 1",new String[]{code});
            c.moveToNext();

            if(c.getCount()>0) {
                String transdate = c.getString(c.getColumnIndex("TRANSDATE"));
                dt = sdf.parse(transdate);
            }
		}
		catch (ParseException e)
		{
            Log.e("",e.getMessage());
        }

		return dt;
	}

    public void closeDB(){
        db.close();
    }

    public List<StockDay> queryFavorite() {
        ArrayList<StockDay> stocks = new ArrayList<StockDay>();
        Cursor c = queryFavoriteCursor();
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");

        while (c.moveToNext()){
            StockDay stock = new StockDay();

            stock.CODE = c.getString(c.getColumnIndex("CODE"));
            stock.NAME = c.getString(c.getColumnIndex("NAME"));
            stock.INDUSTRY = c.getString(c.getColumnIndex("INDUSTRY"));
            stock.REGION = c.getString(c.getColumnIndex("REGION"));

            try{
                stock.OPENDATE = simpleDateFormat.parse(c.getString(c.getColumnIndex("OPENDATE")));
                stock.LASTDATE = simpleDateFormat.parse(c.getString(c.getColumnIndex("LASTDATE")));
            }catch (Exception ex){}

            stocks.add(stock);
        }
        c.close();
        return stocks;
    }

    private Cursor queryFavoriteCursor() {
        Cursor c=db.rawQuery("select b.* from FavoriteStock as a left join Stock as b on a.code = b.code",null);
        return c;
    }

    public void addFavorite(String stockcode) {
        Cursor c = db.rawQuery("select count(*) from FavoriteStock where code = ?",new String[]{stockcode});
        c.moveToNext();
        int count = c.getInt(0);
        c.close();
        if(count>0) return;

        db.beginTransaction();
        try {
            db.execSQL("insert into FavoriteStock values(null,?)", new String[]{stockcode});
            db.setTransactionSuccessful();
        }catch (Exception err){
            Log.e("加入自选错误",err.getMessage());
        }finally {
            db.endTransaction();
        }
    }

    /**
     * 移除自选股
     * @param code
     */
    public void removeFavorite(String code) {
        db.beginTransaction();
        try {
            db.delete("FavoriteStock","code = ?", new String[]{code});
            db.setTransactionSuccessful();
        }catch (Exception err){
            Log.e("移除自选错误",err.getMessage());
        }finally {
            db.endTransaction();
        }
    }

    /**
     * 更新股票列表
     * @param stocks
     */
    public void updateStocks(List<StockDay> stocks) {

    }

    /**
     * 保存资金记录
     * @param record
     */
    public void saveTradeRecord(TradeRecord record) {
        db.beginTransaction();

        try{
            switch (record.TradeType)
            {
                case PAYINTO:
                case ROLLOUT:
                    db.execSQL("insert into TRADERECORD values(null,null,?,null,?,null,?)",new Object[]{Utils.DateFormatter.format(record.TradeTime),record.TurnOver,Utils.TRADETYPETOSTRING(record.TradeType)});
                    break;
                case BUY:
                case SELL:
                    db.execSQL("insert into TRADERECORD values(null,?,?,?,?,?,?)",new Object[]{record.Code,Utils.DateFormatter.format(record.TradeTime),record.Price,record.TurnOver,record.TurnVolumn,Utils.TRADETYPETOSTRING(record.TradeType)});
                    break;
            }
            db.setTransactionSuccessful();
        }catch (Exception err){
            Log.e("保存资金记录",err.getMessage());
        }finally {
            db.endTransaction();
        }
    }

    /**
     * 从数据库读取交易记录
     * @return
     */
    public List<TradeRecord> getTradeRecords() {
        ArrayList<TradeRecord> records = new ArrayList<TradeRecord>();
        Cursor c = db.rawQuery("select * from traderecord order by datetime(tradetime) desc",null);

        while(c.moveToNext()){
            TradeRecord record = new TradeRecord();

            try {
                record.TradeTime = Utils.DateFormatter.parse(c.getString(c.getColumnIndex("TRADETIME")));
            } catch (ParseException e) {
                Log.e("成交记录",e.getMessage());
            }
            record.Price = c.getFloat(c.getColumnIndex("PRICE"));
            record.TurnOver = c.getFloat(c.getColumnIndex("TURNOVER"));
            record.TurnVolumn = c.getFloat(c.getColumnIndex("TURNVOLUMN"));
            record.TradeType  = Utils.STRINGTOTRADETYPE(c.getString(c.getColumnIndex("TRADETYPE")));
            records.add(record);
        }

        c.close();

        return records;
    }

    /**
     * 从数据库读取账户信息
     * @return
     */
    public Account LoadAccountInfo() {
        Account result = new Account();

        Cursor c = db.rawQuery("select * from StockAccount",null);
        c.moveToNext();

        if(c.getCount() > 0){
            result.ASSETS = c.getFloat(c.getColumnIndex("ASSETS"));
            result.AVAILABLEBALANCE = c.getFloat(c.getColumnIndex("AVAILABLEBALANCE"));
            result.CAPITALBALANCE = c.getFloat(c.getColumnIndex("CAPITALBALANCE"));
            result.MARKETVALUE = c.getFloat(c.getColumnIndex("MARKETVALUE"));
            result.SHARES = c.getFloat(c.getColumnIndex("SHARES"));
        }

        c.close();
        return result;
    }

    public void saveAccountInfo(Account mAccount) {
        try {
            Cursor c = db.rawQuery("select * from StockAccount", null);
            c.moveToNext();
            int count = c.getCount();
            c.close();

            db.beginTransaction();
            if (count == 0) {
                db.execSQL("insert into StockAccount values(null,?,?,?,?,?)", new Object[]{mAccount.ASSETS, mAccount.CAPITALBALANCE, mAccount.AVAILABLEBALANCE, mAccount.MARKETVALUE, mAccount.SHARES});
            } else {
                db.execSQL("update StockAccount set ASSETS=?,CAPITALBALANCE=?,AVAILABLEBALANCE=?,MARKETVALUE=?,SHARES=?",new Object[]{mAccount.ASSETS, mAccount.CAPITALBALANCE, mAccount.AVAILABLEBALANCE, mAccount.MARKETVALUE, mAccount.SHARES});
            }
            db.setTransactionSuccessful();
        }catch (Exception err){
            Log.e("更新账户信息",err.getMessage());
        }finally {
            db.endTransaction();
        }
    }
}
