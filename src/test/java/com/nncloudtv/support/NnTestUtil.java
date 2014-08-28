package com.nncloudtv.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.model.Mso;

public class NnTestUtil {
    
    public static void assertEqualURL(String expectedURL, String actualURL) {
        
        if ("".equals(expectedURL)) {
            fail("exceptedURL should not be null or empty string.");
        }
        if ("".equals(actualURL)) {
            fail("actualURL should not be null or empty string.");
        }
        
        URL url = null;
        URI expectedURI = null;
        URI actualURI = null;
        
        // check exceptedURL valid
        url = null;
        try {
            url = new URL(expectedURL);
            expectedURI = new URI(expectedURL);
        } catch (MalformedURLException e) {
        } catch (URISyntaxException e) {
        }
        assertFalse("expectedURL should be a valid URL.", url == null);
        assertFalse("expectedURL should be a valid URI.",  expectedURI == null);
        
        // check actualURL valid
        url = null;
        try {
            url = new URL(actualURL);
            actualURI= new URI(actualURL);
        } catch (MalformedURLException e) {
        } catch (URISyntaxException e) {
        }
        assertFalse("actualURL should be a valid URL.", url == null);
        assertFalse("actualURL should be a valid URI.", actualURI == null);
        
        // check protocol + domain + path
        assertEquals("URL not match.", expectedURL.split("\\?")[0], actualURL.split("\\?")[0]);
        
        // check parameters
        List<NameValuePair> expectedParams = URLEncodedUtils.parse(expectedURI, NnStringUtil.UTF8);
        List<NameValuePair> actualParams = URLEncodedUtils.parse(actualURI, NnStringUtil.UTF8);
        Map<String, String> map = new HashMap<String, String>();
        for (NameValuePair param : actualParams) {
            map.put(param.getName(), param.getValue());
        }
        for (NameValuePair param : expectedParams) {
            
            if (!map.containsKey(param.getName())) {
                fail("URL should contain '" + param.getName() + "' parameter.");
            }
            assertEquals("URL's parameter '" + param.getName() + "', its value should be '" + param.getValue() + "'.",
                    param.getValue(), map.get(param.getName()));
        }
    }
    
    public static void assertEqualParameters(String expectedParameters, String actualParameters) {
        
        String prifix = "http://nntest.com/test?";
        assertEqualURL(prifix + expectedParameters, prifix + actualParameters);
    }
    
    public static Mso getNnMso() {
        
        int id = 1;
        String name = Mso.NAME_9X9;
        String title = "title";
        String logoUrl = "logoUrl";
        String jingleUrl = "jingleUrl";
        String preferredLangCode = "preferredLangCode";
        
        Mso mso = new Mso(name, "intro", "email", Mso.TYPE_NN);
        mso.setId(id);
        mso.setTitle(title);
        mso.setLogoUrl(logoUrl);
        mso.setJingleUrl(jingleUrl);
        mso.setLang(preferredLangCode);
        
        return mso;
    }

}
