#!/bin/bash


(cd ./src/main/OPI

R -e 'library(devtools);document()'

cd ..

R CMD BUILD OPI
R CMD INSTALL OPI
)

#(cd ./src/main ; R CMD check --as-cran OPI3_3.0.0.tar.gz)
#(cd ./src/main ; R CMD check --no-manual --no-install  OPI3_3.0.0.tar.gz)

#(cd ./src/main ; R CMD check --as-cran OPI_3.0.1.tar.gz)
