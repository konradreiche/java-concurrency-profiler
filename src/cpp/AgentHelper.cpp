/*
 * AgentHelper.cpp
 *
 *  Created on: 19.04.2011
 *      Author: Konrad Johannes Reiche
 */

#include "AgentHelper.h"

using namespace google::protobuf::io;

void Agent::Helper::commitAgentMessage(AgentMessage agentMessage,
		AgentSocket agentSocket, int JVM_ID) {

	timespec ts;
	clock_gettime(CLOCK_THREAD_CPUTIME_ID, &ts);

	agentMessage.set_timestamp(ts.tv_nsec);
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

std::string Agent::Helper::getMethodContext(jvmtiEnv *jvmti, jthread thread,
		bool isMonitorCall) {
	int monitorCallOffset = isMonitorCall ? 0 : 1;

	jvmtiFrameInfo stackTraceFrames[4];
	jclass declaring_class;
	char *signature_ptr;
	char *methodName;
	std::string className;
	jint count;

	jvmti->GetStackTrace(thread, 0, 3, stackTraceFrames, &count);
	jvmti->GetMethodName(stackTraceFrames[2 - monitorCallOffset].method, &methodName,
			NULL, NULL);

	return methodName;
}

std::string Agent::Helper::getMonitorClass(jvmtiEnv *jvmti, jthread thread) {
	jvmtiFrameInfo stackTraceFrames[4];
	jclass declaring_class;
	char *signature_ptr;
	char *name;
	std::string className;
	jint count;

	jvmti->GetStackTrace(thread, 0, 3, stackTraceFrames, &count);
	jvmti->GetMethodDeclaringClass(stackTraceFrames[2].method, &declaring_class);
	jvmti->GetClassSignature(declaring_class, &signature_ptr, NULL);
	className = std::string(signature_ptr);

	boost::algorithm::replace_first(className, "L", "");
	boost::algorithm::replace_all(className, "/", ".");
	boost::algorithm::replace_first(className, ";", "");

	return className;
}
