package com.example.KGraph;

import android.app.*;
import android.content.*;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.widget.AbsListView.OnScrollListener;
import java.text.*;
import java.text.ParseException;
import java.util.*;
import org.apache.http.*;
import org.apache.http.client.methods.*;
import org.apache.http.params.*;
import org.apache.http.util.*;

/**
 * Created by yangj on 13-12-16.
 */
public class StockDayList extends Activity{
    private ListView m_stockdaylist;
    private Button mbtnPreYear,mbtnNextYear,mbtnReturn,mbtnRefresh,mbtnAddFavorite;
	private Thread currentThread;
    private MyHandler myHandler;
    private DBManager dbMgr;
    private ArrayList<Map<String,String>> list;
	private Calendar m_currentDate;
    private int mYearIndex;
    private int mYearCurr;
	
    List<StockDay> m_downlowdStocks = null;
    SimpleAdapter adapter;
    String stockcode;
	String urlcode;
    String urltpl = Utils.DAYURL;
    String stockurl;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stockdayview);

        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        stockcode = bundle.getString("STOCKCODE");
        String code = stockcode;
        this.setTitle(code);
        myHandler = new MyHandler();
        dbMgr = new DBManager(this);

        m_currentDate= Calendar.getInstance();
        mYearIndex = m_currentDate.get(Calendar.YEAR);
        mYearCurr = mYearIndex;

        if(Integer.parseInt(code)<600000)
            urlcode = "1"+code;
        else
            urlcode = "0"+code;

        m_stockdaylist = (ListView)findViewById(R.id.stockdaylist);
        m_stockdaylist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView mTxtDate = (TextView)view.findViewById(R.id.txtDate);
                String date = mTxtDate.getText().toString();
                openStockDayDetials(date);
                mTxtDate = null;
            }
        });

        this.registerForContextMenu(m_stockdaylist);

        list = new ArrayList<Map<String, String>>();
        adapter = new SimpleAdapter(this,list,R.layout.stockdaylist,new String[]{"date","week","tclose","chg","pchg","TOPEN","HIGH","LOW","TURNOVER","VATURNOVER"},new int[]{R.id.txtDate,R.id.txtWeek,R.id.txtTCLOSE,R.id.txtCHG,R.id.txtPCHG,R.id.txtTOPEN,R.id.txtHIGH,R.id.txtLOW,R.id.txtTURNOVER,R.id.txtVATURNOVER});
		m_stockdaylist.setAdapter(adapter);

        mbtnPreYear = (Button)findViewById(R.id.btnPreYear);
        mbtnNextYear = (Button)findViewById(R.id.btnNextYear);
        mbtnNextYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mYearIndex>1990){
                    --mYearIndex;
                    loadStockDays();
                    adapter.notifyDataSetChanged();
                }
            }
        });
        mbtnPreYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mYearIndex < mYearCurr) {
                    ++mYearIndex;
                    loadStockDays();
                    adapter.notifyDataSetChanged();
                }
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
        mbtnRefresh = (Button)findViewById(R.id.btnRefresh);
        mbtnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateStockDays();
            }
        });
        mbtnAddFavorite = (Button)findViewById(R.id.btnAddFavorite);
        mbtnAddFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbMgr.addFavorite(stockcode);
                Toast.makeText(getApplicationContext(),"已经加入自选股",Toast.LENGTH_SHORT).show();
            }
        });

        loadStockDays();
        adapter.notifyDataSetChanged();
    }

    /**
     * 打开分笔明细列表
     * @param date
     */
    private void openStockDayDetials(String date) {
        Intent intent = new Intent();
        intent.setClass(StockDayList.this,StockDayDetails.class);
        Bundle bundle = new Bundle();
        bundle.putString("STOCKCODE",stockcode);
        bundle.putString("TRADEDATE",date);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo){
        menu.add(0,1,0,"分笔明细");
        menu.add(0,2,0,"模拟交易");
        super.onCreateContextMenu(menu,v,menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem mi){
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo)mi.getMenuInfo();
        HashMap<String, String> map  = (HashMap<String, String>)adapter.getItem(menuInfo.position);
        HashMap<String, String> Lmap  = (HashMap<String, String>)adapter.getItem(menuInfo.position+1);
        String date = map.get("date");
        String lclose = Lmap.get("tclose");

        switch (mi.getItemId()){
            case 1:
                openStockDayDetials(date);
                break;
            case 2:
                startStockTrade(date,lclose);
                break;
        }
        return super.onContextItemSelected(mi);
    }

    /**
     * 开始模拟交易
     * @param date
     */
    private void startStockTrade(String date,String lclose) {
        Intent intent = new Intent();
        intent.setClass(StockDayList.this,TradeActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("STOCKCODE",stockcode);
        bundle.putString("TRADEDATE",date);
        bundle.putString("LCLOSE",lclose);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void loadStockDays(){
        String Year = String.valueOf(mYearIndex);
        String cn1 = Year.concat("-12-31");
        String cs1 = Year.concat("-01-01");
        String cn2 = Year.concat("1231");
        String cs2 = Year.concat("0101");
        stockurl = String.format(urltpl,urlcode,cs2,cn2);
        loadStockDays(stockcode,cs1,cn1);
	}

    private void loadStockDays(String code,String startdate,String enddate){
        m_downlowdStocks = dbMgr.queryStockDay(code,startdate,enddate);

        if(m_downlowdStocks == null || m_downlowdStocks.size() ==0)
            ThreadPoolUtils.execute(new MyRunnable());
        else{
            loadStockList(m_downlowdStocks);
		}
    }

    /**
     * 更新日数据库
     */
    private void updateStockDays(){
        Date lastdate = dbMgr.getlastStockday(stockcode);
        Calendar cal = Calendar.getInstance();
        cal.setTime(lastdate);
        int lastyear = cal.get(Calendar.YEAR);

        if(cal.before(m_currentDate)&&(lastyear == m_currentDate.get(Calendar.YEAR))){
            String cn1 = Utils.DayFormatter.format(m_currentDate.getTime());
            cal.add(Calendar.DATE,1);
            String cs1 = Utils.DayFormatter.format(cal.getTime());
            String cn2 = cn1.replace("-","");
            String cs2 = cs1.replace("-","");
            stockurl = String.format(urltpl,urlcode,cs2,cn2);
            loadStockDays(stockcode,cs1,cn1);
        }

        if((mYearIndex == lastyear)&&(lastyear<mYearCurr)){
            cal.add(Calendar.DATE,1);
            String Year = String.valueOf(mYearIndex);
            String cn1 = Year.concat("-12-31");
            String cs1 = Utils.DayFormatter.format(cal.getTime());
            String cn2 = Year.concat("1231");
            String cs2 = cs1.replace("-","");
            stockurl = String.format(urltpl,urlcode,cs2,cn2);
            loadStockDays(stockcode,cs1,cn1);
        }
    }

    /**
     * 加载日数据到列表
     * @param stocks
     */
    private void loadStockList(List<StockDay> stocks){
        list.clear();
        for (StockDay stock:stocks){
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
            map.put("TURNOVER",String.format("%.2f",stock.TURNOVER).concat("%"));
            map.put("VATURNOVER",String.format("%.2f",stock.VATURNOVER/100000000));//亿元
            //map.put("LCLOSE",String.format("%.2f",stock.LCLOSE));
            //TURNOVER;VOTURNOVER;VATURNOVER
            list.add(map);
        }
    }

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
                    m_downlowdStocks = StockDay.parse(result);
                    if(m_downlowdStocks.size()>0) {
                        dbMgr.addStockDay(m_downlowdStocks);
                        loadStockDays();
                    }
                }

                Message msg = myHandler.obtainMessage();
                msg.what = StockDayDetails.MyHandler.FINISH_DOWNLOAD_MESSAGE;
                myHandler.sendMessage(msg);
            }catch (Exception ex){
                Log.e("下载日线数据",ex.getMessage());
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
