package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
        	String url="/?data=234";
        	BufferedReader br = new BufferedReader (new InputStreamReader(in, "UTF-8"));
        	String line = br.readLine();
        	log.debug("Request line : {}", line);
        	if (line == null) {
        		return;
        	}
        	String[] tokens = line.split(" ");
        	while(!"".equals(line)) {
        		line = br.readLine();
        		log.debug("Request line : {}", line);
        	}
        	//메모리 주소가 다른데 equals는 값을 비교할 때, ==은 메모리주소는 같은데 값이 같은지 비        	        	
        	int index = url.indexOf("?"); // ?가 문자열의 몇번째 위치인지 숫자값이 나온다. 없으면 -1 나온다.
        	String path;
        	DataOutputStream dos = new DataOutputStream(out);
        	
        	if (index == -1) {
        		response(dos, url);
        		
        	} else {
        		path = url.substring(0, index); // index 까지 
        		String queryString = url.substring(index+1);
        		if (path.equals("/user/create")) {
        	       	Map<String, String> params = HttpRequestUtils.parseQueryString(queryString);
        	        User user = new User(params.get("userId"), params.get("password"), params.get("name"), params.get("email"));
        	        log.debug("user : {}", user);
        	        DataBase.addUser(user);
        	        response(dos, "/index.html");
        		}

        	}
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    
    private void response(DataOutputStream dos, String filePath) throws IOException {
    	byte[] body = Files.readAllBytes(new File("./webapp" + filePath).toPath());
    	response200Header(dos, body.length);
    	responseBody(dos, body);
    }
    

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
