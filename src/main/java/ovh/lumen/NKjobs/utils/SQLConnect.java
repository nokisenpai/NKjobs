package ovh.lumen.NKjobs.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import ovh.lumen.NKcore.api.data.DBAccess;
import ovh.lumen.NKjobs.data.NKData;

public class SQLConnect 
{
	private static final HikariConfig jdbcConfig = new HikariConfig();
	private static HikariDataSource ds = null;

	public static HikariDataSource getHikariDS() 
	{
		if(ds.isClosed())
		{
			ds = new HikariDataSource(jdbcConfig);
		}
		return ds;
	}
	
	public static void setInfo(DBAccess dbAccess)
	{
		jdbcConfig.setPoolName(NKData.PLUGIN_NAME);
		jdbcConfig.setMaximumPoolSize(10);
		jdbcConfig.setMinimumIdle(2);
		jdbcConfig.setMaxLifetime(900000);
		jdbcConfig.setJdbcUrl("jdbc:mysql://" + dbAccess.getHost() + ":" + dbAccess.getPort() + "/" + dbAccess.getDbName() + "?useSSL=false&autoReconnect=true&useUnicode=yes");
		jdbcConfig.setUsername(dbAccess.getUser());
		jdbcConfig.setPassword(dbAccess.getPassword());
		ds = new HikariDataSource(jdbcConfig);
	}
}