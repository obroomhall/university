#pragma once

#include <jengar/network.hpp>
#include <jengar/client/camera.hpp>

#include <readerwriterqueue/readerwriterqueue.h>
#include <opencv2/opencv.hpp>
#include <opencv2/aruco.hpp>

class Client : public Network
{
private:
	std::string address;
	int port;
	moodycamel::BlockingReaderWriterQueue<cv::Mat> imageQueue;
	bool endOfData = false;;
	
public:
	Client(std::string address, int port = 8080);
	int getPort() { return port; }
	std::string getAddress() { return address; }

	void setEndOfData() { endOfData = true; };
	bool getMapCompleteness();
	bool queueImage(cv::Mat &img);
	void sendQueuedImages();
	bool sendCameraToServer(Camera &camera);
	bool getDictionaryFromServer(std::string name, int nMarkers, cv::Ptr<cv::aruco::Dictionary> &dictionary);
	bool getCustomDictionaryNamesFromSever(std::vector<std::string> &names);
};