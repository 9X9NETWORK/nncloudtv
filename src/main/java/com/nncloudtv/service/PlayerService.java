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

import org.springframework.ui.Model;

import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.lib.stream.YouTubeLib;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.MsoConfig;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.NnEpisode;
import com.nncloudtv.model.NnProgram;
import com.nncloudtv.web.api.ApiContext;

public class PlayerService {
    
    protected static final Logger log = Logger.getLogger(PlayerService.class.getName());
    
    public static final String META_TITLE           = "meta_title";
    public static final String META_THUMBNAIL       = "meta_thumbnail";
    public static final String META_DESCRIPTION     = "meta_desciption";
    public static final String META_URL             = "meta_url";
    public static final String META_KEYWORD         = "meta_keyword";
    public static final String META_CHANNEL_TITLE   = "crawlChannelTitle";
    public static final String META_EPISODE_TITLE   = "crawlEpisodeTitle";
    public static final String META_VIDEO_THUMBNAIL = "crawlVideoThumb";
    public static final String META_FAVICON         = "favicon";
    
    public Model prepareBrand(Model model, String msoName, HttpServletResponse resp) {        
        if (msoName != null) {
            msoName = msoName.toLowerCase();
        } else {
            msoName = Mso.NAME_SYS;
        }
        
        // bind favicon
        Mso mso = NNF.getMsoMngr().findByName(msoName);
        if (mso == null)
            return model;
        
        model.addAttribute(PlayerService.META_TITLE,       mso.getTitle());
        model.addAttribute(PlayerService.META_DESCRIPTION, mso.getIntro());
        model.addAttribute(PlayerService.META_THUMBNAIL,   mso.getLogoUrl());
        MsoConfig config = NNF.getConfigMngr().findByMsoAndItem(mso, MsoConfig.FAVICON_URL);
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
    	if (mso != null && !mso.equals(Mso.NAME_SYS))
    		msoDomain = mso + ".9x9.tv"; 
    	reportUrl += "?mso=" + msoDomain;
    	return reportUrl;
    }
    
    //it is likely for old ios app who doesn't know about episdoe
    public String findFirstSubepisodeId(String eId) {
        if (eId != null && eId.matches("e[0-9]+")) {
            String eid = eId.replace("e", "");
            NnEpisode episodeObj = NNF.getEpisodeMngr().findById(Long.valueOf(eid));
            if (episodeObj != null) {
                List<NnProgram> programs = NNF.getProgramMngr().findByEpisodeId(episodeObj.getId());
                if (programs.size() > 0)
                    eId = String.valueOf(programs.get(0).getId());
            }
        }
        log.info("first subepisode id:" + eId);
        return eId;        
    }
    
    private String prepareFb(String text, int type) {
        //0 = name, 1 = description, 2 = image url, 3 = share url
        if (type == 1) {
            if (text == null || text.length() == 0) {
                log.info("make fb description empty space with &nbsp;");
                return "&nbsp;";
            }
            return NnStringUtil.htmlSafeChars(text);
        }
        if (type == 2) {
            if (text == null || text.length() == 0) {
                return " ";
            }
            return NnStringUtil.htmlSafeChars(text);
        }
        if (type == 3) {
            return NnStringUtil.htmlSafeChars(text);
        }
        return NnStringUtil.htmlSafeChars(text); 
    }
    
    public Model prepareEpisode(Model model, String pid,
            String mso, HttpServletResponse resp) {
        if (pid == null)
            return model;
        if (pid.matches("[0-9]+")) {
            NnProgram program = NNF.getProgramMngr().findById(Long.valueOf(pid));
            if (program != null) {
                log.info("nnprogram found = " + pid);
                model.addAttribute(META_EPISODE_TITLE, prepareFb(program.getName(),     0));
                model.addAttribute("crawlEpThumb1",    prepareFb(program.getImageUrl(), 2));
                model.addAttribute(META_TITLE,         prepareFb(program.getName(),     0));
                model.addAttribute(META_DESCRIPTION,   prepareFb(program.getIntro(),    1));
                model.addAttribute(META_THUMBNAIL,     prepareFb(program.getImageUrl(), 2));
                model.addAttribute(META_URL,           prepareFb(NnStringUtil.getSharingUrl(false, null, "" + program.getChannelId(), pid), 3));
            }
        } else if (pid.matches("e[0-9]+")){
            String eid = pid.replace("e", "");
            NnEpisode episode = NNF.getEpisodeMngr().findById(Long.valueOf(eid));
            if (episode != null) {
                log.info("nnepisode found = " + eid);
                model.addAttribute(META_EPISODE_TITLE, prepareFb(episode.getName(),     0));
                model.addAttribute("crawlEpThumb1",    prepareFb(episode.getImageUrl(), 2));
                model.addAttribute(META_TITLE,         prepareFb(episode.getName(),     0));
                model.addAttribute(META_DESCRIPTION,   prepareFb(episode.getIntro(),    1));
                model.addAttribute(META_THUMBNAIL,     prepareFb(episode.getImageUrl(), 2));
                model.addAttribute(META_URL,           prepareFb(NnStringUtil.getSharingUrl(false, mso, episode.getChannelId(), episode.getId()), 3));
            }
        }
        return model;
    }
    
    public Model prepareChannel(Model model, String cid,
            String mso, HttpServletResponse resp) {
        
        if (cid == null || !cid.matches("[0-9]+")) {
            return model;
        }
        NnChannel channel = NNF.getChannelMngr().findById(Long.valueOf(cid));
        if (channel != null) {
            log.info("found channel = " + cid);
            model.addAttribute(META_CHANNEL_TITLE,   prepareFb(channel.getName(),        0));
            model.addAttribute(META_VIDEO_THUMBNAIL, prepareFb(channel.getOneImageUrl(), 2));
            model.addAttribute(META_TITLE,           prepareFb(channel.getName(),        0));
            model.addAttribute(META_DESCRIPTION,     prepareFb(channel.getIntro(),       1));
            model.addAttribute(META_THUMBNAIL,       prepareFb(channel.getOneImageUrl(), 2));
            model.addAttribute(META_URL,             prepareFb(NnStringUtil.getSharingUrl(false, mso, channel.getId(), null), 3));
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
        model.addAttribute("locale", NNF.getUserMngr().findLocaleByHttpRequest(req));
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
            pattern = Pattern.compile(NnStringUtil.DIGITS_REGEX);
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
            NnChannel c = NNF.getChannelMngr().findById(Long.parseLong(ch));
            if (c != null) {
                String sharingUrl = NnStringUtil.getSharingUrl(false, context, ch, (ep == null ? youtubeEp : ep));
                model.addAttribute(META_URL,             prepareFb(sharingUrl, 3));
                model.addAttribute(META_CHANNEL_TITLE,   c.getName());
                //in case not enough episode data, use channel for default
                model.addAttribute(META_EPISODE_TITLE,   c.getName());
                model.addAttribute(META_VIDEO_THUMBNAIL, c.getOneImageUrl());
                model.addAttribute("crawlEpThumb1",      c.getOneImageUrl());
                model.addAttribute(META_TITLE,           prepareFb(c.getName(), 0));
                model.addAttribute(META_DESCRIPTION,     prepareFb(c.getIntro(), 1));
                model.addAttribute(META_THUMBNAIL,       prepareFb(c.getOneImageUrl(), 2));
                
                if (ep != null && ep.startsWith("e")) {
                    ep = ep.replaceFirst("e", "");
                    List<NnEpisode> episodes = NNF.getEpisodeMngr().findPlayerEpisodes(c.getId(), c.getSorting(), 0, 50);
                    int i = 1;                    
                    for (NnEpisode e : episodes) {
                        if (i > 1 && i < 4) {
                            model.addAttribute("crawlEpThumb" + i, prepareFb(e.getImageUrl(), 2));
                            System.out.println("crawlEpThumb" + i + ":" + e.getImageUrl());
                            i++;
                        }
                        if (e.getId() == Long.parseLong(ep)) {
                            model.addAttribute(META_VIDEO_THUMBNAIL, prepareFb(e.getImageUrl(), 2));
                            model.addAttribute(META_EPISODE_TITLE,   prepareFb(e.getName(),     0));
                            model.addAttribute("crawlEpThumb" + i,   prepareFb(e.getImageUrl(), 2));
                            if (episodeShare) {
                               model.addAttribute(META_TITLE,       prepareFb(e.getName(),     0));
                               model.addAttribute(META_DESCRIPTION, prepareFb(e.getIntro(),    1));
                               model.addAttribute(META_THUMBNAIL,   prepareFb(e.getImageUrl(), 2));
                            }
                            i++;
                        }
                        if (i == 4) {
                            break;
                        }
                    }            
                } else {
                    List<NnProgram> programs = NNF.getProgramMngr().findPlayerProgramsByChannel(c.getId());
                    if (programs.size() > 0) {
                        int i=1;                    
                        if (ep == null)
                            ep = String.valueOf(programs.get(0).getId());
                        for (NnProgram p : programs) {
                            if (i > 1 && i < 4) {
                                model.addAttribute("crawlEpThumb" + i, prepareFb(p.getImageUrl(), 2));
                                System.out.println("crawlEpThumb" + i + ":" + p.getImageUrl());
                                i++;
                            }
                            if (p.getId() == Long.parseLong(ep)) {
                                model.addAttribute(META_VIDEO_THUMBNAIL, prepareFb(p.getImageUrl(), 2));
                                model.addAttribute(META_EPISODE_TITLE,   prepareFb(p.getName(),     0));
                                model.addAttribute("crawlEpThumb" + i,   prepareFb(p.getImageUrl(), 2));
                                if (episodeShare) {
                                   model.addAttribute(META_TITLE,       prepareFb(p.getName(),     0));
                                   model.addAttribute(META_DESCRIPTION, prepareFb(p.getIntro(),    1));
                                   model.addAttribute(META_THUMBNAIL,   prepareFb(p.getImageUrl(), 2));
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
}
