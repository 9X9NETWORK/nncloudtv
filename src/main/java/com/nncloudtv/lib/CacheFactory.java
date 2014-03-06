package com.nncloudtv.lib;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.spy.memcached.BinaryConnectionFactory;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.OperationTimeoutException;
import net.spy.memcached.internal.CheckedOperationTimeoutException;

import com.nncloudtv.model.Mso;
import com.nncloudtv.service.MsoConfigManager;
import com.nncloudtv.service.PlayerApiService;

public class CacheFactory {
    
    protected static final Logger log = Logger.getLogger(CacheFactory.class.getName());
    
    public static final int EXP_DEFAULT = 2592000;
    public static final int PORT_DEFAULT = 11211;
    public static final int ASYNC_CACHE_TIMEOUT = 2000; // milliseconds
    public static final int HEALTH_CHECK_INTERVAL = 100000; // milliseconds
    public static final String ERROR = "ERROR";
    
    public static boolean isEnabled = true;
    public static boolean isRunning = true;
    private static long lastCheck = 0;
    private static List<InetSocketAddress> memcacheServers = null;
    private static MemcachedClient cache = null;
    private static MemcachedClient outdated = null;
    
    private static boolean checkServer(InetSocketAddress addr) {
        
        String key = "loop_test(" + new Date().getTime() + ")";
        log.info("key = " + key);
        boolean alive = false;
        
        MemcachedClient cache = null;
        Future<Object> future = null;
        long now = new Date().getTime();
        try {
            cache = new MemcachedClient(addr);
            cache.set(key, EXP_DEFAULT, addr);
            future = cache.asyncGet(key);
            if (future.get(ASYNC_CACHE_TIMEOUT, TimeUnit.MILLISECONDS) != null) {
                alive = true;
            }
        } catch (NullPointerException e) {
            log.warning(e.getMessage());
        } catch (InterruptedException e) {
            log.warning(e.getMessage());
        } catch (ExecutionException e) {
            log.warning(e.getMessage());
        } catch (TimeoutException e) {
            log.warning(e.getMessage());
        } catch (IOException e) {
            log.warning(e.getMessage());
        } finally {
            long delta = new Date().getTime() - now;
            log.info("it takes " + delta + " milliseconds");
            if (cache != null)
                cache.shutdown();
            if (future != null)
                future.cancel(false);
        }
        if (alive)
            log.info("memcache server " + addr + " is alive");
        return alive;
    }
    
    // needs to shutdown manually (for public use)
    public static MemcachedClient getClient() {
        
        try {
            if (isRunning && isEnabled) {
                
                return new MemcachedClient(new BinaryConnectionFactory(), memcacheServers);
            }
        } catch (IOException e) {
            log.severe("memcache io exception");
        } catch (Exception e) {
            log.severe("memcache exception");
            e.printStackTrace();
        }
        return null;
    }
    
    // don't need to shutdown manually (for speed, internally use)
    private static MemcachedClient getSharedClient() {
        
        return cache;
    }
    
    private static void reconfigClient() {
        
        // config & rebuild available server list
        System.setProperty("net.spy.log.LoggerImpl", "net.spy.memcached.compat.log.SunLogger"); 
        Logger.getLogger("net.spy.memcached").setLevel(Level.SEVERE);
        String serverStr = MsoConfigManager.getMemcacheServer();
        List<InetSocketAddress> checkedServers = new ArrayList<InetSocketAddress>();
        log.info("memcache server = " + serverStr);
        String[] serverList = serverStr.split(",");
        for (String server : serverList) {
            
            InetSocketAddress addr = new InetSocketAddress(server, PORT_DEFAULT);
            if (checkServer(addr)) {
                checkedServers.add(addr);
            }
        }
        memcacheServers = checkedServers;
        isRunning = (memcacheServers == null || memcacheServers.isEmpty()) ? false : true;
        if (!isRunning)
            log.severe("no available memcache server");
        
        // take care of current cache
        if (outdated != null)
            outdated.shutdown(ASYNC_CACHE_TIMEOUT, TimeUnit.MILLISECONDS);
        outdated = cache;
        
        // rebuild cache client
        MemcachedClient newCache = null;
        try {
            newCache = isRunning ? new MemcachedClient(new BinaryConnectionFactory(), memcacheServers) : null;
            
        } catch (IOException e) {
            log.severe("memcache io exception");
        } catch (NullPointerException e) {
            log.severe("memcache is missing");
        } catch (Exception e) {
            log.severe("memcache exception");
            e.printStackTrace();
        } finally {
            cache = newCache;
        }
    }
    
    public static Object get(String key) {
        
        if (!isEnabled || key == null || key.isEmpty()) return null;
        
        long now = new Date().getTime();
        if (now - lastCheck > HEALTH_CHECK_INTERVAL) {
            lastCheck = now;
            reconfigClient();
            log.info("memcache reconfig costs " + (new Date().getTime() - now) + " milliseconds");
            log.info("memory: max = " + Runtime.getRuntime().maxMemory()
                       + ", total = " + Runtime.getRuntime().totalMemory()
                        + ", free = " + Runtime.getRuntime().freeMemory()
                        + ", used = " + Runtime.getRuntime().freeMemory());
        } else if (!isRunning) {
            // cache is temporarily not running
            return null;
        }
        MemcachedClient cache = getSharedClient();
        if (cache == null) return null;
        
        Object obj = null;
        Future<Object> future = null;
        try {
            future = cache.asyncGet(key);
            obj = future.get(ASYNC_CACHE_TIMEOUT, TimeUnit.MILLISECONDS); // Asynchronously 
        } catch (CheckedOperationTimeoutException e) {
            log.warning("get CheckedOperationTimeoutException");
        } catch (OperationTimeoutException e) {
            log.severe("get OperationTimeoutException");
        } catch (NullPointerException e) {
            log.warning("there is no future");
        } catch (Exception e) {
            log.severe("get Exception");
            e.printStackTrace();
        } finally {
            if (future != null)
                future.cancel(false);
        }
        if (obj == null)
            log.info("cache [" + key + "] --> missed");
        return obj;
    }
    
    public static Object set(String key, Object obj) {
        
        if (!isEnabled || !isRunning || key == null || key.isEmpty()) return null;
        
        long now = new Date().getTime();
        MemcachedClient cache = getClient();
        if (cache == null) return null;
        
        Future<Object> future = null;
        Object retObj = null;
        try {
            cache.set(key, EXP_DEFAULT, obj);
            future = cache.asyncGet(key);
            retObj = future.get(ASYNC_CACHE_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (CheckedOperationTimeoutException e){
            log.warning("get CheckedOperationTimeoutException");
        } catch (OperationTimeoutException e) {
            log.severe("memcache OperationTimeoutException");
        } catch (NullPointerException e) {
            log.warning("there is no future");
        } catch (Exception e) {
            log.severe("get Exception");
            e.printStackTrace();
        } finally {
            cache.shutdown(ASYNC_CACHE_TIMEOUT, TimeUnit.MILLISECONDS);
            if (future != null)
                future.cancel(false);
        }
        log.info("save operation costs " + (new Date().getTime() - now) + " milliseconds");
        if (retObj == null)
            log.info("cache [" + key + "] --> not saved");
        else
            log.info("cache [" + key + "] --> saved");
        return retObj;
    }    
    
    public static void delete(String key) {
        
        if (!isEnabled || !isRunning || key == null || key.isEmpty()) return;
        
        boolean isDeleted = false;
        long now = new Date().getTime();
        MemcachedClient cache = getClient();
        if (cache == null) return;
        
        try {
            cache.delete(key).get(ASYNC_CACHE_TIMEOUT, TimeUnit.MILLISECONDS);
            isDeleted = true;
        } catch (CheckedOperationTimeoutException e){
            log.warning("get CheckedOperationTimeoutException");
        } catch (OperationTimeoutException e) {
            log.severe("memcache OperationTimeoutException");
        } catch (NullPointerException e) {
            log.warning("there is no future");
        } catch (Exception e) {
            log.severe("get Exception");
            e.printStackTrace();
        } finally {
            cache.shutdown(ASYNC_CACHE_TIMEOUT, TimeUnit.MILLISECONDS);
        }
        log.info("delete operation costs " + (new Date().getTime() - now) + " milliseconds");
        if (isDeleted) {
            log.info("cache [" + key + "] --> deleted");
        } else {
            log.info("cache [" + key + "] --> not deleted");
        }
    }
            
	//example: brandInfo(9x9)[json]
    public static String getBrandInfoKey(Mso mso, String os, short format) {
    	if (format == PlayerApiService.FORMAT_PLAIN)
    		return "brandInfo(" + mso.getName() + ")(" + os + ")";
    	return "brandInfo(" + mso.getName() + ")(" + os + ")" + "[json]";
    }
    
    /**
     * key looks like nnprogram-v version number-channelId-pagination-json
     * pagination has value 0, 50, 100, 150
     * examples: nnprogram-v31-1-0-text
     *           nnprogram-v40-2-50-json
     */
    public static String getProgramInfoKey(long channelId, int start, int version, short format) {
    	if (version == 31) {
    		return "nnprogram-v31-" + channelId + "-0-" + "text";
    	} else if (version == 32) {
            return "nnprogram-v32-" + channelId + "-0-" + "text";
    	}
        String str = "nnprogram-v40-" + channelId + "-" + start + "-"; 
        if (format == PlayerApiService.FORMAT_JSON) {
        	str += "json";
        } else {
        	str += "text";
        }
        log.info("programInfo cache key:" + str);
        return str;
    }
        
    /**
     * cache the 1st program of channel
     * format: nnprogramLatest-channel_id-format
     * example: nnprogramLatest-1-json
     *          nnprogramLatest-1-text
     */
    public static String getLatestProgramInfoKey(long channelId, short format) {
        String str = "nnprogramLatest-" + channelId + "-";
        if (format == PlayerApiService.FORMAT_JSON) {
        	str += "json";
        } else {
        	str += "text";
        }
        return str;
    }
        
    /**
     * format: nnchannel-v version_number-channel_id-format
     * example: nnchannel-v31-1-text
     *          nnchannel-v40-2-json 
     */
    public static String getChannelLineupKey(String channelId, int version, short format) {
    	String key = "";
    	if (version == 32) {
    		//nnchannel-v32(1)
    		key = "nnchannel-v32-" + channelId;
    	} else if (version < 32) {
    		//nnchannel-v31(1)
        	key = "nnchannel-v31-" + channelId;
    	} else {			
    		//nnchannel(1)
            key = "nnchannel-v40-" + channelId;
    	}
        if (format == PlayerApiService.FORMAT_JSON) {
        	key += "-json";
        } else {
        	key += "-text";
        }
        log.info("channelLineup key:" + key);
        return key;
    }
        
}
