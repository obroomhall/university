#include <jengar/client/settings.hpp>
#include <set>

Settings::Settings(std::string address, int port)
{
	Client *client = new Client(address, port);
	//assert(client->ping());
	this->client = client;

	setCustomDictionaryNames();

	auto detectorParams = cv::aruco::DetectorParameters::create();
	detectorParams->adaptiveThreshConstant = 2;
	setDetectorParameters(detectorParams);

	
}