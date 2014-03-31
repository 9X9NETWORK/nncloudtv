package com.nncloudtv.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
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

}
