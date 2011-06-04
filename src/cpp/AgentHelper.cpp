/*
 * AgentHelper.cpp
 *
 *  Created on: 19.04.2011
 *      Author: Konrad Johannes Reiche
 */

#include "AgentHelper.h"

using namespace google::protobuf::io;

extern "C" {
__inline__ uint64_t rdtsc() {
	uint32_t lo, hi;
	__asm__ __volatile__ ( // serialize
			"xorl %%eax,%%eax \n        cpuid"
			::: "%rax", "%rbx", "%rcx", "%rdx");
	/* We cannot use "=A", since this would use %rax on x86_64 and return only the lower 32bits of the TSC */
	__asm__ __volatile__ ("rdtsc" : "=a" (lo), "=d" (hi));
	return (uint64_t) hi << 32 | lo;
}
}

uint64_t Agent::Helper::getCurrentClockCycle() {
	return rdtsc();
}

/** Every JVMTI interface returns an error code, which should be checked
 *   to avoid any cascading errors down the line.
 *   The interface GetErrorName() returns the actual enumeration constant
 *   name, making the error messages much easier to understand.
 */
void Agent::Helper::checkError(jvmtiEnv *jvmti, jvmtiError errnum,
		const char *str) {
	if (errnum != JVMTI_ERROR_NONE) {
		char *errnum_str;

		errnum_str = NULL;
		jvmti->GetErrorName(errnum, &errnum_str);


		printf("ERROR: JVMTI: %d(%s): %s\n", errnum,
				(errnum_str == NULL ? "Unknown" : errnum_str),
				(str == NULL ? "" : str));

		if (errnum == JVMTI_ERROR_WRONG_PHASE) {
			exit(0);
		}
	}
}

/**
 * jvmtiFrameInfo is initialized with index + 2, thus one more than required due to the case
 * of an inherited method.
 *
 * @param jvmti
 * @param thread
 * @param index
 * @param isInheritedMethod
 * @return
 */
Agent::Helper::StrackTraceElement Agent::Helper::getStackTraceElement(
		jvmtiEnv *jvmti, jthread thread, int index) {

	jvmtiError error;

	Agent::Helper::StrackTraceElement stackTraceElement;
	jvmtiFrameInfo stackTraceFrames[index + 1];
	jclass declaringClass;
	jint count;

	char *methodName;
	char *classSignature;
	char *sourceFile;
	jboolean isNative;

	std::string className;
	error = jvmti->GetStackTrace(thread, 0, index+1, stackTraceFrames, &count);
	checkError(jvmti, error, "GetStackTrace");


	if (index > count - 1) {
		index = count - 1;
	}

	error = jvmti->GetMethodName(stackTraceFrames[index].method, &methodName, NULL,
			NULL);
	checkError(jvmti, error, "GetMethodName");

	error = jvmti->GetMethodDeclaringClass(
			stackTraceFrames[index].method, &declaringClass);
	checkError(jvmti, error, "GetMethodDeclaringClass");

	error = jvmti->GetClassSignature(declaringClass, &classSignature, NULL);
	checkError(jvmti, error, "GetClassSignature");

	error = jvmti->GetSourceFileName(declaringClass, &sourceFile);
	checkError(jvmti, error, "GetSourceFileName");

	error = jvmti->IsMethodNative(stackTraceFrames[index].method, &isNative);
	checkError(jvmti, error, "IsMethodNative");

	className = std::string(classSignature);

	boost::algorithm::replace_first(className, "L", "");
	boost::algorithm::replace_all(className, "/", ".");
	boost::algorithm::replace_first(className, ";", "");

	stackTraceElement.className = className;
	stackTraceElement.methodName = methodName;
	stackTraceElement.sourceFile = sourceFile;
	stackTraceElement.isNativeMethod = isNative;

	return stackTraceElement;
}
