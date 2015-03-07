package com.ideanest.vegos;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ideanest.vegos.*;
import com.ideanest.vegos.gtp.GTPClient;

/**
 * 
 * @author Piotr Kaminski
 */
public class ClientProcess extends Process {
	
	private final InputStream p_out, p_err, c_in;
	private final OutputStream c_out, c_err, p_in;
	private final GTPClient client;
	
	protected static final Logger log = Logger.getLogger(ClientProcess.class.getName());
	
	public ClientProcess() throws IOException {
		c_in = new PipedInputStream();
		p_in = new PipedOutputStream((PipedInputStream) c_in);
		c_out = new PipedOutputStream();
		p_out = new PipedInputStream((PipedOutputStream) c_out);
		c_err = new PipedOutputStream();
		p_err = new PipedInputStream((PipedOutputStream) c_err);
		System.setErr(new PrintStream(c_err));
		client = new GTPClient(new InputStreamReader(c_in), new OutputStreamWriter(c_out));
		client.registerController(new Engine());
		client.start();
	}

	public OutputStream getOutputStream() {return p_in;}

	public InputStream getInputStream() {return p_out;}

	public InputStream getErrorStream() {return p_err;}

	public int waitFor() throws InterruptedException {
		client.join();
		return 0;
	}

	public int exitValue() {
		return 0;
	}

	public void destroy() {
		try {
			client.stop();
		} catch (IllegalStateException e) {
			log.log(Level.WARNING, "attempted to destroy dead client process", e);
		}
	}

}
