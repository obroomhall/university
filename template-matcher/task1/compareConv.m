function res = compareConv(A, k)

C = myconv2(A, k);
D = conv2(A, k, 'same');
res = compareMat(C, D);

end