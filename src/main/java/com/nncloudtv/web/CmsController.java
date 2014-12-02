package com.nncloudtv.web;

import java.security.SignatureException;
import java.util.logging.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

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
	public String genericCMSLogout(HttpServletResponse resp, Model model) {
		CookieHelper.deleteCookie(resp, CookieHelper.USER);
		model.asMap().clear();
		return "redirect:../";
	}
	
    @RequestMapping(value = "{cmsTab}", method = RequestMethod.GET)
    public String genericCMSLogin(HttpServletRequest request, @PathVariable String cmsTab, Model model)
            throws SignatureException {
        
        ApiContext ctx = new ApiContext(request);
        NnUser user = ctx.getAuthenticatedUser();
        if (user == null) {
            
            log.info("user not login");
            model.asMap().clear();
            return "redirect:../9x9";
            
        } else {
            Mso mso = ctx.getMso();
            if (isReadonlyMode()) {
                model.addAttribute("msoLogo", mso.getLogoUrl());
                return "cms/readonly";
            }
            mso.setLogoUrl(MsoConfigManager.getExternalRootPath() + "/images/9x9.tv.png");
            model = setAttributes(model, mso);
            model.addAttribute("locale", ctx.getLang());
            if (cmsTab.equals("channelManagement") || cmsTab.equals("channelSetManagement")) {
                String policy = AmazonLib.buildS3Policy(MsoConfigManager.getS3UploadBucket(), "public-read", "");
                model.addAttribute("s3Policy", policy);
                model.addAttribute("s3Signature", AmazonLib.calculateRFC2104HMAC(policy, MsoConfigManager.getAWSKey()));
                model.addAttribute("s3Id", MsoConfigManager.getAWSId());
                return "cms/" + cmsTab;
            } else if (cmsTab.equals("promotionTools") || cmsTab.equals("setup") || cmsTab.equals("statistics")) {
                return "cms/" + cmsTab;
            } else {
                return "error/404";
            }
        }
    }
    
    @RequestMapping(value = "{msoName}/admin", method = RequestMethod.GET)
    public String admin(HttpServletRequest request, HttpServletResponse response,
            @PathVariable("msoName") String msoName, Model model) throws SignatureException {
        
        ApiContext ctx = new ApiContext(request);
        
        log.info("msoName = " + msoName);
		Mso mso = NNF.getMsoMngr().findByName(msoName);
		if (mso == null)
			return "error/404";
		if (isReadonlyMode()) {
			model.addAttribute("msoLogo", mso.getLogoUrl());
			return "cms/readonly";
		}
		NnUser user = ctx.getAuthenticatedUser(mso.getId());
		
		if (user != null) {
			model.asMap().clear();
			if (NnUserProfileManager.checkPriv(user, NnUserProfile.PRIV_PCS))
				return "redirect:../channelSetManagement";
			else
				return "redirect:../channelManagement";
			
			
		} else {
			Cookie[] cookies = request.getCookies();
			if (cookies != null) {
				for (Cookie cookie : cookies) {
					log.info(cookie.getName());
					if (cookie.getName().length() > 0 && cookie.getName().compareTo("cms_login_" + msoName) == 0) {
						String[] split = cookie.getValue().split("\\|");
						if (split.length >= 2) {
							model.addAttribute("email", split[0]);
							model.addAttribute("password", split[1]);
						}
					}
				}
			}
			model.addAttribute("msoLogo", mso.getLogoUrl());
			model.addAttribute("locale", request.getLocale().getLanguage());
			CookieHelper.deleteCookie(response, CookieHelper.USER);
			return "cms/login";
		}
	}
	
    @RequestMapping(value = "{msoName}/admin", method = RequestMethod.POST)
    public String login(HttpServletRequest request,
                        HttpServletResponse response,
                        Model model,
                        @RequestParam String email,
                        @RequestParam String password,
                        @RequestParam(required = false) Boolean rememberMe,
                        @PathVariable String msoName) {
        
        log.info(msoName);
        log.info("email = " + email);
        log.info("password = " + password);
        log.info("rememberMe = " + rememberMe);
        
        Mso mso = NNF.getMsoMngr().findByName(msoName);
        if (mso == null)
            return "error/404";
        String msoLogo = mso.getLogoUrl();
        if (isReadonlyMode()) {
            model.addAttribute("msoLogo", msoLogo);
            return "cms/readonly";
        }
        NnUser user = NNF.getUserMngr().findAuthenticatedUser(email, password, mso.getId(), request);
        if (user != null) {
            CookieHelper.setCookie(response, CookieHelper.USER, user.getToken());
        }
        if (user == null) {
            log.info("login failed");
            
            NnUser found = NNF.getUserMngr().findByEmail(email, mso.getId(), request);
            String error;
            if (found != null && found.getEmail().equals(email)) {
                error = "invalid password";
            } else {
                error = "invalid account";
            }
            model.addAttribute("email", email);
            model.addAttribute("password", password);
            model.addAttribute("msoLogo", msoLogo);
            model.addAttribute("error", error);
            CookieHelper.deleteCookie(response, CookieHelper.USER);
            return "cms/login";
        }
        
        // set cookie
        if (rememberMe != null && rememberMe) {
            log.info("set cookie");
            CookieHelper.setCookie(response, "cms_login_" + msoName, email + "|" + password);
        } else {
            CookieHelper.setCookie(response, "cms_login_" + msoName, "");
        }
        model.asMap().clear();
        if (NnUserProfileManager.checkPriv(user, NnUserProfile.PRIV_PCS))
            return "redirect:../channelSetManagement";
        else
            return "redirect:../channelManagement";
    }
    
    @RequestMapping(value = "{msoName}/logout")
    public String logout(Model model, HttpServletRequest request, HttpServletResponse response,
            @PathVariable String msoName) {
        CookieHelper.deleteCookie(response, CookieHelper.USER);
        model.asMap().clear();
        return "redirect:../admin";
    }
    
    @RequestMapping(value = "{msoName}/{cmsTab}")
    public String management(HttpServletRequest request, HttpServletResponse response,
            @PathVariable String msoName, @PathVariable String cmsTab,
            Model model) throws SignatureException {
        
        ApiContext ctx = new ApiContext(request);
		Mso mso = NNF.getMsoMngr().findByName(msoName);
		if (mso == null) {
			return "error/404";
		}
		if (isReadonlyMode()) {
			model.addAttribute("msoLogo", mso.getLogoUrl());
			return "cms/readonly";
		}
		
        NnUser user = ctx.getAuthenticatedUser(mso.getId());
		if (user != null) {
			model = setAttributes(model, mso);
			model.addAttribute("locale", request.getLocale().getLanguage());
			
			if (cmsTab.equals("channelManagement") || cmsTab.equals("channelSetManagement")) {
				String policy = AmazonLib.buildS3Policy(MsoConfigManager.getS3UploadBucket(), "public-read", "");
				model.addAttribute("s3Policy", policy);
				model.addAttribute("s3Signature", AmazonLib.calculateRFC2104HMAC(policy, MsoConfigManager.getAWSKey()));
				model.addAttribute("s3Id", MsoConfigManager.getAWSId());
				return "cms/" + cmsTab;
			} else if (cmsTab.equals("promotionTools") || cmsTab.equals("setup") || cmsTab.equals("statistics")) {
				return "cms/" + cmsTab;
			} else {
				return "error/404";
			}
		} else {
            CookieHelper.deleteCookie(response, CookieHelper.USER);
			model.asMap().clear();
			return "redirect:../admin";
		}
	}
}
