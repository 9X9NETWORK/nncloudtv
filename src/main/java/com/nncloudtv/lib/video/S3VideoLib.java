package com.nncloudtv.lib.video;

import java.io.InputStream;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.nncloudtv.lib.AmazonLib;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.model.Mso;
import com.nncloudtv.service.MsoConfigManager;

public class S3VideoLib implements VideoLib {
    
    protected final static Logger log = Logger.getLogger(S3VideoLib.class.getName());
    
    public boolean isUrlMatched(String url) {
        
        return (url == null) ? null : url.matches(AmazonLib.REGEX_S3_URL);
    }
    
    public String getDirectVideoUrl(String url) {
        
        // always return null, because video hosted on S3 should be private
        return null;
    }
    
    public InputStream getDirectVideoStream(String url) {
        
        if (url == null) { return null; }
        Matcher matcher = Pattern.compile(AmazonLib.REGEX_S3_URL).matcher(url);
        if (matcher.find()) {
            
            String bucket = matcher.group(1);
            String filename = matcher.group(2);
            Mso mso = NNF.getConfigMngr().findMsoByVideoBucket(bucket);
            AWSCredentials credentials = new BasicAWSCredentials(MsoConfigManager.getAWSId(mso), MsoConfigManager.getAWSKey(mso));
            AmazonS3 s3 = new AmazonS3Client(credentials);
            try {
                
                return s3.getObject(new GetObjectRequest(bucket, filename)).getObjectContent();
                
            } catch (AmazonS3Exception e) {
                
                log.warning(e.getClass().getName());
                log.warning(e.getMessage());
            }
        }
        
        return null;
    }
}
