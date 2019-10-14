function R = corr2(A, B)
[m, n] = size(B);
xPad = floor(m/2);
yPad = floor(n/2);

A = padarray(A, [xPad, yPad], 'post');
[h, w] = size(A);
C = real(ifft2(fft2(A) .* fft2(rot90(B,2),h,w)));
R = C(xPad:end, yPad:end);
end

