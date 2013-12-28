package com.example.KGraph;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stockview);
        dbmgr = new DBManager(this);

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
        //加载stock列表
        loadStockList();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        dbmgr.closeDB();
    }

    public void loadStockList(){
        List<StockDay> stocks = dbmgr.queryStock();

        //如果本地数据库没有数据，从本地文件读取，并写入数据库
        if(stocks==null || stocks.size()==0){
            stocks = StockDay.ReadFromFile(this.getApplicationContext());
            dbmgr.addStock(stocks);
        }

        //加载到listview
        ArrayList<Map<String,String>> list = new ArrayList<Map<String, String>>();
        for (StockDay stock:stocks){
            HashMap<String,String> map = new HashMap<String, String>();
            map.put("name",stock.NAME);
            map.put("code",stock.CODE);
            map.put("industry",stock.INDUSTRY);
            map.put("region",stock.REGION);
            list.add(map);
        }
        //SimpleAdapter adapter = new SimpleAdapter(this,list,android.R.layout.simple_list_item_2,new String[]{"name","info"},new int[]{android.R.id.text1,android.R.id.text2});
        adapter = new SimpleAdapter(this,list,R.layout.stocklist,new String[]{"code","name","industry","region"},new int[]{R.id.txtStockCode,R.id.txtStockName,R.id.txtIndustry,R.id.txtRegion});
        m_stocklist.setAdapter(adapter);
		
    }

    public void queryTheCursor(){
        /*
        Cursor c = dbmgr.queryTheCursor();
        startManagingCursor(c);
        CursorWrapper cursorWrapper = new CursorWrapper(c){
            @Override
            public String getString(int columnIndex){
                if(getColumnName(columnIndex).equals("CODE")){
                    String code = getString(getColumnIndex("CODE"));
                    return code;
                }
                return super.getString(columnIndex);
            }
        };
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,android.R.layout.simple_list_item_2,cursorWrapper,new String[]{"name","code"},new int[]{android.R.id.text1,android.R.id.text2});
        */
    }
}
