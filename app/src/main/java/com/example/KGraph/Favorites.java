package com.example.KGraph;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Favorites extends Activity {
    private DBManager dbmgr;
    private SimpleAdapter adapter;
    private ArrayList<Map<String,String>> mList;
    private Button mbtnReturn,mbtnNextPage,mbtnPrePage;
    private ListView m_stocklist;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
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
                intent.setClass(Favorites.this,StockDayList.class);
                Bundle bundle = new Bundle();
                bundle.putString("STOCKCODE",code);
                intent.putExtras(bundle);
                startActivity(intent);
                mTxtcode = null;
            }
        });

        initFavorites();

        mbtnReturn = (Button)findViewById(R.id.btnReturn);
        mbtnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(Activity.RESULT_OK);
                finish();
            }
        });
    }

    private void initFavorites() {
        List<StockDay> stocks = dbmgr.queryFavorite();
        loadStockList(stocks);
    }

    private void loadStockList(List<StockDay> stocks){
        if(stocks!=null && stocks.size()>0) {
            mList.clear();
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
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_favorites, menu);
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
