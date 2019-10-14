function NCorrs = corr(img, patch)

[imgRows, imgCols] = size(img);
[patchRows, patchCols] = size(patch);

% Take away mean from each element in template and test
Ifull = img - mean(img(:));
P = patch - mean(patch(:));

totalRows = imgRows-patchRows+1;
totalCols = imgCols-patchCols+1;

NCorrs = zeros(totalRows, totalCols);

for r = 1 : totalRows
    for c = 1 : totalCols
        
        % Get patch
        I = Ifull(...
            r : r+patchRows-1,...
            c : c+patchCols-1 ...
        );

        % Get normalised correlation value
        NCorrs(r, c) = sum(sum(I.*P)) / sqrt( sum(sum(I.^2)) * sum(sum(P.^2)) );

    end
end

end