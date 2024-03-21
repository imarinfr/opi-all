#
# An implementation of the OPI that simulates a patient that always responds.
#
# Author: Andrew Turpin
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

#' Does nothing.
#'
#' @return A list with elements:
#'   * \code{error} Always \code{FALSE}.
#'   * \code{msg} A string "Close OK".
#'
opiClose_for_SimYes <- function() list(error = FALSE, msg = "Close OK")

#' Returns name of the machine.
#'
#' @return A list with elements:
#'   * \code{error} Always \code{FALSE}.
#'   * \code{msg} A list containing \code{machine} that is set to `"SimYes"`.
#'
opiQueryDevice_for_SimYes <- function() list(error = FALSE, msg = list(machine = "SimYes"))

#' Does nothing.
#'
#' @param ... Any object you like, it is ignored.
#'
#' @return A list with elements:
#'   * \code{error} Always \code{FALSE}.
#'   * \code{msg} A string "Initialise OK"
#'
opiInitialise_for_SimYes <- function(...) list(error = FALSE, msg = "Initialise OK")

#' Does nothing.
#'
#' @param state Any object you like, it is ignored.
#'
#' @return A list with elements:
#'   * \code{error} Always \code{FALSE}.
#'   * \code{msg} A string "All setup!"
#'
opiSetup_for_SimYes <- function(...) list(error = FALSE, msg = "All setup!")

#' Always respond 'yes' immediately to any parameter.
#' No checking is done on the validity of `stim`.
#'
#' @param ... Any parameters you like, they are ignored.
#'
#' @return A list with elements:
#'   * \code{error} Always \code{FALSE}.
#'   * \code{msg} A list containing
#'     * \code{seen} Always \code{TRUE}.
#'     * \code{time} Always \code{NA}.
#'
opiPresent_for_SimYes <- function(stim, ...) list(error = FALSE, msg = list(seen = TRUE, time = NA))