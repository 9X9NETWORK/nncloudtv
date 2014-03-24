package com.nncloudtv.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.apache.commons.lang.SerializationUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.nncloudtv.model.SysTag;
import com.nncloudtv.model.SysTagDisplay;
import com.nncloudtv.model.SysTagMap;
import com.nncloudtv.web.json.cms.Set;

/**
 * This is unit test for ApiMsoService's method, use Mockito mock dependence object.
 * Each test case naming begin with target method name, plus dash and a serial number. 
 */
@RunWith(MockitoJUnitRunner.class)
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
        
        SysTag fetched_systag = new SysTag();
        fetched_systag.setId(setId);
        when(sysTagMngr.findById(setId)).thenReturn(fetched_systag);
        
        SysTagDisplay fetched_display = new SysTagDisplay();
        fetched_display.setSystagId(setId);
        when(sysTagDisplayMngr.findBySysTagId(setId)).thenReturn(fetched_display);
        when(sysTagMapMngr.findBySysTagId(setId)).thenReturn(new ArrayList<SysTagMap>());
        
        SysTag wanted_systag = (SysTag) SerializationUtils.clone(fetched_systag);
        wanted_systag.setSeq(seq);
        wanted_systag.setSorting(sortingType);
        SysTag saved_systag = (SysTag) SerializationUtils.clone(wanted_systag);
        when(sysTagMngr.save(wanted_systag)).thenReturn(saved_systag);
        
        SysTagDisplay wanted_display = (SysTagDisplay) SerializationUtils.clone(fetched_display);
        wanted_display.setName(name);
        wanted_display.setPopularTag(tag);
        wanted_display.setCntChannel(0);
        SysTagDisplay saved_display = (SysTagDisplay) SerializationUtils.clone(wanted_display);
        when(sysTagDisplayMngr.save(wanted_display)).thenReturn(saved_display);
        
        Set set = new Set();
        set.setName(name);
        set.setSeq(seq);
        set.setTag(tag);
        set.setSortingType(sortingType);
        set.setChannelCnt(0);
        set.setId(setId);
        Set expected = (Set) SerializationUtils.clone(set);
        when(setService.composeSet(wanted_systag, wanted_display)).thenReturn(set);
        
        Set actual = apiMsoService.setUpdate(setId, name, seq, tag, sortingType);
        
        verify(sysTagMngr).findById(setId);
        verify(sysTagDisplayMngr).findBySysTagId(setId);
        verify(sysTagMapMngr).findBySysTagId(setId);
        verify(sysTagMngr).save(wanted_systag);
        verify(sysTagDisplayMngr).save(wanted_display);
        verify(setService).composeSet(wanted_systag, wanted_display);
        assertEquals(expected, actual);
    }

}
