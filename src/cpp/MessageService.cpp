/*
 * MessageService.cpp
 *
 *  Created on: 04.06.2011
 *      Author: Konrad Johannes Reiche
 */

#include "MessageService.h"

using namespace google::protobuf::io;
using boost::asio::ip::tcp;

MessageService::MessageService(std::string ip, std::string port) :
	work(io_service), resolver(io_service), socket(io_service) {

	messageQueue = new std::deque<AgentMessage>;
	tcp::resolver::query query(ip, port);
	endpoint_iterator = resolver.resolve(query);

	boost::system::error_code error = boost::asio::error::host_not_found;
	socket.connect(*endpoint_iterator++,error);

	//boost::thread t(boost::bind(&boost::asio::io_service::run, &io_service));
}

void MessageService::await() {

	while (!messageQueue->empty()) {

		signal(SIGINT, exit);

		int messagesLeft = messageQueue->size();
		sleep(3);
		std::cout << "Pending Profiler Agents Messages: "
				<< messageQueue->size() << std::endl;
		if (messagesLeft == messageQueue->size()) {
			std::cout << "Connection Error" << std::endl;
			break;
		}
	}
}

void MessageService::write(AgentMessage agentMessage, long systemTime,
		int JVM_ID) {
	agentMessage.set_timestamp(Agent::Helper::getCurrentClockCycle());
	agentMessage.set_jvm_id(JVM_ID);
	agentMessage.set_systemtime(systemTime);
	transmitMessage(agentMessage);
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

	boost::asio::write(socket, b.data(),boost::asio::transfer_all(), ignored_error);
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
		std::cout << error << std::endl;
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
