/*
Source: main.c
- Contains functions for performing the relaxation technique
- Separate functions for sequential and parallel implementations

My implementation of the relaxation technique:
- For each element in the working array, calculate the average of its four neighbours
- Store the result of the averaging in a separate array
- Compare the values in the working array to the average array
- If values differ by less than the given precision, then the array has 'settled'
- Update the working array with the values from the average array
- If the array is unsettled, then repeat the steps above
- If the array is settled, then end computation and return
*/

#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>

struct ThreadArgs;
void* relaxSharedFn(void *_args);
void relaxShared(double* workingArray, int dimension, int threads, double precision);

// Struct used to pass arguments to threads
typedef struct ThreadArgs {
	double* wrkArr;	// Pointer to the working array
	double* avgArr;	// Pointer to the average array
	int width;
	double precision;		// Precision to work to
	int locationCount;	// Number of locations in *locations
	int* locations;// Pointer to location values
} ThreadArgs;

// Flag tells threads whether array has settled to a precision
int settled;

// Barrier used to make threads wait before continuing execution
pthread_barrier_t barrier;

/*
Function: relaxSharedFn
- Performs relaxation technique on a specific elements in a 2D array
- Elements are assigned to a thread in the @ptrs argument
- Function should be run in parallel through the use of threads

@ptrs:	Pointer to an ArrayElements struct which contains arguments.
*/
void* relaxSharedFn(void* _args) {

	ThreadArgs *args = _args;
	int width = args->width;

	// Loop while working array is unsettled
	while (!settled) {

		/* Barrier ensures that all threads reach inside the while loop before
		the settled flag is updated. If a thread changes settled to TRUE, then
		some threads may miss the loop altogether. This is a race condition. */
		pthread_barrier_wait(&barrier);
		
		// If a thread finds the settled flag is FALSE, then change it to TRUE
		// If a thread has already updated the settled flag, then skip forward
		if (!settled) {
			settled = 1;
		}

		/* Must wait here to ensure that threads agree on the settled flag
		being TRUE, so that the above step does not interfere with the element
		comparison later on. */ 
		pthread_barrier_wait(&barrier);

		// Get and update the average at each assigned location
		for (int i = 0; i < args->locationCount; i++) {

			// Get the coordinates of the location
			int idx = args->locations[i];
			int uIdx = idx - width;
			int lIdx = idx - 1;
			int rIdx = idx + 1;
			int dIdx = idx + width;

			// Calculate average of neighbours (up, left, right and down)
			double u = args->wrkArr[uIdx];
			double l = args->wrkArr[lIdx];
			double r = args->wrkArr[rIdx];
			double d = args->wrkArr[dIdx];
			double avg = (u + l + r + d) / 4;

			// Update the average array with a mean average
			args->avgArr[idx] = avg;
			
			/* If the array is still settled, then compare the current value with the
			average value. If the values differ by more than the precision,	then the
			array is unsettled.	If the array is unsettled, then do not waste time
			comparing more elements. */
			if (settled && !compareDouble(args->wrkArr[idx], avg, args->precision)) {
				settled = 0;
			}
		}

		/* Must wait here to ensure all averages have been calculated before
		updating the working array. Updating the working array before this could
		cause a race condition where averages are calculated from incorrect values. */
		pthread_barrier_wait(&barrier);

		// Update working array elements from average array elements
		for (int i = 0; i < args->locationCount; i++) {
			int idx = args->locations[i];
			args->wrkArr[idx] = args->avgArr[idx];
		}

		/* It is safe to loop back from here because the settled flag has not been
		 modified since the last pthread_barrier_wait() */
	}

	// Free allocated memory
	free(args->locations);
	free(args);
}

/*
Function: relaxShared
- Sets up threads which perform the relaxation technique on a 2D array
- Uses threads and barriers for parallelisation
- Threads will be assigned as close to an equal share of locations for computation
	- e.g. for 4 threads, and 9 locations, assignment will be 3,2,2,2

@workingArray:	Pointer to a square 2D array which needs to be relaxed.
@dimension:		Dimension of the working array.
@threadCount:	Number of threads to use.
@precision:		Precision to work towards.
*/
void relaxShared(double* workingArray, int dimension, int threadCount, double precision) {

	// Assigns memory for an array used to store averages
	double* averageArray = initialiseArray(dimension, dimension);

	// Initialise the barrier, and create array of threads
	pthread_barrier_init(&barrier, NULL, threadCount);
	pthread_t threads[threadCount];
	
	int workingDimension = dimension-2; // Dimension minus the border elements
    int workingElementCount = workingDimension * workingDimension; // Number of elements to be updated
	int totalElementsAssigned = 0; // Number of elements already assigned to threads
	settled = 0; // Flag to determine whether array has settled down to the precision

	for (int i = 0; i < threadCount; i++) {
        
		// Get the number of elements to assign to this thread
		int elementCount = workingElementCount / threadCount;
        if (i < workingElementCount % threadCount) {
			elementCount++;
        }
		
		int* locations = malloc(sizeof(int)*elementCount);

		// Get and set locations of the elements to be passed to the thread
		for (int j = 0; j < elementCount; j++) {
			int x = (j+totalElementsAssigned) / workingDimension + 1;
			int y = (j+totalElementsAssigned) % workingDimension + 1;
			locations[j] = x * dimension + y;
		}

		totalElementsAssigned += elementCount;

		// Create the struct containing the thread arguments
		ThreadArgs* elements = (ThreadArgs*)malloc(sizeof(ThreadArgs) + sizeof(locations));
        elements->wrkArr = workingArray;
		elements->avgArr = averageArray;
		elements->width = dimension;
		elements->precision = precision;
		elements->locations = locations;
		elements->locationCount = elementCount;
		
		// Create the thread with the arguments above
		pthread_create(&threads[i], NULL, relaxSharedFn, elements);
	}
	
	// Join all the threads
	for (int i = 0; i < threadCount; i++) {
		pthread_join(threads[i], NULL);
	}

	// Destroy and free memory
	pthread_barrier_destroy(&barrier);
	free(averageArray);
}

