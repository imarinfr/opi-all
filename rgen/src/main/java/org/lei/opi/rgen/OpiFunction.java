package org.lei.opi.rgen;

import java.io.PrintStream;
import java.io.PrintWriter;

import java.lang.reflect.Method;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.util.function.Function;

import org.apache.commons.lang3.ArrayUtils;
import org.lei.opi.core.OpiMachine;
import org.lei.opi.core.definitions.Parameter;
import org.lei.opi.core.definitions.ReturnMsg;

/**
 * Class to hold information about the mapping from opi functions 
 * to functions in Imo.java and also to generate the R code.
*/
public class OpiFunction {
        /** This is the parameter name for the ip on which OPI R should create socket */
    static final String parameterForIp = "ip";
        /** This is the parameter name for the port on which OPI R should create socket */
    static final String parameterForPort = "port";
        /** This is the name of the OPI environemnt for storing variables, settings, etc */
    static final String opiEnvName = ".opi_env";

    public record MethodData(Parameter[] parameters, ReturnMsg[] returnMsgs) {
        static MethodData append(MethodData m1, MethodData m2) {
            if (m1 == null) return m2;
            if (m2 == null) return m1;
            return new MethodData(
                ArrayUtils.addAll(m1.parameters, m2.parameters),
                ArrayUtils.addAll(m1.returnMsgs, m2.returnMsgs)
            );
        }
    };
    
    String opiName;
    String opiCoreName;
    String opiInputFieldName;
    String opiReturnTemplate;
    boolean createSocket;
    MethodData methodData;
    String machineName;
    String callingExample; // String that is a roxygen2 @example that should at least include this function.

    /**
    * @param machine Name of OPI machine.
    * @param opiName Name used in OPI standard and R code 
    * @param opiCoreName Name used in this java pacakge (opi-core) 
    * @param opiInputFieldName Essential input field name in OPI Standard. Can be empty for no param in the OPI standard.
    * @param opiReturnTemplate A format string that is valid R with %s for places where return values should be plugged in. eg "list(err=%s")" 
    * @param createSocket If true, look for Paramters ipOPI... and portOPI and create a socket for other functions to use.
    */
    public OpiFunction(OpiMachine machine, String opiName, String opiCoreName, String opiInputFieldName,
            String opiReturnTemplate, boolean createSocket) {
        this.opiName = opiName;
        this.opiCoreName = opiCoreName;
        this.opiInputFieldName = opiInputFieldName;
        this.opiReturnTemplate = opiReturnTemplate;
        this.createSocket = createSocket;
        this.machineName = machine.getClass().getSimpleName();

                // get @Parameter and @ReturnMsg annotations for this function 
                // in the base class and any parent classes
        this.methodData = new MethodData(null, null);
        Class<?> c = machine.getClass();
        while (c != null) {
            this.methodData = MethodData.append(Stream.of(c.getMethods())
                .filter((Method m) -> m.getName() == this.opiCoreName)
                .findAny()
                .map((Method m) -> new MethodData(
                    m.getAnnotationsByType(Parameter.class), 
                    m.getAnnotationsByType(ReturnMsg.class)))
                .orElse(null), 
                this.methodData
            );
            c = c.getSuperclass();
        }

        if (this.methodData == null) {
            System.err.print(String.format("Cannot generate R code for function %s in machine %s", 
                this.opiCoreName, machine));
        } 

            // TODO isListList
        String s = Stream.of(this.methodData.parameters())
                   .filter((Parameter p) -> !p.optional())
                   .map((Parameter p) -> 
                       p.className().getSimpleName().equals("Double") || p.isList() ?
                           String.format("%s = %s", p.name(), p.defaultValue()) :
                           String.format("%s = \"%s\"", p.name(), p.defaultValue()))
                   .collect(Collectors.joining(", "));
        this.callingExample = OpiFunction.wrapR(s, 10 + this.opiName.length(), true);
    }

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
    public static String wrapR(String s, int leadingSpaces, boolean isCode) {
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
    private String makeDocumentation() {
        String params = Stream.of(methodData.parameters)
            .map(prettyParam)
            .collect(Collectors.joining("\n"));
        System.out.println(params);
        String rets = "#' @return a list contianing:\n" + 
            Stream.of(methodData.returnMsgs)
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
    params.length() > 0 ? params : "#'", 
    rets.length() > 0 ? rets : "#'",
    machineName,    // chooseOpi
    this.opiName, 
    this.opiInputFieldName.length() > 0 ? String.format("%s = list(%s)", this.opiInputFieldName, this.callingExample) : this.callingExample,
    this.opiName   // seealso
        );
    }

    /**
     * R code to generate a JSON msg of list of params and values
     * @param params List of params to send to machine
     */
    private final String sendMessage() {
        //msg <- c(list(command = "present"), lapply(stim, function(p) ifelse(is.null(p), NULL, p)))
        return String.format("""
        msg <- list(%s)
        msg <- c(list(command = "%s"), msg)
        msg <- msg[!unlist(lapply(msg, is.null))]
        msg <- rjson::toJSON(msg)
        writeLines(msg, %s$%s$socket)
    """, 
        Stream.of(methodData.parameters())
            .map((Parameter p) -> String.format("%s = %s%s", 
                p.name(), 
                this.opiInputFieldName.length() > 0 ? this.opiInputFieldName + "$" : "",
                p.name()))
            .collect(Collectors.joining(", "))
        ,
        this.opiCoreName,
        opiEnvName, this.machineName);
    }

    /**
     * R code to generate a JSON msg of list of params and values
     */
    private String makeReturnCode() {
        return String.format("""
        res <- readLines(%s$%s$socket, n = 1)
        res <- rjson::fromJSON(res)
        return(res)
    """, 
        opiEnvName, this.machineName);
    }

    /**
    * Read @Parameters from opiCoreName function in machine and 
    * generate R code for function opiName.
    *
    * @param writer {@link PrintWriter} to which to write output.
    */
    public void generateR(PrintStream writer) {
            // 1 Get Parameter and ResultMsg annotations for Method this.opiCoreName in machine
            // 2 Write an R function called this.opiName that takes the Parameters and 
            // 3 returns the ResultMsg fields in the this.opiReturnTemplate 
            // 4 Don't forget to write the roxygen2 doco as well!


        //Stream.of(mData.parameters).map((Parameter p) -> p.className().getSimpleName()).forEach(System.out::println);
        //Stream.of(mData.returnMsgs).map((ReturnMsg p) -> p.name()).forEach(System.out::println);

            // (2) make the function header
        String params = Stream.of(this.methodData.parameters()).map((Parameter p) -> String.format("%s = NULL",p.name()))
            .collect(Collectors.joining(", "));

        if (opiInputFieldName.length() == 0) {
            if (Stream.of(this.methodData.parameters()).filter((Parameter p) -> p.name().contains(".")).findAny().isPresent())
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
            socketCode = String.format("    assign(\"socket\", open_socket(%s, %s), %s$%s)", parameterForIp, parameterForPort, opiEnvName, this.machineName);
            if (!Stream.of(this.methodData.parameters()).filter((Parameter p) -> p.name().equals(parameterForIp)).findAny().isPresent())
                System.err.println(String.format("PANIC: asking to create R function %s to call open_socket without paramter %s.", this.opiName, parameterForIp));
            if (!Stream.of(this.methodData.parameters()).filter((Parameter p) -> p.name().equals(parameterForPort)).findAny().isPresent())
                System.err.println(String.format("PANIC: asking to create R function %s to call open_socket without paramter %s.", this.opiName, parameterForPort));

            socketCode = String.format("""
        %s

            msg <- list(command = "choose", machine = "%s")
            msg <- rjson::toJSON(msg)
            writeLines(msg, .opi_env$Jovp$socket)
            res <- readLines(.opi_env$Jovp$socket, n = 1)
            res <- rjson::fromJSON(res)
                    """, socketCode, this.machineName);
        } else {
            socketCode = String.format("""
                if(!exists("%s") || !exists("%s", envir = %s) || !("socket" %%in%% names(%s$%s)) || is.null(%s$%s$socket))
                    stop("Cannot call %s without an open socket to Monitor. Did you call opiInitialise()?.")
                """, opiEnvName, 
                this.machineName, opiEnvName,
                opiEnvName, this.machineName, 
                opiEnvName, this.machineName,
                this.opiName
                );
        }

            // (3) make the second part of function body which 
            //    - gets back JSON msg from socket
            //    - returns it as returnMsg format
        // something here about returnmsg???

        writer.print(makeDocumentation());
        writer.print(funcSignature);
        writer.println(" {");
        writer.println(socketCode);
        writer.println(sendMessage());
        writer.print(makeReturnCode());
        writer.println("}\n");
    }
};
