#ifdef __APPLE__
#include <OpenGL/gl.h>  // The GL header file.
#include <GLUT/glut.h>  // The GL Utility Toolkit (glut) header.
#else
#ifdef _WIN32
#include <windows.h>
#endif
#include <GL/gl.h>      // The GL header file.
#include "glut.h"       // The GL Utility Toolkit (glut) header (boundled with this program).
#endif

#define _USE_MATH_DEFINES
#include <math.h>       // For mathematic operations.
#include <array>

#include "objloader.hpp"

// Global variable for current rendering mode.
char renderMode;
char prevRenderMode;

std::vector<std::array<float, 3>> vertices;
std::vector<std::array<int, 3>> vertexIndices;

GLfloat cubeVertex[24] = {
	1.0f, 1.0f, 1.0f,
	-1.0f, 1.0f, 1.0f,
	-1.0f, -1.0f, 1.0f,
	1.0f, -1.0f, 1.0f,
	1.0f, 1.0f, -1.0f,
	-1.0f, 1.0f, -1.0f,
	-1.0f, -1.0f, -1.0f,
	1.0f, -1.0f, -1.0f
};

// Front, Back, Right, Left, Top, Bottom
GLint cubeFaces[6][4] = { {0, 1, 2, 3}, {4, 5, 6, 7}, {0, 4, 7, 3}, {1, 5, 6, 2}, {0, 1, 5, 4}, {3, 2, 6, 7} };
GLint cubeEdges[12][2] = { {0, 1}, {1, 2}, {2, 3}, {3, 0}, {4, 5}, {5, 6}, {6, 7}, {7, 4}, {0, 4}, {1, 5}, {2, 6}, {3, 7} };

GLfloat normals[6][3] = { {0.0f,0.0f,1.0f }, {0.0f,0.0f,-1.0f }, {1.0f,0.0f,0.0f }, {-1.0f,0.0f,0.0f }, {0.0f,1.0f,0.0f }, {0.0f,-1.0f,0.0f } };

GLfloat axisPoints[9] = {
	5.0f, 0.0f, 0.0f,
	0.0f, 5.0f, 0.0f,
	0.0f, 0.0f, 5.0f
};

GLfloat *v1 = &cubeVertex[0];
GLfloat *v2 = &cubeVertex[3];
GLfloat *v3 = &cubeVertex[6];
GLfloat *v4 = &cubeVertex[9];
GLfloat *v5 = &cubeVertex[12];
GLfloat *v6 = &cubeVertex[15];
GLfloat *v7 = &cubeVertex[18];
GLfloat *v8 = &cubeVertex[21];

/*const std::array<GLfloat, 3> RED = { 1.0f, 0.0f, 0.0f };
const std::array<GLfloat, 3> YELLOW = { 1.0f, 1.0f, 0.0f };
const std::array<GLfloat, 3> GREEN = { 0.0f, 1.0f, 0.0f };
const std::array<GLfloat, 3> CYAN = { 0.0f, 1.0f, 1.0f };
const std::array<GLfloat, 3> BLUE = { 0.0f, 0.0f, 1.0f };
const std::array<GLfloat, 3> MAGENTA = { 1.0f, 0.0f, 1.0f };
const std::array<GLfloat, 3> WHITE = { 1.0f, 1.0f, 1.0f };
const std::array<GLfloat, 3> BLACK = { 0.0f, 0.0f, 0.0f };
const std::array<GLfloat, 3> GREY = { 0.5f, 0.5f, 0.5f };*/

const GLfloat colours[6][3] = {
	{ 1.0f, 0.0f, 0.0f },	// RED
	{ 1.0f, 1.0f, 0.0f },	// YELLOW
	{ 0.0f, 1.0f, 0.0f },	// GREEN
	{ 0.0f, 1.0f, 1.0f },	// CYAN
	{ 0.0f, 0.0f, 1.0f },	// BLUE
	{ 1.0f, 0.0f, 1.0f }	// MAGENTA
};

// Scene initialisation.
void InitGL(GLvoid)
{
	glShadeModel(GL_SMOOTH);               // Enable smooth shading.
	glClearColor(0.0f, 0.0f, 0.0f, 1.0f);  // Black background.
	glClearDepth(1.0f);                    // Depth buffer setup.
	glEnable(GL_DEPTH_TEST);               // Enables depth testing.
	glDepthFunc(GL_LEQUAL);                // The type of depth testing to do.
	glEnable(GL_COLOR_MATERIAL);
	glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
}


void idle(void)
{
	glutPostRedisplay();  // Trigger display callback.
}

// p = start point, q = end point
void drawLine(const GLfloat colour[3], GLfloat p[3], GLfloat q[3]) {
	glBegin(GL_LINES);
	glColor3fv(colour);
	glVertex3fv(p);
	glVertex3fv(q);
	glEnd();
}
/*void drawLine(std::array<GLfloat, 3> colour, GLfloat p[3], char dir, GLfloat distance) {
	GLfloat q[3] = p;

	switch (dir) {
	case 'x':
		q[0] = q[0] + distance; break;
	case 'y':
		q[1] = q[1] + distance; break;
	case 'z':
		q[2] = q[2] + distance; break;
	default:
		return;
	}

	drawLine(colour, p, q);
}*/

void drawAxis(void) {
	glBegin(GL_LINES);

	glColor3fv(colours[0]);
	glVertex3f(0.0f, 0.0f, 0.0f);
	glVertex3f(axisPoints[0], axisPoints[1], axisPoints[2]);

	glColor3fv(colours[2]);
	glVertex3f(0.0f, 0.0f, 0.0f);
	glVertex3f(axisPoints[3], axisPoints[4], axisPoints[5]);

	glColor3fv(colours[4]);
	glVertex3f(0.0f, 0.0f, 0.0f);
	glVertex3f(axisPoints[6], axisPoints[7], axisPoints[8]);

	glEnd();
}

void drawQuads(void) {
	glBegin(GL_QUADS);
	for (int i = 0; i < 6; i++) {
		glColor3fv(colours[i]);
		glNormal3fv(normals[i]);
		for (int j = 0; j < 4; j++) {
			glVertex3fv(&cubeVertex[cubeFaces[i][j] * 3]);
		}
	}
	glEnd();
}

void drawVertices(void) {
	glBegin(GL_POINTS);
	glColor3f(0.0f, 1.0f, 0.0f);

	for (int i = 0; i < 24; i = i + 3) {
		glVertex3f(cubeVertex[i], cubeVertex[i + 1], cubeVertex[i + 2]);
	}

	glEnd();
	glPointSize(5);
}

void drawEdges(void) {
	for (int i = 0; i < 12; i++) {
		drawLine(colours[4], &cubeVertex[cubeEdges[i][0]*3], &cubeVertex[cubeEdges[i][1]*3]);
	}
}

void loadObj(const char * path) {
	vertices.clear();
	vertexIndices.clear();
	load_obj(path, vertices, vertexIndices);
}

void objDrawFaces() {
	glBegin(GL_TRIANGLES);
	glColor3fv(colours[0]);
	for (int i = 0; i < vertexIndices.size(); i++) {
		glVertex3f(vertices[vertexIndices[i][0]][0], vertices[vertexIndices[i][1]][1], vertices[vertexIndices[i][2]][2]);
	}
	glEnd();
}

void multiplyMatrices(const GLfloat m[3][3], char dir, bool axis) {
	/*if (axis) {
		for (int n = 0; n < 9; n = n + 3) {
			GLfloat temp[3] = { 0.0f, 0.0f, 0.0f };
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					temp[i] = temp[i] + m[i][j] * axisPoints[n + j];
				}
			}
			axisPoints[n] = temp[0];
			axisPoints[n+1] = temp[1];
			axisPoints[n+2] = temp[2];
		}
	}

	GLfloat tempAxis[9];
	for (int i = 0; i < 9; i++) {
		tempAxis[i] = axisPoints[i];
		if (i == 0 || i == 4 || i == 8) {
			axisPoints[i] = 5.0f;
		}
		else {
			axisPoints[i] = 0.0f;
		}
	}*/

	for (int n = 0; n < 24; n = n + 3) {
		GLfloat temp[3] = { 0.0f, 0.0f, 0.0f };
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				temp[i] = temp[i] + m[i][j] * cubeVertex[n+j];
			}
		}
		cubeVertex[n] = temp[0];
		cubeVertex[n + 1] = temp[1];
		cubeVertex[n + 2] = temp[2];
	}


	
	/*for (int i = 0; i < 9; i++) {
		axisPoints[i] = tempAxis[i];
	}*/

}

void rotate(char dir, char pos, bool axis) {
	
	GLfloat theta;
	if (pos == '-') {
		theta = -M_PI / 30;
	}
	else {
		theta = M_PI / 30;
	}
	
	GLfloat Rx[3][3] = { {1.0f, 0.0f, 0.0f}, {0.0f, cos(theta), -sin(theta)}, {0.0f, sin(theta), cos(theta)} };
	GLfloat Ry[3][3] = { {cos(theta), 0.0f, sin(theta)}, {0.0f, 1.0f, 0.0f}, {-sin(theta), 0.0f, cos(theta)} };
	GLfloat Rz[3][3] = { {cos(theta), -sin(theta), 0.0f}, {sin(theta), cos(theta), 0.0f}, {0.0f, 0.0f, 1.0f} };

	switch (dir)
	{
	case 'x':
		multiplyMatrices(Rx, dir, axis);
		break;
	case 'y':
		multiplyMatrices(Ry, dir, axis);
		break;
	case 'z':
		multiplyMatrices(Rz, dir, axis);
		break;
	default:
		break;
	}
}

void display(void)
{
	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

	glLoadIdentity();
	// Set the camera.
	gluLookAt(5.0f, 5.0f, 10.0f,
		0.0f, 0.0f, 0.0f,
		0.0f, 1.0f, 0.0f);

	/*glEnable(GL_LIGHTING);
	glEnable(GL_LIGHT0);
	GLfloat lightpos[] = { .5, 1., 1., 0. };
	glLightfv(GL_LIGHT0, GL_POSITION, lightpos);*/

	drawAxis();

	// Different render modes.
	switch(renderMode) {

		case 'f': // to display faces
		{
			drawQuads();
			prevRenderMode = 'f';
			break;
		}

		case 'v': // to display points
		{
			drawVertices();
			prevRenderMode = 'v';
			break;
		}

		case 'e': // to display edges
		{
			drawEdges();
			prevRenderMode = 'e';
			break;
		}

		case 'b':
		{
			const char *path = "bunny.obj";
			loadObj(path);
			objDrawFaces();
			prevRenderMode = 'b';
		}

		case 'x':
			rotate('x', '+', false);
			renderMode = prevRenderMode;
			break;
		case 'y':
			rotate('y', '+', false);
			renderMode = prevRenderMode;
			break;
		case 'z':
			rotate('z', '+', false);
			renderMode = prevRenderMode;
			break;
	}

	// TO DO: Draw Cartesian coordinate system as lines.

	glutSwapBuffers();
}


// The reshape function sets up the viewport and projection.
void reshape(int width, int height)
{
	// Prevent a divide by zero error by making height equal to 1
	if (height == 0)
		height = 1;

	glViewport(0, 0, width, height);
	glMatrixMode(GL_PROJECTION);
	glLoadIdentity();

	// Need to calculate the aspect ratio of the window for gluPerspective.
	gluPerspective(45.0f, (GLfloat)width / (GLfloat)height, 0.1f, 100.0f);

	// Return to ModelView mode for future operations.
	glMatrixMode(GL_MODELVIEW);
	glLoadIdentity();
}


// Callback for standard keyboard presses.
void keyboard(unsigned char key, int x, int y)
{
	switch (key)
	{
		// Exit the program when escape is pressed
		case 27:
			exit(0);
			break;

			// Switch render mode.
		case 'v': renderMode = 'v'; break;  // vertices
		case 'e': renderMode = 'e'; break;  // edges
		case 'f': renderMode = 'f'; break;  // faces
		case 'x': renderMode = 'x'; break;	// rotate x
		case 'y': renderMode = 'y'; break;	// rotate y
		case 'z': renderMode = 'z'; break;	// rotate z
		case 'b': renderMode = 'b'; break;	// show bunny

		default:
			break;
	}

	glutPostRedisplay();
}


// Arrow keys need to be handled in a separate function from other keyboard presses.
void arrow_keys(int a_keys, int x, int y)
{
	switch (a_keys)
	{
		case GLUT_KEY_UP:
			rotate('x', '+', true);
			break;

		case GLUT_KEY_DOWN:
			rotate('x', '-', true);
			break;

		case GLUT_KEY_LEFT:
			rotate('y', '+', true);
			break;

		case GLUT_KEY_RIGHT:
			rotate('y', '-', true);
			break;

		default:
			break;
	}
}


// Handling mouse button event.
void mouseButton(int button, int state, int x, int y)
{
}


// Handling mouse move events.
void mouseMove(int x, int y)
{
}


// Note: You may wish to add interactivity like clicking and dragging to move the camera.
//       In that case, please use the above functions.


// Entry point to the application.
int main(int argc, char** argv)
{
	glutInit(&argc, argv);
	glutInitDisplayMode(GLUT_RGBA | GLUT_DOUBLE | GLUT_MULTISAMPLE);
	glutInitWindowSize(500, 500);
	glutCreateWindow("CM20219 OpenGL Coursework");
	//glutFullScreen();  // Uncomment to start in full screen.
	InitGL();
	renderMode = 'f';

	// Callback functions
	glutDisplayFunc(display);
	glutReshapeFunc(reshape);
	glutKeyboardFunc(keyboard);
	glutSpecialFunc(arrow_keys);  // For special keys
	glutMouseFunc(mouseButton);
	glutMotionFunc(mouseMove);
	glutIdleFunc(idle);

	glutMainLoop();
}
