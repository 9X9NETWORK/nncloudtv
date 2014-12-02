package com.nncloudtv.web;

import java.security.SignatureException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.nncloudtv.lib.AmazonLib;
import com.nncloudtv.lib.CookieHelper;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnLogUtil;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.MsoConfig;
import com.nncloudtv.model.NnUser;
import com.nncloudtv.model.NnUserProfile;
import com.nncloudtv.service.MsoConfigManager;
import com.nncloudtv.service.NnUserProfileManager;
import com.nncloudtv.web.api.ApiContext;

@Controller
@RequestMapping("cms")
public class CmsController {
	
	protected static final Logger log = Logger.getLogger(CmsController.class.getName());
	
	private boolean isReadonlyMode() {
        boolean readonly = false;
        MsoConfig config = NNF.getConfigMngr().findByItem(MsoConfig.RO);
        if (config != null && config.getValue() != null && config.getValue().equals("1"))
            readonly = true;
		log.info("readonly mode: " + readonly);
		return readonly;
	}
	
	@ModelAttribute("uploadBucket")
	public String getS3UploadBucket() {
		return MsoConfigManager.getS3UploadBucket();
	}
	
	@ModelAttribute("root")
	public String getExternalRootPath() {
		return MsoConfigManager.getExternalRootPath();
	}
    
    private Model setAttributes(Model model, NnUserProfile profile) {
        
        model.addAttribute("priv", profile.getPriv());
        
        return model;
    }

	private Model setAttributes(Model model, Mso mso) {
		
		model.addAttribute("msoLogo", mso.getLogoUrl());
		model.addAttribute("mso", mso);
		model.addAttribute("msoId", mso.getId());
		model.addAttribute("msoType", mso.getType());
		model.addAttribute("msoName", mso.getName());
        model.addAttribute("logoutUrl", "/api/logout");
		
		return model;
	}
	
	@ExceptionHandler(Exception.class)
	public String exception(Exception e) {
		NnLogUtil.logException(e);
		return "error/blank";
	}
	
	@RequestMapping("logout")
	public String genericLogout(HttpServletResponse resp, Model model) {
		CookieHelper.deleteCookie(resp, CookieHelper.USER);
		model.asMap().clear();
		return "redirect:../";
	}
	
    @RequestMapping(value = "{cmsTab}", method = RequestMethod.GET)
    public String genericCMS(HttpServletRequest request, @PathVariable String cmsTab, Model model)
            throws SignatureException {
        
        ApiContext ctx = new ApiContext(request);
        NnUser nnuser = ctx.getAuthenticatedUser(0);
        if (nnuser == null) {
            
            log.info("user not login");
            model.asMap().clear();
            return "redirect:../";
            
        } else {
            
            NnUserProfile profile = NNF.getProfileMngr().pickupBestProfile(nnuser);
            nnuser.setMsoId(profile.getMsoId());
            nnuser.setProfile(profile);
            Mso mso = ctx.getMso();
            if (profile != null) {
                profile.setCntChannel(NNF.getChannelMngr().calculateUserChannels(nnuser));
                nnuser.setProfile(profile);
                mso = NNF.getMsoMngr().findById(profile.getMsoId());
                model = setAttributes(model, profile);
            }
            
            if (isReadonlyMode()) {
                model.addAttribute("msoLogo", mso.getLogoUrl());
                return "cms/readonly";
            }
            
            model = setAttributes(model, mso);
            model.addAttribute("locale", ctx.getLang());
            
            if (cmsTab.equals("admin") || cmsTab.equals("channelManagement") || cmsTab.equals("channelSetManagement")) {
                String policy = AmazonLib.buildS3Policy(MsoConfigManager.getS3UploadBucket(), "public-read", "");
                model.addAttribute("s3Policy", policy);
                model.addAttribute("s3Signature", AmazonLib.calculateRFC2104HMAC(policy, MsoConfigManager.getAWSKey()));
                model.addAttribute("s3Id", MsoConfigManager.getAWSId());
                if (cmsTab.equals("admin")) {
                    if (NnUserProfileManager.checkPriv(nnuser, NnUserProfile.PRIV_PCS))
                        return "cms/channelSetManagement";
                    else
                        return "cms/channelManagement";
                }
                
                return "cms/" + cmsTab;
            } else if (cmsTab.equals("promotionTools") || cmsTab.equals("setup") || cmsTab.equals("statistics")) {
                return "cms/" + cmsTab;
            } else {
                return "error/404";
            }
        }
    }
}
