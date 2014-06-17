package com.nncloudtv.lib;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.logging.Logger;

import com.clearcommerce.ccxclientapi.CcApiBadHostException;
import com.clearcommerce.ccxclientapi.CcApiBadKeyException;
import com.clearcommerce.ccxclientapi.CcApiBadPortException;
import com.clearcommerce.ccxclientapi.CcApiBadValueException;
import com.clearcommerce.ccxclientapi.CcApiDocument;
import com.clearcommerce.ccxclientapi.CcApiInvalidDocumentException;
import com.clearcommerce.ccxclientapi.CcApiMoney;
import com.clearcommerce.ccxclientapi.CcApiProcessException;
import com.clearcommerce.ccxclientapi.CcApiRecord;
import com.clearcommerce.ccxclientapi.CcApiServerConnectException;
import com.clearcommerce.ccxclientapi.CcApiWriterException;
import com.nncloudtv.model.BillingProfile;
import com.nncloudtv.service.MsoConfigManager;
import com.nncloudtv.web.json.cms.CreditCard;

public class ClearCommerceLib {
    
    protected static final Logger log = Logger.getLogger(ClearCommerceLib.class.getName());
    
    static final short CC_PORT = 12000; 
    static final String FIRST_TIME_PREAUTH_CHARGE = "100"; // in cent
    
    static final String USD              = "840";
    static final String DOC_VERSION      = "1.0";
    static final String ORDER_FORM_DOC   = "OrderFormDoc";
    static final String PAYMENT_NO_FRAUD = "PaymentNoFraud";
    static final String CREDIT_CARD      = "CreditCard";
    
    static final String CC_ENGINE_DOC   = "EngineDoc";
    static final String CC_OVERVIEW     = "Overview";
    static final String CC_MESSAGE_LIST = "MessageList";
    
    private static CcApiRecord populateCCUserField(CcApiRecord ccRecord) throws CcApiBadKeyException, CcApiBadValueException {
        
        Integer ccClientId = Integer.parseInt(MsoConfigManager.getCCClientId());
        
        CcApiRecord ccUser = ccRecord.addRecord("User");
        ccUser.setFieldString("Name", MsoConfigManager.getCCUserName());
        ccUser.setFieldString("Password", MsoConfigManager.getCCPassword());
        ccUser.setFieldS32("ClientId", ccClientId);
        ccUser.setFieldS32("EffectiveClientId", ccClientId);
        
        return ccRecord;
    }
    
    public static CcApiDocument verifyCreditCardNumber(CreditCard creditCard, BillingProfile profile) {
        
        CcApiDocument ccResult = null;
        
        try {
            CcApiDocument ccDoc = new CcApiDocument();
            ccDoc.setFieldString("DocVersion", DOC_VERSION);
            
            CcApiRecord ccEngine = populateCCUserField(ccDoc.addRecord(CC_ENGINE_DOC));
            ccEngine.addRecord("Instructions").setFieldString("Pipeline", PAYMENT_NO_FRAUD);
            ccEngine.setFieldString("ContentType", ORDER_FORM_DOC);
            
            CcApiRecord ccOrderForm = ccEngine.addRecord(ORDER_FORM_DOC);
            ccOrderForm.setFieldString("Mode", "P"); // "P" is for Production Mode
            
            CcApiRecord ccCunsumer = ccOrderForm.addRecord("Consumer");
            CcApiRecord ccPaymentMech = ccCunsumer.addRecord("PaymentMech");
            CcApiRecord ccBillTo = ccCunsumer.addRecord("BillTo");
            
            ccPaymentMech.setFieldString("Type", CREDIT_CARD);
            CcApiRecord ccCreditCard = ccPaymentMech.addRecord(CREDIT_CARD);
            //ccCreditCard.setFieldS32("Type", 1);
            ccCreditCard.setFieldS32("Cvv2Indicator", 1);
            ccCreditCard.setFieldString("Cvv2Val", creditCard.getVeridicationCode());
            ccCreditCard.setFieldString("Number", creditCard.getCardNumber());
            ccCreditCard.setFieldExpirationDate("Expires", creditCard.getExpires());
            
            CcApiRecord ccLocation = ccBillTo.addRecord("Location");
            CcApiRecord ccAddress = ccLocation.addRecord("Address");
            ccAddress.setFieldString("Street1", profile.getAddr1());
            ccAddress.setFieldString("PostalCode", profile.getZip());
            
            CcApiRecord ccTransaction = ccOrderForm.addRecord("Transaction");
            ccTransaction.setFieldString("Type", "PreAuth");
            
            ccResult = process(ccDoc);
            
        } catch (CcApiBadKeyException e) {
            NnLogUtil.logException(e);
            return null;
        } catch (CcApiBadValueException e) {
            NnLogUtil.logException(e);
            return null;
        }
        
        return ccResult;
    }
    
    public static CcApiDocument preAuth(BillingProfile profile, CreditCard creditCard) {
        
        CcApiDocument ccResult = null;
        
        try {
            CcApiDocument ccDoc = new CcApiDocument();
            ccDoc.setFieldString("DocVersion", DOC_VERSION);
            
            CcApiRecord ccEngine = populateCCUserField(ccDoc.addRecord(CC_ENGINE_DOC));
            ccEngine.addRecord("Instructions").setFieldString("Pipeline", PAYMENT_NO_FRAUD);
            ccEngine.setFieldString("ContentType", ORDER_FORM_DOC);
            ccEngine.setFieldString("SourceId", String.valueOf(profile.getId()));
            
            CcApiRecord ccOrderForm = ccEngine.addRecord(ORDER_FORM_DOC);
            ccOrderForm.setFieldString("Mode", "Y"); // Qoo: fixme! // "P" is for Production Mode
            
            CcApiRecord ccCunsumer = ccOrderForm.addRecord("Consumer");
            ccCunsumer.setFieldString("Email", profile.getEmail());
            CcApiRecord ccPaymentMech = ccCunsumer.addRecord("PaymentMech");
            CcApiRecord ccBillTo = ccCunsumer.addRecord("BillTo");
            
            ccPaymentMech.setFieldString("Type", CREDIT_CARD);
            CcApiRecord ccCreditCard = ccPaymentMech.addRecord(CREDIT_CARD);
            //ccCreditCard.setFieldS32("Type", 1);
            //ccCreditCard.setFieldString("Cvv2Indicator", "1");
            //ccCreditCard.setFieldString("Cvv2Val", creditCard.getVeridicationCode());
            ccCreditCard.setFieldString("Number", creditCard.getCardNumber());
            ccCreditCard.setFieldExpirationDate("Expires", creditCard.getExpires());
            
            CcApiRecord ccLocation = ccBillTo.addRecord("Location");
            CcApiRecord ccAddress = ccLocation.addRecord("Address");
            ccAddress.setFieldString("Name", profile.getName());
            //ccAddress.setFieldString("Street1", profile.getAddr1());
            //ccAddress.setFieldString("City", profile.getCity());
            //ccAddress.setFieldString("StateProv", profile.getState());
            //ccAddress.setFieldString("PostalCode", profile.getZip());
            //ccAddress.setFieldString("Country", USD); // Qoo
            
            CcApiRecord ccTransaction = ccOrderForm.addRecord("Transaction");
            ccTransaction.setFieldString("Type", "PreAuth");
            
            CcApiRecord ccCurrentTotals = ccTransaction.addRecord("CurrentTotals");
            CcApiRecord ccTotals = ccCurrentTotals.addRecord("Totals");
            CcApiMoney money = new CcApiMoney();
            money.setValue(FIRST_TIME_PREAUTH_CHARGE, USD);
            ccTotals.setFieldMoney("Total", money);
            
            ccResult = process(ccDoc);
            
        } catch (CcApiBadKeyException e) {
            NnLogUtil.logException(e);
            return null;
        } catch (CcApiBadValueException e) {
            NnLogUtil.logException(e);
            return null;
        }
        
        return ccResult;
    }
    
    private static CcApiDocument process(CcApiDocument ccDoc) {
        
        CcApiDocument ccResult = null;
        
        // NOTE: can only be opened in development site
        try {
            Writer outStream = new PrintWriter(System.out);
            ccDoc.writeTo(outStream);
        } catch (CcApiWriterException e) {
        }
        
        try {
            ccResult = ccDoc.process(MsoConfigManager.getCCBillingGayeway(), CC_PORT, true);
            
        } catch (CcApiServerConnectException e) {
            NnLogUtil.logException(e);
            return null;
        } catch (CcApiProcessException e) {
            NnLogUtil.logException(e);
            return null;
        } catch (CcApiInvalidDocumentException e) {
            NnLogUtil.logException(e);
            return null;
        } catch (CcApiBadHostException e) {
            NnLogUtil.logException(e);
            return null;
        } catch (CcApiBadPortException e) {
            NnLogUtil.logException(e);
            return null;
        }
        
        if (ccResult != null) {
            
            // NOTE: can only be opened in development site
            try {
                Writer outStream = new PrintWriter(System.out);
                ccResult.writeTo(outStream);
            } catch (CcApiWriterException e) {
            }
            
            try {
                CcApiRecord ccEngine = ccResult.getFirstRecord(CC_ENGINE_DOC);
                if (ccEngine != null) {
                    CcApiRecord ccMessageList = ccEngine.getFirstRecord("MessageList");
                    if (ccMessageList != null) {
                        CcApiRecord ccMessage = ccMessageList.getFirstRecord("Message");
                        int messageCnt = 0;
                        while (ccMessage != null) {
                            log.info("ccMessage[" + messageCnt + "]"
                                  + "Audience = " + ccMessage.getFieldString("Audience")
                               + ", ContextId = " + ccMessage.getFieldString("ContextId")
                               + ", Component = " + ccMessage.getFieldString("Component")
                                     + ", Sev = " + ccMessage.getFieldS32("Sev")
                                    + ", Text = " + ccMessage.getFieldString("Text"));
                            messageCnt++;
                            ccMessage = ccMessageList.getNextRecord("Message");
                        }
                    }
                }
            } catch (CcApiBadKeyException e) {
            }
        }
        
        return ccResult;
    }
    
    public static CcApiRecord getOverview(CcApiDocument ccResult) {
        
        if (ccResult == null) return null;
        
        CcApiRecord ccOverview = null;
        try {
            CcApiRecord ccEngine = ccResult.getFirstRecord(CC_ENGINE_DOC);
            if (ccEngine != null) {
                
                ccOverview = ccEngine.getFirstRecord(CC_OVERVIEW);
            }
            
        } catch (CcApiBadKeyException e) {
            NnLogUtil.logException(e);
            return null;
        }
        
        return ccOverview;
    }
}
