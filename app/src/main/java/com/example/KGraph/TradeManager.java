package com.example.KGraph;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by yangj on 2015/1/27.
 */
public class TradeManager {
    private DBManager dbmgr = null;
    SimpleDateFormat m_fmt = null;

    public TradeManager(DBManager db){
        dbmgr = db;
        m_fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 转入
     * @param fPayNum
     */
    public void PayInto(float fPayNum) {
        TradeRecord record = new TradeRecord();
        Calendar c = Calendar.getInstance();
        Date date = c.getTime();
        record.TradeTime = date;
        record.TurnOver = fPayNum;
        record.TradeType = TRADETYPE.PAYINTO;
        dbmgr.saveTradeRecord(record);
    }

    /**
     * 转出
     * @param fRollOut
     */
    public void RollOut(float fRollOut) {
        TradeRecord record = new TradeRecord();
        Calendar c = Calendar.getInstance();
        Date date = c.getTime();
        record.TradeTime = date;
        record.TurnOver = fRollOut;
        record.TradeType = TRADETYPE.ROLLOUT;
        dbmgr.saveTradeRecord(record);
    }

    public List<TradeRecord> getTradeRecords() {
        List<TradeRecord> records = dbmgr.getTradeRecords();
        return records;
    }
}
