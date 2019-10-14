#include <jengar/server/dictionary.hpp>
#include <aruco/aruco.h>

void showAllMarkers(cv::aruco::Dictionary dict)
{
	for (int i = 0; i < dict.bytesList.total(); i++)
	{
		cv::Mat marker;
		dict.drawMarker(i, 150, marker);
		cv::imshow("Marker", marker);
		cv::waitKey();
	}
}

std::tuple<cv::Mat, int> getBytesListAndMarkerSize(std::string dictName, int nMarkers)
{
	assert(nMarkers > 0 && ("You specified an invalid number of markers for a dictionary: " + nMarkers));

	::aruco::Dictionary dict = ::aruco::Dictionary::loadPredefined(dictName);

	if (dict.size() < nMarkers)
	{
		std::cout << "Warning: The requested dictionary does not have the number of elements you requested (" + std::to_string(nMarkers) + "), changing number of elements to the size of the dictionary (" + std::to_string(dict.size()) + ")." << std::endl;
		nMarkers = dict.size();
	}

	int markerSize = sqrt(dict.nbits());
	cv::Mat bytes = cv::Mat::zeros(cv::Size(5, nMarkers), CV_8UC4);

	for (int i = 0; i < nMarkers; i++)
	{
		cv::Mat marker = dict.getMarkerImage_id(i, 1);
		cv::Mat M = marker(cv::Range(1, marker.rows - 1), cv::Range(1, marker.cols - 1));
		M.setTo(1, M > 0);

		cv::Mat byteList = cv::aruco::Dictionary::getByteListFromBits(M);
		byteList.copyTo(bytes.row(i));
	}

	return std::tuple<cv::Mat, int>(bytes, markerSize);
}

cv::Ptr<cv::aruco::Dictionary> getCustomDictionary(std::string dictName, int nMarkers)
{
	cv::Mat bytesList;
	int markerSize;
	std::tie(bytesList, markerSize) = getBytesListAndMarkerSize(dictName, nMarkers);

	return cv::makePtr<cv::aruco::Dictionary>(bytesList, markerSize);
}