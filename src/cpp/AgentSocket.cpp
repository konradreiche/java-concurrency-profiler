/*
 * client.cpp
 *
 *  Created on: 26.03.2011
 *      Author: Konrad Johannes Reiche
 */

#include "AgentSocket.h"

using boost::asio::ip::tcp;
using namespace std;

AgentSocket::AgentSocket(string ip, string port) :
	ip(ip), port(port) {

}

AgentSocket::~AgentSocket() {

}

void AgentSocket::send(string message) {

	try {
		boost::asio::io_service io_service;
		tcp::resolver resolver(io_service);
		tcp::resolver::query query(ip, port);

		tcp::resolver::iterator endpoint_iterator = resolver.resolve(query);
		tcp::resolver::iterator end;

		tcp::socket socket(io_service);
		boost::system::error_code error = boost::asio::error::host_not_found;
		while (error && endpoint_iterator != end) {
			socket.close();
			socket.connect(*endpoint_iterator++, error);
		}

		if (error) {
			throw boost::system::system_error(error);
		}

		boost::system::error_code ignored_error;
		boost::asio::write(socket, boost::asio::buffer(message),
				boost::asio::transfer_all(), ignored_error);

		socket.close();

	} catch (exception& e) {
		cout << e.what() << endl;
	}
}
