/*
 * The MIT License
 *
 * Copyright 2015 Manuel Schmid.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package logging;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import logging.enums.*;

/**
 *
 * @author Manuel Schmid
 */
public final class LoggingController {

    private final boolean enableLogging;
    private final boolean showOnConsole;
    private final List<Logger> loggerList = new ArrayList<>();

    /**
     * Constructor
     *
     * @param enableLogging enable/disable logging
     * @param showOnConsole enable/disable output on console
     */
    public LoggingController(boolean enableLogging, boolean showOnConsole) {
        this.enableLogging = enableLogging;
        this.showOnConsole = showOnConsole;
    }

    /**
     * Logs a message via a given logger
     *
     * @param logger
     * @param logLevel
     * @param message mnessage to log
     */
    public void log(Logger logger, Level logLevel, String message) {
        // Only log when logging is enabled
        if (this.enableLogging) {
            logger.log(logLevel, message);
        }
    }

    /**
     * Creates a logger
     *
     * @param logName Name of the logger, element of enum LogName
     * @param logPath Path to logfile, element of enum LogPath
     * @return Logger
     */
    public Logger create(LogName logName, LogPath logPath) {
        // Check if logging is enabled
        if (this.enableLogging) {
            // Create logger
            Logger logger = CustomLogging.create(logName, logPath, showOnConsole);
            // Add logger to internal list
            loggerList.add(logger);
            return logger;
        }
        return null;
    }

    /**
     * Gets all loggers which were created in this LoggingController
     *
     * @return List of all loggers created in this LoggingController
     */
    public List<Logger> getAllLoggers() {
        return loggerList;
    }
}
