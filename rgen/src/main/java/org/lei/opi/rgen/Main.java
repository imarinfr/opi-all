package org.lei.opi.rgen;

import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import javafx.scene.Scene;

import java.util.Arrays;

import org.lei.opi.core.OpiMachine;

/**
 * Generate R code for machines defined in org.lei.opi.core.*.
 */
public class Main {

    /** Generate license header for an R file, and open_socket method, 
     * create env.{machinename} list for environment of the R code.
     * 
     * @param machineName Machine name to stick in header (first line)
     * @return Header string
     */
    static final String makeHeader(String machineName) { return String.format("""
# Open Perimetry Interface implementation for %s
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

    # environment for this machine in R
if (exists(".opi_env") && !exists("%s", where = .opi_env))
    assign("%s", new.env(), envir = .opi_env)
        """, 
machineName,  // header title
machineName, machineName);  // environment name
}

    /**
     * Produce the R methods for a single machine and output it on writer.
     * (1) For each of the 5 opi functions
     *   (1.1) Write out the R function header
     *   (1.2) Write out the R function body
     * (2) Write out a function for opiSetBackground which is deprecated
     * 
     * @param machine An {@link OpiMachine} object that will be the basis for the R code.
     * @param writer  A {@link PrintWriter} to which output is sent.
     */
    static void makeR(OpiMachine machine, PrintStream writer) {
        String machineName = machine.getClass().getSimpleName();

        OpiFunction[] functions = { 
            new OpiFunction(machine, "opiInitialise", "initialize", "address", "list(err = %s)", true),
            new OpiFunction(machine, "opiQueryDevice", "query", "", "list(%s)", false),
            new OpiFunction(machine, "opiSetup", "setup", "settings", "%s", false),
            new OpiFunction(machine, "opiPresent",   "present", "stim", "list(err=%s, seen=%s, time=%s", false),
            new OpiFunction(machine, "opiClose", "close", "", "%s", false)
        };

        writer.println(Main.makeHeader(machine.getClass().getSimpleName()));

        for (OpiFunction f : functions) 
            f.generateR(writer);

        writer.println(String.format("""

#' Set background color and luminance in both eyes. 
#' Deprecated for OPI >= v3.0.0 and replaced with [opiSetup()].
#' @usage NULL
#' @seealso [opiSetup()]
opiSetBackground_for_%s <- function(lum, color, ...) {return("Deprecated")}

        """, machineName));
    } 
  
    public static void main(String args[]) {
        String path = "rgen/src/main/OPI/R/";
        String machinesWithoutEcho[] = Arrays.stream(OpiMachine.MACHINES).filter(s -> !s.equals("Echo")).toArray(String[]::new);
        for (String machineName : machinesWithoutEcho) {
            PrintStream printStream = null;
            OpiMachine machine = null;
            try {
                File file = new File(String.format("%s%s.r", path, machineName));
                printStream = new PrintStream(file);

                Class<?> c = Class.forName("org.lei.opi.core." + machineName);
                Constructor<?> ctor = c.getConstructor(new Class[] {Scene.class});
                machine = (OpiMachine)ctor.newInstance(new Object[] {null});
            } catch (Exception e) {
                System.err.println(machineName + "\n" + e);
                System.exit(-1);
            }
            makeR(machine, printStream);
        }
    }
}
