package org.lei.opi.rgen;

import java.io.PrintStream;
import java.io.PrintWriter;

import java.lang.reflect.Method;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.util.function.Function;

import org.lei.opi.core.OpiMachine;
import org.lei.opi.core.structures.Parameter;
import org.lei.opi.core.structures.ReturnMsg;

/**
 * Class to hold information about the mapping from opi functions 
 * to funcitons in Imo.java and also to generate the R code.
 * @param opiName Name used in OPI standard and R code 
 * @param opiCoreName Name used in this java pacakge (opi-core) 
 * @param opiInputFieldName Essential input field name in OPI Standard. Can be empty for no param in the OPI standard.
 * @param opiReturnTemplate A format string that is valid R with %s for places where return values should be plugged in. eg "list(err=%s")" 
 * @param createSocket If true, look for Paramters ipOPI... and portOPI and create a socket for other functions to use.
*/
public record OpiFunction(
    String   opiName,
    String   opiCoreName,
    String   opiInputFieldName,
    String   opiReturnTemplate,
    boolean createSocket) {

        /** This is the parameter name for the ip on which OPI R should create socket */
    static final String parameterForIp = "ip_Monitor";
        /** This is the parameter name for the port on which OPI R should create socket */
    static final String parameterForPort = "port_Monitor";

    /**
     * Break a string into lines using \n#' on spaces if isCode is false else on commas
     * so that no line is longer than 80 chars. Uses simple greedy algorithm.
     * 
     * @param s  String to wrap
     * @param leadingSpaces  Number of leading spaces to allow for on first line, and to 
     *                       add to each line after the first.
     *                       This does not include the 3 chars for "#' ".
     * @param isCode true if an R command (break on commas, not spaces)
     * @return
     */
    private static String wrapR(String s, int leadingSpaces, boolean isCode) {
        String result = "";
        while (s.length() > 80 - leadingSpaces) {
            int i = s.lastIndexOf(isCode ? "," : " ", 80 - leadingSpaces);
            if (i > -1) {
                result += s.substring(0, i);
                if (isCode) 
                    result += ",";
                
                result += "\n#' ";
                result += " ".repeat(leadingSpaces);
                s = s.substring(i + 1);
            } else {
                break;  // could not find the break char, so give up
            }
        } 
        result += s;

        return result;
    }

    private static Function<Parameter, String> prettyParam = (Parameter p) -> {
        String prefix =  String.format("#' @param %s ",p.name());
        return prefix + wrapR(p.desc(), prefix.length(), false);
    };

    private static Function<ReturnMsg, String> prettyReturn = (ReturnMsg r) -> {
        String prefix = r.name().contains(".") ?
            String.format("#'    - %s ",r.name().replaceAll("\\.", "\\$")) :
            String.format("#'  * %s ",r.name());
        return prefix + wrapR(r.desc(), prefix.length(), false);
    };

    /**
     * Roxygen2 comments at the header of the function.
     */
    String makeDocumentation(String machineName, MethodData mData) {
        String params = Stream.of(mData.parameters)
            .map(prettyParam)
            .collect(Collectors.joining("\n"));

        String callingExample = wrapR(
                Stream.of(mData.parameters)
                .filter((Parameter p) -> !p.optional())
                .map((Parameter p) -> 
                    p.className().getSimpleName().equals("Double") || p.isList() ?
                        String.format("%s = %s", p.name(), p.defaultValue()) :
                        String.format("%s = \"%s\"", p.name(), p.defaultValue()))
                .collect(Collectors.joining(", "))
            , 10 + this.opiName.length(), true);

        String rets = "#' @return a list contianing:\n" + 
            Stream.of(mData.returnMsgs)
            .map(prettyReturn)
            .collect(Collectors.joining("\n"));

        return String.format("""
#' Implementation of %s for the %s machine.
#'
#' This is for internal use only. Use [%s()] with
#' these Arguments and you will get the Value back.
#'
#' @usage NULL
#'
%s
#'
%s
#'
#' @examples
#' chooseOpi("%s")
#' result <- %s(%s)
#'
#' @seealso [%s()]
#'
    """,
    this.opiName, machineName, // title
    this.opiName,
    params, rets,
    machineName,    // chooseOpi
    this.opiName, 
    this.opiInputFieldName.length() > 0 ? String.format("%s = list(%s)", this.opiInputFieldName, callingExample) : callingExample,
    this.opiName   // seealso
        );
    }

    /**
     * R code to generate a JSON msg of list of params and values
     * @param params List of params to send to machine
     * @param envName List variable (environment) where $socket lives
     */
    final String sendMessage(Parameter[] params, String envName) {
        return String.format("""
        msg <- list(%s); 
        msg <- rjson::toJSON(msg)
        writeLines(msg, %s$socket)
    """, 
        Stream.of(params)
            .map((Parameter p) -> String.format("%s = %s%s", 
                p.name(), 
                this.opiInputFieldName.length() > 0 ? this.opiInputFieldName + "$" : "",
                p.name()))
            .collect(Collectors.joining(","))
        ,
        envName, envName);
    }

    /**
     * R code to generate a JSON msg of list of params and values
     */
    static final String makeReturnCode(ReturnMsg[] retMsgs, String envName) {
        return String.format("""
        res <- rjson::fromJSON(readLines(%s$socket, n=1))
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

        //Stream.of(mData.parameters).map((Parameter p) -> p.className().getSimpleName()).forEach(System.out::println);
        //Stream.of(mData.returnMsgs).map((ReturnMsg p) -> p.name()).forEach(System.out::println);

            // (2) make the function header
        String params = Stream.of(mData.parameters).map((Parameter p) -> String.format("%s = NULL",p.name())).collect(Collectors.joining(", "));

        if (opiInputFieldName.length() == 0) {
            if (Stream.of(mData.parameters).filter((Parameter p) -> p.name().contains(".")).findAny().isPresent())
                System.err.println("PANIC: I don't know what to do with a Paramter.name() with a '.' in it.");
        } else {
            params = String.format("%s = list(%s)", opiInputFieldName, params);
        } 

        String funcSignature = String.format("%s_for_%s <- function(%s)", this.opiName, machineName, params);

            // (2) Make the first part of function body which 
            //     - either opens socket or uses existing socket
            //     - creates JSON msg from params
            //     - sends the json on the socket
        String socketCode = "";
        if (createSocket) {
            socketCode = String.format("    %s$socket <<- open_socket(%s, %s)", envName, parameterForIp, parameterForPort);
            if (!Stream.of(mData.parameters).filter((Parameter p) -> p.name().equals(parameterForIp)).findAny().isPresent())
                System.err.println(String.format("PANIC: asking to create R function %s to call open_socket without paramter %s.", this.opiName, parameterForIp));
            if (!Stream.of(mData.parameters).filter((Parameter p) -> p.name().equals(parameterForPort)).findAny().isPresent())
                System.err.println(String.format("PANIC: asking to create R function %s to call open_socket without paramter %s.", this.opiName, parameterForPort));
        } else {
            socketCode = String.format("""
                if(!exists(%s$socket) || is.null(%s$socket))
                    stop("Cannot call %s without an open socket to Monitor. Did you call opiInitialise()?.")
                """, envName, envName, this.opiName);
        }

            // (3) make the second part of function body which 
            //    - gets back JSON msg from socket
            //    - returns it as returnMsg format
        // something here about returnmsg???

        writer.print(makeDocumentation(machineName, mData));
        writer.print(funcSignature);
        writer.println(" {");
        writer.println(socketCode);
        writer.println(sendMessage(mData.parameters, envName));
        writer.print(makeReturnCode(mData.returnMsgs, envName));
        writer.println("}\n");
    }
};
