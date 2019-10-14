function out = createRotatedImages(srcDir, ext, numLvls, sigma, octave, numRots, startScale)
%CreateRotatedImages Creates a guassian pyramid and rotates each level
% example: createRotatedImages('dataset/Training/png/', 'png', 8, 5, [3 3], 1.6, 0.8, 8)

imgs = dir(strcat(srcDir, '*.', ext));
out = cell(1, length(imgs));

progressBar = waitbar(0, 'Creating Templates');
for i = 1 : length(imgs)
    
    imgNameWithExt = imgs(i).name;
    %img = rgb2gray(imread(strcat(srcDir, imgNameWithExt)));
    img = rgb2gray(im2double(imread(strcat(srcDir, imgNameWithExt))));
    
    pyr = guPyr(img, numLvls, sigma, octave, startScale);
    out{i} = cell(1, numLvls);
    
    for j = 1 : numLvls
        
        pyrImg = pyr{j};
        [h,w] = size(pyrImg);
        
        rotImgs = getRotatedImgs(pyrImg, numRots);
        out{i}{j} = cell(1, numRots);
        
        for k = 1 : numRots
            
            rotImg = rotImgs{k};
            rotImg.scale = [w,h];
            rotImg.id = i;
            
            img = rotImgs{k}.img;
            img = img - mean(img(:));
            img = img/max(img(:));
            rotImg.img = gpuArray(img);
            
            out{i}{j}{k} = rotImg;
            
        end
    end
    waitbar(i/length(imgs), progressBar);
end
close(progressBar);

end

