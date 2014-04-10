package com.nncloudtv.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang.SerializationUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.nncloudtv.model.Mso;
import com.nncloudtv.model.MsoConfig;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.SysTag;
import com.nncloudtv.model.SysTagDisplay;
import com.nncloudtv.model.SysTagMap;
import com.nncloudtv.web.json.cms.Set;

/**
 * This is unit test for ApiMsoService's method, use Mockito mock dependence object.
 * Each test case naming begin with target method name, plus dash and a serial number. 
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(MsoConfigManager.class)
public class ApiMsoServiceTest {
    
    protected static final Logger log = Logger.getLogger(ApiMsoServiceTest.class.getName());
    
    /** target class for testing */
    private ApiMsoService apiMsoService;
    
    @Mock private SetService setService;
    @Mock private SysTagManager sysTagMngr;
    @Mock private SysTagDisplayManager sysTagDisplayMngr;
    @Mock private SysTagMapManager sysTagMapMngr;
    @Mock private NnChannelManager channelMngr;
    @Mock private StoreService storeService;
    @Mock private StoreListingManager storeListingMngr;
    @Mock private MsoManager msoMngr;
    @Mock private CategoryService categoryService;
    @Mock private MsoConfigManager configMngr;
    
    @Before  
    public void setUp() {
        apiMsoService = new ApiMsoService(setService, sysTagMngr, sysTagDisplayMngr,
                sysTagMapMngr, channelMngr, storeService, storeListingMngr, msoMngr,
                categoryService, configMngr);
    }
    
    @After
    public void tearDown() {
        setService = null;
        sysTagMngr = null;
        sysTagDisplayMngr = null;
        sysTagMapMngr = null;
        channelMngr = null;
        storeService = null;
        storeListingMngr = null;
        msoMngr = null;
        categoryService = null;
        configMngr = null;
        
        apiMsoService = null;
    }
    
    // normal case and provide lang
    @Test
    public void msoSets_0() {
        
        // input arguments
        final Long msoId = (long) 1;
        final String lang = "zh";
        
        // stubs
        when(setService.findByMsoIdAndLang((Long) anyLong(), anyString())).thenReturn(new ArrayList<Set>());
        
        // execute
        List<Set> actual = apiMsoService.msoSets(msoId, lang);
        
        // verify
        verify(setService).findByMsoIdAndLang(msoId, lang);
        assertEquals(new ArrayList<Set>(), actual);
    }
    
    // when bad result returned, should obey contract
    @Test
    public void msoSets_1() {
        
        // input arguments
        final Long msoId = (long) 1;
        final String lang = "zh";
        
        // stubs
        when(setService.findByMsoIdAndLang((Long) anyLong(), anyString())).thenReturn(null);// bad result
        
        // execute
        List<Set> actual = apiMsoService.msoSets(msoId, lang);
        
        // verify
        verify(setService).findByMsoIdAndLang(msoId, lang);
        assertEquals(new ArrayList<Set>(), actual);
    }
    
    // normal case but not provide lang
    @Test
    public void msoSets_2() {
        
        // input arguments
        final Long msoId = (long) 1;
        final String lang = null;
        
        // stubs
        when(setService.findByMsoId((Long) anyLong())).thenReturn(new ArrayList<Set>());
        
        // execute
        List<Set> actual = apiMsoService.msoSets(msoId, lang);
        
        // verify
        verify(setService).findByMsoId(msoId);
        assertEquals(new ArrayList<Set>(), actual);
    }
    
    // invalid input
    @Test
    public void msoSets_3() {
        
        // input arguments
        final Long msoId = null;
        final String lang = null;
        
        // execute
        List<Set> actual = apiMsoService.msoSets(msoId, lang);
        
        // verify
        assertEquals(new ArrayList<Set>(), actual);
    }
    
    @Test
    public void msoSetCreate_0() {
        
        // input arguments
        final Long msoId = (long) 1;
        final short seq = 1;
        final String tag = "tag";
        final String name = "name";
        final short sortingType = SysTag.SORT_SEQ;
        
        // mock data
        Mso mso = new Mso("name", "intro", "contactEmail", Mso.TYPE_MSO);
        mso.setId(msoId);
        
        MsoConfig supportedRegion = new MsoConfig();
        supportedRegion.setMsoId(msoId);
        supportedRegion.setItem(MsoConfig.SUPPORTED_REGION);
        supportedRegion.setValue("zh 台灣");
        
        List<String> spheres = new ArrayList<String>();
        spheres.add("zh");
        
        Set set = new Set();
        set.setMsoId(msoId);
        set.setName(name);
        set.setSeq(seq);
        set.setTag(tag);
        set.setSortingType(sortingType);
        set.setLang("zh");
        
        // stubs
        when(msoMngr.findById((Long) anyLong())).thenReturn(mso);
        when(configMngr.findByMsoAndItem((Mso) anyObject(), anyString())).thenReturn(supportedRegion);
        
        PowerMockito.mockStatic(MsoConfigManager.class);
        when(MsoConfigManager.parseSupportedRegion(anyString())).thenReturn(spheres);
        
        when(setService.create((Set) anyObject())).thenReturn(set);
        
        // execute
        Set expected = (Set) SerializationUtils.clone(set);
        Set actual = apiMsoService.msoSetCreate(msoId, seq, tag, name, sortingType);
        
        // verify
        verify(msoMngr).findById(msoId);
        verify(configMngr).findByMsoAndItem(mso, MsoConfig.SUPPORTED_REGION);
        
        PowerMockito.verifyStatic();
        MsoConfigManager.parseSupportedRegion(supportedRegion.getValue());
        
        ArgumentCaptor<Set> set_arg = ArgumentCaptor.forClass(Set.class);
        verify(setService).create(set_arg.capture());
        assertEquals((Object) msoId, set_arg.getValue().getMsoId());
        assertEquals(name, set_arg.getValue().getName());
        assertEquals(seq, set_arg.getValue().getSeq());
        assertEquals(tag, set_arg.getValue().getTag());
        assertEquals(sortingType, set_arg.getValue().getSortingType());
        assertEquals("zh", set_arg.getValue().getLang());
        
        assertEquals(expected, actual);
    }
    
    // if NnSet exist
    @Test
    public void set_0() {
        
        when(setService.findById(anyLong())).thenReturn(new Set());
        
        Set result = apiMsoService.set(anyLong());
        assertNotNull(result);
        
        verify(setService).findById(anyLong());
    }
    
    // if NnSet not exist
    @Test
    public void set_1() {
        
        when(setService.findById(anyLong())).thenReturn(null);
        
        Set result = apiMsoService.set(anyLong());
        assertNull(result);
        
        verify(setService).findById(anyLong());
    }
    
    // if invalid input such as NULL
    @Test
    public void set_2() {
        
        Set result = apiMsoService.set(null);
        assertNull(result);
        
        verifyZeroInteractions(setService);
    }
    
    // given arguments and return wanted result
    @Test
    public void setUpdate_0() {
        
        // input arguments
        final Long setId = (long) 1;
        final String name = "name";
        final short seq = 1;
        final String tag = "tag";
        final short sortingType = SysTag.SORT_SEQ;
        
        // mock data
        SysTag systag = new SysTag();
        systag.setId(setId);
        SysTagDisplay display = new SysTagDisplay();
        display.setSystagId(setId);
        Set set = new Set();
        set.setName(name);
        set.setSeq(seq);
        set.setTag(tag);
        set.setSortingType(sortingType);
        set.setChannelCnt(0);
        set.setId(setId);
        
        // stubs
        when(sysTagMngr.findById((Long) anyLong())).thenReturn(systag);
        when(sysTagDisplayMngr.findBySysTagId((Long) anyLong())).thenReturn(display);
        when(sysTagMapMngr.findBySysTagId((Long) anyLong())).thenReturn(new ArrayList<SysTagMap>());
        when(sysTagMngr.save((SysTag) anyObject())).thenReturn(systag);
        when(sysTagDisplayMngr.save((SysTagDisplay) anyObject())).thenReturn(display);
        when(setService.composeSet((SysTag) anyObject(), (SysTagDisplay) anyObject())).thenReturn(set);
        
        // execute
        Set expected = (Set) SerializationUtils.clone(set);
        Set actual = apiMsoService.setUpdate(setId, name, seq, tag, sortingType);
        
        // verify
        verify(sysTagMngr).findById(setId);
        verify(sysTagDisplayMngr).findBySysTagId(setId);
        verify(sysTagMapMngr).findBySysTagId(setId);
        
        ArgumentCaptor<SysTag> systag_arg = ArgumentCaptor.forClass(SysTag.class);
        verify(sysTagMngr).save(systag_arg.capture());
        assertEquals(seq, systag_arg.getValue().getSeq());
        assertEquals(sortingType, systag_arg.getValue().getSorting());
        
        ArgumentCaptor<SysTagDisplay> display_arg = ArgumentCaptor.forClass(SysTagDisplay.class);
        verify(sysTagDisplayMngr).save(display_arg.capture());
        assertEquals(name, display_arg.getValue().getName());
        assertEquals(tag, display_arg.getValue().getPopularTag());
        assertEquals(0, display_arg.getValue().getCntChannel());
        
        verify(setService).composeSet(systag, display);
        assertEquals(expected, actual);
    }
    
    @Test
    public void setDelete_0() {
        
        // input arguments
        final Long setId = (long) 1;
        
        // execute
        apiMsoService.setDelete(setId);
        
        // verify
        verify(setService).delete(setId);
    }
    
    @Test
    public void setDelete_1() {
        
        // input arguments
        final Long setId = null;
        
        // execute
        apiMsoService.setDelete(setId);
        
        // verify
        verifyZeroInteractions(setService);
    }
    
    @Test
    public void setChannels_0() {
        
        // input arguments
        final Long setId = (long) 1;
        
        // mock data
        SysTag systag = new SysTag();
        systag.setId(setId);
        systag.setType(SysTag.TYPE_SET);
        systag.setSorting(SysTag.SORT_SEQ);
        List<NnChannel> channels = new ArrayList<NnChannel>();
        
        // stubs
        when(sysTagMngr.findById((Long) anyLong())).thenReturn(systag);
        when(setService.getChannelsOrderBySeq((Long) anyLong())).thenReturn(channels);
        when(channelMngr.responseNormalization(anyListOf(NnChannel.class))).thenReturn(channels);
        
        // execute
        List<NnChannel> actual = apiMsoService.setChannels(setId);
        
        // verify
        verify(sysTagMngr).findById(setId);
        verify(setService).getChannelsOrderBySeq(setId);
        verify(setService, never()).getChannelsOrderByUpdateTime((Long) anyLong());
        verify(channelMngr).responseNormalization(anyListOf(NnChannel.class));
        assertNotNull(actual);
    }
    
    @Test
    public void setChannels_1() {
        
        // input arguments
        final Long setId = (long) 1;
        
        // mock data
        SysTag systag = new SysTag();
        systag.setId(setId);
        systag.setType(SysTag.TYPE_SET);
        systag.setSorting(SysTag.SORT_DATE);
        List<NnChannel> channels = new ArrayList<NnChannel>();
        
        // stubs
        when(sysTagMngr.findById((Long) anyLong())).thenReturn(systag);
        when(setService.getChannelsOrderByUpdateTime((Long) anyLong())).thenReturn(channels);
        when(channelMngr.responseNormalization(anyListOf(NnChannel.class))).thenReturn(channels);
        
        // execute
        List<NnChannel> actual = apiMsoService.setChannels(setId);
        
        // verify
        verify(sysTagMngr).findById(setId);
        verify(setService).getChannelsOrderByUpdateTime(setId);
        verify(setService, never()).getChannelsOrderBySeq((Long) anyLong());
        verify(channelMngr).responseNormalization(anyListOf(NnChannel.class));
        assertNotNull(actual);
    }
    
    @Test
    public void setChannels_2() {
        
        // input arguments
        final Long setId = null;
        
        // execute
        List<NnChannel> actual = apiMsoService.setChannels(setId);
        
        // verify
        assertNotNull(actual);
    }
    
    @Test
    public void setChannelAdd_0() {
        
        // input arguments
        final Long setId = (long) 1;
        final Long channelId = (long) 1;
        final Short timeStart = null;
        final Short timeEnd = null;
        final Boolean alwaysOnTop = null;
        
        // execute
        apiMsoService.setChannelAdd(setId, channelId, timeStart, timeEnd, alwaysOnTop);
        
        // verify
        verify(setService).addChannelToSet(setId, channelId, timeStart, timeEnd, alwaysOnTop);
    }
    
    @Test
    public void setChannelAdd_1() {
        
        // input arguments
        final Long setId = null;
        final Long channelId = null;
        final Short timeStart = null;
        final Short timeEnd = null;
        final Boolean alwaysOnTop = null;
        
        // execute
        apiMsoService.setChannelAdd(setId, channelId, timeStart, timeEnd, alwaysOnTop);
        
        // verify
        verifyZeroInteractions(setService);
    }
    
    @Test
    public void setChannelRemove_0() {
        
        // input arguments
        final Long setId = (long) 1;
        final Long channelId = (long) 1;
        
        // mocks
        SysTagMap sysTagMap = new SysTagMap(setId, channelId);
        
        // stubs
        when(sysTagMapMngr.findBySysTagIdAndChannelId((Long) anyLong(), (Long) anyLong())).thenReturn(sysTagMap);
        
        // execute
        apiMsoService.setChannelRemove(setId, channelId);
        
        // verify
        verify(sysTagMapMngr).findBySysTagIdAndChannelId(setId, channelId);
        verify(sysTagMapMngr).delete(sysTagMap);
    }
    
    @Test
    public void setChannelRemove_1() {
        
        // input arguments
        final Long setId = null;
        final Long channelId = null;
        
        // execute
        apiMsoService.setChannelRemove(setId, channelId);
        
        // verify
        verifyZeroInteractions(sysTagMapMngr);
    }
    
    @Test
    public void setChannelsSorting_0() {
        
        // input arguments
        final Long setId = (long) 1;
        final List<Long> sortedChannels = new ArrayList<Long>();
        sortedChannels.add((long) 3);
        sortedChannels.add((long) 2);
        sortedChannels.add((long) 1);
        
        // mocks
        List<SysTagMap> setChannels = new ArrayList<SysTagMap>();
        for(int count = 1; count <= 3; count++) {
            SysTagMap sysTagMap = new SysTagMap(setId, count);
            sysTagMap.setSeq((short) count);
            setChannels.add(sysTagMap);
        }
        
        // stubs
        when(sysTagMapMngr.findBySysTagId((Long) anyLong())).thenReturn(setChannels);
        
        // execute
        apiMsoService.setChannelsSorting(setId, sortedChannels);
        
        // verify
        verify(sysTagMapMngr).findBySysTagId(setId);
        
        ArgumentCaptor<List<SysTagMap>> sysTagMaps_arg = ArgumentCaptor.forClass((Class) List.class);
        verify(sysTagMapMngr).saveAll(sysTagMaps_arg.capture());
        List<SysTagMap> sysTagMaps = sysTagMaps_arg.getValue();
        assertEquals(3, sysTagMaps.size());
        assertEquals(3, sysTagMaps.get(0).getChannelId());
        assertEquals(2, sysTagMaps.get(1).getChannelId());
        assertEquals(1, sysTagMaps.get(2).getChannelId());
    }
    
    @Test
    public void storeChannels_0() {
        
        // input arguments
        final Long msoId = (long) 1;
        final java.util.Set<Long> channelIds = new HashSet<Long>();
        channelIds.add(new Long(1));
        channelIds.add(new Long(2));
        channelIds.add(new Long(3));
        final Long categoryId = null;
        
        // mocks
        List<NnChannel> channels = new ArrayList<NnChannel>();
        
        // stubs
        when(channelMngr.findByIds(anyListOf(Long.class))).thenReturn(channels);
        when(storeService.checkChannelsInMsoStore(anyListOf(NnChannel.class), (Long) anyLong()))
        .thenReturn(new ArrayList<Long>());
        
        // execute
        List<Long> actual = apiMsoService.storeChannels(msoId, channelIds, categoryId);
        
        // verify
        ArgumentCaptor<List<Long>> arg = ArgumentCaptor.forClass((Class) List.class);
        verify(channelMngr).findByIds(arg.capture());
        List<Long> channelIds_arg = arg.getValue();
        assertEquals(3, channelIds_arg.size());
        assertEquals(1, (long) channelIds_arg.get(0));
        assertEquals(2, (long) channelIds_arg.get(1));
        assertEquals(3, (long) channelIds_arg.get(2));
        
        verify(storeService).checkChannelsInMsoStore(channels, msoId);
        assertNotNull(actual);
    }
    
    @Test
    public void storeChannels_1() {
        
        // input arguments
        final Long msoId = (long) 1;
        final java.util.Set<Long> channelIds = null;
        final Long categoryId = (long) 1;
        
        // stubs
        when(storeService.getChannelIdsFromMsoStoreCategory((Long) anyLong(), (Long) anyLong()))
        .thenReturn(new ArrayList<Long>());
        
        // execute
        List<Long> actual = apiMsoService.storeChannels(msoId, channelIds, categoryId);
        
        // verify
        verifyZeroInteractions(channelMngr);
        verify(storeService).getChannelIdsFromMsoStoreCategory(categoryId, msoId);
        assertNotNull(actual);
    }
    
    @Test
    public void storeChannels_2() {
        
        // input arguments
        final Long msoId = (long) 1;
        final java.util.Set<Long> channelIds = null;
        final Long categoryId = null;
        
        // stubs
        when(storeService.getChannelIdsFromMsoStore((Long) anyLong())).thenReturn(new ArrayList<Long>());
        
        // execute
        List<Long> actual = apiMsoService.storeChannels(msoId, channelIds, categoryId);
        
        // verify
        verifyZeroInteractions(channelMngr);
        verify(storeService).getChannelIdsFromMsoStore(msoId);
        assertNotNull(actual);
    }
    
    @Test
    public void storeChannels_3() {
        
        // input arguments
        final Long msoId = null;
        final java.util.Set<Long> channelIds = null;
        final Long categoryId = null;
        
        // execute
        List<Long> actual = apiMsoService.storeChannels(msoId, channelIds, categoryId);
        
        // verify
        verifyZeroInteractions(channelMngr);
        verifyZeroInteractions(storeService);
        assertNotNull(actual);
    }

}
