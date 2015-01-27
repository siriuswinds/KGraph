package com.example.KGraph;

import java.text.SimpleDateFormat;

/**
 * Created by yangj on 2015/1/27.
 */
public class Utils {
    public static SimpleDateFormat DateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static SimpleDateFormat DayFormatter = new SimpleDateFormat("yyyy-MM-dd");
    public static SimpleDateFormat TimeFormatter = new SimpleDateFormat("HH:mm:ss");

    public static String TRADETYPETOSTRING(TRADETYPE tradeType) {
        String result = "";

        if(tradeType != null) {
            switch (tradeType) {
                case PAYINTO:
                    result = "转入";
                    break;
                case ROLLOUT:
                    result = "转出";
                    break;
                case BUY:
                    result = "买入";
                    break;
                case SELL:
                    result = "卖出";
                    break;
            }
        }
        return result;
    }

    public static TRADETYPE STRINGTOTRADETYPE(String tradetype) {
        TRADETYPE result = null;

        if(tradetype.equals("转入")) result = TRADETYPE.PAYINTO;
        if(tradetype.equals("转出")) result = TRADETYPE.ROLLOUT;
        if(tradetype.equals("买入")) result = TRADETYPE.BUY;
        if(tradetype.equals("卖出")) result = TRADETYPE.SELL;

        return result;
    }
}
