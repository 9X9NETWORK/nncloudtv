package com.nncloudtv.web;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.nncloudtv.lib.NnNetUtil;

@Controller
@RequestMapping("version")
public class VersionController {
        
    @RequestMapping(value="current", produces = "text/plain; charset=utf-8")
    public ResponseEntity<String> current() {
        String appVersion = "4.4.0.1";
        String hash = "50442fcf6be0525267e8cdfe58f7c114cb2daeef";
        String packagedTime = "2014-02-24 22:33:08.489894";
        String info = "app version: " + appVersion + "\n"; 
        info += "hash: " + hash + "\n";
        info += "packaged time: " + packagedTime + "\n";
        return NnNetUtil.textReturn(info);
    }
    
}
