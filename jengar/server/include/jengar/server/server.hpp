#pragma once

#include <jengar/network.hpp>

#include <opencv2/opencv.hpp>
#include <opencv2/aruco.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <ucoslam/imageparams.h>
#include <ucoslam/ucoslam.h>
#include <aruco/aruco.h>
#include <readerwriterqueue/readerwriterqueue.h>

class Server : public Network
{
private:
	ucoslam::UcoSlam SLAM; //The main class
	ucoslam::ImageParams cameraParams;
	ucoslam::Params UcoSlamParams; //processing parameters
	int blocks;
	bool isMapComplete = false;
	bool endOfData = false;
	moodycamel::BlockingReaderWriterQueue<cv::Mat> imageQueue;

	aruco::CameraParameters Camera;

public:
	Server(int port);
	void getSettings();
	int getBlocks() { return blocks; }
	void slam();
	void markermapper();
	void mymapper();
	void sendMapCompleteness();
	void getImages();
	void sendDictionaryToClient();
	void getCameraFromClient();
	void sendCustomDictionaryNamesToClient();
};