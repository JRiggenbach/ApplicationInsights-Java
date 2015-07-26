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

import com.microsoft.applicationinsights.agent.internal.agent.instrumentor.DefaultClassInstrumentor;
import com.microsoft.applicationinsights.agent.internal.agent.instrumentor.DefaultMethodInstrumentor;
import com.microsoft.applicationinsights.agent.internal.agent.instrumentor.SqlStatementMethodInstrumentor;
import com.microsoft.applicationinsights.agent.internal.common.StringUtils;
import com.microsoft.applicationinsights.agent.internal.coresync.InstrumentedClassType;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

/**
 * An 'instrumented' class data
 *
 * Created by gupele on 5/19/2015.
 */
public final class ClassInstrumentationData {
    private final String className;

    // The type of class
    private final InstrumentedClassType classType;

    // Methods that will be instrumented
    private final MethodInstrumentationInfo methodInstrumentationInfo;

    private boolean reportExecutionTime;
    private boolean reportCaughtExceptions;

    private DefaultClassInstrumentorFactory classVisitorFactory;

    public ClassInstrumentationData(String className, InstrumentedClassType classType) {
        this(className, classType, null);
    }

    public ClassInstrumentationData(String className, InstrumentedClassType classType, DefaultClassInstrumentorFactory classVisitorFactory) {
        this.className = className;
        this.classType = classType;
        this.classVisitorFactory = classVisitorFactory;
        this.methodInstrumentationInfo = new MethodInstrumentationInfo();
    }

    public boolean addMethod(String methodName, String signature, boolean reportCaughtExceptions, boolean reportExecutionTime) {
        if (StringUtils.isNullOrEmpty(methodName)) {
            return false;
        }

        MethodInstrumentationRequest request =
                new MethodInstrumentationRequestBuilder()
                .withMethodName(methodName)
                .withMethodSignature(signature)
                .withReportCaughtExceptions(reportCaughtExceptions)
                .withReportExecutionTime(reportExecutionTime).create();
        methodInstrumentationInfo.addMethod(request);
        return true;
    }

    public MethodInstrumentationDecision getDecisionForMethod(String methodName, String methodSignature) {
        return methodInstrumentationInfo.getDecision(methodName, methodSignature);
    }

    public ClassInstrumentationData setReportExecutionTime(boolean reportExecutionTime) {
        this.reportExecutionTime = reportExecutionTime;
        return this;
    }

    public ClassInstrumentationData setReportCaughtExceptions(boolean reportCaughtExceptions) {
        this.reportCaughtExceptions = reportCaughtExceptions;
        return this;
    }

    public void addAllMethods(boolean reportCaughtExceptions, boolean reportExecutionTime) {
        methodInstrumentationInfo.addAllMethods(reportCaughtExceptions, reportExecutionTime);
    }

    public String getClassName() {
        return className;
    }

    public InstrumentedClassType getClassType() {
        return classType;
    }

    public MethodInstrumentationInfo getMethodInstrumentationInfo() {
        return methodInstrumentationInfo;
    }

    public boolean isReportExecutionTime() {
        return reportExecutionTime;
    }

    public boolean isReportCaughtExceptions() {
        return reportCaughtExceptions;
    }

    public DefaultClassInstrumentor getDefaultClassInstrumentor(ClassWriter cw) {
        if (classVisitorFactory == null) {
            return new DefaultClassInstrumentor(this, cw);
        }
        return classVisitorFactory.create(this, cw);
    }
}

