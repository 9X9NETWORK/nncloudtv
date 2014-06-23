package com.nncloudtv.service;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

import com.clearcommerce.ccxclientapi.CcApiBadKeyException;
import com.clearcommerce.ccxclientapi.CcApiBadValueException;
import com.clearcommerce.ccxclientapi.CcApiDocument;
import com.clearcommerce.ccxclientapi.CcApiRecord;
import com.nncloudtv.exception.NnApiBadRequestException;
import com.nncloudtv.exception.NnApiInternalErrorException;
import com.nncloudtv.exception.NnClearCommerceException;
import com.nncloudtv.lib.ClearCommerceLib;
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
}
