package com.example.KGraph;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Handler;
import java.util.logging.LogRecord;


public class TradeActivity extends Activity {
    private Button mbtnReturn,mbtnBuy,mbtnSell;
    private EditText mtxtPrice,mtxtVolumn;
    private ListView mlvHostStocks,mlvMarket;
    private SimpleAdapter adapterHoldStocks,adapterMarket;
    private ArrayList<Map<String,String>> mlistHostStocks,mlistMarket;
    private TradeManager trademgr;
    private DBManager dbmgr;
    private Timer mtimer;
    private TimerTask mtask;
    private String mCode;
    private String mDate;
    private List<StockDayDeal> mdeals;
    private int mdisplayIndex=8;
    private float mCurrentPrice;

    final android.os.Handler mhandler = new android.os.Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    adapterMarket.notifyDataSetChanged();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trade);

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        mCode = bundle.getString("STOCKCODE");
        mDate = bundle.getString("TRADEDATE");

        mtimer = new Timer();
        dbmgr = new DBManager(this);
        trademgr = new TradeManager(dbmgr);

        mlvHostStocks = (ListView)findViewById(R.id.holdstocklist);
        mlistHostStocks = new ArrayList<Map<String, String>>();
        adapterHoldStocks = new SimpleAdapter(this,mlistHostStocks,R.layout.holdstocklist,new String[]{"code","name","price","currentprice","volumn","share"},new int[]{R.id.txtCode,R.id.txtName,R.id.txtPrice,R.id.txtCurrentPrice,R.id.txtTURNVOLUMN,R.id.txtSHARE});
        mlvHostStocks.setAdapter(adapterHoldStocks);

        mlvMarket = (ListView)findViewById(R.id.marketinfo);
        mlistMarket = new ArrayList<Map<String, String>>();
        adapterMarket = new SimpleAdapter(this,mlistMarket,R.layout.marketinfo,new String[]{"成交时间","成交价","价格变动","成交量","成交额","性质"},new int[]{R.id.txtDealTime,R.id.txtPrice,R.id.txtPriceCHG,R.id.txtDealCount,R.id.txtDealAmount,R.id.txtDealType});
        mlvMarket.setAdapter(adapterMarket);

        initHoldStocks();
        //initStockMarket();

        mbtnReturn = (Button)findViewById(R.id.btnReturn);
        mbtnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(Activity.RESULT_OK);
                finish();
            }
        });
        mbtnBuy = (Button)findViewById(R.id.btnBuy);
        mbtnBuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        mbtnSell = (Button)findViewById(R.id.btnSell);
        mbtnSell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        mtxtPrice = (EditText)findViewById(R.id.txtPrice);
        mtxtVolumn = (EditText)findViewById(R.id.txtVolumn);

        mtask = new TimerTask() {
            @Override
            public void run() {
                if(mdeals == null || (mdisplayIndex == 8 && mdeals.size()==0))
                    initStockMarket();
                else
                    displayMarket();

                Message msg = new Message();
                msg.what = 1;
                mhandler.sendMessage(msg);
            }
        };
        mtimer.schedule(mtask,1000,Utils.SPEED);
    }

    private void initStockMarket() {
        mdeals = dbmgr.queryStockDeals(mCode,mDate);
        mlistMarket.clear();

        if(mdeals.size()==0){
            mdeals = Utils.downloadDayDeals(dbmgr,mCode,mDate);
        }

        for(int i = 0;i<mdisplayIndex;i++){
            StockDayDeal deal = mdeals.get(i);
            addMarketItem(deal);
        }
    }

    private void displayMarket() {
        ++mdisplayIndex;
        if(mdisplayIndex == mdeals.size()){
            loadNextDayMarket();
            return;
        }
        mlistMarket.remove(0);
        StockDayDeal deal = mdeals.get(mdisplayIndex);
        addMarketItem(deal);
    }

    /**
     * 读取下一天分笔数据
     */
    private void loadNextDayMarket() {
        mdeals.clear();
        mdisplayIndex = 8;
        Calendar c = Calendar.getInstance();
        try {
            Date d = Utils.DayFormatter.parse(mDate);
            c.setTime(d);
            c.add(Calendar.DATE,1);
            if(c.after(Calendar.getInstance())){
                mtimer.cancel();
                return;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        mDate = Utils.DayFormatter.format(c.getTime());
        initStockMarket();
    }

    private void addMarketItem(StockDayDeal deal) {
        HashMap<String,String> map = new HashMap<String, String>();
        map.put("成交时间", deal.DealTime);
        map.put("成交价", String.format("%1$.2f", deal.Price));
        map.put("价格变动",deal.PriceChange);
        map.put("成交量",String.valueOf(deal.DealCount));
        map.put("成交额",String.format("%1$.2f", deal.DealAmount / 10000.0));
        map.put("性质",deal.DealType);
        mCurrentPrice = deal.Price;
        mlistMarket.add(map);
    }

    /**
     * 初始化持股信息
     */
    private void initHoldStocks() {
        List<TradeRecord> stocks = trademgr.getHoldStocks();
        mlistHostStocks.clear();
        for (TradeRecord stock:stocks){
            HashMap<String,String> map = new HashMap<String, String>();
            map.put("code", stock.Code);
            map.put("name","");
            map.put("price",String.format("%1$.2f",stock.Price));
            map.put("currentprice",String.format("%1$.2f", stock.Price));
            map.put("volumn",String.format("%1$.0f",stock.TurnVolumn));
            map.put("share","");
            mlistHostStocks.add(map);
        }
        adapterHoldStocks.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_trade, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
