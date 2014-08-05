package com.nncloudtv.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nncloudtv.dao.NnEpisodeDao;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.lib.QueueFactory;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.NnChannelPref;
import com.nncloudtv.model.NnEpisode;
import com.nncloudtv.model.NnProgram;
import com.nncloudtv.model.NnUser;
import com.nncloudtv.model.TitleCard;
import com.nncloudtv.web.json.facebook.FBPost;

@Service
public class NnEpisodeManager {
    
    protected static final Logger log = Logger.getLogger(NnEpisodeManager.class.getName());
    
    private NnEpisodeDao dao = NNF.getEpisodeDao();
    
    public NnEpisode findById(long id) {
        return dao.findById(id);
    }
    
    public NnEpisode save(NnEpisode episode) {
        
        Date now = new Date();
        
        episode.setUpdateDate(now);
        
        NNF.getProgramMngr().resetCache(episode.getChannelId());
        
        return dao.save(episode);
        
    }
    
    public List<NnEpisode> save(List<NnEpisode> episodes) {
        
        Date now = new Date();
        List<Long> channelIds = new ArrayList<Long>();
        
        for (NnEpisode episode : episodes) {
            episode.setUpdateDate(now);
            
            if (channelIds.indexOf(episode.getChannelId()) < 0) {
                channelIds.add(episode.getChannelId());
            }
        }
        
        log.info("channel count = " + channelIds.size());
        for (Long channelId : channelIds) {
            NNF.getProgramMngr().resetCache(channelId);
        }
        
        return dao.saveAll(episodes);
    }
    
    public NnEpisode save(NnEpisode episode, boolean rerun) {
    
        // rerun - to make episode on top again and public
        if (rerun) {
            
            log.info("rerun!");
            
            episode.setPublishDate(new Date());
            episode.setPublic(true);
            episode.setSeq(0);
            save(episode);
            
            reorderChannelEpisodes(episode.getChannelId());
            
            return episode;
        }
        
        log.info("publishDate = " + episode.getPublishDate());
        
        return save(episode);
    }
    
    public List<NnEpisode> findByChannelId(long channelId) {
    
        return dao.findByChannelId(channelId);
    }
    
    public int getProgramCnt(NnEpisode episode) {
        
        if (episode == null) {
            return 0;
        }
        
        List<NnProgram> programs = NNF.getProgramMngr().findByEpisodeId(episode.getId());
        
        return programs.size();
    }
    
    public static Comparator<NnEpisode> getComparator(String sort) {
        
        if (sort.equals("publishDate")) {
            
            return new Comparator<NnEpisode>() {
                
                public int compare(NnEpisode episode1, NnEpisode episode2) {
                    
                    Date publishDate1 = episode1.getPublishDate();
                    Date publishDate2 = episode2.getPublishDate();
                    
                    if (publishDate1 == null && publishDate2 == null) {
                        
                        return 0;
                        
                    } else if (publishDate1 == null) {
                        
                        return 1;
                        
                    } else if (publishDate2 == null) {
                        
                        return -1;
                        
                    } else {
                        
                        return publishDate2.compareTo(publishDate1);
                    }
                }
            };
        } else if (sort.equals("isPublicFirst")) {
            
            return new Comparator<NnEpisode>() {
                
                public int compare(NnEpisode episode1, NnEpisode episode2) {
                    
                    if (episode1.isPublic() == episode2.isPublic()) {
                        
                        return (episode1.getSeq() - episode2.getSeq());
                        
                    } else if (episode1.isPublic() == false) {
                        
                        return -1;
                                
                    }
                    
                    return 1;
                }
            };
        } else if (sort.equals("reverse")) {
            
            return new Comparator<NnEpisode>() {
                
                public int compare(NnEpisode episode1, NnEpisode episode2) {
                    
                    return (episode2.getSeq() - episode1.getSeq());
                }
            };
        } else {
            
            return new Comparator<NnEpisode>() {
                
                public int compare(NnEpisode episode1, NnEpisode episode2) {
                    
                    return (episode1.getSeq() - episode2.getSeq());
                }
            };
        }
    }
    
    public void reorderChannelEpisodes(long channelId) {
        
        List<NnEpisode> episodes = findByChannelId(channelId);
        Collections.sort(episodes, getComparator("seq"));
        
        for (int i = 0; i < episodes.size(); i++) {
            
            episodes.get(i).setSeq(i + 1);
        }
        
        save(episodes);
    }
    
    public void delete(NnEpisode episode) {
    
        NNF.getProgramMngr().resetCache(episode.getChannelId());
        
        // delete programs
        List<NnProgram> programs = NNF.getProgramMngr().findByEpisodeId(episode.getId());
        NNF.getProgramMngr().delete(programs);
        
        // TODO delete poiPoints at episode level
        
        dao.delete(episode);
    }
    
    public List<NnEpisode> list(long page, long rows, String sidx, String sord,
            String filter) {
    
        return dao.list(page, rows, sidx, sord, filter);
    }

    public List<NnEpisode> findPlayerEpisodes(long channelId, short sort, int start, int end) {
        return dao.findPlayerEpisode(channelId, sort, start, end);
    }

    public List<NnEpisode> findPlayerLatestEpisodes(long channelId, short sort) {
        
        return dao.findPlayerLatestEpisode(channelId, sort);
    }
    
    public int calculateEpisodeDuration(NnEpisode episode) {
    
        TitleCardManager titleCardMngr = new TitleCardManager();
        List<NnProgram> programs = NNF.getProgramMngr().findByEpisodeId(episode.getId());
        List<TitleCard> titleCards = titleCardMngr.findByEpisodeId(episode.getId());
        
        int totalDuration = 0;
        
        for (NnProgram program : programs) {
            totalDuration += program.getDurationInt();
        }
        
        for (TitleCard titleCard : titleCards) {
            totalDuration += titleCard.getDurationInt();
        }
        
        return totalDuration;
    }
    
    // hook, auto share to facebook
    public void autoShareToFacebook(NnEpisode episode) {
        
        FBPost fbPost = new FBPost(NnStringUtil.revertHtml(episode.getName()), NnStringUtil.revertHtml(episode.getIntro()), episode.getImageUrl());
        String url = NnStringUtil.getSharingUrl(false, null, episode.getChannelId(), episode.getId());
        fbPost.setLink(url);
        log.info("share link: " + url);
        
        NnChannel channel = NNF.getChannelMngr().findById(episode.getChannelId());
        if (channel == null) {
            return ;
        }
        
        NnUser user = NNF.getUserMngr().findById(channel.getUserId(), 1);
        if (user == null) {
            return ;
        }
        
        NnChannelPrefManager prefMngr = NNF.getChPrefMngr();
        List<NnChannelPref> prefList = prefMngr.findByChannelIdAndItem(episode.getChannelId(), NnChannelPref.FB_AUTOSHARE);
        String facebookId, accessToken;
        String[] parsedObj;
        
        fbPost.setCaption(" ");
        
        for (NnChannelPref pref : prefList) {
            parsedObj = prefMngr.parseFacebookAutoshare(pref.getValue());
            if (parsedObj == null) {
                continue;
            }
            facebookId = parsedObj[0];
            accessToken = parsedObj[1];
            fbPost.setFacebookId(facebookId);
            fbPost.setAccessToken(accessToken);
            
            QueueFactory.add("/fb/postToFacebook", fbPost);
        }
        log.info(fbPost.toString());
    }
    
    /** adapt NnEpisode to format that CMS API required */
    public void normalize(NnEpisode episode) {
        if (episode != null) {
            episode.setName(NnStringUtil.revertHtml(episode.getName()));
            episode.setIntro(NnStringUtil.revertHtml(episode.getIntro()));
        }
    }
    
    /** adapt NnEpisode to format that CMS API required */
    public void normalize(List<NnEpisode> episodes) {
        if (episodes != null) {
            for (NnEpisode episode : episodes) {
                normalize(episode);
            }
        }
    }
    
    public int total() {
        return dao.total();
    }
    
    public int total(String filter) {
        return dao.total(filter);
    }
    
    public List<NnEpisode> findByChannels(List<NnChannel> channels) {
        
        List<NnEpisode> episodes = new ArrayList<NnEpisode>();
        
        for (NnChannel channel : channels) {
            
            episodes.addAll(findByChannelId(channel.getId()));
        }
        
        return episodes;
    }
}
