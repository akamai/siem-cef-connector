package net.meta.cefconnector.dataaccess;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DBUtilTest {

	@Before
	public void init() {
		try {
			DBUtil.initDatabase();
		} catch (SQLException e) {
			fail("Could not create database");
		}
	}

	@After
	public void destroy() {
		try {
			DBUtil.destroyDatabase();
		} catch (SQLException e) {
			fail("Could not create database");
		}
	}

	@Test
	public void testGetConnection() {
		try (Connection connection = DBUtil.getConnection()) {
			assertNotNull(connection);
		} catch (SQLException e) {
			fail("Could not get connection");
		}
	}

	@Test
	public void testInitDatabase() {
		try {
			String str = DBUtil.getLastOffset();
			System.out.println(str);
		} catch (SQLException e) {
			fail("Could not create database");
		}
	}

	@Test
	public void testSetLastOffset() {
		try {
			String expected = "abcd";
			DBUtil.setLastOffset(expected);
			String actual = DBUtil.getLastOffset();
			assertEquals(actual, expected);

		} catch (SQLException e) {
			fail("Could not create database");
		}
	}

}
