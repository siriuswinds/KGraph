package com.example.KGraph;

import android.content.*;
import android.database.*;
import android.database.sqlite.*;
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
    }
	
	public void addStockDayDeal(List<StockDayDeal> deals){
		db.beginTransaction();
		try{
			for(StockDayDeal deal:deals){
				db.execSQL("insert into STOCKDAYDEAL values(null,?,?,?,?,?,?,?,?)",new Object[]{deal.Code,deal.TransDate,deal.DealTime,deal.Price,deal.PriceChange,deal.DealCount,deal.DealAmount,deal.DealType});
			}
			db.setTransactionSuccessful();
			
		}catch(Exception ex){
			
		}finally{
			db.endTransaction();
		}
	}

    public void addStockDay(List<StockDay> stocks){
        db.beginTransaction();
        try{
            for(StockDay stock:stocks){
                db.execSQL("insert into STOCKDAY values(null,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",new Object[]{stock.CODE,stock.NAME,stock.TRANSDATE,stock.TCLOSE,stock.HIGH,stock.LOW,stock.TOPEN,stock.LCLOSE,stock.CHG,stock.PCHG,stock.TURNOVER,stock.VOTURNOVER,stock.VATURNOVER,stock.TCAP,stock.MCAP});
            }
            db.setTransactionSuccessful();

        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }
        finally {
            db.endTransaction();
        }
    }

    public void addStock(List<StockDay> stocks){
        db.beginTransaction();
        try{
            for(StockDay stock:stocks){
                db.execSQL("insert into STOCK values(null,?,?,?,?,null,null)",new Object[]{stock.CODE,stock.NAME, stock.INDUSTRY, stock.REGION});
            }
            db.setTransactionSuccessful();
        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }
        finally {
            db.endTransaction();
        }
    }

    public void deleteStocks(){
        db.delete("STOCK","",null);
    }

    public void deleteStockDays(){
        db.delete("STOCKDAY","",null);
    }

    public void deleteOldStockDay(StockDay stock){
        db.delete("STOCKDAY","CODE=? and TRANSDATE=date(?)",new String[]{stock.CODE,stock.TRANSDATE});
    }
	
	public List<StockDayDeal> queryStockDeals(String code,String date){
		ArrayList<StockDayDeal> deals = new ArrayList<StockDayDeal>();
		Cursor c = queryTheDealCursor(code,date);
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

    /*
        get stock day datas
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

    /*
        get stocklists
    */
    public List<StockDay> queryStock(){
        ArrayList<StockDay> stocks = new ArrayList<StockDay>();
        Cursor c = queryTheStockCursor();
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

    public Cursor queryTheStockCursor(){
        Cursor c = db.rawQuery("select * from STOCK where code in(?,?,?)",new String[]{"300036","002666","002395"});
        return c;
    }

    public Cursor queryTheCursor(String code,String startdate,String enddate){
        Cursor c = db.rawQuery("select * from STOCKDAY where CODE=? and transdate>=date(?) and transdate<=date(?) order by transdate desc",new String[]{code,startdate,enddate});
        return c;
    }
	
	public Cursor queryTheDealCursor(String code,String date){
		Cursor c=db.rawQuery("select * from stockdaydeal where code=? and transdate=date(?) order by dealtime desc",new String[]{code,date});
		return c;
	}
	
	public Date getlastStockday(String code){	
	    Date dt=null;
		Cursor c= db.rawQuery("select TRANSDATE from stockday where code=? order by transdate desc limit 1",new String[]{code});
		String transdate = c.getString(c.getColumnIndex("TRANSDATE"));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try
		{
			dt=sdf.parse(transdate);
		}
		catch (ParseException e)
		{}

		return dt;
	}

    public void closeDB(){
        db.close();
    }
}
