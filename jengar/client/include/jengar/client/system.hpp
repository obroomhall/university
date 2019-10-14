#pragma once

#include <opencv2/opencv.hpp>
#include <opencv2/core.hpp>
#include <vector>

cv::Ptr<std::vector<cv::Mat>> readImagesFromDir(std::string imgDir, std::string regex_str);
bool fileExists(const std::string &name);
void getRealImage(cv::Mat &image);