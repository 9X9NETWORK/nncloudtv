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
import com.nncloudtv.lib.NnDateUtil;
import com.nncloudtv.lib.NnNetUtil;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.model.BillingOrder;
import com.nncloudtv.model.BillingPackage;
import com.nncloudtv.service.BillingOrderManager;
import com.nncloudtv.service.BillingPackageManager;
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
            creditCard = billingService.checkCreditCard(new ApiContext(req), true);
            
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
    
    @RequestMapping("recurringCharge")
    public ResponseEntity<String> recurringCharge(HttpServletRequest req) {
        
        BillingPackageManager packMngr = new BillingPackageManager(); 
        BillingOrderManager orderMngr = new BillingOrderManager();
        List<BillingOrder> orders = orderMngr.findByType(BillingOrder.RECURRING);
        ApiContext context = new ApiContext(req);
        String results = "";
        int total = 0;
        
        for (BillingOrder order : orders) {
            
            if (order.getExpiryDate() != null && NnDateUtil.isToday(order.getExpiryDate())) {
                
                total++;
                String txnStatus = null;
                List<String> result = new ArrayList<String>();
                result.add(String.valueOf(order.getId()));
                CcApiDocument ccResult = null;
                try {
                    ccResult = ClearCommerceLib.referencedAuth(order, context.isProductionSite());
                    CcApiRecord ccOverview = ClearCommerceLib.getOverview(ccResult);
                    txnStatus = ccOverview.getFieldString("TransactionStatus");
                    
                } catch (NnDataIntegrityException e) {
                    result.add(e.getMessage());
                    continue;
                } catch (NnBillingException e) {
                    result.add(e.getMessage());
                    continue;
                } catch (CcApiBadValueException e) {
                    result.add(e.getMessage());
                    continue;
                } catch (CcApiBadKeyException e) {
                    result.add(e.getMessage());
                    continue;
                } catch (NnClearCommerceException e) {
                    result.add(e.getMessage());
                    continue;
                }
                
                if (!"A".equals(txnStatus)) {
                    
                    result.add("charge failed");
                    continue;
                }
                
                BillingPackage pack = packMngr.findById(order.getPackageId());
                if (BillingPackage.DAILY.equals(pack.getChargeCycle())) {
                    order.setExpiryDate(NnDateUtil.tomorrow());
                } else if (BillingPackage.WEEKLY.equals(pack.getChargeCycle())) {
                    order.setExpiryDate(NnDateUtil.nextWeek());
                } else {
                    order.setExpiryDate(NnDateUtil.nextMonth());
                }
                order.setTotalPaymentAmount(order.getTotalPaymentAmount() + pack.getPrice());
                order.setCntPayment(order.getCntPayment() + 1);
                orderMngr.save(order);
                result.add("successfully charged");
                
                results += NnStringUtil.getDelimitedStr((String[]) result.toArray()) + "\n";
            }
        }
        
        return NnNetUtil.textReturn(NnStatusCode.SUCCESS + "\n\n--\ntotal\t" + total + "\n--\n" + results);
    }
}
