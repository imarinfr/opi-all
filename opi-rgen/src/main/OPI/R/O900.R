# Open Perimetry Interface implementation for O900
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

    # environment for this machine in R
if (exists(".opi_env") && !exists("O900", where = .opi_env))
    assign("O900", new.env(), envir = .opi_env)

#' Implementation of opiInitialise for the O900 machine.
#'
#' This is for internal use only. Use [opiInitialise()] with
#' these Arguments and you will get the Value back.
#'
#' @usage NULL
#'
#' @param ip IP Address of the perimeter.
#' @param port TCP port of the perimeter.
#' @param ip_Monitor IP Address of the O900 server.
#' @param port_Monitor TCP port of the O900 server.
#' @param eye Eye to set.
#' @param eyeSuite Path to EyeSuite.
#' @param gazeFeed Path where to save gaze feed. Directory must exists
#' @param bigWheel Whether O900 has a big wheel for displaying Goldmann Size VI
#'                    stimuli.
#' @param pres Volume for auditory feedback when a stimulus is presented: 0
#'                means no buzzer.
#' @param resp Volume for auditory feedback when observer presses the clicker: 0
#'                means no buzzer.
#' @param max10000 Whether O900 can handle a maximum luminance of 10000
#'                    apostilbs instead of 4000. Check the settings in EyeSuite
#'
#' @return a list contianing:
#'  * error Empty string for all good, else error messages from Imo.
#'  * msg JSON Object with all of the other fields described in @ReturnMsg
#'           except 'error'.
#'    - msg$jovp Any messages that the JOVP sent back.
#'
#' @examples
#' chooseOpi("O900")
#' result <- opiInitialise(ip = "192.126.0.1", port = 50000,
#'                         ip_Monitor = "localhost", port_Monitor = 50001,
#'                         eye = "left", eyeSuite = "C:/XXX/eyeSuite/",
#'                         gazeFeed = "C:/XXX/gazeFeed/", bigWheel = 0, pres = 0,
#'                         resp = 0, max10000 = 0)
#'
#' @seealso [opiInitialise()]
#'
opiInitialise_for_O900 <- function(ip = NULL, port = NULL, ip_Monitor = NULL, port_Monitor = NULL, eye = NULL, eyeSuite = NULL, gazeFeed = NULL, bigWheel = NULL, pres = NULL, resp = NULL, max10000 = NULL) {
    .opi_env$O900$socket <<- open_socket(ip_Monitor, port_Monitor)
    msg <- list(ip = ip, port = port, ip_Monitor = ip_Monitor, port_Monitor = port_Monitor, eye = eye, eyeSuite = eyeSuite, gazeFeed = gazeFeed, bigWheel = bigWheel, pres = pres, resp = resp, max10000 = max10000);
    msg <- rjson::toJSON(msg)
    writeLines(msg, .opi_env$O900$socket)

    res <- rjson::fromJSON(readLines(.opi_env$O900$socket, n=1))
    return(res)
}

#' Implementation of opiPresent for the O900 machine.
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
#'  * error Empty string for all good, else error messages from ImoVifa.
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
#' chooseOpi("O900")
#' result <- opiPresent(stim = list(x = list(0), y = list(0), t = list(200), w = 1500, lum = 20,
#'                      size = "GV", color = "white"))
#'
#' @seealso [opiPresent()]
#'
opiPresent_for_O900 <- function(stim = list(x = NULL, y = NULL, t = NULL, w = NULL, lum = NULL, size = NULL, color = NULL)) {
if(!exists(".opi_env$O900$socket") || is.null(.opi_env$O900$socket))
    stop("Cannot call opiPresent without an open socket to Monitor. Did you call opiInitialise()?.")

    msg <- list(x = stim$x, y = stim$y, t = stim$t, w = stim$w, lum = stim$lum, size = stim$size, color = stim$color);
    msg <- rjson::toJSON(msg)
    writeLines(msg, .opi_env$O900$socket)

    res <- rjson::fromJSON(readLines(.opi_env$O900$socket, n=1))
    return(res)
}

#' Implementation of opiSetup for the O900 machine.
#'
#' This is for internal use only. Use [opiSetup()] with
#' these Arguments and you will get the Value back.
#'
#' @usage NULL
#'
#' @param bgLum Background luminance for eye.
#' @param bgCol Background color for eye.
#' @param fixType Fixation target type for eye.
#' @param fixLum Fixation luminance color for eye (from 0% to 100%).
#' @param f310 Whether to use Logitech's F310 controlles
#'
#' @return a list contianing:
#'  * error Empty string for all good, else error messages from ImoVifa.
#'  * msg JSON Object with all of the other fields described in @ReturnMsg
#'           except 'error'.
#'    - msg$jovp Any messages that the JOVP sent back.
#'
#' @examples
#' chooseOpi("O900")
#' result <- opiSetup(settings = list(bgLum = "white", bgCol = "white", fixType = "center",
#'                    fixLum = 50, f310 = 0))
#'
#' @seealso [opiSetup()]
#'
opiSetup_for_O900 <- function(settings = list(bgLum = NULL, bgCol = NULL, fixType = NULL, fixLum = NULL, f310 = NULL)) {
if(!exists(".opi_env$O900$socket") || is.null(.opi_env$O900$socket))
    stop("Cannot call opiSetup without an open socket to Monitor. Did you call opiInitialise()?.")

    msg <- list(bgLum = settings$bgLum, bgCol = settings$bgCol, fixType = settings$fixType, fixLum = settings$fixLum, f310 = settings$f310);
    msg <- rjson::toJSON(msg)
    writeLines(msg, .opi_env$O900$socket)

    res <- rjson::fromJSON(readLines(.opi_env$O900$socket, n=1))
    return(res)
}

#' Implementation of opiClose for the O900 machine.
#'
#' This is for internal use only. Use [opiClose()] with
#' these Arguments and you will get the Value back.
#'
#' @usage NULL
#'
#'
#'
#' @return a list contianing:
#'  * error Empty string for all good, else error messages from Imo.
#'  * msg JSON Object with all of the other fields described in @ReturnMsg
#'           except 'error'.
#'    - msg$jovp Any messages that the JOVP sent back.
#'
#' @examples
#' chooseOpi("O900")
#' result <- opiClose()
#'
#' @seealso [opiClose()]
#'
opiClose_for_O900 <- function() {
if(!exists(".opi_env$O900$socket") || is.null(.opi_env$O900$socket))
    stop("Cannot call opiClose without an open socket to Monitor. Did you call opiInitialise()?.")

    msg <- list();
    msg <- rjson::toJSON(msg)
    writeLines(msg, .opi_env$O900$socket)

    res <- rjson::fromJSON(readLines(.opi_env$O900$socket, n=1))
    return(res)
}

#' Implementation of opiQueryDevice for the O900 machine.
#'
#' This is for internal use only. Use [opiQueryDevice()] with
#' these Arguments and you will get the Value back.
#'
#' @usage NULL
#'
#'
#'
#' @return a list contianing:
#'  * error Empty string for all good, else error message.
#'  * msg JSON Object with all of the other fields described in @ReturnMsg
#'           except 'error'.
#'    - msg$jovp Any messages that the JOVP sent back.
#'
#' @examples
#' chooseOpi("O900")
#' result <- opiQueryDevice()
#'
#' @seealso [opiQueryDevice()]
#'
opiQueryDevice_for_O900 <- function() {
if(!exists(".opi_env$O900$socket") || is.null(.opi_env$O900$socket))
    stop("Cannot call opiQueryDevice without an open socket to Monitor. Did you call opiInitialise()?.")

    msg <- list();
    msg <- rjson::toJSON(msg)
    writeLines(msg, .opi_env$O900$socket)

    res <- rjson::fromJSON(readLines(.opi_env$O900$socket, n=1))
    return(res)
}


#' Set background color and luminance in both eyes.
#' Deprecated for ImoVifa and replaced with [opiSetup()].
#' @usage NULL
#' @seealso [opiSetup()]
opiSetBackground_for_ImoVifa <- function(lum, color, ...) {return("Deprecated")}


