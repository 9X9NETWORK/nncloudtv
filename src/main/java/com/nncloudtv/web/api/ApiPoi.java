package com.nncloudtv.web.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.NnProgram;
import com.nncloudtv.model.NnUser;
import com.nncloudtv.model.Poi;
import com.nncloudtv.model.PoiCampaign;
import com.nncloudtv.model.PoiEvent;
import com.nncloudtv.model.PoiPoint;
import com.nncloudtv.service.TagManager;

@Controller
@RequestMapping("api")
public class ApiPoi extends ApiContext {
    
    public ApiPoi(HttpServletRequest req) {
        super(req);
    }
    
    protected static Logger log = Logger.getLogger(ApiPoi.class.getName());
    
    @RequestMapping(value = "users/{userId}/poi_campaigns", method = RequestMethod.GET)
    public @ResponseBody
    List<PoiCampaign> userCampaigns(HttpServletRequest req,
            HttpServletResponse resp,
            @PathVariable("userId") String userIdStr) {
        
        Long userId = NnStringUtil.evalLong(userIdStr);
        if (userId == null) {
            notFound(resp, ApiContext.INVALID_PATH_PARAMETER);
            return null;
        }
        
        NnUser user = ApiContext.getAuthenticatedUser(req);
        if (user == null) {
            unauthorized(resp);
            return null;
        } else if (user.getId() != userId) {
            forbidden(resp);
            return null;
        }
        
        List<PoiCampaign> results = NNF.getPoiCampaignMngr().findByUserId(userId);
        if (results == null) {
            return new ArrayList<PoiCampaign>();
        }
        
        for (PoiCampaign result : results) {
            result.setName(NnStringUtil.revertHtml(result.getName()));
        }
        
        return results;
    }
    
    @RequestMapping(value = "users/{userId}/poi_campaigns", method = RequestMethod.POST)
    public @ResponseBody
    PoiCampaign userCampaignCreate(HttpServletRequest req,
            HttpServletResponse resp,
            @PathVariable("userId") String userIdStr) {
        
        Long userId = NnStringUtil.evalLong(userIdStr);
        if (userId == null) {
            notFound(resp, ApiContext.INVALID_PATH_PARAMETER);
            return null;
        }
        
        NnUser user = ApiContext.getAuthenticatedUser(req);
        if (user == null) {
            unauthorized(resp);
            return null;
        } else if (user.getId() != userId) {
            forbidden(resp);
            return null;
        }
        
        // name
        String name = req.getParameter("name");
        if (name == null) {
            badRequest(resp, ApiContext.MISSING_PARAMETER);
            return null;
        }
        name = NnStringUtil.htmlSafeAndTruncated(name);
        
        PoiCampaign campaign = new PoiCampaign();
        campaign.setMsoId(user.getMsoId());
        campaign.setUserId(userId);
        campaign.setName(name);
        
        // startDate
        Long startDateLong = null;
        String startDateStr = req.getParameter("startDate");
        if (startDateStr != null) {
            
            startDateLong = NnStringUtil.evalLong(startDateStr);
            if (startDateLong == null) {
                badRequest(resp, ApiContext.INVALID_PARAMETER);
                return null;
            }
        }
        
        // endDate
        Long endDateLong = null;
        String endDateStr = req.getParameter("endDate");
        if (endDateStr != null) {
            
            endDateLong = NnStringUtil.evalLong(endDateStr);
            if (endDateLong == null) {
                badRequest(resp, ApiContext.INVALID_PARAMETER);
                return null;
            }
        }
        
        if (startDateStr != null && endDateStr != null) {
            if (endDateLong < startDateLong) { 
                badRequest(resp, ApiContext.INVALID_PARAMETER);
                return null;
            }
            campaign.setStartDate(new Date(startDateLong));
            campaign.setEndDate(new Date(endDateLong));
        } else if (startDateStr == null && endDateStr == null) {
            campaign.setStartDate(null);
            campaign.setEndDate(null);
        } else { // should be pair
            badRequest(resp, ApiContext.INVALID_PARAMETER);
            return null;
        }
        
        campaign = NNF.getPoiCampaignMngr().save(campaign);
        campaign.setName(NnStringUtil.revertHtml(campaign.getName()));
        
        return campaign;
    }
    
    @RequestMapping(value = "poi_campaigns/{campaignId}", method = RequestMethod.GET)
    public @ResponseBody
    PoiCampaign campaign(HttpServletRequest req,
            HttpServletResponse resp,
            @PathVariable("campaignId") String campaignIdStr) {
        
        Long campaignId = NnStringUtil.evalLong(campaignIdStr);
        if (campaignId == null) {
            notFound(resp, ApiContext.INVALID_PATH_PARAMETER);
            return null;
        }
        
        PoiCampaign canpaign = NNF.getPoiCampaignMngr().findById(campaignId);
        if (canpaign == null) {
            notFound(resp, "Campaign Not Found");
            return null;
        }
        
        NnUser user = ApiContext.getAuthenticatedUser(req);
        if (user == null) {
            unauthorized(resp);
            return null;
        } else if (user.getId() != canpaign.getUserId()) {
            forbidden(resp);
            return null;
        }
        
        canpaign.setName(NnStringUtil.revertHtml(canpaign.getName()));
        
        return canpaign;
    }
    
    @RequestMapping(value = "poi_campaigns/{poiCampaignId}", method = RequestMethod.PUT)
    public @ResponseBody
    PoiCampaign campaignUpdate(HttpServletRequest req,
            HttpServletResponse resp,
            @PathVariable("poiCampaignId") String poiCampaignIdStr) {
        
        Long campaignId = NnStringUtil.evalLong(poiCampaignIdStr);
        if (campaignId == null) {
            notFound(resp, ApiContext.INVALID_PATH_PARAMETER);
            return null;
        }
        PoiCampaign campaign = NNF.getPoiCampaignMngr().findById(campaignId);
        if (campaign == null) {
            notFound(resp, "Campaign Not Found");
            return null;
        }
        
        NnUser user = ApiContext.getAuthenticatedUser(req);
        if (user == null) {
            unauthorized(resp);
            return null;
        } else if (user.getId() != campaign.getUserId()) {
            forbidden(resp);
            return null;
        }
        
        // name
        String name = req.getParameter("name");
        if (name != null) {
            name = NnStringUtil.htmlSafeAndTruncated(name);
            campaign.setName(name);
        }
        
        // startDate
        Long startDateLong = null;
        String startDateStr = req.getParameter("startDate");
        if (startDateStr != null) {
            
            startDateLong = NnStringUtil.evalLong(startDateStr);
            if (startDateLong == null) {
                badRequest(resp, ApiContext.INVALID_PARAMETER);
                return null;
            }
        }
        
        // endDate
        Long endDateLong = null;
        String endDateStr = req.getParameter("endDate");
        if (endDateStr != null) {
            
            endDateLong = NnStringUtil.evalLong(endDateStr);
            if (endDateLong == null) {
                badRequest(resp, ApiContext.INVALID_PARAMETER);
                return null;
            }
        }
        
        if (startDateStr != null && endDateStr != null) {
            if (endDateLong < startDateLong) { 
                badRequest(resp, ApiContext.INVALID_PARAMETER);
                return null;
            }
            campaign.setStartDate(new Date(startDateLong));
            campaign.setEndDate(new Date(endDateLong));
        } else if (startDateStr == null && endDateStr == null) {
            // skip
        } else { // should be pair
            badRequest(resp, ApiContext.INVALID_PARAMETER);
            return null;
        }
        
        campaign = NNF.getPoiCampaignMngr().save(campaign);
        
        campaign.setName(NnStringUtil.revertHtml(campaign.getName()));
        
        return campaign;
    }
    
    @RequestMapping(value = "poi_campaigns/{poiCampaignId}", method = RequestMethod.DELETE)
    public @ResponseBody
    void campaignDelete(HttpServletRequest req,
            HttpServletResponse resp,
            @PathVariable("poiCampaignId") String poiCampaignIdStr) {
        
        Long poiCampaignId = NnStringUtil.evalLong(poiCampaignIdStr);
        if (poiCampaignId == null) {
            notFound(resp, ApiContext.INVALID_PATH_PARAMETER);
            return;
        }
        
        PoiCampaign campaign = NNF.getPoiCampaignMngr().findById(poiCampaignId);
        if (campaign == null) {
            notFound(resp, "Campaign Not Found");
            return;
        }
        
        NnUser user = ApiContext.getAuthenticatedUser(req);
        if (user == null) {
            unauthorized(resp);
            return;
        } else if (user.getId() != campaign.getUserId()) {
            forbidden(resp);
            return;
        }
        
        NNF.getPoiCampaignMngr().delete(campaign);
        
        msgResponse(resp, ApiContext.OK);
    }
    
    @RequestMapping(value = "poi_campaigns/{poiCampaignId}/pois", method = RequestMethod.GET)
    public @ResponseBody
    List<Poi> campaignPois(HttpServletRequest req,
            HttpServletResponse resp,
            @PathVariable("poiCampaignId") String poiCampaignIdStr) {
        
        Long poiCampaignId = NnStringUtil.evalLong(poiCampaignIdStr);
        if (poiCampaignId == null) {
            notFound(resp, ApiContext.INVALID_PATH_PARAMETER);
            return null;
        }
        
        PoiCampaign campaign = NNF.getPoiCampaignMngr().findById(poiCampaignId);
        if (campaign == null) {
            notFound(resp, "Campaign Not Found");
            return null;
        }
        
        NnUser user = ApiContext.getAuthenticatedUser(req);
        if (user == null) {
            unauthorized(resp);
            return null;
        } else if (user.getId() != campaign.getUserId()) {
            forbidden(resp);
            return null;
        }
        
        // poiPointId
        Long poiPointId = null;
        String poiPointIdStr = req.getParameter("poiPointId");
        if (poiPointIdStr != null) {
            poiPointId = NnStringUtil.evalLong(poiPointIdStr);
            if (poiPointId == null) {
                badRequest(resp, ApiContext.INVALID_PARAMETER);
                return null;
            }
        }
        
        if (poiPointId != null) {
            // find pois with point
            return NNF.getPoiMngr().findByPointId(poiPointId);
        } else {
            // find pois with campaign
            return NNF.getPoiCampaignMngr().findPoisByCampaignId(campaign.getId());
        }
    }
    
    @RequestMapping(value = "poi_campaigns/{poiCampaignId}/pois", method = RequestMethod.POST)
    public @ResponseBody
    Poi campaignPoiCreate(HttpServletRequest req,
            HttpServletResponse resp,
            @PathVariable("poiCampaignId") String campaignIdStr) {
        
        // TODO: auth check
        
        Long poiCampaignId = NnStringUtil.evalLong(campaignIdStr);
        if (poiCampaignId == null) {
            notFound(resp, ApiContext.INVALID_PATH_PARAMETER);
            return null;
        }
        
        PoiCampaign campaign = NNF.getPoiCampaignMngr().findById(poiCampaignId);
        if (campaign == null) {
            notFound(resp, "Campaign Not Found");
            return null;
        }
        
        NnUser user = ApiContext.getAuthenticatedUser(req);
        if (user == null) {
            unauthorized(resp);
            return null;
        } else if (user.getId() != campaign.getUserId()) {
            forbidden(resp);
            return null;
        }
        
        // pointId
        Long pointId = null;
        String pointIdStr = req.getParameter("pointId");
        if (pointIdStr != null) {
            pointId = NnStringUtil.evalLong(pointIdStr);
            if (pointId == null) {
                badRequest(resp, ApiContext.INVALID_PARAMETER);
                return null;
            }
        } else {
            badRequest(resp, ApiContext.MISSING_PARAMETER);
            return null;
        }
        
        PoiPoint point = NNF.getPoiPointMngr().findById(pointId);
        if (point == null) {
            badRequest(resp, ApiContext.INVALID_PARAMETER);
            return null;
        }
        
        // eventId
        Long eventId = null;
        String eventIdStr = req.getParameter("eventId");
        if (eventIdStr != null) {
            eventId = NnStringUtil.evalLong(eventIdStr);
            if (eventId == null) {
                badRequest(resp, ApiContext.INVALID_PARAMETER);
                return null;
            }
        } else {
            badRequest(resp, ApiContext.MISSING_PARAMETER);
            return null;
        }
        
        PoiEvent event = NNF.getPoiEventMngr().findById(eventId);
        if (event == null) {
            badRequest(resp, ApiContext.INVALID_PARAMETER);
            return null;
        }
        
        // create the poi
        Poi poi = new Poi();
        poi.setCampaignId(campaign.getId());
        poi.setPointId(point.getId());
        poi.setEventId(event.getId());
        
        // startDate
        String startDateStr = req.getParameter("startDate");
        if (startDateStr != null && startDateStr.length() > 0) {
            Long startDateLong = NnStringUtil.evalLong(startDateStr);
            if (startDateLong == null) {
                badRequest(resp, ApiContext.INVALID_PARAMETER);
                return null;
            }
            
            poi.setStartDate(new Date(startDateLong));
        } else {
            poi.setStartDate(null);
        }
        
        // endDate
        String endDateStr = req.getParameter("endDate");
        if (endDateStr != null && endDateStr.length() > 0) {
            Long endDateLong = NnStringUtil.evalLong(endDateStr);
            if (endDateLong == null) {
                badRequest(resp, ApiContext.INVALID_PARAMETER);
                return null;
            }
            
            poi.setEndDate(new Date(endDateLong));
        } else {
            poi.setEndDate(null);
        }
        
        // hoursOfWeek
        String hoursOfWeek = req.getParameter("hoursOfWeek");
        if (hoursOfWeek != null) {
            if (hoursOfWeek.matches("[01]{168}")) {
                // valid hoursOfWeek format
            } else {
                badRequest(resp, ApiContext.INVALID_PARAMETER);
                return null;
            }
            
            poi.setHoursOfWeek(hoursOfWeek);
        } else {
            hoursOfWeek = "";
            for (int i = 1; i <= 7; i++) { // maybe type 111... in the code, will execute faster
                hoursOfWeek = hoursOfWeek.concat("111111111111111111111111");
            }
            
            poi.setHoursOfWeek(hoursOfWeek);
        }
        
        return NNF.getPoiMngr().save(poi);
    }
    
    @RequestMapping(value = "pois/{poiId}", method = RequestMethod.GET)
    public @ResponseBody
    Poi poi(HttpServletRequest req,
            HttpServletResponse resp,
            @PathVariable("poiId") String poiIdStr) {
        
        Long poiId = NnStringUtil.evalLong(poiIdStr);
        if (poiId == null) {
            notFound(resp, ApiContext.INVALID_PATH_PARAMETER);
            return null;
        }
        
        Poi poi = NNF.getPoiCampaignMngr().findPoiById(poiId);
        if (poi == null) {
            notFound(resp, "Poi Not Found");
            return null;
        }
        
        PoiCampaign campaign = NNF.getPoiCampaignMngr().findById(poi.getCampaignId());
        if (campaign == null) {
            // ownership crashed
            // TODO: log
            internalError(resp);
            return null;
        }
        
        NnUser user = ApiContext.getAuthenticatedUser(req);
        if (user == null) {
            unauthorized(resp);
            return null;
        } else if (user.getId() != campaign.getUserId()) {
            forbidden(resp);
            return null;
        }
        
        return poi;
    }
    
    @RequestMapping(value = "pois/{poiId}", method = RequestMethod.PUT)
    public @ResponseBody
    Poi poiUpdate(HttpServletRequest req,
            HttpServletResponse resp,
            @PathVariable("poiId") String poiIdStr) {
        
        Long poiId = NnStringUtil.evalLong(poiIdStr);
        if (poiId == null) {
            notFound(resp, ApiContext.INVALID_PATH_PARAMETER);
            return null;
        }
        
        Poi poi = NNF.getPoiCampaignMngr().findPoiById(poiId);
        if (poi == null) {
            notFound(resp, "Poi Not Found");
            return null;
        }
        
        PoiCampaign campaign = NNF.getPoiCampaignMngr().findById(poi.getCampaignId());
        if (campaign == null) {
            // ownership crashed
            // TODO: log
            internalError(resp);
            return null;
        }
        
        NnUser user = ApiContext.getAuthenticatedUser(req);
        if (user == null) {
            unauthorized(resp);
            return null;
        } else if (user.getId() != campaign.getUserId()) {
            forbidden(resp);
            return null;
        }
        
        // startDate
        String startDateStr = req.getParameter("startDate");
        if (startDateStr != null && startDateStr.length() > 0) {
            Long startDateLong = NnStringUtil.evalLong(startDateStr);
            if (startDateLong == null) {
                badRequest(resp, ApiContext.INVALID_PARAMETER);
                return null;
            }
            
            poi.setStartDate(new Date(startDateLong));
        }
        
        // endDate
        String endDateStr = req.getParameter("endDate");
        if (endDateStr != null && endDateStr.length() > 0) {
            Long endDateLong = NnStringUtil.evalLong(endDateStr);
            if (endDateLong == null) {
                badRequest(resp, ApiContext.INVALID_PARAMETER);
                return null;
            }
            
            poi.setEndDate(new Date(endDateLong));
        }
        
        // hoursOfWeek
        String hoursOfWeek = req.getParameter("hoursOfWeek");
        if (hoursOfWeek != null) {
            if (hoursOfWeek.matches("[01]{168}")) {
                // valid hoursOfWeek format
                poi.setHoursOfWeek(hoursOfWeek);
            } else {
                badRequest(resp, ApiContext.INVALID_PARAMETER);
                return null;
            }
        }
        
        return NNF.getPoiMngr().save(poi);
    }
    
    @RequestMapping(value = "pois/{poiId}", method = RequestMethod.DELETE)
    public @ResponseBody
    void poiDelete(HttpServletRequest req,
            HttpServletResponse resp,
            @PathVariable("poiId") String poiIdStr) {
        
        Long poiId = NnStringUtil.evalLong(poiIdStr);
        if (poiId == null) {
            notFound(resp, ApiContext.INVALID_PATH_PARAMETER);
            return;
        }
        
        Poi poi = NNF.getPoiCampaignMngr().findPoiById(poiId);
        if (poi == null) {
            notFound(resp, "Poi Not Found");
            return;
        }
        
        PoiCampaign campaign = NNF.getPoiCampaignMngr().findById(poi.getCampaignId());
        if (campaign == null) {
            // ownership crashed
        }
        
        NnUser user = ApiContext.getAuthenticatedUser(req);
        if (user == null) {
            unauthorized(resp);
            return;
        } else if (user.getId() != campaign.getUserId()) {
            forbidden(resp);
            return;
        }
        
        NNF.getPoiMngr().delete(poi);
        
        msgResponse(resp, ApiContext.OK);
    }
    
    @RequestMapping(value = "programs/{programId}/poi_points", method = RequestMethod.GET)
    public @ResponseBody
    List<PoiPoint> programPoints(HttpServletRequest req,
            HttpServletResponse resp,
            @PathVariable("programId") String programIdStr) {
        
        Long programId = NnStringUtil.evalLong(programIdStr);
        if (programId == null) {
            notFound(resp, ApiContext.INVALID_PATH_PARAMETER);
            return null;
        }
        
        NnProgram program = NNF.getProgramMngr().findById(programId);
        if (program == null) {
            notFound(resp, "Program Not Found");
            return null;
        }
        
        List<PoiPoint> points = NNF.getPoiPointMngr().findByProgram(program.getId());
        if (points == null) {
            return new ArrayList<PoiPoint>();
        }
        
        for (PoiPoint point : points) {
            point.setName(NnStringUtil.revertHtml(point.getName()));
        }
        
        return points;
    }
    
    @RequestMapping(value = "programs/{programId}/poi_points", method = RequestMethod.POST)
    public @ResponseBody
    PoiPoint programPointCreate(HttpServletRequest req,
            HttpServletResponse resp,
            @PathVariable("programId") String programIdStr) {
        
        Long programId = NnStringUtil.evalLong(programIdStr);
        if (programId == null) {
            notFound(resp, ApiContext.INVALID_PATH_PARAMETER);
            return null;
        }
        
        NnProgram program = NNF.getProgramMngr().findById(programId);
        if (program == null) {
            notFound(resp, "Program Not Found");
            return null;
        }
        
        NnChannel channel = NNF.getChannelMngr().findById(program.getChannelId());
        if (channel == null) {
            // ownership crashed, it is orphan object
            forbidden(resp);
            return null;
        }
        NnUser user = ApiContext.getAuthenticatedUser(req);
        if (user == null) {
            
            unauthorized(resp);
            return null;
            
        } else if (!user.getIdStr().equals(channel.getUserIdStr())) {
            
            forbidden(resp);
            return null;
        }
        
        // targetId
        Long targetId = program.getId();
        
        // targetType
        Short targetType = PoiPoint.TYPE_SUBEPISODE;
        
        // name
        String name = req.getParameter("name");
        if (name == null) {
            badRequest(resp, ApiContext.MISSING_PARAMETER);
            return null;
        }
        name = NnStringUtil.htmlSafeAndTruncated(name);
        
        // startTime & endTime
        Integer startTime = null;
        Integer endTime = null;
        String startTimeStr = req.getParameter("startTime");
        String endTimeStr = req.getParameter("endTime");
        if (startTimeStr == null || endTimeStr == null) {
            badRequest(resp, ApiContext.MISSING_PARAMETER);
            return null;
        } else {
            try {
                startTime = Integer.valueOf(startTimeStr);
                endTime = Integer.valueOf(endTimeStr);
            } catch (NumberFormatException e) {
            }
            if ((startTime == null) || (startTime < 0) || (endTime == null) || (endTime <= 0) || (endTime - startTime <= 0)) {
                badRequest(resp, ApiContext.INVALID_PARAMETER);
                return null;
            }
        }
        // collision check
        PoiPoint point = new PoiPoint();
        point.setTargetId(targetId);
        point.setType(targetType);
        point.setName(name);
        point.setStartTime(startTime);
        point.setEndTime(endTime);
        
        // tag
        String tag = req.getParameter("tag");;
        if (tag != null) {
            point.setTag(TagManager.processTagText(tag));
        }
        
        // active, default : true
        Boolean active = true;
        String activeStr = req.getParameter("active");
        if (activeStr != null) {
            active = Boolean.valueOf(activeStr);
        }
        point.setActive(active);
        
        PoiPoint result = NNF.getPoiPointMngr().create(point);
        
        return result.setName(NnStringUtil.revertHtml(result.getName()));
    }
    
    @RequestMapping(value = "poi_points/{poiPointId}", method = RequestMethod.GET)
    public @ResponseBody
    PoiPoint point(HttpServletRequest req,
            HttpServletResponse resp,
            @PathVariable("poiPointId") String poiPointIdStr) {
        
        Long poiPointId = NnStringUtil.evalLong(poiPointIdStr);
        if (poiPointId == null) {
            notFound(resp, ApiContext.INVALID_PATH_PARAMETER);
            return null;
        }
        
        PoiPoint result = NNF.getPoiPointMngr().findById(poiPointId);
        if (result == null) {
            notFound(resp, "PoiPoint Not Found");
            return null;
        }
        
        return result.setName(NnStringUtil.revertHtml(result.getName()));
    }
    
    @RequestMapping(value = "poi_points/{pointId}", method = RequestMethod.PUT)
    public @ResponseBody
    PoiPoint pointUpdate(HttpServletRequest req,
            HttpServletResponse resp,
            @PathVariable("pointId") String pointIdStr) {
        
        Long pointId = NnStringUtil.evalLong(pointIdStr);
        if (pointId == null) {
            notFound(resp, ApiContext.INVALID_PATH_PARAMETER);
            return null;
        }
        
        PoiPoint point = NNF.getPoiPointMngr().findById(pointId);
        if (point == null) {
            notFound(resp, "PoiPoint Not Found");
            return null;
        }
        
        Long ownerUserId = NNF.getPoiPointMngr().findOwner(point);
        if (ownerUserId == null) { // no one can access orphan object
            forbidden(resp);
            return null;
        }
        NnUser user = ApiContext.getAuthenticatedUser(req);
        if (user == null) {
            unauthorized(resp);
            return null;
        } else if (user.getId() != ownerUserId) {
            forbidden(resp);
            return null;
        }
        
        // name
        String name = req.getParameter("name");
        if (name != null) {
            name = NnStringUtil.htmlSafeAndTruncated(name);
            point.setName(name);
        }
        
        if (point.getType() == PoiPoint.TYPE_SUBEPISODE) {
            
            // startTime
            Integer startTime = null;
            String startTimeStr = req.getParameter("startTime");
            if (startTimeStr != null) {
                try {
                    startTime = Integer.valueOf(startTimeStr);
                } catch (NumberFormatException e) {
                }
                if ((startTime == null) || (startTime < 0)) {
                    badRequest(resp, ApiContext.INVALID_PARAMETER);
                    return null;
                }
            } else {
                // origin setting
                startTime = point.getStartTimeInt();
            }
            
            // endTime
            Integer endTime = null;
            String endTimeStr = req.getParameter("endTime");
            if (endTimeStr != null) {
                try {
                    endTime = Integer.valueOf(endTimeStr);
                } catch (NumberFormatException e) {
                }
                if ((endTime == null) || (endTime <= 0)) {
                    badRequest(resp, ApiContext.INVALID_PARAMETER);
                    return null;
                }
            } else {
                // origin setting
                endTime = point.getEndTimeInt();
            }
            
            if (endTime - startTime <= 0) {
                badRequest(resp, ApiContext.INVALID_PARAMETER);
                return null;
            }
            // collision check
            point.setStartTime(startTime);
            point.setEndTime(endTime);
        }
        
        // tag
        String tagText = req.getParameter("tag");
        String tag = null;
        if (tagText != null) {
            tag = TagManager.processTagText(tagText);
            point.setTag(tag);
        }
        
        // active
        Boolean active;
        String activeStr = req.getParameter("active");
        if (activeStr != null) {
            active = Boolean.valueOf(activeStr);
            point.setActive(active);
        }
        
        PoiPoint result = NNF.getPoiPointMngr().save(point);
        return result.setName(NnStringUtil.revertHtml(result.getName()));
    }
    
    @RequestMapping(value = "poi_points/{poiPointId}", method = RequestMethod.DELETE)
    public @ResponseBody
    void pointDelete(HttpServletRequest req,
            HttpServletResponse resp,
            @PathVariable("poiPointId") String poiPointIdStr) {
        
        Long poiPointId = NnStringUtil.evalLong(poiPointIdStr);
        if (poiPointId == null) {
            notFound(resp, ApiContext.INVALID_PATH_PARAMETER);
            return;
        }
        
        PoiPoint point = NNF.getPoiPointMngr().findById(poiPointId);
        if (point == null) {
            notFound(resp, "PoiPoint Not Found");
            return;
        }
        
        Long ownerUserId = NNF.getPoiPointMngr().findOwner(point);
        if (ownerUserId == null) { // orphan object
            forbidden(resp);
            return;
        }
        NnUser user = ApiContext.getAuthenticatedUser(req);
        if (user == null) {
            unauthorized(resp);
            return;
        } else if (user.getId() != ownerUserId) {
            forbidden(resp);
            return;
        }
        
        NNF.getPoiPointMngr().delete(point);
        
        msgResponse(resp, ApiContext.OK);
    }
    
    @RequestMapping(value = "users/{userId}/poi_events", method = RequestMethod.POST)
    public @ResponseBody
    PoiEvent eventCreate(HttpServletRequest req,
            HttpServletResponse resp,
            @PathVariable("userId") String userIdStr) {
        
        Long userId = NnStringUtil.evalLong(userIdStr);
        if (userId == null) {
            notFound(resp, ApiContext.INVALID_PATH_PARAMETER);
            return null;
        }
        
        NnUser user = ApiContext.getAuthenticatedUser(req);
        if (user == null) {
            unauthorized(resp);
            return null;
        } else if (user.getId() != userId) {
            forbidden(resp);
            return null;
        }
        
        // name
        String name = req.getParameter("name");
        if (name == null) {
            badRequest(resp, ApiContext.MISSING_PARAMETER);
            return null;
        }
        name = NnStringUtil.htmlSafeAndTruncated(name);
        
        // type
        Short type = null;
        String typeStr = req.getParameter("type");
        if (typeStr != null) {
            try {
                type = Short.valueOf(typeStr);
            } catch (NumberFormatException e) {
            }
            if (type == null) {
                badRequest(resp, ApiContext.INVALID_PARAMETER);
                return null;
            }
        } else {
            badRequest(resp, ApiContext.MISSING_PARAMETER);
            return null;
        }
        
        // context
        String context = req.getParameter("context");
        if (context == null) {
            badRequest(resp, ApiContext.MISSING_PARAMETER);
            return null;
        }
        
        PoiEvent event = new PoiEvent();
        event.setUserId(user.getId());
        event.setMsoId(user.getMsoId());
        event.setName(name);
        event.setType(type);
        event.setContext(context);
        
        // notifyMsg
        if (event.getType() == PoiEvent.TYPE_INSTANTNOTIFICATION ||
             event.getType() == PoiEvent.TYPE_SCHEDULEDNOTIFICATION) {
            
            String notifyMsg = req.getParameter("notifyMsg");
            if (notifyMsg == null) {
                badRequest(resp, ApiContext.MISSING_PARAMETER);
                return null;
            }
            notifyMsg = NnStringUtil.htmlSafeAndTruncated(notifyMsg);
            event.setNotifyMsg(notifyMsg);
        }
        
        // notifyScheduler
        if (event.getType() == PoiEvent.TYPE_SCHEDULEDNOTIFICATION) {
            String notifyScheduler = req.getParameter("notifyScheduler");
            if (notifyScheduler == null) {
                badRequest(resp, ApiContext.MISSING_PARAMETER);
                return null;
            }
            String[] timestampList = notifyScheduler.split(",");
            Long timestamp = null;
            for (String timestampStr : timestampList) {
                
                timestamp = NnStringUtil.evalLong(timestampStr);
                if (timestamp == null) {
                    badRequest(resp, ApiContext.INVALID_PARAMETER);
                    return null;
                }
            }
            event.setNotifyScheduler(notifyScheduler);
        }
        
        PoiEvent result = NNF.getPoiEventMngr().save(event);
        
        result.setName(NnStringUtil.revertHtml(result.getName()));
        if (result.getType() == PoiEvent.TYPE_INSTANTNOTIFICATION ||
                result.getType() == PoiEvent.TYPE_SCHEDULEDNOTIFICATION) {
            result.setNotifyMsg(NnStringUtil.revertHtml(result.getNotifyMsg()));
        }
        
        return result;
    }
    
    @RequestMapping(value = "poi_events/{poiEventId}", method = RequestMethod.GET)
    public @ResponseBody
    PoiEvent event(HttpServletRequest req,
            HttpServletResponse resp,
            @PathVariable("poiEventId") String poiEventIdStr) {
        
        Long poiEventId = NnStringUtil.evalLong(poiEventIdStr);
        if (poiEventId == null) {
            notFound(resp, ApiContext.INVALID_PATH_PARAMETER);
            return null;
        }
        
        PoiEvent event = NNF.getPoiEventMngr().findById(poiEventId);
        if (event == null) {
            notFound(resp, "PoiEvent Not Found");
            return null;
        }
        
        NnUser user = ApiContext.getAuthenticatedUser(req);
        if (user == null) {
            unauthorized(resp);
            return null;
        } else if (user.getId() != event.getUserId()) {
            forbidden(resp);
            return null;
        }
        
        event.setName(NnStringUtil.revertHtml(event.getName()));
        if (event.getType() == PoiEvent.TYPE_INSTANTNOTIFICATION ||
                event.getType() == PoiEvent.TYPE_SCHEDULEDNOTIFICATION) {
            event.setNotifyMsg(NnStringUtil.revertHtml(event.getNotifyMsg()));
        }
        
        return event;
    }
    
    @RequestMapping(value = "poi_events/{poiEventId}", method = RequestMethod.PUT)
    public @ResponseBody
    PoiEvent eventUpdate(HttpServletRequest req,
            HttpServletResponse resp,
            @PathVariable("poiEventId") String poiEventIdStr) {
        
        Long poiEventId = NnStringUtil.evalLong(poiEventIdStr);
        if (poiEventId == null) {
            notFound(resp, ApiContext.INVALID_PATH_PARAMETER);
            return null;
        }
        
        PoiEvent event = NNF.getPoiEventMngr().findById(poiEventId);
        if (event == null) {
            notFound(resp, "PoiEvent Not Found");
            return null;
        }
        
        NnUser user = ApiContext.getAuthenticatedUser(req);
        if (user == null) {
            unauthorized(resp);
            return null;
        } else if (user.getId() != event.getUserId()) {
            forbidden(resp);
            return null;
        }
        
        // name
        String name = req.getParameter("name");
        if (name != null) {
            name = NnStringUtil.htmlSafeAndTruncated(name);
            event.setName(name);
        }
        
        Boolean shouldContainNotifyMsg = false; // TODO rewrite flag control
        Boolean shouldContainNotifyScheduler = false; // TODO rewrite flag control
        // type
        Short type = null;
        String typeStr = req.getParameter("type");
        if (typeStr != null) {
            try {
                type = Short.valueOf(typeStr);
            } catch (NumberFormatException e) {
            }
            if (type == null) {
                badRequest(resp, ApiContext.INVALID_PARAMETER);
                return null;
            }
            
            Short originType = event.getType();
            if (originType == PoiEvent.TYPE_POPUP || originType == PoiEvent.TYPE_HYPERLINK ||
                 originType == PoiEvent.TYPE_POLL) {
                if (type == PoiEvent.TYPE_INSTANTNOTIFICATION || type == PoiEvent.TYPE_SCHEDULEDNOTIFICATION) {
                    shouldContainNotifyMsg = true;
                }
            }
            if (originType == PoiEvent.TYPE_POPUP || originType == PoiEvent.TYPE_HYPERLINK ||
                    originType == PoiEvent.TYPE_POLL || originType == PoiEvent.TYPE_INSTANTNOTIFICATION) {
                   if (type == PoiEvent.TYPE_SCHEDULEDNOTIFICATION) {
                       shouldContainNotifyScheduler = true;
                   }
            }
            
            event.setType(type);
        }
        
        // context
        String context = req.getParameter("context");
        if (context != null) {
            event.setContext(context);
        }
        
        // notifyMsg
        if (event.getType() == PoiEvent.TYPE_INSTANTNOTIFICATION ||
             event.getType() == PoiEvent.TYPE_SCHEDULEDNOTIFICATION) {
            String notifyMsg = req.getParameter("notifyMsg");
            if (shouldContainNotifyMsg == true && notifyMsg == null) {
                badRequest(resp, ApiContext.MISSING_PARAMETER);
                return null;
            }
            if (notifyMsg != null) {
                notifyMsg = NnStringUtil.htmlSafeAndTruncated(notifyMsg);
                event.setNotifyMsg(notifyMsg);
            }
        }
        
        // notifyScheduler
        if (event.getType() == PoiEvent.TYPE_SCHEDULEDNOTIFICATION) {
            String notifyScheduler = req.getParameter("notifyScheduler");
            if (shouldContainNotifyScheduler == true && notifyScheduler == null) {
                badRequest(resp, ApiContext.MISSING_PARAMETER);
                return null;
            }
            if (notifyScheduler != null) {
                String[] timestampList = notifyScheduler.split(",");
                Long timestamp = null;
                for (String timestampStr : timestampList) {
                    
                    timestamp = NnStringUtil.evalLong(timestampStr);
                    if (timestamp == null) {
                        badRequest(resp, ApiContext.INVALID_PARAMETER);
                        return null;
                    }
                }
                event.setNotifyScheduler(notifyScheduler);
            }
        }
        
        PoiEvent result = NNF.getPoiEventMngr().save(event);
        
        result.setName(NnStringUtil.revertHtml(result.getName()));
        if (result.getType() == PoiEvent.TYPE_INSTANTNOTIFICATION ||
                result.getType() == PoiEvent.TYPE_SCHEDULEDNOTIFICATION) {
            result.setNotifyMsg(NnStringUtil.revertHtml(result.getNotifyMsg()));
        }
        
        return result;
    }
    
    @RequestMapping(value = "poi_events/{eventId}", method = RequestMethod.DELETE)
    public @ResponseBody
    void eventDelete(HttpServletRequest req,
            HttpServletResponse resp,
            @PathVariable("eventId") String eventIdStr) {
        
        PoiEvent event = NNF.getPoiEventMngr().findById(eventIdStr);
        if (event == null) {
            notFound(resp, "PoiEvent Not Found");
            return;
        }
        
        NnUser user = ApiContext.getAuthenticatedUser(req);
        if (user == null) {
            unauthorized(resp);
            return;
        } else if (user.getId() != event.getUserId()) {
            forbidden(resp);
            return;
        }
        
        NNF.getPoiEventMngr().delete(event);
        
        msgResponse(resp, ApiContext.OK);
    }
    
    @RequestMapping(value = "channels/{channelId}/poi_points", method = RequestMethod.GET)
    public @ResponseBody
    List<PoiPoint> channelPoints(HttpServletRequest req,
            HttpServletResponse resp,
            @PathVariable("channelId") String channelIdStr) {
        
        Long channelId = NnStringUtil.evalLong(channelIdStr);
        if (channelId == null) {
            notFound(resp, ApiContext.INVALID_PATH_PARAMETER);
            return null;
        }
        
        NnChannel channel = NNF.getChannelMngr().findById(channelId);
        if (channel == null) {
            notFound(resp, "Channel Not Found");
            return null;
        }
        
        List<PoiPoint> results = NNF.getPoiPointMngr().findByChannel(channel.getId());
        for (PoiPoint result : results) {
            
            result.setName(NnStringUtil.revertHtml(result.getName()));
        }
        
        return results;
    }
    
    @RequestMapping(value = "channels/{channelId}/poi_points", method = RequestMethod.POST)
    public @ResponseBody
    PoiPoint channelPointCreate(HttpServletRequest req,
            HttpServletResponse resp,
            @PathVariable("channelId") String channelIdStr) {
        
        Long channelId = NnStringUtil.evalLong(channelIdStr);
        if (channelId == null) {
            notFound(resp, ApiContext.INVALID_PATH_PARAMETER);
            return null;
        }
        
        NnChannel channel = NNF.getChannelMngr().findById(channelId);
        if (channel == null) {
            notFound(resp, "Channel Not Found");
            return null;
        }
        
        NnUser user = ApiContext.getAuthenticatedUser(req);
        if (user == null) {
            
            unauthorized(resp);
            return null;
            
        } else if (!user.getIdStr().equals(channel.getUserIdStr())) {
            
            forbidden(resp);
            return null;
        }
        
        // targetId
        Long targetId = channel.getId();
        
        // targetType
        Short targetType = PoiPoint.TYPE_CHANNEL;
        
        // name
        String name = req.getParameter("name");
        if (name == null) {
            badRequest(resp, ApiContext.MISSING_PARAMETER);
            return null;
        }
        name = NnStringUtil.htmlSafeAndTruncated(name);
        
        PoiPoint point = new PoiPoint();
        point.setTargetId(targetId);
        point.setType(targetType);
        point.setName(name);
        point.setStartTime(0);
        point.setEndTime(0);
        
        // tag
        String tag = req.getParameter("tag");
        if (tag != null) {
            tag = TagManager.processTagText(tag);
            point.setTag(tag);
        }
        
        // active, default : true
        Boolean active = true;
        String activeStr = req.getParameter("active");
        if (activeStr != null) {
            active = Boolean.valueOf(activeStr);
        }
        point.setActive(active);
        
        PoiPoint result = NNF.getPoiPointMngr().create(point);
        
        return result.setName(NnStringUtil.revertHtml(result.getName()));
    }
}
