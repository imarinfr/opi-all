---
title: "OPI-JOVP Server"
output:
  html_document:
    toc: true
    number_sections: true
---

# OPI-JOVP Server

Created by Iv&aacute;n Mar&iacute;n-Franch and Andrew Turpin commencing October 2022.

## Description

This is a new <a href="https://perimetry.org/opi">Open Perimetry Interface</a>
middleware (or "OPI server") and some associated changes
to the <a href="https://cran.r-project.org/web/packages/OPI/index.html">OPI R package</a>
to allow for use on screen-based devices such as phones,
VR headsets, and monitors. In attempt to be device independent for screen-based perimeters, it
makes use of the
<a href = "https://github.com/imarinfr/jovp">JOVP</a> written by Iv&aacute;n Mar&iacute;n-Franch which in turn
is built upon the Vulkan platform.

Some of the old implementations of the OPI Server (Octopus 900, Kowa AP7000 and iCare Compass/MAIA)
will remain the same for now, but perhaps they could eventually be incorporated into this
framework.

## Overall architecture

The system works using TCP/IP sockets to connect this code (the OPI-JOVP SERVER)
with both a controlling *client* (for example, R code that uses the OPI R package)
and a target *machine* (for example, a Tempo perimeter or an Android Phone).
Messages are sent in JSON format according to the protocol specified as part of the
core code using the `@Parameter` annotator.

<pre>
+--------------------+        +-----------+        +-----------------+
| Controlling Client |  JSON  |           |  JSON  |     Machine     |
| (eg OPI R package) |<------>|  Monitor  |<------>| (Display Device |
|                    | TCP/IP |           | TCP/IP |  or Perimeter)  |
+--------------------+        +-----------+        +-----------------+

                           |------------- OPI-JOVP Sever ---------------|

</pre>

Both the client connection and machine connection are handled
using socket connections that use JSON strings for messages.

## Packages

### Monitor
The "middle" executable is driven by the `monitor` package, which displays a
GUI written with javaFX for selecting and connecting to Machines,
listening to Clients, and for displaying Machine relevant information about JSON
messages that pass through from Client to Machine and back again.
Specific GUI `Scenes` for each machine
are specified as FXML documents in the core/resources folder and
their class definitions (subclasses of `OpiMachine`) act as
fx:controllers for the associated document.

                                  +---------+
                                  | Monitor |
                   +--------------+---------+--------------+
                   |                                       |
                   |                      +------------+   |
                   |  +----------+        | OPIMachine |   |
                   |  |   GUI    |      +-+------------+-+ |
                   |  |  fx:...  |      |                | |
                   |  +----------+      |  +----------+  | |
                   |                    |  |  Socket  |<-------> Machine
                   |                    |  +----------+  | |
                   |  +-------------+   |                | |
         Client <---->| OpiListener |<->|  fx:controller |<- - - - - - core/resources/*.fxml
                   |  +-------------+   +----------------+ |
                   |                                       |
                   +---------------------------------------+


### Core
The classes in the `core` module specify machines, the protocol and GUI for each machine,
and basic utilities for socket communication and JSON processing. Reflection is used heavily
in this system, protocol so it is not for the Java novice.
The (abstract) super class for all devices is `OpiMachine` which defines the protocol and behaviour
for the 5 functions defined in the OPI Standard: `opiInitialise`, `opiQueryDevice`, `opiSetup`,
`opiPresent`, and `opiClose`.
Machine specific parameters that make up the format of JSON messages that machine expects (ie
the protocol) are defined by `@Parameter` and `@ReturnMsg` annotations on each of the 5 methods in the
machine's subclass of `OpiMachine`.

### JOVP

This executable package implements the JOVP Machine that in turn calls the
<a href = "https://github.com/imarinfr/jovp">JOVP</a> library written by Iv&aacute;n Mar&iacute;n-Franch.
This library allows display of psychophysical stimuli on display devices.
This repo implements the left hand box in this JOVP machine diagram.

<pre>

                            A JOVP Machine
           +-------------------------------------------------+
           |                     Physical Device             |
           |                     (eg PicoVR, imoVifa, ...)   |
      JSON |  +---------+       +-------------------------+  |
     <-----+->|  JOVP   |<----->|  +---------+            |  |
           |  | package |       |  |  JOVP   |  Native    |  |
           +  +---------|       |  | Library |  Software  |  |
           |                    |  +---------+            |  |
           |                    |  | Vulkan  |            |  |
           |                    |  +---------+            |  |
           |                    +-------------------------+  |
           +-------------------------------------------------+

</pre>
### R Generation
In an attempt to reduce mismatches in the protocol between the Client and Machine,
the R code for sending messages is automatically
generated to match that expected by the relevant Machine.
This happens in the `rgen` package. In essence, the `@Parameter` and `@ReturnMsg`
annotators of the five opi functions in the `core::OpiMachine` subclasses are used to
create the relevant R code.
This module is not for general use, but rather to be run to update the OPI
R package whenever a new machine is added, or an interface to an existing machine changes.

## Licence and Copyright

The [OPI-project] (WEBSITE) project and all its modules are COPYRIGHTED by Andrew Turpin
and Iv&aacute;n Mar&iacute;n-Franch, and is distributed
under the Apache 2.0 license. Please read the license information in the attached file

## Future Work
* Write a doclet to format @Parameters Annotation nicely in javadoc.
* Modularise OpiMachine.process() so Enum, Double and List are all handled consistently.

* Add eye tracking to GUI if available

* Far future: incorporate Compass, O900, etc into this framework


# OPI R package v3.*

This is an updated version of the OPI R package to allow for the
increased functionality of screen based perimeters over projection
based perimeters. For example, screen based perimeters make no hardware
distinction between static, kinetic and temporal stimuli; nor do they
have arbitrary restrictions on stimuli size or colors.

Backwards compatibility with previous OPI versions
is maintained.

There are three main changes.

  1. Calling of opi functions internally. Each machine now has
  its own set of OPI functions called `opiX_for_machine()` rather
  than  the previous `machine.opiX()` functions (which were not
  exposed/exported to the user). This should not affect backwards
  compatibility and makes no change from a user point of view who
  still just calls `opiX()` after `chooseOPI()`. This change will
  generate help pages for each `opiX_for_machine()`, hopefully
  making the help easier to read.

  2. Deprecation of `opiSetBackground` replaced with `opiSetup`. The
  screen based perimeters have a lot more options other than
  background and fixation and might want those options changed
  frequently, unlike in the projection perimeters where they are
  usually set once at the beginning of the test. As a result, this
  change limits `opiInitialise` specifically to establishing a
  connection with the machine.

  3. New OPI machines have been added for screen-based perimeters
  that rely on the The Java Open Visual Psychophysics (JOVP) library:
  `Display`, `ImoVifa`, `PhoneHMD`, `PivoVR`. For these to function, one
  needs the external OPI-JOVP server and JOVP Java packages.
  Note that code for these machines is automatically generated as per above.
  These machines have slightly different calls for `opiX()` functions than in
  the previous standard.


# Developer notes

## Installing opencv on mac June 2024

<pre>
  pom contains

    <dependency>
      <groupId>org.openpnp</groupId>
      <artifactId>opencv</artifactId>
      <version>4.9.0-0</version>
    </dependency>

  Then on the command line

    brew install opencv   # make sure the version matches the pom
    brew edit opencv      # change java to "ON", note name of formula file edited = X
    export HOMEBREW_NO_INSTALL_FROM_API=1
    brew reinstall --build-from-source opencv --formula <X>
</pre>