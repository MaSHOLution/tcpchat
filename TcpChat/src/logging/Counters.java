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

/**
 * Contains all counters
 *
 * @author Manuel Schmid
 */
public class Counters {

    /**
     * Contains counters of clients
     */
    public static class Clients {

        /**
         * currently logged in clients
         */
        public static int clients = 0;

        /**
         * clients in login process
         */
        public static int inLoginProcess = 0;
    }

    public static class Totals {

        /**
         * exceptions total
         */
        public static int exceptions = 0;

        /**
         * Contains totals of messages
         */
        public static class Messages {

            /**
             * private messages total
             */
            public static int pmTotal = 0;
            /**
             * group messages total
             */
            public static int gmTotal = 0;
        }

        /**
         * Contains totals of clients
         */
        public static class Clients {

            /**
             * total logins
             */
            public static int logins = 0;

            /**
             * total disconnects
             */
            public static int disconnects = 0;

            /**
             * total disconnects
             */
            public static int connections = 0;
        }

    }

    public static void login() {
        Clients.inLoginProcess++;
    }

    public static void loggedIn() {
        Clients.inLoginProcess--;
        Clients.clients++;
        Totals.Clients.logins++;
    }

    public static void pm() {
        Totals.Messages.pmTotal++;
    }

    public static void gm() {
        Totals.Messages.gmTotal++;
    }

    public static void disconnect() {
        Clients.clients--;
        Totals.Clients.disconnects++;
    }

    public static void connection() {
        Totals.Clients.connections++;
    }

    public static void exception() {
        Totals.exceptions++;
    }

}
