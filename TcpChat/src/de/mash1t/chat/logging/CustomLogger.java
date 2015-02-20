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
package de.mash1t.chat.logging;

import de.mash1t.chat.core.RoleType;
import static de.mash1t.chat.logging.LoggingController.checkDir;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * This class can create loggers
 *
 * @author Manuel Schmid
 */
public final class CustomLogger {

    // Setting up date formats 
    private static final DateFormat dateFormatFiles = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss");
    private static final DateFormat dateFormatLogs = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

    /**
     * Creates a logger and adds handler
     *
     * @param logName Name of the logger, element of enum RoleType
     * @param logPath Path to logfile, element of enum LogPath
     * @param logToFiles enable/disable logging to files
     * @param showOnConsole enable/disable output on console
     * @return Logger
     */
    public static Logger create(RoleType logName, LogPath logPath, boolean logToFiles, boolean showOnConsole) {

        // Basic declarations
        Logger logger = Logger.getLogger(logName + "." + logPath);

        if (!showOnConsole) {
            logger.setUseParentHandlers(false);
        }

        if (logToFiles) {
            FileHandler fh = null;
            checkDir();

            // Setting up format for filename
            try {
                fh = new FileHandler(LogPath.LOGDIR.getPath() + "/" + logPath.getPath() + "_" + getCurrentTateTime() + ".log");
            } catch (IOException | SecurityException ex) {
                // TODO handle
            } finally {
                de.mash1t.chat.logging.Counters.exception();
            }

            // Set formatter for logger to get rid of ugly standard format
            fh.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord record) {
                    Calendar cal = new GregorianCalendar();
                    cal.setTimeInMillis(record.getMillis());
                    String recordLevel = record.getLevel().toString();

                    // Giving LogLevels the same margin for better overview
                    while (recordLevel.length() < 10) {
                        recordLevel += " ";
                    }

                    // Building output string
                    String returnString = recordLevel
                            + dateFormatLogs.format(cal.getTime())
                            + ": "
                            + record.getMessage() + System.getProperty("line.separator");
                    return returnString;
                }
            });

            logger.addHandler(fh);
        }
        return logger;
    }

    private static String getCurrentTateTime() {
        //get current date time with Date()
        Date date = new Date();
        return dateFormatFiles.format(date);
    }

    /**
     * Returns a Logger by name and purpose
     *
     * @param logName name of the logger, defined in enum RoleType
     * @param logPath purpose of logger, defined in enum LogPath
     * @return
     */
    public static Logger get(RoleType logName, LogPath logPath) {
        Logger logger = Logger.getLogger(logName + "." + logPath);
        return logger;
    }

    /**
     * Resets all loggers
     */
    public static void resetAllLoggers() {
        LogManager.getLogManager().reset();
    }
}
