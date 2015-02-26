package com.example.KGraph;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class SettingActivity extends Activity {
    private Button btnOK;
    private EditText txtSpeed,txtTradeListCount;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        btnOK = (Button)findViewById(R.id.btnOK);
        txtSpeed = (EditText)findViewById(R.id.txtSpeed);
        txtTradeListCount = (EditText)findViewById(R.id.txtTradeListCount);
        txtSpeed.setText(String.valueOf(Utils.SPEED));
        txtTradeListCount.setText(String.valueOf(Utils.TRADELISTCOUNT));

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.SPEED = Integer.parseInt(String.valueOf(txtSpeed.getText()));
                Utils.TRADELISTCOUNT = Integer.parseInt(String.valueOf(txtTradeListCount.getText()));
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_setting, menu);
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
