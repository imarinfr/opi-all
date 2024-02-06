#!/bin/bash


(cd ./src/main/OPI3

R -e 'library(devtools);document()'

cd ..

R CMD BUILD OPI3
R CMD INSTALL OPI3
)

#(cd ./src/main ; R CMD check -for-cran OPI3_1.0.0.tar.gz)
