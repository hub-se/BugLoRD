/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.internal.matchers;

import org.hamcrest.SelfDescribing;

import java.io.Serializable;

public interface ContainsExtraTypeInformation extends Serializable {
    SelfDescribing withExtraTypeInfo();

    boolean typeMatches(Object object);
}