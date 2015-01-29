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

    /**
     * 获取交易记录
     * @return
     */
    public List<TradeRecord> getTradeRecords() {
        List<TradeRecord> records = dbmgr.getTradeRecords();
        return records;
    }

    /**
     * 获取当前持股信息
     * @return
     */
    public List<TradeRecord> getHoldStocks() {
        List<TradeRecord> stocks = dbmgr.getHoldStocks();
        return stocks;
    }
}
