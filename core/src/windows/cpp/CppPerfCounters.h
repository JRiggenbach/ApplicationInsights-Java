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

#include <jni.h>

#ifndef _CPPPERFCOUNTERS_H_
#define _CPPPERFCOUNTERS_H_

#ifdef __cplusplus
extern "C" {
#endif

	/**
     * Get the Windows instnace name of the current JVM process
	 */
	JNIEXPORT jstring JNICALL Java_com_microsoft_applicationinsights_internal_perfcounter_JniPCConnector_getInstanceName
		(JNIEnv *, jclass, jint);

	/**
	 * Add Performance Counter by suppling its category name, counter name and instance name, the function returns a key
	 */
	JNIEXPORT jstring JNICALL Java_com_microsoft_applicationinsights_internal_perfcounter_JniPCConnector_addCounter
		(JNIEnv *, jclass, jstring, jstring, jstring);

	/**
	 * Get the value of a Perfomance Counter by supplying its key as we got it from the '...._addCounter' function.
	 */
	JNIEXPORT jdouble JNICALL Java_com_microsoft_applicationinsights_internal_perfcounter_JniPCConnector_getPerformanceCounterValue
		(JNIEnv *, jclass, jstring);
#ifdef __cplusplus
}
#endif
#endif // _CPPPERFCOUNTERS_H_

