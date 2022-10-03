# Open Perimetry Interface implementation for Jovp
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
if (exists(".opi_env") && !exists("Jovp", where = .opi_env))
    assign("Jovp", new.env(), envir = .opi_env)

#' Implementation of opiInitialise for the Jovp machine.
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
#' chooseOpi("Jovp")
#' result <- opiInitialise(null)
#'
#' @seealso [opiInitialise()]
#'
opiInitialise_for_Jovp <- function(ipMonitor = NULL, portMonitor = NULL, ip = NULL, port = NULL) {
    assign("socket", open_socket(ipMonitor, portMonitor), .opi_env$Jovp)

    msg <- list(command = "choose", machine = "Jovp")
    msg <- rjson::toJSON(msg)
    writeLines(msg, .opi_env$Jovp$socket)
    res <- readLines(.opi_env$Jovp$socket, n = 1)
    print(res)
    res <- rjson::fromJSON(res)

    msg <- list(ipMonitor = ipMonitor, portMonitor = portMonitor, ip = ip, port = port, command = "initialize")
    msg <- rjson::toJSON(msg)
    writeLines(msg, .opi_env$Jovp$socket)

    res <- rjson::fromJSON(readLines(.opi_env$Jovp$socket, n = 1))
    return(res)
}

#' Implementation of opiQueryDevice for the Jovp machine.
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
#' chooseOpi("Jovp")
#' result <- opiQueryDevice(null)
#'
#' @seealso [opiQueryDevice()]
#'
opiQueryDevice_for_Jovp <- function() {
if(!exists(".opi_env$Jovp") || !exists(".opi_env$Jovp$socket") || is.null(.opi_env$Jovp$socket))
    stop("Cannot call opiQueryDevice without an open socket to Monitor. Did you call opiInitialise()?.")

    msg <- list()
    msg <- rjson::toJSON(msg)
    writeLines(msg, .opi_env$Jovp$socket)

    res <- rjson::fromJSON(readLines(.opi_env$Jovp$socket, n = 1))
    return(res)
}

#' Implementation of opiSetup for the Jovp machine.
#'
#' This is for internal use only. Use [opiSetup()] with
#' these Arguments and you will get the Value back.
#'
#' @usage NULL
#'
#' @param eye The eye for which to apply the settings.
#' @param bgLum Background luminance for eye.
#' @param bgCol Background color for eye.
#' @param fixShape Fixation target type for eye.
#' @param fixLum Fixation target luminance for eye.
#' @param fixCol Fixation target color for eye.
#' @param fixCx x-coordinate of fixation target (degrees).
#' @param fixCy y-coordinate of fixation target (degrees).
#' @param fixSx diameter along major axis of ellipse (degrees).
#' @param fixSy diameter along minor axis of ellipse (degrees). If not received,
#'                 then sy = sx.
#' @param fixRotation Angles of rotation of fixation target (degrees). Only
#'                       useful if sx != sy specified.
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
#' chooseOpi("Jovp")
#' result <- opiSetup(settings = list(null))
#'
#' @seealso [opiSetup()]
#'
opiSetup_for_Jovp <- function(settings = list(eye = NULL, bgLum = NULL, bgCol = NULL, fixShape = NULL, fixLum = NULL, fixCol = NULL, fixCx = NULL, fixCy = NULL, fixSx = NULL, fixSy = NULL, fixRotation = NULL, tracking = NULL)) {
if(!exists(".opi_env$Jovp") || !exists(".opi_env$Jovp$socket") || is.null(.opi_env$Jovp$socket))
    stop("Cannot call opiSetup without an open socket to Monitor. Did you call opiInitialise()?.")

    msg <- list(eye = settings$eye, bgLum = settings$bgLum, bgCol = settings$bgCol, fixShape = settings$fixShape, fixLum = settings$fixLum, fixCol = settings$fixCol, fixCx = settings$fixCx, fixCy = settings$fixCy, fixSx = settings$fixSx, fixSy = settings$fixSy, fixRotation = settings$fixRotation, tracking = settings$tracking)
    msg <- rjson::toJSON(msg)
    writeLines(msg, .opi_env$Jovp$socket)

    res <- rjson::fromJSON(readLines(.opi_env$Jovp$socket, n = 1))
    return(res)
}

#' Implementation of opiPresent for the Jovp machine.
#'
#' This is for internal use only. Use [opiPresent()] with
#' these Arguments and you will get the Value back.
#'
#' @usage NULL
#'
#' @param eye Eye to test.
#' @param shape Stimulus shape.
#' @param type Stimulus type.
#' @param x List of x co-ordinates of stimuli (degrees).
#' @param y List of y co-ordinates of stimuli (degrees).
#' @param sx List of diameters along major axis of ellipse (degrees).
#' @param sy List of diameters along minor axis of ellipse (degrees). If not
#'              received, then sy = sx
#' @param lum List of stimuli luminances (cd/m^2).
#' @param color1 List of stimulus colors 1.
#' @param color2 List of stimulus colors 2. Only useful if stimulus type != FLAT
#' @param rotation List of angles of rotation of stimuli (degrees). Only useful
#'                    if sx != sy specified.
#' @param contrast List of stimulus contrasts (from 0 to 1). Only useful if type
#'                    != FLAT.
#' @param phase List of phases (in degrees) for generation of spatial patterns.
#'                 Only useful if type != FLAT
#' @param frequency List of frequencies (in cycles per degrees) for generation
#'                     of spatial patterns. Only useful if type != FLAT
#' @param defocus List of defocus values in Diopters for stimulus
#'                   post-processing.
#' @param textRotation List of angles of rotation of stimuli (degrees). Only
#'                        useful if type != FLAT
#' @param t List of stimuli presentation times (ms).
#' @param w List of stimuli response windows (ms).
#'
#' @return a list contianing:
#'    - res$error '0' if success, '1' if error.
#'    - res$msg Error message or a structure with the following fields.
#'    - res$msg$seen '1' if seen, '0' if not.
#'    - res$msg$time Response time from stimulus onset if button pressed (ms).
#'    - res$msg$eyex x co-ordinates of pupil at times eyet (degrees).
#'    - res$msg$eyey y co-ordinates of pupil at times eyet (degrees).
#'    - res$msg$eyed Diameter of pupil at times eyet (mm).
#'    - res$msg$eyet Time of (eyex, eyey) pupil from stimulus onset (ms).
#'
#' @examples
#' chooseOpi("Jovp")
#' result <- opiPresent(stim = list(null))
#'
#' @seealso [opiPresent()]
#'
opiPresent_for_Jovp <- function(stim = list(eye = NULL, shape = NULL, type = NULL, x = NULL, y = NULL, sx = NULL, sy = NULL, lum = NULL, color1 = NULL, color2 = NULL, rotation = NULL, contrast = NULL, phase = NULL, frequency = NULL, defocus = NULL, textRotation = NULL, t = NULL, w = NULL)) {
if (!exists(".opi_env") || !exists("Jovp", envir = .opi_env))
    stop("Cannot call opiPresent without an open socket to Monitor. Did you call opiInitialise()?.")
if (!("socket" %in% names(.opi_env$Jovp)) || is.null(.opi_env$Jovp$socket))
    stop("Cannot call opiPresent without an open socket to Monitor. Did you call opiInitialise()?.")

    msg <- c(list(command = "present"), lapply(stim, function(p) ifelse(is.null(p), NULL, p)))
    print(msg)
    #msg <- list(command = "present", eye = stim$eye, shape = stim$shape, type = stim$type, x = stim$x, y = stim$y, sx = stim$sx, sy = stim$sy, lum = stim$lum, color1 = stim$color1, color2 = stim$color2, rotation = stim$rotation, contrast = stim$contrast, phase = stim$phase, frequency = stim$frequency, defocus = stim$defocus, textRotation = stim$textRotation, t = stim$t, w = stim$w)
    msg <- rjson::toJSON(msg)
    writeLines(msg, .opi_env$Jovp$socket)

    res <- readLines(.opi_env$Jovp$socket, n = 1)
    print(res)
    res <- rjson::fromJSON(res)
    return(res)
}

#' Implementation of opiClose for the Jovp machine.
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
#'
#' @examples
#' chooseOpi("Jovp")
#' result <- opiClose(null)
#'
#' @seealso [opiClose()]
#'
opiClose_for_Jovp <- function() {
if(!exists(".opi_env$Jovp") || !exists(".opi_env$Jovp$socket") || is.null(.opi_env$Jovp$socket))
    stop("Cannot call opiClose without an open socket to Monitor. Did you call opiInitialise()?.")

    msg <- list()
    msg <- rjson::toJSON(msg)
    writeLines(msg, .opi_env$Jovp$socket)

    res <- rjson::fromJSON(readLines(.opi_env$Jovp$socket, n = 1))
    return(res)
}


#' Set background color and luminance in both eyes.
#' Deprecated for OPI >= v3.0.0 and replaced with [opiSetup()].
#' @usage NULL
#' @seealso [opiSetup()]
opiSetBackground_for_Jovp <- function(lum, color, ...) {return("Deprecated")}


