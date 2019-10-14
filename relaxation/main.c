int compareSeqToPar(double* arr, int dimension, int threads, double precision);
int compareSeqToDist(double* initialArray, int dimension, int threads, double precision);
int compareExpectedToResultSeq(double* initialArray, double* expectedResult, int dimension, double precision);

int test_iterative(int(*t)(int, int), int tests, int varMin, int varMax, char* name);
int test(int(*t)(int, int), int tests, int var, char* name);
int isSuccess(int success, int tests, char* name, int var);

int test_withVarShared(int tests, int dimension, int threads, int precision, int maxElement);
int test_dimension_shared(int tests, int dimension);
int test_precision_shared(int tests, int precision);
int test_threads_shared(int tests, int threads);

int test_withVarDist(int tests, int dimension, int threads, int precision, int maxElement);
int test_dimension_dist(int tests, int dimension);
int test_precision_dist(int tests, int precision);

int test_expected_1();
int test_expected_2();
int test_expected_3();
int all(int tests, int iterations);
	
#include <stdio.h>
#include <string.h>
#include "func.c"
#include "shared.c"
#include "seq.c"
#include "dist.c"

int main(int argc, char *argv[]) {

	// Get remote connection between processes
	int rc = MPI_Init(NULL, NULL);
	if (rc != MPI_SUCCESS) {
		printf("Error starting MPI program\n");
		MPI_Abort(MPI_COMM_WORLD, rc);
		return 1;
	}

	int processRank;
	MPI_Comm_rank(MPI_COMM_WORLD, &processRank);

	if (argc == 1) {
		test(all, 8, 10, "all");
	}
	else {

		if (argc != 7) {
			if (processRank == 0) {
				printf("incorrect arguments provided\n");
				printf("[source] [print?] [precision] [threads] [dimension] [array file]\n");
			}
			return 1;
		}
		
		int print = (strcmp(argv[2], "y") == 0);
		double precision = atof(argv[3]);
		int threads = atoi(argv[4]);
		int dimension = atoi(argv[5]);

		if (processRank == 0) printf("Building array.\n");

		double* workingArray;
		if (strcmp(argv[6], "ones") == 0) {
			workingArray = initialiseArrayOnes(dimension, dimension);
		}
		else {
			char* arrayStr = StringFromFile(argv[6], dimension*dimension * 2);
			workingArray = ArrayFromString(arrayStr, dimension, dimension);
		}

		if (processRank == 0) printf("Begin.\n");

		if (print && processRank == 0) {
			printf("Initial Array:\n");
			printArray(workingArray, dimension, dimension);
		}

		if (strcmp(argv[1], "seq") == 0 && processRank == 0)
		{
			relaxSeq(workingArray, dimension, precision);
		}
		else if (strcmp(argv[1], "shared") == 0 && processRank == 0)
		{
			relaxShared(workingArray, dimension, threads, precision);
		}
		else if (strcmp(argv[1], "dist") == 0)
		{
			relaxDist(workingArray, dimension, precision);
		}
		else
		{
			if (processRank == 0) printf("Could not find source file for %s\n", argv[1]);
			return 4;
		}

		if (print && processRank == 0) {
			printf("Final Array:\n");
			printArray(workingArray, dimension, dimension);
		}

		if (processRank == 0) printf("Complete.\n");
	}

	MPI_Finalize();
	return 0;
}

int all(int tests, int testIterations) {

	int processRank;
	MPI_Comm_rank(MPI_COMM_WORLD, &processRank);

	int success = 0;

	// Only need one process for these tests
	if (processRank == 0) {
		success += isSuccess(test_expected_1(), 1, "test_expected_1", -1);
		success += isSuccess(test_expected_2(), 1, "test_expected_2", -1);
		success += isSuccess(test_expected_3(), 1, "test_expected_3", -1);

		success += test_iterative(test_dimension_shared, testIterations, 3, 20, "test_dimension_shared");
		success += test_iterative(test_precision_shared, testIterations, 0, 10, "test_precision_shared");
		success += test_iterative(test_threads_shared, testIterations, 1, 16, "test_threads_shared");
	}
	
	int rootSuccess = 0;

	rootSuccess += test_iterative(test_dimension_dist, testIterations, 3, 20, "test_dimension_dist");
	rootSuccess += test_iterative(test_precision_dist, testIterations, 0, 10, "test_precision_dist");
	
	if (processRank == 0) {
		success += rootSuccess;
		return success;
	}
	else {
		return -1;
	}
}

int compareSeqToPar(double* initialArray, int dimension, int threads, double precision) {

	double* seqArray = initialiseArrayFromArray(initialArray, dimension, dimension);
	double* parArray = initialiseArrayFromArray(initialArray, dimension, dimension);

	relaxSeq(seqArray, dimension, precision);
	relaxShared(parArray, dimension, threads, precision);

	return compareArray(seqArray, parArray, dimension, dimension, precision);
}

int compareSeqToDist(double* initialArray, int dimension, int threads, double precision) {

	int processRank;
	MPI_Comm_rank(MPI_COMM_WORLD, &processRank);

	double* seqArray = initialiseArrayFromArray(initialArray, dimension, dimension);
	double* distArray = initialiseArrayFromArray(initialArray, dimension, dimension);

	relaxSeq(seqArray, dimension, precision);
	relaxDist(distArray, dimension, precision);

	if (processRank == 0) {
		/*printf("Initial Array:\n");
		printArray(initialArray, dimension, dimension);
		printf("Seq Array:\n");
		printArray(seqArray, dimension, dimension);
		printf("Dist Array:\n");
		printArray(distArray, dimension, dimension);*/
		return compareArray(seqArray, distArray, dimension, dimension, precision);
	}
	else {
		return -1;
	}
}

int test_dimension_shared(int tests, int dimension) {
	return test_withVarShared(tests, dimension, 1, 5, 10);
}

int test_threads_shared(int tests, int threads) {
	return test_withVarShared(tests, 5, threads, 5, 10);
}

int test_precision_shared(int tests, int precision) {
	return test_withVarShared(tests, 5, 1, precision, 10);
}

int test_dimension_dist(int tests, int dimension) {
	return test_withVarDist(tests, dimension, 1, 5, 10);
}

int test_precision_dist(int tests, int precision) {
	return test_withVarDist(tests, 5, 1, precision, 10);
}

int test(int(*t)(int, int), int tests, int var, char* name) {

	int success = (*t)(tests, var);
	return isSuccess(success, tests, name, var);
}

int test_withVarShared(int tests, int dimension, int threads, int intPrecision, int maxElement) {

	int success = 0;

	double precision = getPrecision(intPrecision);
	double* randomArray = initialiseArrayRandom(dimension, dimension, maxElement);

	for (int i = 0; i < tests; i++) {
		if (compareSeqToPar(randomArray, dimension, threads, precision) > 0) {
			success++;
		}
	}

	return success;
}

int test_withVarDist(int tests, int dimension, int threads, int intPrecision, int maxElement) {

	int success = 0;

	double precision = getPrecision(intPrecision);
	double* randomArray = initialiseArrayRandom(dimension, dimension, maxElement);

	for (int i = 0; i < tests; i++) {
		success += compareSeqToDist(randomArray, dimension, threads, precision);
	}

	return success;
}

int test_iterative(int(*t)(int, int), int tests, int varMin, int varMax, char* name) {
	
	int success = 0;
	
	for (int i = varMin; i < varMax+1; i++) {
		success += test((*t), tests, i, name);
	}

	return isSuccess(success, varMax-varMin+1, name, -1);
}

int isSuccess(int success, int tests, char* name, int var) {

	int processRank;
	MPI_Comm_rank(MPI_COMM_WORLD, &processRank);

	if (processRank == 0) {
		if (var < 0) {
			printf("%d/%d tests passed in %s\n\n", success, tests, name);
		}
		else {
			printf("- %d/%d tests passed in %s_%d\n", success, tests, name, var);
		}
		return (success == tests);
	}
	else {
		return -1;
	}
	
}

int compareExpectedToResultSeq(double* initialArray, double* expectedResult, int dimension, double precision) {

	double* seqArray = initialiseArrayFromArray(initialArray, dimension, dimension);
	relaxSeq(seqArray, dimension, precision);
	return compareArray(seqArray, expectedResult, dimension, dimension, precision);
}

int test_expected_1() {

	int dimension = 5;

	double _initialArray[25] = {
		7,5,6,1,3,
		7,4,9,0,1,
		8,3,2,0,4,
		9,2,6,7,1,
		3,2,0,6,2
	};

	double _expectedArray[25] = {
		7.000000, 5.000000, 6.000000, 1.000000, 3.000000,
		7.000000, 5.571425, 4.589287, 2.535710, 1.000000,
		8.000000, 5.696430, 4.249992, 3.553572, 4.000000,
		9.000000, 4.964282, 3.160715, 3.428568, 1.000000,
		3.000000, 2.000000, 0.000000, 6.000000, 2.000000
	};

	double* initialArray = initialiseArray(dimension, dimension);
	double* expectedArray = initialiseArray(dimension, dimension);

	for (int i = 0; i < dimension; i++) {
		for (int j = 0; j < dimension; j++) {
			initialArray[i * dimension + j] = _initialArray[i * dimension + j];
			expectedArray[i * dimension + j] = _expectedArray[i * dimension + j];
		}
	}

	double precision = getPrecision(5);
	int threads = 4;
	return compareExpectedToResultSeq(initialArray, expectedArray, dimension, precision);
}

int test_expected_2() {

	int dimension = 4;

	double _initialArray[16] = {
		1,2,3,4,
		5,6,7,8,
		9,10,11,12,
		13,14,15,16
	};

	double _expectedArray[16] = {
		1,2,3,4,
		5,6,7,8,
		9,10,11,12,
		13,14,15,16
	};

	double* initialArray = initialiseArray(dimension, dimension);
	double* expectedArray = initialiseArray(dimension, dimension);

	for (int i = 0; i < dimension; i++) {
		for (int j = 0; j < dimension; j++) {
			initialArray[i * dimension + j] = _initialArray[i * dimension + j];
			expectedArray[i * dimension + j] = _expectedArray[i * dimension + j];
		}
	}

	double precision = getPrecision(5);
	int threads = 4;
	return compareExpectedToResultSeq(initialArray, expectedArray, dimension, precision);
}

int test_expected_3() {

	int dimension = 4;

	double _initialArray[16] = {
		6,5,7,8,
		2,5,2,5,
		4,4,9,10,
		1,1,3,3
	};

	double _expectedArray[16] = {
		6.000000, 5.000000, 7.000000, 8.000000,
		2.000000, 4.000009, 5.374994, 5.000000,
		4.000000, 3.624994, 5.500009, 10.000000,
		1.000000, 1.000000, 3.000000, 3.000000
	};

	double* initialArray = initialiseArray(dimension, dimension);
	double* expectedArray = initialiseArray(dimension, dimension);

	for (int i = 0; i < dimension; i++) {
		for (int j = 0; j < dimension; j++) {
			initialArray[i * dimension + j] = _initialArray[i * dimension + j];
			expectedArray[i * dimension + j] = _expectedArray[i * dimension + j];
		}
	}

	double precision = getPrecision(5);
	int threads = 4;
	return compareExpectedToResultSeq(initialArray, expectedArray, dimension, precision);
}

