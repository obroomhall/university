function savedBoxes = nonMaxSupp(boxes, iou_thresh, draw)
%nonMaxSupp Performs non maxima suppression on input boxes
%   boxes[row, col, height, width, intensity, id]

%progressBar = waitbar(0, 'Non-Maxima Suppression');
savedBoxes = [];
while (size(boxes, 1) > 0)
    
    % Gets max intensity and index
    [~,max_intensity_idx] = max(boxes(:,5));

    % Add box to saved boxes
    savedBoxes = [savedBoxes; boxes(max_intensity_idx, :)];

    % Get bounding box for max intensity
    bboxA = boxes(max_intensity_idx, 1:4);

    if draw
        matchA = boxes(max_intensity_idx,:);
        rectangle('Position', bboxA, 'EdgeColor', 'g');
        fprintf('bboxA at %d for id: %d\n', matchA(5), matchA(6));
    end
    
    % Remove boxes with IoU > IoU_thresh
    numBoxesRemoved = 0;
    for i = 1 : size(boxes)
        
        idx = i-numBoxesRemoved;
        bboxB = boxes(idx, 1:4);

        if bboxOverlapRatio(bboxA, bboxB, 'Min') > iou_thresh
            if draw
                matchB = boxes(idx,:);
                rectangle('Position', bboxB, 'EdgeColor', 'r');
                fprintf('bboxA at %d for id: %d\n', matchB(5), matchB(6));
            end
            boxes(idx,:) = [];
            numBoxesRemoved = numBoxesRemoved + 1;
        else
            if draw
                matchB = boxes(idx,:);
                rectangle('Position', bboxB, 'EdgeColor', 'c');
                fprintf('bboxA at %d for id: %d\n', matchB(5), matchB(6));
            end
        end
    end
    %waitbar(size(savedBoxes, 1)/size(boxes, 1), progressBar);
end
%close(progressBar);

end

