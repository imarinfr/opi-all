IP <- "localhost"

MACHINE <- "ImoVifa"
#MACHINE <- "Display"

require(OPI3)

chooseOpi(MACHINE)
params <- list(address = list(ip = IP, port = 50001))

res <- do.call(opiInitialize, params)
print(res)

on.exit(opiClose())
options(error = opiClose)

while (opiQueryDevice()$error)
    Sys.sleep(0.5)


res_setup <- opiSetup(settings = list(eye = "BOTH", 
            fixShape = "CROSS", 
            fixCx = 0.0, fixCy = 0.0,
            fixSx = 0.43, fixSy = 0.43, 
            fixRotation = 45,
            fixCol = list(0.0, 1.0, 0.0),
            fixLum = 0,
            bgLum = 10, 
            bgCol = list(1.0, 1.0, 1.0)
))
if (res_setup[["error"]]) {
    print(res_setup)
    stop("failed to setup")
}

cat("Type <something> [enter] to continue\n")
b <- scan("stdin", character(), n = 1)

pts <- matrix(c(9,-21, 3,-21, -3,-21, -9,-21, 15,-15, 9,-15, 3,-15, -3,-15, -9,-15, -15,-15, 21,-9, 15,-9, 9,-9, 3,-9, -3,-9,
-9,-9, -15,-9, -21,-9, 21,-3, 15,-3, 9,-3, 3,-3, -3,-3, -9,-3, -21,-3, 21,3, 15,3, 9,3, 3,3, -3,3, -9,3, -21,3,
21,9, 15,9, 9,9, 3,9, -3,9, -9,9, -15,9, -21,9, 15,15, 9,15, 3,15, -3,15, -9,15, -15,15, 9,21, 3,21, -3,21, 
-9,21, 27,3, 27,-3), ncol = 2, byrow = TRUE)

#pts <- matrix(c(
#-27, 21, 0, 21, 27, 21,
#-27,  0,        27,  0,
#-27,-21, 0,-21, 27,-21
#), ncol = 2, byrow = TRUE)

pts <- matrix(c(
15, -3, 16, -3, 17, -3
), ncol = 2, byrow = TRUE)

#pts <- matrix(c(
#-33, 0, 0, 33, 33, 0, 0, -33
#), ncol = 2, byrow = TRUE)

print(nrow(pts))
n <- nrow(pts)
for (i in 1:100) {
    stim <- list(
        stim.length = n,
        eye = as.list(rep("right", n)),
        x = as.list(pts[, 1]),
        y = as.list(pts[, 2]),
        lum = as.list(rep(dbTocd(10) + 10.0, n)),
        sx = as.list(rep(0.43, n)),
        sy = as.list(rep(0.43, n)),
        #color1 = list(c(1,0,0), c(0,1,0), c(0,0,1)),
        color1 = lapply(1:n, function(x) list(1.0, 1.0, 1.0)),
        t = as.list(rep(200, n)),
        w = 2000
    )

    res <- opiPresent(stim)

    if (!res[["error"]]) {
        print(paste(i, res[["msg"]][["seen"]]))
    } else {
        print(res$msg)
    }
}

cat("Type <something> [enter] to continue\n")
b <- scan("stdin", character(), n = 1)

print(opiClose())
