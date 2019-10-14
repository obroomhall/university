% set threshold values
IoU_thresh = 0.01;
%intensity_thresh = 0.0001;

% set values for creating scaled and rotated imgs
% near perfect result, but slow as hell
% numLvls = 14;
% sigma = 0.5;
% octave = 0.8;
% numRots = 12;
% skipNum = 2;

% great result, but quick
% numLvls = 8;
% sigma = 1.5;
% octave = 0.7;
% numRots = 12;
% skipNum = 1;

% good result
numLvls = 7;
sigma = 1.2;
octave = 0.65;
numRots = 12;
skipNum = 2;

% get imgs
ext = 'png';
testDir = 'dataset/Test/';
trainingDir = 'dataset/Training/png/';
testImgs = dir(strcat(testDir, '*.png'));
trainingImgs = dir(strcat(trainingDir, '*.png'));

% gets names of classes, e.g. 1 = lighthouse
numTrainingImgs = size(trainingImgs, 1);
trainingClasses = cell(1, numTrainingImgs);
for i = 1 : numTrainingImgs
    trainingImgName = trainingImgs(i).name;
    trainingImgId = str2double(trainingImgName(1:3));
    trainingImgClass = trainingImgName(5:end-4);
    trainingClasses{trainingImgId} = trainingImgClass;
end

% get template imgs
templateImgs = createRotatedImagesGpu('dataset/Training/png/', 'png', numLvls, sigma, octave, numRots, skipNum);

% do template matching on each test img
for i = 1 : length(testImgs)
    
    % get new test image
    testImgPath = strcat(testDir, testImgs(i).name);
    testImg = rgb2gray(im2double(imread(testImgPath)));
    testImg(testImg < 0) = 0;
    testImg = gpuArray(testImg);
    
    % preallocated array for matches
    matches = gpuArray(zeros(length(templateImgs)*15,9)); % [row, col, height, width, intensity, id, rot, scaleR, scaleC]

    progressBarTemplates = waitbar(0, 'Correlating Templates');

    % get correlation values for each rotated and scaled template
    matchesInd = 1;
    for templateInd =  1 : length(templateImgs) %[11,16,22,24,38,44] %
        
        templateMatches = gpuArray(zeros(numLvls*numRots,9)); %assumes 1 appearance of each template
        templateMatchesInd = 1;
        for pyrInd = 1 : numLvls % 3 %
            for rotInd = 1 : numRots % 14 %
        
                % get template image
                template = templateImgs{templateInd}{pyrInd}{rotInd};
                
                % get normalised correlation value for template on test
                %correlation = corr3(testImg, template.img);
                correlation = normxcorr2(template.img, testImg);
                
                % saves a lot of time
                if isempty(correlation)
                    continue;
                end
                
                % get max intensity value and location
                [intensity, max_ind] = max(correlation(:));
                [h,w] = size(template.img);
                [y,x] = ind2sub(size(correlation),max_ind);

                %disp([intensity, templateInd]);
                
                % add max match to matches
                templateMatches(templateMatchesInd, :) = [x-w, y-h, w, h, intensity, template.id, template.rot, template.scale];
                templateMatchesInd = templateMatchesInd + 1;
                
            end
        end
        
        templateMatches(templateMatches(:,3) == 0, :) = [];
        templateMatchesSupp = nonMaxSupp(gather(templateMatches), IoU_thresh, false);
        matches(matchesInd:matchesInd+size(templateMatchesSupp,1)-1, :) = templateMatchesSupp;
        matchesInd = matchesInd + size(templateMatchesSupp,1);
        
        waitbar(templateInd/length(templateImgs), progressBarTemplates);
    end
    close(progressBarTemplates);
    
    % remove [0 0 0 0] matches (based on w=0)
    matches(matches(:,3) == 0, :) = [];
    
    % Remove all boxes with intensity less than thresh
    %matches(matches(:,5) < intensity_thresh*maxIntensity, :) = [];
    
    % adds weighting to larger boxes
    weightedMatches = matches;
    weightedMatches(:,5) = weightedMatches(:,5) .* (1 + weightedMatches(:,8)*(18/10000));
    
    
    figure, imshow(testImg), axis on, hold on;
    % perform non maxima suppression on matches
    boxes = nonMaxSupp(gather(weightedMatches), IoU_thresh, false);

    maxIntensity = max(boxes(:,5));
  
    % show boxes with text
    for k = 1 : size(boxes)
        
        box = gather(boxes(k, :));
 
        prob = box(5);
        id = box(6);
        idAndProbStr = strcat(trainingClasses{id});%, ': ', num2str(prob*100/maxIntensity), '%');
        text('Position',box(1:2)-[2,34],'string',idAndProbStr, 'Color', 'g');
        
        rot = box(7);
        rotStr = strcat('Rot: ', num2str(rot), char(176));
        text('Position',box(1:2)-[2,22],'string',rotStr, 'Color', 'g');
        
        scaleR = box(8);
        scaleC = box(9);
        scaleStr = strcat('Scale: ', num2str(scaleR), 'x', num2str(scaleC), ' px');
        text('Position',box(1:2)-[2,10],'string',scaleStr, 'Color', 'g');
        
        x = box(1);
        y = box(2);
        w = box(3);
        h = box(4);
        halfSizeDiff = ([w,h] - [scaleC,scaleR])/2;
        
        topLeft = [x,y] + halfSizeDiff;
        topRight = [x+w-halfSizeDiff(1),y+halfSizeDiff(2)];
        bottomLeft = [x+halfSizeDiff(1),y+h-halfSizeDiff(2)];
        bottomRight = [x+w,y+h] - halfSizeDiff;

        % rotation matrix
        theta = 360 - box(7);
        R = [cosd(theta) -sind(theta); sind(theta) cosd(theta)];
        
        halfSize = [scaleC,scaleR]/2;
        lineCoordinates = [topLeft; topRight; bottomRight; bottomLeft; topLeft];
        lineCoordinatesOrigin = lineCoordinates - topLeft - halfSize;
        rotatedLineCoordinates = R*lineCoordinatesOrigin';
        lineCoordinatesRotated = rotatedLineCoordinates' + topLeft + halfSize;
        
        plot(lineCoordinatesRotated(:,1),lineCoordinatesRotated(:,2),'Color','m','LineWidth',2);
    end
    
    saveas(gcf, strcat('matches3/', testImgs(i).name));
    disp(strcat('saved ', testImgs(i).name));
end



