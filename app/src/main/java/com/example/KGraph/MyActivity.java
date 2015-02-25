package com.example.KGraph;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MyActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new KGraphView(this));

        String url = "http://stock.gtimg.cn/data/index.php?appn=detail&action=download&c=sz300036&d=20130816";

        try
        {
            HttpClient hc = new DefaultHttpClient();
            HttpGet hg = new HttpGet(url);

            HttpResponse hr = hc.execute(hg);

            if(hr.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
            {
                InputStream is = hr.getEntity().getContent();

                BufferedReader br = new BufferedReader(new InputStreamReader(hr.getEntity().getContent()));
                StringBuffer string = new StringBuffer("");
                String lineStr = "";
                while ((lineStr = br.readLine()) != null) {
                    string.append(lineStr + "\n");
                }
                br.close();
                String resultStr = string.toString();
                System.out.println(resultStr);
            }
        }catch (Exception err)
        {
            System.out.println(err.getMessage());
        }
    }

    class KGraphView extends View{
        Paint paint;
        public KGraphView(Context context) {
            super(context);
            paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(1);
            paint.setStyle(Paint.Style.STROKE);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int m_width = this.getWidth();
            int m_height = this.getHeight();

            canvas.drawRect(new Rect(10, 10, m_width - 10, m_height * 4 / 10), paint);
        }
    }
}
