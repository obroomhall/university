#include <jengar/client/settings.hpp>
#include <jengar/client/system.hpp>
#include <jengar/client/client.hpp>

#include <opencv2/opencv.hpp>
#include <opencv2/aruco.hpp>
#include <spdlog/spdlog.h>

#include <set>
#include <iostream>
#include <algorithm>
#include <string>
#include <vector>
#include <sstream>
#include <iterator>

void sendFromVideo(std::string videoPath, Client &client, Camera &camera, cv::Ptr<cv::aruco::Dictionary> dictionary, cv::Ptr<cv::aruco::DetectorParameters> detectorParameters)
{
	spdlog::debug("Sending video frames to server");
	auto vidcap = cv::VideoCapture(videoPath);
	cv::Mat frame;
	auto cameraMatrix = camera.getCameraMatrix();
	auto distCoeffs = camera.getDistCoeffs();
	while (vidcap.read(frame))
	{
		std::vector<int> ids;
		std::vector<std::vector<cv::Point2f>> corners, rejected;
		cv::aruco::detectMarkers(frame, dictionary, corners, ids, detectorParameters, rejected, cameraMatrix, distCoeffs);

		if (ids.size() > 1)
		{
			bool success = client.queueImage(frame);
		}
		else
		{
			spdlog::warn("[Detection] No markers detected");
		}
		
	}
}

void sendFromImages(cv::Ptr<std::vector<cv::Mat>> images, int minDetected, Client &client, Camera &camera, cv::Ptr<cv::aruco::Dictionary> dictionary, cv::Ptr<cv::aruco::DetectorParameters> detectorParameters)
{
	spdlog::debug("Sending worthy images to server");
	cv::Mat frame;
	auto cameraMatrix = camera.getCameraMatrix();
	auto distCoeffs = camera.getDistCoeffs();
	for (size_t i = 0; i < images->size(); i++)
	{
		frame = images->at(i);
		std::vector<int> ids;
		std::vector<std::vector<cv::Point2f>> corners, rejected;
		cv::aruco::detectMarkers(frame, dictionary, corners, ids, detectorParameters, rejected, cameraMatrix, distCoeffs);

		if (ids.size() >= minDetected)
		{
			bool success = client.queueImage(frame);
		}
		else
		{
			spdlog::warn("[Detection] No markers detected");
		}
	}
}

int main(int argc, char **argv)
{
	spdlog::set_level(spdlog::level::debug);
	std::string datasetDir = "client/data/";

	spdlog::debug("Connecting to server");
	Client client("127.0.0.1", 8080);

	spdlog::debug("Getting dictionary names from server");
	std::vector<std::string> names;
	client.getCustomDictionaryNamesFromSever(names);

	const char* const delim = ", ";
	std::ostringstream imploded;
	std::copy(names.begin(), names.end(), std::ostream_iterator<std::string>(imploded, delim));
	spdlog::debug(imploded.str());

	spdlog::debug("Getting dictionary from server");
	cv::Ptr<cv::aruco::Dictionary> dictionary;
	client.getDictionaryFromServer("ARUCO_MIP_36h12", 12, dictionary);

	spdlog::debug("Creating camera");
	Camera camera = Camera();
	if (!camera.isCalibrated())
	{
		camera.setCharucoBoard(cv::aruco::CharucoBoard::create(4, 6, 2, 1, dictionary));
		auto images = readImagesFromDir(datasetDir + "calibration/5/", "^IMG.+\\.jpg$");

		int i = 0;
		while (!camera.calibrate())
		{
			camera.calibrationAddImage(images->at(i++));
		}

		auto drawnImages = camera.getDrawnImages();
		cv::namedWindow("Marked Images", CV_WINDOW_NORMAL);
		for (size_t i = 0; i < drawnImages.size(); i++)
		{
			cv::imshow("Marked Images", drawnImages.at(i));
			cv::waitKey(10);
		}
		cv::destroyWindow("Marked Images");
	}

	spdlog::debug("Sending camera to server");
	client.sendCameraToServer(camera);

	auto detectorParameters = cv::aruco::DetectorParameters::create();
	detectorParameters->adaptiveThreshConstant = 2;

	auto images = readImagesFromDir(datasetDir + "detection/state_1", "^IMG.+\\.jpg$");
	
	std::thread sendImagesThread = std::thread(&Client::sendQueuedImages, &client);

	sendFromImages(images, 0, client, camera, dictionary, detectorParameters);
	//sendFromVideo(datasetDir + "video/light.mp4", client, camera, dictionary, detectorParameters);

	client.setEndOfData();
	
	client.sendTypeOfData(Network::END_OF_DATA);
}