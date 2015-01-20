package com.example.KGraph;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yangj on 13-12-14.
 */
public class StockDayDetails extends Activity {
    private ListView m_stockdetaillist;
	private DBManager dbmgr;
    private MyHandler myHandler;
    private ArrayList<Map<String,String>> list;
    private String mStockCode;
    private String mTransDate;
    List<StockDayDeal> m_downlowdStockDayDeals = null;
    SimpleAdapter adapter;
    String stockurl = "http://market.finance.sina.com.cn/downxls.php?date=%1$s&symbol=%2$s";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stockdaydetailview);

        m_stockdetaillist = (ListView)findViewById(R.id.stockdetaillist);

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();

        String code = bundle.getString("STOCKCODE");
        String date = bundle.getString("TRANSDATE");

        mStockCode = code;
        mTransDate = date;

        this.setTitle(code + " " + date);

        myHandler = new MyHandler();
		dbmgr=new DBManager(this);

        if(Integer.parseInt(code)<600000)
            code = "sz"+code;
        else
            code = "sh"+code;

        list = new ArrayList<Map<String, String>>();
        adapter = new SimpleAdapter(this,list,R.layout.stockdaydetaillist,new String[]{"成交时间","成交价","价格变动","成交量","成交额","性质"},new int[]{R.id.txtDealTime,R.id.txtPrice,R.id.txtPriceCHG,R.id.txtDealCount,R.id.txtDealAmount,R.id.txtDealType});

        m_stockdetaillist.setAdapter(adapter);
        //date = date.substring(0,4) + "-" + date.substring(4,2) +"-"+date.substring(6);
        stockurl = String.format(stockurl,date,code);
		
		loadStockTransactions();
    }
	
	private void loadStockTransactions(){
		m_downlowdStockDayDeals = dbmgr.queryStockDeals(mStockCode,mTransDate);
		
		if(m_downlowdStockDayDeals==null ||m_downlowdStockDayDeals.size()==0){
		   ThreadPoolUtils.execute(new MyRunnable());
		}else{
			loadStockDealList(m_downlowdStockDayDeals);
		}
	}

	public void loadStockDealList(List<StockDayDeal> deals){
		//加载到listview
		list.clear();
		for (StockDayDeal deal:deals){
			HashMap<String,String> map = new HashMap<String, String>();
			map.put("成交时间", deal.DealTime);
			map.put("成交价",String.format("%1$.2f",deal.Price));
			map.put("价格变动",deal.PriceChange);
			map.put("成交量",String.valueOf(deal.DealCount));
			map.put("成交额",String.format("%1$.2f",deal.DealAmount/10000.0));
			map.put("性质",deal.DealType);
			list.add(map);
		}
		//SimpleAdapter adapter = new SimpleAdapter(this,list,android.R.layout.simple_list_item_2,new String[]{"name","info"},new int[]{android.R.id.text1,android.R.id.text2});
	}

    /**
     * 下载分笔数据
     */
    private class MyRunnable implements Runnable{
        @Override
        public void run(){
            HttpGet httpGet = new HttpGet(stockurl);
            HttpParams params = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(params, 60000);
            httpGet.setParams(params);

            try
            {
                HttpResponse httpResponse = HttpClientHelper.getHttpClient().execute(httpGet);
                String result = EntityUtils.toString(httpResponse.getEntity(), "GB2312");

                if(result != null){
                    m_downlowdStockDayDeals = StockDayDeal.parse(result,mStockCode,mTransDate);
                    dbmgr.addStockDayDeal(m_downlowdStockDayDeals);
                }

                loadStockDealList(m_downlowdStockDayDeals);

                Message msg = myHandler.obtainMessage();
                msg.what = StockDayDetails.MyHandler.FINISH_DOWNLOAD_MESSAGE;
                myHandler.sendMessage(msg);
				
				dbmgr.addStockDayDeal(m_downlowdStockDayDeals);
            }catch (Exception ex){
                System.out.println(ex.getMessage());
            }
        }
	}

    public class MyHandler extends Handler {
        public static final int START_DOWNLOAD_MESSAGE = 0x01;
        public static final int FINISH_DOWNLOAD_MESSAGE = 0x02;
        public static final int ERROR_DOWNLOAD_MESSAGE = 0x03;

        @Override
        public void dispatchMessage(Message msg) {
            switch(msg.what){
                case FINISH_DOWNLOAD_MESSAGE:
                    //loadStockList((List<StockDay>)msg.obj);
                    adapter.notifyDataSetChanged();
                    break;
                case START_DOWNLOAD_MESSAGE:
                    break;
                case ERROR_DOWNLOAD_MESSAGE:
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    }
}
