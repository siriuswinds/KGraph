package com.example.KGraph;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

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
                String code  = view.findViewById(R.id.txtStockCode).toString();
                loadStockDayListActivity(code);
            }
        });

        this.registerForContextMenu(m_stocklist);
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

    /**
     * 加载日线数据
     * @param code
     */
    private void loadStockDayListActivity(String code) {
        Intent intent = new Intent();
        intent.setClass(Favorites.this,StockDayList.class);
        Bundle bundle = new Bundle();
        bundle.putString("STOCKCODE",code);
        intent.putExtras(bundle);
        startActivity(intent);
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
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo){
        menu.add(0,1,0,"日线数据");
        menu.add(0,2,0,"移除自选");

        super.onCreateContextMenu(menu,v,menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem mi){
        AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo)mi.getMenuInfo();
        HashMap<String, String> map  = (HashMap<String, String>)adapter.getItem(menuInfo.position);
        String code = map.get("code");

        switch (mi.getItemId()){
            case 1:
                loadStockDayListActivity(code);
                break;
            case 2:
                dbmgr.removeFavorite(code);
                Toast.makeText(getApplicationContext(), "已经移除自选", Toast.LENGTH_SHORT).show();
                initFavorites();
                break;
        }
        return super.onContextItemSelected(mi);
    }

}
