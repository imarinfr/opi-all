package org.lei.opi.rgen;

import java.io.PrintStream;
import java.io.PrintWriter;

import java.lang.reflect.Method;
import java.util.HashMap;

import org.lei.opi.core.OpiMachine;
import org.lei.opi.core.structures.Parameter;
import org.lei.opi.core.structures.Parameters;
import org.lei.opi.core.structures.ReturnMsg;
import org.lei.opi.core.structures.ReturnMsgs;

/**
 * Class to hold information about the mapping from opi functions 
 * to funcitons in Imo.java and also to generate the R code.
 * @param opiName Name used in OPI standard and R code 
 * @param opiCoreName Name used in this java pacakge (opi-core) 
 * @param opiInputFields Essential input field names in OPI Standard. These will be paramters in the R function. 
 * @param opiReturnTemplate A format string that is valid R with %s for places where return values should be plugged in. eg "list(err=%s")" 
*/
public record OpiFunction(
    String   opiName,
    String   opiCoreName,
    String[] opiInputFields,
    String   opiReturnTemplate) {

    /**
    * Read @Parameters from opiCoreName function in machine and 
    * generate R code for function opiName.
    *
    * @param machine {@link OpiMachine} for which to gerneate R code.
    * @param writer {@link PrintWriter} to which to write output.
    */
    public void generateR(OpiMachine machine, PrintStream writer) {
            // 1 Get Parameter and ResultMsg annotations for Method this.opiCoreName in machine
            // 2 Write an R function called this.opiName that takes the Parameters and 
            // 3 returns the ResultMsg fields in the this.opiReturnTemplate 
            // 4 Don't forget to write the roxygen2 doco as well!

                // (1)
            record MethodData(Parameters parameters, ReturnMsgs returnMsgs) {}
            HashMap<String, MethodData> methods = new HashMap<String, MethodData>();

            for (Method method : machine.getClass().getMethods())
                methods.put(method.getName(), new MethodData(
                    method.getAnnotation(Parameters.class),
                    method.getAnnotation(ReturnMsgs.class)
                ));

            String funcSignature = String.format("%s <- function(%s)", 
                this.opiName, 
                String.join(", ", this.opiInputFields));
            //for (String inP : this.opiInputFields)
             //   funcSignature += String.format("%s, ", inP);
            //funcSignature += String.format("%s, ", inP);

            writer.println(funcSignature);
    }
};
