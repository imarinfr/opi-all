# Open Perimetry Interface implementation for Kowa
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
if (exists(".opi_env") && !exists("Kowa", where = .opi_env))
    assign("Kowa", new.env(), envir = .opi_env)

#' Implementation of opiInitialise for the Kowa machine.
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
#'
#' @return a list contianing:
#'  * error Empty string for all good, else error messages from Imo.
#'  * msg JSON Object with all of the other fields described in @ReturnMsg
#'           except 'error'.
#'    - msg$jovp Any messages that the JOVP sent back.
#'
#' @examples
#' chooseOpi("Kowa")
#' result <- opiInitialise(null)
#'
#' @seealso [opiInitialise()]
#'
opiInitialise_for_Kowa <- function(ip = NULL, port = NULL, ip_Monitor = NULL, port_Monitor = NULL) {
    assign("socket", open_socket(ip_Monitor, port_Monitor), .opi_env$Kowa)
    msg <- list(ip = ip, port = port, ip_Monitor = ip_Monitor, port_Monitor = port_Monitor)
    msg <- rjson::toJSON(msg)
    writeLines(msg, .opi_env$Kowa$socket)

    res <- rjson::fromJSON(readLines(.opi_env$Kowa$socket, n=1))
    return(res)
}

#' Implementation of opiQueryDevice for the Kowa machine.
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
#' chooseOpi("Kowa")
#' result <- opiQueryDevice(null)
#'
#' @seealso [opiQueryDevice()]
#'
opiQueryDevice_for_Kowa <- function() {
if(!exists(".opi_env$Kowa") || !exists(".opi_env$Kowa$socket") || is.null(.opi_env$Kowa$socket))
    stop("Cannot call opiQueryDevice without an open socket to Monitor. Did you call opiInitialise()?.")

    msg <- list()
    msg <- rjson::toJSON(msg)
    writeLines(msg, .opi_env$Kowa$socket)

    res <- rjson::fromJSON(readLines(.opi_env$Kowa$socket, n=1))
    return(res)
}

#' Implementation of opiSetup for the Kowa machine.
#'
#' This is for internal use only. Use [opiSetup()] with
#' these Arguments and you will get the Value back.
#'
#' @usage NULL
#'
#' @param bgLum Background luminance for eye.
#' @param bgCol Background color for eye.
#' @param fixType Fixation target type for eye.
#'
#' @return a list contianing:
#'  * error Empty string for all good, else error messages from ImoVifa.
#'  * msg JSON Object with all of the other fields described in @ReturnMsg
#'           except 'error'.
#'    - msg$jovp Any messages that the JOVP sent back.
#'
#' @examples
#' chooseOpi("Kowa")
#' result <- opiSetup(settings = list(null))
#'
#' @seealso [opiSetup()]
#'
opiSetup_for_Kowa <- function(settings = list(bgLum = NULL, bgCol = NULL, fixType = NULL)) {
if(!exists(".opi_env$Kowa") || !exists(".opi_env$Kowa$socket") || is.null(.opi_env$Kowa$socket))
    stop("Cannot call opiSetup without an open socket to Monitor. Did you call opiInitialise()?.")

    msg <- list(bgLum = settings$bgLum, bgCol = settings$bgCol, fixType = settings$fixType)
    msg <- rjson::toJSON(msg)
    writeLines(msg, .opi_env$Kowa$socket)

    res <- rjson::fromJSON(readLines(.opi_env$Kowa$socket, n=1))
    return(res)
}

#' Implementation of opiPresent for the Kowa machine.
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
#' @param size Stimulus size (degrees).
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
#' chooseOpi("Kowa")
#' result <- opiPresent(stim = list(null))
#'
#' @seealso [opiPresent()]
#'
opiPresent_for_Kowa <- function(stim = list(x = NULL, y = NULL, t = NULL, w = NULL, lum = NULL, size = NULL, color = NULL)) {
if(!exists(".opi_env$Kowa") || !exists(".opi_env$Kowa$socket") || is.null(.opi_env$Kowa$socket))
    stop("Cannot call opiPresent without an open socket to Monitor. Did you call opiInitialise()?.")

    msg <- list(x = stim$x, y = stim$y, t = stim$t, w = stim$w, lum = stim$lum, size = stim$size, color = stim$color)
    msg <- rjson::toJSON(msg)
    writeLines(msg, .opi_env$Kowa$socket)

    res <- rjson::fromJSON(readLines(.opi_env$Kowa$socket, n=1))
    return(res)
}

#' Implementation of opiClose for the Kowa machine.
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
#' chooseOpi("Kowa")
#' result <- opiClose(null)
#'
#' @seealso [opiClose()]
#'
opiClose_for_Kowa <- function() {
if(!exists(".opi_env$Kowa") || !exists(".opi_env$Kowa$socket") || is.null(.opi_env$Kowa$socket))
    stop("Cannot call opiClose without an open socket to Monitor. Did you call opiInitialise()?.")

    msg <- list()
    msg <- rjson::toJSON(msg)
    writeLines(msg, .opi_env$Kowa$socket)

    res <- rjson::fromJSON(readLines(.opi_env$Kowa$socket, n=1))
    return(res)
}


#' Set background color and luminance in both eyes.
#' Deprecated for OPI >= v3.0.0 and replaced with [opiSetup()].
#' @usage NULL
#' @seealso [opiSetup()]
opiSetBackground_for_Kowa <- function(lum, color, ...) {return("Deprecated")}


