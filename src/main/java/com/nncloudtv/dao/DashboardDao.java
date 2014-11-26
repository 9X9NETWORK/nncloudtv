package com.nncloudtv.dao;

import java.util.List;

import com.nncloudtv.model.Dashboard;

public class DashboardDao  extends GenericDao<Dashboard> {
    
    public DashboardDao() {
        super(Dashboard.class);
    }
    
    public List<Dashboard> findFrontpage(short baseTime, long msoId) {
        
        String query = "SELECT * FROM dashboard "
                     + "        WHERE msoId = " + msoId 
                     + "          AND (((timeStart != 0 OR timeEnd != 0) AND timeEnd > " + baseTime
                     + "                AND timeStart <= " + baseTime + ")"
                     + "                OR (timeStart = 0 and timeEnd = 0)) " 
                     + "     ORDER BY seq";
        
        return sql(query);
    }
    
}
