package com.example.KGraph;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

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
    private com.example.KGraph.MyGraph mGraph;
    private EditText mtxtPrice,mtxtVolumn;
    private ListView mlvHostStocks,mlvMarket;
    private TextView mtxtLastClose,mtxtOpen,mtxtHigh,mtxtLow,mtxtTurnVolumn,mtxtTurnOver,mtxtCHG,mtxtPCHG,mtxtCurrentPrice,mtxtDate;
    private SimpleAdapter adapterHoldStocks,adapterMarket;
    private ArrayList<Map<String,String>> mlistHostStocks,mlistMarket;
    private TradeManager trademgr;
    private DBManager dbmgr;
    private Timer mtimer;
    private TimerTask mtask;
    private String mCode;
    private String mDate;
    private List<StockDayDeal> mdeals,mMinuteData,mGraphData;
    private int mdisplayIndex = Utils.DISPLAYINDEX;
    private float mLastClose,mOpen,mHigh,mLow,mChg,mVol,mTurnover,mCurrentPrice;
    private boolean canStopTimer = true;
    private int SimulateType;

    final android.os.Handler mhandler = new android.os.Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    adapterMarket.notifyDataSetChanged();
                    updateStatics();
                    break;
            }
        }
    };

    private void updateStatics() {
        mtxtLastClose.setText(String.format("%1$.2f",mLastClose));
        mtxtOpen.setText(String.format("%1$.2f",mOpen));
        mtxtHigh.setText(String.format("%1$.2f",mHigh));
        mtxtLow.setText(String.format("%1$.2f",mLow));
        mtxtTurnVolumn.setText(String.format("%1$.2f",mVol/10000));
        mtxtTurnOver.setText(String.format("%1$.2f",mTurnover/100000000));

        mtxtDate.setText(mDate);
        mtxtCurrentPrice.setText(String.format("%1$.2f",mCurrentPrice));
        mtxtCHG.setText(String.format("%1$.2f", mChg));
        mtxtPCHG.setText(String.format("%1$.2f",mChg*100/mLastClose).concat("%"));

        if(mChg >  0) setStaticsColor(Color.RED);
        if(mChg <  0) setStaticsColor(Color.GREEN);
        if(mChg == 0) setStaticsColor(Color.WHITE);
    }

    private void setStaticsColor(int color) {
        mtxtCHG.setTextColor(color);
        mtxtPCHG.setTextColor(color);
        mtxtCurrentPrice.setTextColor(color);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trade);

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        mCode = bundle.getString("STOCKCODE");
        mDate = bundle.getString("TRADEDATE");
        mLastClose = Float.parseFloat(bundle.getString("LCLOSE"));
        SimulateType = Integer.parseInt(bundle.getString("TYPE"));

        mtimer = new Timer();
        dbmgr = new DBManager(this);
        trademgr = new TradeManager(dbmgr);
        mGraphData = new ArrayList<StockDayDeal>();
        initControls();
        initHoldStocks();

        mtask = new TimerTask() {
            @Override
            public void run() {
                if(mdeals == null || (mdisplayIndex == Utils.DISPLAYINDEX && mdeals.size()==0))
                    initStockMarket();
                else
                    displayMarket();
            }
        };

        mtimer.schedule(mtask,1,Utils.SPEED);
    }

    /**
     * 初始化界面
     */
    private void initControls() {
        mlvHostStocks = (ListView)findViewById(R.id.holdstocklist);
        mlistHostStocks = new ArrayList<Map<String, String>>();
        adapterHoldStocks = new SimpleAdapter(this,mlistHostStocks,R.layout.holdstocklist,new String[]{"code","name","price","currentprice","volumn","share"},new int[]{R.id.txtCode,R.id.txtName,R.id.txtPrice,R.id.txtCurrentPrice,R.id.txtTURNVOLUMN,R.id.txtSHARE});
        mlvHostStocks.setAdapter(adapterHoldStocks);

        mlvMarket = (ListView)findViewById(R.id.marketinfo);
        mlistMarket = new ArrayList<Map<String, String>>();
        adapterMarket = new SimpleAdapter(this,mlistMarket,R.layout.marketinfo,new String[]{"成交时间","成交价","价格变动","成交量","成交额","性质"},new int[]{R.id.txtDealTime,R.id.txtPrice,R.id.txtPriceCHG,R.id.txtDealCount,R.id.txtDealAmount,R.id.txtDealType});
        SimpleAdapter.ViewBinder binder = new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object o, String s) {
                if(s.equalsIgnoreCase("卖盘")) {
                    ((TextView) view).setTextColor(Color.GREEN);
                }
                if(s.equalsIgnoreCase("买盘")) {
                    ((TextView) view).setTextColor(Color.RED);
                }
                if(s.equalsIgnoreCase("中性盘")) {
                    ((TextView) view).setTextColor(Color.WHITE);
                }
                return false;
            }
        };
        adapterMarket.setViewBinder(binder);
        mlvMarket.setAdapter(adapterMarket);

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

        mtxtLastClose = (TextView)findViewById(R.id.txtLastClose);
        mtxtLastClose.setText(String.format("%1$.2f",mLastClose));

        mtxtOpen = (TextView)findViewById(R.id.txtOPEN);
        mtxtHigh = (TextView)findViewById(R.id.txtHIGH);
        mtxtLow = (TextView)findViewById(R.id.txtLOW);
        mtxtTurnOver = (TextView)findViewById(R.id.txtTURNOVER);
        mtxtTurnVolumn = (TextView)findViewById(R.id.txtTURNVOLUMN);

        mtxtCHG = (TextView)findViewById(R.id.txtCHG);
        mtxtPCHG = (TextView)findViewById(R.id.txtPCHG);
        mtxtCurrentPrice = (TextView)findViewById(R.id.txtCurrentPrice);
        mtxtDate = (TextView)findViewById(R.id.txtDate);
        mGraph = (MyGraph)findViewById(R.id.myGraph);
    }

    /**
     * 初始化市场信息
     */
    private void initStockMarket() {
        mdeals = dbmgr.queryStockDeals(mCode,mDate);
        mlistMarket.clear();

        if(mdeals.size()==0){
            canStopTimer = false;
            mdeals = Utils.downloadDayDeals(dbmgr,mCode,mDate);
            canStopTimer = true;
        }

        if(mdeals.size()==0) {
            loadNextDayMarket();
            return;
        }

        for(int i = 0;i<mdisplayIndex;i++){
            StockDayDeal deal = mdeals.get(i);
            addMarketItem(deal);

            if(i == 0) {
                mCurrentPrice = mHigh = mLow = mOpen = deal.Price;
                mGraph.initMinuteGraph(mLastClose);
            }
        }
    }

    /**
     * 显示成交记录
     */
    private void displayMarket() {
        ++mdisplayIndex;

        if(mdisplayIndex == mdeals.size()){
            loadNextDayMarket();
            mGraphData.clear();
            return;
        }

        mlistMarket.remove(0);

        Message msg = new Message();
        msg.what = 1;
        mhandler.sendMessage(msg);

        StockDayDeal deal = mdeals.get(mdisplayIndex);
        addMarketItem(deal);
    }

    /**
     * 读取下一天分笔数据
     */
    private void loadNextDayMarket() {
        mLastClose = mCurrentPrice;
        //mCurrentPrice = 0;
        mVol = 0;
        mTurnover = 0;

        mdeals.clear();
        mGraphData.clear();
        mMinuteData.clear();
        mdisplayIndex = Utils.DISPLAYINDEX;
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

    /**
     * 成交记录列表
     * @param deal
     */
    private void addMarketItem(StockDayDeal deal) {
        HashMap<String,String> map = new HashMap<String, String>();
        map.put("成交时间", deal.DealTime);
        map.put("成交价", String.format("%1$.2f", deal.Price));
        map.put("价格变动",deal.PriceChange);
        map.put("成交量",String.valueOf(deal.DealCount));
        map.put("成交额",String.format("%1$.2f", deal.DealAmount / 10000.0));
        map.put("性质",deal.DealType);

        mlistMarket.add(map);

        Message msg = new Message();
        msg.what = 1;
        mhandler.sendMessage(msg);

        if(deal.Price > mHigh) mHigh = deal.Price;
        if(deal.Price < mLow) mLow = deal.Price;

        mCurrentPrice = deal.Price;
        mVol += deal.DealCount;
        mTurnover += deal.DealAmount;
        mChg = mCurrentPrice - mLastClose;

        DrawMinGraph(deal);
    }

    /**
     * 绘制分时图
     * @param deal
     */
    private void DrawMinGraph(StockDayDeal deal) {
        if(mMinuteData != null && mMinuteData.size()>1) {
            try {
                int size = mMinuteData.size();

                Date dealtime = Utils.TimeFormatter.parse(deal.DealTime);
                Date lasttime = Utils.TimeFormatter.parse(mMinuteData.get(size-1).DealTime);

                if((dealtime.getTime() - lasttime.getTime())>60*1000)
                    mGraphData.clear();

                mGraphData.add(deal);

                List<StockDayDeal> minutedata = Utils.GetMinuteData(mGraphData,lasttime);

                int size2 = minutedata.size();

                Date time = Utils.TimeFormatter.parse(minutedata.get(size2-1).DealTime);

                if(time.compareTo(lasttime)==0)
                    mMinuteData.set(size-1,minutedata.get(size2-1));
                else
                    mMinuteData.addAll(minutedata);

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }else {
            mGraphData.add(deal);
            try {
                Date t1 =Utils.TimeFormatter.parse("09:30:00");
                mMinuteData = Utils.GetMinuteData(mGraphData,t1);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        mGraph.DrawMinuteGraph(mMinuteData);
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

    public void onDestroy(){
        Log.d(this.getLocalClassName(),"退出清理");

        if(mtimer != null) {
            while(canStopTimer){
                mtimer.cancel();
                Log.d(this.getLocalClassName(),"线程终止");
                canStopTimer = false;
            }
        }

        super.onDestroy();
    }
}
