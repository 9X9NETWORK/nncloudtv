package com.nncloudtv.wrapper;

import com.nncloudtv.lib.NNF;
import com.nncloudtv.service.MsoConfigManager;
import com.nncloudtv.service.MsoManager;
import com.nncloudtv.service.NnUserManager;
import com.nncloudtv.service.NnUserProfileManager;

public class NNFWrapper  extends NNF {
    
    public static void setMsoMngr(MsoManager msoMngr) {
        NNF.msoMngr = msoMngr;
    }
    
    public static void setConfigMngr(MsoConfigManager configMngr) {
        NNF.configMngr = configMngr;
    }
    
    public static void setProfileMngr(NnUserProfileManager profileMngr) {
        NNF.profileMngr = profileMngr;
    }
    
    public static void setUserMngr(NnUserManager userMngr) {
        NNF.userMngr = userMngr;
    }

}
