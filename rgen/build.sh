#!/bin/bash


(cd ./src/main/OPI 

R -e 'library(devtools);document()'

cd ..

R CMD BUILD OPI
R CMD INSTALL OPI
)

#(cd ./src/main ; R CMD check -for-cran OPI_3.0.0.tar.gz)
