/*
Source: dist.c
- Contains functions for performing the relaxation technique in a distributed architecture

My implementation of the relaxation technique:
- For each process, calculate averages for upper and lower rows
- Send/Receive upper and lower rows to/from neighbouring processes asynchronously
- Calculate averages for all other rows
- Calculate whether array is settled
- Send/Receive settled value to/from all other processes
- If some processors have not settled, repeat from first step
- Else, send settled array to root processor
*/

int* getRowCounts(int processCount, int workingDimension);
int* getStartRows(int processCount, int rows[]);
void relaxDist(double* initialArray, int dimension, double precision);
void computeDist(double* workingArray, double* averageArray, int height, int width, double precision, int processRank, int processCount);

#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <mpi.h>

// A communicator used to distinguish which processors to use
MPI_Comm MPI_COMM_WORK;

// Gets the row count for a processor rank
int* getRowCounts(int processCount, int dimension) {

	int workingDimension = dimension - 2;
	int extraRows = workingDimension - (workingDimension / processCount * processCount);

	// Create array for the counts of rows per process
	int* rows = malloc(sizeof(int) * processCount);
	for (int i = 0; i < processCount; i++) {

		rows[i] = workingDimension / processCount;
		if (i < extraRows) rows[i]++;
	}

	return rows;
}

// Gets the displacement of array elements for a processor rank
int* getDispls(int processCount, int counts[], int width) {

	int* displs = malloc(sizeof(int) * processCount);
	for (int i = 0; i < processCount; i++) {
		displs[i] = width;
		for (int j = 0; j < i; j++) {
			displs[i] += counts[j];
		}
	}

	return displs;
}

// Splits the workload equally across all processes
void relaxDist(double* initialArray, int dimension, double precision) {

	// Gets the rank of this processor
	int processRank;
	MPI_Comm_rank(MPI_COMM_WORLD, &processRank);

	// Only use processor if it can be assigned at least 1 line
	int useProcess = (processRank < dimension - 2);

	// Splits communicator into 2; processors to use and to not use
	int processCount;
	MPI_Comm_split(MPI_COMM_WORLD, useProcess, processRank, &MPI_COMM_WORK);
	MPI_Comm_size(MPI_COMM_WORK, &processCount);

	if (useProcess) {

		// Gets the row count assigned to this processor
		int* rows = getRowCounts(processCount, dimension);
		
		// Gets the dimensions of the working array
		int height = rows[processRank] + 2;
		int width = dimension;

		// Gets the working element count
		int recvCounts[processCount];
		for (int i = 0; i < processCount; i++) {
			recvCounts[i] = rows[i] * width;
		}

		// Gets the displacement indices for the working array
		int* displs = getDispls(processCount, recvCounts, width);
		
		// Points to the first element to use in the array
		double* workingArray = &initialArray[displs[processRank] - width];

		// Array used for averaging elements
		double* averageArray = initialiseArray(height, width);

		// Compute averages until all processors are settled
		computeDist(workingArray, averageArray, height, width, precision, processRank, processCount);

		// Gather all the computed averages to the base process
		MPI_Gatherv(&workingArray[width], recvCounts[processRank], MPI_DOUBLE,
			initialArray, recvCounts, displs, MPI_DOUBLE, 0, MPI_COMM_WORK);

		free(averageArray);
	}
}

void computeDist(double* workingArray, double* averageArray, int height, int width, double precision,
	int processRank, int processCount) {

	int updateUpper = (processRank != 0); // Top row for the first process stays fixed
	int updateLower = (processRank != processCount - 1); // Bottom row for the last process stays fixed
	MPI_Request reqSendTop, reqRecTop, reqSendBot, reqRecBot, reqSendSettled[processCount], reqRecSettled[processCount];
	
	int allSettled = 0;
	int settled[processCount];
	for (int i = 0; i < processCount; i++) {
		settled[i] = 0;
	}

	while (!allSettled) {

		/*
			Top and bottom rows are computed first, then sent asynchronously to
			neighbouring processors. Carry on computing other rows whilst waiting
			for messages to be sent and received.

			TAG = 0 for messages sending from upper to lower
			TAG = 1 for messages sending from lower to upper
		 	TAG = 2 for messages regarding settling
		*/

		// Calculate averages on top row
		updateAverageByRow(workingArray, averageArray, 1, width);

		// Send and receive top row asynchronously
		if (updateUpper) {
			MPI_Isend(&averageArray[width + 1], width - 2, MPI_DOUBLE, processRank - 1, 0, MPI_COMM_WORK, &reqSendTop);
			MPI_Irecv(&averageArray[1], width - 2, MPI_DOUBLE, processRank - 1, 1, MPI_COMM_WORK, &reqRecTop);
		}

		// Calculate averages on bottom row
		if (height - 2 > 1) {
			updateAverageByRow(workingArray, averageArray, height - 2, width);
		}

		int lowInnerIndex = (height - 1) * width + 1;

		// Send and receive bottom row asynchronously
		if (updateLower) {
			MPI_Isend(&averageArray[lowInnerIndex - width], width - 2, MPI_DOUBLE, processRank + 1, 1, MPI_COMM_WORK, &reqSendBot);
			MPI_Irecv(&averageArray[lowInnerIndex], width - 2, MPI_DOUBLE, processRank + 1, 0, MPI_COMM_WORK, &reqRecBot);
		}

		// Calculate averages for middle rows
		for (int i = 2; i < height - 2; i++) {
			updateAverageByRow(workingArray, averageArray, i, width);
		}

		// Check array to see if it has settled
		settled[processRank] = 1;
		for (int i = width; i < width * (height - 1); i++)
		{
			if (i % width != 0 && i % width != width - 1) {
				if (!compareDouble(workingArray[i], averageArray[i], precision)) {
					settled[processRank] = 0;
					break;
				}
			}
		}

		// Send and receive settled flags to all other processors asynchronously
		for (int i = 0; i < processCount; i++) {
			if (i != processRank) {

				MPI_Isend(&settled[processRank], 1, MPI_INT, i, 2, MPI_COMM_WORK, &reqSendSettled[i]);
				MPI_Irecv(&settled[i], 1, MPI_INT, i, 2, MPI_COMM_WORK, &reqRecSettled[i]);
			}
		}

		// Update the working array with computed averages
		for (int i = 1; i < height - 1; i++) {
			for (int j = 1; j < width - 1; j++) {

				int idx = i * width + j;
				workingArray[idx] = averageArray[idx];
			}
		}

		/*
			Must ensure that the top and bottom rows have been updated. If they
			have not been updated then we run into a possible race condition.
			Future averages could then be altered by late messages.

			It is okay to wait here because we assume that for large enough arrays
			there will be little to no waiting time, as messages should have been
			received during the computation of the middle rows.
		*/

		// Ensure bottom row has been updated
		if (updateLower) {

			MPI_Wait(&reqSendBot, MPI_STATUS_IGNORE);
			MPI_Wait(&reqRecBot, MPI_STATUS_IGNORE);

			for (int i = lowInnerIndex; i < lowInnerIndex + width - 1; i++) {
				workingArray[i] = averageArray[i];
			}
		}

		// Ensure top row has been updated
		if (updateUpper) {

			MPI_Wait(&reqRecTop, MPI_STATUS_IGNORE);
			MPI_Wait(&reqSendTop, MPI_STATUS_IGNORE);

			for (int i = 1; i < width - 1; i++) {
				workingArray[i] = averageArray[i];
			}
		}

		// Ensure settled flags have been sent and received
		for (int i = 0; i < processCount; i++) {
			if (i != processRank) {

				MPI_Wait(&reqSendSettled[i], MPI_STATUS_IGNORE);
				MPI_Wait(&reqRecSettled[i], MPI_STATUS_IGNORE);
			}
		}

		// Check whether all processors have settled
		allSettled = 1;
		for (int i = 0; i < processCount; i++) {
			if (!settled[i]) {
				allSettled = 0;
				break;
			}
		}
	}
}
