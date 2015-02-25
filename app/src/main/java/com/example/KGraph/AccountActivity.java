package com.example.KGraph;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class AccountActivity extends Activity {
    private DBManager dbmgr;
    private TradeManager trademgr;
    private Button mbtnPayInto,mbtnRollOut,mbtnReset;
    private TextView mtxtCapticalBalance,mtxtAvailableBalance,mtxtMarketValue,mtxtShares,mtxtAssets;
    private EditText mtxtPayInto,mtxtRollOut;
    private Account mAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        dbmgr = new DBManager(this);
        trademgr = new TradeManager(dbmgr);

        mtxtCapticalBalance = (TextView)findViewById(R.id.txtCapticalBalance);
        mtxtAvailableBalance = (TextView)findViewById(R.id.txtAvailableBalance);
        mtxtMarketValue = (TextView)findViewById(R.id.txtMarketValue);
        mtxtShares =(TextView)findViewById(R.id.txtShares);
        mtxtAssets = (TextView)findViewById(R.id.txtAssets);

        mtxtPayInto = (EditText)findViewById(R.id.txtPayInto);
        mtxtRollOut = (EditText)findViewById(R.id.txtRollOut);

        initAccountInfo();

        mbtnPayInto = (Button)findViewById(R.id.btnPayInto);
        mbtnPayInto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sPayNum = mtxtPayInto.getText().toString();
                float fPayNum = Float.parseFloat(sPayNum);
                trademgr.PayInto(fPayNum);

                mAccount.AVAILABLEBALANCE += fPayNum;
                mAccount.CAPITALBALANCE += fPayNum;
                mAccount.ASSETS += fPayNum;

                Toast.makeText(getApplicationContext(),"已经转入",Toast.LENGTH_SHORT).show();
                mtxtPayInto.setText("");
                dbmgr.saveAccountInfo(mAccount);
                UpdateAccount();
            }
        });
        mbtnReset = (Button)findViewById(R.id.btnReset);
        mbtnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbmgr.deleteTradeRecords();
                dbmgr.deleteAccount();
                Toast.makeText(getApplicationContext(),"账户和交易记录已重置",Toast.LENGTH_SHORT).show();
            }
        });
        mbtnRollOut = (Button)findViewById(R.id.btnRollOut);
        mbtnRollOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String sRollOut = mtxtRollOut.getText().toString();
                float fRollOut = -Float.parseFloat(sRollOut);
                trademgr.RollOut(fRollOut);

                mAccount.CAPITALBALANCE += fRollOut;
                mAccount.AVAILABLEBALANCE +=fRollOut;
                mAccount.ASSETS += fRollOut;

                Toast.makeText(getApplicationContext(),"已经转出",Toast.LENGTH_SHORT).show();
                mtxtRollOut.setText("");
                dbmgr.saveAccountInfo(mAccount);
                UpdateAccount();
            }
        });
    }

    /**
     * 初始化账户信息
     */
    private void initAccountInfo() {
        mAccount = dbmgr.LoadAccountInfo();

        UpdateAccount();
    }

    /**
     * 更新账户信息
     */
    private void UpdateAccount() {
        mtxtAssets.setText(String.format("%.2f",mAccount.ASSETS));
        mtxtShares.setText(String.format("%.2f",mAccount.SHARES));
        mtxtMarketValue.setText(String.format("%.2f",mAccount.MARKETVALUE));
        mtxtAvailableBalance.setText(String.format("%.2f",mAccount.AVAILABLEBALANCE));
        mtxtCapticalBalance.setText(String.format("%.2f",mAccount.CAPITALBALANCE));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_account, menu);
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
