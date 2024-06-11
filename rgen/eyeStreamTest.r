
IP <- "192.168.4.149"
IP <- "localhost"

#MACHINE <- "Display"
MACHINE <- "ImoVifa"

require(OPI3)

chooseOpi(MACHINE)
params <- list(address = list(ip = IP, port = 50001))

print(params)

res <- do.call(opiInitialize, params)
print(res)

count <- 0
while(!is.null(opiQueryDevice()$err)) {
    print(paste("Waiting for OPI-JOVP", count))
    Sys.sleep(0.5)
    count <- count + 1
}

print("Setting up")

settings <- list(eye = "BOTH",
            bgLum = 10.0,
            fixShape = "MALTESE",
            fixCx = 0.0,
            fixCy = 0.0,
            fixSx = 1.0,
            fixCol = list(0.0, 1.0, 0.0),
            fixLum = 100,
            viewMode = "Stereo"
)
if (MACHINE == "ImoVifa") {
    s <- system("ifconfig", intern=T)
    ii <- head(grep("inet 192", s))
    myIp <- strsplit(s[ii], " ")[[1]][[2]]

    settings <- c(settings, list(eyeStreamIP = myIp, eyeStreamPortLeft = 50200, eyeStreamPortRight = 50201))
}

result <- opiSetup(settings)
print(result)

cat("Type <something> [enter] to continue\n")
b <- scan("stdin", character(), n=1)

# opiQueryDevice()
