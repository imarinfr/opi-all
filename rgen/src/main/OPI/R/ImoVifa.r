# Open Perimetry Interface implementation for ImoVifa
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
if (exists(".opi_env") && !exists("ImoVifa", where = .opi_env))
    assign("ImoVifa", new.env(), envir = .opi_env)

#' Implementation of opiInitialise for the ImoVifa machine.
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
#'  * res List with all of the other fields described in @ReturnMsg except
#'           'error'.
#'    - res$msg The success or error message.
#'    - res$error Error code '0' if all good, something else otherwise.
#'
#' @examples
#' chooseOpi("ImoVifa")
#' result <- opiInitialise(port = 50001, ip = "localhost")
#'
#' @seealso [opiInitialise()]
#'
opiInitialise_for_ImoVifa <- function(port = NULL, ip = NULL) {
    if (!exists("socket", where = .opi_env$ImoVifa))
        assign("socket", open_socket(ip, port), .opi_env$ImoVifa)
    else
        return(list(error = 4, msg = "Socket connection to Monitor already exists. Perhaps not closed properly last time? Restart Monitor and R."))

    msg <- list(port = port, ip = ip)
    msg <- c(list(command = "initialize"), msg)
    msg <- msg[!unlist(lapply(msg, is.null))]
    msg <- rjson::toJSON(msg)
    writeLines(msg, .opi_env$ImoVifa$socket)

    res <- readLines(.opi_env$ImoVifa$socket, n = 1)
    if (length(res) == 0)
        return(list(error = 5, msg = "Monitor server exists but a connection was not closed properly using opiClose() last time it was used. Restart Monitor."))
    res <- rjson::fromJSON(res)
    return(res)
}

#' Implementation of opiQueryDevice for the ImoVifa machine.
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
#'    - res$msg The error message or a structure with the following data.
#'    - res$error '0' if success, something else if error.
#'
#' @examples
#' chooseOpi("ImoVifa")
#' result <- opiQueryDevice()
#'
#' @seealso [opiQueryDevice()]
#'
opiQueryDevice_for_ImoVifa <- function() {
if(!exists(".opi_env") || !exists("ImoVifa", envir = .opi_env) || !("socket" %in% names(.opi_env$ImoVifa)) || is.null(.opi_env$ImoVifa$socket))
    stop("Cannot call opiQueryDevice without an open socket to Monitor. Did you call opiInitialise()?.")

    msg <- list()
    msg <- c(list(command = "query"), msg)
    msg <- msg[!unlist(lapply(msg, is.null))]
    msg <- rjson::toJSON(msg)
    writeLines(msg, .opi_env$ImoVifa$socket)

    res <- readLines(.opi_env$ImoVifa$socket, n = 1)
    if (length(res) == 0)
        return(list(error = 5, msg = "Monitor server exists but a connection was not closed properly using opiClose() last time it was used. Restart Monitor."))
    res <- rjson::fromJSON(res)
    return(res)
}

#' Implementation of opiSetup for the ImoVifa machine.
#'
#' This is for internal use only. Use [opiSetup()] with
#' these Arguments and you will get the Value back.
#'
#' @usage NULL
#'
#' @param eye The eye for which to apply the settings.
#' @param fixShape Fixation target type for eye.
#' @param fixLum Fixation target luminance for eye.
#' @param fixCx x-coordinate of fixation target (degrees).
#' @param fixSx diameter along major axis of ellipse (degrees).
#' @param fixCy y-coordinate of fixation target (degrees).
#' @param fixSy diameter along minor axis of ellipse (degrees). If not received,
#'                 then sy = sx.
#' @param fixRotation Angles of rotation of fixation target (degrees). Only
#'                       useful if sx != sy specified.
#' @param fixCol Fixation target color for eye.
#' @param bgLum Background luminance for eye.
#' @param tracking Whether to correct stimulus location based on eye position.
#' @param bgCol Background color for eye.
#'
#' @return a list contianing:
#'  * res List with all of the other fields described in @ReturnMsg except
#'           'error'.
#'    - res$msg The error message or a structure with the result of QUERY OPI
#'                 command.
#'    - res$error '0' if success, something else if error.
#'
#' @examples
#' chooseOpi("ImoVifa")
#' result <- opiSetup(settings = list(eye = "list('left')", fixShape = "maltese", fixLum = 20,
#'                    fixCx = 0, fixSx = 1, fixCy = 0, fixCol = list(0, 1, 0),
#'                    bgLum = 10, bgCol = list(1, 1, 1)))
#'
#' @seealso [opiSetup()]
#'
opiSetup_for_ImoVifa <- function(settings = list(eye = NULL, fixShape = NULL, fixLum = NULL, fixCx = NULL, fixSx = NULL, fixCy = NULL, fixSy = NULL, fixRotation = NULL, fixCol = NULL, bgLum = NULL, tracking = NULL, bgCol = NULL)) {
if(!exists(".opi_env") || !exists("ImoVifa", envir = .opi_env) || !("socket" %in% names(.opi_env$ImoVifa)) || is.null(.opi_env$ImoVifa$socket))
    stop("Cannot call opiSetup without an open socket to Monitor. Did you call opiInitialise()?.")

    msg <- list(eye = settings$eye, fixShape = settings$fixShape, fixLum = settings$fixLum, fixCx = settings$fixCx, fixSx = settings$fixSx, fixCy = settings$fixCy, fixSy = settings$fixSy, fixRotation = settings$fixRotation, fixCol = settings$fixCol, bgLum = settings$bgLum, tracking = settings$tracking, bgCol = settings$bgCol)
    msg <- c(list(command = "setup"), msg)
    msg <- msg[!unlist(lapply(msg, is.null))]
    msg <- rjson::toJSON(msg)
    writeLines(msg, .opi_env$ImoVifa$socket)

    res <- readLines(.opi_env$ImoVifa$socket, n = 1)
    if (length(res) == 0)
        return(list(error = 5, msg = "Monitor server exists but a connection was not closed properly using opiClose() last time it was used. Restart Monitor."))
    res <- rjson::fromJSON(res)
    return(res)
}

#' Implementation of opiPresent for the ImoVifa machine.
#'
#' This is for internal use only. Use [opiPresent()] with
#' these Arguments and you will get the Value back.
#'
#' @usage NULL
#'
#' @param phase List of phases (in degrees) for generation of spatial patterns.
#'                 Only useful if type != FLAT
#' @param shape Stimulus shape.
#' @param sx List of diameters along major axis of ellipse (degrees).
#' @param lum List of stimuli luminances (cd/m^2).
#' @param sy List of diameters along minor axis of ellipse (degrees). If not
#'              received, then sy = sx
#' @param rotation List of angles of rotation of stimuli (degrees). Only useful
#'                    if sx != sy specified.
#' @param type Stimulus type.
#' @param defocus List of defocus values in Diopters for stimulus
#'                   post-processing.
#' @param frequency List of frequencies (in cycles per degrees) for generation
#'                     of spatial patterns. Only useful if type != FLAT
#' @param eye Eye to test.
#' @param color1 List of stimulus colors 1.
#' @param color2 List of stimulus colors 2. Only useful if stimulus type != FLAT
#' @param t List of stimuli presentation times (ms).
#' @param contrast List of stimulus contrasts (from 0 to 1). Only useful if type
#'                    != FLAT.
#' @param textRotation List of angles of rotation of stimuli (degrees). Only
#'                        useful if type != FLAT
#' @param w List of stimuli response windows (ms).
#' @param x List of x co-ordinates of stimuli (degrees).
#' @param y List of y co-ordinates of stimuli (degrees).
#'
#' @return a list contianing:
#'  * res List with all of the other fields described in @ReturnMsg except
#'           'error'.
#'    - res$msg$eyey y co-ordinates of pupil at times eyet (degrees).
#'    - res$msg$eyex x co-ordinates of pupil at times eyet (degrees).
#'    - res$msg$time Response time from stimulus onset if button pressed (ms).
#'    - res$msg$eyed Diameter of pupil at times eyet (mm).
#'    - res$msg$eyet Time of (eyex, eyey) pupil from stimulus onset (ms).
#'    - res$msg Error message or a structure with the following fields.
#'    - res$error '0' if success, something else if error.
#'    - res$msg$seen '1' if seen, '0' if not.
#'
#' @examples
#' chooseOpi("ImoVifa")
#' result <- opiPresent(stim = list(sx = list(1.72), lum = list(20), eye = list('left'),
#'                      color1 = list(list(1, 1, 1)), t = list(200), w = list(1500),
#'                      x = list(0), y = list(0)))
#'
#' @seealso [opiPresent()]
#'
opiPresent_for_ImoVifa <- function(stim = list(phase = NULL, shape = NULL, sx = NULL, lum = NULL, sy = NULL, rotation = NULL, type = NULL, defocus = NULL, frequency = NULL, eye = NULL, color1 = NULL, color2 = NULL, t = NULL, contrast = NULL, textRotation = NULL, w = NULL, x = NULL, y = NULL)) {
if(!exists(".opi_env") || !exists("ImoVifa", envir = .opi_env) || !("socket" %in% names(.opi_env$ImoVifa)) || is.null(.opi_env$ImoVifa$socket))
    stop("Cannot call opiPresent without an open socket to Monitor. Did you call opiInitialise()?.")

    msg <- list(phase = stim$phase, shape = stim$shape, sx = stim$sx, lum = stim$lum, sy = stim$sy, rotation = stim$rotation, type = stim$type, defocus = stim$defocus, frequency = stim$frequency, eye = stim$eye, color1 = stim$color1, color2 = stim$color2, t = stim$t, contrast = stim$contrast, textRotation = stim$textRotation, w = stim$w, x = stim$x, y = stim$y)
    msg <- c(list(command = "present"), msg)
    msg <- msg[!unlist(lapply(msg, is.null))]
    msg <- rjson::toJSON(msg)
    writeLines(msg, .opi_env$ImoVifa$socket)

    res <- readLines(.opi_env$ImoVifa$socket, n = 1)
    if (length(res) == 0)
        return(list(error = 5, msg = "Monitor server exists but a connection was not closed properly using opiClose() last time it was used. Restart Monitor."))
    res <- rjson::fromJSON(res)
    return(res)
}

#' Implementation of opiClose for the ImoVifa machine.
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
#'    - res$msg The error message or additional results from the CLOSE command
#'    - res$error '0' if success, something else if error.
#'
#' @examples
#' chooseOpi("ImoVifa")
#' result <- opiClose()
#'
#' @seealso [opiClose()]
#'
opiClose_for_ImoVifa <- function() {
if(!exists(".opi_env") || !exists("ImoVifa", envir = .opi_env) || !("socket" %in% names(.opi_env$ImoVifa)) || is.null(.opi_env$ImoVifa$socket))
    stop("Cannot call opiClose without an open socket to Monitor. Did you call opiInitialise()?.")

    msg <- list()
    msg <- c(list(command = "close"), msg)
    msg <- msg[!unlist(lapply(msg, is.null))]
    msg <- rjson::toJSON(msg)
    writeLines(msg, .opi_env$ImoVifa$socket)

    res <- readLines(.opi_env$ImoVifa$socket, n = 1)
    if (length(res) == 0)
        return(list(error = 5, msg = "Monitor server exists but a connection was not closed properly using opiClose() last time it was used. Restart Monitor."))
    res <- rjson::fromJSON(res)
    return(res)
}


#' Set background color and luminance in both eyes.
#' Deprecated for OPI >= v3.0.0 and replaced with [opiSetup()].
#' @usage NULL
#' @seealso [opiSetup()]
opiSetBackground_for_ImoVifa <- function(lum, color, ...) {return("Deprecated")}


