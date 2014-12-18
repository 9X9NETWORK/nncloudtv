package com.nncloudtv.task;

public interface ScheduledTask {
    
    public static final int GC_INTERVAL  = 99149;  // Garbage Collection interval (milliseconds)
    public static final int CC_INTERVAL  = 108301; // Counter Clearing interval (milliseconds)
    public static final int TP_INTERVAL  = 167437; // Thread Pool report interval
    public static final int MQ_INTERVAL  = 193741; // Message Queue check interval
    public static final int MC_INTERVAL  = 505447; // Memcache Check interval (milliseconds)
    public static final int PM_INTERVAL  = 756839; // clean dao sharedPersistenceMngr
    
}
