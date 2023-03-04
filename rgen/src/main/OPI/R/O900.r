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
#' @param ip IP Address of the OPI Monitor.
#' @param port TCP port of the OPI Monitor.
#'
#' @return a list contianing:
#'  * res List with all of the other fields described in @ReturnMsg except
#'           'error'.
#'    - res$error Error code '0' if all good, something else otherwise.
#'    - res$msg The success or error message.
#'
#' @examples
#' chooseOpi("O900")
#' result <- opiInitialise(ip = "localhost", port = 50001)
#'
#' @seealso [opiInitialise()]
#'
opiInitialise_for_O900 <- function(ip = NULL, port = NULL) {
    if (!exists("socket", where = .opi_env$O900))
        assign("socket", open_socket(ip, port), .opi_env$O900)
    else
        return(list(error = 4, msg = "Socket connection to Monitor already exists. Perhaps not closed properly last time? Restart Monitor and R."))

    msg <- list(ip = ip, port = port)
    msg <- c(list(command = "initialize"), msg)
    msg <- msg[!unlist(lapply(msg, is.null))]
    msg <- rjson::toJSON(msg)
    writeLines(msg, .opi_env$O900$socket)

    res <- readLines(.opi_env$O900$socket, n = 1)
    if (length(res) == 0)
        return(list(error = 5, msg = "Monitor server exists but a connection was not closed properly using opiClose() last time it was used. Restart Monitor."))
    res <- rjson::fromJSON(res)
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
#'  * res List with all of the other fields described in @ReturnMsg except
#'           'error'.
#'    - res$error '0' if success, something else if error.
#'    - res$msg The error message or a structure with the following data.
#'
#' @examples
#' chooseOpi("O900")
#' result <- opiQueryDevice()
#'
#' @seealso [opiQueryDevice()]
#'
opiQueryDevice_for_O900 <- function() {
if(!exists(".opi_env") || !exists("O900", envir = .opi_env) || !("socket" %in% names(.opi_env$O900)) || is.null(.opi_env$O900$socket))
    stop("Cannot call opiQueryDevice without an open socket to Monitor. Did you call opiInitialise()?.")

    msg <- list()
    msg <- c(list(command = "query"), msg)
    msg <- msg[!unlist(lapply(msg, is.null))]
    msg <- rjson::toJSON(msg)
    writeLines(msg, .opi_env$O900$socket)

    res <- readLines(.opi_env$O900$socket, n = 1)
    if (length(res) == 0)
        return(list(error = 5, msg = "Monitor server exists but a connection was not closed properly using opiClose() last time it was used. Restart Monitor."))
    res <- rjson::fromJSON(res)
    return(res)
}

#' Implementation of opiSetup for the O900 machine.
#'
#' This is for internal use only. Use [opiSetup()] with
#' these Arguments and you will get the Value back.
#'
#' @usage NULL
#'
#' @param eye Eye to set.
#' @param bgLum Background luminance for eye.
#' @param bgCol Background color for eye.
#' @param fixShape Fixation target.
#' @param fixIntensity Fixation intensity(from 0% to 100%).
#' @param pres Volume for auditory feedback when a stimulus is presented: 0
#'                means no buzzer.
#' @param resp Volume for auditory feedback when observer presses the clicker: 0
#'                means no buzzer.
#'
#' @return a list contianing:
#'  * res List with all of the other fields described in @ReturnMsg except
#'           'error'.
#'    - res$error '0' if success, something else if error.
#'    - res$msg The error message or a structure with the result of QUERY OPI
#'                 command.
#'
#' @examples
#' chooseOpi("O900")
#' result <- opiSetup(settings = list(eye = "left", bgLum = "10", bgCol = "white",
#'                    fixShape = "center", fixIntensity = 50, pres = 0, resp = 0))
#'
#' @seealso [opiSetup()]
#'
opiSetup_for_O900 <- function(settings = list(eye = NULL, bgLum = NULL, bgCol = NULL, fixShape = NULL, fixIntensity = NULL, pres = NULL, resp = NULL)) {
if(!exists(".opi_env") || !exists("O900", envir = .opi_env) || !("socket" %in% names(.opi_env$O900)) || is.null(.opi_env$O900$socket))
    stop("Cannot call opiSetup without an open socket to Monitor. Did you call opiInitialise()?.")

    msg <- list(eye = settings$eye, bgLum = settings$bgLum, bgCol = settings$bgCol, fixShape = settings$fixShape, fixIntensity = settings$fixIntensity, pres = settings$pres, resp = settings$resp)
    msg <- c(list(command = "setup"), msg)
    msg <- msg[!unlist(lapply(msg, is.null))]
    msg <- rjson::toJSON(msg)
    writeLines(msg, .opi_env$O900$socket)

    res <- readLines(.opi_env$O900$socket, n = 1)
    if (length(res) == 0)
        return(list(error = 5, msg = "Monitor server exists but a connection was not closed properly using opiClose() last time it was used. Restart Monitor."))
    res <- rjson::fromJSON(res)
    return(res)
}

#' Implementation of opiPresent for the O900 machine.
#'
#' This is for internal use only. Use [opiPresent()] with
#' these Arguments and you will get the Value back.
#'
#' @usage NULL
#'
#' @param type Stimulus type: STATIC or KINETIC.
#' @param x List of x co-ordinates of stimuli (degrees).
#' @param y List of y co-ordinates of stimuli (degrees).
#' @param lum List of stimuli luminances (cd/m^2).
#' @param size Stimulus size (degrees). Can be Goldmann Size I to V (or VI if
#'                device has a big wheel)
#' @param color Stimulus color (degrees).
#' @param t List of Stimulus presentation times (ms). For STATIC, list must be
#'             of length 1. For KINETIC, it must the same length and 'x' and 'y'
#'             co-ordinates minus 1
#' @param w [STATIC] Response window (ms).
#'
#' @return a list contianing:
#'  * res List with all of the other fields described in @ReturnMsg except
#'           'error'.
#'    - res$error '0' if success, something else if error.
#'    - res$msg Error message or a structure with the following fields.
#'    - res$msg$seen '1' if seen, '0' if not.
#'    - res$msg$time Response time from stimulus onset if button pressed (ms).
#'    - res$msg$eyex x co-ordinates of pupil at times eyet (degrees).
#'    - res$msg$eyey y co-ordinates of pupil at times eyet (degrees).
#'    - res$msg$x [KINETIC] x co-ordinate when oberver responded (degrees).
#'    - res$msg$y [KINETIC] y co-ordinate when oberver responded (degrees).
#'
#' @examples
#' chooseOpi("O900")
#' result <- opiPresent(stim = list(type = "static", x = list(0), y = list(0), lum = 3183.099,
#'                      size = "list('GV')", color = "white"))
#'
#' @seealso [opiPresent()]
#'
opiPresent_for_O900 <- function(stim = list(type = NULL, x = NULL, y = NULL, lum = NULL, size = NULL, color = NULL, t = NULL, w = NULL)) {
if(!exists(".opi_env") || !exists("O900", envir = .opi_env) || !("socket" %in% names(.opi_env$O900)) || is.null(.opi_env$O900$socket))
    stop("Cannot call opiPresent without an open socket to Monitor. Did you call opiInitialise()?.")

    msg <- list(type = stim$type, x = stim$x, y = stim$y, lum = stim$lum, size = stim$size, color = stim$color, t = stim$t, w = stim$w)
    msg <- c(list(command = "present"), msg)
    msg <- msg[!unlist(lapply(msg, is.null))]
    msg <- rjson::toJSON(msg)
    writeLines(msg, .opi_env$O900$socket)

    res <- readLines(.opi_env$O900$socket, n = 1)
    if (length(res) == 0)
        return(list(error = 5, msg = "Monitor server exists but a connection was not closed properly using opiClose() last time it was used. Restart Monitor."))
    res <- rjson::fromJSON(res)
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
#'  * res List with all of the other fields described in @ReturnMsg except
#'           'error'.
#'    - res$error '0' if success, something else if error.
#'    - res$msg The error message or additional results from the CLOSE command
#'
#' @examples
#' chooseOpi("O900")
#' result <- opiClose()
#'
#' @seealso [opiClose()]
#'
opiClose_for_O900 <- function() {
if(!exists(".opi_env") || !exists("O900", envir = .opi_env) || !("socket" %in% names(.opi_env$O900)) || is.null(.opi_env$O900$socket))
    stop("Cannot call opiClose without an open socket to Monitor. Did you call opiInitialise()?.")

    msg <- list()
    msg <- c(list(command = "close"), msg)
    msg <- msg[!unlist(lapply(msg, is.null))]
    msg <- rjson::toJSON(msg)
    writeLines(msg, .opi_env$O900$socket)

    res <- readLines(.opi_env$O900$socket, n = 1)
    if (length(res) == 0)
        return(list(error = 5, msg = "Monitor server exists but a connection was not closed properly using opiClose() last time it was used. Restart Monitor."))
    res <- rjson::fromJSON(res)
    return(res)
}


#' Set background color and luminance in both eyes.
#' Deprecated for OPI >= v3.0.0 and replaced with [opiSetup()].
#' @usage NULL
#' @seealso [opiSetup()]
opiSetBackground_for_O900 <- function(lum, color, ...) {return("Deprecated")}


