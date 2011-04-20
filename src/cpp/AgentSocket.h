/*
 * client.h
 *
 *  Created on: 26.03.2011
 *      Author: Konrad Johannes Reiche
 */

#ifndef CLIENT_H_
#define CLIENT_H_

#include <string>
#include <iostream>
#include <boost/array.hpp>
#include <boost/asio.hpp>

class AgentSocket {
public:
	AgentSocket(std::string ip, std::string port);
	virtual ~AgentSocket();
	/**
	 * Sends the streambuf data to a specified socket and closed it.
	 *
	 * @param b
	 */
	void send(boost::asio::streambuf &b);

private:
	std::string ip;
	std::string port;
};

#endif /* CLIENT_H_ */
