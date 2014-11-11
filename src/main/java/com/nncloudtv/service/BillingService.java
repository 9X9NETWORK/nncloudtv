package com.nncloudtv.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;

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
import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.model.BillingOrder;
import com.nncloudtv.model.BillingPackage;
import com.nncloudtv.model.BillingProfile;
import com.nncloudtv.model.NnEmail;
import com.nncloudtv.model.NnItem;
import com.nncloudtv.web.api.ApiContext;
import com.nncloudtv.web.api.ApiGeneric;
import com.nncloudtv.web.json.cms.CreditCard;
import com.nncloudtv.web.json.cms.IapInfo;

@Service
public class BillingService {
    
    protected static final Logger log = Logger.getLogger(BillingService.class.getName());
    
    public static final String PREFIX  = "\\{\\{";
    public static final String POSTFIX = "\\}\\}";
    
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
        
        if (orders == null || orders.isEmpty()) return;
        
        List<Long> ids = new ArrayList<Long>();
        for (BillingOrder order : orders) {
            ids.add(order.getId());
        }
        orders = NNF.getOrderMngr().findByIds(ids);
        ids.clear();
        if (orders.isEmpty())
            throw new NnDataIntegrityException("can not find those order IDs - " + StringUtils.join(ids, ','));
        BillingProfile profile = NNF.getBillingProfileMngr().findById(orders.get(0).getProfileId());
        if (profile == null)
            throw new NnDataIntegrityException("can not find billingProfile " + orders.get(0).getId());
        for (BillingOrder order : orders) {
            if (profile.getId() != order.getProfileId())
                throw new NnDataIntegrityException(String.format("expecting order %d which profileId is %d, but now it's %d", order.getId(), profile.getId(), order.getProfileId()));
            ids.add(order.getPackageId());
        }
        List<BillingPackage> packages = new ArrayList<BillingPackage>();
        packages = NNF.getPackageMngr().findByIds(ids);
        if (packages.isEmpty()) {
            log.warning("no packages was found");
            return;
        }
        
        String content = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("purchase_confirm_mail.html"), NnStringUtil.UTF8);
        
        float  totalPrice   = 0;
        for (int i = 0; i < packages.size(); i++) {
            
            float price = (((float) packages.get(i).getPrice()) / 100);
            totalPrice += price;
            
            String num = String.valueOf(i + 1);
            String itemName = packages.get(i).getName();
            if (i == 0) {
                itemName += " and Chromecast app";
            }
            content = content.replaceAll(PREFIX + "item"  + num + POSTFIX, itemName);
            content = content.replaceAll(PREFIX + "price" + num + POSTFIX, Matcher.quoteReplacement(String.format("$%.2f", price)));
        }
        content = content.replaceAll(PREFIX + "item2"  + POSTFIX, "");
        content = content.replaceAll(PREFIX + "price2" + POSTFIX, "");
        content = content.replaceAll(PREFIX + "user"   + POSTFIX, profile.getName());
        content = content.replaceAll(PREFIX + "card"   + POSTFIX, profile.getCardRemainDigits());
        content = content.replaceAll(PREFIX + "total"  + POSTFIX, Matcher.quoteReplacement(String.format("$%.2f", totalPrice)));
        
        EmailService emailService = NNF.getEmailService();
        NnEmail email = new NnEmail(profile.getEmail(),
                                    profile.getName(),
                                    NnEmail.SEND_EMAIL_VIDCON2014,
                                    NnEmail.SEND_NAME_FLIPR,
                                    NnEmail.REPLY_EMAIL_NOREPLY,
                                    String.format(subject, profile.getName()),
                                    content);
        email.setHtml(true);
        emailService.sendEmail(email, NnEmail.SEND_EMAIL_NNCLOUDTV, NnEmail.SEND_NAME_NNCLOUDTV);
        log.info("sent purchase confirm mail to " + profile.getEmail());
    }
    
    public void sendItemCreationEmail(NnItem item) throws IOException {
        
        IapInfo iapInfo = NNF.getChPrefMngr().getIapInfo(item.getChannelId());
        
        String subject = "Need to add paid program";
        String content = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("item_creation_mail.html"), NnStringUtil.UTF8);
        content = content.replaceAll(PREFIX + "msoId"       + POSTFIX, String.valueOf(item.getMsoId()));
        content = content.replaceAll(PREFIX + "channelId"   + POSTFIX, String.valueOf(item.getChannelId()));
        content = content.replaceAll(PREFIX + "title"       + POSTFIX, String.valueOf(iapInfo.getTitle()));
        content = content.replaceAll(PREFIX + "price"       + POSTFIX, String.valueOf(iapInfo.getPrice()));
        content = content.replaceAll(PREFIX + "description" + POSTFIX, String.valueOf(iapInfo.getDescription()));
        content = content.replaceAll(PREFIX + "image"       + POSTFIX, String.valueOf(iapInfo.getThumbnail()));
        
        NnEmail email = new NnEmail(NnEmail.TO_EMAIL_PAIDSUPPORT,
                                    NnEmail.TO_NAME_PAIDSUPPORT,
                                    NnEmail.SEND_EMAIL_CMS,
                                    NnEmail.SEND_NAME_CMS,
                                    NnEmail.REPLY_EMAIL_NOREPLY,
                                    subject,
                                    content);
        email.setHtml(true);
        NNF.getEmailService().sendEmail(email, NnEmail.SEND_EMAIL_NNCLOUDTV, NnEmail.SEND_NAME_NNCLOUDTV);
        log.info("sent notification mail to " + email.getToEmail());
    }
}
