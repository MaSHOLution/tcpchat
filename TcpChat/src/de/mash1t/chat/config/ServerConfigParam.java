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
package de.mash1t.chat.config;

/**
 * Class for configuration parameters for server
 *
 * @author Manuel Schmid
 */
public enum ServerConfigParam {

    Port("port", "8000"),
    LogFiles("log_to_files", "true"),
    LogConsole("log_to_console", "false"),
    CleanLogsOnStartup("clean_logs_on_start", "false");

    private final String configString;
    private final String defaultValue;

    /**
     * Constructor
     *
     * @param name
     */
    ServerConfigParam(String configString, String defaultValue) {
        this.configString = configString;
        this.defaultValue = defaultValue;
    }

    /**
     * Getter for configString
     *
     * @return
     */
    public String getConfigString() {
        return configString;
    }

    /**
     * Getter for defaultValue
     *
     * @return
     */
    public String getDefaultValue() {
        return defaultValue;
    }
}