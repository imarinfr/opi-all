---
title: "OPI Server 3.0 Installation"
output:
  html_document:
    toc: true
    number_sections: true
---

# OPI Server 3.0 

Created by Iv&aacute;n Mar&iacute;n-Franch and Andrew Turpin commencing October 2022.

This is a complete re-write of the <a href="https://perimetry.org/opi">Open Perimetry Interface</a> 
middleware (or "OPI server") and some associated changes
to the <a href="https://cran.r-project.org/web/packages/OPI/index.html">OPI R package</a> 
to allow for use on screen-based devices such as phones, 
VR headsets, and monitors. In attempt to be device independent for screen-based perimeters, it 
makes use of the 
<a href = "https://github.com/imarinfr/jovp">JOVP</a> written by Iv&aacute;n Mar&iacute;n-Franch which in turn
is built upon the Vulkan platform.

# Basic installation

1. Install Java Runtime (JRE) 19
2. Install JavaFX API version 19 
3. Execute run_opi_monitor.bat 

# Test installation
1. Make sure you have the OPI R package installed (v3.0 or higher)
2. Start the Opi Monitor (by running run_opi_monitor.bat)
3. Take note of the IP address of the Monitor in the "localhost" box (copy it to clipboard)
4. In the Monitor window choose the "Echo" machine and press "Connect"
5. Edit the test.r script making the variable `IP` equal to the IP address of the Monitor (you copied it, right!?)
6. Run the test.r script. You should see commands echoed in the Monitor window.