// Client side C/C++ program to demonstrate Socket programming
// https://www.geeksforgeeks.org/socket-programming-cc/
#include <jengar/client/client.hpp>
#include <jengar/network.hpp>
#include <jengar/client/camera.hpp>

#include <opencv2/opencv.hpp>

#include <stdio.h>
#include <sys/socket.h>
#include <stdlib.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <assert.h>
#include <thread>

Client::Client(std::string address, int port) : address(address), port(port)
{
	assert(!address.empty() && port > 0);

	// Create socket
	if ((socket = ::socket(AF_INET, SOCK_STREAM, 0)) < 0)
		throw std::runtime_error("Socket creation error");

	// Create socket address
	struct sockaddr_in serv_addr;
	memset(&serv_addr, '0', sizeof(serv_addr));
	serv_addr.sin_family = AF_INET;
	serv_addr.sin_port = htons(port);

	// Convert IPv4 and IPv6 addresses from text to binary form
	if (inet_pton(AF_INET, address.c_str(), &serv_addr.sin_addr) <= 0)
		throw std::runtime_error("Invalid address/ Address not supported");

	// Open socket for connection
	if (connect(socket, (struct sockaddr *)&serv_addr, sizeof(serv_addr)) < 0)
		throw std::runtime_error("Failed to connect to server, " + address + ":" + std::to_string(port));
	else
		spdlog::info("Connected to server");
	
}

bool Client::queueImage(cv::Mat &img)
{
	spdlog::debug("[Data] [Queuing] Image");
	return imageQueue.try_enqueue(img);
}

void Client::sendQueuedImages()
{
	cv::Mat img;
	while (!endOfData)
	{
		if (imageQueue.wait_dequeue_timed(img, 1000))
			if (sendTypeOfData(SERVE_MAT))
				sendMat(img);
	}
}

bool Client::getMapCompleteness()
{
	bool complete;
	if (sendTypeOfData(MAP_COMPLETE))
	{
		receive(&complete, sizeof(bool));
	}
	return complete;
}

bool Client::getDictionaryFromServer(std::string name, int markers, cv::Ptr<cv::aruco::Dictionary> &dictionary)
{
	if (sendTypeOfData(REQUEST_DICTIONARY))
	{
		// Send dictionary details
		sendString(name);
		send(&markers, sizeof(markers));

		// Get dictionary content
		int markerSize;
		receive(&markerSize, sizeof(int));
		cv::Mat bytesList = getMat();

		dictionary = cv::makePtr<cv::aruco::Dictionary>(bytesList, markerSize);

		spdlog::info("[{0}] [Received] Dictionary", LOG_DATA);
		return true;
	}
	else
	{
		return false;
	}
}

bool Client::sendCameraToServer(Camera &camera)
{
	if (sendTypeOfData(SERVE_CAMERA))
	{
		int cameraDims[2] = {camera.getWidth(), camera.getHeight()};
		send(&cameraDims[0], sizeof(int) * 2);
		sendMat(camera.getCameraMatrix());
		sendMat(camera.getDistCoeffs());
		spdlog::info("[{0}] [Sent] Camera", LOG_DATA);
		return true;
	}
	else
	{
		return false;
	}
}

bool Client::getCustomDictionaryNamesFromSever(std::vector<std::string> &names)
{
	if (sendTypeOfData(REQUEST_DICTNAMES))
	{
		size_t nameCount;
		receive(&nameCount, sizeof(size_t));

		for (size_t i = 0; i < nameCount; i++)
			names.push_back(getString());

		spdlog::info("[{0}] [Received] Custom dictionaries", LOG_DATA);
		return true;
	}
	else
	{
		return false;
	}
}