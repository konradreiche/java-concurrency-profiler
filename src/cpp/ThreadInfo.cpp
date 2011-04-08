/*
 * threadinfo.cpp
 *
 *  Created on: 01.04.2011
 *      Author: Konrad Johannes Reiche
 */

#include "ThreadInfo.h"

ThreadInfo::~ThreadInfo() {

}

string ThreadInfo::getXML(int JVM_ID, ThreadInfo threadInfo, bool lifeCycle) {

	TiXmlDocument document;
	TiXmlDeclaration *declaration = new TiXmlDeclaration("1.0", "", "");

	std::string jvmString = boost::lexical_cast<string>(JVM_ID);
	TiXmlElement *messageElement = new TiXmlElement("message");
	messageElement->SetAttribute("jvm",jvmString.c_str());

	TiXmlElement *threadsElement = new TiXmlElement("threads");
	string lifeCycleString = lifeCycle ? "start" : "end";

	threadsElement->SetAttribute("lifeCycle", lifeCycleString.c_str());
	messageElement->LinkEndChild(threadsElement);

	string numberString;
	TiXmlElement *threadElement = new TiXmlElement("thread");
	threadsElement->LinkEndChild(threadElement);

	TiXmlElement *threadNameElement = new TiXmlElement("name");
	threadElement->LinkEndChild(threadNameElement);

	TiXmlText *threadName = new TiXmlText(threadInfo.name.c_str());
	threadNameElement->LinkEndChild(threadName);

	TiXmlElement *threadPriorityElement = new TiXmlElement("priority");
	threadElement->LinkEndChild(threadPriorityElement);

	numberString = boost::lexical_cast<string>(threadInfo.priority);
	TiXmlText *threadPriority = new TiXmlText(numberString.c_str());
	threadPriorityElement->LinkEndChild(threadPriority);

	TiXmlElement *threadIsContextClassLoaderSetElement = new TiXmlElement(
			"isContextClassLoaderSet");
	threadElement->LinkEndChild(threadIsContextClassLoaderSetElement);

	string isContextClassLoaderSetString =
			(threadInfo.isContextClassLoaderSet ? "true" : "false");
	TiXmlText *ccl = new TiXmlText(isContextClassLoaderSetString.c_str());
	threadIsContextClassLoaderSetElement->LinkEndChild(ccl);

	document.LinkEndChild(declaration);
	document.LinkEndChild(messageElement);

	TiXmlPrinter printer;
	document.Accept(&printer);

	string result = printer.CStr();

	return result;
}

string ThreadInfo::getXML(int JVM_ID, vector<ThreadInfo> allThreadInfos, bool lifeCycle) {

	TiXmlDocument document;
	TiXmlDeclaration *declaration = new TiXmlDeclaration("1.0", "", "");

	std::string jvmString = boost::lexical_cast<string>(JVM_ID);
	TiXmlElement *messageElement = new TiXmlElement("message");
	TiXmlElement *threadsElement = new TiXmlElement("threads");
	messageElement->SetAttribute("jvm",jvmString.c_str());

	string lifeCycleString = lifeCycle ? "start" : "end";
	threadsElement->SetAttribute("lifeCycle", lifeCycleString.c_str());
	messageElement->LinkEndChild(threadsElement);

	string numberString;
	for (vector<ThreadInfo>::const_iterator it = allThreadInfos.begin(); it
			!= allThreadInfos.end(); ++it) {
		TiXmlElement *currentThreadElement = new TiXmlElement("thread");
		threadsElement->LinkEndChild(currentThreadElement);

		TiXmlElement *threadNameElement = new TiXmlElement("name");
		currentThreadElement->LinkEndChild(threadNameElement);

		TiXmlText *threadName = new TiXmlText((*it).name.c_str());
		threadNameElement->LinkEndChild(threadName);

		TiXmlElement *threadPriorityElement = new TiXmlElement("priority");
		currentThreadElement->LinkEndChild(threadPriorityElement);

		numberString = boost::lexical_cast<string>((*it).priority);
		TiXmlText *threadPriority = new TiXmlText(numberString.c_str());
		threadPriorityElement->LinkEndChild(threadPriority);

		TiXmlElement *threadIsContextClassLoaderSetElement = new TiXmlElement(
				"isContextClassLoaderSet");
		currentThreadElement->LinkEndChild(threadIsContextClassLoaderSetElement);

		string isContextClassLoaderSetString =
				((*it).isContextClassLoaderSet ? "true" : "false");

		TiXmlText *ccl = new TiXmlText(isContextClassLoaderSetString.c_str());
		threadIsContextClassLoaderSetElement->LinkEndChild(ccl);
	}

	document.LinkEndChild(declaration);
	document.LinkEndChild(messageElement);

	TiXmlPrinter printer;
	document.Accept(&printer);

	string result = printer.CStr();

	return result;
}
