package swx.dbaccess;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;

public class ProcParameter {
	public String Name;
	public Object Value;
	public ProcPrameterType Type;
	public int DataType;
	public ResultSetHandlerType OutResultSetHandlerType;
	public Class<?> OutBeanType;

	public ResultSetHandler<?> getOutResultHandler() {
		switch (OutResultSetHandlerType) {
		case BeanList:
			return new BeanListHandler<>(OutBeanType);
		case MapList:
			return new MapListHandler();
		case Json:
			return new JsonHandler();
		case ModuleJson:
			return new ModuleJsonHandler(Value == null ? "" : Value.toString());
		default:
			return null;
		}
	}
}
