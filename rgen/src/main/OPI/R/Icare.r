# Open Perimetry Interface implementation for Icare
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
if (exists(".opi_env") && !exists("Icare", where = .opi_env))
    assign("Icare", new.env(), envir = .opi_env)

#' Implementation of opiInitialise for the Icare machine.
#'
#' This is for internal use only. Use [opiInitialise()] with
#' these Arguments and you will get the Value back.
#'
#' @usage NULL
#'
#' @param ipMonitor IP Address of the OPI monitor.
#' @param portMonitor TCP port of the OPI monitor.
#' @param ip IP Address of the OPI machine.
#' @param port TCP port of the OPI machine.
#'
#' @return a list contianing:
#'  * res JSON Object with all of the other fields described in @ReturnMsg
#'           except 'error'.
#'    - res$error Error code '0' if all good, '1' something wrong.
#'    - res$msg The success or error message.
#'
#' @examples
#' chooseOpi("Icare")
#' result <- opiInitialise(null)
#'
#' @seealso [opiInitialise()]
#'
opiInitialise_for_Icare <- function(ipMonitor = NULL, portMonitor = NULL, ip = NULL, port = NULL) {
    assign("socket", open_socket(ipMonitor, portMonitor), .opi_env$Icare)

    msg <- list(command = "choose", machine = "Icare")
    msg <- rjson::toJSON(msg)
    writeLines(msg, .opi_env$Jovp$socket)
    res <- readLines(.opi_env$Jovp$socket, n = 1)
    res <- rjson::fromJSON(res)

    msg <- list(ipMonitor = ipMonitor, portMonitor = portMonitor, ip = ip, port = port)
    msg <- c(list(command = "initialize"), msg)
    msg <- msg[!unlist(lapply(msg, is.null))]
    msg <- rjson::toJSON(msg)
    writeLines(msg, .opi_env$Icare$socket)

    res <- rjson::fromJSON(readLines(.opi_env$Icare$socket, n = 1))
    return(res)
}

#' Implementation of opiQueryDevice for the Icare machine.
#'
#' This is for internal use only. Use [opiQueryDevice()] with
#' these Arguments and you will get the Value back.
#'
#' @usage NULL
#'
#'
#'
#' @return a list contianing:
#'  * res JSON Object with all of the other fields described in @ReturnMsg
#'           except 'error'.
#'    - res$error '0' if success, '1' if error.
#'    - res$msg The error message or a structure with the following data.
#'
#' @examples
#' chooseOpi("Icare")
#' result <- opiQueryDevice(null)
#'
#' @seealso [opiQueryDevice()]
#'
opiQueryDevice_for_Icare <- function() {
if(!exists(".opi_env") || !exists("Icare", envir = .opi_env) || !("socket" %in% names(.opi_env$Icare)) || is.null(.opi_env$Icare$socket))
    stop("Cannot call opiQueryDevice without an open socket to Monitor. Did you call opiInitialise()?.")

    msg <- list()
    msg <- c(list(command = "query"), msg)
    msg <- msg[!unlist(lapply(msg, is.null))]
    msg <- rjson::toJSON(msg)
    writeLines(msg, .opi_env$Icare$socket)

    res <- rjson::fromJSON(readLines(.opi_env$Icare$socket, n = 1))
    return(res)
}

#' Implementation of opiSetup for the Icare machine.
#'
#' This is for internal use only. Use [opiSetup()] with
#' these Arguments and you will get the Value back.
#'
#' @usage NULL
#'
#' @param fixShape Fixation target type for eye.
#' @param fixCx x-coordinate of fixation target (degrees): Only valid values are
#'                 -20, -6, -3, 0, 3, 6, 20 for fixation type 'spot' and -3, 0, 3
#'                 for fixation type 'square'.
#' @param tracking Whether to correct stimulus location based on eye position.
#'
#' @return a list contianing:
#'  * res JSON Object with all of the other fields described in @ReturnMsg
#'           except 'error'.
#'    - res$error '0' if success, '1' if error.
#'    - res$msg The error message or a structure with the result of QUERY OPI
#'                 command.
#'
#' @examples
#' chooseOpi("Icare")
#' result <- opiSetup(settings = list(null))
#'
#' @seealso [opiSetup()]
#'
opiSetup_for_Icare <- function(settings = list(fixShape = NULL, fixCx = NULL, tracking = NULL)) {
if(!exists(".opi_env") || !exists("Icare", envir = .opi_env) || !("socket" %in% names(.opi_env$Icare)) || is.null(.opi_env$Icare$socket))
    stop("Cannot call opiSetup without an open socket to Monitor. Did you call opiInitialise()?.")

    msg <- list(fixShape = settings$fixShape, fixCx = settings$fixCx, tracking = settings$tracking)
    msg <- c(list(command = "setup"), msg)
    msg <- msg[!unlist(lapply(msg, is.null))]
    msg <- rjson::toJSON(msg)
    writeLines(msg, .opi_env$Icare$socket)

    res <- rjson::fromJSON(readLines(.opi_env$Icare$socket, n = 1))
    return(res)
}

#' Implementation of opiPresent for the Icare machine.
#'
#' This is for internal use only. Use [opiPresent()] with
#' these Arguments and you will get the Value back.
#'
#' @usage NULL
#'
#' @param x x co-ordinates of stimulus (degrees).
#' @param y y co-ordinates of stimulus (degrees).
#' @param lum Stimuli luminance (cd/m^2).
#' @param t Presentation time (ms).
#' @param w Response window (ms).
#'
#' @return a list contianing:
#'    - res$error '0' if success, '1' if error.
#'    - res$msg Error message or a structure with the following fields.
#'    - res$msg$seen '1' if seen, '0' if not.
#'    - res$msg$time Response time from stimulus onset if button pressed (ms).
#'  * res JSON Object with all of the other fields described in @ReturnMsg
#'           except 'error'.
#'    - res$eyex x co-ordinates of pupil at times eyet (pixels).
#'    - res$eyey y co-ordinates of pupil at times eyet (pixels).
#'    - res$eyed Diameter of pupil at times eyet (mm).
#'    - res$eyet Time of (eyex, eyey) pupil from stimulus onset (ms).
#'    - res$time_rec Time since 'epoch' when command was received at Compass or
#'                      Maia (ms).
#'    - res$time_resp Time since 'epoch' when stimulus response is received, or
#'                       response window expired (ms).
#'    - res$num_track_events Number of tracking events that occurred during
#'                              presentation.
#'    - res$num_motor_fails Number of times motor could not follow fixation
#'                             movement during presentation.
#'
#' @examples
#' chooseOpi("Icare")
#' result <- opiPresent(stim = list(null))
#'
#' @seealso [opiPresent()]
#'
opiPresent_for_Icare <- function(stim = list(x = NULL, y = NULL, lum = NULL, t = NULL, w = NULL)) {
if(!exists(".opi_env") || !exists("Icare", envir = .opi_env) || !("socket" %in% names(.opi_env$Icare)) || is.null(.opi_env$Icare$socket))
    stop("Cannot call opiPresent without an open socket to Monitor. Did you call opiInitialise()?.")

    msg <- list(x = stim$x, y = stim$y, lum = stim$lum, t = stim$t, w = stim$w)
    msg <- c(list(command = "present"), msg)
    msg <- msg[!unlist(lapply(msg, is.null))]
    msg <- rjson::toJSON(msg)
    writeLines(msg, .opi_env$Icare$socket)

    res <- rjson::fromJSON(readLines(.opi_env$Icare$socket, n = 1))
    return(res)
}

#' Implementation of opiClose for the Icare machine.
#'
#' This is for internal use only. Use [opiClose()] with
#' these Arguments and you will get the Value back.
#'
#' @usage NULL
#'
#'
#'
#' @return a list contianing:
#'  * res JSON Object with all of the other fields described in @ReturnMsg
#'           except 'error'.
#'    - res$error '0' if success, '1' if error.
#'    - res$msg The error message or additional results from the CLOSE command
#'    - res$time The time stamp for fixation data
#'    - res$x The time stamp for fixation data
#'    - res$y The time stamp for fixation data
#'
#' @examples
#' chooseOpi("Icare")
#' result <- opiClose(null)
#'
#' @seealso [opiClose()]
#'
opiClose_for_Icare <- function() {
if(!exists(".opi_env") || !exists("Icare", envir = .opi_env) || !("socket" %in% names(.opi_env$Icare)) || is.null(.opi_env$Icare$socket))
    stop("Cannot call opiClose without an open socket to Monitor. Did you call opiInitialise()?.")

    msg <- list()
    msg <- c(list(command = "close"), msg)
    msg <- msg[!unlist(lapply(msg, is.null))]
    msg <- rjson::toJSON(msg)
    writeLines(msg, .opi_env$Icare$socket)

    res <- rjson::fromJSON(readLines(.opi_env$Icare$socket, n = 1))
    return(res)
}


#' Set background color and luminance in both eyes.
#' Deprecated for OPI >= v3.0.0 and replaced with [opiSetup()].
#' @usage NULL
#' @seealso [opiSetup()]
opiSetBackground_for_Icare <- function(lum, color, ...) {return("Deprecated")}


