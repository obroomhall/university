time(X) :- X = 8..18, X != 12.
day(monday;tuesday;wednesday;thursday;friday).
lectureSlot(D, T) :- time(T), day(D).

student(alice).
student(bob).
student(cara).
student(davos).
student(euron).
student(fred).
student(gendry).
student(harry).

room(101..106).

% Capacity doesn't work as planned.
% Only works if capacity > number of students taking a unit.
#const capacity = 10.

unit(maths;physics;chemistry;biology).

lecturer("Mr. Abu";"Mrs. Bolton";"Mr. Cox").

takes(maths, alice).
takes(maths, bob).
takes(maths, cara).
takes(maths, davos).
takes(maths, euron).
takes(maths, fred).
takes(maths, gendry).
takes(maths, harry).
takes(physics, alice).
takes(physics, bob).
takes(physics, cara).
takes(physics, davos).
takes(physics, fred).
takes(chemistry, davos).
takes(chemistry, euron).
takes(biology, fred).
takes(biology, gendry).
takes(biology, harry).

teaches(maths, "Mr. Abu").
teaches(physics, "Mrs. Bolton").
teaches(chemistry, "Mr. Cox").
teaches(biology, "Mrs. Bolton").

contactHours(5, maths).
contactHours(4, physics).
contactHours(3, chemistry).
contactHours(2, biology).

commitment(lectureSlot(monday, 8), "Mr. Abu").
commitment(lectureSlot(monday, 15), "Mrs. Bolton").

lecture(monday,9,10000,maths,"Mr. Abu",alice).