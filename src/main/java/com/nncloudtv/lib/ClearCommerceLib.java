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
import com.nncloudtv.exception.NnBillingException;
import com.nncloudtv.exception.NnClearCommerceException;
import com.nncloudtv.exception.NnDataIntegrityException;
import com.nncloudtv.model.BillingOrder;
import com.nncloudtv.model.BillingPackage;
import com.nncloudtv.model.BillingProfile;
import com.nncloudtv.service.BillingPackageManager;
import com.nncloudtv.service.BillingProfileManager;
import com.nncloudtv.service.MsoConfigManager;
import com.nncloudtv.web.json.cms.CreditCard;

public class ClearCommerceLib {
    
    protected static final Logger log = Logger.getLogger(ClearCommerceLib.class.getName());
    
    static final short CC_PORT = 12000; 
    static final String ONE_DOLLAR = "100"; // in cent
    
    static final String USD              = "840";
    static final String DOC_VERSION      = "1.0";
    static final String ORDER_FORM_DOC   = "OrderFormDoc";
    static final String PAYMENT_NO_FRAUD = "PaymentNoFraud";
    static final String CREDIT_CARD      = "CreditCard";
    
    static final String CC_ENGINE_DOC   = "EngineDoc";
    static final String CC_OVERVIEW     = "Overview";
    static final String CC_MESSAGE_LIST = "MessageList";
    static final String CC_ORDER_FORM   = "OrderFormDoc";
    
    private static CcApiRecord populateCCUserField(CcApiRecord ccRecord) throws CcApiBadKeyException, CcApiBadValueException {
        
        Integer ccClientId = Integer.parseInt(MsoConfigManager.getCCClientId());
        
        CcApiRecord ccUser = ccRecord.addRecord("User");
        ccUser.setFieldString("Name", MsoConfigManager.getCCUserName());
        ccUser.setFieldString("Password", MsoConfigManager.getCCPassword());
        ccUser.setFieldS32("ClientId", ccClientId);
        ccUser.setFieldS32("EffectiveClientId", ccClientId);
        
        return ccRecord;
    }
    
    public static CcApiDocument verifyCreditCardNumber(CreditCard creditCard, boolean isProduction) throws NnClearCommerceException, CcApiBadKeyException, CcApiBadValueException {
        
        CcApiDocument ccDoc = new CcApiDocument();
        ccDoc.setFieldString("DocVersion", DOC_VERSION);
        
        CcApiRecord ccEngine = populateCCUserField(ccDoc.addRecord(CC_ENGINE_DOC));
        ccEngine.addRecord("Instructions").setFieldString("Pipeline", PAYMENT_NO_FRAUD);
        ccEngine.setFieldString("ContentType", ORDER_FORM_DOC);
        
        CcApiRecord ccOrderForm = ccEngine.addRecord(ORDER_FORM_DOC);
        ccOrderForm.setFieldString("Mode", "P"); // "P" is for Production Mode
        
        CcApiRecord ccCunsumer = ccOrderForm.addRecord("Consumer");
        CcApiRecord ccPaymentMech = ccCunsumer.addRecord("PaymentMech");
        
        ccPaymentMech.setFieldString("Type", CREDIT_CARD);
        CcApiRecord ccCreditCard = ccPaymentMech.addRecord(CREDIT_CARD);
        ccCreditCard.setFieldString("Cvv2Indicator", "1");
        ccCreditCard.setFieldString("Cvv2Val", creditCard.getVeridicationCode());
        ccCreditCard.setFieldString("Number", creditCard.getCardNumber());
        ccCreditCard.setFieldExpirationDate("Expires", creditCard.getExpires());
        
        CcApiRecord ccTransaction = ccOrderForm.addRecord("Transaction");
        ccTransaction.setFieldString("Type", "PreAuth");
        
        return process(ccDoc, isProduction);
    }
    
    public static CcApiDocument preAuth(BillingProfile profile, CreditCard creditCard, boolean isProduction) throws CcApiBadKeyException, CcApiBadValueException, NnClearCommerceException {
        
        CcApiMoney oneDollar = new CcApiMoney();
        oneDollar.setValue(ONE_DOLLAR, USD);
        
        CcApiDocument ccDoc = new CcApiDocument();
        ccDoc.setFieldString("DocVersion", DOC_VERSION);
        
        CcApiRecord ccEngine = populateCCUserField(ccDoc.addRecord(CC_ENGINE_DOC));
        ccEngine.addRecord("Instructions").setFieldString("Pipeline", PAYMENT_NO_FRAUD);
        ccEngine.setFieldString("ContentType", ORDER_FORM_DOC);
        ccEngine.setFieldString("SourceId", String.valueOf(profile.getId()));
        
        CcApiRecord ccOrderForm = ccEngine.addRecord(ORDER_FORM_DOC);
        ccOrderForm.setFieldString("Mode", "R"); // Qoo: fixme! // "P" is for Production Mode
        
        CcApiRecord ccCunsumer = ccOrderForm.addRecord("Consumer");
        ccCunsumer.setFieldString("Email", profile.getEmail());
        CcApiRecord ccPaymentMech = ccCunsumer.addRecord("PaymentMech");
        CcApiRecord ccBillTo = ccCunsumer.addRecord("BillTo");
        CcApiRecord ccOrderItemList = ccCunsumer.addRecord("OrderItemList");
        
        ccPaymentMech.setFieldString("Type", CREDIT_CARD);
        CcApiRecord ccCreditCard = ccPaymentMech.addRecord(CREDIT_CARD);
        //ccCreditCard.setFieldS32("Type", 1);
        if (creditCard.getVeridicationCode() != null) {
            ccCreditCard.setFieldString("Cvv2Indicator", "1");
            ccCreditCard.setFieldString("Cvv2Val", creditCard.getVeridicationCode());
        } else {
            ccCreditCard.setFieldString("Cvv2Indicator", "2"); // CVV code is not present
        }
        ccCreditCard.setFieldString("Number", creditCard.getCardNumber());
        ccCreditCard.setFieldExpirationDate("Expires", creditCard.getExpires());
        
        CcApiRecord ccLocation = ccBillTo.addRecord("Location");
        CcApiRecord ccAddress = ccLocation.addRecord("Address");
        ccAddress.setFieldString("Name", profile.getName());
        //ccAddress.setFieldString("Street1", profile.getAddr1());
        //ccAddress.setFieldString("City", profile.getCity());
        //ccAddress.setFieldString("StateProv", profile.getState());
        //ccAddress.setFieldString("PostalCode", profile.getZip());
        //ccAddress.setFieldString("Country", USD);
        
        CcApiRecord ccOrderItem = ccOrderItemList.addRecord("OrderItem");
        ccOrderItem.setFieldS32("ItemNumber", 1);
        ccOrderItem.setFieldS32("Qty", 1);
        ccOrderItem.setFieldString("Id", "999999999");
        ccOrderItem.setFieldString("CommCode", "Billing Profile PreAuth");
        ccOrderItem.setFieldString("Desc", "PreAuth for recurring charge");
        ccOrderItem.setFieldMoney("Price", oneDollar);
        ccOrderItem.setFieldMoney("Total", oneDollar);
        
        CcApiRecord ccTransaction = ccOrderForm.addRecord("Transaction");
        ccTransaction.setFieldString("Type", "PreAuth");
        
        CcApiRecord ccCurrentTotals = ccTransaction.addRecord("CurrentTotals");
        CcApiRecord ccTotals = ccCurrentTotals.addRecord("Totals");
        ccTotals.setFieldMoney("Total", oneDollar);
        
        return process(ccDoc, isProduction);
    }
    
    private static CcApiDocument process(CcApiDocument ccDoc, boolean isProduction) throws NnClearCommerceException {
        
        CcApiDocument ccResult = null;
        
        // NOTE: can only be opened in development site
        if (!isProduction) {
            try {
                Writer outStream = new PrintWriter(System.out);
                ccDoc.writeTo(outStream);
            } catch (CcApiWriterException e) {
            }
        }
        
        try {
            ccResult = ccDoc.process(MsoConfigManager.getCCBillingGayeway(), CC_PORT, true);
            
        } catch (CcApiServerConnectException e) {
            throw new NnClearCommerceException(e.getMessage());
        } catch (CcApiProcessException e) {
            throw new NnClearCommerceException(e.getMessage());
        } catch (CcApiInvalidDocumentException e) {
            throw new NnClearCommerceException(e.getMessage());
        } catch (CcApiBadHostException e) {
            throw new NnClearCommerceException(e.getMessage());
        } catch (CcApiBadPortException e) {
            throw new NnClearCommerceException(e.getMessage());
        }
        
        if (ccResult != null) {
            
            // NOTE: can only be opened in development site
            if (!isProduction) {
                try {
                    Writer outStream = new PrintWriter(System.out);
                    ccResult.writeTo(outStream);
                } catch (CcApiWriterException e) {
                }
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
    
    public static CcApiRecord getOverview(CcApiDocument ccResult) throws CcApiBadKeyException {
        
        if (ccResult == null) throw new IllegalArgumentException("ccResult must not be null");
        
        CcApiRecord ccEngine = ccResult.getFirstRecord(CC_ENGINE_DOC);
        
        return ccEngine.getFirstRecord(CC_OVERVIEW);
    }
    
    public static CcApiRecord getOrderForm(CcApiDocument ccResult) throws CcApiBadKeyException {
        
        if (ccResult == null) throw new IllegalArgumentException("ccResult must not be null");
        
        CcApiRecord ccEngine = ccResult.getFirstRecord(CC_ENGINE_DOC);
        
        return ccEngine.getFirstRecord(CC_ORDER_FORM);
    }
    
    public static CcApiDocument referencedAuth(BillingOrder order, boolean isProduction) throws NnDataIntegrityException, NnBillingException, CcApiBadValueException, CcApiBadKeyException, NnClearCommerceException {
        
        CcApiDocument ccResult = null;
        BillingProfileManager profileMngr = new BillingProfileManager();
        BillingPackageManager packageMngr = new BillingPackageManager();
        BillingProfile profile = profileMngr.findById(order.getProfileId());
        BillingPackage pack = packageMngr.findById(order.getPackageId());
        if (profile == null)
            throw new NnDataIntegrityException("billingOrder " + order.getId() + " has invalid profileId");
        if (pack == null)
            throw new NnDataIntegrityException("billingOrder " + order.getId() + " has invalid packageId");
        if (profile.getCardStatus() < BillingProfile.AUTHED || profile.getCcRefOrderId() == null)
            throw new NnBillingException("profile " + profile.getId() + " is not authed");
        
        CcApiMoney price = new CcApiMoney();
        price.setValue(String.valueOf(pack.getPrice()), USD);
        
        CcApiDocument ccDoc = new CcApiDocument();
        ccDoc.setFieldString("DocVersion", DOC_VERSION);
        
        CcApiRecord ccEngine = populateCCUserField(ccDoc.addRecord(CC_ENGINE_DOC));
        ccEngine.addRecord("Instructions").setFieldString("Pipeline", PAYMENT_NO_FRAUD);
        ccEngine.setFieldString("ContentType", ORDER_FORM_DOC);
        ccEngine.setFieldString("SourceId", String.valueOf(profile.getId()));
        
        CcApiRecord ccOrderForm = ccEngine.addRecord(ORDER_FORM_DOC);
        ccOrderForm.setFieldString("Mode", "R"); // Qoo: fixme! // "P" is for Production Mode
        ccOrderForm.setFieldString("GroupId", String.valueOf(order.getId()));
        
        CcApiRecord ccCunsumer = ccOrderForm.addRecord("Consumer");
        CcApiRecord ccBillTo = ccCunsumer.addRecord("BillTo");
        CcApiRecord ccOrderItemList = ccCunsumer.addRecord("OrderItemList");
        ccCunsumer.setFieldString("Email", profile.getEmail());
        ccCunsumer.setFieldString("ReferenceOrderId", profile.getCcRefOrderId());
        if (profile.getCcRefTransId() != null)
            ccCunsumer.setFieldString("ReferenceTransId", profile.getCcRefTransId());
        
        CcApiRecord ccLocation = ccBillTo.addRecord("Location");
        CcApiRecord ccAddress = ccLocation.addRecord("Address");
        ccAddress.setFieldString("Name", profile.getName());
        //ccAddress.setFieldString("Street1", profile.getAddr1());
        //ccAddress.setFieldString("City", profile.getCity());
        //ccAddress.setFieldString("StateProv", profile.getState());
        //ccAddress.setFieldString("PostalCode", profile.getZip());
        //ccAddress.setFieldString("Country", USD);
        
        CcApiRecord ccOrderItem = ccOrderItemList.addRecord("OrderItem");
        ccOrderItem.setFieldS32("ItemNumber", 1);
        ccOrderItem.setFieldS32("Qty", 1);
        ccOrderItem.setFieldString("Id", String.valueOf(pack.getId()));
        ccOrderItem.setFieldString("ProductCode", pack.getName());
        ccOrderItem.setFieldMoney("Price", price);
        ccOrderItem.setFieldMoney("Total", price);
        
        CcApiRecord ccTransaction = ccOrderForm.addRecord("Transaction");
        ccTransaction.setFieldString("Type", "Auth");
        
        CcApiRecord ccCurrentTotals = ccTransaction.addRecord("CurrentTotals");
        CcApiRecord ccTotals = ccCurrentTotals.addRecord("Totals");
        ccTotals.setFieldMoney("Total", price);
        
        ccResult = process(ccDoc, isProduction);
        
        return ccResult;
    }
}
