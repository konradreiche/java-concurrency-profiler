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

void Agent::Helper::commitAgentMessage(AgentMessage agentMessage,
		AgentSocket agentSocket, int JVM_ID) {

	agentMessage.set_timestamp(rdtsc());
	agentMessage.set_jvm_id(JVM_ID);

	boost::asio::streambuf b;
	std::ostream os(&b);

	ZeroCopyOutputStream *raw_output = new OstreamOutputStream(&os);
	CodedOutputStream *coded_output = new CodedOutputStream(raw_output);

	coded_output->WriteVarint32(agentMessage.ByteSize());
	agentMessage.SerializeToCodedStream(coded_output);

	delete coded_output;
	delete raw_output;

	agentSocket.send(b);
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

	Agent::Helper::StrackTraceElement stackTraceElement;
	jvmtiFrameInfo stackTraceFrames[index + 1];
	jclass declaringClass;
	jint count;

	char *methodName;
	char *classSignature;
	char *sourceFile;
	jboolean isNative;

	std::string className;

	jvmti->GetStackTrace(thread, 0, index+1, stackTraceFrames, &count);

	if (index > count - 1) {
		index = count - 1;
	}

	jvmti->GetMethodName(stackTraceFrames[index].method, &methodName, NULL,
			NULL);
	jvmti->GetMethodDeclaringClass(
			stackTraceFrames[index].method, &declaringClass);
	jvmti->GetClassSignature(declaringClass, &classSignature, NULL);
	jvmti->GetSourceFileName(declaringClass, &sourceFile);
	jvmti->IsMethodNative(stackTraceFrames[index].method, &isNative);

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
