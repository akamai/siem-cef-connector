package net.meta.cefconnector.dataaccess;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;


public enum ConnectionPool {

	INSTANCE;

	private DataSource ds = null;

	private ConnectionPool() {
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl("jdbc:sqlite:cefconnector.db");
		config.setDriverClassName("org.sqlite.JDBC");
		config.setIdleTimeout(0);
		config.setAutoCommit(true);
		ds = new HikariDataSource(config);
	}

	public DataSource getDatasource() {
		return ds;
	}
}
