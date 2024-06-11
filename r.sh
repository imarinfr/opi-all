#!/bin/bash

cat<<HERE | R --slave 
require(OPI)
#chooseOpi("Octopus900")
#opiInitialise(ip = "localhost", serverPort = 52525)
chooseOpi("Compass")
#opiInitialize(ip = "192.168.4.162", port = 50001)
#chooseOpi("Display")
opiInitialize(address = list(ip = "192.168.4.85", port = 50001))
print(opiQueryDevice())
print(opiClose())
HERE 
