kIdentity = [0 0 0; 0 1 0; 0 0 0];

kEdgeA = [1 0 -1; 0 0 0; -1 0 1];
kEdgeB = [0 1 0; 1 -4 1; 0 1 0];
kEdgeC = [-1 -1 -1; -1 8 -1; -1 -1 -1];

kSharpen = [0 -1 0; -1 5 -1; 0 -1 0];

kBox = 1/9 * [1 1 1; 1 1 1; 1 1 1];

kGaussianSml = 1/16 * [1 2 1; 2 4 2; 1 2 1];
kGaussianLrg = 1/256 * [1 4 6 4 1; 4 16 24 16 4; 6 24 -476 24 6; 4 16 24 16 4; 1 4 6 4 1];