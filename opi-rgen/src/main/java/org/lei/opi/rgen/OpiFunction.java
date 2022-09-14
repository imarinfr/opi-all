package org.lei.opi.rgen;

import java.io.PrintWriter;

import org.lei.opi.core.OpiMachine;

/**
 * Class to hold information about the mapping from opi functions 
 * to funcitons in Imo.java and also to generate the R code.
 */
public class OpiFunction {
    /** Name used in OPI standard and R code */
    private String opiName;
    /** Name used in this java pacakge (opi-core) */
    private String opiCoreName;
    /** Essential input field names in OPI Standard */
    private String[] opiInputFields;
    /** Format string with %s for places where return values should be plugged in */
    private String opiReturnTemplate;

    public OpiFunction(String opiName, String opiCoreName, String[] opiInputFields, String opiReturnTemplate) { 
        this.opiName = opiName; 
        this.opiCoreName = opiCoreName;
        this.opiInputFields = opiInputFields;
        this.opiReturnTemplate = opiReturnTemplate;
    }

    /**
    * Read @Parameters from opiCoreName function in machine and 
    * generate R code for function opiName.
    *
    * @param machine {@link OpiMachine} for which to gerneate R code.
    * @param writer {@link PrintWriter} to which to write output.
    */
    public void generateR(OpiMachine machine, PrintWriter writer) {
            // get Paramter and ResultMsg annotations for Method this.opiCoreName in machine
            // write an R function called this.opiName that takes the Parameters and 
            // returnrs the ResultMsg fields
    }
};
