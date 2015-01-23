package com.example.KGraph;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yangj on 13-12-12.
 */
public class StockList extends Activity {
    private DBManager dbmgr;
    private SimpleAdapter adapter;
    private ArrayList<Map<String,String>> mList;
    private ListView m_stocklist;
    private Button mBtnPrePage,mBtnNextPage,mbtnReturn;
    private EditText mtxtSearchCode;
    private int mPageIndex = 0,mLineCount = 11,mPageCount = 0;
    private String mQuery="";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stockview);
        dbmgr = new DBManager(this);

        mList = new ArrayList<Map<String, String>>();
        adapter = new SimpleAdapter(this,mList,R.layout.stocklist,new String[]{"code","name","industry","region","price","open","high","low","chg","phg"},new int[]{R.id.txtStockCode,R.id.txtStockName,R.id.txtIndustry,R.id.txtRegion,R.id.txtPrice,R.id.txtOPEN,R.id.txtHIGH,R.id.txtLOW,R.id.txtCHG,R.id.txtPHG});
        m_stocklist = (ListView)findViewById(R.id.stocklist);
        m_stocklist.setAdapter(adapter);
        m_stocklist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView mTxtcode = (TextView)view.findViewById(R.id.txtStockCode);
                String code = mTxtcode.getText().toString();
                Intent intent = new Intent();
                intent.setClass(StockList.this,StockDayList.class);
                Bundle bundle = new Bundle();
                bundle.putString("STOCKCODE",code);
                intent.putExtras(bundle);
                startActivity(intent);
                mTxtcode = null;
            }
        });

        initStockList(mPageIndex,mLineCount);

        mtxtSearchCode=(EditText)findViewById(R.id.txtSearchCode);
        mtxtSearchCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mQuery = editable.toString();
                loadStockList(mPageIndex,mLineCount,mQuery);
            }
        });

        mBtnPrePage=(Button)findViewById(R.id.buttonPrePage);
        mBtnPrePage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mPageIndex>0){
                    --mPageIndex;
                    loadStockList(mPageIndex*mLineCount,mLineCount,mQuery);
                }
            }
        });

        mBtnNextPage = (Button)findViewById(R.id.buttonNextPage);
        mBtnNextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mPageIndex<mPageCount-1){
                    ++mPageIndex;
                    loadStockList(mPageIndex*mLineCount,mLineCount,mQuery);
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
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        dbmgr.closeDB();
    }

    public void initStockList(int index,int count){
        List<StockDay> stocks = dbmgr.queryStock(index,count,"");
        mPageCount = dbmgr.queryStockCount("");
        //如果本地数据库没有数据，从本地文件读取，并写入数据库
        if(stocks==null || stocks.size()==0){
            List<StockDay> stocks1 = StockDay.ReadFromFile(this.getApplicationContext());
            dbmgr.addStock(stocks1);
            mPageCount = stocks1.size();
            stocks = dbmgr.queryStock(index,count,"");
        }
        loadStockList(stocks);
    }

    public void loadStockList(int index,int count,String cnd){
        List<StockDay> stocks = dbmgr.queryStock(index,count,cnd);
        mPageCount = dbmgr.queryStockCount(cnd);
        loadStockList(stocks);
    }

    private void loadStockList(List<StockDay> stocks){
        if(stocks!=null && stocks.size()>0) {
            mList.clear();
            //加载到listview
            for (StockDay stock : stocks) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("name", stock.NAME);
                map.put("code", stock.CODE);
                map.put("industry", stock.INDUSTRY);
                map.put("region", stock.REGION);
                map.put("price","");
                map.put("open","");
                map.put("high","");
                map.put("close","");
                map.put("chg","");
                map.put("phg","");
                mList.add(map);
            }
            //http://hq.sinajs.cn/?_=0.9504842679016292&list=sh601988,sh000001
            //http://hq.sinajs.cn/rn=1421287988869&list=s_sh600030,s_sz002736,s_sh601766,s_sh601669,s_sh600795,s_sh601899
            //http://qt.gtimg.cn/r=0.7385807468090206q=s_sh601398,s_sh601328,s_sh601288,s_sh600036,s_sh601818,s_sh601939
            //http://api.money.126.net/data/feed/0000001,0601988,hk04601,money.api?callback=_ntes_quote_callback809987
            adapter.notifyDataSetChanged();
        }
    }
}
