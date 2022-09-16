package org.lei.opi.rgen;

import java.io.PrintStream;

import org.lei.opi.core.ImoVifa;
import org.lei.opi.core.OpiMachine;

/**
 * Generate R code for machines defined in org.lei.opi.core.*.
 */
public class Main {

    /** Generate licence header for an R file, and open_socket method, 
     * create env.{machinename} list for environment of the R code.
     * 
     * @param machineName Machine name to stick in header (first line)
     * @return Header string
     */
    static final String makeHeader(String machineName) { return String.format("""
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

require(rjson)

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
    cat("Looking for server... ")
    suppressWarnings(tryCatch(    
        v <- socketConnection(host = ip, port,
                    blocking = TRUE, open = "w+b",
                    timeout = 10)
        , error=function(e) { 
            stop(paste("Cannot find a server at", ip, "on port",port))
        }
    ))
    close(v)

    cat("Found server at",ip,port,"\\n")

    socket <- tryCatch(
        socketConnection(host=ip, port, open = "w+b", blocking = TRUE, timeout = 1000), 
        error=function(e) stop(paste("Cannot connect to", machineName, "at",ip,"on port", port))
    )
    return(socket)
}

env.%s <- vector("list")    # environment for this machine in R
        """, machineName, machineName);
}


    static final OpiFunction[] functions = { 
        new OpiFunction("opiInitialise", "initialize", new String[] {}, "list(err = %s)", true),
        new OpiFunction("opiPresent",   "present", new String[] {"stim"}, "list(err=%s, seen=%s, time=%s", false),
        new OpiFunction("opiSetup", "setup", new String[] {"settings"}, "%s", false),
        new OpiFunction("opiClose", "close", new String[] {}, "%s", false),
        new OpiFunction("opiQueryDevice", "query", new String[] {}, "list(%s)", false)
    };

    /**
     * Produce the R methods for a single machine and output it on writer.
     * 
     * @param writer  A {@link PrintWriter} to which output is sent.
     * @param machine An {@link OpiMachine} object that will be the basis for the R code.
     */
    static void makeR(OpiMachine machine, PrintStream writer) {
        writer.println(Main.makeHeader(machine.getClass().getSimpleName()));

        for (OpiFunction f : Main.functions) 
            f.generateR(machine, writer);

        writer.println("""

#' Set background color and luminance in both eyes. 
#' Deprecated for ImoVifa and replaced with [opiSetup()].
#' @usage NULL
#' @seealso [opiSetup()]
opiSetBackground_for_ImoVifa <- function(lum, color, ...) {return("Deprecated")}

        """);
    } 
  
    public static void main(String args[]) {
        makeR(new ImoVifa(), System.out);
    }
}
