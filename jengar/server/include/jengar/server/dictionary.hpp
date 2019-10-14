#pragma once

#include <opencv2/opencv.hpp>
#include <opencv2/aruco.hpp>

void showAllMarkers(cv::aruco::Dictionary dict);
cv::Ptr<cv::aruco::Dictionary> getCustomDictionary(std::string dictName, int nMarkers);
std::tuple<cv::Mat, int> getBytesListAndMarkerSize(std::string dictName, int nMarkers);