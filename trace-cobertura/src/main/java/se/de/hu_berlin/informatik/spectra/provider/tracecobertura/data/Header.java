package se.de.hu_berlin.informatik.spectra.provider.tracecobertura.data;

import java.io.PrintStream;

public abstract class Header {

    public static String version() {
        Package thisPackage = Header.class.getPackage();
        return (thisPackage != null
                ? thisPackage.getImplementationVersion()
                : "cvs");
    }

    public static void print(PrintStream out) {
        out.println("Cobertura " + version()
                + " - GNU GPL License (NO WARRANTY) - See COPYRIGHT file");
    }
}