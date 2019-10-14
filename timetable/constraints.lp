% Gets the number of students taking a particular unit.
taking(N, U) :- unit(U), N = #count { Student : takes(U, Student) }.

% Counts the number of rooms needed per lecture.
% Based on room capacity and number of students taking each unit.
% For rounding up, add the divisor (room capacity) minus one to the dividend (number of students).
roomsNeeded(N, U) :- A = Students + capacity - 1, N = A / capacity, taking(Students, U).


% Generates the correct number of lectures according to the number of contact hours.
% Also ensures lecture times for the same unit are different.
N #count { lectureSlot(D, T) : timeBooked(U, lectureSlot(D, T)) : lectureSlot(D, T) } N :- contactHours(H, U), roomsNeeded(A, U), N = H * A.

% Assigns the number of rooms needed for each lecture.
1 { roomAssigned(R, timeBooked(U, lectureSlot(D, T))) : room(R) } 1 :- timeBooked(U, lectureSlot(D, T)).


% Restricts rooms from being assigned to different units at the same time.
:- roomAssigned(R, timeBooked(U1, Slot)), roomAssigned(R, timeBooked(U2, Slot)), U1 != U2.

% Restricts a unit from being taught at the same time in different rooms.
:- roomAssigned(R1, timeBooked(U, Slot)), roomAssigned(R2, timeBooked(U, Slot)), R1 != R2.


% Assigns each student to the units they take.
N #count { S: studentAssigned(S, timeBooked(U, Slot)) : takes(U, S) } N :- roomAssigned(R, timeBooked(U, Slot)), taking(N, U).

lecture(D, T, R, U, L, S) :- roomAssigned(R, UnitTime), studentAssigned(S, UnitTime), UnitTime = timeBooked(U, lectureSlot(D, T)), teaches(U, L).
#show lecture/6.

% Lecturer cannot teach when they have other commitments.
:- lecture(D, T, R, U, L, S), commitment(lectureSlot(D, T), L).

% Try to give lecturers their preferred teaching times.
%:~ lecture(D, T, R, U, L, S), preferedTime(lectureSlot(D, T), L). [10]
%#maximize { A@1 : preferedTime(lectureSlot(D, T), L), lecture(D, T, R, U, L, S), A=lecturePreferred(lectureSlot(D, T), L) }.
%#show lecturePreferred/2.