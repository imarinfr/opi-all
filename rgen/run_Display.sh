#!/bin/bash

# A test for any JOVP machine (eg Display, ImoVifa)

IP <- "192.168.1.4"
IP <- "192.168.4.188"
IP <- "10.21.115.138"
IP <- "192.168.4.135"
IP <- "localhost"

#MACHINE <- "ImoVifa"
MACHINE <- "Display"

require(OPI3)

chooseOpi(MACHINE)
params <- list(address = list( ip = IP, port = 50001))
#if (MACHINE == "ImoVifa")
#    params <- c(params, list(screen = 1))

res <- do.call(opiInitialize, params)
print(res)

on.exit(opiClose())
options(error = opiClose)

while(!is.null(opiQueryDevice()$err))
    Sys.sleep(0.5)

print("Setting up")

result <- opiSetup(settings = list(eye = "BOTH", 
            bgLum = 10.0, 
            fixShape = "MALTESE", 
            fixCx = 0.0,
            fixCy = 0.0, 
            fixSx = 1.0, 
            fixCol = list(0.0, 1.0, 0.0),
            fixLum = 100,
            viewMode = "Stereo"
))
print(result)

cat("Type <something> [enter] to continue\n")
b <- scan("stdin", character(), n=1)



backs <- function(bgLums = c(NA)) {
    if (all(is.na(bgLums))) {
        result <- opiSetup(settings = list(eye = "BOTH", 
                    fixShape = "MALTESE", fixLum = 20.0, fixCx = 0.0,
                    fixSx = 1.0, fixCy = 0.0, 
                    fixCol = list(0.0, 1.0, 0.0),
                    fixLum = 100,
                    viewMode = "Stereo"
        ))
    } else 
    for (bgLum in bgLums) {
        result <- opiSetup(settings = list(eye = "BOTH", 
                    fixShape = "MALTESE", 
                    fixSx = 0.0, fixSy = 0.0, 
                    fixCx = 0.0, fixCy = 0.0, 
                    fixCol = list(0.0, 1.0, 0.0),
                    fixLum = 0,
                    bgLum = bgLum, 
                    bgCol = list(1.0, 1.0, 1.0),
                    viewMode = "Stereo"
        ))
        print(result)
        Sys.sleep(0.5)
    }
}


stims1 <- function() {
    #res_setup <- opiSetup(settings = list(eye = "BOTH", 
    #            fixShape = "CIRCLE", 
    #            fixType = "IMAGE",
    #            fixImageFilename = "/Users/aturpin/src/opi-all/jovp/x.jpg",
    #            fixCx = 0.0, fixCy = 0.0,
    #            fixSx = 0.43, fixSy = 0.43, 
    #            fixRotation = 45,
    #            fixCol = list(0.0, 1.0, 0.0),
    #            fixLum = 100,
    #            bgLum = 10, 
    #            bgCol = list(1.0, 1.0, 1.0),
    #            viewMode = "Stereo"
    #))
    #if (!is.null(res_setup$err)) {
    #    print(res_setup)
    #    stop("failed to setup")
    #}

    n <- 1
    while(TRUE) {
        #for (i in seq(2, 2, 5)) {
            x <- rep(runif(1, -20, 20), n)
            y <- rep(runif(1, -20, 20), n)
            res <- opiPresent(stim = list(
                stim.length = n, 
                eye = as.list(rep("left", n)),
                x = as.list(x),
                y = as.list(y),
                lum = as.list(rep(dbTocd(10) + 10.0, n)),
                sx = as.list(rep(0.43, n)),
                sy = as.list(rep(0.43, n)),
                color1 = lapply(1:n, function(i) list(1, 1, 1)),
                t = as.list(rep(200, n)),
                w = n*1000 + 200
            ))
print("************  Present")
print(res)
            if (is.null(res$err))
                print(paste("x=", round(x, 2), "y=", round(y, 2), "seen=", res$seen))
            else
                print(res)
        #} 
    }
}

# single stims getting bigger with each button press
stims2 <- function() {
    res_setup <- opiSetup(settings = list(eye = "BOTH", 
                fixShape = "CROSS", 
                fixCx = 0.0, fixCy = 0.0,
                fixSx = 0.43, fixSy = 0.43, 
                fixRotation = 45,
                fixCol = list(0.0, 1.0, 0.0),
                fixLum = 0,
                bgLum = 128, 
                bgCol = list(1.0, 1.0, 1.0)
    ))
    if (!is.nul(res_setup$err)) {
        print(res_setup)
        stop("failed to setup")
    }

    cat("Type <something> [enter] to continue\n")
    b <- scan("stdin", character(), n=1)

    n <- 1
    for (i in 58:68) {
        print(i)
        res <- opiPresent(stim = list(
        x <- rep(0, n), # runif(1, -1, 1), n)
        y <- rep(0, n), # runif(1, -1, 1), n)
            stim.length = n, 
            eye = as.list(rep("right", n)),
            x = as.list(x),
            y = as.list(y),
            lum = as.list(255, n),
            sx = as.list(rep(i, n)),
            sy = as.list(rep(i, n)),
            color1 = lapply(1:n, function(i) list(1, 1, 1)),
            t = as.list(rep(9000, n)),
            w = 12200
        ))

        if (is.null(res$err))
            print(paste("x=", round(x, 2), "y=", round(y, 2), "seen=", res$seen))
        else
            print(res$err)

        #cat("Type <something> [enter] to continue\n")
        #b <- scan("stdin", character(), n=1)
    }
}

fos <- function() {
    res_setup <- opiSetup(settings = list(eye = "BOTH", 
                fixShape = "MALTESE", 
                fixCx = 0.0, fixCy = 0.0,
                fixSx = 0.0, fixSy = 0.0, 
                fixRotation = 45,
                fixCol = list(0.0, 1.0, 0.0),
                fixLum = 0,
                bgLum = 10, 
                bgCol = list(1.0, 1.0, 1.0)
    ))
    if (!is.nul(res_setup$err)) {
        print(res_setup)
        stop("failed to setup")
    }

    cat("Type <something> [enter] to continue\n")
    b <- scan("stdin", character(), n=1)

    lums <- rep(seq(0, 40, 5), 5)
    #lums <- rep(seq(28, 28, 2), 10)
    lums <- lums[order(runif(length(lums)))]
    d <- data.frame(lums = lums)
    d$res <- NA
    n <- 1
    for (i in seq_along(lums)) {
        res <- opiPresent(stim = list(
        x <- rep(0, n), # runif(1, -1, 1), n)
        y <- rep(0, n), # runif(1, -1, 1), n)
            stim.length = n, 
            eye = as.list(rep("left", n)),
            x = as.list(x),
            y = as.list(y),
            lum = as.list(rep(dbTocd(lums[i]) + 10.0, n)),
            sx = as.list(rep(0.43, n)),
            sy = as.list(rep(0.43, n)),
            color1 = lapply(1:n, function(i) list(1, 1, 1)),
            t = as.list(rep(1000, n)),
            w = 2200
        ))

        if (is.null(res$err)) {
            print(paste("x=", round(x, 2), "y=", round(y, 2), "seen=", res$seen))
            d$res[i] <- res$seen
        } else {
            print("ERROR")
            print(res)
        }
    }

    print(d)
    l <- split(d$res, d$lums)
    for (lum in sort(unique(lums)))
        print(paste(lum, sum(l[[as.character(lum)]])))
}

fos_B <- function() {
    for (i in sapply(seq(10, 40, 1), dbTocd)) {
        res_setup <- opiSetup(settings = list(eye = "BOTH", 
                    fixShape = "MALTESE", 
                    fixCx = 0.0, fixCy = 0.0,
                    fixSx = 0.0, fixSy = 0.0, 
                    fixRotation = 45,
                    fixCol = list(0.0, 1.0, 0.0),
                    fixLum = 0,
                    bgLum = i, 
                    bgCol = list(1.0, 1.0, 1.0)
        ))

        if (!is.null(res_setup$err)) {
            print(res_setup)
            stop("failed to setup")
        }
        print(paste("bgLum", i))
        cat("Type <something> [enter] to continue\n")
        b <- scan("stdin", character(), n=1)
    }
}


thetas <- c(seq(0, 359, 30), seq(0, 359, 30))
colP <- colorRampPalette(c("red", "blue"))(length(thetas))
cols <- sapply(1:length(thetas), function(i) col2rgb(colP[i])/255) 

circleLetters <- function() {
    res <- opiPresent(stim = list(
          stim.length = length(thetas),
          eye = as.list(rep('both', length(thetas))),
          x = lapply(thetas, function(t) 15 * cos(t/180*pi)), 
          y = lapply(thetas, function(t) 15 * sin(t/180*pi)), 
          sx = as.list(rep(2.2, length(thetas))),
          sy = as.list(rep(2.2, length(thetas))),
          lum = as.list(rep(255, length(thetas))),
          shape = lapply(1:length(thetas), function(t) "OPTOTYPE"),
          optotype = lapply(1:length(thetas), function(t) LETTERS[t]), 
          color1 = lapply(1:ncol(cols), function(ci) cols[, ci]),
          t = as.list(rep(300, length(thetas))),
          w = 100 * length(thetas) + 1500
    ))

    print(res)
}


circleImage <- function() {
    print(opiPresent(stim = list(
          stim.length = length(thetas),
          eye = as.list(rep('both', length(thetas))),
          #eye = as.list(rep('left', length(thetas))),
          x = lapply(thetas, function(t) 15 * cos(t/180*pi)), 
          y = lapply(thetas, function(t) 15 * sin(t/180*pi)), 
          t = as.list(rep(1000, length(thetas))),
          w = 10000 * length(thetas) + 1500,
          sx = as.list(rep(8.0, length(thetas))),
          sy = as.list(rep(8.0, length(thetas))),
          lum = as.list(rep(255, length(thetas))),
          color1 = lapply(1:length(thetas), function(i) list(1, 0, 0)),
          #type = as.list(rep("SINE", length(thetas))),
          #color2 = lapply(1:length(thetas), function(i) list(0, 1, 0)), 
          #frequency = as.list(rep(10, length(thetas)))
          #contrast = as.list(rep(0.5, length(thetas))),
          #type = as.list(rep("CHECKERBOARD", length(thetas))),
          type = as.list(rep("IMAGE", length(thetas))),
          imageFilename = as.list(rep("/Users/aturpin/src/opi-all/jovp/x.jpg", length(thetas)))
          #imageFilename = as.list(rep("ecceIvanito.jpeg", length(thetas)))
          #imageFilename = as.list(rep("c:/Users/imo vifa/Desktop/opi3/opi-all/jovp/x.jpg", length(thetas)))
    )))
}

# big red in left then little blue in right
diff_eye <- function() {
    print(opiPresent(stim = list(
          stim.length = 2, 
          eye = list("Left", "Right"),
          x = list(-5, 5),
          y = list(0, 0),
          t = list(3000, 3000),
          w = 10000,
          sx = list(3, 1),
          sy = list(3, 1),
          lum = list(255, 255),
          color1 = list(list(1, 0, 0), list(0, 0, 1))
    )))
}

fixImage <- function() {
    #if (Sys.info()[['sysname']] == "Darwin") {
    #    g_fix_image_folder <- "/Users/aturpin/Documents/papers/interruptible_perim/experiments/app"
    #} else {
        g_fix_image_folder <- "c:/Users/imo vifa/Desktop/opi3"
    #}

    g_fixation_images <- list("circle_Blue.png", "circle_Red.png", "circle_Yellow.png", "circle_Green.png")

    for (i in seq(1000, 10, -100)) {
        res <- opiSetup(settings = list(eye = "BOTH",
            bgLum = 10,
            bgCol = list(1.0, 1.0, 1.0),
            viewMode = "Stereo",
            fixShape = "CIRCLE",
            fixType = "IMAGE",
            fixImageFilename = paste(g_fix_image_folder, g_fixation_images[[1]], sep = "/"),
            fixSx = 1.43,
            fixSy = 1.43,
            fixLum = i
        ))
        Sys.sleep(5)
    }

    print(res)
}

tfd2 <- function() {
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
    if (!is.null(res$err)) {
        print(res_setup)
        stop("failed to setup")
    }

    cat("Type <something> [enter] to continue\n")
    b <- scan("stdin", character(), n=1)

    n <- 2
    for (i in 1:10) {
        res <- opiPresent(stim = list(
        x <- c(0, 15),
        y <- c(0, 0),
            stim.length = n, 
            eye = as.list(rep("left", n)),
            x = as.list(x),
            y = as.list(y),
            lum = as.list(rep(dbTocd(10) + 10.0, n)),
            sx = list(15*2, 0.43),
            sy = list(15*2, 0.43),
            color1 = lapply(1:n, function(i) list(1, 1, 1)),
            t = as.list(rep(500, n)),
            w = 2200
        ))

        if (is.null(res$err))
            print(paste(round(x, 2), round(y, 2), res$seen))
        else
            print(res$err)
    }
}

#backs(10)  # 10/25 for linear 8 bit gamma
stims1()
#circleLetters()

#backs(c(128))  # white for linear 8 bit gamma
#diff_eye()
#circleImage()

#backs(c(255))  # white for linear 8 bit gamma

#fixImage()
#stims2()
#tfd2()
#fos()
#fos_B()

cat("Type <something> [enter] to continue\n")
b <- scan("stdin", character(), n=1)

#cat("Sleeping 3s\n"); Sys.sleep(3)
print(opiClose())
