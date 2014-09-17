package com.nncloudtv.lib;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.Security;
import java.security.SignatureException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;

import org.jets3t.service.CloudFrontService;
import org.jets3t.service.CloudFrontServiceException;
import org.jets3t.service.utils.ServiceUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.google.api.client.util.Base64;
import com.nncloudtv.service.MsoConfigManager;

public class AmazonLib {
    
    protected static final Logger log = Logger.getLogger(AmazonLib.class.getName());
    
    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
    
    public static final String AWS_TOKEN = "QWJvdXQtTWU=";
    public static final String S3_TOKEN = "YWJvdXR5b3U=";
    public static final String S3_CONTEXT_CODE = "aHR0cDovL2dvby5nbC9lVTNtWg==";
    public static final String S3_EXT = "WW91IEFyZSBOb3QgQWxvbmU=";
    public static final String REGEX_S3_URL = "^https?:\\/\\/(.+)\\.s3\\.amazonaws\\.com\\/(.+)$";
    
    /**
    * Computes RFC 2104-compliant HMAC signature.
    * * @param data
    * The data to be signed.
    * @return
    * The Base64-encoded RFC 2104-compliant HMAC signature.
    * @throws
    * java.security.SignatureException when signature generation fails
    */
    public static String calculateRFC2104HMAC(String data, String key) throws java.security.SignatureException {
        
        String result = null;
        
        try {
            
            // get an hmac_sha1 key from the raw key bytes
            SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), HMAC_SHA1_ALGORITHM);
            
            // get an hmac_sha1 Mac instance and initialize with the signing key
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(signingKey);
            
            // compute the hmac on input data bytes
            byte[] rawHmac = mac.doFinal(data.getBytes());
            
            // base64-encode the hmac            
            byte[] policy;
            try {
                policy = Base64.encode(rawHmac);
                result = new String(policy, "UTF8");            
            } catch (UnsupportedEncodingException e) {
                log.info("unsupported encoding:" + e.getMessage());        
            }                                        
            log.info(result);
            
        } catch (Exception e) {
            throw new SignatureException("Failed to generate HMAC : " + e.getMessage());
        }
        return result;
    }
    
    public static String buildS3Policy(String bucket, String acl, String contentType, long size) {
        
        String result = "";
        result += "{ 'expiration': '" + AmazonLib.getFormattedExpirationDate() + "',";
        result += "'conditions': [";
        result += "{ 'bucket': '" + bucket + "' },";
        result += "[ 'starts-with', '$key', ''],";
        result += "{ 'acl': '" + acl + "' },";
        result += "[ 'starts-with', '$Content-Type', '" + contentType + "' ],";
        result += "{ 'success_action_status': '201' },";
        result += "[ 'starts-with', '$Filename', '' ],";
        result += "[ 'content-length-range', 0, " + size + " ],";
        result += "]";
        result += "}";
        
        log.info(result);
        byte[] policy;
        String roundTrip = "";
        try {
            policy = Base64.encode(result.getBytes("UTF8"));
            roundTrip = new String(policy, "UTF8");
        } catch (UnsupportedEncodingException e) {
            log.info("unsupported encoding:" + e.getMessage());
        }
        return roundTrip;
    }
    
    public static String buildS3Policy(String buket, String acl, String contentType) {
        
        String result = "";
        result += "{ 'expiration': '" + AmazonLib.getFormattedExpirationDate() + "',";
        result += "'conditions': [";
        result += "{ 'bucket': '" + buket + "' },";
        result += "[ 'starts-with', '$key', ''],";
        result += "{ 'acl': '" + acl + "' },";
        result += "[ 'starts-with', '$Content-Type', '" + contentType + "' ],";
        result += "{ 'success_action_status': '201' },";
        result += "[ 'starts-with', '$Filename', '' ],";
        result += "[ 'content-length-range', 0, 1073741824 ],"; // 1 GB
        result += "]";
        result += "}";
        
        log.info(result);
        byte[] policy;
        String roundTrip = "";
        try {
            policy = Base64.encode(result.getBytes("UTF8"));
            roundTrip = new String(policy, "UTF8");            
        } catch (UnsupportedEncodingException e) {
            log.info("unsupported encoding:" + e.getMessage());        
        }                
        return roundTrip;
    }
    
    public static String decodeS3Token(String token, Date timestamp, byte[] salt, long rand) throws IOException {
        
        byte[] encrypt = AuthLib.encryptPassword(token, salt);
        String pattern = timestamp.toString() + String.valueOf(rand);
        int radius = pattern.length();
        String perimeter = pattern.substring(radius) + token;
        int len1 = encrypt.length, len2 = salt.length;
        if (len1 < len2) {
            encrypt[(len2 % len1)] = salt[len1];
        } else {
            salt[(len1 % len2)] = encrypt[len2 - 1];
        }
        log.info("length = " + perimeter.length());
        
        return org.datanucleus.util.Base64.decodeString(perimeter);
    }
    
    public static String getFormattedExpirationDate() {
        Date now = new Date();
        Date oneHourFromNow = new Date(now.getTime() + 3600 * 1000);
        TimeZone tz = TimeZone.getTimeZone( "UTC" );
        SimpleDateFormat dfm = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        dfm.setTimeZone(tz);
        String formattedExpirationDate = dfm.format(oneHourFromNow);
        return formattedExpirationDate;
    }
    
    public static String s3Upload(String bucket, String filename, BufferedImage image) throws IOException {
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        log.info("image size = " + baos.size());
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("image/png");
        metadata.setContentLength(baos.size());
        
        return s3Upload(MsoConfigManager.getS3DepotBucket(), filename, bais, metadata);
    }
    
    public static String s3Upload(String bucket, String filename, InputStream in, ObjectMetadata metadata)
            throws AmazonClientException, AmazonServiceException {
        
        AWSCredentials credentials = new BasicAWSCredentials(MsoConfigManager.getAWSId(), MsoConfigManager.getAWSKey());
        AmazonS3 s3 = new AmazonS3Client(credentials);
        s3.putObject(bucket, filename, in, metadata);
        
        s3.setObjectAcl(bucket, filename, CannedAccessControlList.PublicRead);
        
        return "http://" + bucket + ".s3.amazonaws.com/" + filename;
    }
    
    public static String cfUrlSignature(String domain, String filePath, String keyPair, String s3Obj) {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        String distributionDomain = domain; //d31cfzp2gk0td6.cloudfront.net
        String privateKeyFilePath = filePath; ///var/opt/cf/rsa-private-key.der
        String s3ObjectKey = s3Obj; //"_DSC0006-X3.jpg" or "layer1/_DSC0006-X3.jpg"
        String signedUrlCanned = null;
        //expire in one day
        DateTime now = new DateTime(DateTimeZone.UTC);
        DateTime expires = new DateTime(now).plusDays(1);        
        
        // Convert your DER file into a byte array.
        try {
            byte[] derPrivateKey = ServiceUtils.readInputStreamToBytes(new
                FileInputStream(privateKeyFilePath));    
            String keyPairId = keyPair; //APKAJAMGXWAZ24S62ZSA;
            // Generate a "canned" signed URL to allow access to a 
            // specific distribution and object
            signedUrlCanned = CloudFrontService.signUrlCanned(
                "http://" + distributionDomain + "/" + s3ObjectKey, // Resource URL or Path
                keyPairId ,    // Certificate identifier, 
                               // an active trusted signer for the distribution
                derPrivateKey, // DER Private key data
                ServiceUtils.parseIso8601Date(expires.toString()) // DateLessThan
                );
            log.info("signed canned:" + signedUrlCanned);    
        } catch (IOException e) {
            System.out.println(e);
        } catch (CloudFrontServiceException e) {
            System.out.println(e);
        } catch (ParseException e) {
            System.out.println(e);
        }
        return signedUrlCanned;
    }
}
