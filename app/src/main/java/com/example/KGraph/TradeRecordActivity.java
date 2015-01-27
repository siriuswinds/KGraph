package com.example.KGraph;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TradeRecordActivity extends Activity {
    private DBManager dbmgr;
    private SimpleAdapter adapter;
    private ArrayList<Map<String,String>> mList;
    private ListView m_tradelist;
    private Button mbtnPrePage,mbtnNextPage,mbtnReturn,mbtnRefresh;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traderecord);
        dbmgr = new DBManager(this);

        mList = new ArrayList<Map<String, String>>();
        adapter = new SimpleAdapter(this,mList,R.layout.traderecordlist,new String[]{"date","time","price","turnover","turnvolumn","tradetype"},new int[]{R.id.txtTradeDate,R.id.txtTradeTime,R.id.txtPrice,R.id.txtTURNOVER,R.id.txtTURNVOLUMN,R.id.txtTRADETYPE});
        m_tradelist = (ListView)findViewById(R.id.tradelist);
        m_tradelist.setAdapter(adapter);
        initTradeList();

        mbtnReturn = (Button)findViewById(R.id.btnReturn);
        mbtnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(Activity.RESULT_OK);
                finish();
            }
        });
    }

    private void initTradeList() {
        TradeManager trademgr = new TradeManager(dbmgr);
        List<TradeRecord> tradeRecords = trademgr.getTradeRecords();
        loadTradeRecordList(tradeRecords);
    }
    private void loadTradeRecordList(List<TradeRecord> traderecords){
        if(traderecords!=null && traderecords.size()>0) {
            mList.clear();
            //加载到listview
            for (TradeRecord record : traderecords) {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("date", Utils.DayFormatter.format(record.TradeTime));
                map.put("time", Utils.TimeFormatter.format(record.TradeTime));
                map.put("price", String.format("%.2f",record.Price));
                map.put("turnover", String.format("%.2f",record.TurnOver));
                map.put("turnvolumn", String.format("%.0f",record.TurnVolumn));
                map.put("tradetype",Utils.TRADETYPETOSTRING(record.TradeType));

                mList.add(map);
            }
            adapter.notifyDataSetChanged();
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
}
