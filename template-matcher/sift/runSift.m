clear;
clc;
img = imread('../rots/stpauls.jpg'); %dataset/Training/png/003-bridge-1.png');
img = mean(im2double(img), 3);
DoG = createDoG(img, 4, 1.6, 5);
keyPoints = getKeyPoints(img, 0.5, DoG, 0.03, 10);
figure;
imshow(img, []);
hold on; % Prevent image from being blown away.

% create boxes depending on feature size
for i = 1 : length(keyPoints)
    l = keyPoints(i, 3);
    size = 6 * 2^(l-1);
    rectangle('Position', [keyPoints(i, 2), keyPoints(i, 1), size, size]...
        , 'EdgeColor', 'm', 'LineWidth', 1);
end

% uncomment below to get dots of key points
%plot(keyPoints(:, 2), keyPoints(:, 1),'m.', 'MarkerSize', 15);