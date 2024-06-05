#!/bin/bash

cat<<HERE | R --slave 

IP <- "192.168.1.4"
IP <- "192.168.4.146"
IP <- "localhost"

require(OPI3)
chooseOpi("ImoVifa")
#chooseOpi("Display")
opiInitialize(address = list(ip = IP, port = 50001))

cat("Sleeping 2s\n"); Sys.sleep(2)

result <- opiSetup(settings = list(eye = "BOTH", 
                fixShape = "MALTESE", fixLum = 20.0, fixCx = 0.0,
                fixSx = 1.0, fixCy = 0.0, 
                fixCol = list(0.0, 1.0, 0.0),
                fixLum = 99,
                bgLum = 10, 
                bgCol = list(1.0, 1.0, 1.0)))
    print(result)
    Sys.sleep(0.5)

print(opiQueryDevice())


xs <- as.list(10 * c(-1, 0, 1))
for (i in 1:10)
print(opiPresent(
    stim = list(
      stim.length = length(xs),
      eye = as.list(rep('both', length(xs))),
      x = xs,
      y = as.list(rep(0, length(xs))),
      sx = as.list(rep(13, length(xs))),
      sy = as.list(rep(13, length(xs))),
      lum = as.list(rep(300, length(xs))),
      shape = lapply(1:length(xs), function(t) "OPTOTYPE"),
      optotype = list('O', 'P', 'I'),
      color1 = list(list(1,0,0), list(0,1,0), list(0,0,1)),
      t = as.list(rep(500, length(xs))),
      w = 500 * length(xs) + 500
)))

cat("Sleeping 2s\n"); Sys.sleep(2)
print(opiClose())
HERE
