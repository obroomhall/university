cmake_minimum_required(VERSION 3.0.0)
project(jengar VERSION 0.1.0)

SET(CMAKE_CXX_FLAGS  "${CMAKE_CXX_FLAGS} -std=c++11")
set (CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_SOURCE_DIR}/bin)
set (CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_SOURCE_DIR}/lib)

FIND_PACKAGE(OpenCV REQUIRED COMPONENTS aruco calib3d core features2d highgui imgcodecs imgproc video videoio)
link_libraries(pthread)

include_directories(
	${CMAKE_CURRENT_SOURCE_DIR}/shared/include
	${CMAKE_CURRENT_SOURCE_DIR}/3rdparty/spdlog/include
	${CMAKE_CURRENT_SOURCE_DIR}/3rdparty)

if(NOT DEFINED BUILD_SERVER)
    set(BUILD_SERVER ON)
endif()

if (BUILD_SERVER)
	add_subdirectory(server)
endif()

if(NOT DEFINED BUILD_CLIENT)
    set(BUILD_CLIENT ON)
endif()

if (BUILD_CLIENT)
	add_subdirectory(client)
endif()