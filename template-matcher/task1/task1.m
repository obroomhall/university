trainingDir = 'dataset/Training/png/';
testDir = 'dataset/Test/';
imgExt = '*.png';

trainingImgs = dir(strcat(trainingDir, imgExt));
testImgs = dir(strcat(testDir, imgExt));

I = imread(strcat(trainingDir, trainingImgs(1).name));
Igray = rgb2gray(I);
Iconv = myconv2(Igray, kEdgeC);

imshowpair(Igray, Iconv, 'montage');
