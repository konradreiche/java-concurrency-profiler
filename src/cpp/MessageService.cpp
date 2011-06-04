/*
 * MessageService.cpp
 *
 *  Created on: 04.06.2011
 *      Author: konrad
 */

#include "MessageService.h"

using namespace google::protobuf::io;
using boost::asio::ip::tcp;

MessageService::MessageService(std::string ip, std::string port) :
	work(io_service), resolver(io_service), socket(io_service) {

	messageQueue = new std::deque<AgentMessage>;
	tcp::resolver::query query(ip, port);
	endpoint_iterator = resolver.resolve(query);

	tcp::endpoint endpoint = *endpoint_iterator;

	socket.async_connect(endpoint, boost::bind(&MessageService::handle_connect,
			this, boost::asio::placeholders::error, ++endpoint_iterator));

	boost::thread t(boost::bind(&boost::asio::io_service::run, &io_service));
}

void MessageService::write(AgentMessage agentMessage, int JVM_ID) {
	agentMessage.set_timestamp(Agent::Helper::getCurrentClockCycle());
	agentMessage.set_jvm_id(JVM_ID);
	io_service.post(boost::bind(&MessageService::do_write, this, agentMessage));
}

void MessageService::do_close() {
	socket.close();
}

void MessageService::transmitMessage(AgentMessage agentMessage) {

	boost::asio::streambuf b;
	std::ostream os(&b);

	ZeroCopyOutputStream *raw_output = new OstreamOutputStream(&os);
	CodedOutputStream *coded_output = new CodedOutputStream(raw_output);

	coded_output->WriteVarint32(agentMessage.ByteSize());
	agentMessage.SerializeToCodedStream(coded_output);

	delete coded_output;
	delete raw_output;

	boost::system::error_code ignored_error;

	boost::asio::async_write(socket, b.data(), boost::bind(
			&MessageService::handle_write, this,
			boost::asio::placeholders::error));
}

void MessageService::do_write(AgentMessage agentMessage) {

	bool write_in_progress = !messageQueue->empty();
	messageQueue->push_back(agentMessage);

	if (!write_in_progress) {
		transmitMessage(agentMessage);
	}
}

void MessageService::handle_write(const boost::system::error_code &error) {

	if (!error) {
		messageQueue->pop_front();
		if (!messageQueue->empty()) {
			transmitMessage(messageQueue->front());
		}
	} else {
		do_close();
	}
}

void MessageService::handle_connect(const boost::system::error_code &error,
		tcp::resolver::iterator endpoint_iterator) {
	// can be used to receive commands from the Java profiler interface
}



MessageService::~MessageService() {
	// TODO Auto-generated destructor stub
}
