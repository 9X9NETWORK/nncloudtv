package com.nncloudtv.web.api;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

import com.nncloudtv.lib.ClearCommerceLib;
import com.nncloudtv.lib.NnLogUtil;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.model.BillingOrder;
import com.nncloudtv.model.BillingPackage;
import com.nncloudtv.model.BillingProfile;
import com.nncloudtv.service.BillingOrderManager;
import com.nncloudtv.service.BillingPackageManager;
import com.nncloudtv.service.BillingProfileManager;
import com.nncloudtv.web.json.cms.CreditCard;

@Controller
@RequestMapping("api/billing")
public class ApiBilling extends ApiGeneric {
    
    protected static Logger log = Logger.getLogger(ApiBilling.class.getName());
    
    private BillingPackageManager packageMngr;
    private BillingProfileManager profileMngr;
    private BillingOrderManager   orderMngr;
    
    public ApiBilling() {
        
        packageMngr = new BillingPackageManager();
        profileMngr = new BillingProfileManager();
        orderMngr   = new BillingOrderManager();
    }
    
    @RequestMapping(value = "packages", method = RequestMethod.GET)
    public @ResponseBody List<BillingPackage> login(HttpServletRequest req, HttpServletResponse resp) {
        
        return packageMngr.findAll();
    }
    
    @RequestMapping(value = "profiles/{profileId}", method = RequestMethod.PUT)
    public @ResponseBody BillingProfile billingProfileUpdate(HttpServletRequest req, HttpServletResponse resp, @PathVariable("profileId") String profileIdStr) {
        
        BillingProfile profile = profileMngr.findById(profileIdStr);
        if (profile == null) {
            notFound(resp);
            return null;
        }
        String token = req.getParameter("token");
        if (token == null) {
            badRequest(resp, MISSING_PARAMETER + " - token");
            return null;
        }
        if (!token.equals(profile.getToken())) {
            badRequest(resp, INVALID_PARAMETER + " - token");
            return null;
        }
        Date now = new Date();
        if (profile.getTokenExpDate() == null || !now.before(profile.getTokenExpDate())) {
            badRequest(resp, INVALID_PARAMETER + " - token expired");
            return null;
        }
        
        String cardHolderName = req.getParameter("cardHolderName");
        String cardNumber     = req.getParameter("cardNumber").replaceAll("-", "");
        String cardExpires    = req.getParameter("cardExpires");
        String cardVerificationCode = req.getParameter("cardVerificationCode");
        
        if (cardHolderName != null && cardNumber != null && cardExpires != null && cardVerificationCode != null) {
            
            if (cardHolderName.isEmpty()) {
                badRequest(resp, INVALID_PARAMETER + " - cardHolderName is empty");
                return null;
            }
            if (!cardNumber.matches("\\d{16}")) {
                badRequest(resp, INVALID_PARAMETER + " - cardNumber length");
                return null;
            }
            if (!cardExpires.matches("\\d\\d\\/\\d\\d")) {
                badRequest(resp, INVALID_PARAMETER + " - cardExpires format");
                return null;
            }
            if (!cardVerificationCode.matches("\\d{3,4}")) {
                badRequest(resp, INVALID_PARAMETER + " - cardVerificationCode");
                return null;
            }
            CreditCard creditCard = new CreditCard(cardNumber, cardHolderName, cardExpires, cardVerificationCode);
            
            // Qoo: to verify credit card
            ClearCommerceLib.verifyCreditCardNumber(creditCard);
            profile.setCardStatus(BillingProfile.VERIFIED);
            profile.setCardHolderName(cardHolderName);
            profile.setCardRemainDigits(cardNumber.substring(cardNumber.length() - 4));
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
        
        return profileMngr.save(profile);
    }
    
    @RequestMapping(value = "profiles", method = RequestMethod.POST)
    public @ResponseBody BillingProfile billingProfileCreate(HttpServletRequest req, HttpServletResponse resp) {
        
        String[] parameters = {
                "cardHolderName",
                "cardNumber",
                "cardExpires",
                "cardVerificationCode",
                "name",
                "email",
                "phone",
                "addr1",
                "city",
                "state",
                "zip",
                "country"};
        for (String param : parameters) {
            
            if (req.getParameter(param) == null) {
                
                this.badRequest(resp, MISSING_PARAMETER + " - " + param);
                return null;
            }
        }
        
        BillingProfile profile = new BillingProfile();
        profile.setName(req.getParameter("name"));
        profile.setEmail(req.getParameter("email"));
        profile.setPhone(req.getParameter("phone"));
        profile.setAddr2(req.getParameter("addr1"));
        profile.setAddr2(req.getParameter("addr2"));
        profile.setCity(req.getParameter("city"));
        profile.setState(req.getParameter("state"));
        profile.setZip(req.getParameter("zip"));
        profile.setCountry(req.getParameter("country"));
        profile.setCardStatus(BillingProfile.UNKNOWN);
        
        String cardHolderName = req.getParameter("cardHolderName");
        String cardNumber     = req.getParameter("cardNumber").replaceAll("-", "");
        String cardExpires    = req.getParameter("cardExpires");
        String cardVerificationCode = req.getParameter("cardVerificationCode");
        if (cardHolderName.isEmpty()) {
            badRequest(resp, INVALID_PARAMETER + " - cardHolderName is empty");
            return null;
        }
        if (!cardNumber.matches("\\d{16}")) {
            badRequest(resp, INVALID_PARAMETER + " - cardNumber length");
            return null;
        }
        if (!cardExpires.matches("\\d\\d\\/\\d\\d")) {
            badRequest(resp, INVALID_PARAMETER + " - cardExpires format");
            return null;
        }
        if (!cardVerificationCode.matches("\\d{3,4}")) {
            badRequest(resp, INVALID_PARAMETER + " - cardVerificationCode");
            return null;
        }
        CreditCard creditCard = new CreditCard(cardNumber, cardHolderName, cardExpires, cardVerificationCode);
        
        // Qoo: to verify credit card
        ClearCommerceLib.verifyCreditCardNumber(creditCard);
        profile.setCardStatus(BillingProfile.VERIFIED);
        profile.setCardHolderName(cardHolderName);
        profile.setCardRemainDigits(cardNumber.substring(cardNumber.length() - 4));
        
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
        
        return profileMngr.save(profile);
    }
    
    @RequestMapping(value = "orders", method = RequestMethod.POST)
    public @ResponseBody BillingOrder billingOrderCreate(HttpServletRequest req, HttpServletResponse resp) {
        
        // verify packageId
        String packageIdStr = req.getParameter("packageId");
        if (packageIdStr == null) {
            badRequest(resp, MISSING_PARAMETER + " - packageId");
            return null;
        }
        BillingPackage billingPackage = packageMngr.findById(packageIdStr);
        if (billingPackage == null) {
            badRequest(resp, INVALID_PARAMETER + " - package not found");
            return null;
        }
        if (billingPackage.getStatus() != BillingPackage.ONLINE) {
            badRequest(resp, INVALID_PARAMETER + " - package is not serving");
            return null;
        }
        
        // verify profileId / profileToken
        String profileIdStr = req.getParameter("profileId");
        if (profileIdStr == null) {
            badRequest(resp, MISSING_PARAMETER + " - profileId");
            return null;
        }
        BillingProfile profile = profileMngr.findById(profileIdStr);
        if (profile == null) {
            badRequest(resp, INVALID_PARAMETER + " - profile not found");
            return null;
        }
        if (profile.getCardStatus() <= 0) {
            badRequest(resp, INVALID_PARAMETER + " - profile is not verified");
            return null;
        }
        String token = req.getParameter("profileToken");
        if (token == null) {
            badRequest(resp, MISSING_PARAMETER + " - profileToken");
            return null;
        }
        if (!token.equals(profile.getToken())) {
            badRequest(resp, INVALID_PARAMETER + " - profileToken");
            return null;
        }
        Date now = new Date();
        if (profile.getTokenExpDate() == null || !now.before(profile.getTokenExpDate())) {
            badRequest(resp, INVALID_PARAMETER + " - profileToken expired");
            return null;
        }
        
        BillingOrder order = new BillingOrder(billingPackage.getId(), profile.getId(), BillingOrder.CLEARCOMMERCE);
        order.setStatus(BillingOrder.VERIFIED);
        orderMngr.save(order);
        
        // Qoo: preauth
        ClearCommerceLib.preAuth(order);
        order.setStatus(BillingOrder.PREAUTHED);
        
        return orderMngr.save(order);
    }
    
}
