# Open Perimetry Interface implementation for PhoneHMD
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
if (exists(".opi_env") && !exists("PhoneHMD", where = .opi_env))
    assign("PhoneHMD", new.env(), envir = .opi_env)

#' Implementation of opiInitialise for the PhoneHMD machine.
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
#'  * error Empty string for all good, else error messages from PhoneHMD.
#'  * msg JSON Object with all of the other fields described in @ReturnMsg
#'           except 'error'.
#'    - msg$jovp Any messages that the JOVP sent back.
#'
#' @examples
#' chooseOpi("PhoneHMD")
#' result <- opiInitialise(ip = "192.126.0.1", port = 50000,
#'                         ip_Monitor = "localhost", port_Monitor = 50001)
#'
#' @seealso [opiInitialise()]
#'
opiInitialise_for_PhoneHMD <- function(ip = NULL, port = NULL, ip_Monitor = NULL, port_Monitor = NULL) {
    .opi_env$PhoneHMD$socket <<- open_socket(ip_Monitor, port_Monitor)
    msg <- list(ip = ip, port = port, ip_Monitor = ip_Monitor, port_Monitor = port_Monitor);
    msg <- rjson::toJSON(msg)
    writeLines(msg, .opi_env$PhoneHMD$socket)

    res <- rjson::fromJSON(readLines(.opi_env$PhoneHMD$socket, n=1))
    return(res)
}

#' Implementation of opiPresent for the PhoneHMD machine.
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
#' @param sy List of diameters along minor axis of ellipse (degrees).
#' @param lum List of stimuli luminances (cd/m^2).
#' @param color List of stimuli colors.
#' @param rotation List of angles of rotation of stimuli (degrees). Only useful
#'                    if sx != sy specified.
#' @param contrast List of stimulus contrasts (from 0 to 1).
#' @param defocus List of defocus values in Diopters for stimulus
#'                   post-processing.
#' @param phase List of phases (in degrees) for generation of spatial patterns.
#'                 Only useful if type != FLAT
#' @param frequency List of frequencies (in cycles per degrees) for generation
#'                     of spatial patterns. Only useful if type != FLAT
#' @param textRotation List of angles of rotation of stimuli (degrees). Only
#'                        useful if type != FLAT
#' @param t List of stimuli presentation times (ms).
#' @param w List of stimuli response windows (ms).
#'
#' @return a list contianing:
#'  * error Empty string for all good, else error messages from PhoneHMD.
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
#' chooseOpi("PhoneHMD")
#' result <- opiPresent(stim = list(eye = list('left'), x = list(0), y = list(0),
#'                      sx = list(1.72), sy = list(1.72), lum = list(20),
#'                      color = list(list(1, 1, 1)), t = list(200), w = list(1500)))
#'
#' @seealso [opiPresent()]
#'
opiPresent_for_PhoneHMD <- function(stim = list(eye = NULL, shape = NULL, type = NULL, x = NULL, y = NULL, sx = NULL, sy = NULL, lum = NULL, color = NULL, rotation = NULL, contrast = NULL, defocus = NULL, phase = NULL, frequency = NULL, textRotation = NULL, t = NULL, w = NULL)) {
if(!exists(".opi_env$PhoneHMD$socket") || is.null(.opi_env$PhoneHMD$socket))
    stop("Cannot call opiPresent without an open socket to Monitor. Did you call opiInitialise()?.")

    msg <- list(eye = stim$eye, shape = stim$shape, type = stim$type, x = stim$x, y = stim$y, sx = stim$sx, sy = stim$sy, lum = stim$lum, color = stim$color, rotation = stim$rotation, contrast = stim$contrast, defocus = stim$defocus, phase = stim$phase, frequency = stim$frequency, textRotation = stim$textRotation, t = stim$t, w = stim$w);
    msg <- rjson::toJSON(msg)
    writeLines(msg, .opi_env$PhoneHMD$socket)

    res <- rjson::fromJSON(readLines(.opi_env$PhoneHMD$socket, n=1))
    return(res)
}

#' Implementation of opiSetup for the PhoneHMD machine.
#'
#' This is for internal use only. Use [opiSetup()] with
#' these Arguments and you will get the Value back.
#'
#' @usage NULL
#'
#' @param eye Eye to set.
#' @param bgLum Background color for eye.
#' @param bgCol Background color for eye.
#' @param fixType Fixation target type for eye.
#' @param fixLum Fixation target luminance for eye.
#' @param fixCol Fixation target color for eye.
#' @param fixCx x-coordinate of fixation target (degrees).
#' @param fixCy y-coordinate of fixation target (degrees).
#' @param fixSx diameter along major axis of ellipse (degrees).
#' @param fixSy diameter along minor axis of ellipse (degrees).
#' @param fixRotation Angles of rotation of fixation target (degrees). Only
#'                       useful if sx != sy specified.
#'
#' @return a list contianing:
#'  * error Empty string for all good, else error messages from PhoneHMD.
#'  * msg JSON Object with all of the other fields described in @ReturnMsg
#'           except 'error'.
#'    - msg$jovp Any messages that the JOVP sent back.
#'
#' @examples
#' chooseOpi("PhoneHMD")
#' result <- opiSetup(settings = list(eye = "both", bgLum = 10, bgCol = list(1, 1, 1),
#'                    fixType = "maltese", fixLum = 20, fixCol = list(0, 1, 0),
#'                    fixCx = 0, fixCy = 0, fixSx = 1, fixSy = 1, fixRotation = 0))
#'
#' @seealso [opiSetup()]
#'
opiSetup_for_PhoneHMD <- function(settings = list(eye = NULL, bgLum = NULL, bgCol = NULL, fixType = NULL, fixLum = NULL, fixCol = NULL, fixCx = NULL, fixCy = NULL, fixSx = NULL, fixSy = NULL, fixRotation = NULL)) {
if(!exists(".opi_env$PhoneHMD$socket") || is.null(.opi_env$PhoneHMD$socket))
    stop("Cannot call opiSetup without an open socket to Monitor. Did you call opiInitialise()?.")

    msg <- list(eye = settings$eye, bgLum = settings$bgLum, bgCol = settings$bgCol, fixType = settings$fixType, fixLum = settings$fixLum, fixCol = settings$fixCol, fixCx = settings$fixCx, fixCy = settings$fixCy, fixSx = settings$fixSx, fixSy = settings$fixSy, fixRotation = settings$fixRotation);
    msg <- rjson::toJSON(msg)
    writeLines(msg, .opi_env$PhoneHMD$socket)

    res <- rjson::fromJSON(readLines(.opi_env$PhoneHMD$socket, n=1))
    return(res)
}

#' Implementation of opiClose for the PhoneHMD machine.
#'
#' This is for internal use only. Use [opiClose()] with
#' these Arguments and you will get the Value back.
#'
#' @usage NULL
#'
#'
#'
#' @return a list contianing:
#'  * error Empty string for all good, else error messages from PhoneHMD.
#'  * msg JSON Object with all of the other fields described in @ReturnMsg
#'           except 'error'.
#'    - msg$jovp Any messages that the JOVP sent back.
#'
#' @examples
#' chooseOpi("PhoneHMD")
#' result <- opiClose()
#'
#' @seealso [opiClose()]
#'
opiClose_for_PhoneHMD <- function() {
if(!exists(".opi_env$PhoneHMD$socket") || is.null(.opi_env$PhoneHMD$socket))
    stop("Cannot call opiClose without an open socket to Monitor. Did you call opiInitialise()?.")

    msg <- list();
    msg <- rjson::toJSON(msg)
    writeLines(msg, .opi_env$PhoneHMD$socket)

    res <- rjson::fromJSON(readLines(.opi_env$PhoneHMD$socket, n=1))
    return(res)
}

#' Implementation of opiQueryDevice for the PhoneHMD machine.
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
#'    - msg$isTracking 0 eye tracking is off, any other value it is on.
#'    - msg$isCalibrated 0 eye tracking has not been calibrated, any other value
#'                          it has.
#'
#' @examples
#' chooseOpi("PhoneHMD")
#' result <- opiQueryDevice()
#'
#' @seealso [opiQueryDevice()]
#'
opiQueryDevice_for_PhoneHMD <- function() {
if(!exists(".opi_env$PhoneHMD$socket") || is.null(.opi_env$PhoneHMD$socket))
    stop("Cannot call opiQueryDevice without an open socket to Monitor. Did you call opiInitialise()?.")

    msg <- list();
    msg <- rjson::toJSON(msg)
    writeLines(msg, .opi_env$PhoneHMD$socket)

    res <- rjson::fromJSON(readLines(.opi_env$PhoneHMD$socket, n=1))
    return(res)
}


#' Set background color and luminance in both eyes.
#' Deprecated for ImoVifa and replaced with [opiSetup()].
#' @usage NULL
#' @seealso [opiSetup()]
opiSetBackground_for_ImoVifa <- function(lum, color, ...) {return("Deprecated")}


