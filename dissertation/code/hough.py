import sys
import cv2 as cv
import numpy as np

# Load image
def get_source():
    if len(sys.argv) > 1:
        filename = sys.argv[1]
    else:
        filename = "img/jenga.jpeg"

    return cv.imread(filename, cv.IMREAD_GRAYSCALE)


# Recalculate transform on update of variables
def update(var): hough_transform()


# Create track bars
def create_trackers(winname, trackers):
    for default, maximum, name in trackers:
        cv.createTrackbar(name, winname, default, maximum, update)


# Blur image with bilateral filter
def blur_img(img, blur):
    d, colour, space = blur
    return cv.bilateralFilter(img, d, colour, space)


# Get canny edges
def get_edges(img, canny):
    low, high = canny
    return cv.Canny(img, low, high, None, 3)


# Get lines from hough transform
def get_lines(edges, hough):
    thresh, min_len, max_gap = hough
    return cv.HoughLinesP(edges, 1, np.pi / 180, thresh, None, min_len, max_gap)


# Draw hough lines on image
def draw_lines(img, lines):
    if lines is not None:
        for line in lines[0]:
            cv.line(img, (line[0], line[1]), (line[2], line[3]), (0, 0, 255), 3, cv.CV_AA)


# Get values for blur from tracker
def get_blur():
    d = cv.getTrackbarPos(bil_d[2], winname)
    colour = cv.getTrackbarPos(bil_colour[2], winname)
    space = cv.getTrackbarPos(bil_space[2], winname)
    return d, colour, space


# Get values for canny from tracker
def get_canny():
    low = cv.getTrackbarPos(canny_low[2], winname)
    high = cv.getTrackbarPos(canny_high[2], winname)
    return low, high


# Get values for hough from tracker
def get_hough():
    thresh = cv.getTrackbarPos(hough_thresh[2], winname)
    min_len = cv.getTrackbarPos(hough_min_len[2], winname)
    max_gap = cv.getTrackbarPos(hough_max_gap[2], winname)
    return thresh, min_len, max_gap


# Do hough transform on source image
def hough_transform(blur=None, canny=None, hough=None):
    blur = get_blur() if blur is None else blur
    canny = get_canny() if canny is None else canny
    hough = get_hough() if hough is None else hough

    blurred = blur_img(src, blur)
    edges = get_edges(blurred, canny)

    lines = get_lines(edges, hough)
    lines_on_edges = cv.cvtColor(edges, cv.COLOR_GRAY2BGR)
    draw_lines(lines_on_edges, lines)

    out = get_step_by_step(blurred, edges, lines_on_edges)
    cv.imshow(winname, out)

    print(len(lines))


# Get multiple images for display
def get_step_by_step(blurred, edges, lines_on_edges):
    blurred = cv.cvtColor(blurred, cv.COLOR_GRAY2BGR)
    edges = cv.cvtColor(edges, cv.COLOR_GRAY2BGR)
    return np.hstack((blurred, edges, lines_on_edges))


def main():
    if src is None:
        print('Error opening image!')
        return -1

    cv.namedWindow(winname, cv.WINDOW_NORMAL)
    cv.resizeWindow(winname, 1500, 1000)

    create_trackers(winname, trackers)

    hough_transform()
    cv.waitKey()

# Trackers. (min, def, max, name)
bil_d = 9, 30, 'BIL_d'
bil_colour = 75, 200, 'BIL_Colour'
bil_space = 75, 200, 'BIL_Space'
canny_low = 20, 100, 'Canny Low'
canny_high = 60, 300, 'Canny High'
hough_thresh = 50, 200, 'Hough Thresh'
hough_min_len = 50, 200, 'Hough Min Length'
hough_max_gap = 10, 200, 'Hough Max Gap'
trackers = [bil_d, bil_colour, bil_space, canny_low, canny_high, hough_thresh, hough_min_len, hough_max_gap]

src = get_source()
winname = 'Output'
main()
