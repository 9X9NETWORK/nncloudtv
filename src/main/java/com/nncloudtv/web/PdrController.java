package com.nncloudtv.web;

/**
 * Please reference playerAPI
*/
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnNetUtil;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.NnDevice;
import com.nncloudtv.model.NnUser;
import com.nncloudtv.model.NnUserReport;
import com.nncloudtv.model.Pdr;
import com.nncloudtv.service.MsoManager;
import com.nncloudtv.service.NnUserReportManager;
import com.nncloudtv.service.PdrManager;
import com.nncloudtv.service.PlayerApiService;
import com.nncloudtv.web.api.NnStatusCode;

@Controller
@RequestMapping("pdr")
public class PdrController {
         
    /**
     * List all the devices a user has, including device token and device type.
     * 
     * @param userToken user token
     * @return lines of device token
     */
    @RequestMapping("listDevice")
    public ResponseEntity<String> listDevice(
            @RequestParam(required=false,value="user") String userToken,
            @RequestParam(required=false,value="mso")  String msoName) {
        
        PlayerApiService pservice = new PlayerApiService();
        Mso mso = NNF.getMsoMngr().findByName(msoName);
        if (mso == null) {
            mso = MsoManager.getSystemMso();
        }
        NnUser u = NNF.getUserMngr().findByToken(userToken, mso.getId());
        if (u == null)
            return NnNetUtil.textReturn((String) pservice.assembleMsgs(NnStatusCode.USER_INVALID, null));
        List<NnDevice> devices = NNF.getDeviceMngr().findByUser(u);
        
        if (devices.size() == 0)
            return NnNetUtil.textReturn((String) pservice.assembleMsgs(NnStatusCode.SUCCESS, null));
        
        String[] result = {""};
        for (NnDevice d : devices) {
            result[0] += d.getToken() + "\t" + d.getType() + "\n";
        }
        return NnNetUtil.textReturn((String) pservice.assembleMsgs(NnStatusCode.SUCCESS, result));
    }    
    
    /**
     * List PDR based on device OR user OR device + session OR user + session OR ip + since
     * 
     * @param device device token
     * @param userToken user token
     * @param session session id 
     * @param ip ip addr
     * @param since since date. format yyyymmdd
     * @return user token "tab" session id "\n" detail "\n\n" (next user)
     */
    @RequestMapping("listPdr")
    public ResponseEntity<String> listPdr(
            @RequestParam(required=false, value="mso")  String msoName,
            @RequestParam(required=false, value="user") String userToken,
            @RequestParam(required=false) String device,
            @RequestParam(required=false) String session,
            @RequestParam(required=false) String ip,
            @RequestParam(required=false) String since) {
        
        PdrManager pdrMngr = new PdrManager();
        PlayerApiService pservice = new PlayerApiService();
        NnUser u = null;
        List<NnDevice> ds = new ArrayList<NnDevice>();
        Mso mso = NNF.getMsoMngr().findByName(msoName);
        if (mso == null) {
            mso = MsoManager.getSystemMso();
        }
        NnDevice d = null;
        if (userToken != null) {
            u = NNF.getUserMngr().findByToken(userToken, mso.getId());
            if (u == null)
                return NnNetUtil.textReturn((String) pservice.assembleMsgs(NnStatusCode.USER_INVALID, null)); 
        }
        if (device!= null) {
            ds = NNF.getDeviceMngr().findByToken(device);
            if (ds.size() == 0)
                return NnNetUtil.textReturn((String) pservice.assembleMsgs(NnStatusCode.DEVICE_INVALID, null));
            d = ds.get(0);
        }
        if ((ip != null && since == null) || (ip == null && since != null)) 
            return NnNetUtil.textReturn((String) pservice.assembleMsgs(NnStatusCode.INPUT_MISSING, null));
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");        
        Date sinceDate = null;
        if (since != null) {
            try {
                sinceDate = sdf.parse(since);
            } catch (ParseException e) {
                return NnNetUtil.textReturn("wrong date format: yyyymmdd");
            }
        }
        List<Pdr> list = pdrMngr.findDebugging(u, d, session, ip, sinceDate);
        String[] result = {""};
        for (Pdr r : list) {
            String token = r.getDeviceToken();
            if (token == null)
                r.getUserToken();
            result[0] += token + "\t" + r.getSession() + "\t" + r.getIp() + "\n" +  
                         r.getDetail() + "\n\n";
        }
        return NnNetUtil.textReturn((String) pservice.assembleMsgs(NnStatusCode.SUCCESS, result));        
    }

    /**
     * List any issue users report. Please note: Return format does not comply with playerAPI.
     * 
     * @param userToken user token
     * @param since since date. format yyyymmdd
     * @return html format, user token, session, and user comment
     */
    @RequestMapping("listReport")
    public ResponseEntity<String> listReport(
            @RequestParam(required=false, value="user") String userToken,
            @RequestParam(required=false, value="mso")  String msoName,
            @RequestParam(required=false) String since) {
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        List<NnUserReport> list = new ArrayList<NnUserReport>();
        NnUserReportManager reportMngr = new NnUserReportManager();
        Date sinceDate = null;
        if (since != null) {
            try {
                sinceDate = sdf.parse(since);
            } catch (ParseException e) {
                return NnNetUtil.textReturn("wrong date format: yyyymmdd");
            }
        }
        if (userToken == null && since == null) {
            list = reportMngr.findAll();    
        }
        if (sinceDate != null && userToken == null) {
            System.out.println("since date:" + sinceDate);
            list = reportMngr.findSince(sinceDate);
        }
        if (sinceDate == null && userToken != null) {
            list = reportMngr.findByUser(userToken);
        }
        if (sinceDate != null && userToken != null) {
            list = reportMngr.findByUserSince(userToken, sinceDate);
        }
        String output = "";
        String email = "guest";
        Mso mso = NNF.getMsoMngr().findByName(msoName);
        if (mso == null) {
            mso = MsoManager.getSystemMso();
        }
        String nbsp = "&nbsp;&nbsp;&nbsp;";
        for (NnUserReport r : list) {
            NnUser found = NNF.getUserMngr().findByToken(r.getUserToken(), mso.getId());
            if (found != null)
                email = found.getEmail();
            output += "<p>" +
            r.getId() + nbsp + 
            "<a href='listPdr?user=" + r.getUserToken() + "&session=" + r.getSession() + "'>" + r.getSession() + "</a>" +                         
            nbsp + r.getUserToken() + nbsp + email + nbsp + r.getCreateDate() +
            "<br/>" + r.getComment() + "</p>";
        }
        return NnNetUtil.htmlReturn(output);
    }
}

