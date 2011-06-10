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

#include <stdint.h>
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

//AgentMessage generateMethodEvent()

/** Every JVMTI interface returns an error code, which should be checked
 *   to avoid any cascading errors down the line.
 *   The interface GetErrorName() returns the actual enumeration constant
 *   name, making the error messages much easier to understand.
 */
void checkError(jvmtiEnv *jvmti, jvmtiError errnum, const char *str);

/**
 * When this is a native call, the class signature is read from the stack trace element depth-1
 */
StrackTraceElement getStackTraceElement(jvmtiEnv *jvmti, jthread thread,
		int index);

uint64_t getCurrentClockCycle();

}
}

#endif /* AGENTHELPER_H_ */
