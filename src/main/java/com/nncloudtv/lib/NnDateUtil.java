package com.nncloudtv.lib;

import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

/**
 * @source http://www.java2s.com/Code/Java/Data-Type/Checksifacalendardateistoday.htm
 */
public class NnDateUtil {
    
    protected final static Logger log = Logger.getLogger(NnDateUtil.class.getName());
    
    public static Date now() {
        
        return new Date() {
            
            private static final long serialVersionUID = -7165289423942509938L;
            
            @Override
            protected void finalize() throws Throwable {
                NnLogUtil.logFinalize(getClass().getName());
            }
        };
    }
    
    public static long timestamp() {
        
        return System.currentTimeMillis();
    }
    
    public static boolean isSameDay(Date date1, Date date2) {
        
        if (date1 == null || date2 == null) throw new IllegalArgumentException("The dates must not be null");
        
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        
        return isSameDay(cal1, cal2);
    }
    
    public static boolean isSameDay(Calendar cal1, Calendar cal2) {
        
        if (cal1 == null || cal2 == null) throw new IllegalArgumentException("The dates must not be null");
        
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA)   &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }
    
    public static boolean isToday(Date date) {
        
        return isSameDay(date, Calendar.getInstance().getTime());
    }
    
    public static Date yesterday() {
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        
        return cal.getTime();
    }
    
    public static Date tomorrow() {
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 1);
        
        return cal.getTime();
    }
    
    public static Date nextWeek() {
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 7);
        
        return cal.getTime();
    }
    
    public static Date nextMonth() {
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, 1);
        
        return cal.getTime();
    }
}
