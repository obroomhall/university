% get test imgs paths
testDir = 'dataset/Test/';
testImgs = dir(strcat(testDir, '*.png'));
testImgPath = strcat(testDir, testImgs(1).name);
testImg = mean(im2double(imread(testImgPath)),3);
testImg = testImg - mean(testImg(:));

% get template image
templateImg = testImg(250:350, 430:550);%518:642, 484:619);
templateImg = templateImg - mean(templateImg(:));

% get normalised correlation value for template on test
correlation = corr3(testImg, templateImg);

[val,ind] = max(correlation(:));
[y,x] = ind2sub(size(correlation), ind);
[h,w] = size(templateImg);

box = [x, y, w, h];

% adjust position
pos = box(1:4);
pos(1) = pos(1) - pos(3);
pos(2) = pos(2) - pos(4);

% show boxes
figure, imshow(templateImg);
figure, imshow(testImg), axis on;
rectangle('Position', pos, 'EdgeColor', 'm');