package com.nncloudtv.service;

import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nncloudtv.dao.NnContentDao;
import com.nncloudtv.lib.NnDateUtil;
import com.nncloudtv.model.NnContent;

@Service
public class NnContentManager {

    protected static final Logger log = Logger.getLogger(NnContentManager.class.getName());
    
    private NnContentDao nnContentDao = new NnContentDao();
    
    public NnContent create(NnContent content) {
        NnContent existed = this.findByItemAndLang(content.getItem(), content.getLang(), content.getMsoId());
        if (existed != null) {
            content.setValue(content.getValue());
            this.save(existed);
            return existed;
        }
        content.setCreateDate(NnDateUtil.now());
        return save(content);
    }
    
    public NnContent save(NnContent content) {
        content.setUpdateDate(NnDateUtil.now());
        return nnContentDao.save(content);
    }

    public NnContent findByItemAndLang(String item, String lang, long msoId) {        
        return nnContentDao.findByItemAndLang(item, lang, msoId);
    }

    public NnContent findById(long id) {
        return nnContentDao.findById(id);
    }
    
    public List<NnContent> findAll() {        
        return nnContentDao.findAll();
    }
    
}
