# Open Perimetry Interface implementation for O600
#
# Copyright [2022] [Andrew Turpin & Ivan Marin-Franch]
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

require(rjson)

env.O600 <- vector("list")    # environment for this machine in R

#' Implementation of opiInitialise for the O600 machine.
#'
#' This is for internal use only. Use [opiInitialise()] with
#' these Arguments and you will get the Value back.
#'
#' @usage NULL
#'
#' @param ip IP Address of the perimeter.
#' @param port TCP port of the perimeter.
#' @param ip_Monitor IP Address of the OPI JOVP server.
#' @param port_Monitor TCP port of the OPI JOVP server.
#' @param eye Eye to set.
#'
#' @return a list contianing:
#'  * error Empty string for all good, else error messages from Imo.
#'  * msg JSON Object with all of the other fields described in @ReturnMsg
#'           except 'error'.
#'    - msg$jovp Any messages that the JOVP sent back.
#'
#' @examples
#' chooseOpi("O600")
#' result <- opiInitialise(ip = "192.126.0.1", port = 50000,
#'                         ip_Monitor = "localhost", port_Monitor = 50001,
#'                         eye = "left")
#'
#' @seealso [opiInitialise()]
#'
opiInitialise_for_O600 <- function(ip = NULL, port = NULL, ip_Monitor = NULL, port_Monitor = NULL, eye = NULL) {
    env.O600$socket <<- open_socket(ip_Monitor, port_Monitor)
    msg <- list(ip = ip,port = port,ip_Monitor = ip_Monitor,port_Monitor = port_Monitor,eye = eye);
    msg <- rjson::toJSON(msg)
    writeLines(msg, env.O600$socket)

    res <- rjson::fromJSON(readLines(env.O600$socket, n=1))
    return(res)
}

#' Implementation of opiPresent for the O600 machine.
#'
#' This is for internal use only. Use [opiPresent()] with
#' these Arguments and you will get the Value back.
#'
#' @usage NULL
#'
#' @param x List of x co-ordinates of stimuli (degrees).
#' @param y List of y co-ordinates of stimuli (degrees).
#' @param t List of stimuli presentation times (ms).
#' @param w List of stimuli response windows (ms).
#' @param lum List of stimuli luminances (cd/m^2).
#' @param size Stimulus size (degrees). Can be Goldmann Size I to V (or VI if
#'                device has a big wheel)
#' @param color List of stimuli colors.
#'
#' @return a list contianing:
#'  * error Empty string for all good, else error messages from O600.
#'  * msg JSON Object with all of the other fields described in @ReturnMsg
#'           except 'error'.
#'    - msg$seen true if seen, false if not.
#'    - msg$time Response time from stimulus onset if button pressed, -1
#'                  otherwise (ms).
#'    - msg$eyex x co-ordinates of pupil at times eyet (degrees).
#'    - msg$eyey y co-ordinates of pupil at times eyet (degrees).
#'    - msg$eyed Diameter of pupil at times eyet (degrees).
#'    - msg$eyet Time of (eyex,eyey) pupil relative to stimulus onset t=0 (ms).
#'    - msg$jovp Any JOVP-specific messages that the JOVP sent back.
#'
#' @examples
#' chooseOpi("O600")
#' result <- opiPresent(stim = list(x = list(0), y = list(0), t = list(200), w = 1500, lum = 20,
#'                      size = "GV", color = "white"))
#'
#' @seealso [opiPresent()]
#'
opiPresent_for_O600 <- function(stim = list(x = NULL, y = NULL, t = NULL, w = NULL, lum = NULL, size = NULL, color = NULL)) {
if(!exists(env.O600$socket) || is.null(env.O600$socket))
    stop("Cannot call opiPresent without an open socket to Monitor. Did you call opiInitialise()?.")

    msg <- list(x = stim$x,y = stim$y,t = stim$t,w = stim$w,lum = stim$lum,size = stim$size,color = stim$color);
    msg <- rjson::toJSON(msg)
    writeLines(msg, env.O600$socket)

    res <- rjson::fromJSON(readLines(env.O600$socket, n=1))
    return(res)
}

#' Implementation of opiSetup for the O600 machine.
#'
#' This is for internal use only. Use [opiSetup()] with
#' these Arguments and you will get the Value back.
#'
#' @usage NULL
#'
#' @param bgLum Background luminance for eye.
#' @param fixType Fixation target type for eye.
#' @param fixLum Fixation target luminance for eye.
#' @param fixCol Fixation target color for eye.
#' @param tracking Whether to correct stimulus location based on eye position.
#'
#' @return a list contianing:
#'  * error Empty string for all good, else error messages from ImoVifa.
#'  * msg JSON Object with all of the other fields described in @ReturnMsg
#'           except 'error'.
#'    - msg$jovp Any messages that the JOVP sent back.
#'
#' @examples
#' chooseOpi("O600")
#' result <- opiSetup(settings = list(bgLum = 10, fixType = "maltese", fixLum = 20, fixCol = "green",
#'                    tracking = 0))
#'
#' @seealso [opiSetup()]
#'
opiSetup_for_O600 <- function(settings = list(bgLum = NULL, fixType = NULL, fixLum = NULL, fixCol = NULL, tracking = NULL)) {
if(!exists(env.O600$socket) || is.null(env.O600$socket))
    stop("Cannot call opiSetup without an open socket to Monitor. Did you call opiInitialise()?.")

    msg <- list(bgLum = settings$bgLum,fixType = settings$fixType,fixLum = settings$fixLum,fixCol = settings$fixCol,tracking = settings$tracking);
    msg <- rjson::toJSON(msg)
    writeLines(msg, env.O600$socket)

    res <- rjson::fromJSON(readLines(env.O600$socket, n=1))
    return(res)
}

#' Implementation of opiClose for the O600 machine.
#'
#' This is for internal use only. Use [opiClose()] with
#' these Arguments and you will get the Value back.
#'
#' @usage NULL
#'

#'
#' @return a list contianing:
#'  * error Empty string for all good, else error messages from Imo.
#'  * msg JSON Object with all of the other fields described in @ReturnMsg
#'           except 'error'.
#'    - msg$jovp Any messages that the JOVP sent back.
#'
#' @examples
#' chooseOpi("O600")
#' result <- opiClose()
#'
#' @seealso [opiClose()]
#'
opiClose_for_O600 <- function() {
if(!exists(env.O600$socket) || is.null(env.O600$socket))
    stop("Cannot call opiClose without an open socket to Monitor. Did you call opiInitialise()?.")

    msg <- list();
    msg <- rjson::toJSON(msg)
    writeLines(msg, env.O600$socket)

    res <- rjson::fromJSON(readLines(env.O600$socket, n=1))
    return(res)
}

#' Implementation of opiQueryDevice for the O600 machine.
#'
#' This is for internal use only. Use [opiQueryDevice()] with
#' these Arguments and you will get the Value back.
#'
#' @usage NULL
#'

#'
#' @return a list contianing:
#'  * error Empty string for all good, else error message.
#'  * msg JSON Object with all of the other fields described in @ReturnMsg
#'           except 'error'.
#'    - msg$jovp Any messages that the JOVP sent back.
#'
#' @examples
#' chooseOpi("O600")
#' result <- opiQueryDevice()
#'
#' @seealso [opiQueryDevice()]
#'
opiQueryDevice_for_O600 <- function() {
if(!exists(env.O600$socket) || is.null(env.O600$socket))
    stop("Cannot call opiQueryDevice without an open socket to Monitor. Did you call opiInitialise()?.")

    msg <- list();
    msg <- rjson::toJSON(msg)
    writeLines(msg, env.O600$socket)

    res <- rjson::fromJSON(readLines(env.O600$socket, n=1))
    return(res)
}


#' Set background color and luminance in both eyes.
#' Deprecated for ImoVifa and replaced with [opiSetup()].
#' @usage NULL
#' @seealso [opiSetup()]
opiSetBackground_for_ImoVifa <- function(lum, color, ...) {return("Deprecated")}


