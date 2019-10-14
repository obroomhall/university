#pragma once

#include <opencv2/opencv.hpp>
#include <opencv2/aruco.hpp>
#include <opencv2/aruco/charuco.hpp>

class Camera
{
private:
	cv::Ptr<cv::aruco::CharucoBoard> charucoBoard;
	cv::Ptr<cv::aruco::DetectorParameters> detectorParams;
	cv::Mat cameraMatrix, distCoeffs;
	std::vector<cv::Mat> rvecs, tvecs;
	double arucoRepError, charucoRepError;

	cv::Size imageSize;
	std::vector<std::vector<std::vector<cv::Point2f>>> allCorners;
	std::vector<std::vector<int>> allIds;
	std::vector<cv::Mat> allImgs;
	std::vector<cv::Mat> allDrawnImgs;
	bool calibrated = false;

public:
	Camera()
	{
		detectorParams = cv::aruco::DetectorParameters::create();
		detectorParams->cornerRefinementMethod = cv::aruco::CORNER_REFINE_NONE;
	};
	Camera(std::string calibrationFile) { readCalibration(calibrationFile); }
	Camera(cv::Size _imageSize, cv::Mat _cameraMatrix, cv::Mat _distCoeffs)
		: imageSize(_imageSize), cameraMatrix(_cameraMatrix), distCoeffs(_distCoeffs)
	{
		calibrated = true;
	}

	bool isCalibrated() { return calibrated; };
	cv::Mat &getCameraMatrix() { return cameraMatrix; };
	cv::Mat &getDistCoeffs() { return distCoeffs; };
	int getWidth() { return imageSize.width; };
	int getHeight() { return imageSize.height; };
	cv::Ptr<cv::aruco::CharucoBoard> getCharucoBoard() { return charucoBoard; }
	int getMarkersX() { return charucoBoard->getChessboardSize().width; }
	int getMarkersY() { return charucoBoard->getChessboardSize().height; }
	float getSquareLength() { return charucoBoard->getSquareLength(); }
	float getMarkerLength() { return charucoBoard->getMarkerLength(); }
	cv::Ptr<cv::aruco::Dictionary> getDictionary() { return charucoBoard->dictionary; }
	std::vector<cv::Mat> getDrawnImages() { return allDrawnImgs; }

	void setCharucoBoard(cv::Ptr<cv::aruco::CharucoBoard> charucoBoard)
	{
		this->charucoBoard.release();
		this->charucoBoard = charucoBoard;
	}
	void setCharucoBoard(int markersX, int markersY, float squareLength, float markerLength)
	{
		setCharucoBoard(cv::aruco::CharucoBoard::create(markersX, markersY, squareLength, markerLength, getDictionary()));
	}
	void setDictionary(cv::Ptr<cv::aruco::Dictionary> dictionary)
	{
		setCharucoBoard(cv::aruco::CharucoBoard::create(getMarkersX(), getMarkersY(), getSquareLength(), getMarkerLength(), dictionary));
	}

	bool calibrationAddImage(cv::Mat &image);
	void calibrationAddImages(cv::Ptr<std::vector<cv::Mat>> images);
	bool calibrate();
	bool saveCalibration(const std::string &filename);
	bool readCalibration(const std::string &filename);
};