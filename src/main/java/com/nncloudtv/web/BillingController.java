package com.nncloudtv.web;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.clearcommerce.ccxclientapi.CcApiBadKeyException;
import com.clearcommerce.ccxclientapi.CcApiBadValueException;
import com.clearcommerce.ccxclientapi.CcApiDocument;
import com.clearcommerce.ccxclientapi.CcApiRecord;
import com.nncloudtv.exception.NnApiBadRequestException;
import com.nncloudtv.exception.NnApiInternalErrorException;
import com.nncloudtv.exception.NnBillingException;
import com.nncloudtv.exception.NnClearCommerceException;
import com.nncloudtv.exception.NnDataIntegrityException;
import com.nncloudtv.lib.ClearCommerceLib;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnDateUtil;
import com.nncloudtv.lib.NnNetUtil;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.model.BillingOrder;
import com.nncloudtv.model.BillingPackage;
import com.nncloudtv.model.NnPurchase;
import com.nncloudtv.service.BillingService;
import com.nncloudtv.web.api.ApiContext;
import com.nncloudtv.web.api.NnStatusCode;
import com.nncloudtv.web.json.cms.CreditCard;

@Controller
@RequestMapping("billingAPI")
public class BillingController {
    
    protected static final Logger log = Logger.getLogger(BillingController.class.getName());
    
    @RequestMapping("checkCreditCard")
    public ResponseEntity<String> checkCreditCard(HttpServletRequest req, HttpServletResponse resp) {
        
        BillingService billingService = new BillingService();
        
        String result = "";
        CreditCard creditCard= null;
        try {
            creditCard = billingService.checkCreditCard(new ApiContext(), true);
            
        } catch (NnApiInternalErrorException e) {
            
            result = e.getMessage();
            
        } catch (NnApiBadRequestException e) {
            
            result = e.getMessage();
            
        } catch (CcApiBadKeyException e) {
            
            result = e.getMessage();
            
        } catch (NnClearCommerceException e) {
            
            result = e.getMessage();
            
        } catch (CcApiBadValueException e) {
            
            result = e.getMessage();
            
        } finally {
            
            result = creditCard.toString();
        }
        
        return NnNetUtil.textReturn(NnStatusCode.SUCCESS + "\n\n--\n\n" + result);
    }
    
    @RequestMapping("verifyPurchases")
    public ResponseEntity<String> verifyPurchases(HttpServletRequest req) {
        
        List<NnPurchase> purchases = new ArrayList<NnPurchase>();
        String purchaseIdStr = req.getParameter("purchaseId");
        if (purchaseIdStr != null) {
            
            NnPurchase purchase = NNF.getPurchaseMngr().findById(purchaseIdStr);
            if (purchase != null) {
                purchases.add(purchase);
            }
            
        } else {
            
            purchases = (req.getParameter("all") == null) ? NNF.getPurchaseMngr().findAllActive() : NNF.getPurchaseMngr().findAll();
        }
        ApiContext ctx = new ApiContext();
        
        int cntVerified = 0;
        int cntTotal    = 0;
        int cntInactive = 0;
        int cntInvalid  = 0;
        
        for (NnPurchase purchase : purchases) {
            
            NNF.getPurchaseMngr().verifyPurchase(purchase, ctx.isProductionSite());
            if (purchase.isVerified()) {
                
                cntVerified ++;
            }
            if (purchase.getStatus() == NnPurchase.INACTIVE) {
                
                cntInactive++;
                
            } else if (purchase.getStatus() == NnPurchase.INVALID) {
                
                cntInvalid++;
            }
            
            cntTotal++;
        }
        
        return NnNetUtil.textReturn(String.format("OK\n--\ntotal\t%d\nverified\t%d\ninactive\t%d\ninvalid\t%d", cntTotal, cntVerified, cntInactive, cntInvalid));
    }
    
    @RequestMapping("recurringCharge")
    public ResponseEntity<String> recurringCharge(HttpServletRequest req) {
        
        List<BillingOrder> orders = NNF.getOrderMngr().findByStatus(BillingOrder.RECURRING);
        ApiContext context = new ApiContext();
        String results = "";
        int total = 0;
        
        for (BillingOrder order : orders) {
            
            if (order.getExpiryDate() != null && NnDateUtil.isToday(order.getExpiryDate())) {
                
                total++;
                String txnStatus = null;
                String[] result = {"", ""};
                result[0] = String.valueOf(order.getId());
                CcApiDocument ccResult = null;
                try {
                    ccResult = ClearCommerceLib.referencedAuth(order, context.isProductionSite());
                    CcApiRecord ccOverview = ClearCommerceLib.getOverview(ccResult);
                    txnStatus = ccOverview.getFieldString("TransactionStatus");
                    
                } catch (NnDataIntegrityException e) {
                    result[1] = e.getMessage();
                    results += NnStringUtil.getDelimitedStr(result) + "\n";
                    continue;
                } catch (NnBillingException e) {
                    result[1] = e.getMessage();
                    results += NnStringUtil.getDelimitedStr(result) + "\n";
                    continue;
                } catch (CcApiBadValueException e) {
                    result[1] = e.getMessage();
                    results += NnStringUtil.getDelimitedStr(result) + "\n";
                    continue;
                } catch (CcApiBadKeyException e) {
                    result[1] = e.getMessage();
                    results += NnStringUtil.getDelimitedStr(result) + "\n";
                    continue;
                } catch (NnClearCommerceException e) {
                    result[1] = e.getMessage();
                    results += NnStringUtil.getDelimitedStr(result) + "\n";
                    continue;
                }
                
                if ("A".equals(txnStatus) == false) {
                    
                    result[1] = "charge failed";
                    results += NnStringUtil.getDelimitedStr(result) + "\n";
                    continue;
                }
                
                BillingPackage pack = NNF.getPackageMngr().findById(order.getPackageId());
                if (BillingPackage.DAILY.equals(pack.getChargeCycle())) {
                    order.setExpiryDate(NnDateUtil.tomorrow());
                } else if (BillingPackage.WEEKLY.equals(pack.getChargeCycle())) {
                    order.setExpiryDate(NnDateUtil.nextWeek());
                } else {
                    order.setExpiryDate(NnDateUtil.nextMonth());
                }
                order.setTotalPaymentAmount(order.getTotalPaymentAmount() + pack.getPrice());
                order.setCntPayment(order.getCntPayment() + 1);
                NNF.getOrderMngr().save(order);
                result[1] = "successfully charged";
                results += NnStringUtil.getDelimitedStr(result) + "\n";
            }
        }
        
        return NnNetUtil.textReturn("OK\n--\ntotal\t" + total + "\n--\n" + results);
    }
}
