package swx.dbaccess;

import java.sql.Clob;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;

import org.apache.commons.dbutils.BaseResultSetHandler;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import swx.dbaccess.SqlHelper;

class ModuleJsonHandler extends BaseResultSetHandler<String> {

	private String _tableName;

	public ModuleJsonHandler(String tableName) {
		this._tableName = tableName;
	}

	@Override
	protected String handle() throws SQLException {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		StringBuilder sb = new StringBuilder();
		sb.append("[{\"TableName\":\"" + this._tableName + "\",\"Model\":[");
		Boolean flag = false;
		while (this.next()) {
			flag = true;
			ResultSetMetaData rsmd = this.getMetaData();
			sb.append("{\"Operation\":0,\"Data\":[");
			int j = rsmd.getColumnCount();
			for (int i = 1; i <= j; i++) {
				JsonObject keyJson = new JsonObject();
				keyJson.addProperty("Key", rsmd.getColumnLabel(i));
				
				JsonObject valueJson = new JsonObject();
				
//				sb.append("{\"Key\":\"");
//				sb.append(rsmd.getColumnLabel(i));
//				sb.append("\",\"Value\":");

				Object val = this.getObject(i);
				switch (rsmd.getColumnType(i)) {
				case Types.BIGINT:
				case Types.BIT:
				case Types.DECIMAL:
				case Types.DOUBLE:
				case Types.FLOAT:
				case Types.INTEGER:
				case Types.NUMERIC:
				case Types.TINYINT:
					if (val != null) {
						valueJson.addProperty("Value", (Number) val);
					} else {
						valueJson.add("Value", JsonNull.INSTANCE);
					}
					/*if (val != null) {
						sb.append(val);
					} else {
						sb.append("null");
					}*/
					break;
				case Types.DATE:
				case Types.TIME:
				case Types.TIME_WITH_TIMEZONE:
				case Types.TIMESTAMP:
				case Types.TIMESTAMP_WITH_TIMEZONE:
					if (val != null) {
						valueJson.addProperty("Value", sdf.format(val));
					} else {
						valueJson.add("Value", JsonNull.INSTANCE);
					}
					/*if (val != null) {
						sb.append("\"").append(sdf.format(val)).append("\"");
					} else {
						sb.append("null");
					}*/
					break;
				case Types.BLOB:
					break;
				case Types.CLOB:
					Clob vClob = this.getClob(i);
					if (vClob != null) {						
						valueJson.addProperty("Value", SqlHelper.ClobToString(vClob));
					} else {
						valueJson.add("Value", JsonNull.INSTANCE);
					}
					break;
				default:
					if (val != null) {
						StringBuilder ssb = new StringBuilder();
						ssb.append(val);
						valueJson.addProperty("Value", ssb.toString());
					} else {
						valueJson.add("Value", JsonNull.INSTANCE);
					}
					/*if (val != null) {
						sb.append("\"").append(val).append("\"");
					} else {
						sb.append("null");
					}*/
					break;
				}
				sb.append(keyJson.toString().substring(0, keyJson.toString().lastIndexOf("}"))).append(",").append(valueJson.toString().substring(1));
				if (i + 1 <= j) {
					//sb.append("},");
					sb.append(",");
				} else {
					//sb.append("}");
				}
			}
			sb.append("]},");
		}
		this.close();
		if (flag) {
			return sb.substring(0, sb.lastIndexOf(",")) + "]}]";
		} else {
			return sb.append("]}]").toString();
		}
	}

}
