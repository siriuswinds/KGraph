package com.example.KGraph;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class TradeKActivity extends Activity {
    private Button mbtnReturn,mbtnBuy,mbtnSell;
    private MyGraph mGraph;
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
    private List<StockDay> mlists,mGraphData;
    private int mdisplayIndex = Utils.DISPLAYKINDEX;
    private float mLastClose,mOpen,mHigh,mLow,mChg,mPchg,mVol,mTurnover,mCurrentPrice;
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
        mtxtPCHG.setText(String.format("%1$.2f",mPchg).concat("%"));

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

        mtimer = new Timer();
        dbmgr = new DBManager(this);
        trademgr = new TradeManager(dbmgr);
        mGraphData = new ArrayList<StockDay>();

        initControls();
        initHoldStocks();

        mtask = new TimerTask() {
            @Override
            public void run() {
                if(mlists == null || (mdisplayIndex == Utils.DISPLAYKINDEX && mlists.size()==0))
                    initStockMarket();
                else
                    displayMarket();
            }
        };

        mtimer.schedule(mtask,1,Utils.KSPEED);
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
        adapterMarket = new SimpleAdapter(this,mlistMarket,R.layout.stockdaylist,new String[]{"date","week","tclose","chg","pchg","TOPEN","HIGH","LOW","TURNOVER","VATURNOVER"},new int[]{R.id.txtDate,R.id.txtWeek,R.id.txtTCLOSE,R.id.txtCHG,R.id.txtPCHG,R.id.txtTOPEN,R.id.txtHIGH,R.id.txtLOW,R.id.txtTURNOVER,R.id.txtVATURNOVER});
        SimpleAdapter.ViewBinder binder = new SimpleAdapter.ViewBinder(){
            @Override
            public boolean setViewValue(View view, Object o, String s) {
                int length = s.length();
                if(s.substring(length-1).equalsIgnoreCase("%")){
                    TableRow row = (TableRow)view.getParent();
                    TextView txt = (TextView)row.getChildAt(1);

                    String val2 = s.substring(0,length-1);
                    if(Float.parseFloat(val2)>0){
                        ((TextView) view).setTextColor(Color.RED);
                        txt.setTextColor(Color.RED);
                    }
                    if(Float.parseFloat(val2)==0) {
                        ((TextView) view).setTextColor(Color.WHITE);
                        txt.setTextColor(Color.WHITE);
                    }
                    if(Float.parseFloat(val2)<0) {
                        ((TextView) view).setTextColor(Color.GREEN);
                        txt.setTextColor(Color.GREEN);
                    }
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
        Calendar c = Calendar.getInstance();
        try {
            Date d = Utils.DayFormatter.parse(mDate);
            c.setTime(d);
            c.add(Calendar.DATE,-Utils.DISPLAYKINDEX);
            mDate = Utils.DayFormatter.format(c.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        mlists = dbmgr.queryStockDay(mCode, mDate);
        mlistMarket.clear();
        Message msg = new Message();
        msg.what = 1;
        mhandler.sendMessage(msg);

        for(int i = 0;i<mdisplayIndex&&i<mlists.size();i++){
            StockDay stock = mlists.get(i);
            addMarketItem(stock);

            if(i == 0) {
                mCurrentPrice = mHigh = mLow = mOpen = stock.TOPEN;
                mGraph.initKGraph(mLastClose);
            }
        }
    }

    /**
     * 显示成交记录
     */
    private void displayMarket() {
        ++mdisplayIndex;

        if(mdisplayIndex < mlists.size()){
            mlistMarket.remove(0);
            Message msg = new Message();
            msg.what = 1;
            mhandler.sendMessage(msg);
            mGraphData.remove(0);
            StockDay stock = mlists.get(mdisplayIndex);
            addMarketItem(stock);
        }else
            stopTask();
    }

    /**
     * 成交记录列表
     * @param stock
     */
    private void addMarketItem(StockDay stock) {
        HashMap<String,String> map = new HashMap<String, String>();
        map.put("date", stock.TRANSDATE);
        try {
            Date dt = Utils.DayFormatter.parse(stock.TRANSDATE);
            Calendar c = Calendar.getInstance();
            c.setTime(dt);
            int week = c.get(Calendar.DAY_OF_WEEK);
            map.put("week",Utils.WeekName[week-1]);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        map.put("tclose",String.format("%.2f",stock.TCLOSE));
        map.put("chg",String.format("%.2f",stock.CHG));
        map.put("pchg",String.format("%1$.2f",stock.PCHG).concat("%"));
        map.put("TOPEN",String.format("%.2f",stock.TOPEN));
        map.put("HIGH",String.format("%.2f",stock.HIGH));
        map.put("LOW",String.format("%.2f",stock.LOW));
        map.put("TURNOVER",String.format("%.2f",stock.TURNOVER));//.concat("%"));
        map.put("VATURNOVER",String.format("%.2f",stock.VATURNOVER/100000000));//亿元
        //map.put("LCLOSE",String.format("%.2f",stock.LCLOSE));
        //TURNOVER;VOTURNOVER;VATURNOVER
        mlistMarket.add(map);

        Message msg = new Message();
        msg.what = 1;
        mhandler.sendMessage(msg);

        mGraphData.add(stock);

        mDate = stock.TRANSDATE;
        mHigh = stock.HIGH;
        mLow = stock.LOW;
        mLastClose = stock.LCLOSE;
        mCurrentPrice = stock.TCLOSE;
        mVol = stock.VOTURNOVER;
        mTurnover = stock.VATURNOVER;
        mChg = stock.CHG;
        mPchg = stock.PCHG;

        DrawKGraph();
    }

    private void DrawKGraph() {
        mGraph.DrawKGraph(mGraphData);
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
            adapterHoldStocks.notifyDataSetChanged();
        }
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
        stopTask();
        super.onDestroy();
    }

    private void stopTask() {
        if(mtimer != null) {
            while(canStopTimer){
                mtimer.cancel();
                Log.d(this.getLocalClassName(),"线程终止");
                canStopTimer = false;
            }
        }
    }
}
