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
package de.mash1t.chat.server.config;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for reading/writing config data from/to file system
 *
 * @author Manuel Schmid
 */
public final class ConfigController {

    private Properties properties = new Properties();
    private EnumSet<ConfigParam> allParamTypes = EnumSet.allOf(ConfigParam.class);

    /**
     * Constructor, directly loads configurations from file into internal variable
     *
     * @param filename name of the config file
     */
    public ConfigController(String filename) {

        BufferedInputStream stream = null;
        try {
            stream = new BufferedInputStream(new FileInputStream(filename));
            properties.load(stream);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ConfigController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ConfigController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                stream.close();
            } catch (IOException ex) {
                Logger.getLogger(ConfigController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
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
     * Validates all set values
     *
     * @return validated
     */
    private boolean validateConfig() {
        String temp;
        for (ConfigParam param : allParamTypes) {
            temp = getConfigValue(param);
            if (temp == null) {
                return false;
            } else {
                validateParam(param, temp);
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
        }
        return true;
    }
}
