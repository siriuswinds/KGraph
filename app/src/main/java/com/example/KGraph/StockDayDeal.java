package com.example.KGraph;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by yangj on 13-12-16.
 */
public class StockDayDeal {
    String DealTime;
    float Price;
    String PriceChange;
    int DealCount;
    float DealAmount;
    String DealType;
	String Code;
	String TransDate;

    /*
        parse the data from quotes.money.163.com
    */
    public static List<StockDayDeal> parse(String data,String code,String date){
        List<StockDayDeal> deals = null;
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
        String[] datas = data.split("\n");
        deals = new ArrayList<StockDayDeal>();

        for(int i=1;i<datas.length;i++){
            String[] values = datas[i].split("\t");
            StockDayDeal deal = new StockDayDeal();
            try{
                deal.Code = code;
                deal.TransDate = date;
                deal.DealTime = values[0];
                deal.Price = Float.valueOf(values[1]);
                deal.PriceChange = values[2];
                deal.DealCount = Integer.valueOf(values[3]);
                deal.DealAmount = Float.valueOf(values[4]);
                deal.DealType = values[5];
                deals.add(deal);
            }catch (Exception ex){}
        }
        Collections.reverse(deals);
        return deals;
    }

    @Override
    public  String toString(){
        return this.DealTime;
    }
}
