function keyPoints = getKeyPoints(img, octave, DoG, contrastThresh, curvThresh)

keyPoints = zeros(100000, 3);
levels = length(DoG);
totalKP = 0;

% create gradient image for x and y for each level
G = gradients(img, levels, octave);

for l=1:levels
    Gx = G{l}{1}(2:end-1,2:end-1);
    Gy = G{l}{2}(2:end-1,2:end-1);
    DoGScales = DoG{l};
    scales = length(DoGScales);
    for s=2:scales-1
        upperScale = DoGScales{s-1};
        thisScale = DoGScales{s};
        lowerScale = DoGScales{s+1};
        [row, col] = size(thisScale);

        for y=2:col-1
            for x=2:row-1
                upperNeighbourhood = upperScale(x-1:x+1, y-1:y+1);
                thisNeighbourhood = thisScale(x-1:x+1, y-1:y+1);
                lowerNeighbourhood = lowerScale(x-1:x+1, y-1:y+1);
                scales = [upperNeighbourhood(:), thisNeighbourhood(:), lowerNeighbourhood(:)];
                maxima = max([scales(1:13), scales(15:end)]);
                minima = min([scales(1:13), scales(15:end)]);
                val = thisNeighbourhood(2,2);
                if val > maxima || val < minima
                    % remove low contrast
                    if abs(val) > contrastThresh
                        thisGx = Gx(x, y);
                        thisGy = Gy(x, y);                        
                        Dx2 = thisGx^2;
                        Dy2 = thisGy^2;
                        Dxy = thisGx*thisGy;
                        % create hessian matrix
                        Hess = [Dx2 Dxy;...
                             Dxy Dy2];
                        curveRatio = trace(Hess)^2/det(Hess);
                        % remove edges, keep corners
                        if curveRatio > (curvThresh+1)^2/curvThresh
                            totalKP = totalKP + 1;
                            
                            % x and y coords mapped to original images size
                            keyPoints(totalKP, 1) = x*2^(l-1);
                            keyPoints(totalKP, 2) = y*2^(l-1);
                            keyPoints(totalKP, 3) = l;
                        end
                    end
                end
            end
        end
    end
end
keyPoints = keyPoints(1:totalKP, :);
end

function G = gradients(img, levels, octave)

    xK = [-1 0 1;...
          -1 0 1;...
          -1 0 1];
           
    yK = [-1 -1 -1;...
          0  0  0;...
          1  1  1];
    
    G = cell(1, levels);
    
    for i = 1 : levels
        G{i} = cell(1,2);
        Gx = conv2(img, xK);
        Gy = conv2(img, yK);
        G{i}{1} = Gx;
        G{i}{2} = Gy;
        img = imresize(img, octave);
    end
end
