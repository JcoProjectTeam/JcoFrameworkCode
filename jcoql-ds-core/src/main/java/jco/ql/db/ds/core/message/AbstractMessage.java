package jco.ql.db.ds.core.message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractMessage<T extends IMessageData> implements IMessage, IMessageData {
	
	protected final long code;
	protected Map<String, Object> params;
	protected Map<String, Object> body;
	
	@JsonIgnore
	private final ObjectMapper messageMapper;
	
	public AbstractMessage(long commandCode) {
		this.code = commandCode;
		this.messageMapper = new ObjectMapper();
	}

	@Override
	public long getCode() {
		return code;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public IMessageData getMessageData() {
		return (T) this;
	}
	
	@Override
	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}
	
	public void addParam(String name, Object value) {
		if(this.params == null) {
			this.params = new HashMap<>();
		}
		this.params.put(name, value);
	}

	@Override
	public Map<String, Object> getBody() {
		return body;
	}

	public void setBody(Map<String, Object> body) {
		this.body = body;
	}

	public void addBodyParam(String name, Object value) {
		if(this.body == null) {
			this.body = new HashMap<>();
		}
		this.body.put(name, value);
	}

	@Override
	public void sendMessage(IMessageData message, OutputStream os) throws IOException {
		byte[] messageData = null;
		if(message != null) {
			messageData = encodeMessageData(message);
		}
		
		if(messageData != null) {
			os.write(messageData);
			os.flush();
		}
	}
	
	@Override
	public IMessageData decodeMessageData(byte[] data) throws IOException {
		return decodeMessageData(code, data);
	}
	
	public IMessageData decodeMessageData(long messageCode, byte[] data) throws IOException {
		SimpleMessage message = new SimpleMessage(messageCode);
		if(data != null && data.length > 0) {
			ByteBuffer buffer = ByteBuffer.wrap(data);
			int paramsSize = buffer.getInt();
			int bodySize = buffer.getInt();
			if(paramsSize > 0) {
				byte[] paramsBuf = new byte[paramsSize];
	 			buffer.get(paramsBuf);
	 			message.setParams(messageMapper.readValue(paramsBuf, new TypeReference<Map<String, Object>>() {}));
			}
			if(bodySize > 0) {
				byte[] bodyBuf = new byte[bodySize];
				buffer.get(bodyBuf);
				Map<String, Object> jsonBody = null;
//				if(bodySize > 1024*1024*2) { //If more than 2MB
					JsonParser parser = messageMapper.tokenStreamFactory().createParser(bodyBuf);
					if(parser.nextToken() != null) {
						jsonBody = parseObject(parser);
					}
					parser.close();
//				} else {
//					jsonBody = messageMapper.readValue(bodyBuf, new TypeReference<Map<String, Object>>() {});
//				}
				message.setBody(jsonBody);
			}
		}
		
		return message;
	}

	@Override
	public IMessageData decodeMessageData(int paramsSize, int bodySize, byte[] data) throws IOException {
		return decodeMessageData(code, paramsSize, bodySize, data);
	}
	
	public IMessageData decodeMessageData(long messageCode, int paramsSize, int bodySize, byte[] data) throws IOException {
		SimpleMessage message = new SimpleMessage(messageCode);
		if(data != null && data.length > 0) {
			ByteBuffer buffer = ByteBuffer.wrap(data);
			if(paramsSize > 0) {
				byte[] paramsBuf = new byte[paramsSize];
	 			buffer.get(paramsBuf);
	 			message.setParams(messageMapper.readValue(paramsBuf, new TypeReference<Map<String, Object>>() {}));
			}
			if(bodySize > 0) {
				byte[] bodyBuf = new byte[bodySize];
				buffer.get(bodyBuf);
				Map<String, Object> jsonBody = null;
//				if(bodySize > 1024*1024*2) { //If more than 2MB
					JsonParser parser = messageMapper.tokenStreamFactory().createParser(bodyBuf);
					if(parser.nextToken() != null) {
						jsonBody = parseObject(parser);
					}
					parser.close();
//				} else {
//					jsonBody = messageMapper.readValue(bodyBuf, new TypeReference<Map<String, Object>>() {});
//				}
				message.setBody(jsonBody);
			}
		}
		
		return message;
	}

	protected byte[] encodeMessageData(IMessageData data) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		writeLong(baos, data.getCode());

		int paramsSize = 0;
		int bodySize = 0;
		byte[] paramsBuf = null;
		byte[] bodyBuf = null;
		
		if(data.getParams() != null) {
			paramsBuf = messageMapper.writeValueAsBytes(data.getParams());
			paramsSize = paramsBuf.length;
		}
		writeInt(baos, paramsSize);

		if(data.getBody() != null) {
			bodyBuf = messageMapper.writeValueAsBytes(data.getBody());
			bodySize = bodyBuf.length;
		}
		
		writeInt(baos, bodySize);
		if(paramsSize > 0) {
			baos.write(paramsBuf);
		}
		if(bodySize > 0) {
			baos.write(bodyBuf);
		}

		return baos.toByteArray();
	}

	private void writeLong(ByteArrayOutputStream baos, long value) {
		for(int i = Long.BYTES - 1; i >= 0; i--) {
			byte b = (byte) (value >>> (i * 8));
			baos.write(b);
		}
	}

	private void writeInt(ByteArrayOutputStream baos, int value) {
		for(int i = Integer.BYTES - 1; i >= 0; i--) {
			baos.write((byte) (value >>> (i * 8)));
		}
	}

	protected Object parseValue(JsonToken currentToken, JsonParser parser) throws IOException {
		Object value = null;
		if(currentToken == JsonToken.START_ARRAY) {
			value = parseArray(parser);
		} else if(currentToken == JsonToken.START_OBJECT) {
			value = parseObject(parser);
		} else if(currentToken == JsonToken.VALUE_FALSE) {
			value = false;
		} else if(currentToken == JsonToken.VALUE_NUMBER_FLOAT) {
			value = parser.getValueAsDouble();
		} else if(currentToken == JsonToken.VALUE_NUMBER_INT) {
			value = parser.getIntValue();
		} else if(currentToken == JsonToken.VALUE_STRING) {
			value = parser.getValueAsString();
		} else if(currentToken == JsonToken.VALUE_TRUE) {
			value = true;
		}
		
		return value;
	}

	protected List<Object> parseArray(JsonParser parser) throws IOException {
		List<Object> values = new LinkedList<Object>();
		while(parser.nextToken() != JsonToken.END_ARRAY) {
			JsonToken currentToken = parser.currentToken();
			
			if(currentToken == JsonToken.START_ARRAY) {
				values.add(parseArray(parser));
			} else if(currentToken == JsonToken.START_OBJECT) {
				values.add(parseObject(parser));
			} else {
				values.add(parseValue(currentToken, parser));
			}
		}
		return values;
	}

	protected Map<String, Object> parseObject(JsonParser parser) throws IOException {
		Map<String, Object> object = new HashMap<String, Object>();
		
		while(parser.nextToken() != JsonToken.END_OBJECT) {
			JsonToken currentToken = parser.getCurrentToken();
			String fieldName = null;
			Object value = null;
	
			if(currentToken == JsonToken.FIELD_NAME) {
				fieldName = parser.getValueAsString();
				value = parseValue(parser.nextToken(), parser);
			} else {
				value = parseValue(currentToken, parser);
			}
	
			object.put(fieldName, value);
		}
		return object;
	}

}
