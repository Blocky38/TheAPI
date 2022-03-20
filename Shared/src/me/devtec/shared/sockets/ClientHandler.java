package me.devtec.shared.sockets;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;

import me.devtec.shared.dataholder.Config;
import me.devtec.shared.dataholder.DataType;

public class ClientHandler extends Thread {
	protected final BufferedReader dis; 
	protected final PrintWriter dos; 
	protected final Socket s;
    protected String name;
    private boolean access;
    private boolean accessFull;
    protected final Server ser;
	protected ServerClient c;
	protected final Config data = new Config();
	protected boolean closed;
    
    private LinkedList<String> postQueue;
    
    public ClientHandler(Server server, Socket s, BufferedReader dis2, PrintWriter dos2) {
        this.s = s;
        ser=server;
        this.dis = dis2; 
        this.dos = dos2;
    }
    
    public void exit() {
    	closed=true;
    	ser.sockets.remove(s);
    	data.clear();
    	try {
    		dos.println("exit");
    		dos.flush(); 
    		dos.close();
    		dis.close();
		} catch (Exception e) {
		}
    	try {
			s.close();
    	}catch(Exception e) {
    	}
    	interrupt();
    }
    
    public void write(String path, Object object) {
    	data.set(path, object);
    }
    
    public void send() {
    	if(accessFull) {
	    	try {
	    		dos.println(data.toString(DataType.BYTE));
	    		dos.flush();
			} catch (Exception e) {}
		}else {
    		if(postQueue==null)
    			postQueue = new LinkedList<>();
    		postQueue.add(data.toString(DataType.BYTE));
		}
		data.clear();
	}
    
    public boolean isConnected() {
    	return !closed;
    }
  
    @Override
    public void run() {
		while(isConnected()) {
			try {
			String read = dis.readLine();
			if(read==null)continue;
        	if(read.startsWith("login:")) {
        		if(!access) {
        		access=true;
            	name=read.substring(6);
        		}
        		continue;
        	}
        	if(read.startsWith("password:")) {
        		if(access && !accessFull && ser.pas.equals(read.substring(9)))
        			accessFull=true;
	    		dos.println("logged");
        		dos.flush();
		    	if(postQueue!=null && !postQueue.isEmpty()) {
			    	for(String s : postQueue)
			    		dos.println(s);
			    	dos.flush();
			    	postQueue.clear();
		    	}
        		continue;
        	}
			if(read.equals("exit")) {
		    	closed=true;
				break;
			}
        	if(access && accessFull) {
        		Config data = new Config();
        		data.reload(read);
        		ser.read(c, data);
        	}
			}catch(Exception e) {
				break;
			}
		}
        exit();
    }
    
    public String getUser() {
    	return name;
    }
}