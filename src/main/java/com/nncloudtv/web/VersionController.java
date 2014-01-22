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
        String appVersion = "4.0.49.0";
        String hash = "4619860e4476b23afd7e53511f877f173d4226d4";
        String packagedTime = "2014-01-09 22:00:06.967024";
        String info = "app version: " + appVersion + "\n";
        info += "hash: " + hash + "\n";
        info += "packaged time: " + packagedTime + "\n";
        return NnNetUtil.textReturn(info);
    }
    
}
