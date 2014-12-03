package com.nncloudtv.web;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.nncloudtv.lib.AmazonLib;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnDateUtil;
import com.nncloudtv.lib.NnLogUtil;
import com.nncloudtv.lib.NnNetUtil;
import com.nncloudtv.model.Counter;
import com.nncloudtv.model.MsoPromotion;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.NnEpisode;
import com.nncloudtv.model.SysTagDisplay;
import com.nncloudtv.service.DepotService;
import com.nncloudtv.service.MsoConfigManager;
import com.nncloudtv.service.NnChannelManager;
import com.nncloudtv.service.NnStatusMsg;
import com.nncloudtv.service.PlayerService;
import com.nncloudtv.web.api.ApiGeneric;
import com.nncloudtv.web.api.NnStatusCode;
import com.nncloudtv.web.json.transcodingservice.Channel;
import com.nncloudtv.web.json.transcodingservice.ChannelInfo;
import com.nncloudtv.web.json.transcodingservice.PostResponse;
import com.nncloudtv.web.json.transcodingservice.RtnChannel;
import com.nncloudtv.web.json.transcodingservice.RtnProgram;


/**
 * <p>Serves for Transcoding Service.</p>
 * <p>Url examples: (method name is used at the end of URL) <br/> 
 * http://hostname:port/podcastAPI/itemUpdate<br/>
 * http://hostname:port/podcastAPI/channelUpdate<br/>
 * <p/>
 * <p>Flow: <br/>
 * (1) nnsmvo notify transcoding service a new podcast channel <br/>
 * (2) transcoding service returns channel metadata via channelUpdate <br/>
 * (3) transcoding service returns program metadata via itemUpdate. (the episode is ready with MP4 format and basic metadata)<br/>
 * (4) transcoding service returns additional program metadata via itemUpdate. (webm is supported) <br/> 
 * </p>
 */
@Controller
@RequestMapping("podcastAPI")
public class DepotController {
    
    protected static final Logger log = Logger.getLogger(DepotController.class.getName());
    
    @ExceptionHandler(Exception.class)
    public @ResponseBody PostResponse exception(Exception e, HttpServletRequest req, HttpServletResponse resp) {
        if (e.getClass().equals(HttpMediaTypeNotSupportedException.class)) {
            log.info("HttpMediaTypeNotSupportedException");
        }
        NnLogUtil.logException(e);
        PostResponse post = new PostResponse();
        post.setErrorCode(String.valueOf(NnStatusCode.ERROR));
        post.setErrorReason("error");
        resp.setStatus(200);
        return post;
    }        
    
    /**
     * Transcoding Service update Podcast Program information
     * 
     * @param podcastProgram podcastProgram returns in Json format <br/>
     * {<br/>
     * "action":"updateItem",<br/>
     * "key":"channel_key_id",<br/>
     * "errorCode":0, <br/>
     * "errorReason":"error description", <br/>        
     * "item": [ <br/>        
     *   {<br/>
     *     "title":"title", <br/>
     *     "description":"description", <br/>
     *     "pubDate","pubDate", <br/>
     *     "enclosure":"video_url",    <br/>    
     *     "type":"mp4",<br/>
     *   }<br/>
     *} 
     * 
     * @return keys keys include channel key and item key <br/>
     *  {<br/>
      *     "key":"channel_key_id",<br/>
      *      "itemkey":"item_key_id",<br/>
     *  } 
     */
    @RequestMapping("itemUpdate")
    public ResponseEntity<String> itemUpdate(@RequestBody RtnProgram rtnProgram, HttpServletRequest req) {
        log.info(rtnProgram.toString());
        PostResponse resp = new PostResponse(
                String.valueOf(NnStatusCode.ERROR), NnStatusMsg.getPlayerMsg(NnStatusCode.ERROR));        
        try {
            resp = NNF.getDepotService().updateProgram(rtnProgram);
        } catch (Exception e) {
            resp = NNF.getDepotService().handleException(e);
        }
        log.info(resp.getErrorCode());
        return NnNetUtil.textReturn("OK");
    }
    
    /**
     * Get Channel/Set Meta data (title,description,image)
     * 
     * This is used by Piwik to fetch channel/set info
     * 
     * @return json encoded key value pair
     */
    
    @RequestMapping("getMetaInfo")
    public @ResponseBody void getMetaInfo(Model model,
                                          HttpServletResponse resp,
                                          HttpServletRequest req,
                                          @RequestParam(required=false) String jsoncallback,
                                          @RequestParam(required=false) Long set,
                                          @RequestParam(required=false) Long ch) {
        
        PlayerService playerService = new PlayerService();
        
        ObjectMapper mapper = new ObjectMapper();
        
        if (set != null) {
            model.addAttribute("type", "set");
            model.addAttribute("id", String.valueOf(set));
        } else if (ch != null) {
            model = playerService.prepareChannel(model, String.valueOf(ch), null, resp);
            model.addAttribute("type", "ch");
            model.addAttribute("id", String.valueOf(ch));
        }
        
        try {
            OutputStream out = resp.getOutputStream();
            resp.setCharacterEncoding("UTF-8");
            resp.addDateHeader("Expires", System.currentTimeMillis() + 3600000);
            resp.addHeader("Cache-Control", "private, max-age=3600");
            if (jsoncallback == null) {
                resp.setContentType("application/json");
                mapper.writeValue(out, model.asMap());
            } else {
                resp.setContentType("application/x-javascript");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                OutputStreamWriter writer = new OutputStreamWriter(baos, "UTF-8");
                writer.write(jsoncallback + "(");
                writer.close();
                baos.writeTo(out);
                
                baos = new ByteArrayOutputStream();
                writer = new OutputStreamWriter(baos, "UTF-8");
                log.info("encoding: " + writer.getEncoding());
                mapper.writeValue(writer, model.asMap());
                baos.writeTo(out);
                out.write(')');
                out.close();
            }
        } catch (Exception e) {
            NnLogUtil.logException(e);
        }
    }
    
    @RequestMapping("processThumbnail")
    public @ResponseBody String processThumbnail(HttpServletRequest req) {
        
        String result = "NOTHING";
        String episodeIdStr = req.getParameter("episode");
        String channelIdStr = req.getParameter("channel");
        String setIdStr = req.getParameter("set");
        String promotionIdStr = req.getParameter("promotion");
        if (episodeIdStr != null) {
            
            result = "EPISODE_NOT_OK";
            NnEpisode episode = NNF.getEpisodeMngr().findById(episodeIdStr);
            if (episode == null) {
                return "episode not found!";
            }
            log.info("resize episode thumbnail - " + episodeIdStr);
            
            String imageUrl = episode.getImageUrl();
            if (imageUrl == null) {
                return "episode has no imageUrl";
            }
            
            String resizedImageUrl = null;
            try {
                BufferedImage image = NNF.getDepotService()
                                         .resizeImage(imageUrl, NnEpisode.DEFAULT_WIDTH, NnEpisode.DEFAULT_HEIGHT);
                if (image != null) {
                    resizedImageUrl = AmazonLib.s3Upload(MsoConfigManager.getS3DepotBucket(),
                            "thumb-ep" + episode.getId() + ".png", image);
                    if (resizedImageUrl != null) {
                        
                        log.info("update episode with new imageUrl - " + resizedImageUrl);
                        episode.setImageUrl(resizedImageUrl);
                        NNF.getEpisodeMngr().save(episode);
                        result = "OK";
                    }
                }
                
            } catch (AmazonServiceException e) {
                
                log.warning("amazon service exception - " + e.getMessage());
                return "AmazonServiceException";
                
            } catch (AmazonClientException e) {
                
                log.warning("amazon client exception - " + e.getMessage());
                return "AmazonClientException";
                
            } catch (MalformedURLException e) {
                
                log.warning("episode image url is malformed - " + episode.getId());
                return "MalformedURLException";
                
            } catch (IOException e) {
                
                log.warning("failed to load image - " + episode.getImageUrl());
                return "IOException";
            }
            
        } else if (channelIdStr != null) {
            
            result = "CHANNEL_NOT_OK";
            NnChannel channel = NNF.getChannelMngr().findById(channelIdStr);
            if (channel == null) {
                return "channel not found!";
            }
            log.info("resize channel thumbnail - " + channelIdStr);
            
            String imageUrl = channel.getImageUrl();
            if (imageUrl == null) {
                return "channel has no imageUrl";
            }
            
            String resizedImageUrl = null;
            try {
                BufferedImage image = NNF.getDepotService()
                                         .resizeImage(imageUrl, NnChannel.DEFAULT_WIDTH, NnChannel.DEFAULT_HEIGHT);
                if (image != null) {
                    resizedImageUrl = AmazonLib.s3Upload(MsoConfigManager.getS3DepotBucket(),
                            "thumb-ch" + channel.getId() + ".png", image);
                    if (resizedImageUrl != null) {
                        
                        log.info("update channel with new imageUrl - " + resizedImageUrl);
                        channel.setImageUrl(resizedImageUrl);
                        NNF.getChannelMngr().save(channel);
                        result = "OK";
                    }
                }
                
            } catch (AmazonServiceException e) {
                
                log.warning("amazon service exception - " + e.getMessage());
                return "AmazonServiceException";
                
            } catch (AmazonClientException e) {
                
                log.warning("amazon client exception - " + e.getMessage());
                return "AmazonClientException";
                
            } catch (MalformedURLException e) {
                
                log.warning("channel image url is malformed - " + channel.getId());
                return "MalformedURLException";
                
            } catch (IOException e) {
                
                log.warning("failed to load image - " + channel.getImageUrl());
                return "IOException";
            }
            
        } else if (setIdStr != null) {
            
            long sysTagId = Long.parseLong(setIdStr);
            SysTagDisplay display = NNF.getDisplayMngr().findBySysTagId(sysTagId);
            if (display == null) {
                return "SysTagDisplay not found";
            }
            boolean dirty = false;
            
            try {
                // branner
                String bannerUrl = display.getBannerImageUrl();
                String resizedBannerUrl = null;
                if (bannerUrl != null) {
                    
                    log.info("the origin banner (android) url = " + bannerUrl);
                    BufferedImage image = NNF.getDepotService()
                                             .resizeImage(bannerUrl, SysTagDisplay.DEFAULT_WIDTH, SysTagDisplay.DEFAULT_HEIGHT);
                    if (image != null) {
                        resizedBannerUrl = AmazonLib.s3Upload(MsoConfigManager.getS3DepotBucket(),
                                "banner-set" + display.getSystagId() + ".png", image);
                        if (resizedBannerUrl != null) {
                            log.info("update set with new (android) banner url = " + resizedBannerUrl);
                            display.setBannerImageUrl(resizedBannerUrl + "?_=" + NnDateUtil.now().getTime());
                            dirty = true;
                        }
                    }
                }
                
                // banner (retina)
                bannerUrl = display.getBannerImageUrl2();
                resizedBannerUrl = null;
                if (bannerUrl != null) {
                    
                    log.info("the origin banner (ios) url = " + bannerUrl);
                    BufferedImage image = NNF.getDepotService()
                                             .resizeImage(bannerUrl, SysTagDisplay.RETINA_WIDTH, SysTagDisplay.RETINA_HEIGHT);
                    if (image != null) {
                        resizedBannerUrl = AmazonLib.s3Upload(MsoConfigManager.getS3DepotBucket(),
                                "banner-set" + display.getSystagId() + "-retina.png", image);
                        if (resizedBannerUrl != null) {
                            log.info("update set with new (ios) banner url = " + resizedBannerUrl);
                            display.setBannerImageUrl2(resizedBannerUrl + "?_=" + NnDateUtil.now().getTime());
                            dirty = true;
                        }
                    }
                }
            } catch (AmazonServiceException e) {
                
                log.warning("amazon service exception - " + e.getMessage());
                return "AmazonServiceException";
                
            } catch (AmazonClientException e) {
                
                log.warning("amazon client exception - " + e.getMessage());
                return "AmazonClientException";
                
            } catch (MalformedURLException e) {
                
                log.warning("channel banner url is malformed - " + display.getId());
                return "MalformedURLException";
                
            } catch (IOException e) {
                
                log.warning("failed to load image - " + display.getBannerImageUrl2());
                return "IOException";
            }
            
            if (dirty) {
                NNF.getDisplayMngr().save(display);
                result = "OK";
            }
        } else if (promotionIdStr != null) {
            
            result = "NOT_OK";
            MsoPromotion promotion = NNF.getMsoPromotionMngr().findById(promotionIdStr);
            if (promotion == null) {
                return "promotion not found!";
            }
            log.info("resize promotion logo - " + promotionIdStr);
            
            String imageUrl = promotion.getLogoUrl();
            if (imageUrl == null) {
                return "promotion has no logo";
            }
            
            String resizedImageUrl = null;
            try {
                BufferedImage image = NNF.getDepotService()
                                         .resizeImage(imageUrl, MsoPromotion.DEFAULT_WIDTH, MsoPromotion.DEFAULT_HEIGHT);
                if (image != null) {
                    resizedImageUrl = AmazonLib.s3Upload(MsoConfigManager.getS3DepotBucket(),
                            "logo-promot" + promotion.getId() + ".png", image);
                    if (resizedImageUrl != null) {
                        
                        log.info("update promotion with new logo - " + resizedImageUrl);
                        promotion.setLogoUrl(resizedImageUrl);
                        NNF.getMsoPromotionMngr().save(promotion);
                        result = "OK";
                    }
                }
                
            } catch (AmazonServiceException e) {
                
                log.warning("amazon service exception - " + e.getMessage());
                return "AmazonServiceException";
                
            } catch (AmazonClientException e) {
                
                log.warning("amazon client exception - " + e.getMessage());
                return "AmazonClientException";
                
            } catch (MalformedURLException e) {
                
                log.warning("promotion logo url is malformed - " + promotion.getId());
                return "MalformedURLException";
                
            } catch (IOException e) {
                
                log.warning("failed to load logo - " + promotion.getLogoUrl());
                return "IOException";
            }
        }
        return result;
    }
    
    /** 
     * @param page
     * @param msoName * indicates to retrieve all the channels
     *                msoName indicates to retrieve a mso's Ipg
     * @return channel list
     */
    @RequestMapping("getChannelList")
    public @ResponseBody ChannelInfo getChannelList(
              @RequestParam(value="page", required=false)String page, 
                                                @RequestParam(value="msoName", required=false)String msoName,
                                                @RequestParam(value="type", required=false)String type,
                                                HttpServletRequest req) {
        ChannelInfo info = new ChannelInfo();
        List<NnChannel> channels = new ArrayList<NnChannel>();
        NnChannelManager channelMngr = NNF.getChannelMngr();
        short srtType = 0;
        if (type== null)
            srtType = 0;//place holder
        else
            srtType = Short.parseShort(type);
        try {
            if (srtType == NnChannel.CONTENTTYPE_YOUTUBE_SPECIAL_SORTING) {
                channels = channelMngr.findByType(NnChannel.CONTENTTYPE_YOUTUBE_SPECIAL_SORTING);
            } else {
                channels = channelMngr.findMaples();
            }
            String[] transcodingEnv = NNF.getDepotService().getTranscodingEnv(req);        
            String callbackUrl = transcodingEnv[1];        
            List<Channel> cs = new ArrayList<Channel>();
            for (NnChannel c : channels) {
                cs.add(new Channel(String.valueOf(c.getId()), 
                                   c.getSourceUrl(), 
                                   c.getTranscodingUpdateDate(), 
                                   "0",
                                   String.valueOf(c.getCntSubscribe())));
            }
            log.info("maple channels:" + channels.size());
            info.setErrorCode(String.valueOf(NnStatusCode.SUCCESS));
            info.setErrorReason("Success");
            info.setChannels(cs);
            info.setCallBack(callbackUrl);
        } catch (Exception e) {
            PostResponse resp = NNF.getDepotService().handleException(e);
            info.setErrorCode(resp.getErrorCode());
            info.setErrorReason(resp.getErrorReason());
        }
        return info;
    }
        
    /**
     * Transcoding service update Podcast Channel Information
     * 
     * @param podcast podcast in json type <br/>
     * {  <br/>
     *    "action":"updateChannel", <br/>
     *    "key":"channel_key_id", <br/>
     *    "title":"channel_title", <br/>
     *    "description":"channel_description", <br/>    
     *    "pubDate":"channel_pubDate", <br/>
     *    "image":"channel_thumbnail",<br/>   
     *    "errorCode":0, <br/>
     *    "errorReason":"error description" <br/>     
     * } 
     */
    @RequestMapping("channelUpdate")
    public @ResponseBody PostResponse channelUpdate(@RequestBody RtnChannel podcast) {
        log.info(podcast.toString());
        PostResponse resp = new PostResponse(
            String.valueOf(NnStatusCode.ERROR), NnStatusMsg.getPlayerMsg(NnStatusCode.ERROR));
        try {
            resp = NNF.getDepotService().updateChannel(podcast);
        } catch (Exception e) {
            resp = NNF.getDepotService().handleException(e);
        }
        return resp;
    }

    /**
     * Transcoding service update Podcast Channel Information
     * 
     * @param podcast podcast in json type <br/>
     * {  <br/>
     *    "action":"updateChannel", <br/>
     *    "key":"channel_key_id", <br/>
     *    "title":"channel_title", <br/>
     *    "description":"channel_description", <br/>    
     *    "pubDate":"channel_pubDate", <br/>
     *    "image":"channel_thumbnail",<br/>   
     *    "errorCode":0, <br/>
     *    "errorReason":"error description" <br/>     
     * } 
     */
    @RequestMapping("channelUpdateTest")
    public  @ResponseBody PostResponse channelUpdateTest(
            @RequestParam(value="key", required=false)String key,
            @RequestParam(value="title", required=false)String title            
            ) {
        RtnChannel podcast = new RtnChannel();
        podcast.setKey(key);
        podcast.setTitle(title);
        podcast.setErrorCode("0");
        this.channelUpdate(podcast);
        
        log.info(podcast.toString());
        PostResponse resp = new PostResponse(
                String.valueOf(NnStatusCode.ERROR), NnStatusMsg.getPlayerMsg(NnStatusCode.ERROR));
        try {
            resp = NNF.getDepotService().updateChannel(podcast);
        } catch (Exception e) {
            resp = NNF.getDepotService().handleException(e);
        }
        return resp;
    }
    
    @RequestMapping("getDepotServer")
    public ResponseEntity<String> getDepotServer(HttpServletRequest req) {
        DepotService depot = new DepotService();
        String[] transcodingEnv = depot.getTranscodingEnv(req);
        String transcodingServer = transcodingEnv[0];
        String callbackUrl = transcodingEnv[1];
        String output = "transcodingServer:" + transcodingServer + "\n" + ";callbackUrl:" + callbackUrl;
        return NnNetUtil.textReturn(output);
    }
    
    //format 
    @RequestMapping(value="resetViewCntCache", produces = ApiGeneric.PLAIN_TEXT_UTF8)
    public @ResponseBody String resetViewCntCache() {
        
        List<Counter> counters = NNF.getCounterDao().getViewCounters();
        log.info("reset counters size = " + counters.size());
//        for (Counter counter : counters) {
//            String counterName = counter.getCounterName();
//            long count = CounterFactory.getCount(counterName);
//            log.info(String.format("cache name = %s, value = %d", counterName, count));
//            CacheFactory.set(counterName, String.valueOf(count));
//        }
        return "OK";
    }
    
}
