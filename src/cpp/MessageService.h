/*
 * MessageService.h
 *
 *  Created on: 04.06.2011
 *      Author: konrad
 */

#include <deque>
#include <signal.h>
#include <boost/asio.hpp>
#include <boost/asio/ip/tcp.hpp>
#include <boost/bind.hpp>
#include "AgentMessage.pb.h"
#include "AgentHelper.h"

#include <google/protobuf/io/coded_stream.h>
#include <google/protobuf/io/zero_copy_stream_impl.h>

#include <boost/asio.hpp>
#include <boost/thread.hpp>

#ifndef MESSAGESERVICE_H_
#define MESSAGESERVICE_H_

using boost::asio::ip::tcp;

class MessageService {
public:
	MessageService(std::string ip, std::string port);
	virtual ~MessageService();
	void write(AgentMessage agentMessage, long systemTime, int JVM_ID);
	void await();

private:
	boost::asio::io_service io_service;
	boost::asio::io_service::work work;
	tcp::resolver resolver;
	tcp::resolver::iterator endpoint_iterator;
	tcp::socket socket;
	std::deque<AgentMessage> *messageQueue;

	void do_write(AgentMessage agentMessage);

	void do_close();

	void handle_write(const boost::system::error_code &error);

	void handle_connect(const boost::system::error_code &error,
			tcp::resolver::iterator endpoint_iterator);

	void transmitMessage(AgentMessage agentMessage);
};

#endif /* MESSAGESERVICE_H_ */
