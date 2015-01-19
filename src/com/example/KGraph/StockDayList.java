package com.example.KGraph;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.widget.AbsListView.OnScrollListener;
import java.text.*;
import java.util.*;
import org.apache.http.*;
import org.apache.http.client.methods.*;
import org.apache.http.params.*;
import org.apache.http.util.*;

/**
 * Created by yangj on 13-12-16.
 */
public class StockDayList extends Activity implements OnScrollListener{
    private ListView m_stockdaylist;
	private View header,footer;
	private Button scrollInfo;
	private Thread currentThread;
    private MyHandler myHandler;
    private DBManager dbMgr;
    private ArrayList<Map<String,String>> list;
	private Calendar m_currentDate;
	private SimpleDateFormat m_sdf;
	
    List<StockDay> m_downlowdStocks = null;
    SimpleAdapter adapter;
    String stockcode;
	String urlcode;
    String stockurl = "http://quotes.money.163.com/service/chddata.html?code=%1$s&start=%2$s&end=%3$s&fields=TCLOSE;HIGH;LOW;TOPEN;LCLOSE;CHG;PCHG;TURNOVER;VOTURNOVER;VATURNOVER;TCAP;MCAP";

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

        if(Integer.parseInt(code)<600000)
            urlcode = "1"+code;
        else
            urlcode = "0"+code;

        m_stockdaylist = (ListView)findViewById(R.id.stockdaylist);
		//scrollInfo=(Button)findViewById(R.id.scroll_info);
		//header=getLayoutInflater().inflate(R.layout.simple_text,null);
		//((TextView)header.findViewById(R.id.text1)).setText("头部");
		//footer=getLayoutInflater().inflate(R.layout.simple_text,null);
		//((TextView)footer.findViewById(R.id.text1)).setText("尾部");
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
        adapter = new SimpleAdapter(this,list,R.layout.stockdaylist,new String[]{"date","tclose","chg","pchg","TURNOVER","VOTURNOVER"},new int[]{R.id.txtDate,R.id.txtTCLOSE,R.id.txtCHG,R.id.txtPCHG,R.id.txtTURNOVER,R.id.txtVOTURNOVER});
        
		//m_stockdaylist.addHeaderView(header,null,false);
		m_stockdaylist.setAdapter(adapter);
		//m_stockdaylist.addFooterView(footer,null,false);
		//m_stockdaylist.setOnScrollListener(this);
		
		m_sdf=new SimpleDateFormat("yyyy-MM-dd");
		
		m_currentDate= Calendar.getInstance();
		
        loadStockDays();
    }
	
	@Override
	public void onScrollStateChanged(AbsListView view,int scrollState){
		switch(scrollState){
			case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
			case OnScrollListener.SCROLL_STATE_FLING:
				if(view.getLastVisiblePosition()>=view.getCount()-2){
					if(currentThread==null||!currentThread.isAlive()){
						m_stockdaylist.addFooterView(footer,null,false);
						loadStockDays();
					}
				}
				break;
			case OnScrollListener.SCROLL_STATE_IDLE:
				break;
		}
	}
	
	@Override
	public void onScroll(AbsListView view,int firstVisibleItem,int visibleItemCount,int totalItemCount){
		
	}
	
	private void loadStockDays(){
		String cn1=m_sdf.format(m_currentDate.getTime());
		String cn2=cn1.replace("-",""); //m_sdf2.format(m_currentDate.getTime());
		m_currentDate.add(Calendar.DATE,-1);
		
		for(int i=0;i<50;i++){
		   m_currentDate.add(Calendar.DATE,-1);
		}

		String cs1=m_sdf.format(m_currentDate.getTime());
		String cs2=cs1.replace("-","");//m_sdf2.format(m_currentDate.getTime());

        stockurl = String.format(stockurl,urlcode,cs2,cn2);

        loadStockDays(stockcode,cs1,cn1);
	}

    private void loadStockDays(String code,String startdate,String enddate){
		//Date day= dbMgr.getlastStockday(code);
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
		
		//new AlertDialog.Builder(this).setMessage(stocks.get(0).TRANSDATE).show();
        for (StockDay stock:stocks){
            HashMap<String,String> map = new HashMap<String, String>();
            map.put("date", stock.TRANSDATE);
            map.put("tclose",String.valueOf(stock.TCLOSE));
            map.put("chg",String.valueOf(stock.CHG));
            map.put("pchg",String.format("%1$.2f",stock.PCHG));
            map.put("TURNOVER",String.format("%1$.2f",stock.TURNOVER));
            map.put("VOTURNOVER",String.format("%1$.2f",stock.VOTURNOVER/10000.0));
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
