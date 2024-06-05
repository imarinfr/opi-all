
IP <- "localhost"
IP <- "192.168.4.149"

MACHINE <- "ImoVifa"
#MACHINE <- "Display"

    @Parameter(name = "eyeStreamIP", desc = "Destination IP address to which eye images are streamed. No streaming if empty string (default).", className = String.class, isList = false, defaultValue = "")
    @Parameter(name = "eyeStreamPortLeft", desc = "Destination UDP Port to which left eye images are streamed.", className = Integer.class, isList = false, min = 0, max = 65535, defaultValue = "50600")
    @Parameter(name = "eyeStreamPortRight", desc = "Destination UDP Port to which right eye images are streamed.", className = Integer.class, isList = false, min = 0, max = 65535, defaultValue = "50601")

require(OPI3)

chooseOpi(MACHINE)
params <- list(address = list( ip = IP, port = 50001))
if (MACHINE == "ImoVifa") {
    s <- system("ifconfig", intern=T)
    ii <- head(grep("inet 192", s))
    my_ip <- strsplit(s[ii], " ")[[1]][[2]]

    params <- c(params, list(eyeStreamIP = myip, eyeStreamPortLeft = 50200, eyeStreamPortRight = 50201))
}

res <- do.call(opiInitialize, params)
print(res)

opiQueryDevice()

