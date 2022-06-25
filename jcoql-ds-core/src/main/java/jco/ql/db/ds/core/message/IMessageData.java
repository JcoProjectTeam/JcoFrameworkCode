package jco.ql.db.ds.core.message;

import java.util.Map;

public interface IMessageData {

	long getCode();
	
	Map<String, Object> getParams();
	
	Map<String, Object> getBody();
}
