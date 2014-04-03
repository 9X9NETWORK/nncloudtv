package com.nncloudtv.lib;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.nncloudtv.model.NnChannel;
import com.nncloudtv.service.MsoConfigManager;

public class SearchLib {
    protected static final Logger log = Logger.getLogger(SearchLib.class.getName());
           
    // search criteria
    public static final String STORE_ONLY = "store_only";
    public static final String YOUTUBE_ONLY = "youtube";
    
    public static final String CORE_NNCLOUDTV = "nncloudtv";
    public static final String CORE_YTCHANNEL = "ytchannel";
    /**
     * General search
     * 
     * @param keyword search keyword
     * @param content null or "youtube" or "store-only" 
     * @param extra null or example "sphere:(zh OR other)"
     * @param all true or false. false for store. true for cms
     * @param start start from 0
     * @param limit count
     * @return Stack => Long<Id> (channelId), total number found
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Stack search(String core, String keyword, String content, String extra, boolean all, int start, int limit) {
    	log.info("search content:" + content);
        SolrServer server = getSolrServer(core);
        SolrQuery query = new SolrQuery();
        String queryStr = "\"" + keyword + "\"";
        query.set("defType", "edismax");
        query.setQuery(queryStr);
        query.setFields("id, name");
        query.addSort("updateDate", SolrQuery.ORDER.desc);
        query.set("qf", "name^5 intro");
        if (!all) {
        	query.addFilterQuery("isPublic:true");
            query.addFilterQuery("status:(" + NnChannel.STATUS_SUCCESS + " OR " + NnChannel.STATUS_WAIT_FOR_APPROVAL + ")");
        }        
        if (content != null) {
            if (content.contains("youtube")) {
                query.addFilterQuery("contentType:(" + NnChannel.CONTENTTYPE_YOUTUBE_CHANNEL + " OR " + NnChannel.CONTENTTYPE_YOUTUBE_PLAYLIST + ")");
                log.info("query youtube only");
            }
            if (content.contains("9x9")) {
                query.addFilterQuery("contentType:" + NnChannel.CONTENTTYPE_MIXED);
                log.info("query 9x9 only");
            }
            if (content.contains("store_only")) {
                query.addFilterQuery("status:" + NnChannel.STATUS_SUCCESS);
            }
        }
        if (extra != null) {
            //example: extra = "sphere:(zh or other)" 
            query.addFilterQuery(extra);
        }
        query.setStart(start);
        query.setRows(limit);             
        Stack st = new Stack();        
        List<Long> ids = new ArrayList<Long>();        
        Long numFound = 0L;
        try {
            QueryResponse response = server.query(query);
            SolrDocumentList docList= response.getResults();
            JSONArray jArray =new JSONArray();
            docList = response.getResults();
            log.info("solr status code:" + response.getStatus());
            numFound = docList.getNumFound();
            st.push(numFound);
            log.info("solr numFound:" + numFound + ";doc size:" + docList.size());
            for (int i = 0; i < docList.size(); i++) {
                 JSONObject json = new JSONObject(docList.get(i));
                 jArray.put(json);                   
                 ids.add(json.getLong("id"));
System.out.println("return ids:" + json.getLong("id"));                 
            }
            st.push(ids);
        } catch (SolrServerException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return st;        
    }
    
    private static SolrServer getSolrServer(String core) {
        String host = MsoConfigManager.getSearchNnChannelServer();
    	if (core != null && core.equals(SearchLib.CORE_YTCHANNEL))
            host = MsoConfigManager.getSearchPoolServer();
        SolrServer server = new HttpSolrServer(host);
        return server;
    }    
    
    public static void solrUpdate(NnChannel c) {
        SolrServer server = getSolrServer(SearchLib.CORE_NNCLOUDTV);        
        SolrInputDocument doc = new SolrInputDocument();        
        doc.addField("id", c.getId());
        doc.addField("name", c.getName());        
        try {
            server.add(doc);
            UpdateResponse upres = server.commit();
            System.out.println(upres.getResponse());
        } catch (SolrServerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }        
    }
}