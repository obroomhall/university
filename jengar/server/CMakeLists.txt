cmake_minimum_required(VERSION 3.0.0)

FIND_PACKAGE(aruco REQUIRED)
FIND_PACKAGE(marker_mapper REQUIRED)
FIND_PACKAGE(ucoslam REQUIRED)

include_directories(
	${CMAKE_CURRENT_SOURCE_DIR}/include
	/usr/local/include)

add_executable(server 
	${CMAKE_CURRENT_SOURCE_DIR}/src/dictionary.cpp
	${CMAKE_CURRENT_SOURCE_DIR}/src/server.cpp)

target_link_libraries(server 
	${OpenCV_LIBS}
	${aruco_LIBS} 
	${marker_mapper_LIBS}
	${ucoslam_LIBS})