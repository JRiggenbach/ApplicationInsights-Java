/*
 * ApplicationInsights-Java
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.microsoft.applicationinsights.agent.internal.agent;

import com.microsoft.applicationinsights.agent.internal.agent.instrumentor.DefaultMethodInstrumentor;
import com.microsoft.applicationinsights.agent.internal.agent.instrumentor.HttpClientMethodInstrumentor;
import com.microsoft.applicationinsights.agent.internal.agent.instrumentor.SqlStatementMethodInstrumentor;
import org.objectweb.asm.MethodVisitor;

/**
 * Created by gupele on 5/20/2015.
 */
final class DefaultMethodInstrumentorsFactory {
    private final ClassDataProvider provider;

    public DefaultMethodInstrumentorsFactory(ClassDataProvider provider) {
        this.provider = provider;
    }

    public DefaultMethodInstrumentor getMethodVisitor(MethodInstrumentationDecision decision, int access, String desc, String className, String methodName, MethodVisitor methodVisitor) {
        if (provider.isHttpClass(className)) {
            return new HttpClientMethodInstrumentor(access, desc, className, methodName, methodVisitor);
        } else if (provider.isSqlClass(className)) {
            return new SqlStatementMethodInstrumentor(access, desc, className, methodName, methodVisitor);
        }

        return new DefaultMethodInstrumentor(decision, access, desc, className, methodName, methodVisitor);
    }
}
