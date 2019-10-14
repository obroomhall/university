#include <jni.h>
#include <string>
#include <android/log.h>

#include <opencv2/aruco.hpp>
#include <opencv2/aruco/charuco.hpp>
#include <jengar/network.hpp>
#include <jengar/client/settings.hpp>
#include <jengar/client/client.hpp>
#include <jengar/client/camera.hpp>


Client *client = nullptr;
Camera *camera = nullptr;
bool isMapComplete = false;
cv::Ptr<cv::aruco::DetectorParameters> detectorParameters;
cv::Ptr<cv::aruco::Dictionary> mydict = cv::aruco::getPredefinedDictionary(
        cv::aruco::DICT_ARUCO_ORIGINAL);

std::vector<cv::Scalar> colours = { cv::Scalar(255,0,0), cv::Scalar(0,255,0), cv::Scalar(0,0,255), cv::Scalar(255,255,0), cv::Scalar(255,0,255), cv::Scalar(0,255,255), cv::Scalar(255,255,255), cv::Scalar(0,0,0) };

std::unordered_map<int, int> idsAndComponents;
std::unordered_map<int, float> scores = { {88,	0.2579432f},
        {82,	0.3528687f},
        {80,	0.7252292f},
        {76,	4.516454f},
        {74,	0.4397835f},
        {70,	0.0f},
        {60,	39.0048f},
        {66,	0.7014929f},
        {68,	0.2005249f},
        {98,	0.4828056f},
        {50,	2.281442f},
        {40,	0.5951709f},
        {36,	0.7327178f},
        {48,	0.5844985f},
        {102,	1.780055f},
        {28,	37.67044f},
        {26,	40.0f},
        {24,	0.7407672f},
        {22,	0.5477545f},
        {12,	0.1842147f},
        {20,	0.9173062f},
        {8,	1.841002f},
        {6,	0.7876041f},
        {0,	0.9640911f} };

extern "C" JNIEXPORT void JNICALL
Java_com_github_obroomhall_jengar_CameraActivity_init(
        JNIEnv *env, jobject jobj) {

    camera = new Camera();
    auto dictionary = cv::aruco::getPredefinedDictionary(cv::aruco::DICT_ARUCO_ORIGINAL);
    auto board = cv::aruco::CharucoBoard::create(5, 8, 2, 1, dictionary);
    camera->setCharucoBoard(board);

    detectorParameters = cv::aruco::DetectorParameters::create();
    detectorParameters->adaptiveThreshConstant = 2;

    idsAndComponents = std::unordered_map<int, int>();
}

extern "C" JNIEXPORT void JNICALL
Java_com_github_obroomhall_jengar_CameraActivity_connectToServer(
        JNIEnv *env, jobject jobj,
        jstring jaddress, jint jport) {

    const char *addressChar = env->GetStringUTFChars(jaddress, nullptr);
    std::string address = addressChar;

    if (client != nullptr && client->ping()) {
        //env->ThrowNew(env->FindClass("java/lang/Exception"), "Server already connected");
    }
    else {
        try {
            client = new Client(address, (int) jport);
        }
        catch (...) {
            env->ThrowNew(env->FindClass("java/lang/Exception"), "Couldn't open a new connection with server");
        }
    }
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_github_obroomhall_jengar_CameraActivity_getDictionaryNames(
        JNIEnv *env, jobject jobj) {

    std::vector<std::string> names;
    client->getCustomDictionaryNamesFromSever(names);

    auto ret = (jobjectArray) env->NewObjectArray(names.size(), env->FindClass("java/lang/String"),
                                                  env->NewStringUTF(""));

    for (int i = 0; i < names.size(); i++) {
        env->SetObjectArrayElement(ret, i, env->NewStringUTF(names.at(i).c_str()));
    }

    return (ret);
}

extern "C" JNIEXPORT void JNICALL
Java_com_github_obroomhall_jengar_CameraActivity_setDictionary(
        JNIEnv *env, jobject jobj, jstring jDictName) {

    const char *cDictName = env->GetStringUTFChars(jDictName, nullptr);
    client->getDictionaryFromServer(cDictName, 250, mydict);
}



extern "C" JNIEXPORT jlong JNICALL
Java_com_github_obroomhall_jengar_CameraActivity_detectMarkers(
        JNIEnv *env, jobject jobj, jlong frameAddress) {

    auto *frame = (cv::Mat *) frameAddress;

    std::vector<int> ids;
    std::vector<std::vector<cv::Point2f>> corners;

    cv::aruco::detectMarkers(*frame, mydict, corners, ids);

    if (ids.size() > 0) {

        int component = -1;
        bool sendImage = false;

        // Check if any id belongs to component
for (int i = 0; i < ids.size(); ++i) {
	if (idsAndComponents.find(ids[i]) != idsAndComponents.end()) {
		int currentComponent = idsAndComponents[ids[i]];
		if (currentComponent < component || component < 0) {
			component = currentComponent;
		}
		sendImage = true; 
	}
}
            
        if (sendImage) {
             client->queueImage(*frame);   
        }

        // If no component found, create one
        if (component < 0) {
if (idsAndComponents.size() > 0) {
	auto it = std::max_element(idsAndComponents.begin(), idsAndComponents.end(),
							   [](decltype(idsAndComponents)::value_type &l,
								  decltype(idsAndComponents)::value_type &r) -> bool {
								   return l.second < r.second;
							   });
	component = (it->second)+1;
	std::cout << component;
} else {
	component = 0;
}
        }

        // Assign all ids to the component
        for (int i = 0; i < ids.size(); ++i) {
            idsAndComponents[ids[i]] = component;
        }

        // Draw colour of component
        cv::Scalar colour = colours.at(component);
        for(int i = 0; i < corners.size(); i++) {

                std::vector<cv::Point2f> currentCorners = corners.at(i);
                cv::rectangle(*frame, currentCorners.at(0), currentCorners.at(2), colour,
                              CV_FILLED);
        }
    }

    return (jlong) frame;
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_github_obroomhall_jengar_CameraActivity_showScore(
        JNIEnv *env, jobject jobj, jlong frameAddress) {

    auto *frame = (cv::Mat *) frameAddress;

    std::vector<int> ids;
    std::vector<std::vector<cv::Point2f>> corners;

    cv::aruco::detectMarkers(*frame, mydict, corners, ids);

    if (ids.size() > 0) {

        for(int i = 0; i < corners.size(); i++) {

            float rank = -1;

            if (scores.find(ids[i]) != scores.end()) {
                rank = scores[ids[i]] / 40;
            } else if (scores.find(ids[i]-1) != scores.end()) {
                rank = scores[ids[i]-1] / 40;
            }

            if ( rank >= 0) {

                int r = 2 * rank * 255;
                int g = 2 * (1 - rank) * 255;

                if (r > 255) r = 255;
                if (r < 0) r = 0;
                if (g > 255) g = 255;
                if (g < 0) g = 0;

                cv::Scalar scoreColour = cv::Scalar(r, g, 0);

                std::vector<cv::Point2f> currentCorners = corners.at(i);
                cv::rectangle(*frame, currentCorners.at(0), currentCorners.at(2), scoreColour,
                              CV_FILLED);
            }
        }
    }

    return (jlong) frame;
}
