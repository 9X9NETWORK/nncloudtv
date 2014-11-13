package com.nncloudtv.web.api;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.clearcommerce.ccxclientapi.CcApiBadKeyException;
import com.clearcommerce.ccxclientapi.CcApiBadValueException;
import com.clearcommerce.ccxclientapi.CcApiDocument;
import com.nncloudtv.exception.NnApiBadRequestException;
import com.nncloudtv.exception.NnApiInternalErrorException;
import com.nncloudtv.exception.NnClearCommerceException;
import com.nncloudtv.exception.NnDataIntegrityException;
import com.nncloudtv.lib.ClearCommerceLib;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnLogUtil;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.model.BillingOrder;
import com.nncloudtv.model.BillingPackage;
import com.nncloudtv.model.BillingProfile;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.NnChannelPref;
import com.nncloudtv.model.NnItem;
import com.nncloudtv.model.NnUser;
import com.nncloudtv.service.BillingService;
import com.nncloudtv.web.json.cms.CreditCard;
import com.nncloudtv.web.json.cms.IapInfo;

@Controller
@RequestMapping("api/billing")
public class ApiBilling extends ApiContext {
    
    public ApiBilling(HttpServletRequest req) {
        super(req);
    }
    
    protected static Logger log = Logger.getLogger(ApiBilling.class.getName());
    
    static final String DELIMITER = " - ";
    
    @RequestMapping(value = "packages", method = RequestMethod.GET)
    public @ResponseBody List<BillingPackage> packageList(HttpServletRequest req, HttpServletResponse resp) {
        
        return NNF.getPackageMngr().findAll();
    }
    
    @RequestMapping(value = "profiles/{profileId}", method = RequestMethod.PUT)
    public @ResponseBody BillingProfile billingProfileUpdate(HttpServletRequest req, HttpServletResponse resp, @PathVariable("profileId") String profileIdStr) {
        
        final String TOKEN = "token";
        
        BillingProfile profile = NNF.getBillingProfileMngr().findById(profileIdStr);
        if (profile == null) {
            notFound(resp);
            return null;
        }
        String token = req.getParameter(TOKEN);
        if (token == null) {
            badRequest(resp, ApiContext.MISSING_PARAMETER + DELIMITER + TOKEN);
            return null;
        }
        if (!token.equals(profile.getToken())) {
            badRequest(resp, ApiContext.INVALID_PARAMETER + DELIMITER + TOKEN);
            return null;
        }
        Date now = new Date();
        if (profile.getTokenExpDate() == null || !now.before(profile.getTokenExpDate())) {
            badRequest(resp, ApiContext.INVALID_PARAMETER + DELIMITER + TOKEN);
            return null;
        }
        
        String name  = req.getParameter("name");
        String email = req.getParameter("email");
        String phone = req.getParameter("phone");
        String addr1 = req.getParameter("addr1");
        String addr2 = req.getParameter("addr2");
        String city  = req.getParameter("city");
        String state = req.getParameter("state");
        String zip   = req.getParameter("zip");
        String country = req.getParameter("country");
        if (name != null) {
            
            profile.setName(name);
        }
        if (email != null) {
            
            profile.setEmail(email);
        }
        if (phone != null) {
            
            profile.setPhone(phone);
        }
        if (addr1 != null) {
            
            profile.setAddr1(addr1);
        }
        if (addr2 != null) {
            
            profile.setAddr2(addr2);
        }
        if (city != null) {
            
            profile.setCity(city);
        }
        if (state != null) {
            
            profile.setState(state);
        }
        if (zip != null) {
            
            profile.setZip(zip);
        }
        if (country != null) {
            
            profile.setCountry(country);
        }
        
        return NNF.getBillingProfileMngr().save(profile);
    }
    
    @RequestMapping(value = "profiles", method = RequestMethod.POST)
    public @ResponseBody BillingProfile billingProfileCreate(HttpServletRequest req, HttpServletResponse resp) {
        
        final String NAME = "name";
        final String EMAIL = "email";
        
        String name = req.getParameter(NAME);
        String email = req.getParameter(EMAIL);
        if (name == null) {
            badRequest(resp, ApiContext.MISSING_PARAMETER + DELIMITER + NAME);
            return null;
        }
        if (email == null) {
            badRequest(resp, ApiContext.MISSING_PARAMETER + DELIMITER + EMAIL);
            return null;
        }
        
        BillingProfile profile = new BillingProfile();
        profile.setName(name);
        profile.setEmail(email);
        profile.setPhone(req.getParameter("phone"));
        profile.setAddr1(req.getParameter("addr1"));
        profile.setAddr2(req.getParameter("addr2"));
        profile.setCity(req.getParameter("city"));
        profile.setState(req.getParameter("state"));
        profile.setZip(req.getParameter("zip"));
        profile.setCountry(req.getParameter("country"));
        profile.setCardStatus(BillingProfile.UNKNOWN);
        
        log.info("profile name = " + profile.getName() + ", email = " + profile.getEmail());
        
        try {
            
            Date now = new Date();
            MessageDigest md = MessageDigest.getInstance("MD5");
            String seed = req.getSession() + " * " + (Math.random() * 1000)+ " * " + (now.getTime() / 1000);
            log.info("seed = " + seed);
            byte[] digest = md.digest(seed.getBytes());
            String token = NnStringUtil.bytesToHex(digest);
            log.info("token = " + token);
            profile.setToken(token);
            profile.setTokenExpDate(new Date(now.getTime() + (BillingProfile.DEFAULT_TOKEN_EXPIRATION_TIME * 1000)));
            
        } catch (NoSuchAlgorithmException e) {
            NnLogUtil.logException(e);
            this.internalError(resp);
            return null;
        }
        
        resp.setStatus(ApiContext.HTTP_201);
        return NNF.getBillingProfileMngr().save(profile);
    }
    
    @RequestMapping(value = "orders", method = RequestMethod.POST)
    public @ResponseBody List<BillingOrder> billingOrderCreate(HttpServletRequest req, HttpServletResponse resp) {
        
        final String PROFILE_ID    = "profileId";
        final String PROFILE_TOKEN = "profileToken";
        final String PACKAGE_ID    = "packageId";
        final String NO_MAIL       = "noMail";
        
        // verify packageId
        String packageIdStr = req.getParameter(PACKAGE_ID);
        if (packageIdStr == null) {
            badRequest(resp, ApiContext.MISSING_PARAMETER + DELIMITER + PACKAGE_ID);
            return null;
        }
        
        String[] packageIds = packageIdStr.split(",");
        List<BillingPackage> billingPackages = NNF.getPackageMngr().findByIds(packageIds);
        if (billingPackages == null || billingPackages.size() != packageIds.length) {
            badRequest(resp, ApiContext.INVALID_PARAMETER + DELIMITER + PACKAGE_ID);
            return null;
        }
        for (BillingPackage pack : billingPackages) {
            
            if (pack.getStatus() != BillingPackage.ONLINE) {
                badRequest(resp, ApiContext.INVALID_PARAMETER + DELIMITER + PACKAGE_ID);
                return null;
            }
        }
        
        // verify profileId / profileToken
        String profileIdStr = req.getParameter(PROFILE_ID);
        if (profileIdStr == null) {
            badRequest(resp, ApiContext.MISSING_PARAMETER + DELIMITER + PROFILE_ID);
            return null;
        }
        BillingProfile profile = NNF.getBillingProfileMngr().findById(profileIdStr);
        if (profile == null) {
            badRequest(resp, ApiContext.INVALID_PARAMETER + DELIMITER + PROFILE_ID);
            return null;
        }
        String token = req.getParameter("profileToken");
        if (token == null) {
            badRequest(resp, ApiContext.MISSING_PARAMETER + DELIMITER + PROFILE_TOKEN);
            return null;
        }
        if (!token.equals(profile.getToken())) {
            badRequest(resp, ApiContext.INVALID_PARAMETER + DELIMITER + PROFILE_TOKEN);
            return null;
        }
        Date now = new Date();
        if (profile.getTokenExpDate() == null || !now.before(profile.getTokenExpDate())) {
            badRequest(resp, ApiContext.INVALID_PARAMETER + DELIMITER + PROFILE_TOKEN);
            return null;
        }
        
        List<BillingOrder> orders = new ArrayList<BillingOrder>();
        
        for (BillingPackage pack : billingPackages) {
            orders.add(new BillingOrder(pack.getId(), profile.getId(), BillingOrder.CLEARCOMMERCE, BillingOrder.INITIAL));
        }
        
        // preauth
        if (profile.getCardStatus() < BillingProfile.AUTHED) {
            
            ApiContext context = new ApiContext(req);
            BillingService billingServ = new BillingService();
            CreditCard creditCard = null;
            
            try {
                creditCard = billingServ.checkCreditCard(context, false);
                CcApiDocument ccResult = ClearCommerceLib.preAuth(profile, creditCard, context.isProductionSite());
                profile = NNF.getBillingProfileMngr().updateAuthInfo(profile, creditCard, ccResult);
                
            } catch (CcApiBadValueException e) {
                
                log.warning(e.getMessage());
                internalError(resp);
                return null;
                
            } catch (CcApiBadKeyException e) {
                
                log.warning(e.getMessage());
                internalError(resp);
                return null;
                
            } catch (NnApiInternalErrorException e) {
                
                internalError(resp);
                return null;
                
            } catch (NnApiBadRequestException e) {
                
                badRequest(resp, e.getMessage());
                return null;
                
            } catch (NnClearCommerceException e) {
                
                log.warning(e.getMessage());
                internalError(resp);
                return null;
            }
        }
        
        if (profile.getCardStatus() < BillingProfile.AUTHED) {
            
            badRequest(resp, ApiContext.INVALID_PARAMETER + DELIMITER + "CreditCard");
            return null; 
        }
        
        orders = NNF.getOrderMngr().save(orders);
        
        if (req.getParameter(NO_MAIL) == null) {
            try {
                
                NNF.getBillingService().sendPurchaseConfirmEmail(orders);
                
            } catch (NnDataIntegrityException e) {
                NnLogUtil.logException(e);
                internalError(resp);
                return null;
            } catch (IOException e) {
                NnLogUtil.logException(e);
                internalError(resp);
                return null;
            }
        }
        
        resp.setStatus(ApiContext.HTTP_201);
        return NNF.getOrderMngr().save(orders);
    }
    
    @RequestMapping(value = "channels/{channelId}/iap_info", method = RequestMethod.POST)
    public @ResponseBody IapInfo iapInfoUpdate(HttpServletResponse resp, HttpServletRequest req,
            @PathVariable("channelId") String channelIdStr) {
        
        NnChannel channel = NNF.getChannelMngr().findById(channelIdStr);
        if (channel == null) {
            notFound(resp, ApiContext.CHANNEL_NOT_FOUND);
            return null;
        }
        
        NnUser user = ApiContext.getAuthenticatedUser(req);
        if (user == null) {
            
            unauthorized(resp);
            return null;
            
        } else if (!user.getIdStr().equals(channel.getUserIdStr())) {
            
            forbidden(resp);
            return null;
        }
        
        IapInfo iapInfo = NNF.getChPrefMngr().getIapInfo(channel.getId());
        
        // title
        String title = req.getParameter("title");
        NnChannelPref titlePref = NNF.getChPrefMngr().findByChannelIdAndItem(channel.getId(), NnChannelPref.IAP_TITLE);
        if (title != null) {
            if (titlePref != null) {
                titlePref.setValue(title);
            } else {
                titlePref = new NnChannelPref(channel.getId(), NnChannelPref.IAP_TITLE, title);
            }
            NNF.getChPrefMngr().save(titlePref);
            iapInfo.setTitle(titlePref.getValue());
        }
        
        // description
        String description = req.getParameter("description");
        NnChannelPref descPref = NNF.getChPrefMngr().findByChannelIdAndItem(channel.getId(), NnChannelPref.IAP_DESC);
        if (description != null) {
            if (descPref != null) {
                descPref.setValue(description);
            } else {
                descPref = new NnChannelPref(channel.getId(), NnChannelPref.IAP_DESC, description);
            }
            NNF.getChPrefMngr().save(descPref);
            iapInfo.setDescription(descPref.getValue());
        }
        
        // price
        String price = req.getParameter("price");
        NnChannelPref pricePref = NNF.getChPrefMngr().findByChannelIdAndItem(channel.getId(), NnChannelPref.IAP_PRICE);
        if (price != null) {
            if (pricePref != null) {
                pricePref.setValue(price);
            } else {
                pricePref = new NnChannelPref(channel.getId(), NnChannelPref.IAP_PRICE, price);
            }
            NNF.getChPrefMngr().save(pricePref);
            iapInfo.setPrice(pricePref.getValue());
        }
        
        // thumbnail
        String thumbnail = req.getParameter("thumbnail");
        NnChannelPref thumbPref = NNF.getChPrefMngr().findByChannelIdAndItem(channel.getId(), NnChannelPref.IAP_THUMB);
        if (thumbnail != null) {
            if (thumbPref != null) {
                thumbPref.setValue(thumbnail);
            } else {
                thumbPref = new NnChannelPref(channel.getId(), NnChannelPref.IAP_THUMB, thumbnail);
            }
            NNF.getChPrefMngr().save(thumbPref);
            iapInfo.setThumbnail(thumbPref.getValue());
        }
        
        return iapInfo;
    }
    
    @RequestMapping(value = "channels/{channelId}/iap_items", method = RequestMethod.POST)
    public @ResponseBody void itemsCreate(HttpServletResponse resp, HttpServletRequest req,
            @PathVariable("channelId") String channelIdStr) {
        
        NnChannel channel = NNF.getChannelMngr().findById(channelIdStr);
        if (channel == null) {
            notFound(resp, ApiContext.CHANNEL_NOT_FOUND);
            return;
        }
        
        NnUser user = ApiContext.getAuthenticatedUser(req);
        if (user == null) {
            
            unauthorized(resp);
            return;
            
        } else if (!user.getIdStr().equals(channel.getUserIdStr())) {
            
            forbidden(resp);
            return;
        }
        
        String msoIdStr = req.getParameter("msoId");
        if (msoIdStr == null) {
            
            badRequest(resp, ApiContext.MISSING_PARAMETER);
            return;
        }
        Mso mso = NNF.getMsoMngr().findById(msoIdStr);
        if (mso == null) {
            
            badRequest(resp, ApiContext.MSO_NOT_FOUND);
            return;
        }
        
        NnItem appstoreItem = NNF.getItemMngr().findOne(mso, channel, ApiContext.OS_IOS);
        if (appstoreItem == null) {
            
            appstoreItem = new NnItem(mso.getId(), channel.getId(), NnItem.APPSTORE);
            NNF.getItemMngr().save(appstoreItem);
            
            try {
                NNF.getBillingService().sendItemCreationEmail(appstoreItem);
            } catch (IOException e) {
                log.warning("fail to send notification mail");
            }
        }
        
        NnItem googleplayItem = NNF.getItemMngr().findOne(mso, channel, ApiContext.OS_ANDROID);
        if (googleplayItem == null) {
            
            googleplayItem = new NnItem(mso.getId(), channel.getId(), NnItem.GOOGLEPLAY);
            NNF.getItemMngr().save(googleplayItem);
        }
        
        msgResponse(resp, ApiContext.OK);
    }
    
    @RequestMapping(value = "channels/{channelId}/iap_items", method = RequestMethod.GET)
    public @ResponseBody List<NnItem> items(HttpServletResponse resp, HttpServletRequest req,
            @PathVariable("channelId") String channelIdStr) {
        
        NnChannel channel = NNF.getChannelMngr().findById(channelIdStr);
        if (channel == null) {
            notFound(resp, ApiContext.CHANNEL_NOT_FOUND);
            return null;
        }
        
        return NNF.getItemMngr().findByChannelId(channel.getId());
    }
    
    @RequestMapping(value = "channels/{channelId}/iap_info", method = RequestMethod.GET)
    public @ResponseBody IapInfo iapInfo(HttpServletResponse resp, HttpServletRequest req,
            @PathVariable("channelId") String channelIdStr) {
        
        NnChannel channel = NNF.getChannelMngr().findById(channelIdStr);
        if (channel == null) {
            notFound(resp, ApiContext.CHANNEL_NOT_FOUND);
            return null;
        }
        
        return NNF.getChPrefMngr().getIapInfo(channel.getId());
    }
}
