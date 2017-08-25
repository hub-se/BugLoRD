/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */

import static org.evosuite.shaded.org.mockito.Mockito.*;
import static org.junit.Assert.*;

import org.evosuite.shaded.org.mockito.ArgumentCaptor;
import org.evosuite.shaded.org.mockito.Captor;
import org.evosuite.shaded.org.mockito.Mock;
import org.evosuite.shaded.org.mockito.MockitoAnnotations;
import org.junit.Before;
import org.junit.Test;

//see issue 188
public class CaptorAnnotationAutoboxingTest {
	
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }
    
    interface Fun {
        void doFun(double prmitive);
        void moreFun(int howMuch);
    }
    
    @Mock Fun fun;
    @Captor ArgumentCaptor<Double> captor;

    @Test
    public void shouldAutoboxSafely() {
        //given
        fun.doFun(1.0);
        
        //then
        verify(fun).doFun(captor.capture());
        assertEquals((Double) 1.0, captor.getValue());
    }

    @Captor ArgumentCaptor<Integer> intCaptor;
    
    @Test
    public void shouldAutoboxAllPrimitives() {
        verify(fun, never()).moreFun(intCaptor.capture());
    }
}