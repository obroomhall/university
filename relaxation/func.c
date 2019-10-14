void updateAverage(double* wrkArr, double* avgArr, int idx, int width);
void updateAverageByRow(double* wrkArr, double* avgArr, int row, int width);
int compareDouble(double d, double e, double precision);
double* initialiseArray(int height, int width);
void printArray(double *arr, int height, int width);
double* initialiseArrayFromArray(double* initialArray, int height, int width);
double* initialiseArrayRandom(int height, int width, int maxElement);
int compareArray(double* arr1, double* arr2, int height, int width, double precision);
double* ArrayFromString(char* str, int height, int width);
char* StringFromFile(char* fileName, int fileLen);
double* initialiseArrayOnes(int height, int width);

#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <math.h>

/*
Function: updateAverage
- Gets the average of neighbouring elements
- Updates avgArr with the calculated average

@wrkArr:	Pointer to the working array
@avgArr:	Pointer to the average array
@idx:		Index of element to update
@width:		Width of array
*/
void updateAverage(double* wrkArr, double* avgArr, int idx, int width) {
	double u = wrkArr[idx - width];
	double l = wrkArr[idx - 1];
	double r = wrkArr[idx + 1];
	double d = wrkArr[idx + width];
	avgArr[idx] = (u + l + r + d) / 4;
}

/*
Function: updateAverageByRow
- Calculates averages for an entire row

@wrkArr:	Pointer to the working array
@avgArr:	Pointer to the average array
@row:		Index of row to update
@width:		Width of array
*/
void updateAverageByRow(double* wrkArr, double* avgArr, int row, int width) {
	for (int col = 1; col < width - 1; col++) {
		updateAverage(wrkArr, avgArr, row * width + col, width);
	}
}

/*
Function: compareDouble
- Compares two double values to a given precision
- Returns TRUE if values differ by less than the precision
- Returns FALSE if values differ by more or the same as the precision

@d:			A double value for comparison
@e:			A double value for comparison
@precison:	Number of decimal places to compare by
*/
int compareDouble(double d, double e, double precision) {
	return (fabs(d - e) < precision);
}

/*
Function: getPrecision
- Returns the double precision from an int
- e.g. 1 -> 0.1, 2 -> 0.01

@precision: Precision to calculate to.
*/
double getPrecision(int precision) {
	return pow(10, -1 * precision);
}

/*
Function: initialiseArray
- Allocates memory for an array and then returns a pointer to the array

@height:	Height of the array to create.
@width:		Width of the array to create.
*/
double* initialiseArray(int height, int width) {
	return (double*)malloc(height * width * sizeof(double));
}

/*
Function: printArray
- Prints an array to stdout

@arr:		Pointer to an array.
@height:	Height of the array to create.
@width:		Width of the array to create.
*/
void printArray(double *arr, int height, int width) {
	for (int i = 0; i < height; i++) {
		for (int j = 0; j < width; j++) {
			printf("%f\t", arr[i * width + j]);
		}
		printf("\n");
	}
	printf("\n");
}

/*
Function: ArrayFromString
- Allocates space for an array
- Fills the array with elements from a delimited string

@str:		Delimited string.
@height:	Height of the array to create.
@width:		Width of the array to create.
*/
double* ArrayFromString(char* str, int height, int width) {

	double* arr = (double*)malloc(height * width * sizeof(double));

	for (int i = 0; i < height*width; i++)
	{
		arr[i] = str[i * 2] - '0';
	}

	return arr;
}

/*
Function: StringFromFile
- Returns a string read from a file

@fileName:	Name of file.
@fileLen:	Length of file.
*/
char* StringFromFile(char* fileName, int fileLen) {
	FILE *fp;
	fp = fopen(fileName, "r");
	char* str = (char*)malloc(fileLen*sizeof(char));
	fgets(str, fileLen, fp);
	fclose(fp);
	return str;
}

/*
Function: initialiseArrayFromArray
- Returns a copy of an input array

@initialArrray:	Input array.
@height:		Height of the array to create.
@width:			Width of the array to create.
*/
double* initialiseArrayFromArray(double* initialArray, int height, int width) {

	double* arr = initialiseArray(height, width);

	for (int i = 0; i < height; i++) {
		for (int j = 0; j < width; j++) {
			arr[i * width + j] = initialArray[i * width + j];
		}
	}

	return arr;
}

/*
Function: initialiseArrayRandom
- Returns a random array

@height:		Height of the array to create.
@width:			Width of the array to create.
@maxElement:	Largest integer to store in array.
*/
double* initialiseArrayRandom(int height, int width, int maxElement) {

	double* arr = initialiseArray(height, width);
	srand(time(NULL));

	for (int i = 0; i < height; i++) {
		for (int j = 0; j < width; j++) {
			arr[i * width + j] = rand() % maxElement;
		}
	}

	return arr;
}

/*
Function: initialiseArrayOnes
- Returns an array filled with 1s on the border, and 0s in the middle

@height:		Height of the array to create.
@width:			Width of the array to create.
*/
double* initialiseArrayOnes(int height, int width) {

	double* arr = initialiseArray(height, width);

	for (int i = 0; i < width; i++)
	{
		arr[i] = 1;
	}

	for (int i = width; i < width*(height-1); i++) {
		if (i % width != 0 && i % width != width - 1) {
			arr[i] = 0;
		}
		else {
			arr[i] = 1;
		}
	}

	for (int i = width * (height - 1); i < width * height; i++)
	{
		arr[i] = 1;
	}

	return arr;
}

/*
Function: compareArray
- Compares two arrays
- Returns 0 when array elements are not equal
- Returns 1 when array elements are equal

@arr1, arr2:	Arrays to compare.	
@height:		Height of the array to create.
@width:			Width of the array to create.
@precision:		Precision to compare against.
*/
int compareArray(double* arr1, double* arr2, int height, int width, double precision) {

	for (int i = 1; i < height - 1; i++) {
		for (int j = 1; j < width - 1; j++) {
			if (!compareDouble(arr1[i * width + j], arr2[i * width + j], precision)) {
				return 0;
			}
		}
	}
	return 1;
}