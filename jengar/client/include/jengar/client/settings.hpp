#pragma once

#include <jengar/client/client.hpp>

#include <opencv2/opencv.hpp>
#include <opencv2/aruco/charuco.hpp>

class Settings
{
private:
	const int blocks = 54;
	const std::string calibrationFile = "client/calib.yml";
	std::vector<std::string> standardDictionaryNames = {
		"DICT_4X4_50", "DICT_4X4_100", "DICT_4X4_250", "DICT_4X4_1000",
		"DICT_5X5_50", "DICT_5X5_100", "DICT_5X5_250", "DICT_5X5_1000",
		"DICT_6X6_50", "DICT_6X6_100", "DICT_6X6_250", "DICT_6X6_1000",
		"DICT_7X7_50", "DICT_7X7_100", "DICT_7X7_250", "DICT_7X7_1000",
		"DICT_ARUCO_ORIGINAL"};

	Client *client;
	Camera *camera = new Camera(calibrationFile);
	std::vector<std::string> customDictionaryNames;
	cv::Ptr<cv::aruco::DetectorParameters> detectorParameters;

public:
	Settings(std::string address, int port);

	Client *getClient() { return client; }
	Camera *getCamera() { return camera; }
	void setClient(Client *client) { this->client = client; }
	void setCamera(Camera *camera) { this->camera = camera; }

	cv::Ptr<cv::aruco::Dictionary> getDictionary() { return camera->getDictionary(); }
	std::vector<std::string> getDictionaryNamesStandard() { return standardDictionaryNames; }
	std::vector<std::string> getDictionaryNamesCustom() { return customDictionaryNames; }
	std::string getServerAddress() { return client->getAddress(); }
	int getServerPort() { return client->getPort(); }
	cv::Ptr<cv::aruco::DetectorParameters> getDetectorParameters() { return detectorParameters; }

	bool setDictionaryCustom(std::string name)
	{
		cv::Ptr<cv::aruco::Dictionary> dictionary;
		if (client->getDictionaryFromServer(name, blocks * 2, dictionary))
		{
			camera->setDictionary(dictionary);
			return true;
		}
		else
		{
			return false;
		}
	}
	void setDictionaryStandard(cv::Ptr<cv::aruco::Dictionary> dictionary)
	{
		camera->setDictionary(dictionary);
	}
	void setDetectorParameters(cv::Ptr<cv::aruco::DetectorParameters> detectorParameters)
	{
		this->detectorParameters = detectorParameters;
	}
	bool setCustomDictionaryNames()
	{
		std::vector<std::string> names;
		if (client->getCustomDictionaryNamesFromSever(names))
		{
			customDictionaryNames = names;
			return true;
		}
		else
		{
			return false;
		}
	}
	void setServerAddress(std::string address)
	{
		restartClient(address, client->getPort());
	}
	void setServerPort(int port)
	{
		restartClient(client->getAddress(), port);
	}

	void restartClient(std::string address, int port)
	{
		client->~Client();
		client = new Client(address, port);
		//assert(client->ping());
	}
};