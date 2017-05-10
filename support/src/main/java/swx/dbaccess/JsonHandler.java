package swx.dbaccess;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import org.apache.commons.dbutils.BaseResultSetHandler;

import java.sql.Clob;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;

class JsonHandler extends BaseResultSetHandler<String> {

	@Override
	protected String handle() throws SQLException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		Boolean flag = false;
		while (this.next()) {
			flag = true;
			ResultSetMetaData rsmd = this.getMetaData();
			sb.append("{");
			int j = rsmd.getColumnCount();
			for (int i = 1; i <= j; i++) {
				JsonObject json = new JsonObject();
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
						json.addProperty(rsmd.getColumnLabel(i), (Number) val);
					} else {
						json.add(rsmd.getColumnLabel(i), JsonNull.INSTANCE);
					}
					break;
				case Types.DATE:
				case Types.TIME:
				case Types.TIME_WITH_TIMEZONE:
				case Types.TIMESTAMP:
				case Types.TIMESTAMP_WITH_TIMEZONE:
					if (val != null) {
						json.addProperty(rsmd.getColumnLabel(i), sdf.format(val));
					} else {
						json.add(rsmd.getColumnLabel(i), JsonNull.INSTANCE);
					}
					break;
				case Types.BLOB:
					break;
				case Types.CLOB:
					Clob vClob = this.getClob(i);
					if (vClob != null) {						
						json.addProperty(rsmd.getColumnLabel(i), SqlHelper.ClobToString(vClob));
					} else {
						json.add(rsmd.getColumnLabel(i), JsonNull.INSTANCE);
					}
					break;
				default:
					if (val != null) {
						StringBuilder ssb = new StringBuilder();
						ssb.append(val);
						json.addProperty(rsmd.getColumnLabel(i), ssb.toString());
					} else {
						json.add(rsmd.getColumnLabel(i), JsonNull.INSTANCE);
					}
					break;
				}
				String tmp = json.toString().substring(1);
				sb.append(tmp.substring(0, tmp.lastIndexOf("}")));
				if (i + 1 <= j) {
					sb.append(",");
				}
			}
			sb.append("},");
		}
		this.close();
		if (flag) {
			return sb.substring(0, sb.lastIndexOf(",")) + "]";
		} else {
			return sb.append("]").toString();
		}
	}

}
