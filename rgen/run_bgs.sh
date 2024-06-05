#!/bin/bash

# A test changing backgrounds for 'flicker'
# Wed 21 Feb 2024 06:43:54 AWST

cat<<HERE | R --slave 

IP <- "192.168.1.4"
IP <- "192.168.4.85"
IP <- "192.168.4.106"
IP <- "127.0.0.1"

MACHINE <- "Display"

require(OPI3)

chooseOpi(MACHINE)
params <- list(address = list( ip = IP, port = 50001))
if (MACHINE == "ImoVifa")
    params <- c(params, list(screen = 1))

res <- do.call(opiInitialize, params)
print(res)

cat("Sleeping 2s\n"); Sys.sleep(2)

#print("Does bg_lum cause flicker?") # no
#for (bg_lum in rep(c(254, 255), 200))
#   result <- opiSetup(settings = list(eye = "BOTH", 
#                fixShape = "MALTESE", fixCx = 0.0,
#                fixSx = 1.0, fixCy = 0.0, 
#                fixCol = list(0.0, 1.0, 0.0),
#                fixLum = 100,
#                bgLum = bg_lum, 
#                bgCol = list(1.0, 1.0, 1.0),
#                viewMode = "Stereo"
#    ))

#print("Does fixation change cause bg flicker?") # no
#for (fix_lum in rep(c(200, 255), 200)) {
#    result <- opiSetup(settings = list(eye = "BOTH", 
#                fixShape = "MALTESE", 
#                fixCy = 0.0, 
#                fixCx = 0.0,
#                fixSx = 147.0, 
#                fixSy = 147.0, 
#                fixCol = list(0.0, 1.0, 0.0),
#                fixLum = fix_lum,
#                bgLum = 128,
#                bgCol = list(1.0, 1.0, 1.0),
#                viewMode = "Stereo"
#    ))
#    #print(result)
#}

print("Does image change cause bg flicker?")  # yes
for (i in 1:200) {
    result <- opiSetup(settings = list(eye = "BOTH", 
                fixShape = "MALTESE", 
                fixCy = 0.0, 
                fixCx = 0.0,
                fixSx = 7.0, 
                fixSy = 7.0, 
                fixCol = list(0.0, 1.0, 0.0),
                fixLum = 200,
                bgImageFilename = "/Users/aturpin/src/opi-all/jovp/x.jpg",
                #bgLum = 128,
                #bgCol = list(1.0, 1.0, 1.0),
                viewMode = "Stereo"
    ))
    #print(result)
    Sys.sleep(0.1)
}


print(opiClose())
HERE
