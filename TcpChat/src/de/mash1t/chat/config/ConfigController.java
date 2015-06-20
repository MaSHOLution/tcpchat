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

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.EnumSet;
import java.util.Properties;

/**
 * Class for reading/writing config data from/to file system
 *
 * TODO Add property-support for client
 *
 * @author Manuel Schmid
 */
public final class ConfigController {

    private final Properties properties = new Properties();
    private final EnumSet<ConfigParam> serverConfig = EnumSet.allOf(ConfigParam.class);
    private final String fileName;

    /**
     * Constructor, directly loads configurations from file into internal
     * variable
     *
     * @param filename name of the ini-file
     */
    public ConfigController(String filename) {
        this.fileName = filename;
    }

    /**
     * Reads a config file
     *
     * @return
     */
    public boolean readConfigFile() {
        BufferedInputStream stream = null;
        try {
            stream = new BufferedInputStream(new FileInputStream(this.fileName));
            properties.load(stream);
            return true;
        } catch (IOException ex) {
        } finally {
            try {
                stream.close();
            } catch (Exception ex) {
            }
        }
        return false;
    }

    /**
     * Getter for a specific parameter in the config file
     *
     * @param param the param to get the configuration from
     * @return configuration set in file
     */
    public String getConfigValue(ConfigParam param) {
        return properties.getProperty(param.getConfigString());
    }

    /**
     * Getter for a specific parameter of type integer in the config file
     *
     * @param param
     * @return
     */
    public int getConfigValueInt(ConfigParam param) {
        return Integer.parseInt(properties.getProperty(param.getConfigString()));
    }

    /**
     * Getter for a specific parameter of type integer in the config file
     *
     * @param param
     * @return
     */
    public boolean getConfigValueBoolean(ConfigParam param) {
        return Boolean.parseBoolean((properties.getProperty(param.getConfigString())));
    }

    /**
     * Validates all set values
     *
     * @return validated
     */
    public boolean validateConfig() {
        String temp;
        for (ConfigParam param : serverConfig) {
            temp = getConfigValue(param);
            if (temp == null) {
                return false;
            } else {
                if (!validateParam(param, temp)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks if a single config parameter is set right
     *
     * @param param the config parameter
     * @param temp the value of the config parameter
     * @return
     */
    private boolean validateParam(ConfigParam param, String temp) {
        switch (param) {
            case Port:
                int port = Integer.parseInt(temp);
                if (port < 1 || port > 65535) {
                    return false;
                }
                break;
            case MaxClients:
                int maxClients = Integer.parseInt(temp);
                if (maxClients < 0) {
                    return false;
                }
                break;

            // Validate booleans
            case LogFiles:
            case LogConsole:
            case CleanLogsOnStartup:
                if (!temp.equals("true") && !temp.equals("false")) {
                    return false;
                }
                break;
        }
        return true;
    }

    /**
     * Creates a file with default values
     *
     * @return true/false
     */
    public boolean makeDefaultFile() {

        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.fileName), "utf-8"));
            for (ConfigParam param : serverConfig) {
                writer.write(param.getConfigString() + "=" + param.getDefaultValue());
                writer.newLine();
            }
            return true;
        } catch (IOException ex) {
            return false;
        } finally {
            try {
                writer.close();
            } catch (Exception ex) {
            }
        }
    }
}
