package com.nncloudtv.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.nncloudtv.web.json.cms.Set;

/**
 * This is unit test for ApiMsoService's method, use Mockito mock dependence object.
 * Each test case function name begin with target method name, plus dash and a serial number avoid duplication. 
 */
@RunWith(MockitoJUnitRunner.class)
public class ApiMsoServiceTest {
    
    /** target class for testing */
    private ApiMsoService apiMsoService;
    
    @Mock
    private SetService setService;
    
    @Before  
    public void setUp() {  
        apiMsoService = new ApiMsoService(setService, null, null, null, null, null, null, null, null, null);  
    }  
  
    @After  
    public void tearDown() {  
        setService = null;       
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
        
        verify(setService, never()).findById(anyLong());
        verify(setService, never()).findById(null);
    }

}
