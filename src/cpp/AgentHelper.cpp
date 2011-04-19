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

	time_t ltime;
	ltime = time(NULL);
	agentMessage.set_timestamp(asctime(localtime(&ltime)));
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
