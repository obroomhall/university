function norm = normalise(A, lower, upper)
% Normalise greyscale image between lower and upper

% normalise between 0 and 1
norm = A - min(A(:));
norm = norm ./ max(norm(:));

% change to fit range
range = abs(upper - lower);
norm = (norm .* range) + lower;

end

