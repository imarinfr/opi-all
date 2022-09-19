# Open Perimetry Interface implementation for PicoVR
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

env.PicoVR <- vector("list")    # environment for this machine in R

#' Implementation of opiInitialise for the PicoVR machine.
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
#' chooseOpi("PicoVR")
#' result <- opiInitialise(ip = "192.126.0.1", port = 50000,
#'                         ip_Monitor = "localhost", port_Monitor = 50001)
#'
#' @seealso [opiInitialise()]
#'
opiInitialise_for_PicoVR <- function(ip = NULL, port = NULL, ip_Monitor = NULL, port_Monitor = NULL) {
    env.PicoVR$socket <<- open_socket(ip_Monitor, port_Monitor)
    msg <- list(ip = ip,port = port,ip_Monitor = ip_Monitor,port_Monitor = port_Monitor);
    msg <- rjson::toJSON(msg)
    writeLines(msg, env.PicoVR$socket)

    res <- rjson::fromJSON(readLines(env.PicoVR$socket, n=1))
    return(res)
}

#' Implementation of opiPresent for the PicoVR machine.
#'
#' This is for internal use only. Use [opiPresent()] with
#' these Arguments and you will get the Value back.
#'
#' @usage NULL
#'
#' @param eye Eye to test.
#' @param type Stimulus type.
#' @param x List of x co-ordinates of stimuli (degrees).
#' @param y List of y co-ordinates of stimuli (degrees).
#' @param t List of stimuli presentation times (ms).
#' @param w List of stimuli response windows (ms).
#' @param lum List of stimuli luminances (cd/m^2).
#' @param color List of stimuli colors.
#' @param sx List of diameters along major axis of ellipse (degrees).
#' @param sy List of diameters along minor axis of ellipse (degrees).
#' @param rotation List of angles of rotaion of stimuli (degrees). Only useful
#'                    if sx != sy specified.
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
#' chooseOpi("PicoVR")
#' result <- opiPresent(stim = list(eye = list('left'), type = list('circle'), x = list(0),
#'                      y = list(0), t = list(200), w = list(1500), lum = list(20),
#'                      color = list('white'), sx = list(1.72), sy = list(1.72)))
#'
#' @seealso [opiPresent()]
#'
opiPresent_for_PicoVR <- function(stim = list(eye = NULL, type = NULL, x = NULL, y = NULL, t = NULL, w = NULL, lum = NULL, color = NULL, sx = NULL, sy = NULL, rotation = NULL)) {
if(!exists(env.PicoVR$socket) || is.null(env.PicoVR$socket))
    stop("Cannot call opiPresent without an open socket to Monitor. Did you call opiInitialise()?.")

    msg <- list(eye = stim$eye,type = stim$type,x = stim$x,y = stim$y,t = stim$t,w = stim$w,lum = stim$lum,color = stim$color,sx = stim$sx,sy = stim$sy,rotation = stim$rotation);
    msg <- rjson::toJSON(msg)
    writeLines(msg, env.PicoVR$socket)

    res <- rjson::fromJSON(readLines(env.PicoVR$socket, n=1))
    return(res)
}

#' Implementation of opiSetup for the PicoVR machine.
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
#' @param fixCx x-coordinate of fixation target (degrees).
#' @param fixCy y-coordinate of fixation target (degrees).
#' @param fixSx diameter along major axis of ellipse (degrees).
#' @param fixSy diameter along minor axis of ellipse (degrees).
#' @param fixRotation Angles of rotation of fixation target (degrees). Only
#'                       useful if sx != sy specified.
#' @param tracking Whether to correct stimulus location based on eye position.
#'
#' @return a list contianing:
#'  * error Empty string for all good, else error messages from ImoVifa.
#'  * msg JSON Object with all of the other fields described in @ReturnMsg
#'           except 'error'.
#'    - msg$jovp Any messages that the JOVP sent back.
#'
#' @examples
#' chooseOpi("PicoVR")
#' result <- opiSetup(settings = list(eye = "both", bgLum = 10, bgCol = "white", fixType = "maltese",
#'                    fixCx = 0, fixCy = 0, fixSx = 1, fixSy = 1, fixRotation = 0,
#'                    tracking = 0))
#'
#' @seealso [opiSetup()]
#'
opiSetup_for_PicoVR <- function(settings = list(eye = NULL, bgLum = NULL, bgCol = NULL, fixType = NULL, fixCx = NULL, fixCy = NULL, fixSx = NULL, fixSy = NULL, fixRotation = NULL, tracking = NULL)) {
if(!exists(env.PicoVR$socket) || is.null(env.PicoVR$socket))
    stop("Cannot call opiSetup without an open socket to Monitor. Did you call opiInitialise()?.")

    msg <- list(eye = settings$eye,bgLum = settings$bgLum,bgCol = settings$bgCol,fixType = settings$fixType,fixCx = settings$fixCx,fixCy = settings$fixCy,fixSx = settings$fixSx,fixSy = settings$fixSy,fixRotation = settings$fixRotation,tracking = settings$tracking);
    msg <- rjson::toJSON(msg)
    writeLines(msg, env.PicoVR$socket)

    res <- rjson::fromJSON(readLines(env.PicoVR$socket, n=1))
    return(res)
}

#' Implementation of opiClose for the PicoVR machine.
#'
#' This is for internal use only. Use [opiClose()] with
#' these Arguments and you will get the Value back.
#'
#' @usage NULL
#'

#'
#' @return a list contianing:
#'  * error Empty string for all good, else error messages from Imo.
#'  * msg JSON Object with all of the other fields described in @ReturnMsg
#'           except 'error'.
#'    - msg$jovp Any messages that the JOVP sent back.
#'
#' @examples
#' chooseOpi("PicoVR")
#' result <- opiClose()
#'
#' @seealso [opiClose()]
#'
opiClose_for_PicoVR <- function() {
if(!exists(env.PicoVR$socket) || is.null(env.PicoVR$socket))
    stop("Cannot call opiClose without an open socket to Monitor. Did you call opiInitialise()?.")

    msg <- list();
    msg <- rjson::toJSON(msg)
    writeLines(msg, env.PicoVR$socket)

    res <- rjson::fromJSON(readLines(env.PicoVR$socket, n=1))
    return(res)
}

#' Implementation of opiQueryDevice for the PicoVR machine.
#'
#' This is for internal use only. Use [opiQueryDevice()] with
#' these Arguments and you will get the Value back.
#'
#' @usage NULL
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
#' chooseOpi("PicoVR")
#' result <- opiQueryDevice()
#'
#' @seealso [opiQueryDevice()]
#'
opiQueryDevice_for_PicoVR <- function() {
if(!exists(env.PicoVR$socket) || is.null(env.PicoVR$socket))
    stop("Cannot call opiQueryDevice without an open socket to Monitor. Did you call opiInitialise()?.")

    msg <- list();
    msg <- rjson::toJSON(msg)
    writeLines(msg, env.PicoVR$socket)

    res <- rjson::fromJSON(readLines(env.PicoVR$socket, n=1))
    return(res)
}


#' Set background color and luminance in both eyes.
#' Deprecated for ImoVifa and replaced with [opiSetup()].
#' @usage NULL
#' @seealso [opiSetup()]
opiSetBackground_for_ImoVifa <- function(lum, color, ...) {return("Deprecated")}


