package com.nncloudtv.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nncloudtv.dao.TitleCardDao;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnDateUtil;
import com.nncloudtv.model.NnEpisode;
import com.nncloudtv.model.NnProgram;
import com.nncloudtv.model.TitleCard;

@Service
public class TitleCardManager {

    protected static final Logger log = Logger.getLogger(TitleCardManager.class.getName());
    
    private TitleCardDao dao = NNF.getTitleCardDao();
    
    public TitleCard save(TitleCard card) {
        if (card==null) return null;
        card.setUpdateDate(NnDateUtil.now());
        return dao.save(card);
    }
    
    public void delete(TitleCard card) {
        dao.delete(card);
    }
    
    public void deleteAll(List<TitleCard> titlecards) {
        dao.deleteAll(titlecards);
    }
    
    public List<TitleCard> findByProgramId(long programId) {
        return dao.findByProgramId(programId);
    }
    
    public TitleCard findById(long id) {
        return dao.findById(id);
    }
    
    public TitleCard findByProgramIdAndType(long programId, short type) {
    
        return dao.findByProgramIdAndType(programId, type);
    }
    
    public List<TitleCard> findByEpisodeId(long episodeId) {
        
        List<NnProgram> programs = NNF.getProgramMngr().findByEpisodeId(episodeId);
        if (programs.size() == 0) {
            return new ArrayList<TitleCard>();
        }
        HashMap<Long, NnProgram> programMap = new HashMap<Long, NnProgram>();
        for (NnProgram program : programs) {
            programMap.put(program.getId(), program);
        }
        
        NnEpisode episode = NNF.getEpisodeMngr().findById(episodeId);
        if (episode == null) {
            return new ArrayList<TitleCard>();
        }
        
        List<TitleCard> titleCardsFromEpisode = new ArrayList<TitleCard>();
        List<TitleCard> titleCardsFromChannel = dao.findByChannel(episode.getChannelId());
        for (TitleCard titleCard : titleCardsFromChannel) {
            if (programMap.containsKey(titleCard.getProgramId())) {
                titleCardsFromEpisode.add(titleCard);
            }
        }
        
        return titleCardsFromEpisode;
    }
    
    public TitleCard findById(String idStr) {
        
        return dao.findById(idStr);
    }
}
