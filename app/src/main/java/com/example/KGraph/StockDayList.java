package com.example.KGraph;

import android.app.*;
import android.content.*;
import android.os.*;
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
    private Button mbtnPreYear,mbtnNextYear;
	private Thread currentThread;
    private MyHandler myHandler;
    private DBManager dbMgr;
    private ArrayList<Map<String,String>> list;
	private Calendar m_currentDate;
	private SimpleDateFormat m_sdf;
    private int mYearIndex;
    private int mYearCurr;
    private String[] WeekName = new String[]{"星期日","星期一","星期二","星期三","星期四","星期五","星期六"};
	
    List<StockDay> m_downlowdStocks = null;
    SimpleAdapter adapter;
    String stockcode;
	String urlcode;
    String urltpl = "http://quotes.money.163.com/service/chddata.html?code=%1$s&start=%2$s&end=%3$s&fields=TCLOSE;HIGH;LOW;TOPEN;LCLOSE;CHG;PCHG;TURNOVER;VOTURNOVER;VATURNOVER;TCAP;MCAP";
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

        m_sdf=new SimpleDateFormat("yyyy-MM-dd");
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
                Intent intent = new Intent();
                intent.setClass(StockDayList.this,StockDayDetails.class);
                Bundle bundle = new Bundle();
                bundle.putString("STOCKCODE",stockcode);
                bundle.putString("TRANSDATE",date);
                intent.putExtras(bundle);
                startActivity(intent);
                mTxtDate = null;
            }
        });

        list = new ArrayList<Map<String, String>>();
        adapter = new SimpleAdapter(this,list,R.layout.stockdaylist,new String[]{"date","week","tclose","chg","pchg","TOPEN","HIGH","LOW"},new int[]{R.id.txtDate,R.id.txtWeek,R.id.txtTCLOSE,R.id.txtCHG,R.id.txtPCHG,R.id.txtTOPEN,R.id.txtHIGH,R.id.txtLOW});
		m_stockdaylist.setAdapter(adapter);

        mbtnPreYear = (Button)findViewById(R.id.btnPreYear);
        mbtnNextYear = (Button)findViewById(R.id.btnNextYear);
        mbtnPreYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mYearIndex>1990){
                    --mYearIndex;
                    loadStockDays();
                }
            }
        });
        mbtnNextYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mYearIndex < mYearCurr) {
                    ++mYearIndex;
                    loadStockDays();
                }
            }
        });

        loadStockDays();
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
			adapter.notifyDataSetChanged();
		}
    }

    private void loadStockList(List<StockDay> stocks){
        //加载到listview
        list.clear();

        for (StockDay stock:stocks){
            HashMap<String,String> map = new HashMap<String, String>();
            map.put("date", stock.TRANSDATE);
            try {
                Date dt = m_sdf.parse(stock.TRANSDATE);
                Calendar c = Calendar.getInstance();
                c.setTime(dt);
                int week = c.get(Calendar.DAY_OF_WEEK);
                map.put("week",WeekName[week-1]);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            map.put("tclose",String.format("%.2f",stock.TCLOSE));
            map.put("chg",String.format("%.2f",stock.CHG));
            map.put("pchg",String.format("%1$.2f",stock.PCHG).concat("%"));
            map.put("TOPEN",String.format("%.2f",stock.TOPEN));
            map.put("HIGH",String.format("%.2f",stock.HIGH));
            map.put("LOW",String.format("%.2f",stock.LOW));
            //map.put("LCLOSE",String.format("%.2f",stock.LCLOSE));
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
                    dbMgr.addStockDay(m_downlowdStocks);
                }

                loadStockList(m_downlowdStocks);

                Message msg = myHandler.obtainMessage();
                msg.what = StockDayDetails.MyHandler.FINISH_DOWNLOAD_MESSAGE;
                myHandler.sendMessage(msg);
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
