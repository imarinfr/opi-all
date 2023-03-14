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
#' @param port TCP port of the OPI Monitor.
#' @param ip IP Address of the OPI Monitor.
#'
#' @return a list contianing:
#'  * res List with all of the other fields described in @ReturnMsg except 'error'.
#'    - res$msg The success or error message.
#'    - res$error Error code '0' if all good, something else otherwise.
#'
#' @details 
#' `port` can take on values in the range [0, 65535].
#'
#' @examples
#' chooseOpi("O900")
#' result <- opiInitialise(address = list(port = 50001, ip = "localhost"))
#'
#' @seealso [opiInitialise()]
#'
opiInitialise_for_O900 <- function(address) {
    if (!exists("socket", where = .opi_env$O900))
        assign("socket", open_socket(address$ip, address$port), .opi_env$O900)
    else
        return(list(error = 4, msg = "Socket connection to Monitor already exists. Perhaps not closed properly last time? Restart Monitor and R."))

    if (is.null(address)) return(list(error = 0 , msg = "Nothing to do in opiInitialise."))

    msg <- list(port = address$port, ip = address$ip)
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
#'  * res List with all of the other fields described in @ReturnMsg except 'error'.
#'    - res$msg The error message or a structure with the following data.
#'    - res$error '0' if success, something else if error.
#'
#'
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
#' @param fixShape Fixation target.
#' @param pres Volume for auditory feedback when a stimulus is presented: 0 means no buzzer.
#' @param resp Volume for auditory feedback when observer presses the clicker: 0
#'             means no buzzer.
#' @param fixIntensity Fixation intensity(from 0% to 100%).
#' @param bgLum Background luminance for eye.
#' @param bgCol Background color for eye.
#'
#' @return a list contianing:
#'  * res List with all of the other fields described in @ReturnMsg except 'error'.
#'    - res$msg The error message or a structure with the result of QUERY OPI command.
#'    - res$error '0' if success, something else if error.
#'
#' @details 
#' `eye` can take on values in the set {left, right}.
#' `fixShape` can take on values in the set {center, cross, ring}.
#' `pres` can take on values in the range [0.0, 3.0].
#' `resp` can take on values in the range [0.0, 3.0].
#' `fixIntensity` can take on values in the range [0.0, 100.0].
#' `bgLum` can take on values in the set {bg_off, bg_1, bg_10, bg_100}.
#' `bgCol` can take on values in the set {white, yellow}.
#'
#' @examples
#' chooseOpi("O900")
#' result <- opiSetup(settings = list(eye = "null", fixShape = "null", pres = 0.0, resp = 0.0,
#'                 fixIntensity = 50.0, bgLum = "null", bgCol = "null"))
#'
#' @seealso [opiSetup()]
#'
opiSetup_for_O900 <- function(settings) {
    if(!exists(".opi_env") || !exists("O900", envir = .opi_env) || !("socket" %in% names(.opi_env$O900)) || is.null(.opi_env$O900$socket))
        stop("Cannot call opiSetup without an open socket to Monitor. Did you call opiInitialise()?.")

    if (is.null(settings)) return(list(error = 0 , msg = "Nothing to do in opiSetup."))

    msg <- list(eye = settings$eye, fixShape = settings$fixShape, pres = settings$pres, resp = settings$resp, fixIntensity = settings$fixIntensity, bgLum = settings$bgLum, bgCol = settings$bgCol)
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
#' @param size Stimulus size (degrees). Can be Goldmann Size I to V (or VI if
#'             device has a big wheel)
#' @param color Stimulus color (degrees).
#' @param t List of Stimulus presentation times (ms). For STATIC, list must be
#'          of length 1. For KINETIC, it must the same length and 'x' and 'y'
#'          co-ordinates minus 1(Optional)
#' @param lum List of stimuli luminances (cd/m^2).
#' @param w [STATIC] Response window (ms).(Optional)
#' @param x List of x co-ordinates of stimuli (degrees).
#' @param y List of y co-ordinates of stimuli (degrees).
#' @param type Stimulus type: STATIC or KINETIC.
#' @param stim.length The number of elements in this stimuli.
#'
#' @return a list contianing:
#'  * res List with all of the other fields described in @ReturnMsg except 'error'.
#'    - res$msg$eyey y co-ordinates of pupil at times eyet (degrees).
#'    - res$msg$eyex x co-ordinates of pupil at times eyet (degrees).
#'    - res$msg$time Response time from stimulus onset if button pressed (ms).
#'    - res$msg Error message or a structure with the following fields.
#'    - res$error '0' if success, something else if error.
#'    - res$msg$seen '1' if seen, '0' if not.
#'    - res$msg$x [KINETIC] x co-ordinate when oberver responded (degrees).
#'    - res$msg$y [KINETIC] y co-ordinate when oberver responded (degrees).
#'
#' @details 
#' `size` can take on values in the set {gi, gii, giii, giv, gv, gvi}.
#' `color` can take on values in the set {white, red, blue}.
#' Elements in `t` can take on values in the range [0.0, 1.0E10].
#' `lum` can take on values in the range [0.0, 3183.099].
#' `w` can take on values in the range [0.0, 1.0E10].
#' Elements in `x` can take on values in the range [-90.0, 90.0].
#' Elements in `y` can take on values in the range [-90.0, 90.0].
#' `type` can take on values in the set {static, kinetic}.
#' `stim.length` can take on values in the range [1, 2147483647].
#'
#' @examples
#' chooseOpi("O900")
#' result <- opiPresent(stim = list(size = "GV", color = "null", lum = 3183.099, x = list(0.0),
#'                   y = list(0.0), type = "null", stim.length = 1))
#'
#' @seealso [opiPresent()]
#'
opiPresent_for_O900 <- function(stim) {
    if(!exists(".opi_env") || !exists("O900", envir = .opi_env) || !("socket" %in% names(.opi_env$O900)) || is.null(.opi_env$O900$socket))
        stop("Cannot call opiPresent without an open socket to Monitor. Did you call opiInitialise()?.")

    if (is.null(stim)) return(list(error = 0 , msg = "Nothing to do in opiPresent."))

    msg <- list(size = stim$size, color = stim$color, t = stim$t, lum = stim$lum, w = stim$w, x = stim$x, y = stim$y, type = stim$type, stim.length = stim$stim.length)
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
#'  * res List of result elements.
#'    - res$msg The error message or additional results from the CLOSE command
#'    - res$error '0' if success, something else if error.
#'
#'
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


