function pyr = guPyr(img, levels, sigma, octave, skip)

% Create pyramid
pyr = cell(1, levels+skip);

% First blur
blurredImg = imgaussfilt(img, sigma);
pyr{1} = blurredImg;

% Further resize and blur
for x = 2 : levels+skip
    resizedImg = imresize(pyr{x-1}, octave); %% ask if this is alright
    blurredImg = imgaussfilt(resizedImg, sigma); %conv2(resizedImg, kernel, 'same');
    pyr{x} = blurredImg;
end

pyr(1:skip) = [];

end