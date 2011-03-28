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

class client {
public:
	client();
	virtual ~client();
	void send(std::string message);
};

#endif /* CLIENT_H_ */
