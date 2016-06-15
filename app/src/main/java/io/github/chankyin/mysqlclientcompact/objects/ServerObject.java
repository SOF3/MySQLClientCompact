package io.github.chankyin.mysqlclientcompact.objects;

import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.JsonWriter;
import android.util.Log;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Builder;

import java.io.IOException;
import java.io.StringWriter;

@Builder
@Getter
public class ServerObject{
	private String serverName;
	private String address;
	private int port = 3306;
	@Nullable @Setter private String username;
	@Nullable @Setter private String password;
	private boolean isPasswordSet = true;
	private Options options = Options.builder().build();

	public String getHostname(){
		return address;
	}

	@Builder
	@Getter
	public static class Options{
		@Nullable @Setter private String defaultSchema;

		public String getDefaultSchema(String defaultValue){
			return defaultSchema == null ? defaultValue : defaultSchema;
		}

		public static Options fromJson(JsonReader reader) throws IOException{
			OptionsBuilder builder = builder();
			reader.beginObject();
			while(reader.hasNext()){
				switch(reader.nextName()){
					case "defaultSchema":
						if(reader.peek() == JsonToken.NULL){
							reader.nextNull();
						}else{
							builder.defaultSchema(reader.nextString());
						}
						break;
				}
			}
			reader.endObject();
			return builder.build();
		}

		public String toJson(){
			try{
				StringWriter out = new StringWriter();
				JsonWriter writer = new JsonWriter(out);
				toJson(writer);
				return out.toString();
			}catch(IOException e){
				Log.e("ServerObject", "Error encoding server to JSON", e);
				return null;
			}
		}

		public void toJson(JsonWriter writer) throws IOException{
			writer.beginObject();
			writer.name("defaultSchema").value(defaultSchema);
			writer.endObject();
		}

	}

	public static ServerObject fromUri(Uri uri, String username, String password, String serverName, Options options){
		String ssp = uri.getSchemeSpecificPart();
		String prefix = "mysql://";
		if(ssp.startsWith(prefix)){
			ssp = ssp.substring(prefix.length());
		}
		int j = ssp.indexOf('/');
		if(j == -1){
			j = ssp.length();
		}
		int i = ssp.indexOf(':');
		if(i == -1 || i > j){
			i = j;
		}
		String hostname = ssp.substring(0, i);
		String portName = i == j ? "" : ssp.substring(i + 1, j);
		int port = 3306;
		if(!portName.isEmpty()){
			try{
				port = Integer.parseInt(portName);
			}catch(NumberFormatException e){
				throw new IllegalArgumentException(e);
			}
		}
		String schema = j == ssp.length() ? null : ssp.substring(j + 1);
		options.setDefaultSchema(schema);

		return builder()
				.serverName(serverName == null || serverName.isEmpty() ? hostname : serverName)
				.address(hostname)
				.port(port)
				.username(username)
				.password(password)
				.options(options)
				.build();
	}

	public static ServerObject fromJson(JsonReader reader) throws IOException{
		ServerObjectBuilder builder = builder();
		reader.beginObject();
		while(reader.hasNext()){
			switch(reader.nextName()){
				case "serverName":
					builder.serverName(reader.nextString());
					break;
				case "address":
					builder.address(reader.nextString());
					// check validity upon insertion, not loading
					break;
				case "port":
					builder.port(reader.nextInt());
					break;
				case "username":
					builder.username(reader.nextString());
					break;
				case "password":
					builder.password(reader.nextString());
					break;
				case "isPasswordSet":
					builder.isPasswordSet(reader.nextBoolean());
					break;
				case "options":
					builder.options(Options.fromJson(reader));
					break;
			}
		}
		reader.endObject();
		return builder.build();
	}

	public String toJson(){
		try{
			StringWriter out = new StringWriter();
			JsonWriter writer = new JsonWriter(out);
			toJson(writer);
			return out.toString();
		}catch(IOException e){
			Log.e("ServerObject", "Error encoding server to JSON", e);
			return null;
		}
	}

	public void toJson(JsonWriter writer) throws IOException{
		writer.beginObject();
		writer
				.name("serverName").value(serverName)
				.name("address").value(address)
				.name("port").value(port)
				.name("username").value(username)
				.name("password").value(password)
				.name("isPasswordSet").value(isPasswordSet);
		writer.name("options");
		options.toJson(writer);
		writer.endObject();
	}
}
