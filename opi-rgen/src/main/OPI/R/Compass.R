# Open Perimetry Interface implementation for Compass
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
if (exists(".opi_env") && !exists("Compass", where = .opi_env))
    assign("Compass", new.env(), envir = .opi_env)

#' Implementation of opiInitialise for the Compass machine.
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
#' chooseOpi("Compass")
#' result <- opiInitialise(ip = "192.126.0.1", port = 50000,
#'                         ip_Monitor = "localhost", port_Monitor = 50001,
#'                         eye = "left")
#'
#' @seealso [opiInitialise()]
#'
opiInitialise_for_Compass <- function(ip = NULL, port = NULL, ip_Monitor = NULL, port_Monitor = NULL, eye = NULL) {
    .opi_env$Compass$socket <<- open_socket(ip_Monitor, port_Monitor)
    msg <- list(ip = ip, port = port, ip_Monitor = ip_Monitor, port_Monitor = port_Monitor, eye = eye);
    msg <- rjson::toJSON(msg)
    writeLines(msg, .opi_env$Compass$socket)

    res <- rjson::fromJSON(readLines(.opi_env$Compass$socket, n=1))
    return(res)
}

#' Implementation of opiPresent for the Compass machine.
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
#' chooseOpi("Compass")
#' result <- opiPresent(stim = list(x = list(0), y = list(0), t = list(200), w = list(1500),
#'                      lum = list(20)))
#'
#' @seealso [opiPresent()]
#'
opiPresent_for_Compass <- function(stim = list(x = NULL, y = NULL, t = NULL, w = NULL, lum = NULL)) {
if(!exists(".opi_env$Compass$socket") || is.null(.opi_env$Compass$socket))
    stop("Cannot call opiPresent without an open socket to Monitor. Did you call opiInitialise()?.")

    msg <- list(x = stim$x, y = stim$y, t = stim$t, w = stim$w, lum = stim$lum);
    msg <- rjson::toJSON(msg)
    writeLines(msg, .opi_env$Compass$socket)

    res <- rjson::fromJSON(readLines(.opi_env$Compass$socket, n=1))
    return(res)
}

#' Implementation of opiSetup for the Compass machine.
#'
#' This is for internal use only. Use [opiSetup()] with
#' these Arguments and you will get the Value back.
#'
#' @usage NULL
#'
#' @param fixType Fixation target type for eye.
#' @param fixCx x-coordinate of fixation target (degrees).
#' @param tracking Whether to correct stimulus location based on eye position.
#'
#' @return a list contianing:
#'  * error Empty string for all good, else error messages from ImoVifa.
#'  * msg JSON Object with all of the other fields described in @ReturnMsg
#'           except 'error'.
#'    - msg$jovp Any messages that the JOVP sent back.
#'
#' @examples
#' chooseOpi("Compass")
#' result <- opiSetup(settings = list(fixType = "maltese", fixCx = 0, tracking = 0))
#'
#' @seealso [opiSetup()]
#'
opiSetup_for_Compass <- function(settings = list(fixType = NULL, fixCx = NULL, tracking = NULL)) {
if(!exists(".opi_env$Compass$socket") || is.null(.opi_env$Compass$socket))
    stop("Cannot call opiSetup without an open socket to Monitor. Did you call opiInitialise()?.")

    msg <- list(fixType = settings$fixType, fixCx = settings$fixCx, tracking = settings$tracking);
    msg <- rjson::toJSON(msg)
    writeLines(msg, .opi_env$Compass$socket)

    res <- rjson::fromJSON(readLines(.opi_env$Compass$socket, n=1))
    return(res)
}

#' Implementation of opiClose for the Compass machine.
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
#' chooseOpi("Compass")
#' result <- opiClose()
#'
#' @seealso [opiClose()]
#'
opiClose_for_Compass <- function() {
if(!exists(".opi_env$Compass$socket") || is.null(.opi_env$Compass$socket))
    stop("Cannot call opiClose without an open socket to Monitor. Did you call opiInitialise()?.")

    msg <- list();
    msg <- rjson::toJSON(msg)
    writeLines(msg, .opi_env$Compass$socket)

    res <- rjson::fromJSON(readLines(.opi_env$Compass$socket, n=1))
    return(res)
}

#' Implementation of opiQueryDevice for the Compass machine.
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
#' chooseOpi("Compass")
#' result <- opiQueryDevice()
#'
#' @seealso [opiQueryDevice()]
#'
opiQueryDevice_for_Compass <- function() {
if(!exists(".opi_env$Compass$socket") || is.null(.opi_env$Compass$socket))
    stop("Cannot call opiQueryDevice without an open socket to Monitor. Did you call opiInitialise()?.")

    msg <- list();
    msg <- rjson::toJSON(msg)
    writeLines(msg, .opi_env$Compass$socket)

    res <- rjson::fromJSON(readLines(.opi_env$Compass$socket, n=1))
    return(res)
}


#' Set background color and luminance in both eyes.
#' Deprecated for ImoVifa and replaced with [opiSetup()].
#' @usage NULL
#' @seealso [opiSetup()]
opiSetBackground_for_ImoVifa <- function(lum, color, ...) {return("Deprecated")}


