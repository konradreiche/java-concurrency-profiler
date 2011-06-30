/**
 * The agent profiler collects data based on specified callback events which occur inside the JVM.
 */

#include <stdio.h>
#include <stdlib.h>
#include <iostream>
#include <string>
#include <vector>
#include <map>
#include <stack>
#include <sys/time.h>

#include <jvmti.h>

#include <google/protobuf/io/coded_stream.h>
#include <google/protobuf/io/zero_copy_stream_impl.h>

#include <boost/lexical_cast.hpp>
#include "MessageService.h"
#include "AgentMessage.pb.h"
#include "Agent.h"
#include "AgentHelper.h"

using namespace google::protobuf::io;

/** Some constant maximum sizes */
#define MAX_TOKEN_LENGTH        16
#define MAX_THREAD_NAME_LENGTH  512
#define MAX_METHOD_NAME_LENGTH  1024

static jvmtiEnv *jvmti = NULL;
static jvmtiCapabilities capa;

//static MessageService messageService("192.168.1.101", "50000");
static MessageService messageService("127.0.0.1", "50000");

static int jvmPid;

/** Global agent data structure */
typedef struct {
	/** JVMTI Environment */
	jvmtiEnv *jvmti;
	jboolean vm_is_started;
	/** Data access Lock */
	jrawMonitorID lock;
} GlobalAgentData;

static GlobalAgentData *gdata;

static jlong combined_size;
static int num_class_refs;
static int num_field_refs;
static int num_array_refs;
static int num_classloader_refs;
static int num_signer_refs;
static int num_protection_domain_refs;
static int num_interface_refs;
static int num_static_field_refs;
static int num_constant_pool_refs;

/**
 * Data structures to store information for method measurement.
 * Every thread has it own stack of timestamps. When a method is
 * entered a timestamp is pushed on to the stack. When a method
 * is left the timestamp is popped and its difference is calculated
 * to get the time spent within the method.
 */

typedef std::stack<long> timestampStack;
typedef std::stack<long> timeTakenStack;

static std::map<long, timestampStack> threadToTimestamps;
static std::map<long, timeTakenStack> threadToTimeTaken;

/**
 * Inserts the data accessible by jthread into an AgentMessage.
 *
 * @todo memset ensures the stack variable are garbage free - what does that mean?
 *
 * @param agentMessage the agent message containing valuable information which are send by the socket.
 * @param thread the thread containing valueable information.
 * @param lifeCycle whether the thread is a new one (true) or a finished one (false).
 * @return the extended agent message.
 */
static AgentMessage createThreadEventMessage(AgentMessage agentMessage,
		jthread thread, AgentMessage::ThreadEvent::EventType eventType) {

	AgentMessage::ThreadEvent *threadEvent = agentMessage.mutable_threadevent();
	threadEvent->set_eventtype(eventType);

	AgentMessage::Thread *threadMessage = threadEvent->add_thread();
	Agent::Helper::initializeThreadMessage(jvmti, threadMessage, thread);

	if (eventType == AgentMessage::ThreadEvent::ENDED) {
		jlong cpuTime;
		jvmti->GetThreadCpuTime(thread, &cpuTime);

		threadMessage->set_state(AgentMessage::Thread::TERMINATED);
		threadMessage->set_cputime(cpuTime);
	}

	return agentMessage;
}

static AgentMessage createMonitorEventMessage(AgentMessage agentMessage,
		jthread thread, AgentMessage::MonitorEvent::EventType eventType,
		std::string methodName, std::string className, jlong objectId,
		jvmtiMonitorUsage monitorUseage) {

	AgentMessage::MonitorEvent *monitorEvent =
			agentMessage.mutable_monitorevent();

	monitorEvent->set_eventtype(eventType);
	monitorEvent->set_classname(className);
	monitorEvent->set_methodname(methodName);

	AgentMessage::Thread *threadMessage = monitorEvent->mutable_thread();
	Agent::Helper::initializeThreadMessage(jvmti, threadMessage, thread);

	if (objectId != -1) {
		AgentMessage::Monitor *monitorMessage = monitorEvent->mutable_monitor();

		long ownerId;
		if (monitorUseage.owner != NULL) {
			jvmti->GetTag(monitorUseage.owner, &ownerId);
		} else {
			ownerId = -1;
		}

		Agent::Helper::insertAllStackTraces(jvmti, monitorEvent);

		monitorMessage->set_id(objectId);
		monitorMessage->set_owningthread(ownerId);
		monitorMessage->set_entrycount(monitorUseage.entry_count);
		monitorMessage->set_waitercount(monitorUseage.waiter_count);
		monitorMessage->set_notifywaitercount(monitorUseage.notify_waiter_count);

		for (int i = 0; i < monitorUseage.waiter_count; ++i) {
			AgentMessage::Thread *thread = monitorMessage->add_waiterthreads();
			Agent::Helper::initializeThreadMessage(jvmti, thread,
					monitorUseage.waiters[i]);
		}

		for (int i = 0; i < monitorUseage.notify_waiter_count; ++i) {
			AgentMessage::Thread *thread = monitorMessage->add_notifywaiterthreads();
			Agent::Helper::initializeThreadMessage(jvmti, thread,
					monitorUseage.notify_waiters[i]);
		}
	}

	return agentMessage;
}

/** Enter a critical section by doing a JVMTI Raw Monitor Enter */
static void enter_critical_section(jvmtiEnv *jvmti) {
	jvmtiError error;

	error = jvmti->RawMonitorEnter(gdata->lock);
	Agent::Helper::checkError(jvmti, error, "Cannot enter with raw monitor");
}

/** Exit a critical section by doing a JVMTI Raw Monitor Exit */
static void exit_critical_section(jvmtiEnv *jvmti) {
	jvmtiError error;

	error = jvmti->RawMonitorExit(gdata->lock);
	Agent::Helper::checkError(jvmti, error, "Cannot exit with raw monitor");
}

/**
 * @todo investigate if it is possible to assign the error to a string
 *
 * @param err
 */
void describe(jvmtiError err) {
	jvmtiError err0;
	char *descr;

	err0 = jvmti->GetErrorName(err, &descr);
	if (err0 == JVMTI_ERROR_NONE) {
		std::cout << descr;
	} else {
		printf("error [%d]", err);
	}
}

/** Sent when a thread is attempting to enter a Java programming language
 *  monitor already acquired by another thread.
 */
static void JNICALL callbackMonitorContendedEnter(jvmtiEnv *jvmti_env,
		JNIEnv *jni_env, jthread thread, jobject object) {

	enter_critical_section(jvmti);
	{
		jvmtiMonitorUsage monitorUseage;
		jvmti_env->GetObjectMonitorUsage(object, &monitorUseage);
		jlong currentObjectId;

		jvmti->GetTag(object, &currentObjectId);
		if (currentObjectId == 0) {
			jvmti->SetTag(object, Agent::Helper::objectId);
			++Agent::Helper::objectId;
			jvmti->GetTag(object, &currentObjectId);
		}

		AgentMessage agentMessage;

		Agent::Helper::StackTraceElement stackTraceElement =
				Agent::Helper::getStackTraceElement(jvmti_env, thread, 0);

		agentMessage = createMonitorEventMessage(agentMessage, thread,
				AgentMessage::MonitorEvent::CONTENDED,
				stackTraceElement.methodName, stackTraceElement.className,
				currentObjectId, monitorUseage);

		jlong systemTime;
		jvmti->GetTime(&systemTime);
		messageService.write(agentMessage, systemTime, jvmPid);
	}
	exit_critical_section(jvmti);
}

/**
 * Sent when a thread enters a Java programming language monitor after waiting
 * for it to be released by another thread.
 */
static void JNICALL callbackMonitorContendedEntered(jvmtiEnv *jvmti_env,
		JNIEnv *jni_env, jthread thread, jobject object) {

	enter_critical_section(jvmti);
	{
		jvmtiMonitorUsage monitorUseage;
		jvmti_env->GetObjectMonitorUsage(object, &monitorUseage);
		jlong currentObjectId;

		jvmti->GetTag(object, &currentObjectId);
		if (currentObjectId == 0) {
			jvmti->SetTag(object, Agent::Helper::objectId);
			++Agent::Helper::objectId;
			jvmti->GetTag(object, &currentObjectId);
		}

		AgentMessage agentMessage;

		Agent::Helper::StackTraceElement stackTraceElement =
				Agent::Helper::getStackTraceElement(jvmti_env, thread, 0);

		agentMessage = createMonitorEventMessage(agentMessage, thread,
				AgentMessage::MonitorEvent::ENTERED,
				stackTraceElement.methodName, stackTraceElement.className,
				currentObjectId, monitorUseage);

		jlong systemTime;
		jvmti->GetTime(&systemTime);
		messageService.write(agentMessage, systemTime, jvmPid);
	}
	exit_critical_section(jvmti);
}

/**
 * Sent when a thread is about to wait on an object. That is, a thread is
 * entering Object.wait(). This event is sent only during the live phase.
 */
void JNICALL callbackMonitorWait(jvmtiEnv *jvmti_env, JNIEnv* jni_env,
		jthread thread, jobject object, jlong timeout) {

	enter_critical_section(jvmti);
	{
		jvmtiMonitorUsage monitorUseage;
		jvmti_env->GetObjectMonitorUsage(object, &monitorUseage);
		jlong currentObjectId;

		jvmti->GetTag(object, &currentObjectId);
		if (currentObjectId == 0) {
			jvmti->SetTag(object, Agent::Helper::objectId);
			++Agent::Helper::objectId;
			jvmti->GetTag(object, &currentObjectId);
		}

		Agent::Helper::StackTraceElement stackTraceElement =
				Agent::Helper::getStackTraceElement(jvmti_env, thread, 2);

		AgentMessage agentMessage;
		agentMessage = createMonitorEventMessage(agentMessage, thread,
				AgentMessage::MonitorEvent::WAIT, stackTraceElement.methodName,
				stackTraceElement.className, currentObjectId, monitorUseage);

		jlong systemTime;
		jvmti->GetTime(&systemTime);
		messageService.write(agentMessage, systemTime, jvmPid);
	}
	exit_critical_section(jvmti);
}

/**
 * Sent when a thread finishes waiting on an object. That is, a thread is
 * leaving Object.wait(). This event is sent only during the live phase.
 */
void JNICALL callbackMonitorWaited(jvmtiEnv *jvmti_env, JNIEnv* jni_env,
		jthread thread, jobject object, jboolean timed_out) {

	enter_critical_section(jvmti);
	{
		jvmtiMonitorUsage monitorUseage;
		jvmti_env->GetObjectMonitorUsage(object, &monitorUseage);
		jlong currentObjectId;

		jvmti->GetTag(object, &currentObjectId);
		if (currentObjectId == 0) {
			jvmti->SetTag(object, Agent::Helper::objectId);
			++Agent::Helper::objectId;
			jvmti->GetTag(object, &currentObjectId);
		}

		Agent::Helper::StackTraceElement stackTraceElement =
				Agent::Helper::getStackTraceElement(jvmti_env, thread, 2);

		AgentMessage agentMessage;
		agentMessage = createMonitorEventMessage(agentMessage, thread,
				AgentMessage::MonitorEvent::WAITED,
				stackTraceElement.methodName, stackTraceElement.className,
				currentObjectId, monitorUseage);

		jlong systemTime;
		jvmti->GetTime(&systemTime);
		messageService.write(agentMessage, systemTime, jvmPid);
	}
	exit_critical_section(jvmti);
}

/** A new thread is started */
static void JNICALL callbackThreadStart(jvmtiEnv *jvmti_env, JNIEnv* env,
		jthread thread) {

	enter_critical_section(jvmti);
	{
		AgentMessage agentMessage;
		agentMessage = createThreadEventMessage(agentMessage, thread,
				AgentMessage::ThreadEvent::STARTED);
	}
	exit_critical_section(jvmti);
}

/** Thread End callback */
static void JNICALL callbackThreadEnd(jvmtiEnv *jvmti_env, JNIEnv* env,
		jthread thread) {

	enter_critical_section(jvmti);
	{
		AgentMessage agentMessage;
		agentMessage = createThreadEventMessage(agentMessage, thread,
				AgentMessage::ThreadEvent::ENDED);

		jlong systemTime;
		jvmti->GetTime(&systemTime);
		messageService.write(agentMessage, systemTime, jvmPid);
	}
	exit_critical_section(jvmti);
}

static void JNICALL callbackMethodEntry(jvmtiEnv *jvmti_env, JNIEnv *jni_env,
		jthread thread, jmethodID method) {

	enter_critical_section(jvmti);
	{
		long currentClockCycle = Agent::Helper::getCurrentClockCycle();
		jlong currentTime;
		jvmti->GetTime(&currentTime);

		jlong currentObjectId;
		jvmti->GetTag(thread, &currentObjectId);

		std::map<long, timestampStack>::iterator it1 = threadToTimestamps.find(
				currentObjectId);
		std::map<long, timeTakenStack>::iterator it2 = threadToTimeTaken.find(
				currentObjectId);

		if (it1 == threadToTimestamps.end()) {
			std::stack<long> *newTimeStampStack = new std::stack<long>;
			std::stack<long> *newTimeTakenStack = new std::stack<long>;
			threadToTimestamps.insert(std::pair<long, timestampStack>(
					currentObjectId, *newTimeStampStack));
			threadToTimeTaken.insert(std::pair<long, timestampStack>(
					currentObjectId, *newTimeTakenStack));
			it1 = threadToTimestamps.find(currentObjectId);
			it2 = threadToTimeTaken.find(currentObjectId);
		}

		it1->second.push(currentClockCycle);
		it2->second.push(currentTime);

	}
	exit_critical_section(jvmti);
}

static void JNICALL callbackMethodExit(jvmtiEnv *jvmti_env, JNIEnv *jni_env,
		jthread thread, jmethodID method, jboolean was_popped_by_exception,
		jvalue return_value) {

	enter_critical_section(jvmti);
	{
		Agent::Helper::StackTraceElement stackTraceElement =
				Agent::Helper::getStackTraceElement(jvmti_env, thread, 1);

		// notify wait events
		char *name;
		jvmti_env->GetMethodName(method, &name, NULL, NULL);
		std::string methodName = name;

		if (methodName == "notify" || methodName == "notifyAll") {

			AgentMessage::MonitorEvent::EventType eventType;

			if (methodName == "notify") {
				eventType = AgentMessage::MonitorEvent::NOTIFY;
			} else if (methodName == "notifyAll") {
				eventType = AgentMessage::MonitorEvent::NOTIFY_ALL;
			}

			jvmtiMonitorUsage monitorUseage;
			AgentMessage agentMessage;

			agentMessage = createMonitorEventMessage(agentMessage, thread,
					eventType, stackTraceElement.methodName,
					stackTraceElement.className, -1, monitorUseage);

			AgentMessage::MonitorEvent *monitorEvent =
					agentMessage.mutable_monitorevent();
			Agent::Helper::insertAllStackTraces(jvmti, monitorEvent);

			jlong systemTime;
			jvmti->GetTime(&systemTime);
			messageService.write(agentMessage, systemTime, jvmPid);
		}

		stackTraceElement = Agent::Helper::getStackTraceElement(jvmti_env,
				thread, 0);

		// method measuring
		long currentClockCycle = Agent::Helper::getCurrentClockCycle();
		jlong currentTime;
		jvmti->GetTime(&currentTime);

		jlong currentObjectId;
		jvmti->GetTag(thread, &currentObjectId);

		std::map<long, timestampStack>::iterator it1 = threadToTimestamps.find(
				currentObjectId);
		std::map<long, timeTakenStack>::iterator it2 = threadToTimeTaken.find(
				currentObjectId);

		long &clockCycle = it1->second.top();
		long &timeTaken = it2->second.top();
		long clockCyclesConsumed = currentClockCycle - clockCycle;
		long timeConsumed = currentTime - timeTaken;

		AgentMessage agentMessage;
		AgentMessage::MethodEvent *methodEvent =
				agentMessage.mutable_methodevent();
		AgentMessage::Thread *threadMessage = methodEvent->mutable_thread();
		Agent::Helper::initializeThreadMessage(jvmti, threadMessage, thread);

		methodEvent->set_classname(stackTraceElement.className);
		methodEvent->set_methodname(stackTraceElement.methodName);
		methodEvent->set_clockcycles(clockCyclesConsumed);
		methodEvent->set_timetaken(timeConsumed);

		jlong systemTime;
		jvmti->GetTime(&systemTime);
		messageService.write(agentMessage, systemTime, jvmPid);

		it1->second.pop();
		it2->second.pop();
	}
	exit_critical_section(jvmti);
}

/** Exception callback */
static void JNICALL callbackException(jvmtiEnv *jvmti_env, JNIEnv* env,
		jthread thr, jmethodID method, jlocation location, jobject exception,
		jmethodID catch_method, jlocation catch_location) {

	enter_critical_section(jvmti);
	{

	}
	exit_critical_section(jvmti);
}
/** VM Death callback */
static void JNICALL callbackVMDeath(jvmtiEnv *jvmti_env, JNIEnv* jni_env) {
	enter_critical_section(jvmti);
	{

		printf("Got VM Death event\n");

		jvmtiError error;
		jint threadCount;
		jvmtiThreadInfo jvmtiThreadInfo;
		jthread *threads;

		AgentMessage agentMessage;
		error = jvmti->GetAllThreads(&threadCount, &threads);
		Agent::Helper::checkError(jvmti, error, "Cannot get all threads.");

		if (threadCount > 0) {
			for (int i = 0; i < threadCount; ++i) {
				agentMessage = createThreadEventMessage(agentMessage,
						threads[i], AgentMessage::ThreadEvent::ENDED);
			}

			jlong systemTime;
			jvmti->GetTime(&systemTime);
			messageService.write(agentMessage, systemTime, jvmPid);
		}
		exit_critical_section(jvmti);
	}
}

/** Get a name for a jthread */
static void get_thread_name(jvmtiEnv *jvmti, jthread thread, char *tname,
		int maxlen) {
	jvmtiThreadInfo info;
	jvmtiError error;

	/* Make sure the stack variables are garbage free */
	(void) memset(&info, 0, sizeof(info));

	/* Assume the name is unknown for now */
	(void) strcpy(tname, "Unknown");

	/* Get the thread information, which includes the name */
	error = jvmti->GetThreadInfo(thread, &info);
	Agent::Helper::checkError(jvmti, error, "Cannot get thread info");

	/* The thread might not have a name, be careful here. */
	if (info.name != NULL) {
		int len;

		/* Copy the thread name into tname if it will fit */
		len = (int) strlen(info.name);
		if (len < maxlen) {
			(void) strcpy(tname, info.name);
		}

		/* Every string allocated by JVMTI needs to be freed */
		error = jvmti->Deallocate((unsigned char*) info.name);
		if (error != JVMTI_ERROR_NONE) {
			printf("(get_thread_name) Error expected: %d, got: %d\n",
					JVMTI_ERROR_NONE, error);
			describe(error);
			printf("\n");
		}

	}
}

/** VM init callback */
static void JNICALL callbackVMInit(jvmtiEnv *jvmti_env, JNIEnv* jni_env,
		jthread thread) {
	enter_critical_section(jvmti);
	{

		char tname[MAX_THREAD_NAME_LENGTH];
		static jvmtiEvent events[] = { JVMTI_EVENT_THREAD_START,
				JVMTI_EVENT_THREAD_END };
		int i;
		jvmtiFrameInfo frames[5];
		jvmtiError err, err1;
		jvmtiError error;
		jint count;

		/* The VM has started. */
		printf("Got VM init event\n");

		get_thread_name(jvmti_env, thread, tname, sizeof(tname));
		printf("callbackVMInit:  %s thread\n", tname);

		error = jvmti->SetEventNotificationMode(JVMTI_ENABLE,
				JVMTI_EVENT_EXCEPTION, (jthread) NULL);

		error = jvmti->SetEventNotificationMode(JVMTI_ENABLE,
				JVMTI_EVENT_THREAD_START, (jthread) NULL);

		error = jvmti->SetEventNotificationMode(JVMTI_ENABLE,
				JVMTI_EVENT_THREAD_END, (jthread) NULL);

		error = jvmti->SetEventNotificationMode(JVMTI_ENABLE,
				JVMTI_EVENT_MONITOR_CONTENDED_ENTER, (jthread) NULL);

		error = jvmti->SetEventNotificationMode(JVMTI_ENABLE,
				JVMTI_EVENT_MONITOR_CONTENDED_ENTERED, (jthread) NULL);

		error = jvmti->SetEventNotificationMode(JVMTI_ENABLE,
				JVMTI_EVENT_MONITOR_WAIT, (jthread) NULL);

		error = jvmti->SetEventNotificationMode(JVMTI_ENABLE,
				JVMTI_EVENT_MONITOR_WAITED, (jthread) NULL);

		error = jvmti->SetEventNotificationMode(JVMTI_ENABLE,
				JVMTI_EVENT_METHOD_EXIT, (jthread) NULL);

		Agent::Helper::checkError(jvmti_env, error,
				"Cannot set event notification");

		/*
		 * get all available threads and send them via socket
		 */
		jint threadCount;
		jthread *threads;

		AgentMessage agentMessage;
		error = jvmti->GetAllThreads(&threadCount, &threads);
		Agent::Helper::checkError(jvmti, error, "Cannot get all threads.");

		if (threadCount > 0) {
			for (int i = 0; i < threadCount; ++i) {
				agentMessage = createThreadEventMessage(agentMessage,
						threads[i], AgentMessage::ThreadEvent::STARTED);
			}

			jlong systemTime;
			jvmti->GetTime(&systemTime);
			messageService.write(agentMessage, systemTime, jvmPid);
		}
	}
	exit_critical_section(jvmti);
}

/** JVMTI callback function. */
static jvmtiIterationControl JNICALL
reference_object(jvmtiObjectReferenceKind reference_kind, jlong class_tag,
		jlong size, jlong* tag_ptr, jlong referrer_tag, jint referrer_index,
		void *user_data) {

	combined_size = combined_size + size;

	switch (reference_kind) {

	case JVMTI_REFERENCE_CLASS:
		num_class_refs = num_class_refs + 1;
		break;
	case JVMTI_REFERENCE_FIELD:
		num_field_refs = num_field_refs + 1;
		break;
	case JVMTI_REFERENCE_ARRAY_ELEMENT:
		num_array_refs = num_array_refs + 1;
		break;
	case JVMTI_REFERENCE_CLASS_LOADER:
		num_classloader_refs = num_classloader_refs + 1;
		break;
	case JVMTI_REFERENCE_SIGNERS:
		num_signer_refs = num_signer_refs + 1;
		break;
	case JVMTI_REFERENCE_PROTECTION_DOMAIN:
		num_protection_domain_refs = num_protection_domain_refs + 1;
		break;
	case JVMTI_REFERENCE_INTERFACE:
		num_interface_refs = num_interface_refs + 1;
		break;
	case JVMTI_REFERENCE_STATIC_FIELD:
		num_static_field_refs = num_static_field_refs + 1;
		break;
	case JVMTI_REFERENCE_CONSTANT_POOL:
		num_constant_pool_refs = num_constant_pool_refs + 1;
		break;
	default:
		break;
	}

	return JVMTI_ITERATION_CONTINUE;
}

static void JNICALL callbackVMObjectAlloc(jvmtiEnv *jvmti_env, JNIEnv* jni_env,
		jthread thread, jobject object, jclass object_klass, jlong size) {

	char *methodName;
	char *className;
	char *declaringClassName;
	jclass declaring_class;
	jvmtiError err;

	if (size > 50) {
		err = jvmti->GetClassSignature(object_klass, &className, NULL);

		if (className != NULL) {
			//                        printf("\ntype %s object allocated with size %d\n", className, (jint)size);
		}

		//print stack trace
		jvmtiFrameInfo frames[5];
		jint count;
		int i;

		err
				= jvmti->GetStackTrace(NULL, (jint) 0, (jint) 5, &frames[0],
						&count);
		if (err == JVMTI_ERROR_NONE && count >= 1) {

			for (i = 0; i < count; i++) {
				err = jvmti->GetMethodName(frames[i].method, &methodName, NULL,
						NULL);
				if (err == JVMTI_ERROR_NONE) {

					err = jvmti->GetMethodDeclaringClass(frames[i].method,
							&declaring_class);
					err = jvmti->GetClassSignature(declaring_class,
							&declaringClassName, NULL);
					if (err == JVMTI_ERROR_NONE) {
						//                                                printf("at method %s in class %s\n", methodName, declaringClassName);
					}
				}
			}
		}

		//reset counters
		combined_size = 0;
		num_class_refs = 0;
		num_field_refs = 0;
		num_array_refs = 0;
		num_classloader_refs = 0;
		num_signer_refs = 0;
		num_protection_domain_refs = 0;
		num_interface_refs = 0;
		num_static_field_refs = 0;
		num_constant_pool_refs = 0;

		err = jvmti->IterateOverObjectsReachableFromObject(object,
				&reference_object, NULL);
		if (err != JVMTI_ERROR_NONE) {
			printf("Cannot iterate over reachable objects\n");
		}

		//                printf("\nThis object has references to objects of combined size %d\n", (jint)combined_size);
		//                printf("This includes %d classes, %d fields, %d arrays, %d classloaders, %d signers arrays,\n", num_class_refs, num_field_refs, num_array_refs, num_classloader_refs, num_signer_refs);
		//                printf("%d protection domains, %d interfaces, %d static fields, and %d constant pools.\n\n", num_protection_domain_refs, num_interface_refs, num_static_field_refs, num_constant_pool_refs);

		err = jvmti->Deallocate((unsigned char*) className);
		err = jvmti->Deallocate((unsigned char*) methodName);
		err = jvmti->Deallocate((unsigned char*) declaringClassName);
	}
}

/*
 * Invoked when loading the library
 */
JNIEXPORT
jint JNICALL Agent_OnLoad(JavaVM *jvm, char *options, void *reserved) {

	static GlobalAgentData data;
	jvmtiError error;
	jint res;
	jvmtiEventCallbacks callbacks;

	jvmPid = getpid();

	/* Setup initial global agent data area
	 * Use of static/extern data should be handled carefully here.
	 * We need to make sure that we are able to cleanup after ourselves
	 * so anything allocated in this library needs to be freed in
	 * the Agent_OnUnload() function.
	 */
	(void) memset((void*) &data, 0, sizeof(data));
	gdata = &data;

	/*  get the jvmtiEnv* or JVMTI environment */
	res = jvm->GetEnv((void **) &jvmti, JVMTI_VERSION_1_0);

	if (res != JNI_OK || jvmti == NULL) {
		/* This means that the VM was unable to obtain this version of the
		 *   JVMTI interface, this is a fatal error.
		 */
		printf("ERROR: Unable to access JVMTI Version 1 (0x%x),"
			" is your J2SE a 1.5 or newer version?"
			" JNIEnv's GetEnv() returned %d\n", JVMTI_VERSION_1, res);

	}

	/* Here we save the jvmtiEnv* for Agent_OnUnload(). */
	gdata->jvmti = jvmti;

	/* set the needed capabukutues needed for profiling */
	(void) memset(&capa, 0, sizeof(jvmtiCapabilities));
	capa.can_signal_thread = 1;
	capa.can_get_owned_monitor_info = 1;
	capa.can_get_thread_cpu_time = 1;
	capa.can_generate_method_entry_events = 1;
	capa.can_generate_exception_events = 1;
	capa.can_generate_vm_object_alloc_events = 1;
	capa.can_tag_objects = 1;
	capa.can_generate_monitor_events = 1;
	capa.can_generate_method_entry_events = 1;
	capa.can_generate_method_exit_events = 1;
	capa.can_get_monitor_info = 1;
	capa.can_maintain_original_method_order = 1;
	capa.can_get_source_file_name = 1;

	error = jvmti->AddCapabilities(&capa);
	Agent::Helper::checkError(jvmti, error,
			"Unable to get necessary JVMTI capabilities.");

	/* register callback functions */
	(void) memset(&callbacks, 0, sizeof(callbacks));
	callbacks.VMInit = &callbackVMInit; /* JVMTI_EVENT_VM_INIT */
	callbacks.VMDeath = &callbackVMDeath; /* JVMTI_EVENT_VM_DEATH */
	callbacks.Exception = &callbackException;/* JVMTI_EVENT_EXCEPTION */
	callbacks.VMObjectAlloc = &callbackVMObjectAlloc;/* JVMTI_EVENT_VM_OBJECT_ALLOC */
	callbacks.ThreadStart = &callbackThreadStart;/* JVMTI_EVENT_THREAD_START */
	callbacks.ThreadEnd = &callbackThreadEnd;/* JVMTI_EVENT_THREAD_END */
	callbacks.MonitorContendedEnter = &callbackMonitorContendedEnter;/* JVMTI_EVENT_MONITOR_CONTENDED_ENTER*/
	callbacks.MonitorContendedEntered = &callbackMonitorContendedEntered;/* JVMTI_EVENT_MONITOR_CONTENDED_ENTERED*/
	callbacks.MonitorWait = &callbackMonitorWait;/* JVMTI_EVENT_MONITOR_WAIT*/
	callbacks.MonitorWaited = &callbackMonitorWaited;/* JVMTI_EVENT_MONITOR_WAIT*/
	callbacks.MethodEntry = &callbackMethodEntry;/* JVMTI_EVENT_METHOD_ENTRY */
	callbacks.MethodExit = &callbackMethodExit;/* JVMTI_EVENT_METHOD_EXIT */

	error = jvmti->SetEventCallbacks(&callbacks, (jint) sizeof(callbacks));
	Agent::Helper::checkError(jvmti, error, "Cannot set jvmti callbacks");

	/*
	 * Activate the notification of certain events. At first only initial events are activated.
	 * More events are requested on demand.
	 *
	 * The NULL argument implies global notifying, a thread can be passed for local notifying.
	 */
	error = jvmti->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_VM_INIT,
			(jthread) NULL);
	error = jvmti->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_VM_DEATH,
			(jthread) NULL);
	error = jvmti->SetEventNotificationMode(JVMTI_ENABLE,
			JVMTI_EVENT_VM_OBJECT_ALLOC, (jthread) NULL);
	error = jvmti->SetEventNotificationMode(JVMTI_ENABLE,
			JVMTI_EVENT_METHOD_ENTRY, (jthread) NULL);
	error = jvmti->SetEventNotificationMode(JVMTI_ENABLE,
			JVMTI_EVENT_METHOD_EXIT, (jthread) NULL);

	Agent::Helper::checkError(jvmti, error, "Cannot set event notification");

	/* Here we create a raw monitor for our use in this agent to
	 *   protect critical sections of code.
	 */
	error = jvmti->CreateRawMonitor("agent data", &(gdata->lock));
	Agent::Helper::checkError(jvmti, error, "Cannot create raw monitor");

	/* Return JNI_OK to signify success */
	return JNI_OK;
}

/* Agent_OnUnload: This is called immediately before the shared library is
 *   unloaded. This is the last code executed.
 */
JNIEXPORT void JNICALL
Agent_OnUnload(JavaVM *vm) {
	/* Make sure all malloc/calloc/strdup space is freed */

}
