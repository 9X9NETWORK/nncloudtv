package com.nncloudtv.lib;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
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
import com.nncloudtv.web.api.ApiContext;

public class CacheFactory {
    
    protected static final Logger log = Logger.getLogger(CacheFactory.class.getName());
    
    public static final int EXP_DEFAULT = 2592000; // 30 days
    public static final int EXP_SHORT = 100;  // seconds
    public static final int PORT_DEFAULT = 11211;
    public static final int ASYNC_CACHE_TIMEOUT = 2000; // milliseconds
    public static final int MINIMUM_LOG_INTERVAL = 10;
    public static final String ERROR = "ERROR";
    
    public static boolean isEnabled = true;
    public static boolean isRunning = false;
    private static long lastLogTime = 0;
    private static List<InetSocketAddress> memcacheServers = null;
    private static MemcachedClient cache = null;
    private static MemcachedClient outdated = null;
    
    private static boolean checkServer(InetSocketAddress addr) {
        
        String key = String.format("loop_test(%d)", NnDateUtil.timestamp());
        System.out.println("[memcache] key = " + key);
        boolean alive = false;
        
        MemcachedClient cache = null;
        Future<Object> future = null;
        long before = NnDateUtil.timestamp();
        try {
            cache = new MemcachedClient(addr) {
                
                @Override
                protected void finalize() throws Throwable {
                    
                    NnLogUtil.logFinalize(getClass().getName());
                }
            };
            cache.set(key, EXP_SHORT, addr);
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
            long delta = NnDateUtil.timestamp() - before;
            System.out.println("[memcache] it takes " + delta + " milliseconds");
            if (cache != null)
                cache.shutdown();
            if (future != null)
                future.cancel(false);
        }
        if (!alive)
            log.warning("memcache server " + addr + " is dead!");
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
    
    public static void reconfigClient() {
        
        // config & rebuild available server list
        System.setProperty("net.spy.log.LoggerImpl", "net.spy.memcached.compat.log.SunLogger"); 
        Logger.getLogger("net.spy.memcached").setLevel(Level.SEVERE);
        String serverStr = MsoConfigManager.getMemcacheServer();
        List<InetSocketAddress> checkedServers = new ArrayList<InetSocketAddress>();
        System.out.println("[memcache] server = " + serverStr);
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
            log.severe("No available memcache server!");
        
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
            log.severe(e.getMessage());
        } catch (NullPointerException e) {
            log.severe("memcache is missing");
            log.severe(e.getMessage());
        } catch (Exception e) {
            log.severe("memcache exception");
            log.severe(e.getMessage());
            e.printStackTrace();
        } finally {
            cache = newCache;
        }
    }
    
    public static Object get(String key) {
        
        if (!isEnabled || !isRunning || key == null || key.isEmpty()) return null;
        
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
            System.out.println(String.format("[memcache] %s --> missed", key));
        return obj;
    }
    
    public static Object set(String key, Object obj) {
        
        return set(key, obj, 0);
    }
    
    public static Object set(String key, Object obj, int exp) {
        
        
        if (!isEnabled || !isRunning || key == null || key.isEmpty()) return null;
        
        long before = NnDateUtil.timestamp();
        MemcachedClient cache = getClient();
        if (cache == null) return null;
        
        Future<Object> future = null;
        Object retObj = null;
        try {
            cache.set(key, exp == 0 ? EXP_DEFAULT : exp, obj);
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
        log.info(String.format("save operation costs %d milliseconds", NnDateUtil.timestamp() - before));
        if (retObj == null)
            System.out.println(String.format("[memcache] %s --> NOT saved", key));
        else
            System.out.println(String.format("[memcache] %s --> saved", key));
        return retObj;
    }    
    
    public static void delete(List<String> keys) {
        
        if (!isEnabled || !isRunning || keys == null || keys.isEmpty()) return;
        
        boolean isDeleted = false;
        long before = NnDateUtil.timestamp();
        MemcachedClient cache = getClient();
        if (cache == null) return;
        
        try {
            for (String key : keys) {
                if (key != null && !key.isEmpty()) {
                    cache.delete(key).get(ASYNC_CACHE_TIMEOUT, TimeUnit.MILLISECONDS);
                }
            }
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
        log.info(String.format("delete operation costs %d milliseconds", NnDateUtil.timestamp() - before));
        if (isDeleted) {
            System.out.println(String.format("[memcache] mass: %d --> deleted", keys.size()));
        } else {
            System.out.println(String.format("[memcache] mass: %d --> NOT deleted", keys.size()));
        }
    }
    
    public static void delete(String key) {
        
        if (!isEnabled || !isRunning || key == null || key.isEmpty()) return;
        
        boolean isDeleted = false;
        long before = NnDateUtil.timestamp();
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
        log.info(String.format("delete operation costs %d milliseconds", NnDateUtil.timestamp() - before));
        if (isDeleted) {
            System.out.println(String.format("[memcache] %s --> deleted", key));
        } else {
            System.out.println(String.format("[memcache] %s --> NOT deleted", key));
        }
    }
    
    public static String getMaoConfigKey(long msoId, String key) {
        
        return String.format("msoconfig(%d)(%s)", msoId, key);
    }
    
    // example: mso(9x9)
    public static String getMsoObjectKey(String name) {
        
        return String.format("mso(%s)", name);
    }
    
    // example: brandInfo(9x9)[json]
    public static String getBrandInfoKey(Mso mso, String os, short format) {
    	String key = "";
    	if (format == ApiContext.FORMAT_PLAIN) {
    		key = "brandInfo(" + mso.getName() + ")(" + os + ")";
    	} else {
    		key = "brandInfo(" + mso.getName() + ")(" + os + ")" + "[json]";
    	}
    	log.info("brandInfoKey:" + key);
    	return key;
    }
    
    /**
     * key looks like nnprogram-v version number-channelId-pagination-json
     * pagination has value 0, 50, 100, 150
     * examples: nnprogram-v31-1-0-text
     *           nnprogram-v40-2-50-json
     */
    public static String getProgramInfoKey(long channelId, int start, int version, short format) {
        if (version <= 32) {
            return "nnprogram-" + version + "-" + channelId + "-0-" + "text";
        }
        String str = "nnprogram-v40-" + channelId + "-" + start + "-";
        if (format == ApiContext.FORMAT_JSON) {
            str += "json";
        } else {
            str += "text";
        }
        log.info("programInfo cache key:" + str);
        return str;
    }
    
    public static List<String> getAllprogramInfoKeys(long channelId, short format) {
        
        List<String> keys = new ArrayList<String>();
        
        log.info("get all programInfo keys from ch" + channelId + " in format " + format);
        
        for (int i = 0; i < PlayerApiService.MAX_EPISODES; i++) {
            
            String str = "nnprogram-v40-" + channelId + "-" + i + "-" + ((format == ApiContext.FORMAT_JSON) ? "json" : "text"); 
            
            keys.add(str);
        }
        
        return keys;
    }
    
    
    /**
     * cache the 1st program of channel
     * format: nnprogramLatest-channel_id-format
     * example: nnprogramLatest-1-json
     *          nnprogramLatest-1-text
     */
    public static String getLatestProgramInfoKey(long channelId, short format) {
        String str = "nnprogramLatest-" + channelId + "-";
        if (format == ApiContext.FORMAT_JSON) {
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
        if (format == ApiContext.FORMAT_JSON) {
        	key += "-json";
        } else {
        	key += "-text";
        }
        
        // cool log down
        if (NnDateUtil.timestamp() - lastLogTime > MINIMUM_LOG_INTERVAL) {
            log.info("channelLineup key = " + key);
            lastLogTime = NnDateUtil.timestamp();
        }
        
        return key;
    }
    
    /**
     * format: daypartChannel-msoId-lang-time
     * example: daypartChannel-1-en-2
     */
    public static String getDayPartingChannelKey(long msoId, short time, String lang) {
    	String key = "daypartChannel" + msoId + "-" + lang + "-" + time;
    	log.info("daypartChannel cache key:" + key);
    	return key;
    }
    
    public static String getDaypartingProgramsKey(long msoId, short time, String lang) {
    	String key = "daypartProgram" + msoId + "-" + lang + "-" + time;
    	log.info("daypartProgram cache key:" + key);
    	return key;    	
    }
    
    public static String getYtProgramInfoKey(long channelId) {
        String key = "ytprogram-" + channelId; 
        log.info("ytprogram key:" + key);
        return key;
    }
    
    public static String getAdInfoKey(Mso mso, short format) {
        String key = "";
        if (format == ApiContext.FORMAT_PLAIN) {
            key = "adInfo(" + mso.getName() + ")";
        } else {
            key = "adInfo(" + mso.getName() + ")[json]";
        }
        log.info("adInfoKey:" + key);
        return key;
    }
    
    public static List<String> getAllChannelInfoKeys(long channelId) {
        
        List<String> keys = new ArrayList<String>();
        
        String cId = String.valueOf(channelId);
        keys.add(CacheFactory.getChannelLineupKey(cId, 31, ApiContext.FORMAT_PLAIN));
        keys.add(CacheFactory.getChannelLineupKey(cId, 32, ApiContext.FORMAT_PLAIN));
        keys.add(CacheFactory.getChannelLineupKey(cId, 40, ApiContext.FORMAT_JSON));
        keys.add(CacheFactory.getChannelLineupKey(cId, 40, ApiContext.FORMAT_PLAIN));
        
        return keys;
    }
    
}
