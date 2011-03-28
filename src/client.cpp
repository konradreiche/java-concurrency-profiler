/*
 * client.cpp
 *
 *  Created on: 26.03.2011
 *      Author: Konrad Johannes Reiche
 */

#include "client.h"

using boost::asio::ip::tcp;

client::client() {
	// TODO Auto-generated constructor stub

}

client::~client() {
	// TODO Auto-generated destructor stub
}

void client::send(std::string message) {

	try {
		boost::asio::io_service io_service;
		tcp::resolver resolver(io_service);
		tcp::resolver::query query("localhost", "50000");

		tcp::resolver::iterator endpoint_iterator = resolver.resolve(query);
		tcp::resolver::iterator end;

		tcp::socket socket(io_service);
		boost::system::error_code error = boost::asio::error::host_not_found;
		while (error && endpoint_iterator != end)
		{
			socket.close();
			socket.connect(*endpoint_iterator++, error);
		}

		if (error)
		{
			throw boost::system::system_error(error);
		}

		boost::system::error_code ignored_error;
		boost::asio::write(socket, boost::asio::buffer(message),
				boost::asio::transfer_all(), ignored_error);

		socket.close();

	} catch (std::exception& e) {
		std::cout << e.what() << std::endl;
	}

}
