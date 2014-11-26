package com.nncloudtv.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nncloudtv.dao.NnEpisodeDao;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnDateUtil;
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
    
    public NnEpisode findById(String episodeIdStr) {
        
        return dao.findById(episodeIdStr);
    }
    
    public NnEpisode findById(long id) {
        
        return dao.findById(id);
    }
    
    public NnEpisode save(NnEpisode episode) {
        
        Date now = NnDateUtil.now();
        
        log.info("isPublic = " + episode.isPublic() + ", publishDate = " + episode.getPublishDate());
        if (episode.isPublic() == false && episode.getPublishDate() != null) {
            
            log.warning("to force pubishDate be null when is not published, just in case");
            episode.setPublishDate(null);
            
        } else if (episode.isPublic() && episode.getPublishDate() == null) {
            
            log.warning("to force pubishDate be not null when is published, just in case");
            episode.setPublishDate(now);
        }
        
        if (episode.getCreateDate() == null)
            episode.setCreateDate(now);
        episode.setUpdateDate(now);
        
        NNF.getProgramMngr().resetCache(episode.getChannelId());
        
        return dao.save(episode);
        
    }
    
    public Collection<NnEpisode> saveAll(Collection<NnEpisode> episodes) {
        
        Date now = NnDateUtil.now();
        List<Long> channelIds = new ArrayList<Long>();
        
        for (NnEpisode episode : episodes) {
            episode.setUpdateDate(now);
            if (channelIds.contains(episode.getChannelId()) == false)
                channelIds.add(episode.getChannelId());
        }
        
        log.info("uniq channel count = " + channelIds.size());
        for (Long channelId : channelIds)
            NNF.getProgramMngr().resetCache(channelId);
        
        return dao.saveAll(episodes);
    }
    
    public List<NnEpisode> findByChannelId(long channelId) {
    
        return dao.findByChannelId(channelId);
    }
    
    public static Comparator<NnEpisode> getComparator(String comparator) {
        
        if (comparator == null)
            comparator = "default";
        
        if (comparator.equals("timedLinear")) {
            
            return new Comparator<NnEpisode>() {
                
                public int compare(NnEpisode ep1, NnEpisode ep2) {
                    
                    if (ep1.isPublic() == false && ep2.isPublic() == true) {
                        return -1;
                    } else if (ep1.isPublic() == true && ep2.isPublic() == false) {
                        return 1;
                    } else if (ep1.isPublic() == true && ep2.isPublic() == true) {
                        
                        Date pubDate1 = ep1.getPublishDate();
                        Date pubDate2 = ep2.getPublishDate();
                        if (pubDate1 == null && pubDate2 == null) {
                            return 0;
                        } else if (pubDate1 == null) {
                            return -1;
                        } else if (pubDate2 == null) {
                            return 1;
                        } else {
                            return pubDate2.compareTo(pubDate1);
                        }
                    } else {
                        
                        Date schedule1 = ep1.getScheduleDate();
                        Date schedule2 = ep2.getScheduleDate();
                        if (schedule1 == null && schedule2 == null) {
                            return ep1.getSeq() - ep2.getSeq();
                        } else if (schedule1 == null) {
                            return 1;
                        } else if (schedule2 == null) {
                            return -1;
                        } else {
                            return schedule2.compareTo(schedule1);
                        }
                    }
                }
            };
        } else if (comparator.equals("publishDate")) {
            
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
        } else if (comparator.equals("isPublicFirst")) {
            
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
        } else if (comparator.equals("reverse")) {
            
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
        
        dao.saveAll(episodes);
    }
    
    public void delete(NnEpisode episode) {
    
        if (episode == null) return;
        
        NNF.getProgramMngr().resetCache(episode.getChannelId());
        
        // delete programs
        NnProgramManager programMngr = NNF.getProgramMngr();
        programMngr.deleteAll(programMngr.findByEpisodeId(episode.getId()));
        
        dao.delete(episode);
    }
    
    public List<NnEpisode> listV2(long page, long rows, String sort, String filter) {
    
        return dao.listV2(page, rows, sort, filter);
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
    
    // TODO: to be removed
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
        NnChannelPref pref = prefMngr.findByChannelIdAndItem(episode.getChannelId(), NnChannelPref.FB_AUTOSHARE);
        String facebookId, accessToken;
        String[] parsedObj;
        
        fbPost.setCaption(" ");
        if (pref == null) {
            return;
        }
        parsedObj = prefMngr.parseFacebookAutoshare(pref.getValue());
        if (parsedObj == null) {
            return;
        }
        facebookId = parsedObj[0];
        accessToken = parsedObj[1];
        fbPost.setFacebookId(facebookId);
        fbPost.setAccessToken(accessToken);
        
        QueueFactory.add("/fb/postToFacebook", fbPost);
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
