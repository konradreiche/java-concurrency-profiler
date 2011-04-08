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
	void send(std::string message);

private:
	std::string ip;
	std::string port;
};

#endif /* CLIENT_H_ */
