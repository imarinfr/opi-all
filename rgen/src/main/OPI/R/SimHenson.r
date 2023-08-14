#
# An implementation of the OPI that simulates responses using 
# Henson et al (2000) variability.
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

require(stats)

#' Does nothing.
#'
#' @return NULL
#'
opiClose_for_SimHenson <- function() NULL

#' Returns a simple list.
#'
#' @return A list with one element `machine` that is `"SimHenson"`.
#'
opiQueryDevice_for_SimHenson <- function() list(machine = "SimHenson")

#'
#' Simulates reponses using a Frequency of Seeing (FoS) curve.
#'
#' The FoS is modelled as a cummulative Gaussian function over dB with 
#' standard deviation equal to `min(cap, exp( A * t + B))`, where 
#' t is the threshold/mean of the FoS in dB.
#' All values are in dB relative to `maxStim`.
#'
#' @param type A single character that is:
#'   *  `N` for using the A and B values from the Normals in Henson et al (2000)
#'   *  `G` for using the A and B values from the Glaucomas in Henson et al (2000)
#'   *  `C` for using the A and B values from the Combined in Henson et al (2000)
#'   *  `X` to specify your own A and B values as parameters
#' @param A Coefficient of `t` in the formula (ignored if `type != 'X'`).
#' @param B Addend of `t` in the formula (ignored if `type != 'X'`).
#' @param cap Maximum dB value for the stdev of the FoS curve.
#' @param maxStim The maximum stimuls value (0 dB) in cd/\eqn{\mbox{m}^2}{m^2}.
#' @param ... Any other parameters you like, they are ignored.
#'
#' @return NULL on success or a string message otherwise
#' @examples
#'     # Set up a simple simulation for white-on-white perimetry
#' chooseOpi("SimHenson")
#' if (!is.null(opiInitialize(type = "C", cap = 6)))
#'   stop("opiInitialize failed")
#'
opiInitialise_for_SimHenson <- function(type = "C", A = -0.081, B = 3.27, cap = 6, maxStim = 10000 / pi, ...) {
    if (!is.element(type, c("N", "G", "C", "X"))) {
        msg <- paste("Bad 'type' specified for SimHenson in opiInitialize():", type)
        warning(msg)
        return(msg)
    }

    if (cap < 0)
        warning("cap is negative in call to opiInitialize (simHenson)")

    if (exists(".opi_env") && !exists("sim_henson", where = .opi_env))
        assign("sim_henson", new.env(), envir = .opi_env)

    assign("type",    type, envir = .opi_env$sim_henson)
    assign("cap",     cap, envir = .opi_env$sim_henson)
    assign("maxStim", maxStim, envir = .opi_env$sim_henson)

    if (type == "N") {
        assign("A", -0.066, envir=.opi_env$sim_henson)
        assign("B", 2.81, envir=.opi_env$sim_henson)
    } else if (type == "G") {
        assign("A", -0.098 , envir=.opi_env$sim_henson)
        assign("B", 3.62, envir=.opi_env$sim_henson)
    } else if (type == "C") {
        assign("A", -0.081, envir=.opi_env$sim_henson)
        assign("B", 3.27, envir=.opi_env$sim_henson)
    } else if (type == "X") {
        assign("A" ,  A, envir=.opi_env$sim_henson)
        assign("B",  B, envir=.opi_env$sim_henson)
    }

    if (type == "X" && (is.na(A) || is.na(B))) {
        msg <- "opiInitialize (SimHenson): you have chosen type X, but one/both A and B are NA"
        warning(msg)
        return(msg)
    }

    return(NULL)
}

#' Does nothing.
#'
#' @param settings Any object you like, it is ignored.
#'
#' @return NULL
#'
opiSetup_for_SimHenson <- function(settings)  NULL

#' Determine the response to a stimuli by sampling from a cummulative Gaussian
#' Frequency-of-Seeing (FoS) curve (also known as the psychometric function).
#' 
#' The FoS formula is
#' \deqn{\mbox{fpr}+(1-\mbox{fpr}-\mbox{fnr})(1-\mbox{pnorm}(x, \mbox{tt}, \mbox{pxVar})}
#' where `x` is the stimulus value in dB, and `pxVar` is
#' \deqn{\min(\mbox{cap}, e^{A\times\mbox{tt}+B}).}
#' The ceiling \code{cap} is set with the call to
#' \code{opiInitialize}, and \code{A} and \code{B} are from Table 1 in Henson
#' et al (2000), also set in the call to `opiInitiaise` using the \code{type} parameter.
#'
#' @param stim A list that contains at least:
#'   * `lum` which is the stim value in cd/\eqn{\mbox{m}^2}{m^2}.
#' @param fpr false positive rate for the FoS curve (range 0..1).
#' @param fnr false negative rate for the FoS curve (range 0..1).
#' @param tt  mean of the assumed FoS curve in dB.
#' @param ...  Any other parameters you like, they are ignored.
#'
#' @return A list contianing:
#'   * err, an error msg or NULL if no error.
#'   * seen, which could be TRUE or FALSE
#'   * time, which is always 0
#'
#' @examples
#'     # Stimulus is Size III white-on-white as in the HFA
#' chooseOpi("SimHenson")
#' if (!is.null(opiInitialize(type = "C", cap = 6)))
#'   stop("opiInitialize failed")
#'
#' result <- opiPresent(stim = list(lum = dbTocd(20)), tt = 30, fpr = 0.15, fnr = 0.01)
#'
#' if (!is.null(opiClose()))
#'   warning("opiClose() failed")
#'
opiPresent_for_SimHenson <- function(stim, fpr = 0.03, fnr = 0.01, tt = 30, ...) {
    if (!exists(".opi_env") || !exists("sim_henson", where = .opi_env))
        return(list(err = "You have not called opiInitialise.", seen = NA, time = NA))

    if (is.null(stim) || ! "lum" %in% names(stim))
        return(list(err = "'stim' should be a list with a name 'lum'. stim$lum is the cd/m^2 to present.", seen = NA, time = NA))

    level <- cdTodb(stim$lum, .opi_env$sim_henson$maxStim)
    px_var <- min(.opi_env$sim_henson$cap, exp(.opi_env$sim_henson$A*tt + .opi_env$sim_henson$B)) # variability of patient, henson formula 

    pr_seeing <- fpr + (1 - fpr - fnr) * (1 - stats::pnorm(level, mean = tt, sd = px_var))

    return(list(
        error = NULL,
        seen = stats::runif(1) < pr_seeing,
        time = 0
    ))
}

##################################################################
# Assumes static thresholds/FoS curves and static reaction times.
# Note that false positives and false negatives 
# have to be treated separately from the static responses.
# The location of a false positive is randomly drawn from any 
# location prior to the "true positive" point.
# Note FoS parameters and reaction times picked up 
# from opi_env$SimH set in opiInitialize
#
# NOTE Only works for single path vectors!
#
# Mon Sep 19 17:50:44 AEST 2022 - kept for posterity
##################################################################
#simH.opiPresent.opiKineticStimulus <- function(stim, nextStim=NULL, fpr=0.03, fnr=0.01, tt=NULL, 
#                            criteria=0.95, rt_shape=5.3, rt_rate=1.4, rt_scale=0.1) {
#    if (is.null(stim))
#        stop("stim is NULL in call to opiPresent (using SimHensonRT, opiKineticStimulus)")
#    if (!is.null(nextStim))
#        stop("nextStim should be NULL for kinetic in call to opiPresent (using SimHensonRT, opiKineticStimulus)")
#
#    num_paths <- length(stim$path$x) - 1
#
#    if (num_paths != 1) 
#        stop("Sorry love; kinetic for SimHensonRT only works for single path vectors")
#
#    if (is.null(tt))
#        stop("tt must be a list of functions in call to opiPresent (using SimHensonRT, opiKineticStimulus)")
#
#    if (is(tt[[1]])[1] != "function")
#        stop("tt must be a *list* of functions in call to opiPresent (using SimHensonRT, opiKineticStimulus)")
#
#    if (length(stim$path$y) != num_paths + 1)
#        stop(paste("y is length ",length(stim$path$y), "and should be", num_paths+1, "in SimHensonRT - kinetic"))
#    if (length(stim$sizes) != num_paths)
#        stop(paste("sizes is length ",length(stim$sizes), "and should be", num_paths, "in SimHensonRT - kinetic"))
#    if (length(stim$colors) != num_paths)
#        stop(paste("colors is length ",length(stim$colors), "and should be", num_paths, "in SimHensonRT - kinetic"))
#    if (length(stim$levels) != num_paths)
#        stop(paste("levels is length ",length(stim$levels), "and should be", num_paths, "in SimHensonRT - kinetic"))
#    if (length(stim$speeds) != num_paths)
#        stop(paste("speeds is length ",length(stim$speeds), "and should be", num_paths, "in SimHensonRT - kinetic"))
#
#    ############################################################################## 
#    # Find the first location along the paths where Pr seeing is at least criteria.
#    # Assumes a FoS slope of Gauss(mean=min(6,exp(3.27 -0.081* tt)), stdev=0.25) at all locations.
#    ############################################################################## 
#    eDistP <- function(x1,y1,x2,y2) sqrt((x1-x2)^2 + (y1-y2)^2) 
#
#    prSeeing <- function(stim, tt) {
#        if (is.na(tt))
#            return(0)
#
#        #slope <- rnorm(1, mean=min(.opi_env$SimH$cap, exp(.opi_env$SimH$A*tt + .opi_env$SimH$B)), 0.25)
#        slope <- min(.opi_env$SimH$cap, exp(.opi_env$SimH$A*tt + .opi_env$SimH$B))
#        return(1 - pnorm(stim, tt, slope))
#    }
#
#    path_num <- 1   # for future when multi-paths are supported (bwahahahaha!)
#
#    stim_db <- cdTodb(stim$levels[path_num], .opi_env$SimH$maxStim)
#    
#    path_len <- eDistP(stim$path$x[path_num], stim$path$y[path_num],
#                       stim$path$x[path_num+1], stim$path$y[path_num+1])
#
#    path_angle <- atan2(stim$path$y[path_num+1] - stim$path$y[path_num], stim$path$x[path_num+1] - stim$path$x[path_num])
#
#        # give difference between prSeeing at dist_along_path and criteria
#    f <- function(dist_along_path) prSeeing(stim_db, tt[[path_num]](dist_along_path))
#
#    ds <- seq(0,path_len, 0.01)
#    ii <- which(sapply(ds, f) >= criteria)
#
#    seeing_point <- NULL
#    if (length(ii) > 0)  # found!
#        seeing_point <- list(distance_in_path=ds[head(ii,1)], path_num=path_num)
#
#    #######################################################
#    # Check for false repsonses. Randomise order to check.
#    ######################################################
#    fn_ret <- list(err=NULL, seen=FALSE, time=NA, x=NA, y=NA)
#
#    if (is.null(seeing_point)) {
#        max_d <- eDistP(stim$path$x[path_num], stim$path$y[path_num], stim$path$x[path_num+1], stim$path$y[path_num+1])
#    } else {
#        max_d <- seeing_point$distance_in_path
#    }
#
#    d <- runif(1, min=0, max=max_d)
#
#    fp_ret <- list(err=NULL,
#               seen=TRUE,
#               time=d / stim$speeds[path_num] * 1000, 
#               x=stim$path$x[path_num] + d*cos(path_angle),
#               y=stim$path$y[path_num] + d*sin(path_angle)
#           )
#
#    if (runif(1) < 0.5) {
#        if (runif(1) < fpr) return(fp_ret)   # fp first, then fn
#        if (runif(1) < fnr) return(fn_ret)
#    } else {
#        if (runif(1) < fnr) return(fn_ret)   # fn first, then fp
#        if (runif(1) < fpr) return(fp_ret)
#    }
#
#    #######################################################
#    # Get to here, then no false response
#    ######################################################
#    if (is.null(seeing_point)) {
#        return(list(err=NULL, seen=FALSE, time=NA, x=NA, y=NA))
#    } else {
#            #sample a reaction time
#        rt <- rgamma(1, shape=rt_shape, rate=rt_rate) / rt_scale
#
#        d <- seeing_point$distance_in_path + rt * stim$speeds[path_num] / 1000
#
#        return(list(err=NULL,
#                seen=TRUE,
#                time=d / stim$speeds[path_num] * 1000,
#                x=stim$path$x[path_num] + d*cos(path_angle),
#                y=stim$path$y[path_num] + d*sin(path_angle)
#        ))
#    }
#}
#
