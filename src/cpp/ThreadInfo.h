/*
 * threadinfo.h
 *
 *  Created on: 01.04.2011
 *      Author: Konrad Johannes Reiche
 */

#ifndef THREADINFO_H_
#define THREADINFO_H_

#include <iostream>
#include <stdlib.h>
#include <stdio.h>
#include <sstream>

#include <string>
#include <vector>

#include <boost/lexical_cast.hpp>

#include "tinyxml.h"

using namespace std;

class ThreadInfo {
public:
	ThreadInfo(string name, int priority, bool isContextClassLoaderSet) :
		name(name), priority(priority), isContextClassLoaderSet(
				isContextClassLoaderSet) {
	}

	virtual ~ThreadInfo();

	static string getXML(int JVM_ID, ThreadInfo threadInfo, bool lifeCycle);
	static string getXML(int JVM_ID, vector<ThreadInfo> allThreadInfos, bool lifeCycle);

	string name;
	int priority;
	bool isContextClassLoaderSet;
};

#endif /* THREADINFO_H_ */
