package com.nncloudtv.lib;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.spy.memcached.BinaryConnectionFactory;
import net.spy.memcached.MemcachedClient;

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
    
    public static boolean isEnabled = true;
    public static boolean isRunning = false;
    private static List<InetSocketAddress> memcacheServers = null;
    private static MemcachedClient cache = null;
    private static MemcachedClient outdated = null;
    
    private static boolean checkServer(InetSocketAddress addr) {
        
        String key = String.format("loop_test(%d)", NnDateUtil.timestamp());
        boolean alive = false;
        
        MemcachedClient cache = null;
        Future<Object> future = null;
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
        } catch (Exception e) {
            log.warning(e.getClass().getName());
            log.warning(e.getMessage());
        } finally {
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
    private static MemcachedClient getClient() {
        try {
            if (isRunning && isEnabled)
                return new MemcachedClient(new BinaryConnectionFactory(), memcacheServers);
        } catch (IOException e) {
            log.severe(e.getMessage());
        }
        return null;
    }
    
    public static void reconfigClient() {
        
        // config & rebuild available server list
        System.setProperty("net.spy.log.LoggerImpl", "net.spy.memcached.compat.log.SunLogger"); 
        Logger.getLogger("net.spy.memcached").setLevel(Level.SEVERE);
        String serverStr = MsoConfigManager.getMemcacheServer();
        List<InetSocketAddress> checkedServers = new ArrayList<InetSocketAddress>();
        System.out.println("[cache] server = " + serverStr);
        String[] serverList = serverStr.split(",");
        for (String server : serverList) {
            InetSocketAddress addr = new InetSocketAddress(server, PORT_DEFAULT);
            if (checkServer(addr))
                checkedServers.add(addr);
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
            log.severe(e.getClass().getName());
            log.severe(e.getMessage());
            e.printStackTrace();
        } finally {
            cache = newCache;
        }
    }
    
    public static Object get(String key) {
        
        if (!isEnabled || !isRunning || key == null || key.isEmpty()) return null;
        
        if (cache == null) return null;
        
        Object obj = null;
        Future<Object> future = null;
        try {
            future = cache.asyncGet(key);
            obj = future.get(ASYNC_CACHE_TIMEOUT, TimeUnit.MILLISECONDS); // Asynchronously 
        } catch (NullPointerException e) {
            log.warning(e.getClass().getName());
            log.warning("there is no future");
        } catch (Exception e) {
            log.warning(e.getClass().getName());
            log.warning(e.getMessage());
        } finally {
            if (future != null)
                future.cancel(false);
        }
        if (obj == null) {
            System.out.println(String.format("[cache] {%s} --> missed", key));
        }
        return obj;
    }
    
    public static Object set(String key, Object obj) {
        
        return set(key, obj, 0);
    }
    
    public static Object set(String key, Object obj, int exp) {
        
        if (!isEnabled || !isRunning || key == null || key.isEmpty()) return null;
        
        MemcachedClient cache = getClient();
        if (cache == null) return null;
        
        Future<Object> future = null;
        Object retObj = null;
        try {
            cache.set(key, exp == 0 ? EXP_DEFAULT : exp, obj);
            future = cache.asyncGet(key);
            retObj = future.get(ASYNC_CACHE_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (NullPointerException e) {
            log.warning(e.getClass().getName());
            log.warning("there is no future");
        } catch (Exception e) {
            log.warning(e.getClass().getName());
            log.warning(e.getMessage());
        } finally {
            cache.shutdown(ASYNC_CACHE_TIMEOUT, TimeUnit.MILLISECONDS);
            if (future != null)
                future.cancel(false);
        }
        if (retObj == null) {
            System.out.println(String.format("[cache] {%s} --> NOT saved", key));
        } else {
            System.out.println(String.format("[cache] {%s} --> saved", key));
        }
        
        return retObj;
    }    
    
    public static void deleteAll(List<String> keys) {
        
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
        } catch (NullPointerException e) {
            log.warning(e.getClass().getName());
            log.warning("there is no future");
        } catch (Exception e) {
            log.warning(e.getClass().getName());
            log.warning(e.getMessage());
        } finally {
            cache.shutdown(ASYNC_CACHE_TIMEOUT, TimeUnit.MILLISECONDS);
        }
        if (isDeleted) {
            System.out.println(String.format("[cache] mass: %d --> deleted", keys.size()));
        } else {
            System.out.println(String.format("[cache] mass: %d --> NOT deleted", keys.size()));
        }
        System.out.println(String.format("[cache] deleteAll() operation costs %d milliseconds", NnDateUtil.timestamp() - before));
    }
    
    public static void delete(String key) {
        
        if (!isEnabled || !isRunning || key == null || key.isEmpty()) return;
        
        boolean isDeleted = false;
        MemcachedClient cache = getClient();
        if (cache == null) return;
        
        try {
            cache.delete(key).get(ASYNC_CACHE_TIMEOUT, TimeUnit.MILLISECONDS);
            isDeleted = true;
        } catch (NullPointerException e) {
            log.warning(e.getClass().getName());
            log.warning("there is no future");
        } catch (Exception e) {
            log.warning(e.getClass().getName());
            log.warning(e.getMessage());
        } finally {
            cache.shutdown(ASYNC_CACHE_TIMEOUT, TimeUnit.MILLISECONDS);
        }
        if (isDeleted) {
            System.out.println(String.format("[cache] {%s} --> deleted", key));
        } else {
            System.out.println(String.format("[cache] {%s} --> NOT deleted", key));
        }
    }
    
    public static void flush() {
        
        if (!isEnabled || !isRunning) return;
        
        long before = NnDateUtil.timestamp();
        MemcachedClient cache = getClient();
        if (cache == null) return;
        
        try {
            cache.flush().get(ASYNC_CACHE_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.warning(e.getClass().getName());
            log.warning(e.getMessage());
        } finally {
            cache.shutdown(ASYNC_CACHE_TIMEOUT, TimeUnit.MILLISECONDS);
        }
        
        System.out.println(String.format("[cache] flush operation costs %d milliseconds", NnDateUtil.timestamp() - before));
    }
    
    //////////////////////////////////////////////
    //////// start of cache key functions ////////
    //////////////////////////////////////////////
    
    public static String getSystemCategoryKey(long channelId) {
        
        return String.format("systemCategory(%d)", channelId);
    }
    
    public static String getNnChannelMoreImageUrlKey(long channelId) {
        
        return String.format("NnChannel.getMoreImageUrl(%d)", channelId);
    }
    
    public static String getNnChannelPrefKey(long channelId, String item) {
        
        return String.format("nnchannelpref(%d)(%s)", channelId, item);
    }
    
    public static String getMsoConfigKey(long msoId, String item) {
        
        return String.format("msoconfig(%d)(%s)", msoId, item);
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
        log.fine("brandInfoKey:" + key);
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
        log.fine("programInfo cache key:" + str);
        return str;
    }
    
    public static List<String> getAllprogramInfoKeys(long channelId, short format) {
        
        List<String> keys = new ArrayList<String>();
        
        log.fine("get all programInfo keys from ch" + channelId + " in format " + format);
        
        for (int i = 0; i < PlayerApiService.MAX_EPISODES; i += PlayerApiService.PAGING_ROWS) {
            
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
        
        log.fine("[cache] channelLineup key = " + key);
        
        return key;
    }
    
    /**
     * format: daypartChannel-msoId-lang-time
     * example: daypartChannel-1-en-2
     */
    public static String getDayPartingChannelKey(long msoId, short time, String lang) {
    	String key = "daypartChannel" + msoId + "-" + lang + "-" + time;
    	log.fine("daypartChannel cache key:" + key);
    	return key;
    }
    
    public static String getDaypartingProgramsKey(long msoId, short time, String lang) {
    	String key = "daypartProgram" + msoId + "-" + lang + "-" + time;
    	log.fine("daypartProgram cache key:" + key);
    	return key;    	
    }
    
    public static String getYtProgramInfoKey(long channelId) {
        String key = "ytprogram-" + channelId; 
        log.fine("ytprogram key:" + key);
        return key;
    }
    
    public static String getAdInfoKey(Mso mso, short format) {
        String key = "";
        if (format == ApiContext.FORMAT_PLAIN) {
            key = "adInfo(" + mso.getName() + ")";
        } else {
            key = "adInfo(" + mso.getName() + ")[json]";
        }
        log.fine("adInfoKey:" + key);
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
    
    public static String getFindByIdKey(String className, long id) {
        
        return String.format("%s.findById(%d)", className, id);
    }
    
    public static String getChannelCntItemKey(long channelId) {
        
        return String.format("NnChannel.getCntItem(%d)", channelId);
    }
}
