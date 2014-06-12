package com.nncloudtv.web.json.cms;

public class CreditCard {
    
    String cardNumber;
    
    String cardHolderName;
    
    String expires;
    
    String veridicationCode;
    
    public String getCardNumber() {
        return cardNumber;
    }
    
    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }
    
    public String getCardHolderName() {
        return cardHolderName;
    }
    
    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }
    
    public String getExpires() {
        return expires;
    }
    
    public void setExpires(String expires) {
        this.expires = expires;
    }
    
    public String getVeridicationCode() {
        return veridicationCode;
    }
    
    public void setVeridicationCode(String veridicationCode) {
        this.veridicationCode = veridicationCode;
    }
    
}
