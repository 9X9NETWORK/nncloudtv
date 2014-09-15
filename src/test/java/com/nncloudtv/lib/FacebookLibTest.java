package com.nncloudtv.lib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.nncloudtv.model.Mso;
import com.nncloudtv.model.MsoConfig;
import com.nncloudtv.service.MsoConfigManager;
import com.nncloudtv.support.NnTestAll;
import com.nncloudtv.support.NnTestUtil;
import com.nncloudtv.web.json.facebook.FacebookResponse;
import com.nncloudtv.wrapper.NNFWrapper;

@RunWith(PowerMockRunner.class)
@PrepareForTest({FacebookLib.class, URL.class})
@Category(NnTestAll.class)
public class FacebookLibTest {
    
    protected static final Logger log = Logger.getLogger(FacebookLibTest.class.getName());
    
    private FacebookLib facebookLib;
    
    @Mock private MsoConfigManager configMngr;
    
    @Before
    public void setUp() {
        
        facebookLib = new FacebookLib();
        
        NNFWrapper.setConfigMngr(configMngr);
    }
    
    @After
    public void tearDown() {
        configMngr = null;
        facebookLib = null;
    }
    
    @Test
    public void testGetOAuthAccessToken() {
        
        // input arguments
        final String code = "code";
        final String uri = "uri";
        final String fbLoginUri = "fbLoginUri";
        final Long msoId = (long) 1;
        Mso mso = new Mso(Mso.NAME_9X9, "intro", "contactEmail", Mso.TYPE_NN);
        mso.setId(msoId);
        
        // mock data
        final String clientId = "1248163264128256512";
        final String secret = "secret";
        final int responseCode = HttpURLConnection.HTTP_OK;
        final String token = "token";
        final String expires = "7200";
        final String line = "access_token=" + token + "&expires=" + expires;
        
        // mock object
        URL url = PowerMockito.mock(URL.class);
        HttpURLConnection connection =  Mockito.mock(HttpURLConnection.class);
        OutputStreamWriter writer = Mockito.mock(OutputStreamWriter.class);
        InputStreamReader reader = Mockito.mock(InputStreamReader.class);
        BufferedReader bufferedReader = Mockito.mock(BufferedReader.class);
        
        // stubs
        when(configMngr.getFacebookInfo(MsoConfig.FACEBOOK_CLIENTID, mso)).thenReturn(clientId);
        when(configMngr.getFacebookInfo(MsoConfig.FACEBOOK_CLIENTSECRET, mso)).thenReturn(secret);
        
        try {
            PowerMockito.whenNew(URL.class).withArguments(anyString()).thenReturn(url);
            when(url.openConnection()).thenReturn(connection);
            PowerMockito.whenNew(OutputStreamWriter.class).withArguments((OutputStream) anyObject()).thenReturn(writer);
            //doNothing().when(writer).write(anyString()); // don't need stub this since it still recorded
            when(connection.getResponseCode()).thenReturn(responseCode);
            
            PowerMockito.whenNew(InputStreamReader.class).withArguments((InputStream) anyObject()).thenReturn(reader);
            PowerMockito.whenNew(BufferedReader.class).withArguments((InputStreamReader) anyObject()).thenReturn(bufferedReader);
            when(bufferedReader.readLine()).thenReturn(line);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // execute
        String[] actual = facebookLib.getOAuthAccessToken(code, uri, fbLoginUri, mso);
        
        // verify
        verify(configMngr).getFacebookInfo(MsoConfig.FACEBOOK_CLIENTID, mso);
        verify(configMngr).getFacebookInfo(MsoConfig.FACEBOOK_CLIENTSECRET, mso);
        
        ArgumentCaptor<String> captureParams = ArgumentCaptor.forClass(String.class);
        try {
            PowerMockito.verifyNew(URL.class).withArguments(anyString());
            //verify(url).openConnection(); // don't know why can't verify this
            PowerMockito.verifyNew(OutputStreamWriter.class).withArguments((OutputStream) anyObject());
            verify(writer).write(captureParams.capture());
            verify(connection, atLeastOnce()).getResponseCode();
            
            PowerMockito.verifyNew(InputStreamReader.class).withArguments((InputStream) anyObject());
            PowerMockito.verifyNew(BufferedReader.class).withArguments((InputStreamReader) anyObject());
            verify(bufferedReader).readLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        String actualParams = captureParams.getValue();
        String modifiedRedirectUri = fbLoginUri + "?uri=" + NnStringUtil.urlencode(uri, NnStringUtil.ASCII);
        String expectedParams = "client_id=" + clientId +
                        "&client_secret=" + secret +
                        "&code=" + code +
                        "&redirect_uri=" + NnStringUtil.urlencode(modifiedRedirectUri, NnStringUtil.ASCII);
        // do urlencode twice for uri ??
        NnTestUtil.assertEqualParameters(expectedParams, actualParams);
        
        assertEquals("The token should be matched.", token, actual[0]);
        assertEquals("The expires should be matched.", expires, actual[1]);
    }
    
    @Test
    public void testGetLongLivedAccessToken() {
        
        // input arguments
        final String shortLivedAccessToken = "shortLivedAccessToken";
        final Long msoId = (long) 1;
        Mso mso = new Mso(Mso.NAME_9X9, "intro", "contactEmail", Mso.TYPE_NN);
        mso.setId(msoId);
        
        // mock data
        final String clientId = "1248163264128256512";
        final String secret = "secret";
        final int responseCode = HttpURLConnection.HTTP_OK;
        final String token = "token";
        final String expires = "720000";
        final String line = "access_token=" + token + "&expires=" + expires;
        
        // mock object
        URL url = PowerMockito.mock(URL.class);
        HttpURLConnection connection =  Mockito.mock(HttpURLConnection.class);
        OutputStreamWriter writer = Mockito.mock(OutputStreamWriter.class);
        InputStreamReader reader = Mockito.mock(InputStreamReader.class);
        BufferedReader bufferedReader = Mockito.mock(BufferedReader.class);
        
        // stubs
        when(configMngr.getFacebookInfo(MsoConfig.FACEBOOK_CLIENTID, mso)).thenReturn(clientId);
        when(configMngr.getFacebookInfo(MsoConfig.FACEBOOK_CLIENTSECRET, mso)).thenReturn(secret);
        
        try {
            PowerMockito.whenNew(URL.class).withArguments(anyString()).thenReturn(url);
            when(url.openConnection()).thenReturn(connection);
            PowerMockito.whenNew(OutputStreamWriter.class).withArguments((OutputStream) anyObject()).thenReturn(writer);
            //doNothing().when(writer).write(anyString()); // don't need stub this since it still recorded
            when(connection.getResponseCode()).thenReturn(responseCode);
            
            PowerMockito.whenNew(InputStreamReader.class).withArguments((InputStream) anyObject()).thenReturn(reader);
            PowerMockito.whenNew(BufferedReader.class).withArguments((InputStreamReader) anyObject()).thenReturn(bufferedReader);
            when(bufferedReader.readLine()).thenReturn(line);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // execute
        String[] actual = FacebookLib.getLongLivedAccessToken(shortLivedAccessToken, mso);
        
        // verify
        verify(configMngr).getFacebookInfo(MsoConfig.FACEBOOK_CLIENTID, mso);
        verify(configMngr).getFacebookInfo(MsoConfig.FACEBOOK_CLIENTSECRET, mso);
        
        ArgumentCaptor<String> captureParams = ArgumentCaptor.forClass(String.class);
        try {
            PowerMockito.verifyNew(URL.class).withArguments(anyString());
            //verify(url).openConnection(); // don't know why can't verify this
            PowerMockito.verifyNew(OutputStreamWriter.class).withArguments((OutputStream) anyObject());
            verify(writer).write(captureParams.capture());
            verify(connection, atLeastOnce()).getResponseCode();
            
            PowerMockito.verifyNew(InputStreamReader.class).withArguments((InputStream) anyObject());
            PowerMockito.verifyNew(BufferedReader.class).withArguments((InputStreamReader) anyObject());
            verify(bufferedReader).readLine();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        String actualParams = captureParams.getValue();
        String expectedParams = "client_id=" + clientId +
                                "&grant_type=fb_exchange_token" +
                                "&client_secret=" + secret +
                                "&fb_exchange_token=" + shortLivedAccessToken;
        NnTestUtil.assertEqualParameters(expectedParams, actualParams);
        
        assertEquals("The token should be matched.", token, actual[0]);
        assertEquals("The expires should be matched.", expires, actual[1]);
    }
    
    @Test
    public void testGetDialogOAuthPath() {
        
        // input arguments
        final String referrer = "http://www.mock.com/signin";
        final String fbLoginUri = "http://www.localhost/fb/login";
        final Long msoId = (long) 1;
        Mso mso = new Mso(Mso.NAME_9X9, "intro", "contactEmail", Mso.TYPE_NN);
        mso.setId(msoId);
        
        // mock data
        final String clientId = "110847978946712";
        
        // stubs
        when(configMngr.getFacebookInfo(anyString(), (Mso) anyObject())).thenReturn(clientId);
        
        // execute
        String result = FacebookLib.getDialogOAuthPath(referrer, fbLoginUri, mso);
        
        // verify
        verify(configMngr).getFacebookInfo(MsoConfig.FACEBOOK_CLIENTID, mso);
        
        URL url = null;
        URI uri = null;
        try {
            url = new URL(result);
            uri = new URI(result);
        } catch (MalformedURLException e) {
        } catch (URISyntaxException e) {
        }
        assertFalse("Redirection should be a valid URL.", url == null);
        assertFalse("Redirection should be a valid URI.", uri == null);
        
        List<NameValuePair> params = URLEncodedUtils.parse(uri, NnStringUtil.UTF8);
        Map<String, String> map = new HashMap<String, String>();
        for (NameValuePair nv : params) {
            map.put(nv.getName(), nv.getValue());
        }
        assertNotNull("'client_id' sould not be null.", map.get("client_id"));
        String redirectUri = map.get("redirect_uri");
        assertNotNull("'redirect_uri' should not be null.", redirectUri);
        
        uri = null;
        try {
            uri = new URI(redirectUri);
        } catch (URISyntaxException e) {
        }
        assertNotNull("'redirect_uri' should be valid.", uri);
        params = URLEncodedUtils.parse(uri, NnStringUtil.UTF8);
        map.clear();
        for (NameValuePair nv : params) {
            map.put(nv.getName(), nv.getValue());
        }
        
        assertNotNull("The parameter 'uri' of redirect_uri should not be null.", map.get("uri"));
        assertEquals("The referrer should be matched.", referrer, map.get("uri"));
    }
    
    @Test
    public void testPopulatePageList() {
        
        // input arguments
        final String fbUserId = "123456";
        final String accessToken = "accessToken";
        
        // mock data
        final int responseCode = HttpURLConnection.HTTP_OK;
        final FacebookResponse response = new FacebookResponse();
        
        // mock object
        URL url = PowerMockito.mock(URL.class);
        HttpURLConnection connection =  Mockito.mock(HttpURLConnection.class);
        ObjectMapper mapper = Mockito.mock(ObjectMapper.class);
        InputStream inputStream = Mockito.mock(InputStream.class);
        
        // stubs
        try {
            PowerMockito.whenNew(URL.class).withArguments(anyString()).thenReturn(url);
            when(url.openConnection()).thenReturn(connection);
            when(connection.getResponseCode()).thenReturn(responseCode);
            PowerMockito.whenNew(ObjectMapper.class).withNoArguments().thenReturn(mapper);
            when(connection.getInputStream()).thenReturn(inputStream);
            when(mapper.readValue((InputStream) anyObject(), eq(FacebookResponse.class))).thenReturn(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // execute
        FacebookResponse actual = FacebookLib.populatePageList(fbUserId, accessToken);
        
        // verify
        ArgumentCaptor<String> captureFullpath = ArgumentCaptor.forClass(String.class);
        try {
            PowerMockito.verifyNew(URL.class).withArguments(captureFullpath.capture());
            //verify(url).openConnection(); // don't know why can't verify this
            verify(connection).getResponseCode();
            PowerMockito.verifyNew(ObjectMapper.class).withNoArguments();
            verify(connection).getInputStream();
            verify(mapper).readValue(inputStream, FacebookResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        String actualFullpath = captureFullpath.getValue();
        String expectedFullpath = "https://graph.facebook.com/" + fbUserId + 
                "/accounts?access_token=" + NnStringUtil.urlencode(accessToken, NnStringUtil.ASCII) + "&type=page";
        NnTestUtil.assertEqualURL(expectedFullpath, actualFullpath);
        assertEquals("The response should be matched.", response, actual);
    }

}
