package com.nncloudtv.model;

import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.nncloudtv.lib.NnStringUtil;

/**
 * sub-episode's title card. 
 * each program can have a "before" and "after" card.
 */
@PersistenceCapable(table = "title_card", detachable = "true")
public class TitleCard implements PersistentBaseModel {
    
    private static final long serialVersionUID = -9147138183447630963L;
    private static final boolean cachable = false;
    
    public boolean isCachable() {
        return cachable;
    }
    
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private long id;
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    @Persistent
    private long channelId;
    
    @Persistent
    private long programId;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String duration;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.VERY_LONG_STRING_LENGTH)
    private String message;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.SHORT_STRING_LENGTH)
    private String size; // font size
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.SHORT_STRING_LENGTH)
    private String color; // font color
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.SHORT_STRING_LENGTH)
    private String effect;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.SHORT_STRING_LENGTH)
    private String align;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.SHORT_STRING_LENGTH)
    private String bgColor;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.SHORT_STRING_LENGTH)
    private String style;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.SHORT_STRING_LENGTH)
    private String weight; // font weight
    
    @Persistent
    private short type; //0 begin, 1 end    
    public static short TYPE_BEGIN = 0;
    public static short TYPE_END = 1;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String bgImage;
    
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.EXTENDED_STRING_LENGTH)
    private String playerSyntax;
    
    @Persistent
    private Date updateDate;
    
    public static final String DEFAULT_MESSAGE  = "My Video";
    public static final String DEFAULT_ALIGN    = "center";
    public static final String DEFAULT_EFFECT   = "none";
    public static final String DEFAULT_DURATION = "7";
    public static final String DEFAULT_SIZE     = "20";
    public static final String DEFAULT_COLOR    = "white";
    public static final String DEFAULT_STYLE    = "normal";
    public static final String DEFAULT_WEIGHT   = "normal";
    public static final String DEFAULT_BG_COLOR = "black";
    public static final String DEFAULT_BG_IMG   = "http://9x9ui.s3.amazonaws.com/war/v0/images/titlecard-default.png";
    
    public TitleCard(long channelId, long programId, short type) {
        
        this.channelId = channelId;
        this.programId = programId;
        this.type = type;
    }
    
    public Date getUpdateDate() {
        return updateDate;
    }
    
    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }
    
    public String getBgColor() {
        return bgColor;
    }
    
    public void setBgColor(String bgColor) {
        this.bgColor = bgColor;
    }
    
    public long getChannelId() {
        return channelId;
    }
    
    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }
    
    public String getDuration() {
        return duration;
    }
    
    public int getDurationInt() {
        
        if (this.duration == null) {
            return 0;
        }
        
        int duration = 0;
        try {
            duration = Integer.valueOf(this.duration);
        } catch (NumberFormatException e) {
        }
        
        return duration;
    }
    
    public void setDuration(String duration) {
        this.duration = duration;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getColor() {
        return color;
    }
    
    public void setColor(String color) {
        this.color = color;
    }
    
    public String getStyle() {
        return style;
    }
    
    public void setStyle(String style) {
        this.style = style;
    }
    
    public short getType() {
        return type;
    }
    
    public void setType(short type) {
        this.type = type;
    }
    
    public String getPlayerSyntax() {
        if (getMessage() == null) 
            return null;
        String syntax = "";
        String breakEncoding = NnStringUtil.urlencode("\n");
        String cardmsg = "";
        if (getMessage() != null) {
            cardmsg = NnStringUtil.urlencode(getMessage());
        }
        syntax += "message: " + cardmsg + breakEncoding;
        if (getType() == TitleCard.TYPE_BEGIN)
            syntax += "type: begin" + breakEncoding;
        else
            syntax += "type: end" + breakEncoding;
        if (getDuration() != null)
            syntax += "duration: " + getDuration() + breakEncoding;
        if (getStyle() != null)
            syntax += "style: " + getStyle() + breakEncoding;
        if (getSize() != null)
            syntax += "size: " + getSize() + breakEncoding;
        if (getColor() != null)
            syntax += "color: " + getColor() + breakEncoding;
        if (getEffect() != null)
            syntax += "effect: " + getEffect() + breakEncoding;
        if (getAlign() != null)
            syntax += "align: " + getAlign() + breakEncoding;
        if (getBgColor() != null)
            syntax += "bgcolor: " + getBgColor() + breakEncoding;
        if (getBgImage() != null)
            syntax += "bgimage: " + getBgImage() + breakEncoding;
        if (getWeight() != null)
            syntax += "weight: " + getWeight() + breakEncoding;
        
        System.out.println("syntax in title card:" + syntax);
        
        return syntax;        
        //return playerSyntax;
    }
    
    public void setPlayerSyntax(String playerSyntax) {
        this.playerSyntax = playerSyntax;
    }
    
    public String getSize() {
        return size;
    }
    
    public void setSize(String size) {
        this.size = size;
    }
    
    public String getBgImage() {
        return bgImage;
    }
    
    public void setBgImage(String bgImage) {
        this.bgImage = bgImage;
    }
    
    public String getEffect() {
        return effect;
    }
    
    public void setEffect(String effect) {
        this.effect = effect;
    }
    
    public String getAlign() {
        return align;
    }
    
    public void setAlign(String align) {
        this.align = align;
    }
    
    public long getProgramId() {
        return programId;
    }
    
    public void setProgramId(long programId) {
        this.programId = programId;
    }
    
    public String getWeight() {
        return weight;
    }
    
    public void setWeight(String weight) {
        this.weight = weight;
    }
    
}
