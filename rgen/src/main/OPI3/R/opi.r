# Open Perimetry Interface controlling class.
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
#

packageStartupMessage("OPI3 version", utils::packageVersion("OPI3"))

# Used for holding state of the OPI

#' @export
.opi_env <- new.env(size = 20)

assign("machine_list", list(
    # "SimHensonRT",  No longer supported. Do your own outside opiPresent. data still in package.
    # "Jovp",  Not needed as a standalone, use subclasses: Display, ...
    #"Icare",  Not needed as a standalone, use subclasses: O900, O600
    # "O600",  Never really supported?
    "Compass",
    "ImoVifa",
    # "Kowa",   TODO
    "Maia",
    "O900",
    "SimNo",
    "SimYes",
    "SimHenson",
    "SimGaussian",
    "PhoneHMD",
    "Display",
    "PicoVR"
), envir = .opi_env)

assign("chosen_machine", NULL, .opi_env) # Chosen machine from machine_list by chooseOPI()

#' chooseOPI selects an OPI machine to use.
#'
#' It should be called before any other OPI functions.
#'
#' @param machine Machine name to use. Set to NULL to get a list.
#' @returns NULL on success or list of machines otherwise.
#' @export
chooseOPI <- function(machine = NULL) {
    if (is.null(machine)) {
        cat(sprintf("%s is not a valid OPI machine.\nYou should choose from:\n", machine))
        print(unlist(.opi_env$machine_list))
        return(unlist(.opi_env$machine_list))
    }

    if (! machine %in% .opi_env$machine_list) {
        cat(sprintf("%s is not a valid OPI machine.\nYou should choose from:\n", machine))
        print(unlist(.opi_env$machine_list))
        return(unlist(.opi_env$machine_list))
    }

    #if (machine == "PicoVR") machine <- "Jovp"
    #if (machine == "Display") machine <- "Jovp"
    #if (machine == "PhoneHMD") machine <- "Jovp"
    #if (machine == "ImoVifa") machine <- "Jovp"
    assign("chosen_machine", machine, .opi_env)
    return(NULL)
}

#' @rdname chooseOPI
#' @export
chooseOpi <- chooseOPI

#' opiInitialise that calls opiInitialise_for_MACHINE as appropriate.
#'
#' Establishes connection with the deivce and a Monitor (aka Server) if appropriate.
#' Sends any startup prameters that might be needed by the machine.
#' Specific paramters and return values can be seen in the machine specific versions
#' listed below in the ’See Also’.
#'
#' @param ... Parameters specific to each machine as described in the 'See Also' functions.
#'
#' @seealso [opiInitialise_for_Compass()], [opiInitialise_for_ImoVifa()],
#' [opiInitialise_for_PhoneHMD()], [opiInitialise_for_Display()], [opiInitialise_for_PicoVR()],
#' [opiInitialise_for_O900()], 
# [opiInitialise_for_Kowa()], [opiInitialise_for_O900()],
#' [opiInitialise_for_SimNo()], [opiInitialise_for_SimYes()], [opiInitialise_for_SimHenson()],
#' [opiInitialise_for_SimGaussian()]
#' @export
opiInitialise <- function(...) {
    if (is.null(.opi_env$chosen_machine))
        stop("you should use chooseOPI() before calling opiInitialise.")

    return(do.call(paste0("opiInitialise_for_", .opi_env$chosen_machine), args = list(...)))
}

#' @rdname opiInitialise
#' @export
opiInitialize <- opiInitialise

#' opiQueryDevice that calls opiQueryDevice_for_MACHINE as appropriate.
#'
#' Returns a list that describes the current state of the machine.
#' Specific paramters and return values can be seen in the machine specific versions
#' listed below in the ’See Also’.
#'
#' @seealso [opiQueryDevice_for_Compass()], [opiQueryDevice_for_ImoVifa()],
#' [opiQueryDevice_for_O900()], 
# [opiQueryDevice_for_Kowa()], [opiQueryDevice_for_O600()],
#' [opiQueryDevice_for_PhoneHMD()], [opiQueryDevice_for_Display()], [opiQueryDevice_for_PicoVR()],
#' [opiQueryDevice_for_SimNo()], [opiQueryDevice_for_SimYes()], [opiQueryDevice_for_SimHenson()],
#' [opiQueryDevice_for_SimGaussian()]
#' @export
opiQueryDevice <- function() {
    if (is.null(.opi_env$chosen_machine))
        stop("you should use chooseOPI() before calling opiQueryDevice.")

    return(do.call(paste0("opiQueryDevice_for_", .opi_env$chosen_machine), args = list()))
}

#' opiSetup that calls opiSetup_for_MACHINE as appropriate.
#'
#' Returns a JSON object that describes the current state of the machine.
#' Specific paramters and return values can be seen in the machine specific versions
#' listed below in the ’See Also’.
#'
#' @param settings A list containing the same names as that returned by {@link opi_queryDevice}.
#'
#' @seealso [opiSetup_for_Compass()], [opiSetup_for_ImoVifa()],
#' [opiSetup_for_O900()], 
# [opiSetup_for_Kowa()], [opiSetup_for_O600()],
#' [opiSetup_for_PhoneHMD()], [opiSetup_for_Display()], [opiSetup_for_PicoVR()],
#' [opiSetup_for_SimNo()], [opiSetup_for_SimYes()], [opiSetup_for_SimHenson()],
#' [opiSetup_for_SimGaussian()]
#' @export
opiSetup <- function(settings) {
    if (is.null(.opi_env$chosen_machine))
        stop("you should use chooseOPI() before calling opiSetup.")

    return(do.call(paste0("opiSetup_for_", .opi_env$chosen_machine), list(settings)))
}

#' opiClose that calls opiSetup_for_MACHINE as appropriate.
#'
#' Returns a JSON object that describes the current state of the machine.
#' Specific paramters and return values can be seen in the machine specific versions
#' listed below in the ’See Also’.
#'
#' @seealso [opiClose_for_Compass()], [opiClose_for_ImoVifa()],
#' [opiClose_for_O900()],
# [opiClose_for_Kowa()], [opiClose_for_O600()],
#' [opiClose_for_PhoneHMD()], [opiClose_for_Display()], [opiClose_for_PicoVR()],
#' [opiClose_for_SimNo()], [opiClose_for_SimYes()], [opiClose_for_SimHenson()],
#' [opiClose_for_SimGaussian()]
#' @export
opiClose <- function() {
    if (is.null(.opi_env$chosen_machine))
        stop("you should use chooseOPI() before calling opiClose.")

    return(do.call(paste0("opiClose_for_", .opi_env$chosen_machine), args = list()))
}

#' opiPresent that calls opiPresent_for_MACHINE as appropriate.
#'
#' Returns a JSON object that describes the current state of the machine.
#' Specific paramters and return values can be seen in the machine specific versions
#' listed below in the ’See Also’.
#'
#' @param stim A stimulus object or list as described for each machine in the 'See Also' methods.
#' @param ...  Other arguments that might be needed by each machine in the 'See Also' methods.
#'
#' @seealso [opiPresent_for_Compass()], [opiPresent_for_ImoVifa()],
#' [opiPresent_for_O900()], 
# [opiPresent_for_Kowa()], [opiPresent_for_O600()],
#' [opiPresent_for_PhoneHMD()], [opiPresent_for_Display()], [opiPresent_for_PicoVR()],
#' [opiPresent_for_SimNo()], [opiPresent_for_SimYes()], [opiPresent_for_SimHenson()],
#' [opiPresent_for_SimGaussian()]
#' @export
opiPresent <- function(stim, ...) {
    if (is.null(.opi_env$chosen_machine))
        stop("you should use chooseOPI() before calling opiPresent.")

        # for backwards compatability for version < OPI 3.0
    if ("level" %in% names(stim) && !("lum" %in% names(stim)))
        stim <- c(stim, lum = stim$level)

    return(do.call(paste0("opiPresent_for_", .opi_env$chosen_machine), list(stim = stim, ...)))
}

#' Set background color and luminance in both eyes. 
#' Deprecated for OPI >= v3.0.0 and replaced with [opiSetup()].
#' @usage NULL
#' @seealso [opiSetup()]
#' @export
opiSetBackground <- function(lum, color, ...) {return("Deprecated. Use opiSetup()")}

#'
#' Open a socket on ip and port. 
#'
#' @param ip IP address of socket
#' @param port TCP port of socket
#' @param machineName Machine name for error message
#'
#' @return Socket or NULL on error
#'
open_socket <- function(ip, port, machineName) {
    cat("Looking for a server at ", ip, port, "...\n")

    suppressWarnings(socket <- tryCatch(
        socketConnection(host = ip, port,
                    blocking = TRUE, open = "w+b",
                    timeout = 10)
        , error = function(e) {
            warning(paste("Cannot find a server at", ip, "on port", port))
            return(NULL)
        }
    ))

    cat("Found server at", ip, port, "\n")

    return(socket)
}
