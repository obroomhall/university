% https://stackoverflow.com/questions/938260/comparing-two-matrices-in-matlab

function same = compareMat(x,y)

absTol = 1e-10;   % You choose this value to be what you want!
relTol = 0.005;   % This one too!
absError = x(:)-y(:);
relError = absError./x(:);
relError(~isfinite(relError)) = 0;   % Sets Inf and NaN to 0
same = all( (abs(absError) < absTol) & (abs(relError) < relTol) );

end