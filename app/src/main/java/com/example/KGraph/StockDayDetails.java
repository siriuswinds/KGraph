package com.example.KGraph;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
    private Button mbtnPrePage;
    private Button mbtnNextPage;
    private Button mbtnRefresh;
    private Button mbtnReturn;
    private int mPageIndex = 0,mLineCount = 11,mPageCount = 0;
    private String mStockCode;
    private String mTransDate;

    List<StockDayDeal> m_downlowdStockDayDeals = null;
    SimpleAdapter adapter;
    String stockurl = "http://market.finance.sina.com.cn/downxls.php?date=%1$s&symbol=%2$s";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stockdaydetailview);
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();

        String code = bundle.getString("STOCKCODE");
        String date = bundle.getString("TRADEDATE");
        this.setTitle(code + " " + date);
        mStockCode = code;
        mTransDate = date;
        if(Integer.parseInt(code)<600000)
            code = "sz"+code;
        else
            code = "sh"+code;
        stockurl = String.format(stockurl,date,code);
        myHandler = new MyHandler();
        dbmgr=new DBManager(this);

        m_stockdetaillist = (ListView)findViewById(R.id.stockdetaillist);

        mbtnPrePage = (Button)findViewById(R.id.btnPrePage);
        mbtnPrePage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mPageIndex>0){
                    --mPageIndex;
                    loadStockTransactions(mPageIndex*mLineCount,mLineCount);
                }
            }
        });
        mbtnNextPage = (Button)findViewById(R.id.btnNextPage);
        mbtnNextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mPageIndex<mPageCount-1){
                    ++mPageIndex;
                    loadStockTransactions(mPageIndex*mLineCount,mLineCount);
                }
            }
        });
        mbtnRefresh = (Button)findViewById(R.id.btnRefresh);
        mbtnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbmgr.deleteStockDayDeals(mStockCode,mTransDate);
                initStockTransactions(mPageIndex,mLineCount);
            }
        });
        mbtnReturn = (Button)findViewById(R.id.btnReturn);
        mbtnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(Activity.RESULT_OK);
                finish();
            }
        });

        list = new ArrayList<Map<String, String>>();
        adapter = new SimpleAdapter(this,list,R.layout.stockdaydetaillist,new String[]{"成交时间","成交价","价格变动","成交量","成交额","性质"},new int[]{R.id.txtDealTime,R.id.txtPrice,R.id.txtPriceCHG,R.id.txtDealCount,R.id.txtDealAmount,R.id.txtDealType});
        m_stockdetaillist.setAdapter(adapter);
        initStockTransactions(mPageIndex,mLineCount);
    }

    private void initStockTransactions(int index,int count){
        m_downlowdStockDayDeals = dbmgr.queryStockDeals(mStockCode,mTransDate,index,count);
        mPageCount = dbmgr.queryStockDealsCount(mStockCode,mTransDate);

        if(m_downlowdStockDayDeals==null ||m_downlowdStockDayDeals.size()==0){
            ThreadPoolUtils.execute(new MyRunnable());
        }else{
            loadStockDealList(m_downlowdStockDayDeals);
            adapter.notifyDataSetChanged();
        }
    }

	private void loadStockTransactions(int index,int count){
		m_downlowdStockDayDeals = dbmgr.queryStockDeals(mStockCode,mTransDate,index,count);
		loadStockDealList(m_downlowdStockDayDeals);
        adapter.notifyDataSetChanged();
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
                    mPageCount = m_downlowdStockDayDeals.size();
                }

                loadStockDealList(m_downlowdStockDayDeals.subList(mPageIndex,mLineCount));

                Message msg = myHandler.obtainMessage();
                msg.what = StockDayDetails.MyHandler.FINISH_DOWNLOAD_MESSAGE;
                myHandler.sendMessage(msg);
            }catch (Exception ex){
                Log.e("下载分笔明细",ex.getMessage());
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
