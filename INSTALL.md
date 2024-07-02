---
title: "OPI-JOVP Installation"
output:
  html_document:
    toc: true
    number_sections: true
---

`UNDER CONSTRUCTION Tue  2 Jul 2024 14:02:09 AWST`


# OPI-JOVP 

Created by Iv&aacute;n Mar&iacute;n-Franch and Andrew Turpin commencing October 2022.

This is new middleware (or "OPI server") for the <a href="https://opi.lei.org.au">Open Perimetry Interface</a> 
that allows 
the <a href="https://cran.r-project.org/web/packages/OPI/index.html">OPI R package</a> 
to use screen-based devices such as phones, 
VR headsets, and monitors. In attempt to be device independent for screen-based perimeters, it 
makes use of the 
<a href = "https://github.com/imarinfr/jovp">JOVP</a> written by Iv&aacute;n Mar&iacute;n-Franch which in turn
is built upon the <a href="https://www.vulkan.org">Vulkan</a> platform.


# Test installation
1. Make sure you have the OPI R package installed (v3.0 or higher)
2. Start the Opi Monitor (by running run_opi_monitor.bat)
3. Take note of the IP address of the Monitor in the "localhost" box (copy it to clipboard)
4. In the Monitor window choose the "Echo" machine and press "Connect"
5. Edit the test.r script making the variable `IP` equal to the IP address of the Monitor (you copied it, right!?)
6. Run the test.r script. You should see commands echoed in the Monitor window.