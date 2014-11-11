package com.nncloudtv.model;

/** 
 * email object
 */
public class NnEmail {
    
    public final static String TO_EMAIL_FEEDBACK    = "feedback@flipr.tv";
    public final static String TO_EMAIL_PAIDSUPPORT = "paidsupport@flipr.tv";
    private String toEmail;
    
    public final static String TO_NAME_FEEDBACK    = "Feedback";
    public final static String TO_NAME_PAIDSUPPORT = "Paid Support";
    private String toName;
    
    public final static String SEND_EMAIL_SHARE      = "share@9x9.tv";
    public final static String SEND_EMAIL_NNCLOUDTV  = "nncloudtv@gmail.com";
    public final static String SEND_EMAIL_VIDCON2014 = "vidcon2014@flipr.tv";
    public final static String SEND_EMAIL_SYSTEM     = "system@flipr.tv";
    public final static String SEND_EMAIL_CMS        = "cms@flipr.tv";
    private String senderEmail;
    
    public final static String SEND_NAME_NNCLOUDTV = "nncloudtv";
    public final static String SEND_NAME_FLIPR     = "FLIPr";
    public final static String SEND_NAME_SYSTEM    = "System";
    public final static String SEND_NAME_CMS       = "CMS";
    private String senderName;
    
    public final static String REPLY_EMAIL_NOREPLY = "noreply@notify.9x9.tv";
    private String replyToEmail;
    
    private String subject;
    
    private String body;
    
    private boolean isHtml;
    
    public NnEmail(String toEmail,      String toName, 
                   String senderEmail,  String senderName, 
                   String replyToEmail, String subject,    String body) {
        
        this.toEmail      = toEmail;
        this.toName       = toName;
        this.senderEmail  = senderEmail;
        this.senderName   = senderName;
        this.replyToEmail = replyToEmail;
        this.subject      = subject;
        this.body         = body;
    }
    
    public String getToEmail() {
        return toEmail;
    }

    public void setToEmail(String toEmail) {
        this.toEmail = toEmail;
    }

    public String getToName() {
        return toName;
    }

    public void setToName(String toName) {
        this.toName = toName;
    }

    public String getSendEmail() {
        return senderEmail;
    }

    public void setSendEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getSendName() {
        return senderName;
    }

    public void setSendName(String senderName) {
        this.senderName = senderName;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getReplyToEmail() {
        return replyToEmail;
    }

    public void setReplyToEmail(String replyToEmail) {
        this.replyToEmail = replyToEmail;
    }

    public boolean isHtml() {
        return isHtml;
    }

    public void setHtml(boolean isHtml) {
        this.isHtml = isHtml;
    }
    
}
