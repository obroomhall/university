void relaxSeq(double* workingArray, int dimension, double precision);

#include <stdlib.h>

/*
Function: relaxSeq
- Performs a sequential relaxation on a 2D array

@workingArray:	Pointer to a square 2D array which needs to be relaxed.
@dimension:		Dimension of the working array.
@precision:		Precision to work towards.
*/
void relaxSeq(double* workingArray, int dimension, double precision) {

	double* averageArray = initialiseArray(dimension, dimension);

	int settled = 0;
	while (!settled) {

		settled = 1;

		// Get and set average
		for (int i = 1; i < dimension - 1; i++) {
			for (int j = 1; j < dimension - 1; j++) {
				updateAverage(workingArray, averageArray, i * dimension + j, dimension);
			}
		}

		// Find out if settled, and update average array
		for (int i = 1; i < dimension - 1; i++) {
			for (int j = 1; j < dimension - 1; j++) {

				int idx = i * dimension + j;

				if (settled && !compareDouble(workingArray[idx], averageArray[idx], precision)) {
					settled = 0;
				}

				workingArray[idx] = averageArray[idx];
			}
		}
	}

	free(averageArray);
}

