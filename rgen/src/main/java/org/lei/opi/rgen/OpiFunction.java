package org.lei.opi.rgen;

import java.io.PrintStream;
import java.io.PrintWriter;

import java.lang.reflect.Method;

import java.util.stream.Collectors;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import com.google.gson.JsonSyntaxException;

import org.lei.opi.core.OpiMachine;
import org.lei.opi.core.definitions.Parameter;
import org.lei.opi.core.definitions.ReturnMsg;

/**
 * Class to hold information about the mapping from opi functions 
 * to functions in core::OPiMachine and also to generate the R code.
*/
public class OpiFunction {
        /** This is the parameter name for the ip on which OPI R should create socket */
    static final String parameterForIp = "ip";
        /** This is the parameter name for the port on which OPI R should create socket */
    static final String parameterForPort = "port";
        /** This is the name of the OPI environment for storing variables, settings, etc */
    static final String opiEnvName = ".opi_env";

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
        while (s.length() > 80) {
            int i = s.lastIndexOf(isCode ? "," : " ", 80 - leadingSpaces);
            if (i > -1) {
                result += s.substring(0, i);
                if (isCode) 
                    result += ",";
                
                result += "\n#' ";
                result += " ".repeat(leadingSpaces - 3);
                s = s.substring(i + 1);
            } else {
                break;  // could not find the break char, so give up
            }
        } 
        result += s;

        return result;
    }

    public record MethodData(HashMap<String, Parameter> parameters, HashMap<String, ReturnMsg> returnMsgs) {
        void addU(Parameter[] ps) {
            for (Parameter p : ps)
                if (!this.parameters.containsKey(p.name()))
                    this.parameters.put(p.name(), p);
        }
        void addU(ReturnMsg[] rs) {
            for (ReturnMsg r : rs)
                if (!this.returnMsgs.containsKey(r.name()))
                    this.returnMsgs.put(r.name(), r);
        }
    };
   
    String opiName;
    String opiCoreName;
    String opiInputFieldName;
    String opiReturnTemplate;
    boolean createSocket;
    MethodData methodData;
    String machineName;
    OpiMachine machine;
    String callingExample; // String that is a roxygen2 @example that should at least include this function.

    /**
    * @param machine Name of OPI machine.
    * @param opiName Name used in OPI standard and R code 
    * @param opiCoreName Name used in this java package (opi-core) 
    * @param opiInputFieldName Essential input field name in OPI Standard. Can be empty for no param in the OPI standard.
    * @param opiReturnTemplate A format string that is valid R with %s for places where return values should be plugged in. eg "list(err=%s")" 
    * @param createSocket If true, look for Parameters ipOPI... and portOPI and create a socket for other functions to use.
    */
    public OpiFunction(OpiMachine machine, String opiName, String opiCoreName, String opiInputFieldName,
            String opiReturnTemplate, boolean createSocket) {
        this.opiName = opiName;
        this.opiCoreName = opiCoreName;
        this.opiInputFieldName = opiInputFieldName;
        this.opiReturnTemplate = opiReturnTemplate;
        this.createSocket = createSocket;
        this.machineName = machine.getClass().getSimpleName();
        this.machine = machine;

            // get @Parameter and @ReturnMsg annotations for this function (ie name == this.opiCoreName)
            // Get any annotations from super classes if they don't conflict
        this.methodData = new MethodData(new HashMap<String, Parameter>(), new HashMap<String, ReturnMsg>());
        Class<?> c = machine.getClass();
        while (c != null) {
            for (Method m : c.getMethods()) {
                if (m.getName() == this.opiCoreName) {
                    this.methodData.addU(m.getAnnotationsByType(Parameter.class));
                    this.methodData.addU(m.getAnnotationsByType(ReturnMsg.class));
                }
            }

            c = c.getSuperclass();  // go up to parent
        }

        if (this.methodData == null) {
            System.err.print(String.format("Cannot generate R code for function %s in machine %s", 
                this.opiCoreName, machine));
        } 

        this.callingExample = makeCallingExample();
    }

    /**
     * Make the body of the calling example which will be S in 
     *      "result <- opiFunction(x = list(S))" or 
     *      "result <- opiFunction(S)"
     * So we just need to make a list of non-optional paramters and their default values.
     * 
     * @return String of "param = defaultValue, ..."
     */
    private String makeCallingExample() {
        class Formatter {
            static String example(Parameter p) {
                try {
                    Object o = OpiMachine.buildDefault(p, 1);
                    return String.format("%s = %s", p.name(), format(o));
                } catch (ClassNotFoundException ignored) {
                    return "";
                } catch (JsonSyntaxException ignored) {
                    System.out.println(String.format("@Parameter %s has bad JSON for its default value: %s.", p.name(), p.defaultValue()));
                    return "";
                }
            }

            static String format(Object o) {
                if (o instanceof ArrayList)
                    return String.format("list(%s)", ((ArrayList<Object>)o).stream().map((Object t) -> format(t)).collect(Collectors.joining(", ")));
                if (o instanceof Integer) 
                    return String.format("%d", o); 
                if (o instanceof Double) 
                    return String.format("%s", o); 
                return String.format("\"%s\"", o); 
            }
        }

            String s = this.methodData.parameters().values().stream()
                .filter((Parameter p) -> !p.optional())
                .map((Parameter p) -> Formatter.example(p))
                .collect(Collectors.joining(", "));

            return wrapR(s, 10 + this.opiName.length(), true);
    }

   

      // generate roxygen2 string for parameter p
    private static Function<Parameter, String> prettyParam = (Parameter p) -> {
        String prefix =  String.format("#' @param %s ",p.name());
        return prefix + wrapR(p.desc() + (p.optional() ? "(Optional)" : ""), prefix.length(), false);
    };

      // generate roxygen2 string for return value r
    private static Function<ReturnMsg, String> prettyReturn = (ReturnMsg r) -> {
        String prefix = r.name().contains(".") ?
            String.format("#'    - %s ",r.name().replaceAll("\\.", "\\$")) :
            String.format("#'  * %s ",r.name());
        return prefix + wrapR(r.desc(), prefix.length(), false);
    };

      // generate roxygen2 details text for parameter p
    String makeDetails() {
        String all = "";
        for (Parameter p : methodData.parameters.values()) {
            String str = "";
            int prefixLen = 0;
            if (p.isList() || p.isListList()) {
                str = String.format("#' Elements in `%s` can take on values in ", p.name());
                prefixLen = 16 + p.name().length();
            } else {
                str = String.format("#' `%s` can take on values in ", p.name());
                prefixLen = 5 + p.name().length();
            }

            if (p.className() == Double.class) {
                str += String.format("the range [%s, %s].", p.min(), p.max());
            } else if (p.className() == Integer.class) {
                str += String.format("the range [%s, %s].", (int)p.min(), (int)p.max());
            } else if (p.className().isEnum()) {
                List<String> values = machine.enums.get(p.className().getName());
                str += String.format("the set {%s}.", String.join(", ",values));
            } else {
                str = "";
            }

            if (str.length() > 0)
                all += "\n" + wrapR(str, prefixLen, false);
        }

        if (all.length() > 0)
            all = "#' @details " + all;
        else
            all = "#'";
        return all;
    }

    /**
     * Roxygen2 comments at the header of the function.
    *   - List possible type/range of each parameter
    *   - Indicate if a parameter is optional
     */
    private String makeDocumentation() {
        String params = methodData.parameters.values().stream()
            .map(prettyParam)
            .collect(Collectors.joining("\n"));
        String rets = "#' @return a list contianing:\n" + 
            methodData.returnMsgs.values().stream()
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
    this.opiName, // Use...
    params.length() > 0 ? params : "#'", // @params
    rets.length() > 0 ? rets : "#'",     //@return
    makeDetails(),
    machineName,    // chooseOpi
    this.opiName, //result
    this.opiInputFieldName.length() > 0 ? String.format("%s = list(%s)", this.opiInputFieldName, this.callingExample) : this.callingExample,
    this.opiName   // seealso
        );
    }

    /**
     * R code to generate a JSON msg of list of params and values
     */
    private final String sendMessage() {
        Supplier<String> checkNull = () -> {
            if (this.opiInputFieldName.length() > 0)
                return String.format("if (is.null(%s)) return(list(error = 0 , msg = \"Nothing to do in %s.\"))\n", this.opiInputFieldName, this.opiName);
            else
                return "";
        };

        //msg <- c(list(command = "present"), lapply(stim, function(p) ifelse(is.null(p), NULL, p)))
        return String.format("""
        %s
        msg <- list(%s)
        msg <- c(list(command = "%s"), msg)
        msg <- msg[!unlist(lapply(msg, is.null))]
        msg <- rjson::toJSON(msg)
        writeLines(msg, %s$%s$socket)
    """, 
        checkNull.get(), // First check string
        methodData.parameters().values().stream()  // msg list parameters
            .map((Parameter p) -> String.format("%s = %s%s", 
                p.name(), 
                this.opiInputFieldName.length() > 0 ? this.opiInputFieldName + "$" : "",
                p.name()))
            .collect(Collectors.joining(", "))
        ,
        this.opiCoreName,  // command = 
        opiEnvName, this.machineName);  // socket
    }

    /**
     * R code to generate a JSON msg of list of params and values
     */
    private String makeReturnCode() {
        return String.format("""
        res <- readLines(%s$%s$socket, n = 1)
        if (length(res) == 0)
            return(list(error = 5, msg = \"Monitor server exists but a connection was not closed properly using opiClose() last time it was used. Restart Monitor.\"))
        res <- rjson::fromJSON(res)
        return(res)
    """, 
        opiEnvName, this.machineName);
    }

    /**
    * Read @Parameters from opiCoreName function in machine and 
    * generate R code for function opiName.
    * 1 Get Parameter and ResultMsg annotations for Method this.opiCoreName in machine
    * 2 Write an R function called this.opiName that takes the Parameters and 
    *   2.1 The initialize function needs to open a socket, the rest just use it
    *   2.3 The header for each is opiInputFieldName = list (.map(Parameters p -> p.name() = NULL))
    * 3 Returns the ResultMsg fields in the this.opiReturnTemplate 
    * 4 Don't forget to write the roxygen2 doco as well!
    *   4.1 List possible type/range of each parameter
    *   4.2 Indicate if a parameter is optional
    *
    * @param writer {@link PrintWriter} to which to write output.
    */
    public void generateR(PrintStream writer) {
        //Stream.of(mData.parameters).map((Parameter p) -> p.className().getSimpleName()).forEach(System.out::println);
        //Stream.of(mData.returnMsgs).map((ReturnMsg p) -> p.name()).forEach(System.out::println);

            // (2) make the function header
        String funcSignature = String.format("%s_for_%s <- function(%s)", this.opiName, machineName, this.opiInputFieldName);

            // (2) Make the first part of function body which 
            //     - either opens socket or uses existing socket
        String socketCode = "";
        if (createSocket) {
            if (!this.methodData.parameters().values().stream().filter((Parameter p) -> p.name().equals(parameterForIp)).findAny().isPresent())
                System.err.println(String.format("PANIC: asking to create R function %s to call open_socket without paramter %s.", this.opiName, parameterForIp));
            if (!this.methodData.parameters().values().stream().filter((Parameter p) -> p.name().equals(parameterForPort)).findAny().isPresent())
                System.err.println(String.format("PANIC: asking to create R function %s to call open_socket without paramter %s.", this.opiName, parameterForPort));

            socketCode = String.format("""
                        if (!exists(\"socket\", where = %s$%s))
                            assign(\"socket\", open_socket(%s$%s, %s$%s), %s$%s) 
                        else
                            return(list(error = 4, msg = \"Socket connection to Monitor already exists. Perhaps not closed properly last time? Restart Monitor and R.\"))
                    """,
                    opiEnvName, this.machineName,                                  // if exists
                    this.opiInputFieldName, parameterForIp, this.opiInputFieldName, parameterForPort, opiEnvName, this.machineName // assign
                    );
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
            //    - sends the json on the socket
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
