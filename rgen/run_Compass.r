# #!/bin/bash

# cat<<HERE | R --slave 

IP <- "192.168.4.146"
IP <- "192.168.1.4"
IP <- "192.168.4.85"
IP <- "192.168.105.14"
IP <- "192.168.4.132"
IP <- "192.168.105.95"
IP <- "192.168.22.123"  # Allison's LEI Compass machine, PORT 50001

MACHINE <- "Compass"

require(OPI)

chooseOpi(MACHINE)
res <- opiInitialize(address = list(ip = IP, port = 50001))
print(res)

ijunk <- readline("Press Enter to continue")

result <- opiSetup(settings = list(fixCx = 0, tracking = 0))
print(result)

print(opiQueryDevice())


for (i in 1:3) {
print(opiPresent(stim = list(
    stim.length = 1,
    x = runif(1, -2, 2),
    y = runif(1, -2, 2), 
    level = dbTocd(10),
    duration = 200,
    responseWindow = 1500
)))
Sys.sleep(0.5)
}

opiClose()
# HERE
