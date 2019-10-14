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

%*
Day             Time            Room            Unit            Lecturer        Students


Monday          08:00           101             Physics         Mrs. Bolton     Alice    <-- Mr. Abu cannot teach at 08:00
Monday          08:00           101             Physics         Mrs. Bolton     Bob           on Mondays so Mrs. Bolton
Monday          08:00           101             Physics         Mrs. Bolton     Cara          is teaching instead
Monday          08:00           101             Physics         Mrs. Bolton     Davos
Monday          08:00           101             Physics         Mrs. Bolton     Fred
Monday          09:00           101             Maths           Mr. Abu         Alice
Monday          09:00           101             Maths           Mr. Abu         Bob
Monday          09:00           101             Maths           Mr. Abu         Cara
Monday          09:00           101             Maths           Mr. Abu         Davos
Monday          09:00           101             Maths           Mr. Abu         Euron        All students have been allocated
Monday          09:00           101             Maths           Mr. Abu         Fred          to all the units they take.
Monday          09:00           101             Maths           Mr. Abu         Gendry       As have all lecturers been
Monday          09:00           101             Maths           Mr. Abu         Harry         been assigned a lecture time at
Monday          10:00           101             Maths           Mr. Abu         Alice         which to teach.
Monday          10:00           101             Maths           Mr. Abu         Bob
Monday          10:00           101             Maths           Mr. Abu         Cara         No lecturer or student is in the
Monday          10:00           101             Maths           Mr. Abu         Davos         same place at the same time.
Monday          10:00           101             Maths           Mr. Abu         Euron
Monday          10:00           101             Maths           Mr. Abu         Fred         No room is being used for multiple
Monday          10:00           101             Maths           Mr. Abu         Gendry        lectures at the same time.
Monday          10:00           101             Maths           Mr. Abu         Harry
Monday          11:00           101             Maths           Mr. Abu         Alice
Monday          11:00           101             Maths           Mr. Abu         Bob
Monday          11:00           101             Maths           Mr. Abu         Cara
Monday          11:00           101             Maths           Mr. Abu         Davos
Monday          11:00           101             Maths           Mr. Abu         Euron
Monday          11:00           101             Maths           Mr. Abu         Fred
Monday          11:00           101             Maths           Mr. Abu         Gendry
Monday          11:00           101             Maths           Mr. Abu         Harry
Monday          12:00                                      LUNCH TIME
Monday          13:00           101             Maths           Mr. Abu         Alice
Monday          13:00           101             Maths           Mr. Abu         Bob
Monday          13:00           101             Maths           Mr. Abu         Cara
Monday          13:00           101             Maths           Mr. Abu         Davos
Monday          13:00           101             Maths           Mr. Abu         Euron
Monday          13:00           101             Maths           Mr. Abu         Fred
Monday          13:00           101             Maths           Mr. Abu         Gendry
Monday          13:00           101             Maths           Mr. Abu         Harry
Monday          14:00           101             Maths           Mr. Abu         Alice
Monday          14:00           101             Maths           Mr. Abu         Bob
Monday          14:00           101             Maths           Mr. Abu         Cara
Monday          14:00           101             Maths           Mr. Abu         Davos
Monday          14:00           101             Maths           Mr. Abu         Euron
Monday          14:00           101             Maths           Mr. Abu         Fred
Monday          14:00           101             Maths           Mr. Abu         Gendry
Monday          14:00           101             Maths           Mr. Abu         Harry
Monday          15:00           101             Chemistry       Mr. Cox         Davos    <-- Mrs. Bolton cannot teach at 15:00
Monday          15:00           101             Chemistry       Mr. Cox         Euron         so Mr. Cox teaches Chemistry
Monday          16:00           101             Physics         Mrs. Bolton     Alice         instead.
Monday          16:00           101             Physics         Mrs. Bolton     Bob
Monday          16:00           101             Physics         Mrs. Bolton     Cara
Monday          16:00           101             Physics         Mrs. Bolton     Davos
Monday          16:00           101             Physics         Mrs. Bolton     Fred
Monday          17:00           101             Physics         Mrs. Bolton     Alice
Monday          17:00           101             Physics         Mrs. Bolton     Bob
Monday          17:00           101             Physics         Mrs. Bolton     Cara
Monday          17:00           101             Physics         Mrs. Bolton     Davos
Monday          17:00           101             Physics         Mrs. Bolton     Fred
Monday          18:00           101             Physics         Mrs. Bolton     Alice
Monday          18:00           101             Physics         Mrs. Bolton     Bob
Monday          18:00           101             Physics         Mrs. Bolton     Cara
Monday          18:00           101             Physics         Mrs. Bolton     Davos
Monday          18:00           101             Physics         Mrs. Bolton     Fred

Tuesday         08:00           101             Biology         Mrs. Bolton     Fred
Tuesday         08:00           101             Biology         Mrs. Bolton     Gendry
Tuesday         08:00           101             Biology         Mrs. Bolton     Harry
Tuesday         09:00           101             Biology         Mrs. Bolton     Fred
Tuesday         09:00           101             Biology         Mrs. Bolton     Gendry
Tuesday         09:00           101             Biology         Mrs. Bolton     Harry
Tuesday         10:00           101             Chemistry       Mr. Cox         Davos
Tuesday         10:00           101             Chemistry       Mr. Cox         Euron
Tuesday         11:00           101             Chemistry       Mr. Cox         Davos
Tuesday         11:00           101             Chemistry       Mr. Cox         Euron
Tuesday         12:00                                      LUNCH TIME

Wednesday       12:00                                      LUNCH TIME

Thursday        12:00                                      LUNCH TIME

Friday          12:00                                      LUNCH TIME
*%