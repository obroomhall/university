/*
By downloading, copying, installing or using the software you agree to this
license. If you do not agree to this license, do not download, install,
copy or use the software.

                          License Agreement
               For Open Source Computer Vision Library
                       (3-clause BSD License)

Copyright (C) 2013, OpenCV Foundation, all rights reserved.
Third party copyrights are property of their respective owners.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

  * Redistributions of source code must retain the above copyright notice,
    this list of conditions and the following disclaimer.

  * Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

  * Neither the names of the copyright holders nor the names of the contributors
    may be used to endorse or promote products derived from this software
    without specific prior written permission.

This software is provided by the copyright holders and contributors "as is" and
any express or implied warranties, including, but not limited to, the implied
warranties of merchantability and fitness for a particular purpose are
disclaimed. In no event shall copyright holders or contributors be liable for
any direct, indirect, incidental, special, exemplary, or consequential damages
(including, but not limited to, procurement of substitute goods or services;
loss of use, data, or profits; or business interruption) however caused
and on any theory of liability, whether in contract, strict liability,
or tort (including negligence or otherwise) arising in any way out of
the use of this software, even if advised of the possibility of such damage.
*/

#include <jengar/client/camera.hpp>
#include <opencv2/opencv.hpp>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/imgproc/types_c.h>
#include <spdlog/spdlog.h>

#include <vector>
#include <iostream>
#include <ctime>
#include <dirent.h>

#define ARUCO "aruco"
#define CHARUCO "charuco"

void Camera::calibrationAddImages(cv::Ptr<std::vector<cv::Mat>> images)
{
	for (int i = 0; i < images->size(); i++)
		calibrationAddImage(images->at(i));
}

bool Camera::calibrationAddImage(cv::Mat &image)
{
	if (imageSize.empty())
	{
		imageSize = image.size();
	}

	// handles different rotations
	if (imageSize != image.size())
	{
		if (imageSize.width == image.size().height && imageSize.height == image.size().width)
		{
			cv::rotate(image, image, cv::ROTATE_90_COUNTERCLOCKWISE);
		}
		else
		{
			spdlog::error("[Camera] [Calibration] Calibration images are of different size");
			return false;
		}
	}

	std::vector<int> ids;
	std::vector<std::vector<cv::Point2f>> corners, rejected;
	cv::Mat imageCopy;
	image.copyTo(imageCopy);

	// detect markers
	cv::aruco::detectMarkers(image, getDictionary(), corners, ids);//, detectorParams, rejected);

	// refind strategy to detect more markers
	cv::aruco::refineDetectedMarkers(image, charucoBoard, corners, ids, rejected);

	// interpolate charuco corners
	cv::Mat currentCharucoCorners, currentCharucoIds;
	if (ids.size() > 0)
		cv::aruco::interpolateCornersCharuco(corners, ids, image, charucoBoard, currentCharucoCorners,
											 currentCharucoIds);
	// draw results
	if (ids.size() > 0)
		cv::aruco::drawDetectedMarkers(imageCopy, corners, ids);
	
	if (currentCharucoCorners.total() > 0)
		cv::aruco::drawDetectedCornersCharuco(imageCopy, currentCharucoCorners, currentCharucoIds);

	if (ids.size() > 0)
	{
		allCorners.push_back(corners);
		allIds.push_back(ids);
		allImgs.push_back(image);
		allDrawnImgs.push_back(imageCopy);
		return true;
	}
	else
	{
		spdlog::warn("[Camera] [Calibration] Could not detect marker in image");
		return false;
	}
}

bool Camera::calibrate()
{
	// collect data from each frame
	if (allIds.size() < 5)
	{
		spdlog::warn("[Camera] [Calibration] Not enough captures for calibration");
		return false;
	}

	// prepare data for calibration
	std::vector<std::vector<cv::Point2f>> allCornersConcatenated;
	std::vector<int> allIdsConcatenated;
	std::vector<int> markerCounterPerFrame;
	markerCounterPerFrame.reserve(allCorners.size());
	for (unsigned int i = 0; i < allCorners.size(); i++)
	{
		markerCounterPerFrame.push_back((int)allCorners[i].size());
		for (unsigned int j = 0; j < allCorners[i].size(); j++)
		{
			allCornersConcatenated.push_back(allCorners[i][j]);
			allIdsConcatenated.push_back(allIds[i][j]);
		}
	}

	// calibrate camera using aruco markers
	arucoRepError = cv::aruco::calibrateCameraAruco(allCornersConcatenated, allIdsConcatenated,
											  markerCounterPerFrame, charucoBoard, imageSize, cameraMatrix,
											  distCoeffs);

	// prepare data for charuco calibration
	int nFrames = (int)allCorners.size();
	std::vector<cv::Mat> allCharucoCorners;
	std::vector<cv::Mat> allCharucoIds;
	std::vector<cv::Mat> filteredImages;
	allCharucoCorners.reserve(nFrames);
	allCharucoIds.reserve(nFrames);

	for (int i = 0; i < nFrames; i++)
	{
		// interpolate using camera parameters
		cv::Mat currentCharucoCorners, currentCharucoIds;
		cv::aruco::interpolateCornersCharuco(allCorners[i], allIds[i], allImgs[i], charucoBoard,
										 currentCharucoCorners, currentCharucoIds, cameraMatrix,
										 distCoeffs);

		allCharucoCorners.push_back(currentCharucoCorners);
		allCharucoIds.push_back(currentCharucoIds);
		filteredImages.push_back(allImgs[i]);
	}

	if (allCharucoCorners.size() < 4)
	{
		spdlog::warn("[Camera] [Calibration] Not enough corners for calibration");
		return false;
	}

	// calibrate camera using charuco
	charucoRepError =
		cv::aruco::calibrateCameraCharuco(allCharucoCorners, allCharucoIds, charucoBoard, imageSize,
									  cameraMatrix, distCoeffs, rvecs, tvecs);

	saveCalibration("client/data/calib.yml");
	spdlog::info("[Camera] [Calibration] [Aruco] Total average error: {0}", arucoRepError);
	spdlog::info("[Camera] [Calibration] [Charuco] Total average error: {0}", charucoRepError);
	return true;
}

bool Camera::saveCalibration(const std::string &filename)
{
	cv::FileStorage fs(filename, cv::FileStorage::WRITE);
	if (!fs.isOpened())
		return false;

	fs << "image_width" << imageSize.width;
	fs << "image_height" << imageSize.height;
	fs << "camera_matrix" << cameraMatrix;
	fs << "distortion_coefficients" << distCoeffs;
	fs << "avg_reprojection_error" << charucoRepError;

	calibrated = true;
	return calibrated;
}

bool Camera::readCalibration(const std::string &filename)
{
	cv::FileStorage fs(filename, cv::FileStorage::READ);
	if (!fs.isOpened())
		return false;
	fs["camera_matrix"] >> cameraMatrix;
	fs["distortion_coefficients"] >> distCoeffs;
	fs["image_width"] >> imageSize.width;
	fs["image_height"] >> imageSize.height;
	calibrated = true;
	return calibrated;
}