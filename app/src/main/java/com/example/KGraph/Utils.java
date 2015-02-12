package com.example.KGraph;

import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by yangj on 2015/1/27.
 */
public class Utils {
    public static int DISPLAYINDEX = 4;
    /**
     * 更新速度
     */
    public static int SPEED = 500;
    public static SimpleDateFormat DateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static SimpleDateFormat DayFormatter = new SimpleDateFormat("yyyy-MM-dd");
    public static SimpleDateFormat TimeFormatter = new SimpleDateFormat("HH:mm:ss");
    //http://api.money.126.net/data/feed/1000002,money.api;//bid 数据
    public static String URL = "http://market.finance.sina.com.cn/downxls.php?date=%1$s&symbol=%2$s";
    //http://stock.gtimg.cn/data/index.php?appn=detail&action=download&c=%s&d=%s
    //http://stock.gtimg.cn/data/index.php?appn=detail&action=download&c=sh601988&d=20150205;//QQ 分笔明细
    public static String DAYURL = "http://quotes.money.163.com/service/chddata.html?code=%1$s&start=%2$s&end=%3$s&fields=TCLOSE;HIGH;LOW;TOPEN;LCLOSE;CHG;PCHG;TURNOVER;VOTURNOVER;VATURNOVER;TCAP;MCAP";
    public final static String[] WeekName = new String[]{"星期日","星期一","星期二","星期三","星期四","星期五","星期六"};

    public static String TRADETYPETOSTRING(TradeType tradeType) {
        String result = "";

        if(tradeType != null) {
            switch (tradeType) {
                case PAYINTO:
                    result = "转入";
                    break;
                case ROLLOUT:
                    result = "转出";
                    break;
                case BUY:
                    result = "买入";
                    break;
                case SELL:
                    result = "卖出";
                    break;
            }
        }
        return result;
    }

    public static TradeType STRINGTOTRADETYPE(String tradetype) {
        TradeType result = null;

        if(tradetype.equals("转入")) result = TradeType.PAYINTO;
        if(tradetype.equals("转出")) result = TradeType.ROLLOUT;
        if(tradetype.equals("买入")) result = TradeType.BUY;
        if(tradetype.equals("卖出")) result = TradeType.SELL;

        return result;
    }

    public static List<StockDayDeal> downloadDayDeals(DBManager dbmgr, String code, String date) {
        List<StockDayDeal> mdeals = new ArrayList<StockDayDeal>();
        String url = Utils.URL;

        if(Integer.parseInt(code)<600000)
            code = "sz"+code;
        else
            code = "sh"+code;

        url = String.format(url,date,code);

        HttpGet httpGet = new HttpGet(url);
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, 60000);
        httpGet.setParams(params);

        try
        {
            HttpResponse httpResponse = HttpClientHelper.getHttpClient().execute(httpGet);
            String result = EntityUtils.toString(httpResponse.getEntity(), "GB2312");

            if(result != null){
                mdeals = StockDayDeal.parse(result,code,date);
                dbmgr.addStockDayDeal(mdeals);
            }
        }catch (Exception ex){
            Log.e("下载分笔明细", ex.getMessage());
        }

        return mdeals;
    }

    /**
     * 合成分钟数据
     * @param mdeals
     * @return
     */
    public static List<StockDayDeal> GetMinuteData(List<StockDayDeal> mdeals) {
        int vol = 0;
        float price = 0;
        List<StockDayDeal> minutedata = new ArrayList<StockDayDeal>();

        try {
            Date t1 = TimeFormatter.parse("09:30:00");
            Date t1129 = TimeFormatter.parse("11:29:00");
            Date t1130 = TimeFormatter.parse("11:30:00");
            Date t1131 = TimeFormatter.parse("11:31:00");
            Date t1300 = TimeFormatter.parse("13:00:00");
            Date t1458 = TimeFormatter.parse("14:58:00");
            Date t1459 = TimeFormatter.parse("14:59:00");
            Date t1500 = TimeFormatter.parse("14:59:59");

            for(int i = 0;i<mdeals.size();i++) {
                StockDayDeal deal = mdeals.get(i);
                Date t2 = TimeFormatter.parse(deal.DealTime);

                if(t2.after(t1130)&&t2.before(t1131))
                    t2.setTime(t2.getTime()-60*1000);

                if(t2.after(t1500))
                    t2.setTime(t2.getTime()-59*1000);

                long span = (t2.getTime() - t1.getTime())/(60*1000);

                if(i==mdeals.size()-1){
                    vol += deal.DealCount;
                    StockDayDeal mdata3 = new StockDayDeal();
                    mdata3.DealTime = TimeFormatter.format(t1);
                    mdata3.DealCount = vol;
                    mdata3.Price = deal.Price;
                    minutedata.add(mdata3);
                    continue;
                }

                if(span>=1) {
                    StockDayDeal mdata = new StockDayDeal();
                    mdata.DealTime = TimeFormatter.format(t1);
                    mdata.DealCount = vol;
                    mdata.Price = price;
                    minutedata.add(mdata);
                    vol = 0;
                    //price = 0;

                    if(t1.before(t1459))
                        t1.setTime(t1.getTime() + 60 * 1000);

                    if(t2.after(t1300)&& t1.before(t1300)) {
                        t1 = t1300;
                        span = (t2.getTime() - t1.getTime())/(60*1000);
                    }

                    if(span>=2){
                        for(int j=1;j<span;j++){
                            StockDayDeal mdata2 = new StockDayDeal();
                            mdata2.DealTime = TimeFormatter.format(t1);
                            mdata2.DealCount = vol;
                            mdata2.Price = price;
                            minutedata.add(mdata2);

                            if(t1.before(t1459))
                                t1.setTime(t1.getTime() + 60 * 1000);

                            if(t1.after(t1458)){
                                vol += deal.DealCount;
                                price = deal.Price;
                            }
                        }
                    }
                }

                vol += deal.DealCount;
                price = deal.Price;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return minutedata;
    }
}
