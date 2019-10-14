function objs = getObjectsFromImg(img)

background = img(1,1);

[rows,cols] = size(img);
padding = round((cols-rows)/2);
interCol = round((cols-(padding*2))/3);
interRow = round((rows-(padding*2))/2);
halfInterCol = round(interCol/2);
halfInterRow = round(interRow/2);

objs = {};
objInd = 1;

row = padding; 
while row < rows
    col = padding;
    while col < cols
        if img(row, col) ~= background

            obj = img(row-halfInterRow+1:row+halfInterRow-1, col-halfInterCol+1:col+halfInterCol-1);
            
            testRowUpper = 1;
            while obj(testRowUpper,:) == background
                testRowUpper = testRowUpper + 1;
            end
            
            testRowLower = size(obj, 1);
            while obj(testRowLower,:) == background
                testRowLower = testRowLower - 1;
            end
            
            testColUpper = 1;
            while obj(:,testColUpper) == background
                testColUpper = testColUpper + 1;
            end
            
            testColLower = size(obj, 1);
            while obj(:,testColLower) == background
                testColLower = testColLower - 1;
            end
            
            w = testColLower - testColUpper;
            h = testRowLower - testRowUpper;
            
            x = col-halfInterCol+1+testColUpper;
            y = row-halfInterRow+1+testRowUpper;
            
            scale = length(obj);
   
            objs{objInd}.img = img(y:y+h, x:x+w);
            objs{objInd}.center = [row,col];

            objInd = objInd + 1;
        end
        col = col + interCol;
    end
    row = row + interRow;
end

end