package org.lei.opi.rgen;

import java.io.PrintStream;

import org.lei.opi.core.ImoVifa;
import org.lei.opi.core.OpiMachine;

public class Main {

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
""", machineName);
    }

    static final OpiFunction[] functions = { 
        new OpiFunction("opiInitialise_for_ImoVifa", "initialize", new String[] {}, "list(err = %s)"),
        new OpiFunction("opiPresent_for_ImoVifa",   "present", new String[] {"stim"}, "list(err=%s, seen=%s, time=%s"),
        new OpiFunction("opiSetup_for_ImoVifa", "setup", new String[] {}, "%s"),
        new OpiFunction("opiClose_for_ImoVifa", "close", new String[] {}, "%s"),
        new OpiFunction("opiQueryDevice_for_ImoVifa", "query", new String[] {}, "list(%s)")
    };

    /**
     * Produce the R methods 
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
