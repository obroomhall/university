function res = myconv2(A, k)

[r,c] = size(A);
[m,n] = size(k);

% Flip filter vertically and horizontally
h = rot90(k, 2);

% Amount of padding needed based on kernel
center = floor((size(h)+1)/2);
left = center(2) - 1;
right = n - center(2);
top = center(1) - 1;
bottom = m - center(1);

% Pad new image with zeros
paddedImg = zeros(r + top + bottom, c + left + right);

% Copy image into paddedImg
for x = 1 + top : r + top
    for y = 1 + left : c + left
        paddedImg(x,y) = A(x - top, y - left);
    end
end

% Create resulting matrix
res = zeros(r , c);

% For each row and column in img
for x = 1 : r
    for y = 1 : c
        res(x, y) = sum(sum(paddedImg(x:x+m-1, y:y+n-1).*h));
    end
end