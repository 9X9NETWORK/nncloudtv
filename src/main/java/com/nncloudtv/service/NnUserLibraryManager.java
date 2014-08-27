package com.nncloudtv.service;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nncloudtv.dao.NnUserLibraryDao;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.model.NnUser;
import com.nncloudtv.model.NnUserLibrary;

@Service
public class NnUserLibraryManager {
    
    protected static final Logger log = Logger.getLogger(NnUserLibraryManager.class.getName());
    
    protected NnUserLibraryDao dao = NNF.getLibraryDao();
    
    public NnUserLibrary save(NnUserLibrary library) {
        
        if (library == null) return null;
        
        Date now = new Date();
        
        if (library.getCreateDate() == null) {
            
            library.setCreateDate(now);
        }
        library.setUpdateDate(now);
        
        return dao.save(library);
    }
    
    public List<NnUserLibrary> findByUser(NnUser user) {
        
        return dao.findByUserIdStr(user.getIdStr());
    }
    
    public NnUserLibrary findById(String idStr) {
        
        return dao.findById(idStr);
    }
    
    public void delete(NnUserLibrary library) {
        
        dao.delete(library);
    }
}
