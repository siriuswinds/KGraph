package com.example.KGraph;

import android.os.Message;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yangj on 2015/1/27.
 */
public class Utils {
    public static int DISPLAYINDEX = 8;
    /**
     * 更新速度
     */
    public static int SPEED = 300;
    public static SimpleDateFormat DateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static SimpleDateFormat DayFormatter = new SimpleDateFormat("yyyy-MM-dd");
    public static SimpleDateFormat TimeFormatter = new SimpleDateFormat("HH:mm:ss");
    public static String URL = "http://market.finance.sina.com.cn/downxls.php?date=%1$s&symbol=%2$s";
    public static String DAYURL = "http://quotes.money.163.com/service/chddata.html?code=%1$s&start=%2$s&end=%3$s&fields=TCLOSE;HIGH;LOW;TOPEN;LCLOSE;CHG;PCHG;TURNOVER;VOTURNOVER;VATURNOVER;TCAP;MCAP";
    public final static String[] WeekName = new String[]{"星期日","星期一","星期二","星期三","星期四","星期五","星期六"};

    public static String TRADETYPETOSTRING(TRADETYPE tradeType) {
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

    public static TRADETYPE STRINGTOTRADETYPE(String tradetype) {
        TRADETYPE result = null;

        if(tradetype.equals("转入")) result = TRADETYPE.PAYINTO;
        if(tradetype.equals("转出")) result = TRADETYPE.ROLLOUT;
        if(tradetype.equals("买入")) result = TRADETYPE.BUY;
        if(tradetype.equals("卖出")) result = TRADETYPE.SELL;

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
}
