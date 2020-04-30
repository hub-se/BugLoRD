/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.internal.configuration;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockitousage.IMethods;
import org.mockitoutil.TestBase;

import static org.mockito.Mockito.*;

public class ReadingConfigurationFromClasspathTest extends TestBase {

    @Test
    public void shouldReadConfigurationClassFromClassPath() {
        ConfigurationAccess.getConfig().overrideDefaultAnswer(new Answer<Object>() {
            public Object answer(InvocationOnMock invocation) {
                return "foo";
            }
        });

        IMethods mock = mock(IMethods.class);
        assertEquals("foo", mock.simpleMethod());
    }
}