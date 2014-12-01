package com.nncloudtv.service;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nncloudtv.dao.MyLibraryDao;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnDateUtil;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.NnUser;
import com.nncloudtv.model.MyLibrary;

@Service
public class MyLibraryManager {
    
    protected static final Logger log = Logger.getLogger(MyLibraryManager.class.getName());
    
    protected MyLibraryDao dao = NNF.getLibraryDao();
    
    public MyLibrary save(MyLibrary library) {
        
        if (library == null) return null;
        
        Date now = NnDateUtil.now();
        
        if (library.getCreateDate() == null) {
            
            library.setCreateDate(now);
        }
        library.setUpdateDate(now);
        
        return dao.save(library);
    }
    
    public List<MyLibrary> findByMso(Mso mso) {
        
        return dao.findByMsoId(mso.getId());
    }
    
    public List<MyLibrary> findByUser(NnUser user) {
        
        return dao.findByUserIdStr(user.getIdStr());
    }
    
    public MyLibrary findById(String idStr) {
        
        return dao.findById(idStr);
    }
    
    public void delete(MyLibrary library) {
        
        dao.delete(library);
    }
    
    public void reorderMsoLibrary(long msoId) {
        
        List<MyLibrary> library = dao.findByMsoId(msoId);
        
        for (int i = 0; i < library.size(); i++) {
            
            library.get(i).setSeq((short) (i + 1));
        }
        
        dao.saveAll(library);
    }
}
