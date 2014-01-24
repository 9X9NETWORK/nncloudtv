package com.nncloudtv.service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;

import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.lib.YouTubeLib;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.MsoConfig;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.NnEpisode;
import com.nncloudtv.model.NnProgram;
import com.nncloudtv.web.api.ApiContext;

public class PlayerService {
    
    protected static final Logger log = Logger.getLogger(PlayerService.class.getName());
    
    public static final String META_TITLE = "meta_title";
    public static final String META_THUMBNAIL = "meta_thumbnail";
    public static final String META_DESCRIPTION = "meta_desciption";
    public static final String META_URL = "meta_url";
    public static final String META_KEYWORD = "meta_keyword";
    public static final String META_CHANNEL_TITLE = "crawlChannelTitle";
    public static final String META_EPISODE_TITLE = "crawlEpisodeTitle";
    public static final String META_VIDEO_THUMBNAIL = "crawlVideoThumb";
    public static final String META_FAVICON = "favicon";
    
    private NnUserManager userMngr;
    private MsoConfigManager configMngr;
    private MsoManager msoMngr;
    
    @Autowired
    public PlayerService(NnUserManager userMngr, MsoConfigManager configMngr, MsoManager msoMngr) {
        
        this.userMngr = userMngr;
        this.configMngr = configMngr;
        this.msoMngr = msoMngr;
    }
    
    public PlayerService() {
        
        this.userMngr = new NnUserManager();
        this.configMngr = new MsoConfigManager();
        this.msoMngr = new MsoManager();
    }
    
    public Model prepareBrand(Model model, String msoName, HttpServletResponse resp) {        
        if (msoName != null) {
            msoName = msoName.toLowerCase();
        } else {
            msoName = Mso.NAME_9X9;
        }
        
        // bind favicon
        Mso mso = msoMngr.findByName(msoName);
        if (mso == null)
            return model;
        
        model.addAttribute(PlayerService.META_TITLE, mso.getTitle());
        model.addAttribute(PlayerService.META_DESCRIPTION, mso.getIntro());
        model.addAttribute(PlayerService.META_THUMBNAIL, mso.getLogoUrl());
        MsoConfig config = configMngr.findByMsoAndItem(mso, MsoConfig.FAVICON_URL);
        String faviconUrl = config == null ? null : config.getValue();
        if (faviconUrl != null) { 
            model.addAttribute(META_FAVICON, "<link rel='icon' href='" + faviconUrl + 
                                             "' type='image/x-icon'/>" +
                                             "<link rel='shortcut icon' href='" + faviconUrl +
                                             "' type='image/x-icon'/>");
        }
        
        return model;
    }
    
    public String getGAReportUrl(String ch, String ep, String mso) {
    	String reportUrl = "/promotion";
    	if (ch != null) {
    		reportUrl += "/ch" + ch;
    	}
    	if (ep != null) {
    		if (ep.matches("e[0-9]+")) {
    			reportUrl += "/" + ep;
    		} else {
    			reportUrl += "/yt" + ep;
    		}
    	} 
    	String msoDomain = "www.9x9.tv";
    	if (mso != null && !mso.equals(Mso.NAME_9X9))
    		msoDomain = mso + ".9x9.tv"; 
    	reportUrl += "?mso=" + msoDomain;
    	return reportUrl;
    }
    
    //it is likely for old ios app who doesn't know about episdoe
    public String findFirstSubepisodeId(String eId) {
        if (eId != null && eId.matches("e[0-9]+")) {
            String eid = eId.replace("e", "");
            NnEpisodeManager episodeMngr = new NnEpisodeManager();
            NnEpisode episodeObj = episodeMngr.findById(Long.valueOf(eid));
            if (episodeObj != null) {
                List<NnProgram> programs = new NnProgramManager().findByEpisodeId(episodeObj.getId());
                if (programs.size() > 0)
                    eId = String.valueOf(programs.get(0).getId());
            }
        }
        log.info("first subepisode id:" + eId);
        return eId;        
    }
    
    private String prepareFb(String text, int type) {
        //0 = name, 1 = description, 2 = image
        if (type == 1) {
        	 if (text == null || text.length() == 0) {
        		 log.info("make fb description empty space with &nbsp;");
        		 return "&nbsp;";
        	 }
            return PlayerService.revertHtml(text);
        }
        if (type == 2) {
            if (text == null || text.length() == 0) {
                return PlayerService.revertHtml(" ");
            }
            return PlayerService.revertHtml(text);
        }
        if (type == 3) {
            return NnStringUtil.htmlSafeChars(text);
        }
        return PlayerService.revertHtml(text); 
    }
    
    public Model prepareEpisode(Model model, String pid,
            String mso, HttpServletResponse resp) {
        if (pid == null)
            return model;
        if (pid.matches("[0-9]+")) {
            NnProgramManager programMngr = new NnProgramManager();
            NnProgram program = programMngr.findById(Long.valueOf(pid));
            if (program != null) {
                log.info("nnprogram found = " + pid);
                model.addAttribute(META_EPISODE_TITLE, program.getName());
                model.addAttribute("crawlEpThumb1", program.getImageUrl());
                model.addAttribute(META_TITLE, this.prepareFb(program.getName(), 0));
                model.addAttribute(META_DESCRIPTION, this.prepareFb(program.getIntro(), 1));
                model.addAttribute(META_THUMBNAIL, this.prepareFb(program.getImageUrl(), 2));
                model.addAttribute(META_URL, this.prepareFb(NnStringUtil.getSharingUrl(false, null, "" + program.getChannelId(), pid), 3));
            }
        } else if (pid.matches("e[0-9]+")){
            String eid = pid.replace("e", "");
            NnEpisodeManager episodeMngr = new NnEpisodeManager();
            NnEpisode episode = episodeMngr.findById(Long.valueOf(eid));
            if (episode != null) {
                log.info("nnepisode found = " + eid);
                model.addAttribute(META_EPISODE_TITLE, episode.getName());
                model.addAttribute("crawlEpThumb1", episode.getImageUrl());
                model.addAttribute(META_TITLE, this.prepareFb(episode.getName(), 0));
                model.addAttribute(META_DESCRIPTION, this.prepareFb(episode.getIntro(), 1));
                model.addAttribute(META_THUMBNAIL, this.prepareFb(episode.getImageUrl(), 2));
                model.addAttribute(META_URL, this.prepareFb(NnStringUtil.getSharingUrl(false, mso, episode.getChannelId(), episode.getId()), 3));
            }
            /*
            Map<String, String> entry = YouTubeLib.getYouTubeVideoEntry(pid);
            model.addAttribute(META_NAME, NnStringUtil.htmlSafeChars(entry.get("title")));
            model.addAttribute(META_DESCRIPTION, NnStringUtil.htmlSafeChars(entry.get("description")));
            model.addAttribute(META_IMAGE, NnStringUtil.htmlSafeChars(entry.get("thumbnail")));
            */
        }
        return model;
    }

    public Model prepareChannel(Model model, String cid,
            String mso, HttpServletResponse resp) {
        NnChannelManager channelMngr = new NnChannelManager();
        if (cid == null || !cid.matches("[0-9]+")) {
            return model;
        }
        NnChannel channel = channelMngr.findById(Long.valueOf(cid));
        if (channel != null) {
            log.info("found channel = " + cid);
            model.addAttribute(META_CHANNEL_TITLE, channel.getName());
            model.addAttribute(META_VIDEO_THUMBNAIL, channel.getOneImageUrl());
            model.addAttribute(META_TITLE, this.prepareFb(channel.getName(), 0));
            model.addAttribute(META_DESCRIPTION, this.prepareFb(channel.getIntro(), 1));
            model.addAttribute(META_THUMBNAIL, this.prepareFb(channel.getOneImageUrl(), 2));
            model.addAttribute(META_URL, this.prepareFb(NnStringUtil.getSharingUrl(false, mso, channel.getId(), null), 3));
        }
        return model;
    }

    public Model preparePlayer(Model model, String js, String jsp, HttpServletRequest req) {
        model.addAttribute("js", "");
        if (js != null && js.length() > 0) {
            model.addAttribute("js", js);
        }
        if (jsp != null && jsp.length() > 0) {
            log.info("alternate is enabled: " + jsp);
        }
        model.addAttribute("locale", userMngr.findLocaleByHttpRequest(req));
        return model;
    }

    //get all the query string(things after ?) from url except ch/channel, ep/episode
    public String rewrite(HttpServletRequest req) {
        String url = req.getRequestURL().toString();        
        String queryStr = req.getQueryString();        
        if (queryStr != null && !queryStr.equals("null"))
            queryStr = "?" + queryStr;
        else 
            queryStr = "";
        url = url + queryStr;
        Pattern pattern = Pattern.compile("(.*)\\?(.*)");
        Matcher m = pattern.matcher(url);
        if (m.find()) {
            String matched = m.group(2);
            matched = matched.replaceAll("ch=[^&]*&?",  "");
            log.info("matched 1:" + matched);
            matched = matched.replaceAll("ep=[^&]*&?", "");
            log.info("matched 2:" + matched);
            matched = matched.replaceAll("channel=[^&]*&?", "");
            log.info("matched 3:" + matched);
            matched = matched.replaceAll("episode=[^&]*&?", "");
            log.info("matched 4:" + matched);
            matched = matched.replaceAll("fb_action=[^&]*&?", "");
            log.info("matched 5:" + matched);            
            if (matched.length() > 0)
                return "?" + matched;
        }
        return "";
    }
    
    public Model prepareCrawled(Model model, String escaped, ApiContext context) {
        try {
            escaped = URLDecoder.decode(escaped, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        log.info("escaped=" + escaped);        
        
        //-- determine channel and episode and set --
        String ch=null, ep=null, youtubeEp=null, landing=null;
        boolean episodeShare = false;
        Pattern pattern = Pattern.compile("^/playback/(\\d+)");
        Matcher m = pattern.matcher(escaped);
        if (m.find()) {
            ch = m.group(1);
        }
        pattern = Pattern.compile("^/playback/(\\d+)/(\\w+)");
        m = pattern.matcher(escaped);
        if (m.find()) {
            ch = m.group(1);
            youtubeEp = m.group(2);
            episodeShare = true;
        }
        pattern = Pattern.compile("^/playback/(\\d+)/(e?\\d+)");
        m = pattern.matcher(escaped);
        if (m.find()) {
            ch = m.group(1);
            ep = m.group(2);
            episodeShare = true;
            youtubeEp = null;
        }
        pattern = Pattern.compile("(ch=)(\\d+)");
        m = pattern.matcher(escaped);
        if (m.find()) {            
            ch = m.group(2);
        }
        pattern = Pattern.compile("(ep=)(\\d+)");
        m = pattern.matcher(escaped);
        if (m.find()) {            
            ep = m.group(2);
            episodeShare = true;
        }
        pattern = Pattern.compile("(ep=)(\\w+)");
        m = pattern.matcher(escaped);
        if (m.find()) {            
            youtubeEp = m.group(2);
            episodeShare = true;
        }
        pattern = Pattern.compile("(ep=)(e\\d+)");
        m = pattern.matcher(escaped);
        if (m.find()) {            
            ep = m.group(2);
            episodeShare = true;
            youtubeEp = null;
        }        
        if (ch == null) {
            pattern = Pattern.compile("^\\d+$");
            m = pattern.matcher(escaped);
            if (m.find()) {
                ch = m.group(0);
            }
        }
        pattern = Pattern.compile("(landing=)(\\w+)");
        m = pattern.matcher(escaped);
        if (m.find()) {            
            landing = m.group(2);
        }
        log.info("ch:" + ch + ";ep:" + ep + ";youtubeEp:" + youtubeEp + ";set:" + landing);        
        
        //-- channel/episode info --
        if (ch != null) {
            NnChannelManager channelMngr = new NnChannelManager();        
            NnChannel c = channelMngr.findById(Long.parseLong(ch));
            if (c != null) {
                String sharingUrl = NnStringUtil.getSharingUrl(false, context, ch, (ep == null ? youtubeEp : ep));
                model.addAttribute(META_URL, this.prepareFb(sharingUrl, 3));
                model.addAttribute(META_CHANNEL_TITLE, c.getName());
                //in case not enough episode data, use channel for default  
                model.addAttribute(META_EPISODE_TITLE, c.getName());
                model.addAttribute(META_VIDEO_THUMBNAIL, c.getOneImageUrl());
                model.addAttribute("crawlEpThumb1", c.getOneImageUrl());                
                model.addAttribute(META_TITLE, this.prepareFb(c.getName(), 0));
                model.addAttribute(META_DESCRIPTION, this.prepareFb(c.getIntro(), 1));                
                model.addAttribute(META_THUMBNAIL, this.prepareFb(c.getOneImageUrl(), 2));  
                
                if (ep != null && ep.startsWith("e")) {
                    ep = ep.replaceFirst("e", "");
                    NnEpisodeManager episodeMngr = new NnEpisodeManager(); 
                    List<NnEpisode> episodes = episodeMngr.findPlayerEpisodes(c.getId(), c.getSorting());
                    int i = 1;                    
                    for (NnEpisode e : episodes) {
                        if (i > 1 && i < 4) {
                            model.addAttribute("crawlEpThumb" + i, e.getImageUrl());
                            System.out.println("crawlEpThumb" + i + ":" + e.getImageUrl());
                            i++;
                        }
                        if (e.getId() == Long.parseLong(ep)) {
                            model.addAttribute(META_VIDEO_THUMBNAIL, e.getImageUrl());
                            model.addAttribute(META_EPISODE_TITLE, e.getName());
                            model.addAttribute("crawlEpThumb" + i, e.getImageUrl());
                            if (episodeShare) {
                               model.addAttribute(META_TITLE, this.prepareFb(e.getName(), 0));   
                               model.addAttribute(META_DESCRIPTION, this.prepareFb(e.getIntro(), 1));
                               model.addAttribute(META_THUMBNAIL, this.prepareFb(e.getImageUrl(), 2));
                            }
                            i++;
                        }
                        if (i == 4) {
                            break;
                        }
                    }            
                } else {
                    NnProgramManager programMngr = new NnProgramManager();
                    List<NnProgram> programs = programMngr.findPlayerProgramsByChannel(c.getId());
                    if (programs.size() > 0) {
                        int i=1;                    
                        if (ep == null)
                            ep = String.valueOf(programs.get(0).getId());
                        for (NnProgram p : programs) {
                            if (i > 1 && i < 4) {
                                model.addAttribute("crawlEpThumb" + i, p.getImageUrl());
                                System.out.println("crawlEpThumb" + i + ":" + p.getImageUrl());
                                i++;
                            }
                            if (p.getId() == Long.parseLong(ep)) {
                                model.addAttribute(META_VIDEO_THUMBNAIL, p.getImageUrl());
                                model.addAttribute(META_EPISODE_TITLE, p.getName());
                                model.addAttribute("crawlEpThumb" + i, p.getImageUrl());
                                if (episodeShare) {
                                   model.addAttribute(META_TITLE, this.prepareFb(p.getName(), 0));
                                   model.addAttribute(META_DESCRIPTION, this.prepareFb(p.getIntro(), 1));
                                   model.addAttribute(META_THUMBNAIL, this.prepareFb(p.getImageUrl(), 2));
                                }
                                i++;
                            }
                            if (i == 4) {
                                break;
                            }
                        }
                    } else {                    
                        if (youtubeEp != null) {
                            Map<String, String> result = YouTubeLib.getYouTubeVideo(youtubeEp);
                            model.addAttribute(META_EPISODE_TITLE, result.get("title"));
                            model.addAttribute(META_VIDEO_THUMBNAIL, result.get("imageUrl"));
                            model.addAttribute("crawlEpThumb1", result.get("imageUrl"));
                            model.addAttribute("crawlEpThumb2", result.get("imageUrl"));
                            model.addAttribute("crawlEpThumb3", result.get("imageUrl"));
                        }
                    }
                    if (episodeShare) {
                        model.addAttribute(META_TITLE, this.prepareFb((String)model.asMap().get(META_EPISODE_TITLE), 0));
                        model.addAttribute(META_THUMBNAIL, this.prepareFb((String)model.asMap().get(META_VIDEO_THUMBNAIL), 2));
                    }
                }
            }
        }
        
        return model;
    }
    
    public static String revertHtml(String str) {
        if (str == null) return null;
        return str.replace("&gt;", ">")
                  .replace("&lt;", "<")
                  .replace("&amp;", "&");
    }
    
}
