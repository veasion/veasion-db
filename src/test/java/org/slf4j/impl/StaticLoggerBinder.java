package org.slf4j.impl;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.spi.LoggerFactoryBinder;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * StaticLoggerBinder
 *
 * @author luozhuowei
 * @date 2021/12/10
 */
public class StaticLoggerBinder implements LoggerFactoryBinder {

    public static String REQUESTED_API_VERSION = "1.7";
    private static StaticLoggerBinder binder = new StaticLoggerBinder();
    private static ILoggerFactory loggerFactory = new SimpleLoggerFactory();

    private StaticLoggerBinder() {
    }

    public static StaticLoggerBinder getSingleton() {
        return binder;
    }

    @Override
    public ILoggerFactory getLoggerFactory() {
        return loggerFactory;
    }

    @Override
    public String getLoggerFactoryClassStr() {
        return SimpleLoggerFactory.class.getName();
    }

    static class SimpleLoggerFactory implements ILoggerFactory {

        @Override
        public Logger getLogger(String s) {
            return new SimpleLogger(s);
        }
    }

    static class SimpleLogger implements Logger {
        private String name;
        private boolean trace = false;
        private boolean debug = true;
        private boolean info = true;
        private boolean warn = true;
        private boolean error = true;

        public SimpleLogger(String name) {
            this.name = name;
        }

        private void println(String level, String s, Object... objects) {
            for (Object object : objects) {
                if (object instanceof Throwable) {
                    continue;
                }
                s = s.replace("{}", String.valueOf(object));
            }
            ("error".equalsIgnoreCase(level) ? System.err : System.out).println(
                    String.format("%s [%s] [%s] [%s] - %s",
                            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()),
                            level.toUpperCase(),
                            Thread.currentThread().getName(),
                            name,
                            s
                    )
            );
            for (Object object : objects) {
                if (object instanceof Throwable) {
                    ((Throwable) object).printStackTrace();
                }
            }
        }

        private void println(String level, String s, Throwable e) {
            println(level, s);
            e.printStackTrace();
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isTraceEnabled() {
            return trace;
        }


        @Override
        public boolean isTraceEnabled(Marker marker) {
            return trace;
        }

        @Override
        public boolean isDebugEnabled() {
            return debug;
        }

        @Override
        public boolean isDebugEnabled(Marker marker) {
            return debug;
        }

        @Override
        public boolean isInfoEnabled() {
            return info;
        }

        @Override
        public boolean isInfoEnabled(Marker marker) {
            return info;
        }

        @Override
        public boolean isWarnEnabled() {
            return warn;
        }

        @Override
        public boolean isWarnEnabled(Marker marker) {
            return warn;
        }

        @Override
        public boolean isErrorEnabled() {
            return error;
        }

        @Override
        public boolean isErrorEnabled(Marker marker) {
            return error;
        }

        @Override
        public void trace(String s) {
            println("trace", s);
        }

        @Override
        public void trace(String s, Object o) {
            println("trace", s, o);
        }

        @Override
        public void trace(String s, Object o, Object o1) {
            println("trace", s, o, o1);
        }

        @Override
        public void trace(String s, Object... objects) {
            println("trace", s, objects);
        }

        @Override
        public void trace(String s, Throwable throwable) {
            println("trace", s, throwable);
        }

        @Override
        public void trace(Marker marker, String s) {
            println("trace", s);
        }

        @Override
        public void trace(Marker marker, String s, Object o) {
            println("trace", s, o);
        }

        @Override
        public void trace(Marker marker, String s, Object o, Object o1) {
            println("trace", s, o, o1);
        }

        @Override
        public void trace(Marker marker, String s, Object... objects) {
            println("trace", s, objects);
        }

        @Override
        public void trace(Marker marker, String s, Throwable throwable) {
            println("trace", s, throwable);
        }


        @Override
        public void debug(String s) {
            println("debug", s);
        }

        @Override
        public void debug(String s, Object o) {
            println("debug", s, o);
        }

        @Override
        public void debug(String s, Object o, Object o1) {
            println("debug", s, o, o1);
        }

        @Override
        public void debug(String s, Object... objects) {
            println("debug", s, objects);
        }

        @Override
        public void debug(String s, Throwable throwable) {
            println("debug", s, throwable);
        }

        @Override
        public void debug(Marker marker, String s) {
            println("debug", s);
        }

        @Override
        public void debug(Marker marker, String s, Object o) {
            println("debug", s, o);
        }

        @Override
        public void debug(Marker marker, String s, Object o, Object o1) {
            println("debug", s, o, o1);
        }

        @Override
        public void debug(Marker marker, String s, Object... objects) {
            println("debug", s, objects);
        }

        @Override
        public void debug(Marker marker, String s, Throwable throwable) {
            println("debug", s, throwable);
        }

        @Override
        public void info(String s) {
            println("info", s);
        }

        @Override
        public void info(String s, Object o) {
            println("info", s, o);
        }

        @Override
        public void info(String s, Object o, Object o1) {
            println("info", s, o, o1);
        }

        @Override
        public void info(String s, Object... objects) {
            println("info", s, objects);
        }

        @Override
        public void info(String s, Throwable throwable) {
            println("info", s, throwable);
        }

        @Override
        public void info(Marker marker, String s) {
            println("info", s);
        }

        @Override
        public void info(Marker marker, String s, Object o) {
            println("info", s, o);
        }

        @Override
        public void info(Marker marker, String s, Object o, Object o1) {
            println("info", s, o, o1);
        }

        @Override
        public void info(Marker marker, String s, Object... objects) {
            println("info", s, objects);
        }

        @Override
        public void info(Marker marker, String s, Throwable throwable) {
            println("info", s, throwable);
        }

        @Override
        public void warn(String s) {
            println("warn", s);
        }

        @Override
        public void warn(String s, Object o) {
            println("warn", s, o);
        }

        @Override
        public void warn(String s, Object... objects) {
            println("warn", s, objects);
        }

        @Override
        public void warn(String s, Object o, Object o1) {
            println("warn", s, o, o1);
        }

        @Override
        public void warn(String s, Throwable throwable) {
            println("warn", s, throwable);
        }

        @Override
        public void warn(Marker marker, String s) {
            println("warn", s);
        }

        @Override
        public void warn(Marker marker, String s, Object o) {
            println("warn", s, o);
        }

        @Override
        public void warn(Marker marker, String s, Object o, Object o1) {
            println("warn", s, o, o1);
        }

        @Override
        public void warn(Marker marker, String s, Object... objects) {
            println("warn", s, objects);
        }

        @Override
        public void warn(Marker marker, String s, Throwable throwable) {
            println("warn", s, throwable);
        }

        @Override
        public void error(String s) {
            println("error", s);
        }

        @Override
        public void error(String s, Object o) {
            println("error", s, o);
        }

        @Override
        public void error(String s, Object o, Object o1) {
            println("error", s, o, o1);
        }

        @Override
        public void error(String s, Object... objects) {
            println("error", s, objects);
        }

        @Override
        public void error(String s, Throwable throwable) {
            println("error", s, throwable);
        }

        @Override
        public void error(Marker marker, String s) {
            println("error", s);
        }

        @Override
        public void error(Marker marker, String s, Object o) {
            println("error", s, o);
        }

        @Override
        public void error(Marker marker, String s, Object o, Object o1) {
            println("error", s, o, o1);
        }

        @Override
        public void error(Marker marker, String s, Object... objects) {
            println("error", s, objects);
        }

        @Override
        public void error(Marker marker, String s, Throwable throwable) {
            println("error", s, throwable);
        }
    }
}
