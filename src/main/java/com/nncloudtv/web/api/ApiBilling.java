package com.nncloudtv.web.api;

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
import com.clearcommerce.ccxclientapi.CcApiDocument;
import com.clearcommerce.ccxclientapi.CcApiRecord;
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
    
    static final String DELIMITER = " - ";
    
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
        
        final String TOKEN = "token";
        
        BillingProfile profile = profileMngr.findById(profileIdStr);
        if (profile == null) {
            notFound(resp);
            return null;
        }
        String token = req.getParameter(TOKEN);
        if (token == null) {
            badRequest(resp, MISSING_PARAMETER + DELIMITER + TOKEN);
            return null;
        }
        if (!token.equals(profile.getToken())) {
            badRequest(resp, INVALID_PARAMETER + DELIMITER + TOKEN);
            return null;
        }
        Date now = new Date();
        if (profile.getTokenExpDate() == null || !now.before(profile.getTokenExpDate())) {
            badRequest(resp, INVALID_PARAMETER + DELIMITER + TOKEN);
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
        
        return profileMngr.save(profile);
    }
    
    private CreditCard checkCreditCard(HttpServletRequest req, HttpServletResponse resp, BillingProfile profile) {
        
        final String CARD_HOLDER_NAME = "cardHolderName";
        final String CARD_NUMBER      = "cardNumber";
        final String CARD_EXPIRES     = "cardExpires";
        final String CARD_VERIFICATION_CODE = "cardVerificationCode";
        
        String cardHolderName = req.getParameter(CARD_HOLDER_NAME);
        String cardNumber     = req.getParameter(CARD_NUMBER).replaceAll("-", "");
        String cardExpires    = req.getParameter(CARD_EXPIRES);
        String cardVerificationCode = req.getParameter(CARD_VERIFICATION_CODE);
        
        if (cardHolderName == null || cardHolderName.isEmpty()) {
            badRequest(resp, MISSING_PARAMETER + DELIMITER + CARD_HOLDER_NAME);
            return null;
        }
        if (cardNumber == null) {
            badRequest(resp, MISSING_PARAMETER + DELIMITER + CARD_NUMBER);
            return null;
        }
        if (cardExpires == null) {
            badRequest(resp, MISSING_PARAMETER + DELIMITER + CARD_EXPIRES);
            return null;
        }
        if (cardVerificationCode == null) {
            badRequest(resp, MISSING_PARAMETER + DELIMITER + CARD_VERIFICATION_CODE);
            return null;
        }
        if (!cardNumber.matches("\\d{16}")) {
            badRequest(resp, INVALID_PARAMETER + DELIMITER + CARD_NUMBER);
            return null;
        }
        if (!cardExpires.matches("\\d\\d\\/\\d\\d")) {
            badRequest(resp, INVALID_PARAMETER + DELIMITER + CARD_EXPIRES);
            return null;
        }
        if (!cardVerificationCode.matches("\\d{3,4}")) {
            badRequest(resp, INVALID_PARAMETER + DELIMITER + CARD_VERIFICATION_CODE);
            return null;
        }
        CreditCard creditCard = new CreditCard(cardNumber, cardHolderName, cardExpires, cardVerificationCode);
        
        if (profile != null) {
            
            CcApiDocument ccResult = ClearCommerceLib.verifyCreditCardNumber(creditCard, profile);
            if (ccResult == null) {
                log.warning("ccResult is empty");
                internalError(resp);
                return null;
            }
            CcApiRecord ccOverview = ClearCommerceLib.getOverview(ccResult);
            if (ccOverview == null) {
                log.warning("ccOverview is empty");
                internalError(resp);
                return null;
            }
            try {
                String txnStatus = ccOverview.getFieldString("TransactionStatus");
                if (txnStatus == null || !txnStatus.equals("A")) {
                    badRequest(resp, INVALID_PARAMETER + DELIMITER + CARD_VERIFICATION_CODE);
                    return null;
                }
            } catch (CcApiBadKeyException e) {
                log.warning("TransactionStatus is empty");
                internalError(resp);
                return null;
            }
        }
        
        return creditCard;
    }
    
    @RequestMapping(value = "profiles", method = RequestMethod.POST)
    public @ResponseBody BillingProfile billingProfileCreate(HttpServletRequest req, HttpServletResponse resp) {
        
        /*
        String[] parameters = {
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
        */
        
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
        
        /*
        CreditCard creditCard = checkCreditCard(req, resp, profile);
        if (creditCard == null) {
            return null;
        }
        
        profile.setCardStatus(BillingProfile.VERIFIED);
        profile.setCardHolderName(creditCard.getCardHolderName());
        profile.setCardRemainDigits(creditCard.getCardNumber().substring(creditCard.getCardHolderName().length() - 4));
        */
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
    public @ResponseBody List<BillingOrder> billingOrderCreate(HttpServletRequest req, HttpServletResponse resp) {
        
        final String PROFILE_ID    = "profileId";
        final String PROFILE_TOKEN = "profileToken";
        final String PACKAGE_ID    = "packageId";
        
        // verify packageId
        String packageIdStr = req.getParameter(PACKAGE_ID);
        if (packageIdStr == null) {
            badRequest(resp, MISSING_PARAMETER + DELIMITER + PACKAGE_ID);
            return null;
        }
        
        String[] packageIds = packageIdStr.split(",");
        List<BillingPackage> billingPackages = packageMngr.findByIds(packageIds);
        if (billingPackages == null || billingPackages.size() != packageIds.length) {
            badRequest(resp, INVALID_PARAMETER + DELIMITER + PACKAGE_ID);
            return null;
        }
        for (BillingPackage pack : billingPackages) {
            
            if (pack.getStatus() != BillingPackage.ONLINE) {
                badRequest(resp, INVALID_PARAMETER + DELIMITER + PACKAGE_ID);
                return null;
            }
        }
        
        // verify profileId / profileToken
        String profileIdStr = req.getParameter(PROFILE_ID);
        if (profileIdStr == null) {
            badRequest(resp, MISSING_PARAMETER + DELIMITER + PROFILE_ID);
            return null;
        }
        BillingProfile profile = profileMngr.findById(profileIdStr);
        if (profile == null) {
            badRequest(resp, INVALID_PARAMETER + DELIMITER + PROFILE_ID);
            return null;
        }
        String token = req.getParameter("profileToken");
        if (token == null) {
            badRequest(resp, MISSING_PARAMETER + DELIMITER + PROFILE_TOKEN);
            return null;
        }
        if (!token.equals(profile.getToken())) {
            badRequest(resp, INVALID_PARAMETER + DELIMITER + PROFILE_TOKEN);
            return null;
        }
        Date now = new Date();
        if (profile.getTokenExpDate() == null || !now.before(profile.getTokenExpDate())) {
            badRequest(resp, INVALID_PARAMETER + DELIMITER + PROFILE_TOKEN);
            return null;
        }
        
        List<BillingOrder> orders = new ArrayList<BillingOrder>();
        
        for (BillingPackage pack : billingPackages) {
            orders.add(new BillingOrder(pack.getId(), profile.getId(), BillingOrder.CLEARCOMMERCE, BillingOrder.INITIAL));
        }
        
        CreditCard creditCard = checkCreditCard(req, resp, null);
        if (creditCard == null) {
            return null;
        }
        
        // preauth
        if (profile.getCardStatus() < BillingProfile.AUTHED) {
            
            CcApiDocument ccResult = ClearCommerceLib.preAuth(profile, creditCard);
            if (ccResult == null) {
                log.warning("ccResult is empty");
                internalError(resp);
                return null;
            }
            CcApiRecord ccOverview = ClearCommerceLib.getOverview(ccResult);
            if (ccOverview == null) {
                log.warning("ccOverview is empty");
                internalError(resp);
                return null;
            }
            try {
                String txnStatus = ccOverview.getFieldString("TransactionStatus");
                if (txnStatus == null || !txnStatus.equals("A")) {
                    badRequest(resp, INVALID_PARAMETER + DELIMITER + "CreditCard");
                    return null;
                }
            } catch (CcApiBadKeyException e) {
                log.warning("TransactionStatus is empty");
                internalError(resp);
                return null;
            }
            profileMngr.updateCreditCardInfo(profile, creditCard, BillingProfile.AUTHED);
        }
        
        return orderMngr.save(orders);
    }
    
}
