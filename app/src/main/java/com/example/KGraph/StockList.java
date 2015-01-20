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
    private ListView m_stocklist;
    private SimpleAdapter adapter;
    private Button mBtnPrePage,mBtnNextPage;
    private EditText mtxtSearchCode;
    private int mPageIndex = 0,mLineCount = 10,mPageCount = 0;
    private ArrayList<Map<String,String>> mList;
    private String mQuery="";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stockview);
        dbmgr = new DBManager(this);

        mtxtSearchCode = (EditText)findViewById(R.id.txtSearchCode);
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
                loadStockList(0,mLineCount,mQuery);
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

        m_stocklist = (ListView)findViewById(R.id.stocklist);
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
        mList = new ArrayList<Map<String, String>>();
        adapter = new SimpleAdapter(this,mList,R.layout.stocklist,new String[]{"code","name","industry","region"},new int[]{R.id.txtStockCode,R.id.txtStockName,R.id.txtIndustry,R.id.txtRegion});
        m_stocklist.setAdapter(adapter);
        loadStockList(mPageIndex,mLineCount,"");
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        dbmgr.closeDB();
    }

    public void loadStockList(int index,int count,String query){
        List<StockDay> stocks = dbmgr.queryStock(index,count,query);
        mPageCount = dbmgr.queryStockCount(query);

        //如果本地数据库没有数据，从本地文件读取，并写入数据库
        if(stocks==null || stocks.size()==0){
            List<StockDay> stocks1 = StockDay.ReadFromFile(this.getApplicationContext());
            dbmgr.addStock(stocks1);
            mPageCount = stocks1.size();
            stocks = dbmgr.queryStock(index,count,query);
        }

        if(stocks!=null && stocks.size()>0) {
            mList.clear();
            //加载到listview
            for (StockDay stock : stocks) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("name", stock.NAME);
                map.put("code", stock.CODE);
                map.put("industry", stock.INDUSTRY);
                map.put("region", stock.REGION);
                mList.add(map);
            }
            //adapter = new SimpleAdapter(this,list,R.layout.stocklist,new String[]{"code","name","industry","region"},new int[]{R.id.txtStockCode,R.id.txtStockName,R.id.txtIndustry,R.id.txtRegion});
            //m_stocklist.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }
}
