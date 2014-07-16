package com.nncloudtv.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nncloudtv.dao.NnUserProfileDao;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.model.NnUser;
import com.nncloudtv.model.NnUserProfile;
import com.nncloudtv.web.json.player.PlayerUserProfile;

@Service
public class NnUserProfileManager {

    protected static final Logger log = Logger.getLogger(NnUserProfileManager.class.getName());
    
    private NnUserProfileDao dao = NNF.getProfileDao();
    
    public NnUserProfile findByUser(NnUser user) {
        if (user == null)
            return null;
        return dao.findByUser(user);
    }
    
    public NnUserProfile findByUserIdAndMsoId(Long userId, Long msoId) {
        if (userId == null || msoId == null) {
            return null;
        }
        return dao.findByUserIdAndMsoId(userId, msoId);
    }
    
    public List<NnUserProfile> findByUserId(Long userId) {
        
        if (userId == null) {
            return new ArrayList<NnUserProfile>();
        }
        
        List<NnUserProfile> results = dao.findByUserId(userId);
        if (results == null) {
            return new ArrayList<NnUserProfile>();
        }
        return results;
    }
    
    public NnUserProfile save(NnUser user, NnUserProfile profile) {
        if (profile == null)
            return null;
        return dao.save(user, profile);
    }
    
    public Set<NnUserProfile> search(String keyword, int start, int limit) {
        
        return dao.search(keyword, start, limit);
        
    }
    
    /** return if this user has super priv to access PCS */
    public NnUserProfile pickSuperProfile(Long userId) {
        
        if (userId == null) {
            return null;
        }
        
        NnUserProfile target = null;
        List<NnUserProfile> profiles = findByUserId(userId);
        if (profiles == null || profiles.size() == 0) {
            return null;
        } else {
            for (NnUserProfile profile : profiles) {
                if (profile.getPriv() != null && profile.getPriv().startsWith("111")) { // logic hard coded
                    if (target == null) {
                        target = profile;
                    } else {
                        // multiple assigned 
                        target = profile;
                        log.warning("this userId : " + userId + " has multiple super profile and this func cant choose approriate one");
                    }
                }
            }
        }
        
        return target;
    }

    public Object getPlayerProfile(NnUser user, short format) {
        NnUserProfile profile = user.getProfile();
        String name = profile.getName();
        String email = user.getUserEmail();
        String intro = profile.getIntro();
        String imageUrl = profile.getImageUrl();
        String gender = "";
        if (profile.getGender() != 2)
        	gender = String.valueOf(profile.getGender());
        String year = String.valueOf(profile.getDob());
        String sphere = profile.getSphere();
        String uiLang = profile.getLang();
        String phone = profile.getPhoneNumber();
        if (format == PlayerApiService.FORMAT_PLAIN) {
            String[] result = {""};     
	        result[0] += PlayerApiService.assembleKeyValue("name", name);
	        result[0] += PlayerApiService.assembleKeyValue("email", email);
	        result[0] += PlayerApiService.assembleKeyValue("description", intro);
	        result[0] += PlayerApiService.assembleKeyValue("image", imageUrl);
	        result[0] += PlayerApiService.assembleKeyValue("gender", gender);
	        result[0] += PlayerApiService.assembleKeyValue("year", year);
	        result[0] += PlayerApiService.assembleKeyValue("sphere", sphere);
	        result[0] += PlayerApiService.assembleKeyValue("ui-lang", uiLang);
	        result[0] += PlayerApiService.assembleKeyValue("phone", phone);
	        return result;
        } else {
        	PlayerUserProfile json = new PlayerUserProfile();
        	json.setName(name);
        	json.setEmail(email);
        	json.setDescription(intro);
        	json.setImage(imageUrl);
        	json.setGender(gender);
        	json.setYear(year);
        	json.setSphere(sphere);
        	json.setUiLang(uiLang);
        	json.setPhone(phone);
        	return json;
        }
        
    }
    
}
