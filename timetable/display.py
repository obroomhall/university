import sys
import json
import operator

# Load JSON data from stdin
data = json.load(sys.stdin)

# If not SATISIABLE, print result
if data["Result"] != "SATISFIABLE":
    print(data["Result"])
    sys.exit()

# Formats time
def getTime(time):
    "For argument '9' returns '09:00'"
    return time.zfill(2) + ':00';
    
def removeSpeech(str):
    "For argument '\"Mr. Abu\"' returns 'Mr. Abu'"
    return str[1:-1];
    
def getLectureDetails(lecture):
    "Returns an array of lecture details"
    lecture = lecture[8:-1]
    lecture = lecture.split(',')
    lecture[0] = lecture[0].capitalize()
    lecture[1] = getTime(lecture[1])
    lecture[3] = lecture[3].capitalize()
    lecture[4] = removeSpeech(lecture[4])
    lecture[5] = lecture[5].capitalize()
    return lecture;

currentDay = 'Monday'
    
def printLecture(lecture):
    "Prints out lecture details in readable format from array"
    line = ""
    for detail in lecture:
        tabs = len(detail) / 8
        line = line + detail
        i = 2
        while i > int(tabs):
            line = line + "\t"
            i = i - 1
    global currentDay
    if lecture[0] != currentDay:
        currentDay = lecture[0]
        line = "\n" + line
    print(line);

# Print headings in bold
headings = ['Day', 'Time', 'Room', 'Unit', 'Lecturer', 'Students'];
print("\u001B[1m")
printLecture(headings)
print("\u001B[0m")

# Create list of lectures from stdin
listOfLectures = []
for line in data["Call"][0]["Witnesses"][0]["Value"]:
    if line[:8] == "lecture(":
        listOfLectures.append(getLectureDetails(line))

# Add in lunchtimes
listOfLectures.append(['Monday', '12:00', '\u001B[1m', '           LUNCH', ' TIME', '\u001B[0m'])
listOfLectures.append(['Tuesday', '12:00', '\u001B[1m', '           LUNCH', ' TIME', '\u001B[0m'])
listOfLectures.append(['Wednesday', '12:00', '\u001B[1m', '           LUNCH', ' TIME', '\u001B[0m'])
listOfLectures.append(['Thursday', '12:00', '\u001B[1m', '           LUNCH', ' TIME', '\u001B[0m'])
listOfLectures.append(['Friday', '12:00', '\u001B[1m', '           LUNCH', ' TIME', '\u001B[0m'])

# Sorts the lectures by each column, with priority sorting left to right
# Changes day names into ints for sorting, then changes back
for lecture in listOfLectures:
    day = lecture[0]
    if day == 'Monday':
        lecture[0] = 1
    elif day == 'Tuesday':
        lecture[0] = 2
    elif day == 'Wednesday':
        lecture[0] = 3
    elif day == 'Thursday':
        lecture[0] = 4
    elif day == 'Friday':
        lecture[0] = 5
    
listOfLectures.sort(key = operator.itemgetter(0, 1, 2, 3, 4, 5))

for lecture in listOfLectures:
    day = lecture[0]
    if day == 1:
        lecture[0] = 'Monday'
    elif day == 2:
        lecture[0] = 'Tuesday'
    elif day == 3:
        lecture[0] = 'Wednesday'
    elif day == 4:
        lecture[0] = 'Thursday'
    elif day == 5:
        lecture[0] = 'Friday'
                
for lecture in listOfLectures:
    printLecture(lecture)