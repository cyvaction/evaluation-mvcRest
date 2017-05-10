package swx.dbaccess;

import java.util.*;
import java.beans.*;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.*;
import java.sql.*;

import org.apache.commons.dbutils.*;
import org.apache.commons.dbutils.handlers.*;
import org.apache.tomcat.jdbc.pool.*;

import oracle.jdbc.OracleTypes;
import oracle.jdbc.internal.OracleCallableStatement;

public final class SqlHelper {

	private static DataSource _dataSource;

	private static PropertyDescriptor[] propertyDescriptors(Class<?> c) {
		BeanInfo beanInfo = null;
		try {
			beanInfo = Introspector.getBeanInfo(c);

		} catch (IntrospectionException e) {
			e.printStackTrace();
		}

		return beanInfo.getPropertyDescriptors();
	}

	private static Object ConvertType(Object value, Class<?> type) {
		if (type.equals(Integer.TYPE)) {
			return Integer.valueOf((String) value);
		} else if (type.equals(Long.TYPE)) {
			return Long.valueOf((String) value);
		} else if (type.equals(Boolean.TYPE)) {
			return Boolean.valueOf((String) value);
		}

		return value;
	}

	// 初始化数据库连接（DKM 2016-10-10）
	public static void InitConnection() {
		Connection conn = null;
		try {
			conn = GetConnection();
			ExecuteResultSet(conn, "select 1 from dual");

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO 自动生成的 catch 块
					e.printStackTrace();
				}
			}
		}
	}

	private static void callSetter(Object target, PropertyDescriptor prop, Object value) {

		Method setter = prop.getWriteMethod();

		if (setter == null) {
			return;
		}

		Class<?>[] params = setter.getParameterTypes();
		try {
			if (value instanceof String && params[0].isEnum()) {
				value = Enum.valueOf(params[0].asSubclass(Enum.class), (String) value);
			}
			setter.invoke(target, ConvertType(value, params[0]));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static DatabaseType _dbType;

	static {

		PoolProperties p = new PoolProperties();
		try {
			Properties prop = new Properties();
			String cfgFilePath = System.getProperty("user.dir") + "/support/src/main/resources/application.properties";
//			String cfgFilePath = SqlHelper.class.getResource("/").getPath() + "application.properties";
			System.out.println(cfgFilePath);
			FileInputStream fis = new FileInputStream(cfgFilePath);
			prop.load(fis);// SqlHelper.class.getResourceAsStream(cfgFilePath)
			fis.close();
			PropertyDescriptor[] pds = propertyDescriptors(PoolProperties.class);
			for (PropertyDescriptor pd : pds) {
				String pName = pd.getName();
				pName = pName.substring(0, 1).toUpperCase() + pName.substring(1);

				if (prop.containsKey(pName)) {
					callSetter(p, pd, prop.get(pName));
				}
			}
			if (prop.containsKey("SJKLX")) {
				_dbType = Enum.valueOf(DatabaseType.class, (String) prop.get("SJKLX"));
			} else {
				_dbType = DatabaseType.Oracle;
			}

			_dataSource = new DataSource();
			_dataSource.setPoolProperties(p);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void ExecuteProcOrFunc(Connection conn, String procOrFuncName, ProcParameter retVal,
										  ProcParameter... params) throws SQLException {

		String tmpCmd;
		if (retVal == null) {
			tmpCmd = "call " + procOrFuncName + "(";
		} else {
			tmpCmd = "? = " + procOrFuncName + "(";
		}
		for (int i = 0; i < params.length; i++) {
			if (_dbType == DatabaseType.Oracle) {
				tmpCmd = tmpCmd + ":" + params[i].Name;
			} else {
				tmpCmd = tmpCmd + "?";// ":" + params[i].Name;
			}
			if (i < params.length - 1) {
				tmpCmd = tmpCmd + ",";
			}
		}
		tmpCmd = tmpCmd + ")";

		CallableStatement cst = null;
		try {
			cst = conn.prepareCall(tmpCmd);
			int curType = Types.REF_CURSOR;
			switch (_dbType) {
//				case Dm:
//					curType = DmdbType.CURSOR;
//					break;
				case Oracle:
					curType = OracleTypes.CURSOR;
					break;
				default:
					break;
			}

			if (retVal != null) {
				cst.registerOutParameter(1, retVal.DataType == Types.REF_CURSOR ? curType : retVal.DataType);
			}
			for (ProcParameter param : params) {
				if (param.Type == ProcPrameterType.In || param.Type == ProcPrameterType.InOut) {
					// 是存储过程的入参
					if (param.DataType == Types.CLOB) {
						Clob tmp = conn.createClob();
						tmp.setString(1, param.Value.toString());
						cst.setClob(param.Name, tmp);
					} else if (param.DataType == Types.BLOB) {
						Blob tmp = conn.createBlob();
						tmp.setBytes(1, (byte[]) param.Value);
						cst.setBlob(param.Name, tmp);
					} else {
						cst.setObject(param.Name, param.Value,
								param.DataType == Types.REF_CURSOR ? curType : param.DataType);
					}
				}
				if (param.Type == ProcPrameterType.Out || param.Type == ProcPrameterType.InOut) {
					// 是存储过程的出参
					cst.registerOutParameter(param.Name, param.DataType == Types.REF_CURSOR ? curType : param.DataType);
				}
			}
			// 执行存储过程
			cst.execute();
			if (retVal != null) {
				switch (_dbType) {
//					case Dm:
//						DmdbCallableStatement dcst = (DmdbCallableStatement) cst;
//						retVal.Value = retVal.getOutResultHandler().handle(dcst.getCursor(1));
//						break;
					case Oracle:
						OracleCallableStatement ocst = (OracleCallableStatement) cst;
						retVal.Value = retVal.getOutResultHandler().handle((ResultSet) ocst.getObject(1));// (arg0)(1);
						break;
					default:
						break;
				}
			}
			// 取出参的值
			for (int i = 0; i < params.length; i++) {
				ProcParameter param = params[i];
				if (param.Type == ProcPrameterType.Out) {
					if (param.DataType != Types.REF_CURSOR) {
						if (param.DataType == Types.CLOB) {
							param.Value = cst.getClob(param.Name);
						} else if (param.DataType == Types.BLOB) {
							param.Value = cst.getBlob(param.Name);
						} else {
							param.Value = cst.getObject(param.Name);
						}
					} else {
						switch (_dbType) {
//							case Dm:
//								DmdbCallableStatement dcst = (DmdbCallableStatement) cst;
//								param.Value = param.getOutResultHandler().handle(dcst.getCursor(param.Name));
//								break;
							case Oracle:
								OracleCallableStatement ocst = (OracleCallableStatement) cst;
								param.Value = param.getOutResultHandler().handle((ResultSet) ocst.getObject(param.Name));
								break;
							default:
								break;
						}
					}
				}
			}
			// System.out.println("开始关闭！");
			cst.close();
			// System.out.println("结束关闭！");
		} finally {
			// System.out.println("finally开始关闭！");
			if (cst != null) {
				cst.close();
			}
			// System.out.println("finally结束关闭！");
		}
	}

	public static String ClobToString(Clob vClob) throws SQLException {
		BufferedReader reader = new BufferedReader(vClob.getCharacterStream());
		StringBuffer ssb = new StringBuffer();
		try {
			String s = reader.readLine();
			while (s != null) {
				ssb.append(s);
				s = reader.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			vClob.free();
		}
		return ssb.toString();
	}

	public static <T> List<T> ExecuteEntity(Class<T> entityClass, String sql) throws SQLException {
		return ExecuteEntity(entityClass, sql, (Object[]) null);
	}

	public static <T> List<T> ExecuteEntity(Class<T> entityClass, String sql, Object... params) throws SQLException {
		QueryRunner qr = new QueryRunner(_dataSource);
		return qr.query(sql, new BeanListHandler<T>(entityClass), params);
	}

	public static <T> List<T> ExecuteEntity(Class<T> entityClass, String procedure, ProcParameter... params)
			throws SQLException {
		Connection conn = GetConnection();
		List<T> result = null;
		try {
			result = ExecuteEntity(conn, entityClass, procedure, params);
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (Exception ignore) {
				}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> ExecuteEntity(Connection conn, Class<T> entityClass, String procedure,
											ProcParameter... params) throws SQLException {
		List<T> result = null;

		ExecuteProcOrFunc(conn, procedure, null, params);
		for (ProcParameter param : params) {
			if ((param.Type == ProcPrameterType.Out || param.Type == ProcPrameterType.InOut)
					&& param.OutResultSetHandlerType == ResultSetHandlerType.BeanList
					&& param.OutBeanType == entityClass) {
				result = (List<T>) param.Value;
				break;
			}
		}
		return result;
	}

	public static <T> List<T> ExecuteEntity(Connection conn, Class<T> entityClass, String sql) throws SQLException {
		return ExecuteEntity(conn, entityClass, sql, (Object[]) null);
	}

	public static <T> List<T> ExecuteEntity(Connection conn, Class<T> entityClass, String sql, Object... params)
			throws SQLException {
		QueryRunner qr = new QueryRunner();
		return qr.query(conn, sql, new BeanListHandler<T>(entityClass), params);
	}

	public static List<Map<String, Object>> ExecuteResultSet(String sql) throws SQLException {
		return ExecuteResultSet(sql, (Object[]) null);
	}

	public static List<Map<String, Object>> ExecuteResultSet(String sql, Object... params) throws SQLException {
		QueryRunner qr = new QueryRunner(_dataSource);
		return qr.query(sql, new MapListHandler(), params);
	}

	public static List<Map<String, Object>> ExecuteResultSet(Connection conn, String sql) throws SQLException {
		return ExecuteResultSet(conn, sql, (Object[]) null);
	}

	public static List<Map<String, Object>> ExecuteResultSet(Connection conn, String sql, Object... params)
			throws SQLException {
		QueryRunner qr = new QueryRunner();
		return qr.query(conn, sql, new MapListHandler(), params);
	}

	public static List<Map<String, Object>> ExecuteResultSet(String procedure, ProcParameter... params)
			throws SQLException {
		Connection conn = GetConnection();
		List<Map<String, Object>> result = null;
		try {
			result = ExecuteResultSet(conn, procedure, params);
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (Exception ignore) {
				}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> ExecuteResultSet(Connection conn, String procedure, ProcParameter... params)
			throws SQLException {
		List<Map<String, Object>> result = null;

		ExecuteProcOrFunc(conn, procedure, null, params);
		for (int i = 0; i < params.length; i++) {
			if ((params[i].Type == ProcPrameterType.Out || params[i].Type == ProcPrameterType.InOut)
					&& params[i].OutResultSetHandlerType == ResultSetHandlerType.MapList) {
				result = (List<Map<String, Object>>) params[i].Value;
				break;
			}
		}
		return result;
	}

	public static <T> T ExecuteScalar(String sql) throws SQLException {
		return ExecuteScalar(sql, (Object[]) null);
	}

	public static <T> T ExecuteScalar(String sql, Object... params) throws SQLException {
		QueryRunner qr = new QueryRunner(_dataSource);
		return qr.query(sql, new ScalarHandler<T>(1), params);
	}

	public static <T> T ExecuteScalar(Connection conn, String sql) throws SQLException {
		return ExecuteScalar(conn, sql, (Object[]) null);
	}

	public static <T> T ExecuteScalar(Connection conn, String sql, Object... params) throws SQLException {
		QueryRunner qr = new QueryRunner();
		return qr.query(conn, sql, new ScalarHandler<T>(1), params);
	}

	// public static <T> T ExecuteScalar(String procedure, ProcParameter...
	// params)
	// throws SQLException {
	// Connection conn = GetConnection();
	// T result = null;
	// try {
	// result = ExecuteScalar(conn, procedure, params);
	// } finally {
	// if (conn != null)
	// try {
	// conn.close();
	// } catch (Exception ignore) {
	// }
	// }
	// return result;
	// }
	//
	// @SuppressWarnings("unchecked")
	// public static <T> T ExecuteScalar(Connection conn, String procedure,
	// ProcParameter... params)
	// throws SQLException {
	// T result = null;
	//
	// ExecuteProcOrFunc(conn, procedure, null, params);
	// for (int i = 0; i < params.length; i++) {
	// if ((params[i].Type == ProcPrameterType.Out || params[i].Type ==
	// ProcPrameterType.InOut)
	// && params[i].OutResultSetHandlerType == ResultSetHandlerType.MapList) {
	// result = (T) params[i].Value;
	// break;
	// }
	// }
	// return result;
	// }

	public static String ExecuteJson(String sql) throws SQLException {
		return ExecuteJson(sql, (Object[]) null);
	}

	public static String ExecuteJson(String sql, Object... params) throws SQLException {
		QueryRunner qr = new QueryRunner(_dataSource);
		return qr.query(sql, new JsonHandler(), params);
	}

	public static String ExecuteJson(Connection conn, String sql) throws SQLException {
		return ExecuteJson(conn, sql, (Object[]) null);
	}

	public static String ExecuteJson(Connection conn, String sql, Object... params) throws SQLException {
		QueryRunner qr = new QueryRunner();
		return qr.query(conn, sql, new JsonHandler(), params);
	}

	public static String ExecuteJson(String procedure, ProcParameter... params) throws SQLException {
		Connection conn = GetConnection();
		String result = null;
		try {
			result = ExecuteJson(conn, procedure, params);
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (Exception ignore) {
				}
		}
		return result;
	}

	public static String ExecuteJson(Connection conn, String procedure, ProcParameter... params) throws SQLException {
		String result = null;

		ExecuteProcOrFunc(conn, procedure, null, params);
		for (ProcParameter param : params) {
			if ((param.Type == ProcPrameterType.Out || param.Type == ProcPrameterType.InOut)
					&& param.OutResultSetHandlerType == ResultSetHandlerType.Json) {
				result = (String) param.Value;
				break;
			}
		}
		return result;
	}

	public static String ExecuteModuleJson(String sql, String tableName) throws SQLException {
		return ExecuteModuleJson(sql, tableName, (Object[]) null);
	}

	public static String ExecuteModuleJson(String sql, String tableName, Object... params) throws SQLException {
		QueryRunner qr = new QueryRunner(_dataSource);
		return qr.query(sql, new ModuleJsonHandler(tableName), params);
	}

	public static String ExecuteModuleJson(Connection conn, String sql, String tableName) throws SQLException {
		return ExecuteModuleJson(conn, sql, tableName, (Object[]) null);
	}

	public static String ExecuteModuleJson(Connection conn, String sql, String tableName, Object... params)
			throws SQLException {
		QueryRunner qr = new QueryRunner();
		return qr.query(conn, sql, new ModuleJsonHandler(tableName), params);
	}

	public static String ExecuteModuleJson(String procedure, String tableName, ProcParameter... params)
			throws SQLException {
		Connection conn = GetConnection();
		String result = null;
		try {
			result = ExecuteModuleJson(conn, procedure, tableName, params);
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (Exception ignore) {
				}
		}
		return result;
	}

	public static String ExecuteModuleJson(Connection conn, String procedure, String tableName, ProcParameter... params)
			throws SQLException {
		String result = null;

		ExecuteProcOrFunc(conn, procedure, null, params);
		for (int i = 0; i < params.length; i++) {
			if ((params[i].Type == ProcPrameterType.Out || params[i].Type == ProcPrameterType.InOut)
					&& params[i].OutResultSetHandlerType == ResultSetHandlerType.ModuleJson) {
				result = (String) params[i].Value;
				break;
			}
		}
		return result;
	}

	public static int ExecuteNonQuery(String sql) throws SQLException {
		return ExecuteNonQuery(sql, (Object[]) null);
	}

	public static int ExecuteNonQuery(String sql, Object... params) throws SQLException {
		QueryRunner qr = new QueryRunner(_dataSource);
		if (params != null && params.length > 0) {
			return qr.update(sql, params);
		} else {
			return qr.update(sql);
		}
	}

	public static int ExecuteNonQuery(Connection conn, String sql) throws SQLException {
		return ExecuteNonQuery(conn, sql, (Object[]) null);
	}

	public static int ExecuteNonQuery(Connection conn, String sql, Object... params) throws SQLException {
		QueryRunner qr = new QueryRunner();
		if (params != null && params.length > 0) {
			return qr.update(conn, sql, params);
		} else {
			return qr.update(conn, sql);
		}
	}

	public static int ExecuteNonQuery(String procedure, ProcParameter... params) throws SQLException {
		Connection conn = GetConnection();
		int result = -1;
		try {
			result = ExecuteNonQuery(conn, procedure, params);
		} finally {
			if (conn != null)
				try {
					conn.close();
				} catch (Exception ignore) {
				}
		}
		return result;
	}

	public static int ExecuteNonQuery(Connection conn, String procedure, ProcParameter... params) throws SQLException {
		ExecuteProcOrFunc(conn, procedure, null, params);
		return 0;
	}

	public static Connection GetConnection() throws SQLException {
		return _dataSource.getConnection();
	}

	public static Connection BeginTransaction() throws SQLException {
		Connection conn = GetConnection();
		conn.setAutoCommit(false);
		return conn;
	}

	public static void CommitTransaction(Connection conn) throws SQLException {
		try {
			conn.commit();
		} finally {
			if (conn != null)
				try {
					conn.setAutoCommit(true);
					conn.close();
				} catch (Exception ignore) {
					ignore.printStackTrace();
				}
		}
	}

	public static void RollbackTransaction(Connection conn) throws SQLException {
		try {
			conn.rollback();
		} finally {
			if (conn != null)
				try {
					conn.setAutoCommit(true);
					conn.close();
				} catch (Exception ignore) {
					ignore.printStackTrace();
				}
		}
	}

//	public static void main(String[] args) throws SQLException {
//		String res= SqlHelper.ExecuteJson("select * from xt_pz");
//		System.out.println(res);
//	}
}
