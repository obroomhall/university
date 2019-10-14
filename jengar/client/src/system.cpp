#include <jengar/client/system.hpp>

#include <opencv2/opencv.hpp>
#include <opencv2/core.hpp>

#include <dirent.h>
#include <regex>
#include <unistd.h>
#include <string>
#include <fstream>

cv::Ptr<std::vector<cv::Mat>> readImagesFromDir(std::string imgDir, std::string regex_str)
{
	std::regex reg1(regex_str, std::regex_constants::icase);
	DIR *dir;
	struct dirent *ent;
	std::vector<std::string> filenames;
	if ((dir = opendir(imgDir.c_str())) != NULL)
	{
		while ((ent = readdir(dir)) != NULL)
		{
			std::string imgName = ent->d_name;
			if (std::regex_search(imgName, reg1))
			{
				std::string imgPath = imgDir + '/' + imgName;
				filenames.push_back(imgPath);
			}
		}
		closedir(dir);
	}

	std::sort(filenames.begin(), filenames.end());

	std::vector<cv::Mat> images;
	for (size_t i = 0; i < filenames.size(); i++)
	{
		cv::Mat img = cv::imread(filenames.at(i));
		getRealImage(img);
		images.push_back(img);
	}

	return cv::makePtr<std::vector<cv::Mat>>(images);
}

bool fileExists(const std::string &name)
{
	return (access(name.c_str(), F_OK) != -1);
}

void getRealImage(cv::Mat &image)
{
	// Switch to landscape
	if (image.cols < image.rows)
		cv::rotate(image, image, cv::ROTATE_90_COUNTERCLOCKWISE);

	// Scale and correct for aspect ratio
	if (image.cols > 1920)
	{
		double scale = 1920.0 / image.cols;
		cv::resize(image, image, cv::Size(), scale, scale);
		image = cv::Mat(image, cv::Rect(0, (image.rows-1080)/2, 1920, 1080));
	}
}