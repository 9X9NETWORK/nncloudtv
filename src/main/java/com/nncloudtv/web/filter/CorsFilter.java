package com.nncloudtv.web.filter;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Logger;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

import com.nncloudtv.service.CounterFactory;

public class CorsFilter extends OncePerRequestFilter {
    
    protected static final Logger log = Logger.getLogger(CorsFilter.class.getName());
    
    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain filterChain)
            throws ServletException, IOException {
        
        if (req.getHeader("Origin") != null) {
            resp.addHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
            resp.addHeader("Access-Control-Allow-Credentials", "true");
        } else if (req.getHeader("Referer") != null) {
            URL refererUrl = new URL(req.getHeader("Referer"));
            String domain = refererUrl.getProtocol() + "://" + refererUrl.getHost();
            if (refererUrl.getPort() != -1) {
                domain = domain + ":" + refererUrl.getPort();
            }
            resp.addHeader("Access-Control-Allow-Origin", domain);
            resp.addHeader("Access-Control-Allow-Credentials", "true");
        }
        
        if ((req.getHeader("Access-Control-Request-Method") != null) && (req.getMethod().equals("OPTIONS"))) {
            // CORS "pre-flight" request
            resp.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
            resp.addHeader("Access-Control-Allow-Headers", "content-type, cookie");
            resp.addHeader("Access-Control-Max-Age", "1728000");
            return ;
        }
        
        CounterFactory.increment("[api] " + req.getMethod() + " " + req.getRequestURI()); // count api usage
        
        filterChain.doFilter(req, resp);
    }
    
}
