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
        String appVersion = "4.4.0.1.1";
        String hash = "94d99a5b9febf9901ce691f4656415c716296105";
        String packagedTime = "2014-02-24 23:16:09.469829";
        String info = "app version: " + appVersion + "\n"; 
        info += "hash: " + hash + "\n";
        info += "packaged time: " + packagedTime + "\n";
        return NnNetUtil.textReturn(info);
    }
    
}
