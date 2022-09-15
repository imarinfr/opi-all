package org.lei.opi.rgen;

import java.io.PrintStream;
import java.io.PrintWriter;

import java.lang.reflect.Method;
import java.util.NoSuchElementException;
import java.util.stream.Stream;
import java.util.stream.Collectors;

import org.lei.opi.core.OpiMachine;
import org.lei.opi.core.structures.Parameter;
import org.lei.opi.core.structures.ReturnMsg;

/**
 * Class to hold information about the mapping from opi functions 
 * to funcitons in Imo.java and also to generate the R code.
 * @param opiName Name used in OPI standard and R code 
 * @param opiCoreName Name used in this java pacakge (opi-core) 
 * @param opiInputFields Essential input field names in OPI Standard. These will be paramters in the R function. 
 * @param opiReturnTemplate A format string that is valid R with %s for places where return values should be plugged in. eg "list(err=%s")" 
 * @param createSocket If true, look for Paramters ipOPI... and portOPI and create a socket for other functions to use.
*/
public record OpiFunction(
    String   opiName,
    String   opiCoreName,
    String[] opiInputFields,
    String   opiReturnTemplate,
    boolean createSocket) {

        /** This is the parameter name for the ip on which OPI R should create socket */
    static final String parameterForIp = "ip_OPI_JOVP";
        /** This is the parameter name for the port on which OPI R should create socket */
    static final String parameterForPort = "port_OPI_JOVP";

    /**
     * R code to generate a JSON msg of list of params and values
     */
    static final String sendMessage(Parameter[] params, String envName) {
        return String.format("""
        msg <- list(%s); 
        msg <- toJSON(msg)
        writeLines(msg, %s$socket)
    """, 
        Stream.of(params)
            .map((Parameter p) -> String.format("%s = %s", p.name(), p.name()))
            .collect(Collectors.joining(","))
        ,
        envName, envName);
    }

    /**
     * R code to generate a JSON msg of list of params and values
     */
    static final String makeReturnCode(ReturnMsg[] retMsgs, String envName) {
        return String.format("""
        res <- fromJSON(readLines(%s$socket, n=1))
        return(res)
    """, 
        envName);
    }

    private record MethodData(Parameter[] parameters, ReturnMsg[] returnMsgs) {};

    /**
    * Read @Parameters from opiCoreName function in machine and 
    * generate R code for function opiName.
    *
    * @param machine {@link OpiMachine} for which to gerneate R code.
    * @param writer {@link PrintWriter} to which to write output.
    *
    * @throws NoSuchElementException If this.opiCoreName is not found as a method in machine
    */
    public void generateR(OpiMachine machine, PrintStream writer) {
            // 1 Get Parameter and ResultMsg annotations for Method this.opiCoreName in machine
            // 2 Write an R function called this.opiName that takes the Parameters and 
            // 3 returns the ResultMsg fields in the this.opiReturnTemplate 
            // 4 Don't forget to write the roxygen2 doco as well!

        String machineName = machine.getClass().getSimpleName();
        String envName = String.format("env.%s", machineName);

                // (1)
        MethodData mData = Stream.of(machine.getClass().getMethods())
                .filter((Method m) -> m.getName() == this.opiCoreName)
                .findAny()
                .map((Method m) -> new MethodData(
                    m.getAnnotationsByType(Parameter.class), 
                    m.getAnnotationsByType(ReturnMsg.class)))
                .orElse(null);
        if (mData == null) {
            System.err.print(String.format("Cannot generate R code for function %s in machine %s", 
                this.opiCoreName, machineName));
            return;
        }

        //Stream.of(mData.parameters).map((Parameter p) -> p.name()).forEach(System.out::println);
        //Stream.of(mData.returnMsgs).map((ReturnMsg p) -> p.name()).forEach(System.out::println);

            // (2) make the function header
        String params = Stream.of(mData.parameters).map((Parameter p) -> String.format("%s=NULL",p.name())).collect(Collectors.joining(", "));

        if (opiInputFields.length == 0) {
            if (Stream.of(mData.parameters).filter((Parameter p) -> p.name().contains(".")).findAny().isPresent())
                System.err.println("PANIC: I don't know what to do with a Paramter.name() with a '.' in it.");
        } else if (opiInputFields.length == 1) {
            params = String.format("%s = list(%s)", opiInputFields[0], params);
        } else
            System.err.println("PANIC: I don't know what to do with more than one opiInputFields.");

        String funcSignature = String.format("%s <- function(%s)", this.opiName, params);
        String paramDocumentation = Stream.of(mData.parameters)
            .map((Parameter p) -> String.format("\n#' @param %s %s",p.name(), p.desc()))
            .collect(Collectors.joining(""));

            // (2) Make the first part of function body which 
            //     - either opens socket or uses existing socket
            //     - creates JSON msg from params
            //     - sends the json on the socket
        String socketCode = "";
        if (createSocket) {
            socketCode = String.format("    %s$socket <<- open_socket(%s, %s)", envName, parameterForIp, parameterForPort);
            if (!Stream.of(mData.parameters).filter((Parameter p) -> p.name().equals(parameterForIp)).findAny().isPresent())
                System.err.println(String.format("PANIC: asking to create R function %s to call open_socket without paramter ip.", this.opiName));
            if (!Stream.of(mData.parameters).filter((Parameter p) -> p.name().equals(parameterForPort)).findAny().isPresent())
                System.err.println(String.format("PANIC: asking to create R function %s to call open_socket without paramter port.", this.opiName));
        }

            // (3) make the second part of function body which 
            //    - gets back JSON msg from socket
            //    - returns it as returnMsg format
        // something here about returnmsg???
        String retDocumentation = "\n#' @return a list contianing:" + 
            Stream.of(mData.returnMsgs)
            .map((ReturnMsg r) -> String.format("\n#' * %s %s",r.name(), r.desc()))
            .collect(Collectors.joining(""));

        writer.print(paramDocumentation);
        writer.println(retDocumentation);
        writer.println(funcSignature);
        writer.print("{");
        writer.println(socketCode);
        writer.println(sendMessage(mData.parameters, envName));
        writer.print(makeReturnCode(mData.returnMsgs, envName));
        writer.println("}\n");

    }
};
