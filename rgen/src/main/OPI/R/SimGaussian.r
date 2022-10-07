#
# An implementation of the OPI that simulates responses using
# a Cummulative Gaussian distribution as the Frequency of Seeing curve.
#
# Author: Andrew Turpin    (aturpin@unimelb.edu.au)
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

require(stats)

#'
#' Simulates reponses using a Frequency of Seeing (FoS) curve.
#'
#' The FoS is modelled as a cummulative Gaussian function with standard deviation
#' equal to `sd` as provided and the mean as the true threshold given as `tt` 
#' [opiPresent].
#' All values are in dB relative to `maxStim`.
#'
#' @param type A single character that is:
#'   *  `sd` standard deviation of Cummulative Gaussian
#' @param maxStim The maximum stimuls value (0 dB) in cd/\eqn{\mbox{m}^2}{m^2}.
#' @param ... Any other parameters you like, they are ignored.
#'
#' @return NULL on success or a string message otherwise
#' @examples
#'     # Set up a simple simulation for white-on-white perimetry
#' chooseOpi("SimGaussian")
#' if (!is.null(opiInitialize(sd = 2.5)))
#'   stop("opiInitialize failed")
#'
opiInitialise_for_SimGaussian <- function(sd = 1.0, maxStim = 10000 / pi, ...) {
    if (!is.numeric(sd) || (sd < 0)) {
        msg <- paste("Invalid standard deviation in opiInitialize for SimGaussian:", sd)
        warning(msg)
        return(msg)
    }

    if (exists(".opi_env") && !exists("sim_gaussian", where = .opi_env))
        assign("sim_gaussian", new.env(), envir = .opi_env)

    assign("sd",      sd,      envir = .opi_env$sim_gaussian)
    assign("maxStim", maxStim, envir = .opi_env$sim_gaussian)

    return(NULL)
}

#' Determine the response to a stimuli by sampling from a cummulative Gaussian
#' Frequency-of-Seeing (FoS) curve (also known as the psychometric function).
#'
#' The FoS has formula
#' \deqn{\mbox{fpr}+(1-\mbox{fpr}-\mbox{fnr})(1-\mbox{pnorm}(x, \mbox{tt}, pxVar)}
#' where \eqn{x}{\code{x}} is the stimulus value in dB, and `pxVar` is
#' \deqn{\min\left(\mbox{cap}, e^{A\times\mbox{tt}+B}\right).}
#' The ceiling \code{cap} is set with the call to
#' \code{opiInitialize}, and \code{A} and \code{B} are from Table 1 in Henson
#' et al (2000), also set in the call to `opiInitiaise` using the \code{type} parameter.
#'
#' @param stim A list that contains at least:
#'   * `lum` which is the stim value in cd/\eqn{\mbox{m}^2}{m^2}.
#' @param fpr false positive rate for the FoS curve (range 0..1).
#' @param fnr false negative rate for the FoS curve (range 0..1).
#' @param tt  mean of the assumed FoS curve (cd/\eqn{\mbox{m}^2}{m^2}).
#' @param ...  Any other parameters you like, they are ignored.
#'
#' @return A list contianing:
#'   * err, an error msg or NULL if no error.
#'   * seen, which could be TRUE or FALSE
#'   * time, which is always 0
#'
#' @examples
#'     # Stimulus is Size III white-on-white as in the HFA
#' chooseOpi("SimGaussian")
#' if (!is.null(opiInitialize(sd = 1.6)))
#'   stop("opiInitialize failed")
#'
#' result <- opiPresent(stim = list(lum = dbTocd(20)), tt = 30, fpr = 0.15, fnr = 0.01)
#'
#' if (!is.null(opiClose()))
#'   warning("opiClose() failed")
#'
opiPresent_for_SimGaussian <- function(stim, fpr = 0.03, fnr = 0.01, tt = 30, ...) {

    if (!exists(".opi_env") || !exists("sim_gaussian", where = .opi_env))
        return(list(err = "You have not called opiInitialise.", seen = NA, time = NA))

    if (is.null(stim) || ! "lum" %in% names(stim))
        return(list(err = "'stim' should be a list with a name 'lum'. stim$lum is the cd/m^2 to present.", seen = NA, time = NA))

    px_var <- .opi_env$sim_gaussian$sd

    pr_seeing <- fpr + (1 - fpr - fnr) * (1 - stats::pnorm(stim$lum, mean = tt, sd = px_var))

    return(list(
        err = NULL,
        seen = stats::runif(1) < pr_seeing,
        time = 0
    ))
}

#' Does nothing.
#'
#' @return NULL
#'
opiClose_for_SimGaussian <- function() NULL

#' Returns a simple list.
#'
#' @return A list with one element `machine` that is `"SimHenson"`.
#'
opiQueryDevice_for_SimGaussian <- function() list(machine = "SimGaussian")

#' Does nothing.
#'
#' @param state Any object you like, it is ignored.
#'
#' @return NULL
#'
opiSetup_for_SimGaussian <- function(state)  NULL