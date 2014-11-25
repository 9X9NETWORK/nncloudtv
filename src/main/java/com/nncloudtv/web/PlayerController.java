package com.nncloudtv.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnLogUtil;
import com.nncloudtv.lib.NnNetUtil;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.MsoConfig;
import com.nncloudtv.model.NnUser;
import com.nncloudtv.service.MsoConfigManager;
import com.nncloudtv.service.PlayerService;
import com.nncloudtv.web.api.ApiContext;

@Controller
@RequestMapping("")
public class PlayerController {
    
    protected static final Logger log = Logger.getLogger(PlayerController.class.getName());
    
    @ExceptionHandler(Exception.class)
    public String exception(Exception e) {
        NnLogUtil.logException(e);
        return "error/exception";
    }
    
    @RequestMapping("gwallet") 
    public @ResponseBody String gwallet(HttpServletRequest req, HttpServletResponse resp) {
    	try {
    		 StringBuilder buffer = new StringBuilder();
    		 BufferedReader reader = req.getReader();
    		 String line;
    		 while ((line = reader.readLine()) != null) {
    		    buffer.append(line);
    		 }
    		 String data = buffer.toString();
    		 log.info("\n-----gwallet start----\n" + data + "\n-----gwallet end----\n");    		 
        } catch (IOException e) {
	        e.printStackTrace();
        }
        return "OK";
    }
        
    
    @RequestMapping({"", "tv","10ft"})
    public String tv(@RequestParam(value="mso",required=false) String mso, 
            HttpServletRequest req, HttpServletResponse resp, Model model,
            @RequestParam(value="_escaped_fragment_", required=false) String escaped,
            @RequestParam(value="channel", required=false) String channel,
            @RequestParam(value="episode", required=false) String episode,
            @RequestParam(value="ch", required=false) String ch,
            @RequestParam(value="ep", required=false) String ep,
            @RequestParam(value="jsp",required=false) String jsp,
            @RequestParam(value="js",required=false) String js) {
        
        try {
            ApiContext ctx = new ApiContext(req);
            if (ctx.isAndroid() || ctx.isIos()) {
                return "redirect:/mobile/";
            }
            PlayerService service = new PlayerService();
            Mso brand = ctx.getMso();
            if (brand.getType() == Mso.TYPE_FANAPP) {
                //below, merge with view
                log.info("Fan app sharing");
                MsoConfigManager configMngr = NNF.getConfigMngr();
                MsoConfig androidConfig = configMngr.findByMsoAndItem(brand, MsoConfig.STORE_ANDROID);
                MsoConfig iosConfig = configMngr.findByMsoAndItem(brand, MsoConfig.STORE_IOS);
                String androidStoreUrl = "market://details?id=tv.tv9x9.player";
                String iosStoreUrl = "https://itunes.apple.com/app/9x9.tv/id443352510?mt=8";
                if (androidConfig != null) {
                    androidStoreUrl = androidConfig.getValue();
                }
                if (iosConfig != null) {
                    iosStoreUrl = iosConfig.getValue();
                }
                String reportUrl = service.getGAReportUrl(ch, ep, brand.getName());
                model.addAttribute("reportUrl", reportUrl);
                model.addAttribute("androidStoreUrl", androidStoreUrl);
                model.addAttribute("iosStoreUrl", iosStoreUrl);
                model.addAttribute("brandName", brand.getName());
                return "player/fanapp";
            }
            model = service.prepareBrand(model, ctx.getMsoName(), resp);
            model = service.preparePlayer(model, js, jsp, req);
            if (jsp != null && jsp.length() > 0) {
                return "player/" + jsp;
            }
        } catch (Throwable t) {
            NnLogUtil.logThrowable(t);
        }
        return "player/mini";
    }    
    
    /**
     * original url: /durp
     * redirect to:  #!landing=durp  
     */    
    @RequestMapping("{name}")
    public String zooatomics(
            @PathVariable("name") String name,
            @RequestParam(value="jsp",required=false) String jsp,
            @RequestParam(value="js",required=false) String js,
            @RequestParam(value="mso",required=false) String mso,
            HttpServletRequest req, HttpServletResponse resp, Model model) {
        PlayerService service = new PlayerService();
        if (name != null) {
            if (name.matches("[a-zA-Z].+")) {
                NnUser user = NNF.getUserMngr().findByProfileUrl(name, 1);
                if (user != null) {
                    log.info("user enter from curator brand url:" + name);
                    name = "#!" + user.getProfile().getProfileUrl();
                } else {
                    log.info("invalid curator brand url:" + name);
                    name = "";
                }
            }
            String url = service.rewrite(req) + name;
            log.info("redirect url:" + url);
            return "redirect:/" + url;
        }
        //should never hit here, intercept by index
        service.preparePlayer(model, js, jsp, req);
        return "player/zooatomics";
    }
    
    /**
     * original url: view?channel=x&episode=y
     * redirect to:  #!ch=x!ep=y  
     */
    @RequestMapping("view")
    public String view(@RequestParam(value="mso",required=false) String msoName, 
                       HttpServletRequest req, HttpServletResponse resp, Model model, 
                       @RequestParam(value="channel", required=false) String channel,
                       @RequestParam(value="episode", required=false) String episode,
                       @RequestParam(value="js",required=false) String js,
                       @RequestParam(value="jsp",required=false) String jsp,
                       @RequestParam(value="ch", required=false) String ch,
                       @RequestParam(value="ep", required=false) String ep) {
        //additional params
        PlayerService service = new PlayerService();
        ApiContext context = new ApiContext(req);
        
        Mso mso = NNF.getMsoMngr().getByNameFromCache(msoName);
        if (mso == null) {
            mso = NNF.getMsoMngr().getByNameFromCache(Mso.NAME_SYS);;
        }
        String cid = channel != null ? channel : ch;
        String pid = episode != null ? episode : ep;
                
        if (mso.getType() == Mso.TYPE_FANAPP) {            
            log.info("Fan app sharing");
            MsoConfig androidConfig = NNF.getConfigMngr().findByMsoAndItem(mso, MsoConfig.STORE_ANDROID);
            MsoConfig iosConfig = NNF.getConfigMngr().findByMsoAndItem(mso, MsoConfig.STORE_IOS);
        	String androidStoreUrl = "market://details?id=tv.tv9x9.player";
        	String iosStoreUrl = "https://itunes.apple.com/app/9x9.tv/id443352510?mt=8";        	
            if (androidConfig != null) {
            	androidStoreUrl = androidConfig.getValue();
            }
            if (iosConfig != null) {
            	iosStoreUrl = iosConfig.getValue();
            }
        	String reportUrl = service.getGAReportUrl(ch, ep, mso.getName());
        	model.addAttribute("reportUrl", reportUrl);
        	model.addAttribute("androidStoreUrl", androidStoreUrl);
        	model.addAttribute("iosStoreUrl", iosStoreUrl);
        	model.addAttribute("brandName", mso.getName());
        	return "player/fanapp";
        } else {            
            model = service.prepareBrand(model, mso.getName(), resp);
            model = service.prepareChannel(model, cid, mso.getName(), resp);
            model = service.prepareEpisode(model, pid, mso.getName(), resp);
            
            String brandSharingUrl = NnStringUtil.getSharingUrl(false, context, cid, pid);
            log.info("brand sharing url = " + brandSharingUrl);
            model.addAttribute(PlayerService.META_URL, NnStringUtil.htmlSafeChars(brandSharingUrl));
            
            return "player/crawled";
        }
    }
    
    @RequestMapping("android")
    public String android() {
        log.info("android landing");
        return "player/android";
    }
    
    @RequestMapping("support")
    public String support() {
        return "general/support";
    }    
    
    @RequestMapping("tanks")
    public String tanks() {        
        return "player/tanks";
    }
    
    /*
     * used for dns redirect watch dog 
     */
    @RequestMapping("9x9/wd")
    public ResponseEntity<String> watchdog() {        
        return NnNetUtil.textReturn("OK");
    }
    
    /*
    @RequestMapping("flipr")
    public String flipr(HttpServletRequest request,) {        
        Locale locale = request.getLocale();
        if((locale.getCountry()=="TW")||(locale.getCountry()=="CN")) {
            return "redirect:flipr/tw/";
        } else {
            return "redirect:flipr/en/";
        }
    }
    */
    
}
