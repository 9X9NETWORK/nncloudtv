package com.nncloudtv.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import com.clearcommerce.ccxclientapi.CcApiBadKeyException;
import com.clearcommerce.ccxclientapi.CcApiBadValueException;
import com.clearcommerce.ccxclientapi.CcApiDocument;
import com.clearcommerce.ccxclientapi.CcApiRecord;
import com.nncloudtv.exception.NnApiBadRequestException;
import com.nncloudtv.exception.NnApiInternalErrorException;
import com.nncloudtv.exception.NnClearCommerceException;
import com.nncloudtv.exception.NnDataIntegrityException;
import com.nncloudtv.lib.ClearCommerceLib;
import com.nncloudtv.model.BillingOrder;
import com.nncloudtv.model.BillingPackage;
import com.nncloudtv.model.BillingProfile;
import com.nncloudtv.model.NnEmail;
import com.nncloudtv.web.api.ApiContext;
import com.nncloudtv.web.api.ApiGeneric;
import com.nncloudtv.web.json.cms.CreditCard;

@Service
public class BillingService {
    
    protected static final Logger   log = Logger.getLogger(BillingService.class.getName());
    
    protected BillingOrderManager   orderMngr;
    protected BillingProfileManager profileMngr;
    protected BillingPackageManager packageMngr;
    
    public BillingService() {
        
        orderMngr = new BillingOrderManager();
        profileMngr = new BillingProfileManager();
        packageMngr = new BillingPackageManager();
    }
    
    public CreditCard checkCreditCard(ApiContext context, boolean verify) throws NnApiInternalErrorException,
            NnApiBadRequestException, CcApiBadKeyException, NnClearCommerceException, CcApiBadValueException {
        
        final String DELIMITER = " - ";
        
        final String CARD_HOLDER_NAME = "cardHolderName";
        final String CARD_NUMBER      = "cardNumber";
        final String CARD_EXPIRES     = "cardExpires";
        final String CARD_TYPE        = "cardType";
        final String CARD_VERIFICATION_CODE = "cardVerificationCode";
        
        HttpServletRequest req = context.getHttpRequest();
        
        String cardHolderName       = req.getParameter(CARD_HOLDER_NAME);
        String cardNumber           = req.getParameter(CARD_NUMBER);
        String cardExpires          = req.getParameter(CARD_EXPIRES);
        String cardVerificationCode = req.getParameter(CARD_VERIFICATION_CODE);
        String cardTypeStr          = req.getParameter(CARD_TYPE);
        
        if (cardTypeStr != null && !cardTypeStr.matches("\\d+"))
            throw new NnApiBadRequestException(ApiGeneric.INVALID_PARAMETER + DELIMITER + CARD_TYPE);
        if (cardNumber == null)
            throw new NnApiBadRequestException(ApiGeneric.MISSING_PARAMETER + DELIMITER + CARD_NUMBER);
        cardNumber = cardNumber.replaceAll("-", "");
        if (cardExpires == null)
            throw new NnApiBadRequestException(ApiGeneric.MISSING_PARAMETER + DELIMITER + CARD_EXPIRES);
        if (!cardNumber.matches("\\d{15,16}"))
            throw new NnApiBadRequestException(ApiGeneric.INVALID_PARAMETER + DELIMITER + CARD_NUMBER);
        if (!cardExpires.matches("\\d\\d\\/\\d\\d"))
            throw new NnApiBadRequestException(ApiGeneric.INVALID_PARAMETER + DELIMITER + CARD_EXPIRES);
        if (cardVerificationCode != null && !cardVerificationCode.matches("\\d{3,4}"))
            throw new NnApiBadRequestException(ApiGeneric.INVALID_PARAMETER + DELIMITER + CARD_VERIFICATION_CODE);
        CreditCard creditCard = new CreditCard(cardNumber, cardHolderName, cardExpires, cardVerificationCode);
        if (cardTypeStr != null) {
            creditCard.setCardType(Short.valueOf(cardTypeStr));
        }
        
        if (verify) {
            CcApiDocument ccResult = ClearCommerceLib.verifyCreditCardNumber(creditCard, context.isProductionSite());
            CcApiRecord ccOverview = ClearCommerceLib.getOverview(ccResult);
            String txnStatus = ccOverview.getFieldString("TransactionStatus");
            if ("A".equals(txnStatus) == false)
                throw new NnApiBadRequestException(ApiGeneric.INVALID_PARAMETER + DELIMITER + CARD_VERIFICATION_CODE);
        }
        
        return creditCard;
    }
    
    public void sendPurchaseConfirmEmail(List<BillingOrder> orders) throws NnDataIntegrityException, IOException {
        
        final String subject = "[FLIPr.tv] Welcome to FLIPr.tv %s! Let's get started.";
        /**
        final String purchaseConfirmEmailContent = "Dear %s,<br><br>Thanks for signing for FLIPr. This is confirmation that you have purchase:<br><br>"
                                                 + "<table><tr><th width='300' align='center'>Item</th><th width='100'>Unit Price</th><th width='100'>VAT Rate</th><th widh='100'>Total Cost</th></tr>%s<tr><td colspan='4' align='right'></td></tr></table><br><br>"
                                                 + "Your Visa ending in %s will be charged $%.2f a month as a recurring transaction after your app be ready for sale in app store.<br><br>"
                                                 + "Sincerely,<br>The FLIPr Team<br>www.FLIPr.tv";
        final String purchaseOrderRow            = "<tr><td>%s</td><td>$.2f</td><td>%d</td></tr>";
        **/
        
        if (orders == null || orders.isEmpty()) return;
        
        List<Long> ids = new ArrayList<Long>();
        for (BillingOrder order : orders) {
            ids.add(order.getId());
        }
        orders = orderMngr.findByIds(ids);
        ids.clear();
        if (orders.isEmpty())
            throw new NnDataIntegrityException("can not find those order IDs - " + StringUtils.join(ids, ','));
        BillingProfile profile = profileMngr.findById(orders.get(0).getId());
        if (profile == null)
            throw new NnDataIntegrityException("can not find billingProfile which ID = " + orders.get(0).getId());
        for (BillingOrder order : orders) {
            if (profile.getId() != order.getProfileId())
                throw new NnDataIntegrityException(String.format("expecting order %d which profileId is %d", order.getId(), profile.getId()));
            ids.add(order.getPackageId());
        }
        List<BillingPackage> packages = new ArrayList<BillingPackage>();
        packages = packageMngr.findByIds(ids);
        
        String content = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("purchase_confirm.html"));
        
        float  totalPrice   = 0;
        for (int i = 0; i < packages.size(); i++) {
            
            float price = (((float) packages.get(i).getPrice()) / 100);
            totalPrice += price;
            
            if (i == 0) {
                content.replaceAll("{{item" + (i+1) + "}}", packages.get(i).getName() + " and Chromecast app");
            } else {
                content.replaceAll("{{item" + (i+1) + "}}", packages.get(i).getName());
            }
            content.replaceAll("{{price" + (i+1) + "}}", String.format("$%.3f", price));
            content.replaceAll("{{vat" + (i+1) + "}}", "0%");
        }
        content.replaceAll("{{user_name}}", profile.getName());
        content.replaceAll("{{card}}", profile.getCardRemainDigits());
        content.replaceAll("{{total}}", String.format("$%.2f", totalPrice));
        
        EmailService emailServ = new EmailService();
        NnEmail email = new NnEmail(profile.getEmail(),
                                    profile.getName(),
                                    "vidcon2014@flipr.tv",
                                    "FLIPr",
                                    NnEmail.SEND_EMAIL_NOREPLY,
                                    String.format(subject, profile.getName()),
                                    content);
        email.setHtml(true);
        emailServ.sendEmail(email, null, null);
    }
}
