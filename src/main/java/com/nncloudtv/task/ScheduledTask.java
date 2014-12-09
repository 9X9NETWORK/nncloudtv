package com.nncloudtv.task;

public interface ScheduledTask {
    
    public static final int MC_INTERVAL = 505447; // Memcache Check interval (milliseconds)
    public static final int GC_INTERVAL = 294001; // Garbage Collection interval (milliseconds)
    public static final int CC_INTERVAL = 108301; // Counter Clearing interval (milliseconds)
    public static final int DAO_INTERVAL = 30000; // reset shared PersistenceManager 604171
    
}
