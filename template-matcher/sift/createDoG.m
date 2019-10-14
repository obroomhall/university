function DoG = createDoG(img, levels, sigma, scales)

k = sqrt(2);

G = cell(1, levels);
for l = 1:levels
    G{l} = cell(1, scales);
    for s=1:scales
        % calculate sigma
        sig = sigma * l * k^(s-1);
        G{l}{s} = imgaussfilt(img,sig);
    end
    img = imresize(img, 0.5);
end

DoG = cell(1, levels);
for l=1:levels
    DoG{l} = cell(1, scales-1);
    for s=1:scales-1
        DoG{l}{s} = G{l}{s+1} - G{l}{s}; 
    end
end

end
    