function viewPyramid(pyr, id)

figure;
numImgs = length(pyr{id});
for i = 1 : numImgs
    
    subplot(1,numImgs,i), imshow(pyr{id}{i}{1}.img);
    
    title(strcat(...
        num2str(pyr{id}{i}{1}.scale(1)), 'x', ...
        num2str(pyr{id}{i}{1}.scale(2))));
    
end

end

