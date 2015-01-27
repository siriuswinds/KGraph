package com.example.KGraph;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class TradeActivity extends Activity {
    private Button mbtnReturn,mbtnBuy,mbtnSell;
    private EditText mtxtPrice,mtxtVolumn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trade);

        initMyHoldStocks();
        initStockGraph();

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
    }

    private void initStockGraph() {

    }

    private void initMyHoldStocks() {

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
