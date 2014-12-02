package com.nncloudtv.web;

import java.io.IOException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.base.Joiner;
import com.nncloudtv.lib.CacheFactory;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnLogUtil;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.NnEmail;
import com.nncloudtv.model.NnProgram;
import com.nncloudtv.model.NnUser;
import com.nncloudtv.model.SysTag;
import com.nncloudtv.model.SysTagDisplay;
import com.nncloudtv.model.SysTagMap;
import com.nncloudtv.service.CntSubscribeManager;
import com.nncloudtv.service.ContentWorkerService;
import com.nncloudtv.service.EmailService;
import com.nncloudtv.service.MsoManager;
import com.nncloudtv.service.NnChannelManager;
import com.nncloudtv.service.NnProgramManager;
import com.nncloudtv.service.DepotService;
import com.nncloudtv.web.api.ApiContext;
import com.nncloudtv.web.json.cms.NnSet;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

@Controller
@RequestMapping("CMSAPI")
public class CmsApiController {
	protected static final Logger log = Logger.getLogger(CmsApiController.class.getName());
	
	class NnProgramSeqComparator implements Comparator<NnProgram> {
		public int compare(NnProgram program1, NnProgram program2) {
			int seq1 = (program1.getSeq() == null) ? 0 : Integer.valueOf(program1.getSeq());
			int seq2 = (program2.getSeq() == null) ? 0 : Integer.valueOf(program2.getSeq());
			return (seq1 - seq2);
		}
	}
	
	@ExceptionHandler(Exception.class)
	public String exception(Exception e) {
		NnLogUtil.logException(e);
		return "error/blank";
	}
	
	//////////////////// NnSet Management ////////////////////
	/**
	 * Search channel 
	 * 
	 * @param text search text
	 * @return list of channel objects
	 */
	@RequestMapping("searchChannel")
	public @ResponseBody List<NnChannel> searchChannel(@RequestParam String text) {
		log.info("search: " + text);
		if (text == null || text.length() == 0) {
			log.warning("no query string");
			return new ArrayList<NnChannel>();
		}
		return NnChannelManager.search(text, null, null, true, 0 , 0);
	}
	
	/**
	 * Retrieve sets owned by the mso 
	 * 
	 * @param msoId
	 * @return list of set objects
	 */
    
    @RequestMapping("defaultNnSetInfo")
    public @ResponseBody NnSet defaultNnSetInfo(@RequestParam Long msoId) {
        
        List<NnSet> nnsets = NNF.getSetService().findByMsoIdAndLang(msoId, null);
        if (nnsets.size() > 0)
            return nnsets.get(0);
        else
            return null;
    }
    
	/**
	 * List all channel in mso default channel set
	 * 
	 * @param msoId mso id
	 * @param isGood find success channel or not
	 * @param setId set id
	 * @return list of set objects
	 */
	@RequestMapping("defaultNnSetChannels")
	public @ResponseBody List<NnChannel> defaultNnSetChannels(
            @RequestParam(required=false) Long msoId,
            @RequestParam(required=false) Boolean isGood,
            @RequestParam(required=false) Long setId) {
        
        NnSet channelSet = null;
        if (setId != null) {
            channelSet = NNF.getSetService().findById(setId);
        } else if (msoId != null) {
            List<NnSet> nnsets = NNF.getSetService().findByMsoIdAndLang(msoId, null);
            if (nnsets.size() > 0)
                channelSet = nnsets.get(0);
        }
        if (channelSet == null)
            return new ArrayList<NnChannel>();
        List<NnChannel> cadidate = NNF.getSetService().getChannels(channelSet.getId());
        List<NnChannel> results = new ArrayList<NnChannel>();
        CntSubscribeManager cntMngr = new CntSubscribeManager();
        for (NnChannel channel : cadidate) {
            if (isGood == null || !isGood || channel.getStatus() == NnChannel.STATUS_SUCCESS) {
                channel.setCntSubscribe(cntMngr.findTotalCountByChannel(channel.getId()));
                results.add(channel);
            }
        }
        return results;
    }
	
	/**
	 * Save set information
	 * 
	 * @param req
	 * @param setId set id
	 * @param channelIds channel ids
	 * @param imageUrl image url
	 * @param name name
	 * @param intro description
	 * @param tag tag
	 * @param lang language, en or zh
	 * @param categoryId category id it belongs to
	 * @return status in text
	 */
	@RequestMapping("saveChannelSet")
	public @ResponseBody String saveNnSet(HttpServletRequest req,
	                                           @RequestParam Long setId,
	                                           @RequestParam(required = false) String channelIds,
	                                           @RequestParam(required = false) String imageUrl,
	                                           @RequestParam String name,
	                                           @RequestParam String intro,
	                                           @RequestParam String tag,
	                                           @RequestParam String lang,
	                                           @RequestParam Long categoryId) {
		
		log.info("setId = " + setId);
		log.info("channelIds = " + channelIds);
		log.info("imageUrl = " + imageUrl);
		log.info("name = " + name);
		log.info("intro = " + intro);
		log.info("tag = " + tag);
		log.info("lang = " + lang);
		log.info("categoryId = " + categoryId);
		
        SysTag sysTag = NNF.getSysTagMngr().findById(setId);
        if (sysTag == null)
            return "Invalid NnSetId";
        
        SysTagDisplay display = NNF.getDisplayMngr().findBySysTagId(sysTag.getId());
        if (display == null)
            return "iternal error";
        
        display.setName(name);
        display.setPopularTag(tag);
        display.setLang(lang);
        if (imageUrl != null) {
            display.setImageUrl(imageUrl);
            display.setImageUrl2(imageUrl);
        }
        //display.setIntro(intro);
        NNF.getDisplayMngr().save(display);
        
        if (channelIds != null) {
            
            List<SysTagMap> maps = NNF.getSysTagMapMngr().findBySysTagId(setId);
            NNF.getSysTagMapMngr().deleteAll(maps);
            maps.clear();
            
            String[] split = channelIds.split(",");
            short seq = 1;
            for (String channelIdStr : split) {
                NnChannel channel = NNF.getChannelMngr().findById(channelIdStr);
                if (channel != null) {
                    SysTagMap map = new SysTagMap(setId, channel.getId());
                    map.setSeq(seq++);
                    maps.add(map);
                }
            }
            NNF.getSysTagMapMngr().saveAll(maps);
        }
        
        return "OK";
    }
    
	//////////////////// Channel/Program Management ////////////////////
	/**
	 * Retrieve podcast information
	 *   
	 * @param url
	 * @return Map<String, String> Key includes title, description, thumbnail
	 * @throws IllegalArgumentException
	 * @throws FeedException
	 * @throws IOException
	 */
	@RequestMapping("getPodcastInfo")
	public @ResponseBody Map<String, String> getPodcastInfo(@RequestParam String url) throws IllegalArgumentException, FeedException, IOException {
		//URL feedUrl = new URL(url);
		Map<String, String> result = new HashMap<String, String>();
		SyndFeedInput input = new SyndFeedInput();
		SyndFeed feed = input.build(new XmlReader(new URL(url)));
		String title, description, thumbnail;
		title = feed.getTitle();
		description = feed.getDescription();
		thumbnail = (feed.getImage() != null) ? feed.getImage().getUrl() : null;
		if (title != null) {
			result.put("title", title);
			log.info("title = " + title);
		}
		if (description != null) {
			result.put("description", description);
			log.info("description = " + description);
		}
		if (thumbnail != null) {
			result.put("thumbnail", thumbnail);
			log.info("thumbnail = " + thumbnail);
		} else {
			//List<SyndEntry> entries = feed.getEntries();
		}
		return result;
	}

	/**
	 * List all channels owned by mso
	 * 
	 * @param msoId mso id
	 * @return list of channel objects
	 */
    @RequestMapping("listOwnedChannels")
    public @ResponseBody List<NnChannel> listOwnedChannels(HttpServletRequest req, @RequestParam Long msoId) {
        
        List<NnChannel> results = new ArrayList<NnChannel>();
        ApiContext ctx = new ApiContext(req);
        log.info("msoId = " + msoId);
        
        class NnChannelComparator implements Comparator<NnChannel> { // yes, I know, its a little dirty
            public int compare(NnChannel channel1, NnChannel channel2) {
                Date date1 = channel1.getUpdateDate();
                Date date2 = channel2.getUpdateDate();
                return date2.compareTo(date1);
            }
        }
        NnUser user = ctx.getAuthenticatedUser(msoId);
        if (user != null) {
            
            results = NNF.getChannelMngr().findByUser(user, 0, true);
            Collections.sort(results, new NnChannelComparator());
            
        }
        
        CntSubscribeManager cntMngr = new CntSubscribeManager();
        for (NnChannel channel : results) {
            channel.setCntSubscribe(cntMngr.findTotalCountByChannel(channel.getId()));
        }
        return results;
    }
    
	/**
	 * List all channel sets owned by mso. If msoId is missing, list system sets instead 
	 * @param msoId mso id
	 * @param sortby sort field
	 * @return list of set objects
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("listOwnedChannelSets")
	public @ResponseBody List<NnSet> listOwnedChannelSets(
			HttpServletResponse response,
			@RequestParam(required=false) Long msoId,
			@RequestParam(required=false) String sortby) {
		
		Long expires = Long.valueOf(24 * 60 * 60);
		response.addHeader("Cache-Control", "private, max-age=" + expires);
		response.addDateHeader("Expires", System.currentTimeMillis() + (expires * 1000));
		
		List<NnSet> results = new ArrayList<NnSet>();
		Mso nn = MsoManager.getSystemMso();
		String cacheIdString = "System.NnSets(sortby=lang)";
		
		if (msoId == null) {
			
			log.info("system channel sets");
			msoId = nn.getId();
			
			if (sortby != null) {
				if (sortby.equalsIgnoreCase("lang")) {
					// get from cache
					results = (List<NnSet>) CacheFactory.get(cacheIdString);
					if (results != null) {
						log.info("get from cache");
						return results;
					}
				} else if (sortby.equalsIgnoreCase("reset")) {
					// hack
					log.info("remove from cache");
					CacheFactory.delete(cacheIdString);
				}
			}
		}
		log.info("msoId = " + msoId);
		results = NNF.getSetService().findByMsoIdAndLang(msoId, null);
		class NnSetComparator implements Comparator<NnSet> {  // yes, I know, its a little dirty
			public int compare(NnSet set1, NnSet set2) {
				String lang1 = set1.getLang();
				String lang2 = set2.getLang();
				if (lang1.equalsIgnoreCase(lang2))
					return 0;
				if (lang1.equalsIgnoreCase("en"))
					return -1;
				else
					return 1;
			}
		}
		if (sortby != null && sortby.equalsIgnoreCase("lang")) {
			Collections.sort(results, new NnSetComparator());
			if (msoId == nn.getId()) {
				// put to cache
				log.info("put to cache");
				CacheFactory.set(cacheIdString, new ArrayList<NnSet>(results));
			}
		}
		return results;
	}

	/**
	 * Change program public attribute 
	 * 
	 * @param programId program id
	 * @return true or false of a program's publicity
	 */
	@RequestMapping("switchProgramPublicity")
	public @ResponseBody Boolean switchProgramPublicity(@RequestParam Long programId) {
		NnProgramManager programMngr = new NnProgramManager();
		NnProgram program = programMngr.findById(programId);
		if (program.isPublic())
			program.setPublic(false);
		else
			program.setPublic(true);
		programMngr.save(program);
		return program.isPublic();
	}
	
	/**
	 * Change channel public attribute 
	 * 
	 * @param channelId channel id
	 * @return true or false of a program's publicity
	 */	
	@RequestMapping("switchChannelPublicity")
	public @ResponseBody Boolean switchChannelPublicity(@RequestParam Long channelId) {
		NnChannelManager channelMngr = new NnChannelManager();
		NnChannel channel = channelMngr.findById(channelId);
		if (channel.isPublic())
			channel.setPublic(false);
		else
			channel.setPublic(true);
		channelMngr.save(channel);
		return channel.isPublic();
	}
	
	/**
	 * Remove a program
	 * 
	 * @param programId program id
	 */
	@RequestMapping("removeProgram")
	public @ResponseBody void removeProgram(@RequestParam Long programId) {
		log.info("programId = " + programId);
		NnProgramManager programMngr = new NnProgramManager();
		NnProgram program = programMngr.findById(programId);
		if (program != null) {
			long channelId = program.getChannelId();
			programMngr.delete(program);
			log.info("program deleted, reorder program sequence");
			updateAllProgramsSeq(channelId);
		}
	}
	
	/**
	 * Remove channel's ownership of a mso, but channel itself is kept 
	 * 
	 * @param channelId channel id
	 * @param msoId mso id
	 */
    @RequestMapping("removeChannelFromList")
    public @ResponseBody void removeChannelFromList(@RequestParam Long channelId, @RequestParam Long msoId) {
        
        log.info("msoId = " + msoId + ", channelId = " + channelId);
        
        NnChannel channel = NNF.getChannelMngr().findById(channelId);
        if (channel != null) {
            
            channel.setUserIdStr(null); // unlink
            channel.setStatus(NnChannel.STATUS_REMOVED);
            channel.setPublic(false);
            NNF.getChannelMngr().save(channel);
        }
    }
	
	/**
	 * Retrieve program info
	 * 
	 * @param programId program id
	 * @return program object
	 */
    @RequestMapping("programInfo")
    public @ResponseBody NnProgram programInfo(@RequestParam Long programId) {
        
        NnProgram program = NNF.getProgramMngr().findById(programId);
        if (program != null) {
            
            program.setName(NnStringUtil.revertHtml(program.getName()));
            program.setIntro(NnStringUtil.revertHtml(program.getIntro()));
            program.setComment(NnStringUtil.revertHtml(program.getComment()));
        }
        return program;
    }
	
	/**
	 * Retrieve channel info
	 * 
	 * @param channelId channel id
	 * @return channel object
	 */
    @RequestMapping("channelInfo")
    public @ResponseBody NnChannel channelInfo(@RequestParam Long channelId) {
        
        return NNF.getChannelMngr().findById(channelId);
    }
	
	/**
	 * Create a channel by url
	 * 
	 * @param req
	 * @param sourceUrl
	 * @return channel object
	 */
	@RequestMapping("importChannelByUrl")
	public @ResponseBody NnChannel importChannelByUrl(HttpServletRequest req, @RequestParam String sourceUrl) {
		
		NnChannelManager channelMngr = NNF.getChannelMngr();
		sourceUrl = sourceUrl.trim();
		log.info("import " + sourceUrl);
		sourceUrl = channelMngr.verifyUrl(sourceUrl);
		if (sourceUrl == null) {
			log.warning("invalid source url");
			return null;
		}
		log.info("normalized " + sourceUrl);
		NnChannel channel = channelMngr.findBySourceUrl(sourceUrl);
		if (channel == null) {
			log.info("new source url");
			channel = channelMngr.create(sourceUrl, null, null, req);
			if (channel == null) {
				log.warning("invalid source url");
				return null;
			}
			channel = channelMngr.save(channel);
			if (channel != null && channel.getContentType() != NnChannel.CONTENTTYPE_FACEBOOK) { //!!!
				DepotService tranService = new DepotService();
				tranService.submitToTranscodingService(channel.getId(), sourceUrl, req);
			}
			channel.setTag("NEW_CHANNEL");
		}
		
		return channel;
	}
	
	/**
	 * Create a 9x9 channel
	 * 
	 * @param sourceUrl source url
	 * @param imageUrl image url
	 * @param name name
	 * @param intro description
	 * @param tag tag
	 * @param lang language
	 * @param msoId mso id
	 * @return status in text
	 * @throws NoSuchAlgorithmException
	 */
	@RequestMapping("addChannelByUrl")
	public @ResponseBody String addChannelByUrl(HttpServletRequest req,
	                                            @RequestParam String sourceUrl,
	                                            @RequestParam(required = false) String imageUrl,
	                                            @RequestParam String name,
	                                            @RequestParam String intro,
	                                            @RequestParam String tag,
	                                            @RequestParam String lang,
	                                            @RequestParam Long msoId) throws NoSuchAlgorithmException {
		
		log.info("sourceUrl = " + sourceUrl);
		log.info("imageUrl = " + imageUrl);
		log.info("name = " + name);
		log.info("intro = " + intro);
		log.info("tag = " + tag);
		log.info("lang = " + lang);
		log.info("msoId = " + msoId);
		
		NnChannelManager channelMngr = new NnChannelManager();
		NnChannel channel = channelMngr.findBySourceUrl(sourceUrl);
		MsoManager msoMngr = new MsoManager();
		
		Mso mso = msoMngr.findById(msoId);
		
		if (channel == null)
			return "Invalid Source Url";
		if (mso == null)
			return "Invalid msoId";
		
		channel.setName(name);
		channel.setTag(tag);
		if (imageUrl != null) {
			ContentWorkerService workerService = new ContentWorkerService();
			Long timestamp = System.currentTimeMillis() / 1000L;
			
			MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
			String sudoChannelSource = "http://9x9.tv/channel/" + channel.getId();
			sha1.update(sudoChannelSource.getBytes());
			String prefix = NnStringUtil.bytesToHex(sha1.digest()) + "_" + timestamp + "_";
			
			log.info("prefix = " + prefix);
			
			channel.setImageUrl(imageUrl);
			workerService.channelLogoProcess(channel.getId(), imageUrl, prefix, req);
		}
		channel.setIntro(intro);
		// channel.setStatus(NnChannel.STATUS_SUCCESS); // default import status
		channelMngr.save(channel);
		
		// submit to transcoding server again
		if (channel != null && channel.getContentType() != NnChannel.CONTENTTYPE_FACEBOOK) { //!!!
			DepotService tranService = new DepotService();
			tranService.submitToTranscodingService(channel.getId(), sourceUrl, req);
		}
		
		return "OK";
	}

	/**
	 * Create program
	 * 
	 * @param programId program id
	 * @param channelId channel id
	 * @param sourceUrl source url
	 * @param imageUrl image url
	 * @param name name
	 * @param comment curator comment
	 * @param intro description
	 * @return status in text
	 * @throws NoSuchAlgorithmException
	 */
	@RequestMapping("saveNewProgram")
	public @ResponseBody String saveNewProgram(HttpServletRequest req,
	                                           @RequestParam Long programId,
	                                           @RequestParam Long channelId,
	                                           @RequestParam String sourceUrl,
	                                           @RequestParam(required = false) String imageUrl,
	                                           @RequestParam(required = false) String name,
	                                           @RequestParam(required = false) String comment,
	                                           @RequestParam(required = false) String intro) throws NoSuchAlgorithmException {
		
		log.info("programId = " + programId);
		log.info("channelId = " + channelId);
		log.info("sourceUrl = " + sourceUrl);
		log.info("imageUrl = " + imageUrl);
		log.info("name = " + name);
		log.info("intro = " + intro);
		log.info("comment = " + comment);
		
		NnProgramManager programMngr = new NnProgramManager();
		NnChannelManager channelMngr = new NnChannelManager();
		ContentWorkerService workerService = new ContentWorkerService();
		
		NnProgram program = programMngr.findById(programId);
		if (program == null) {
			return "Invalid programId";
		}
		NnChannel channel = channelMngr.findById(channelId);
		if (channel == null) {
			return "Invalid channelId";
		}		
		Long timestamp = System.currentTimeMillis() / 1000L;
		MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
		sha1.update(sourceUrl.getBytes());
		String prefix = NnStringUtil.bytesToHex(sha1.digest()) + "_" + timestamp + "_";
		log.info("prefix = " + prefix);
		
		if (program.getContentType() == NnProgram.CONTENTTYPE_RADIO) {
			program.setAudioFileUrl(sourceUrl);
		} else {
			program.setFileUrl(sourceUrl);
		}
		if (sourceUrl.indexOf("youtube.com") == -1) {
			// TODO: check source url is valid
			boolean autoGeneratedLogo = (imageUrl == null) ? true : false;
			workerService.programVideoProcess(programId, sourceUrl, prefix, autoGeneratedLogo, req);
			program.setContentType(NnProgram.CONTENTTYPE_DIRECTLINK);
			log.info("direct link");
		} else {
			// TODO: check if youtube url is valid
			program.setContentType(NnProgram.CONTENTTYPE_YOUTUBE);
			log.info("youtube link");
		}
		if (imageUrl != null) {
			program.setImageUrl(imageUrl);
			program.setImageLargeUrl(imageUrl);
			workerService.programLogoProcess(program.getId(), imageUrl, prefix, req);
		}
		if (name != null) {
			program.setName(NnStringUtil.htmlSafeAndTruncated(name));
		}
		if (intro != null) {
			program.setIntro(NnStringUtil.htmlSafeAndTruncated(intro));
		}
		if (comment != null) {
			program.setComment(NnStringUtil.htmlSafeAndTruncated(comment));
		}
		program.setPublic(true);
		programMngr.create(channel, program);
		
		updateAllProgramsSeq(channelId);
		
		return "OK";
	}
	
	/**
	 * Edit program
	 * 
	 * @param programId program id
	 * @param imageUrl image url
	 * @param name name 
	 * @param intro description
	 * @param comment curator comment
	 * @return status intext
	 * @throws NoSuchAlgorithmException
	 */
	@RequestMapping("saveProgram")
	public @ResponseBody String saveProgram(HttpServletRequest req,
	                                        @RequestParam Long programId,
	                                        @RequestParam(required = false) String imageUrl,
	                                        @RequestParam(required = false) String name,
	                                        @RequestParam(required = false) String intro,
	                                        @RequestParam(required = false) String comment) throws NoSuchAlgorithmException {
		log.info("programId = " + programId);
		log.info("imageUrl = " + imageUrl);
		log.info("name = " + name);
		log.info("intro = " + intro);
		log.info("comment = " + comment);
		
		NnProgramManager programMngr = new NnProgramManager();
		NnProgram program = programMngr.findById(programId);
		if (program == null) {
			return "Invalid programId";
		}
		if (name != null)
			program.setName(NnStringUtil.htmlSafeAndTruncated(name));
		if (intro != null)
			program.setIntro(NnStringUtil.htmlSafeAndTruncated(intro));
		if (comment != null)
			program.setComment(NnStringUtil.htmlSafeAndTruncated(comment));
		if (imageUrl != null) {
			ContentWorkerService workerService = new ContentWorkerService();
			Long timestamp = System.currentTimeMillis() / 1000L;
			
			String sourceUrl;
			if (program.getFileUrl() != null)
				sourceUrl = program.getFileUrl();
			else if (program.getAudioFileUrl() != null)
				sourceUrl = program.getAudioFileUrl();
			else
				sourceUrl = "http://9x9.tv/episode/" + program.getId();
			
			MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
			sha1.update(sourceUrl.getBytes());
			String prefix = NnStringUtil.bytesToHex(sha1.digest()) + "_" + timestamp + "_";
			
			log.info("prefix = " + prefix);
			
			program.setImageUrl(imageUrl);
			program.setImageLargeUrl(imageUrl);
			workerService.programLogoProcess(program.getId(), imageUrl, prefix, req);
		}
		programMngr.save(program);
		
		return "OK";
	}

	/**
	 * Channel edit
	 * 
	 * @param channelId channel id
	 * @param msoId mso id
	 * @param imageUrl image url
	 * @param name name
	 * @param intro description
	 * @param tag tag
	 * @param lang language
	 * @return status in text
	 * @throws NoSuchAlgorithmException
	 */
	@RequestMapping("saveChannel")
	public @ResponseBody String saveChannel(HttpServletRequest req,
	                                        @RequestParam Long channelId,
	                                        @RequestParam(required = false) Long   msoId,
	                                        @RequestParam(required = false) String imageUrl,
	                                        @RequestParam(required = false) String name,
	                                        @RequestParam(required = false) String intro,
	                                        @RequestParam(required = false) String tag,
	                                        @RequestParam(required = false) String lang) throws NoSuchAlgorithmException {
		
		log.info("channelId = " + channelId);
		log.info("imageUrl = " + imageUrl);
		log.info("name = " + name);
		log.info("intro = " + intro);
		log.info("tag = " + tag);
		log.info("lang = " + lang);
		log.info("msoId = " + msoId);
		
		NnChannel channel = NNF.getChannelMngr().findById(channelId);
		
		if (channel == null)
			return "Invalid ChannelId";
		
		if (tag != null)
			channel.setTag(tag);
		if (imageUrl != null) {
			ContentWorkerService workerService = new ContentWorkerService();
			Long timestamp = System.currentTimeMillis() / 1000L;
			
			MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
			String sudoChannelSource = "http://9x9.tv/channel/" + channel.getId();
			sha1.update(sudoChannelSource.getBytes());
			String prefix = NnStringUtil.bytesToHex(sha1.digest()) + "_" + timestamp + "_";
			
			log.info("prefix = " + prefix);
			
			channel.setImageUrl(imageUrl);
			workerService.channelLogoProcess(channelId, imageUrl, prefix, req);
		}
		
		if (name != null)
			channel.setName(name);
		if (intro != null)
			channel.setIntro(intro);
		if (lang != null) {
			channel.setLang(lang);
		}
		if (msoId != null) { // first time the channel be saved
			channel.setPublic(true);
		}
		channel.setUpdateDate(new Date());
		// channelMngr.calibrateProgramCnt(channel);
		NNF.getChannelMngr().save(channel);
		
		return "OK";
	}
	
	// this is a wrapper of updateProgramListSeq
	private void updateAllProgramsSeq(long channelId) {
		
		// update all episode sequence
		List<NnProgram> programList = NNF.getProgramMngr().findByChannelId(channelId);
		Collections.sort(programList, new NnProgramSeqComparator());
		List<Long> programIdList = new ArrayList<Long>();
		for (NnProgram program : programList) {
			programIdList.add(program.getId());
		}
		String programListStr = Joiner.on(",").join(programIdList);
		updateProgramListSeq(channelId, programListStr);
		
	}

	/**
	 * Adjust program list
	 * 
	 * @param channelId channel id
	 * @param programIdList program list, separed with comma
	 * @return status in text
	 */
	@RequestMapping("updateProgramListSeq")
	public @ResponseBody String updateProgramListSeq(@RequestParam Long channelId, @RequestParam String programIdList) {
		log.info("channelId: " + channelId);
		log.info("programIdList" + programIdList);
		
		List<Long> programIds = new ArrayList<Long>();
		String[] splitted = programIdList.split(",");
		for (int i = 0; i < splitted.length; i++) {
			programIds.add(Long.valueOf(splitted[i]));
		}
		
		List<Long> origProgramIds = new ArrayList<Long>();
		List<NnProgram> origProgramList = NNF.getProgramMngr().findByChannelId(channelId);
		if (origProgramList.size() != programIds.size()) {
			return "SIZE_NOT_MATCH";
		}
		for (int i = 0; i < origProgramList.size(); i++) {
			origProgramIds.add(origProgramList.get(i).getId());
		}
		if (!programIds.containsAll(origProgramIds)) {
			return "NOT_MATCH";
		}
		
		for (NnProgram program : origProgramList) {
			int seq = programIds.indexOf(program.getId());
			program.setSeq(String.format("%08d", seq + 1));
		}
		NNF.getProgramMngr().saveAll(origProgramList);
		
		// clean cache (though, programMngr.save() do it once before)
		String cacheKey = "nnprogram(" + channelId + ")";
		log.info("remove cached programInfo data");
		CacheFactory.delete(cacheKey);
		
		return "OK";
	}
	/**
	 * Which system set contains this channel
	 * 
	 * @param channelId channel id
	 * @return set object
	 */ 
    @RequestMapping("channelSystemNnSet")
    public @ResponseBody NnSet channelSystemNnSet(@RequestParam Long channelId) {
        
        return null;
    }
	
	/**
	 * Create program with default values
	 * 
	 * @param contentType content type
	 * @return program id
	 */
	@RequestMapping("createProgramSkeleton")
	public @ResponseBody Long createProgramSkeleton(@RequestParam(required=false) Short contentType) {
		NnProgramManager programMngr = new NnProgramManager();
		
		log.info("create program skeleton");
		log.info("contentType: " + contentType);
		
		NnProgram program;		
		if (contentType != null && contentType == NnProgram.CONTENTTYPE_RADIO) {			
			program = new NnProgram(0, "New Program", "New Program", NnChannel.IMAGE_RADIO_URL);
			program.setPublic(false);
			program.setContentType(NnProgram.CONTENTTYPE_RADIO);
			programMngr.save(program);
			
		} else {			
			program = new NnProgram(0, "New Program", "New Program", NnChannel.IMAGE_WATERMARK_URL);
			program.setPublic(false);
			programMngr.save(program);
			
		}
		
		return program.getId();
	}

	/**
	 * Create channel with default values 
	 * @return channel id
	 */
	@RequestMapping("createChannelSkeleton")
	public @ResponseBody Long createChannelSkeleton() {
		
		NnChannel channel = new NnChannel("New Channel", "New Channel", NnChannel.IMAGE_WATERMARK_URL);
		channel.setPublic(false);
		channel.setStatus(NnChannel.STATUS_WAIT_FOR_APPROVAL);
		channel.setContentType(NnChannel.CONTENTTYPE_MIXED); // a channel type in podcast does not allow user to add program in it, so change to mixed type
		NNF.getChannelMngr().save(channel);
		
		return channel.getId();
	}
	
	/**
	 * Program listing
	 * 
	 * @param channelId
	 * @return list of program objects
	 */
	@RequestMapping("programList")
	public @ResponseBody List<NnProgram> programList(Long channelId) {
		
		List<NnProgram> results = NNF.getProgramMngr().findByChannelId(channelId);
		Collections.sort(results, new NnProgramSeqComparator());
		
		return results;
	}
    
    ////////////////////Promotion Tools ////////////////////
    
    //////////////////// statistics ////////////////////
    
    //////////////////// others ////////////////////		
	/**
	 * Change password
	 * 
	 * @param msoId mso id
	 * @param newPassword new password
	 * @return status in text
	 */
	@RequestMapping("changePassword")
	public @ResponseBody String changePassword(@RequestParam Long msoId, @RequestParam String newPassword, HttpServletRequest req) {
		
		return "OK";
	}
	
	/**
	 * Send email
	 * 
	 * @param from from email address
	 * @param to to email address
	 * @param subject email subject
	 * @param msgBody email body
	 * @return status in text
	 */
	@RequestMapping(value="sendEmail", params = {"from", "to", "subject", "msgBody"})
	public @ResponseBody String sendEmail(
					@RequestParam(value = "from") String from,
					@RequestParam(value = "to") String to,
					@RequestParam(value = "subject") String subject,
					@RequestParam(value = "msgBody") String msgBody) {
		
		log.info("sender: " + from);
		log.info("subject:" + subject);
		log.info("content:" + msgBody);
		
		EmailService emailService = new EmailService();
		msgBody = "from: "+from+" , "+msgBody;
		NnEmail email = new NnEmail("flipr@9x9cloud.tv", "flipr", from, null, from, subject, msgBody);
		emailService.sendEmail(email, null, null);		
		return "OK";
	}
	
}

