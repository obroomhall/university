function rotatedImgs = getRotatedImgs(img,numRots)
%rotatedImgs Returns a set of rotated images

angles = 0:(360/numRots):359;
rotatedImgs = cell(1, numRots);

for i = 1 : numRots
    angle = angles(i);
    rotatedImgs{i}.rot = angle;
    rotatedImgs{i}.img = imrotate(img, angle);
end

end

