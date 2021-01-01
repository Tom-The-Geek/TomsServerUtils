package me.geek.tom.serverutils.chatfilter.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GlobalMethods {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final ThreadLocal<Boolean> messageOk = new ThreadLocal<>();

    private GlobalMethods() { }

    public static void setMessageOk(boolean ok) {
        messageOk.set(ok);
    }

    public static void debug(Object message) {
        LOGGER.info("[ SCRIPT DEBUG ] ==> {}", message);
    }

    public static boolean isMessageOk() {
        return messageOk.get();
    }
}
