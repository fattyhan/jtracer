package com.jdjr.tracer.db;

public class Path {


    /**
     * Constructor
     */
    public Path() {
    }

    public static class Database {
        public static String LOCAL_DBNAME = "contacts_db";
        public static String HOST = "127.0.0.1";
        public static int PORT = 27017;
        //heroku 平台
        public static String HEROKU_DB_URI = "mongodb://heroku_n35m7bx6:vf99qjg9otp744biaqjtepvurd@ds011725.mlab.com:11725/heroku_n35m7bx6";
        public static String HEROKU_DB_NAME = "heroku_n35m7bx6"; //this is the last part of the HEROKU_DB_URI

    }
}
