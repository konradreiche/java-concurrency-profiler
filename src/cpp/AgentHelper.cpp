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

void Agent::Helper::insertAllStackTraces(jvmtiEnv *jvmti,
		AgentMessage::MonitorEvent *monitorEvent) {

	jvmtiStackInfo *stackInfo;
	jint threadCount;
	int currentThread;
	jvmtiError error;
	int MAX_FRAMES = 25;

	error = jvmti->GetAllStackTraces(MAX_FRAMES, &stackInfo, &threadCount);

	if (error != JVMTI_ERROR_NONE) {
		std::cout << "All Stack Traces could not been retrieved." << std::endl;
	}

	for (currentThread = 0; currentThread < threadCount; ++currentThread) {

		AgentMessage::StackTrace *stackTrace = monitorEvent->add_stacktraces();

		jvmtiStackInfo *stackInfoPointer = &stackInfo[currentThread];
		jthread thread = stackInfoPointer->thread;
		jvmtiFrameInfo *frames = stackInfoPointer->frame_buffer;

		AgentMessage::Thread *threadMessage = stackTrace->mutable_thread();
		initializeThreadMessage(jvmti, threadMessage, thread);

		int currentFrame;
		for (currentFrame = 0; currentFrame < stackInfoPointer->frame_count; ++currentFrame) {

			AgentMessage::StackTrace::StackTraceElement *stackTraceElement =
					stackTrace->add_stacktrace();

			jclass declaringClass;
			char *methodName;
			char *methodSignature;
			char *classSignature;
			char *sourceFile;
			jboolean isNative;
			std::string className;

			error = jvmti->GetMethodName(frames[currentFrame].method,
					&methodName, &methodSignature, NULL);
			checkError(jvmti, error, "GetMethodName");

			error = jvmti->GetMethodDeclaringClass(frames[currentFrame].method,
					&declaringClass);
			checkError(jvmti, error, "GetMethodDeclaringClass");

			error = jvmti->GetClassSignature(declaringClass, &classSignature,
					NULL);
			checkError(jvmti, error, "GetClassSignature");

			error = jvmti->GetSourceFileName(declaringClass, &sourceFile);
			checkError(jvmti, error, "GetSourceFileName");

			error = jvmti->IsMethodNative(frames[currentFrame].method,
					&isNative);
			checkError(jvmti, error, "IsMethodNative");

			className = std::string(classSignature);

			boost::algorithm::replace_first(className, "L", "");
			boost::algorithm::replace_all(className, "/", ".");
			boost::algorithm::replace_first(className, ";", "");

			stackTraceElement->set_classname(className);
			stackTraceElement->set_methodname(methodName);
			stackTraceElement->set_methodsignature(methodSignature);
			stackTraceElement->set_filename(sourceFile);
			stackTraceElement->set_isnativemethod(isNative);
		}

	}
}

void Agent::Helper::initializeThreadMessage(jvmtiEnv *jvmti,
		AgentMessage::Thread *threadMessage, jthread thread) {

	jvmtiThreadInfo jvmtiThreadInfo;
	jint thr_st_ptr;
	jlong thr_id_ptr;
	jvmtiError error;

	(void) memset(&jvmtiThreadInfo, 0, sizeof(jvmtiThreadInfo));
	error = jvmti->GetThreadInfo(thread, &jvmtiThreadInfo);
	Agent::Helper::checkError(jvmti, error, "Cannot get thread information.");

	error = jvmti->GetThreadState(thread, &thr_st_ptr);
	Agent::Helper::checkError(jvmti, error, "Cannot get thread state.");

	AgentMessage::Thread::State state;
	switch (thr_st_ptr & JVMTI_JAVA_LANG_THREAD_STATE_MASK) {
	case JVMTI_JAVA_LANG_THREAD_STATE_NEW:
		state = AgentMessage::Thread::NEW;
		break;
	case JVMTI_JAVA_LANG_THREAD_STATE_TERMINATED:
		state = AgentMessage::Thread::TERMINATED;
		break;
	case JVMTI_JAVA_LANG_THREAD_STATE_RUNNABLE:
		state = AgentMessage::Thread::RUNNABLE;
		break;
	case JVMTI_JAVA_LANG_THREAD_STATE_BLOCKED:
		state = AgentMessage::Thread::BLOCKED;
		break;
	case JVMTI_JAVA_LANG_THREAD_STATE_WAITING:
		state = AgentMessage::Thread::WAITING;
		break;
	case JVMTI_JAVA_LANG_THREAD_STATE_TIMED_WAITING:
		state = AgentMessage::Thread::TIMED_WAITING;
		break;
	default:
		state = AgentMessage::Thread::NEW;
		break;
	}

	jvmti->GetTag(thread, &thr_id_ptr);
	if (thr_id_ptr == 0) {
		jvmti->SetTag(thread, objectId);
		++objectId;
		jvmti->GetTag(thread, &thr_id_ptr);
	}

	threadMessage->set_id(thr_id_ptr);
	threadMessage->set_name(jvmtiThreadInfo.name);
	threadMessage->set_priority(jvmtiThreadInfo.priority);
	threadMessage->set_state(state);
	threadMessage->set_iscontextclassloaderset(
			jvmtiThreadInfo.context_class_loader == NULL);

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
Agent::Helper::StackTraceElement Agent::Helper::getStackTraceElement(
		jvmtiEnv *jvmti, jthread thread, int index) {

	jvmtiError error;

	Agent::Helper::StackTraceElement stackTraceElement;
	jvmtiFrameInfo stackTraceFrames[index + 1];
	jclass declaringClass;
	jint count;

	char *methodName;
	char *classSignature;
	char *sourceFile;
	jboolean isNative;

	std::string className;
	error
			= jvmti->GetStackTrace(thread, 0, index + 1, stackTraceFrames,
					&count);
	checkError(jvmti, error, "GetStackTrace");

	if (index > count - 1) {
		index = count - 1;
	}

	error = jvmti->GetMethodName(stackTraceFrames[index].method, &methodName,
			NULL, NULL);
	checkError(jvmti, error, "GetMethodName");

	error = jvmti->GetMethodDeclaringClass(stackTraceFrames[index].method,
			&declaringClass);
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
