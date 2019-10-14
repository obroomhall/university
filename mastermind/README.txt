This program is an adaption of Mastermind, a board game
invented by Mordecai Meirowitz. The aim of the game is for
the codebreaker to correctly identify the colour and position
of the hidden pegs chosen by the codemaker.

The codemaker can use between 3-8 colours, with between 3-8
pegs, to make his hidden code. The codebreaker must then try
to guess the code within 12 test cases. The codebreaker will
be awarded a black key peg for each test peg in the correct
position and correct colour, and a white key peg for each
test peg of the correct colour, but wrong position.

The codebreaker wins the game by recieving the same number of
black key pegs as the number of pegs in the hidden code,
meaning they have correctly identified the pattern made by
the codemaker. If after 12 attempts the codebreaker has not
won, then the codemaker wins.

When asked to enter your attempt for the test case, please
enter your attempt in the form 'bgcb', where each letter
corresponds to an available colour, and the length of the
attempt is equal to the number of pegs previously selected.

You can fast run the program by using 4-5 command line
arguments. Running the program with the incorrect number of
arguments, or indeed incorrect arguments themselves will
cause an error. The two possible ways of using this function
are:

                  1     2     3     4         5
java Mastermind [y/n] [4-7] [4-7]   1   [hidden code]
java Mastermind [y/n] [4-7] [4-7] [2/3]

1. Display README.txt
2. Define number of colours used
3. Define number of pegs used
4. Define the mode
5. Define the hidden code to be broken. (Only when mode 1).

Have fun playing!