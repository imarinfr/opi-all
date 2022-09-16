#' Open Perimetry Interface implementation for %s
#'
#' Copyright [2022] [Andrew Turpin & Ivan Marin-Franch]
#'
#' Licensed under the Apache License, Version 2.0 (the "License");
#' you may not use this file except in compliance with the License.
#' You may obtain a copy of the License at
#'
#'   http://www.apache.org/licenses/LICENSE-2.0
#'
#' Unless required by applicable law or agreed to in writing, software
#' distributed under the License is distributed on an "AS IS" BASIS,
#' WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#' See the License for the specific language governing permissions and
#' limitations under the License.
#'

packageStartupMessage("OPI version", utils::packageVersion("OPI"))

#' Used for holding state of the OPI
env.OPI <- vector("list")
env.OPI$machine_list <- list(
    "Compass",
    "ImoVifa",
    "KowaAP7000",
    "O900",
    "O600",
    "SimNo",
    "SimYes",
    "SimHenson",
    "SimGaussian",
    "SimHensonRT",
    "PhoneVR",
    "Display"
)
env.OPI$machine <- NULL # Chosen machine from env.OPI$machine_list by chooseOPI()

#' chooseOPI selects an OPI machine to use.
#'
#' It should be called before any other OPI functions.
#'
#' @param machine Machine name to use. Set to NULL to get a list.
#' @returns NULL
#'
chooseOPI() <- function(machine) {
    if (is.null(machine) {
        cat(sprintf("%s is not a valid OPI machine.\nYou should choose from:\n", machine))
        print(enc.OPI$machine_list)
    } else if (! machine %in% machine_list) {
        cat(sprintf("%s is not a valid OPI machine.\nYou should choose from:\n", machine))
        print(enc.OPI$machine_list)
    } else {
        env.OPI$machine_list <<- machine
    }
    return(NULL)
}

#' opiInitialise that calls opiInitialise_for_MACHINE as appropriate.
#'
#' Establishes connection with the deivce and a Monitor (aka Server) if appropriate.
#' Sends any startup prameters that might be needed by the machine.
#' Specific paramters and return values can be seen in the machine specific versions
#' listed below in the ’See Also’.
#'
#' @seealso [opiInitialise_for_Compass()], [opiInitialise_for_ImoVifa()],
#' [opiInitialise_for_KowaAP7000()], [opiInitialise_for_O900()], [opiInitialise_for_O600()],
#' [opiInitialise_for_SimNo()], [opiInitialise_for_SimYes()], [opiInitialise_for_SimHenson()],
#' [opiInitialise_for_SimGaussian()], [opiInitialise_for_SimHensonRT()],
#' [opiInitialise_for_PhoneVR()], [opiInitialise_for_Display()],
opiInitialise <- function(...) {
    if (is.null(env.OPI$machine))
        stop("you should use chooseOPI() before calling opiInitiaise.")

    return(do.call(paste0("opiInitialise_for_", env.OPI$chosen_machine), ...))
}

#'
#' Open a socket on ip and port. Will `stop()` on error.
#'
#' @param ip IP address of socket
#' @param port TCP port of socket
#' @param machineName Machine name for error message
#'
#' @return Socket
#'
open_socket <- function(ip, port, machineName) {
    cat("Looking for a server at ", ip, port, "...\n")

    suppressWarnings(tryCatch(
        v <- socketConnection(host = ip, port,
                    blocking = TRUE, open = "w+b",
                    timeout = 10)
        , error = function(e) {
            stop(paste("Cannot find a server at", ip, "on port", port))
        }
    ))
    close(v)

    cat("Found server at", ip, port, "\n")

    socket <- tryCatch(
        socketConnection(host = ip, port, open = "w+b", blocking = TRUE, timeout = 1000), 
        error = function(e) stop(paste("Cannot connect to", machineName, "at", ip, "on port", port))
    )
    return(socket)
}
