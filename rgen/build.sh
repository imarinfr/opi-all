#!/bin/bash


(cd ./src/main/OPI 

R -e 'library(devtools);document()'

cd ..

R CMD BUILD OPI
R CMD INSTALL OPI
)
