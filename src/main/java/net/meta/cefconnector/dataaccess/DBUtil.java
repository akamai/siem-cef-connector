package net.meta.cefconnector.dataaccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DBUtil {
	private static final Logger logger = LogManager.getLogger(DBUtil.class);

	private static String CREATE_LASTOFFSET = "CREATE TABLE IF NOT EXISTS lastoffset (id INTEGER PRIMARY KEY, offset VARCHAR(200))";
	private static String CREATE_LASTPULLDATE = "CREATE TABLE IF NOT EXISTS lastpulldate (id INTEGER PRIMARY KEY, pulldate VARCHAR(20))";
	private static String DROP_LASTOFFSET = "DROP TABLE IF EXISTS lastoffset";
	private static String DROP_LASTPULLDATE = "DROP TABLE IF EXISTS lastpulldate";

	private static String QUERY_FETCH_LASTOFFSET = "SELECT offset FROM lastoffset WHERE id = 1";
	private static String QUERY_INSERT_LASTOFFSET = "INSERT INTO lastoffset VALUES (1, ?)";
	private static String QUERY_UPDATE_LASTOFFSET = "UPDATE lastoffset SET offset = ? WHERE id = 1";


	public static Connection getConnection() throws SQLException {
		Connection connection = null;
		try {

			DataSource dataSource = ConnectionPool.INSTANCE.getDatasource();
			connection = dataSource.getConnection();

		} catch (SQLException sqlException) {
			logger.error("Error getting database connection.");
			throw sqlException;
		}
		return connection;
	}

	public static void initDatabase() throws SQLException {
		try (Connection connection = getConnection(); Statement stmt = connection.createStatement()) {
			// create a new table
			stmt.execute(CREATE_LASTOFFSET);
			stmt.execute(CREATE_LASTPULLDATE);
		} catch (SQLException sqlException) {
			logger.error("Error creating database tables : lastoffset, lastpulldate.");
			throw sqlException;
		}

	}

	public static void destroyDatabase() throws SQLException {
		try (Connection connection = getConnection(); Statement stmt = connection.createStatement()) {
			// create a new table
			stmt.execute(DROP_LASTOFFSET);
			stmt.execute(DROP_LASTPULLDATE);
		} catch (SQLException sqlException) {
			logger.error("Error creating database tables : lastoffset, lastpulldate.");
			throw sqlException;
		}
	}

	public static String getLastOffset() throws SQLException {

		try (Connection connection = getConnection()) {

			return getLastOffset(connection);

		} catch (SQLException sqlException) {
			throw sqlException;
		}
	}

	public static String getLastOffset(Connection connection) throws SQLException {

		String offset = null;
		try (PreparedStatement stmt = connection.prepareStatement(QUERY_FETCH_LASTOFFSET);
				ResultSet rs = stmt.executeQuery()) {

			if (rs.next()) {
				offset = rs.getString("offset");
			}

		} catch (SQLException sqlException) {
			logger.error("Error executing query :" + QUERY_FETCH_LASTOFFSET);
			throw sqlException;
		}

		return offset;
	}

	public static void setLastOffset(String offset) throws SQLException {

		boolean isUpdate = false;

		try (Connection connection = getConnection()) {
			if (getLastOffset(connection) != null) {
				isUpdate = true;
			}
			//connection.setAutoCommit(true);
			try (PreparedStatement stmt = isUpdate ? connection.prepareStatement(QUERY_UPDATE_LASTOFFSET)
					: connection.prepareStatement(QUERY_INSERT_LASTOFFSET)) {
				stmt.setString(1, offset);
				stmt.executeUpdate();
				//connection.commit();
			} catch (SQLException sqlException) {
				logger.error("Error executing query :" + QUERY_FETCH_LASTOFFSET);
				throw sqlException;
			}
		} catch (SQLException sqlException) {
			throw sqlException;
		}
	}
}
