package com.nncloudtv.web.filter;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

import com.nncloudtv.exception.NnNotSupportedRegionException;
import com.nncloudtv.model.LocaleTable;
import com.nncloudtv.web.api.ApiContext;

public class RegionFilter extends OncePerRequestFilter {
    
    protected static final Logger log = Logger.getLogger(RegionFilter.class.getName());
    
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain filterChain)
            throws ServletException, IOException {
        
        String sphere = req.getParameter(ApiContext.PARAM_SPHERE);
        String region = req.getParameter(ApiContext.PARAM_REGION);
        if (sphere != null && region == null) {
            try {
                req.setAttribute(ApiContext.PARAM_REGION, LocaleTable.sphere2region(sphere));
            } catch (NnNotSupportedRegionException e) {
                log.warning(e.getMessage());
            }
        }
        
        filterChain.doFilter(req, resp);
    }
    
}
