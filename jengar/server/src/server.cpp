// Server side C/C++ program to demonstrate Socket programming
// https://www.geeksforgeeks.org/socket-programming-cc/

#include <jengar/server/server.hpp>
#include <jengar/network.hpp>
#include <jengar/server/dictionary.hpp>

#include <opencv2/opencv.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/aruco.hpp>
#include <opencv2/aruco/charuco.hpp>
#include <aruco/aruco.h>
#include <aruco/dictionary.h>
#include <ucoslam/mapviewer.h>
#include <ucoslam/ucoslam.h>

#include <marker_mapper/debug.h>
#include <marker_mapper/mapper_types.h>
#include <marker_mapper/markermapper.h>

#include <iostream>
#include <unistd.h>
#include <stdio.h>
#include <sys/socket.h>
#include <stdlib.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <string.h>
#include <fstream>
#include <dirent.h>
#include <thread>
#include <sstream> // for ostringstream

#include <spdlog/spdlog.h>

#define LOG_NETWORK "Network"
#define LOG_SETUP "Setup"
#define LOG_MAIN "Main"
#define PORT 8080

int main(int argc, char **argv)
{
	spdlog::set_level(spdlog::level::debug);

	try
	{
		Server server(PORT);
		server.getSettings();

		std::thread getImagesThread(&Server::getImages, &server);
		std::thread slamThread(&Server::slam, &server);
		//std::thread slamThread(&Server::slam, &server, map);

		getImagesThread.join();
		slamThread.join();

	}
	catch (const std::runtime_error &e)
	{
		spdlog::critical(e.what());
	}
	catch (const Network::SocketException &e)
	{
		spdlog::critical("[Exception] Socket Exception");
	}
	catch (const Network::NoBytesException &e)
	{
		spdlog::critical("[Exception] No Bytes");
	}
	return 0;
}

Server::Server(int port)
{
	// Creating socket file descriptor
	int server_fd;
	assert((server_fd = ::socket(AF_INET, SOCK_STREAM, 0)) != 0 && "socket failed");

	// Forcefully attaching socket to the port 8080
	int opt = 1;
	assert(!setsockopt(server_fd, SOL_SOCKET, SO_REUSEADDR | SO_REUSEPORT, &opt, sizeof(opt)) && "setsockopt");

	struct sockaddr_in address;
	address.sin_family = AF_INET;
	address.sin_addr.s_addr = INADDR_ANY;
	address.sin_port = htons(port);

	// Forcefully attaching socket to the port
	assert(bind(server_fd, (struct sockaddr *)&address, sizeof(address)) >= 0 && "bind failed");

	// Listen for connections
	assert(listen(server_fd, 3) >= 0 && "listen");
	spdlog::info("[{0}] [Server] Open", LOG_NETWORK);

	// Get socket value
	int addrlen = sizeof(address);
	assert((socket = accept(server_fd, (struct sockaddr *)&address, (socklen_t *)&addrlen)) >= 0 && "accept");

	spdlog::info("[{0}] [Client] Connected", LOG_NETWORK);
}

void Server::getSettings()
{
	spdlog::info("[{0}] [Starting]", LOG_SETUP);

	std::vector<int> validRequests = {REQUEST_DICTNAMES,
									  REQUEST_DICTIONARY,
									  SERVE_CAMERA};

	while (!cameraParams.isValid())
	{
		spdlog::debug("CameraParams validity: {0}", cameraParams.isValid());
		spdlog::info("[{0}] [Waiting]", LOG_SETUP);

		int type = getTypeOfData();
		auto ind = std::find(std::begin(validRequests), std::end(validRequests), type);
		if (ind != std::end(validRequests))
		{
			sendTypeOfData(GOOD_REQUEST, false);

			switch (type)
			{
			case REQUEST_DICTNAMES:
				sendCustomDictionaryNamesToClient();
				break;
			case REQUEST_DICTIONARY:
				sendDictionaryToClient();
				break;
			case SERVE_CAMERA:
				getCameraFromClient();
				break;
			}
		}
		else
		{
			sendTypeOfData(BAD_REQUEST, false);
			sendValidRequestTypes(validRequests);
		}
	}

	UcoSlamParams.runSequential = true;
	UcoSlamParams.detectKeyPoints = true;
	UcoSlamParams.detectMarkers = true;
	UcoSlamParams.aruco_DetectionMode = "DM_VIDEO_FAST"; // DM_FAST, DM_NORMAL, DM_VIDEO_FAST
}

void Server::getImages()
{
	spdlog::info("[{0}] Starting", LOG_MAIN);

	std::vector<int> validRequests = {SERVE_MAT,
									  END_OF_DATA,
									  MAP_COMPLETE};

	while (!isMapComplete && !endOfData)
	{
		spdlog::info("[{0}] Waiting", LOG_MAIN);

		int type = getTypeOfData();
		auto ind = std::find(std::begin(validRequests), std::end(validRequests), type);
		if (ind != std::end(validRequests))
		{
			sendTypeOfData(GOOD_REQUEST, false);

			switch (type)
			{
			case SERVE_MAT:
				imageQueue.enqueue(getMat());
				break;
			case END_OF_DATA:
				endOfData = true;
			// case RETURN_TO_SETTINGS:
			// 	getSettings();
			// 	break;
			case MAP_COMPLETE:
				sendMapCompleteness();
				break;
			}
		}
		else
		{
			sendTypeOfData(BAD_REQUEST, false);
			sendValidRequestTypes(validRequests);
		}
	}
}

void Server::slam()
{
		std::shared_ptr<ucoslam::Map> map = std::make_shared<ucoslam::Map>();

	ucoslam::MapViewer mv;
	SLAM.setParams(map, UcoSlamParams, "server/data/orb.fbow");
	int frameNumber = 0;
	while (map->map_markers.size() < blocks * 2 && !endOfData)
	{
		cv::Mat inputImage;
		imageQueue.wait_dequeue(inputImage);

		cv::Mat posef2g = SLAM.process(inputImage, cameraParams, frameNumber++);

		if (posef2g.empty())
			spdlog::warn("[UcoSLAM] [Frame:{0}] Pose not found", frameNumber);
		else
			spdlog::info("[UcoSLAM] [Frame:{0}] Pose found", frameNumber);

		mv.show(map, inputImage, posef2g);
	}

	if (map->map_markers.size() < blocks * 2)
	{
		isMapComplete = true;

	}

	map->saveToMarkerMap("server/data/out.map");

}

void Server::markermapper()
{
	float markerSize = 1.0;
	string dict = "ARUCO_MIP_36h12";
	string outBaseName = "server/data/out";

	int ref_Marker_Id = -1;
	//  if (argc>=7)
	//     if (string(argv[6])=="-ref"){
	//       ref_Marker_Id=stoi( argv[7]);
	//   }

	// aruco_mm::debug::Debug::setLevel(5);
	//start processing
	auto AMM = aruco_mm::MarkerMapper::create();
	AMM->setParams(Camera, markerSize, ref_Marker_Id);
	// if (cml["-c"])
	//     AMM->getMarkerDetector().loadParamsFromFile(cml("-c"));
	// else
	AMM->getMarkerDetector().setDictionary(dict);
	//    AMM->getMarkerDetector().setDetectionMode(aruco::DM_FAST,0.02);
	char key = 0;
	cv::Mat image2;

	//        std::rotate(files.begin(),files.begin()+10,files.end());
	//rotate vector
	int frameidx = 0;

	cv::namedWindow("image", CV_WINDOW_AUTOSIZE);

	while (!endOfData || imageQueue.peek() != nullptr)
	//while (AMM->getMarkerMap().size() < blocks * 2 && clientConnected)
	{
		cv::Mat image;
		imageQueue.wait_dequeue(image);

		if (image.rows == Camera.CamSize.width && image.cols == Camera.CamSize.height)
		{ //auto orient by rotation
			cv::Mat aux;
			cv::transpose(image, aux);
			cv::flip(aux, image, 0);
		}

		if (image.rows != Camera.CamSize.height || image.cols != Camera.CamSize.width)
		{
			spdlog::warn("[MarkerMapper] [Process] Image is not of the dimensions of calibration, {0}x{1} : {2}x{3}", image.rows, image.cols, Camera.CamSize.height, Camera.CamSize.width);
			continue;
		}
		AMM->process(image, frameidx++);
		AMM->drawDetectedMarkers(image, 3);
		cv::resize(image, image2, cv::Size(889, 500));
		cv::imshow("image", image2);
		key = cv::waitKey();

		spdlog::debug("[MarkerMapper] [Mapped] {0} markers mapped", AMM->getMarkerMap().size());
	}

	cv::destroyWindow("image");

	isMapComplete = true;
	//finish processing

	AMM->optimize();
	//        AMM->saveToFile(outBaseName+".amm");
	AMM->saveToPcd(outBaseName + ".pcd", true);
	AMM->saveFrameSetPosesToFile(outBaseName + ".log");
	AMM->getCameraParams().saveToFile(outBaseName + "-cam.yml");
	AMM->getMarkerMap().saveToFile(outBaseName + ".yml");

	//OpenCvMapperViewer Viewer;
	// aruco::MarkerMap mmap;
	// mmap.readFromFile(outBaseName+".yml");
	// Viewer.setParams(mmap,1.5,1280,960,"map_viewer");
	// key=0;
	// while(key!=27)
	//     key =     Viewer.show( );
}

void Server::mymapper()
{

	auto params = aruco::MarkerDetector::Params();
	//params.setDetectionMode = aruco::DetectionMode::DM_FAST;

	auto md = aruco::MarkerDetector("ARUCO_MIP_36h12");
	md.setParameters(params);

	cv::namedWindow("image", CV_WINDOW_AUTOSIZE);

	float markerSize = 1;

	cv::Mat image;
	while (!endOfData || imageQueue.peek() != nullptr)
	{
		imageQueue.wait_dequeue(image);

		auto markers = md.detect(image, Camera, markerSize);

		// for each marker, draw info and its boundaries in the image
		for (unsigned int i = 0; i < markers.size(); i++)
		{
			cout << markers[i] << endl;
			markers[i].draw(image, cv::Scalar(0, 0, 255), 2);
		}
		// draw a 3d cube in each marker if there is 3d info
		if (Camera.isValid() && markerSize != -1)
		{
			for (unsigned int i = 0; i < markers.size(); i++)
			{
				if (markers[i].id == 229 || markers[i].id == 161)
					cout << "Camera Location= " << markers[i].id << " " << Camera.getCameraLocation(markers[i].Rvec, markers[i].Tvec) << endl;
				
				aruco::CvDrawingUtils::draw3dAxis(image, markers[i], Camera);
				//  CvDrawingUtils::draw3dCube(InImage, Markers[i], CamParam);
			}
		}

		cv::resize(image, image, cv::Size(889, 500));
		cv::imshow("image", image);
		cv::waitKey();

		spdlog::debug("[MyMapper] [Mapped] {0} markers mapped");
	}

	cv::destroyWindow("image");
}

void Server::sendMapCompleteness()
{
	send(&isMapComplete, sizeof(bool));
}

void Server::sendDictionaryToClient()
{
	// Get name of dictionary from client
	std::string name = getString();

	int markers;
	receive(&markers, sizeof(int));
	this->blocks = markers / 2;

	cv::Mat bytesList;
	int markerSize;
	std::tie(bytesList, markerSize) = getBytesListAndMarkerSize(name, markers);

	send(&markerSize, sizeof(int));
	sendMat(bytesList);

	spdlog::info("[{0}] [Sent] Dictionary {1}x{2}", LOG_DATA, name, markers);
}

void Server::getCameraFromClient()
{
	int cameraDims[2];
	receive(&cameraDims[0], sizeof(int) * 2);
	cv::Mat cameraMatrix = getMat();
	cv::Mat distCoeffs = getMat();

	std::string calibrationFile = "server/data/calib.yml";

	cv::FileStorage fs(calibrationFile, cv::FileStorage::WRITE);
	if (!fs.isOpened())
		return;

	fs << "image_width" << cameraDims[0];
	fs << "image_height" << cameraDims[1];
	fs << "camera_matrix" << cameraMatrix;
	fs << "distortion_coefficients" << distCoeffs;
	fs.release();

	// UcoSLAM
	cameraParams.readFromXMLFile(calibrationFile);

	// Marker mapper
	Camera.readFromXMLFile(calibrationFile);

	spdlog::info("[{0}] [Received] Camera", LOG_DATA);

	std::ostringstream cameraMatrixStream;
	cameraMatrixStream << cameraMatrix << " " << std::flush;
	spdlog::info("[{0}] [CameraMatrix] {1}", LOG_DATA, cameraMatrixStream.str());

	std::ostringstream distCoeffsStream;
	distCoeffsStream << distCoeffs << " " << std::flush;
	spdlog::info("[{0}] [DistCoeffs] {1}", LOG_DATA, distCoeffsStream.str());
}

void Server::sendCustomDictionaryNamesToClient()
{
	auto names = aruco::Dictionary::getDicTypes();

	size_t nameCount = names.size();
	send(&nameCount, sizeof(size_t));

	for (size_t i = 0; i < nameCount; i++)
		sendString(names.at(i));

	spdlog::info("[{0}] [Sent] Dictionary names", LOG_DATA);
}