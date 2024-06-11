#
# A test for imoVifa
#
# Andrew Turpin
# Wed  1 May 2024 11:02:14 AWST

if (!require(OPI3)) {
    fn <- dir(".", pattern = "OPI3.*", full.names = TRUE)
    if (length(fn) == 0)
        stop("Cannot find OPI3 package. Check OPI.*.tgz file is in this folder.")

    install.packages(fn, repos = NULL, type = "source")
}

    # Connect to the ImoVifa machine
chooseOpi("ImoVifa")

IP <- "localhost"
IP <- "192.168.4.188"
params <- list(address = list(ip = IP, port = 50001))

res <- do.call(opiInitialize, params)
print(res)

on.exit(opiClose())
options(error = opiClose)

while (opiQueryDevice()$error)
    Sys.sleep(0.5)

    # Set the background
result <- opiSetup(settings = list(eye = "BOTH",
            viewMode = "Stereo",
            fixShape = "MALTESE",
            fixSx = 0.0, fixSy = 0.0,
            fixCx = 0.0, fixCy = 0.0,
            fixCol = list(0.0, 1.0, 0.0),
            fixLum = 0,
            bgLum = 10,
            bgCol = list(1.0, 1.0, 1.0)
))
print(result)

    # Loop forever until button is pressed
tt <- strsplit("OPI", "")[[1]]
while(TRUE) {
    cols <- lapply(seq_along(tt), function(i) as.list(runif(3,0,1)))
    res <- opiPresent(stim = list(
        stim.length = length(tt),
        eye = as.list(rep('both', length(tt))),
        x = as.list(-4 + 2 * seq_along(tt)),
        y = as.list(rep(0, length(tt))),
        sx = as.list(rep(4.2, length(tt))),
        sy = as.list(rep(4.2, length(tt))),
        lum = as.list(rep(100, length(tt))),
        shape = lapply(seq_along(tt), function(t) "OPTOTYPE"),
        optotype = as.list(tt),
        color1 = cols,
        t = as.list(rep(100, length(tt))),
        w = 110 * length(tt)
    ))

    if (res$error) {
        print("ERROR")
        print(res$msg)
        break
    }
    if (res$msg$seen)
        break
}

print(opiClose())
