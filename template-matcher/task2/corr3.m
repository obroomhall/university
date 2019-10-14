function NCorrs = corr3(I, P)

%pad

[r,c] = size(I);

fftI = ifft2(fft2(I) .* fft2(rot90(P, 2),r,c));
fftIsqr = ifft2(fft2(I.^2) .* fft2(ones(r,c)));
convPsqr = conv2(P.^2, ones(size(P)), 'valid');

NCorrs = fftI ./ sqrt(fftIsqr * convPsqr);

end