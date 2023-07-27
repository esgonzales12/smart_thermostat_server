package org.estefan.logging;

import java.util.logging.Logger;

public abstract class StaticLogBase {
    protected static Logger log = Logger.getLogger("default");
    static {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "%1$tF %1$tT %4$s %2$s %5$s%6$s%n");
    }
}
