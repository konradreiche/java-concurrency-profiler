/*
 * Agent.h
 *
 *  Created on: 19.04.2011
 *      Author: Konrad Johannes Reiche
 */

#ifndef AGENT_H_
#define AGENT_H_

#include <jvmti.h>
#include "AgentHelper.h"

namespace Agent {

void JNICALL callbackMonitorWait(jvmtiEnv *jvmti_env, JNIEnv* jni_env, jthread thread,
		jobject object, jlong timeout);

void JNICALL MonitorWaited(jvmtiEnv *jvmti_env, JNIEnv* jni_env,
		jthread thread, jobject object, jboolean timed_out);

void JNICALL callbackThreadStart(jvmtiEnv *jvmti_env, JNIEnv* env,
		jthread thread);
}

#endif /* AGENT_H_ */
