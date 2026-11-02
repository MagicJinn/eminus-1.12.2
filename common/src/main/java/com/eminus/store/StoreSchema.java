package com.eminus.store;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.eminus.Eminus;

final class StoreSchema {
    private static final int FRESH_VERSION = 0;

    private static final String CREATE_CELLS =
            "CREATE TABLE cells (key INTEGER PRIMARY KEY, occupancy INTEGER NOT NULL, data BLOB NOT NULL)";
    private static final String CREATE_DICTIONARY =
            "CREATE TABLE dictionary (name TEXT NOT NULL, id INTEGER NOT NULL, value TEXT NOT NULL,"
                    + " PRIMARY KEY (name, id))";
    private static final String CREATE_COLUMNS =
            "CREATE TABLE columns (chunk INTEGER PRIMARY KEY, lowest INTEGER NOT NULL)";
    private static final String DROP_CELLS = "DROP TABLE IF EXISTS cells";
    private static final String DROP_DICTIONARY = "DROP TABLE IF EXISTS dictionary";
    private static final String DROP_COLUMNS = "DROP TABLE IF EXISTS columns";
    private static final String READ_VERSION = "PRAGMA user_version";
    private static final String WRITE_VERSION = "PRAGMA user_version = ";

    private StoreSchema() {
    }

    static void prepare(Connection connection, Path file) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            int version = version(statement);
            if (version == StoreFormat.VERSION) {
                return;
            }

            dropOutdated(statement, version, file);
            create(statement);
        }
    }

    private static int version(Statement statement) throws SQLException {
        try (ResultSet rows = statement.executeQuery(READ_VERSION)) {
            rows.next();
            return rows.getInt(1);
        }
    }

    private static void dropOutdated(Statement statement, int version, Path file) throws SQLException {
        if (version >= StoreFormat.BEFORE_COVERAGE && version < StoreFormat.VERSION) {
            Eminus.LOGGER.warn("The cell store {} is format version {}, whose coarser voxels this build reads "
                    + "otherwise; its cells are dropped and it is rebuilt as version {}", file, version,
                    StoreFormat.VERSION);
            statement.execute(DROP_CELLS);
            statement.execute(DROP_DICTIONARY);
            statement.execute(DROP_COLUMNS);
        } else if (version != FRESH_VERSION) {
            throw new StoreException("The cell store " + file + " is format version " + version
                    + ", this build reads version " + StoreFormat.VERSION, null);
        }
    }

    private static void create(Statement statement) throws SQLException {
        statement.execute(CREATE_CELLS);
        statement.execute(CREATE_DICTIONARY);
        statement.execute(CREATE_COLUMNS);
        statement.execute(WRITE_VERSION + StoreFormat.VERSION);
    }
}
