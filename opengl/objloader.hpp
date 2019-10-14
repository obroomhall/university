#pragma once

#include <stdio.h>
#include <array>
#include <vector>


// Very, VERY simple OBJ loader.
// Ignores everything except vertices and faces.
//
// Originally writen by Yongliang Yang using GLM,
// modified by Andrew Chinery to use Eigen, and
// modified by Christian Richardt to use plain C++11.
inline bool load_obj(const char* path, std::vector<std::array<float, 3>>& vertices, std::vector<std::array<int, 3>>& vertexIndices)
{
	printf("Loading OBJ file '%s' ... ", path);
	FILE* file = fopen(path, "r");
	if (file == NULL)
	{
		printf("Could not open file. Is the path correct?\n");
		return false;
	}

	while (true)
	{
		char lineHeader[128];

		// Read the first word of the line.
		int res = fscanf(file, "%s", lineHeader);
		if (res == EOF) break;  // EOF = End Of File. Quit the loop.

		// Parse the line.
		if (strcmp(lineHeader, "v") == 0)
		{
			std::array<float, 3> vertex;
			fscanf(file, "%f %f %f\n", &vertex[0], &vertex[1], &vertex[2]);
			vertices.push_back(vertex);
		}
		else if (strcmp(lineHeader, "f") == 0)
		{
			std::array<int, 3> vertexIndex;
			fscanf(file, "%i %i %i\n", &vertexIndex[0], &vertexIndex[1], &vertexIndex[2]);
			vertexIndices.push_back(vertexIndex);
		}
		else
		{
			// Probably a comment, eat up the rest of the line
			char ignored[1024];
			fgets(ignored, 1024, file);
		}
	}

	printf("Done.\n");
	return true;
}
