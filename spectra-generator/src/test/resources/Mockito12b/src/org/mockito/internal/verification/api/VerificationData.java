/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.internal.verification.api;

import org.mockito.internal.invocation.Invocation;
import org.mockito.internal.invocation.InvocationMatcher;

import java.util.List;

public interface VerificationData {

    List<Invocation> getAllInvocations();

    InvocationMatcher getWanted();

}