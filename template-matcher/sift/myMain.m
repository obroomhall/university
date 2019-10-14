clear;
clc;
img = imread('../dataset/Training/png/003-bridge-1.png');
img = mean(im2double(img), 3);
DoG = createDoG(img, 5, 1.6, 6);
keyPoints = getKeyPoints(img, 0.5, DoG, 0.03, 10);
figure;
imshow(img, []);
hold on; % Prevent image from being blown away.
plot(keyPoints(:, 2), keyPoints(:, 1),'r+', 'MarkerSize', 5);