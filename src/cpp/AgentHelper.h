/*
 * AgentHelper.h
 *
 *  Created on: 19.04.2011
 *      Author: Konrad Johannes Reiche
 */

#ifndef AGENTHELPER_H_
#define AGENTHELPER_H_

#include <jvmti.h>

#include <boost/asio.hpp>
#include <boost/lexical_cast.hpp>
#include <boost/algorithm/string.hpp>

#include <google/protobuf/io/coded_stream.h>
#include <google/protobuf/io/zero_copy_stream_impl.h>

#include <stdint.h>
#include "AgentSocket.h"
#include "AgentMessage.pb.h"

namespace Agent {

namespace Helper {

/**
 * Sends an agent message via socket to the listening interface.
 * The message is serialized to a byte stream.
 *
 * @param agentMessage
 * @param agentSocket
 * @param JVM_ID
 */

struct StrackTraceElement {
	std::string className;
	std::string methodName;
	std::string sourceFile;
	bool isNativeMethod;
};

void commitAgentMessage(AgentMessage agentMessage, AgentSocket agentSocket,
		int JVM_ID);

/**
 * When this is a native call, the class signature is read from the stack trace element depth-1
 */
StrackTraceElement getStackTraceElement(jvmtiEnv *jvmti, jthread thread,
		int index);

}
}

#endif /* AGENTHELPER_H_ */
