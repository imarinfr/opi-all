# OPI 3.0.0
 * Major rewrite of the OPI 2.11.3 package to include
   - New support for the OPI-JOVP server hence screen-based perimeters
   - Changed from GNU to Apache 2.0 license
   - R code for all machines automatically generated from `rgen` in `OPI-JOVP` package
   - Deprecated opiSetBackground, replaced with opiSetup
   - Algorithms and simulations stay the same as before
 * Addition of the imoVifa
 * New unit tests
 * Gotchas when working across machines
   - Some of the previous commands were documented to return a list containing `err = NULL`
     on success. For commands where this was the only expected item in the return list, 
     like `opiInitialise` on the Octopus900, this meant they returned an empty list.
     That is, `is.null(result$err)` was trivially true as `is.null(result)` was true. 
     But this is not always the case for `opiInitialise` on all machines. 
     So code for use on more than one machine should check opi function return
     `result` success as `is.null(result$err)`, not `is.null(result)` as in the past.
     Algorithms in this package have been updated to reflect this.
     
