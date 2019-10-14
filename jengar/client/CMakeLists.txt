cmake_minimum_required(VERSION 3.0.0)

include_directories(${CMAKE_CURRENT_SOURCE_DIR}/include)

add_executable(client 
	${CMAKE_CURRENT_SOURCE_DIR}/src/camera.cpp
	${CMAKE_CURRENT_SOURCE_DIR}/src/client.cpp
	${CMAKE_CURRENT_SOURCE_DIR}/src/settings.cpp
	${CMAKE_CURRENT_SOURCE_DIR}/src/system.cpp
	${CMAKE_CURRENT_SOURCE_DIR}/tools/test.cpp)

target_link_libraries(client 
	${OpenCV_LIBS})