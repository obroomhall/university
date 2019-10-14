#pragma once

#include <opencv2/opencv.hpp>
#include <spdlog/spdlog.h>

#include <unistd.h>
#include <stdio.h>
#include <sys/socket.h>
#include <stdlib.h>
#include <netinet/in.h>
#include <string.h>
#include <assert.h>
#include <typeinfo>
#include <string>
#include <stdio.h>
#include <iostream>

#define LOG_DATA "Data"

class Network
{
protected:
	int socket;
	~Network()
	{
		shutdown(socket, SHUT_RDWR);
		close(socket);
	}

	template <typename T>
	void receive(T *buf, int length, int retries = 120)
	{
		int bytes = 0;
		int backoff = 10;
		for (int i = 0; i < length; i += bytes)
		{
			bytes = ::read(socket, buf + i, length - i);

			if (bytes < 0)
			{
				throw SocketException(false, typeid(buf).name(), bytes, length);
			}
			else if (bytes == 0)
			{
				if (retries == 0)
				{
					throw NoBytesException();
				}
				else if (retries > 0)
				{
					retries--;
					if (backoff < 10000) backoff*=2;
					usleep(backoff);
				}
			}
		}
	}

	template <typename T>
	void send(T *buf, int length)
	{
		int bytes = 0;
		for (int i = 0; i < length; i += bytes)
			if ((bytes = ::send(socket, buf + i, length - i, 0)) < 0)
				throw SocketException(true, typeid(buf).name(), bytes, length);
	}

public:
	enum DataTypes
	{
		REQUEST_DICTNAMES = 0,
		REQUEST_DICTIONARY,
		SERVE_MAT,
		SERVE_CAMERA,
		END_OF_DATA,
		RETURN_TO_SETTINGS,
		GOOD_REQUEST,
		BAD_REQUEST,
		MAP_COMPLETE,
		PING
	};

	bool ping()
	{
		sendTypeOfData(PING, false);
		bool pong;
		try
		{
			receive(&pong, sizeof(bool), 0);
		}
		catch (...)
		{
			pong = false;
		}
		return pong;
	}

	void pong()
	{
		bool pong = true;
		send(&pong, sizeof(bool));
	}

	int getTypeOfData()
	{
		int type;
		spdlog::info("[{0}] [Receiving] Type", LOG_DATA);
		
		while (true)
		{
			receive(&type, sizeof(type));

			if (type != PING)
			{
				spdlog::info("[{0}] [Received] {1}", LOG_DATA, getStringForDataType(type));
				return type;
			}
			else
			{
				pong();
			}
		}
	}

	bool sendTypeOfData(int type, bool confirm = true)
	{
		spdlog::info("[{0}] [Sending] {1}", LOG_DATA, getStringForDataType(type));
		send(&type, sizeof(type));
		spdlog::info("[{0}] [Sent] {1}", LOG_DATA, getStringForDataType(type));

		if (confirm)
		{
			int requestType = getTypeOfData();
			if (requestType == GOOD_REQUEST)
			{
				return true;
			}
			else if (requestType == BAD_REQUEST)
			{
				auto types = getValidRequestTypes();
				return false;
			}
			else
			{
				throw std::runtime_error("Expected either GOOD_REQUEST or BAD_REQUEST, but got " + std::to_string(requestType));
			}
		}
		return false;
	}

	std::vector<int> getValidRequestTypes()
	{
		int nTypes;
		receive(&nTypes, sizeof(int));

		std::vector<int> validRequests;
		receive(&validRequests, sizeof(int) * nTypes);

		return validRequests;
	}

	void sendValidRequestTypes(std::vector<int> types)
	{
		int nTypes = types.size();
		send(&nTypes, sizeof(int));

		send(types.data(), sizeof(int) * nTypes);
	}

	std::string getStringForDataType(int type)
	{
		switch (type)
		{
		case SERVE_MAT:
			return "Mat";

		case SERVE_CAMERA:
			return "Camera";

		case REQUEST_DICTNAMES:
			return "Custom dictionary names request";

		case REQUEST_DICTIONARY:
			return "Dictionary request";

		case END_OF_DATA:
			return "End of data";

		case RETURN_TO_SETTINGS:
			return "Return to setttings";

		case GOOD_REQUEST:
			return "Good request";

		case BAD_REQUEST:
			return "Request was unknown to server";

		case MAP_COMPLETE:
			return "Map completeness";

		case PING:
			return "Ping";

		default:
			return "Unknown";
		}
	}

	std::string getString()
	{
		// Get length, ensuring host system byte order
		uint32_t length;
		receive(&length, sizeof(uint32_t));
		length = ntohl(length);

		// Receive string into buffer
		std::vector<uint8_t> buf;
		buf.resize(length, 0x00);
		receive(&buf[0], length);

		// Return constructed string
		return std::string(buf.begin(), buf.end());
	}

	void sendString(std::string str)
	{
		// Send length of string in network byte order
		uint32_t length = htonl(str.size());
		send(&length, sizeof(uint32_t));

		// Send name of dictionary
		send(str.c_str(), str.size());
	}

	cv::Mat getMat()
	{
		int matDetails[4];
		receive(&matDetails[0], sizeof(int) * 4);

		for (int i = 0; i < 4; i++)
			std::cout << matDetails[i] << " ";
		std::cout << std::endl;

		uchar sockData[matDetails[3]];
		receive(&sockData[0], matDetails[3]);

		cv::Mat M = cv::Mat::zeros(matDetails[0], matDetails[1], matDetails[2]);

		int channels = M.channels();
		int ptr = 0;
		for (int i = 0; i < M.rows; i++)
		{
			for (int j = 0; j < M.cols; j++)
			{
				if (channels == 1)
					M.at<uchar>(i, j) = sockData[ptr];
				else if (channels == 2)
					M.at<cv::Vec2b>(i, j) = cv::Vec2b(sockData[ptr + 0], sockData[ptr + 1]);
				else if (channels == 3)
					M.at<cv::Vec3b>(i, j) = cv::Vec3b(sockData[ptr + 0], sockData[ptr + 1], sockData[ptr + 2]);
				else if (channels == 4)
					M.at<cv::Vec4b>(i, j) = cv::Vec4b(sockData[ptr + 0], sockData[ptr + 1], sockData[ptr + 2], sockData[ptr + 3]);

				ptr += channels;
			}
		}

		return M;
	}

	void sendMat(cv::Mat frame)
	{
		int imgSize = frame.total() * frame.elemSize();
		int matDetails[] = {frame.size().height, frame.size().width, frame.type(), imgSize};
		for (int i = 0; i < 4; i++)
			std::cout << matDetails[i] << " ";
		std::cout << std::endl;
		send(&matDetails[0], sizeof(int) * 4);
		send(frame.data, imgSize);
	}

	class NoBytesException : public std::exception
	{
	};

	class SocketException : public std::exception
	{
		std::string m_msg;

	public:
		SocketException(const bool &wasSending, const std::string &type, const int &bytes, const int &length)
			: m_msg(get_str(wasSending, type, bytes, length))
		{
		}

		virtual const char *what() const throw()
		{
			return m_msg.c_str();
		}

	private:
		std::string get_str(bool wasSending, std::string type, int bytes, int length)
		{
			std::string bytesStr = std::to_string(bytes);
			std::string lengthStr = std::to_string(length);

			std::string transfer;
			if (wasSending)
				transfer = "send";
			else
				transfer = "receive";

			std::string message = "Failed to " + transfer + " all bytes of message. " + bytesStr + "/" + lengthStr + " of " + type + ".";

			return message;
		}
	};
};

//Functions which send matrices by encode/decode
cv::Mat getMatWithDecode()
{
	// Get size of image from client
	unsigned long imgSize;
	receive(&imgSize, sizeof(unsigned long));

	// Get image from client
	std::vector<uchar> imgvec;
	imgvec.resize(imgSize);
	receive(&imgvec[0], imgSize);

	return cv::imdecode(imgvec, cv::IMREAD_UNCHANGED);
}

void sendMatWithEncode(cv::Mat img, int scaleTo = 0, const char *ext = ".png")
{
	// Resize image to scale
	if (scaleTo > 0)
	{
		double scale = float(scaleTo) / img.size().width;
		cv::resize(img, img, cv::Size(), scale, scale);
	}

	// Encode image into a buffer
	std::vector<uchar> buf;
	cv::imencode(ext, img, buf);

	// Send length of encoded image buffer
	unsigned long bufsize = buf.size();
	send(&bufsize, sizeof(unsigned long));

	// Send encoded image buffer
	send(buf.data(), buf.size());
}