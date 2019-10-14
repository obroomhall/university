# JengAR

Final year project implemented whilst studying Computer Science at the University of Bath. For a video demonstration, follow the YouTube link below.

[![IMAGE ALT TEXT](https://i.imgur.com/23ol2QX.png)](https://www.youtube.com/watch?v=8zgHpTRQlkI&list=PLSlfLylanVW2aSn7l76HOkm6lxRZZjbPf "JengAR Demonstration")

## Features

#### Base Features

- Client detects *square fiducial markers* placed on the ends of each Jenga block in the tower
- Client sends frames in which markers were detected to server for *simulataneous location and mapping* of feature points
- Server rebuilds tower inside a 3D Unity scene
- Server runs several physics simulations and assigns each block a *removal feasibility ranking*
- Server sends the rankings back to the client to display in augmented reality
- Client displays rankings in various ways, most notably using a green-red colour scale

#### Extra Features

- Android application for client frontend
- Automatic camera calibration of marker boards instead of taking pictures manually
- Conversion of aruco::Dictionary to cv::aruco::Dictionary to make use of custom ArUco markers

## Dependencies

- [OpenCV](https://github.com/opencv/opencv) and [OpenCV_Contrib](https://github.com/opencv/opencv_contrib)
- [ArUco](https://www.uco.es/investiga/grupos/ava/node/26)
- [MarkerMapper](http://www.uco.es/investiga/grupos/ava/node/57)
- [Unity 3D](https://unity.com)

## Other Requirements

- [Jenga](http://jenga.com/)
- Unique ArUco markers
- Android phone for client
- Desktop/Laptop for server
