package com.nncloudtv.dao;

import java.util.List;
import java.util.logging.Logger;

import com.nncloudtv.model.SysTag;

public class SysTagDao extends GenericDao<SysTag> {

    protected static final Logger log = Logger.getLogger(SysTagDao.class.getName());
    
    public SysTagDao() {
        super(SysTag.class);
    }
    
    public List<SysTag> findByMsoIdAndType(long msoId, short type) {
        
        return sql(String.format("SELECT * FROM systag WHERE msoId = %d AND type = %d ORDER BY seq asc", msoId, type));
    }
    
    // see SysTagMapDao.findCategoryMapsByChannelId
    public List<SysTag> findCategoriesByChannelId(long channelId, long msoId) {
    
        String query = " select * from systag a1"
                     + " inner join (select s.id from systag_map m, systag s "
                     + "             where s.type = "      + SysTag.TYPE_CATEGORY
                     + "               and s.msoId = "     + msoId
                     + "               and m.channelId = " + channelId
                     + "               and s.id = m.systagId) a2 on a1.id = a2.id ";
        
        return sql(query);
    }
}
