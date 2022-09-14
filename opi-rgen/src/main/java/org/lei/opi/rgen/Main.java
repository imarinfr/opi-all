package org.lei.opi.rgen;

import java.io.PrintStream;

import org.lei.opi.core.Imo;
import org.lei.opi.core.OpiMachine;

public class Main {

    static final String header = """
#' Open Perimetry Interface implementation for imoVifa
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
""";

    static final OpiFunction[] functions = { 
        new OpiFunction("imo.opiInitialise", "initialize", new String[] {}, "list(err = %s)"),
        new OpiFunction("imo.opiPresent.opiStaticStimulus",   "present", new String[] {"stim"}, "list(err=%s, seen=%s, time=%s"),
        new OpiFunction("imo.opiPresent.opiKineticStimulus",  "present", new String[] {"stim"}, "list(err=%s, seen=%s, time=%s"),
        new OpiFunction("imo.opiPresent.opiTemporalStimulus", "present", new String[] {"stim"}, "list(err=%s, seen=%s, time=%s"),
        new OpiFunction("imo.opiSetBackground", "setup", new String[] {"lum", "color"}, "%s"),
        new OpiFunction("imo.opiClose", "close", new String[] {}, "%s"),
        new OpiFunction("imo.opiQueryDevice", "query", new String[] {}, "list(%s)")
    };

    /**
     * Produce the R methods 
     * 
     * @param writer  A {@link PrintWriter} to which output is sent.
     * @param machine An {@link OpiMachine} object that will be the basis for the R code.
     */
    static void makeR(OpiMachine machine, PrintStream writer) {
        writer.println(Main.header);

        writer.println("""
            imo.opiPresent <- function(stim, nextStim=NULL) { UseMethod("imo.opiPresent") }
            setGeneric("imo.opiPresent")
            """);

        //for (OpiFunction f : Main.functions) 
            //f.generateR(writer);
    } 
  
    public static void main(String args[]) {
        makeR(new Imo(), System.out);
    }
}
