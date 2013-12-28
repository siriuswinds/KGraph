package com.example.KGraph;

import android.content.Context;
import android.os.Environment;
import org.apache.http.util.EncodingUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by yangj on 13-12-12.
 */
public class StockDay {
    //String stockurl = "http://quotes.money.163.com/service/chddata.html?code=1300036&start=20100101&end=20131212&fields=TCLOSE;HIGH;LOW;TOPEN;LCLOSE;CHG;PCHG;TURNOVER;VOTURNOVER;VATURNOVER;TCAP;MCAP";
    public String CODE;
    public String NAME;
    public String START;
    public String END;
    public Date OPENDATE;
    public Date LASTDATE;
    public String INDUSTRY;
    public String REGION;
    public String TRANSDATE;
    public float TCLOSE;
    public float HIGH;
    public float LOW;
    public float TOPEN;
    public float LCLOSE;
    public float CHG;
    public float PCHG;
    public float TURNOVER;
    public float VOTURNOVER;
    public float VATURNOVER;
    public float TCAP;
    public float MCAP;

    /*
        从文件读取数据
     */
    public static List<StockDay> ReadFromFile(Context context) {
        List<StockDay> stocks = null;

        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            try{
                //File sdCardDir = Environment.getExternalStorageDirectory();//获取SDCard目录
File fs = new File("/storage/sdcard1/stocks/stocks.csv");
                FileInputStream inputStream = new FileInputStream(fs);// context.openFileInput(sdCardDir.getPath() + "/stocks/stocks.csv");

                byte[] bytes = new byte[1024];

                ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();

                while (inputStream.read(bytes) != -1) {
                    arrayOutputStream.write(bytes, 0, bytes.length);
                }

                inputStream.close();
                arrayOutputStream.close();

                String content = EncodingUtils.getString(arrayOutputStream.toByteArray(), "GB2312");
                String[] s = content.split("\n");
                stocks = new ArrayList<StockDay>();

                for(int i =0;i<s.length;i++){
                    String[] s2 = s[i].split(",");
                    StockDay sd = new StockDay();
                    sd.CODE = s2[0].trim();
                    sd.NAME = s2[1].trim();
                    sd.INDUSTRY = s2[2].trim();
                    sd.REGION = s2[3].trim();
                    stocks.add(sd);
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
        return stocks;
    }

    /*
        parse the data from quotes.money.163.com
    */
    public static List<StockDay> parse(String data){
        List<StockDay> stocks = null;
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
        String[] datas = data.split("\n");
        stocks = new ArrayList<StockDay>();

        for(int i=1;i<datas.length;i++){
            String[] values = datas[i].split(",");
            //TCLOSE;HIGH;LOW;TOPEN;LCLOSE;CHG;PCHG;TURNOVER;VOTURNOVER;VATURNOVER;TCAP;MCAP
            StockDay stock = new StockDay();
            try{
                stock.TRANSDATE = values[0];
                stock.CODE=values[1].replace("'","");
                stock.NAME=values[2];
                stock.TCLOSE=Float.valueOf(values[3]);
                stock.HIGH=Float.valueOf(values[4]);
                stock.LOW=Float.valueOf(values[5]);
                stock.TOPEN=Float.valueOf(values[6]);
                stock.LCLOSE=Float.valueOf(values[7]);
                stock.CHG=Float.valueOf(values[8]);
                stock.PCHG=Float.valueOf(values[9]);
                stock.TURNOVER=Float.valueOf(values[10]);
                stock.VOTURNOVER=Float.valueOf(values[11]);
                stock.VATURNOVER=Float.valueOf(values[12]);
                stock.TCAP=Float.valueOf(values[13]);
                stock.MCAP=Float.valueOf(values[14]);
                stocks.add(stock);
            }catch (Exception ex){}

            System.out.println(values[0]);
        }
        return stocks;
    }

    @Override
    public  String toString(){
        return this.NAME + ":" + this.CODE;
    }
}
