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

    # environment for this machine in R
if (exists(".opi_env") && !exists("O600", where = .opi_env))
    assign("O600", new.env(), envir = .opi_env)

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
#' result <- opiInitialise(null)
#'
#' @seealso [opiInitialise()]
#'
opiInitialise_for_O600 <- function(ip = NULL, port = NULL, ip_Monitor = NULL, port_Monitor = NULL, eye = NULL) {
    assign("socket", open_socket(ip_Monitor, port_Monitor), .opi_env$O600)
    msg <- list(ip = ip, port = port, ip_Monitor = ip_Monitor, port_Monitor = port_Monitor, eye = eye)
    msg <- rjson::toJSON(msg)
    writeLines(msg, .opi_env$O600$socket)

    res <- rjson::fromJSON(readLines(.opi_env$O600$socket, n=1))
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
#'
#' @return a list contianing:
#'  * error Empty string for all good, else error message.
#'  * msg JSON Object with all of the other fields described in @ReturnMsg
#'           except 'error'.
#'    - msg$jovp Any messages that the JOVP sent back.
#'
#' @examples
#' chooseOpi("O600")
#' result <- opiQueryDevice(null)
#'
#' @seealso [opiQueryDevice()]
#'
opiQueryDevice_for_O600 <- function() {
if(!exists(".opi_env$O600") || !exists(".opi_env$O600$socket") || is.null(.opi_env$O600$socket))
    stop("Cannot call opiQueryDevice without an open socket to Monitor. Did you call opiInitialise()?.")

    msg <- list()
    msg <- rjson::toJSON(msg)
    writeLines(msg, .opi_env$O600$socket)

    res <- rjson::fromJSON(readLines(.opi_env$O600$socket, n=1))
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
#' result <- opiSetup(settings = list(null))
#'
#' @seealso [opiSetup()]
#'
opiSetup_for_O600 <- function(settings = list(bgLum = NULL, fixType = NULL, fixLum = NULL, fixCol = NULL, tracking = NULL)) {
if(!exists(".opi_env$O600") || !exists(".opi_env$O600$socket") || is.null(.opi_env$O600$socket))
    stop("Cannot call opiSetup without an open socket to Monitor. Did you call opiInitialise()?.")

    msg <- list(bgLum = settings$bgLum, fixType = settings$fixType, fixLum = settings$fixLum, fixCol = settings$fixCol, tracking = settings$tracking)
    msg <- rjson::toJSON(msg)
    writeLines(msg, .opi_env$O600$socket)

    res <- rjson::fromJSON(readLines(.opi_env$O600$socket, n=1))
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
#' result <- opiPresent(stim = list(null))
#'
#' @seealso [opiPresent()]
#'
opiPresent_for_O600 <- function(stim = list(x = NULL, y = NULL, t = NULL, w = NULL, lum = NULL, size = NULL, color = NULL)) {
if(!exists(".opi_env$O600") || !exists(".opi_env$O600$socket") || is.null(.opi_env$O600$socket))
    stop("Cannot call opiPresent without an open socket to Monitor. Did you call opiInitialise()?.")

    msg <- list(x = stim$x, y = stim$y, t = stim$t, w = stim$w, lum = stim$lum, size = stim$size, color = stim$color)
    msg <- rjson::toJSON(msg)
    writeLines(msg, .opi_env$O600$socket)

    res <- rjson::fromJSON(readLines(.opi_env$O600$socket, n=1))
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
#'
#' @return a list contianing:
#'  * error Empty string for all good, else error messages from Imo.
#'  * msg JSON Object with all of the other fields described in @ReturnMsg
#'           except 'error'.
#'    - msg$jovp Any messages that the JOVP sent back.
#'
#' @examples
#' chooseOpi("O600")
#' result <- opiClose(null)
#'
#' @seealso [opiClose()]
#'
opiClose_for_O600 <- function() {
if(!exists(".opi_env$O600") || !exists(".opi_env$O600$socket") || is.null(.opi_env$O600$socket))
    stop("Cannot call opiClose without an open socket to Monitor. Did you call opiInitialise()?.")

    msg <- list()
    msg <- rjson::toJSON(msg)
    writeLines(msg, .opi_env$O600$socket)

    res <- rjson::fromJSON(readLines(.opi_env$O600$socket, n=1))
    return(res)
}


#' Set background color and luminance in both eyes.
#' Deprecated for OPI >= v3.0.0 and replaced with [opiSetup()].
#' @usage NULL
#' @seealso [opiSetup()]
opiSetBackground_for_O600 <- function(lum, color, ...) {return("Deprecated")}


