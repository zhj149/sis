/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sis.internal.shapefile.jdbc;

import java.io.File;
import java.sql.SQLException;

/**
 * Exception thrown when a connection is closed.
 * @author Marc LE BIHAN
 */
public class SQLConnectionClosedException extends SQLException {
    /** Serial ID. */
    private static final long serialVersionUID = -7806101485624353416L;

    /** The SQL Statement that whas attempted (if known). */
    private String sql;

    /** The database file. */
    private File database;

    /**
     * Build the exception.
     * @param message Exception message.
     * @param sqlStatement SQL Statement who encountered the trouble, if known.
     * @param dbf The database that was queried.
     */
    public SQLConnectionClosedException(String message, String sqlStatement, File dbf) {
        super(message);
        this.sql = sqlStatement;
        this.database = dbf;
    }

    /**
     * Returns the SQL statement who encountered the "connection closed" alert, if known.
     * @return SQL statement or null.
     */
    public String getSQL() {
        return this.sql;
    }

    /**
     * Returns the database file that is not opened for connection.
     * @return Database file.
     */
    public File getDatabase() {
        return this.database;
    }
}
