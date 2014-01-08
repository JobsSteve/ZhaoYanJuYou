/*
Copyright (c) 2011, Marcos Diez --  marcos AT unitron.com.br
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.   
 * Neither the name of  Marcos Diez nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.zhaoyan.common.net.http;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.net.Uri;
import android.util.Log;

/**
 * 
 * Title: A simple Webserver Tutorial NO warranty, NO guarantee, MAY DO damage
 * to FILES, SOFTWARE, HARDWARE!! Description: This is a simple tutorial on
 * making a webserver posted on http://turtlemeat.com . Go there to read the
 * tutorial! This program and sourcecode is free for all, and you can copy and
 * modify it as you like, but you should give credit and maybe a link to
 * turtlemeat.com, you know R-E-S-P-E-C-T. You gotta respect the work that has
 * been put down.
 * 
 * Copyright: Copyright (c) 2002 Company: TurtleMeat
 * 
 * @author: Jon Berg <jon.berg[on_server]turtlemeat.com
 * @version 1.0
 */

// file: server.java
// the real (http) serverclass
// it extends thread so the server is run in a different
// thread than the gui, that is to make it responsive.
// it's really just a macho coding thing.
public class MyHttpServer extends Thread {
	private int mPort;
	private ArrayList<Uri> mFileUris;
	private final ExecutorService mThreadPool = Executors.newCachedThreadPool();

	private ServerSocket mServersocket = null;
	private boolean mWebserverLoop = true;

	// default port is 80
	public MyHttpServer(int listen_port) {
		mPort = listen_port;
	}

	public void startServer() {
		if (mServersocket == null) {
			start();
		}
	}

	public synchronized void stopServer() {
		s("Closing server...\n\n");
		mWebserverLoop = false;
		if (mServersocket != null) {
			try {
				mServersocket.close();
				mServersocket = null;
			} catch (IOException e) {
				s("stopServer error." + e);
			}
		}
	}

	public void setFile(Uri fileUri) {
		ArrayList<Uri> uris = new ArrayList<Uri>();
		uris.add(fileUri);
		setFiles(uris);
	}

	public void setFiles(ArrayList<Uri> fileUris) {
		mFileUris = fileUris;
	}

	public ArrayList<Uri> GetFiles() {
		return mFileUris;
	}

	private boolean normalBind(int thePort) {
		s("Attempting to bind on port " + thePort);
		try {
			mServersocket = new ServerSocket(thePort);
		} catch (Exception e) {
			s("Fatal Error:" + e.getMessage() + " " + e.getClass().toString());
			return false;
		}
		mPort = thePort;
		s("Binding was OK!");
		return true;
	}

	public void run() {
		if (!normalBind(mPort)) {
			return;
		}

		// go in a infinite loop, wait for connections, process request, send
		// response
		while (mWebserverLoop) {
			s("Ready, Waiting for requests...\n");
			try {
				Socket connectionsocket = mServersocket.accept();
				HttpServerConnection theHttpConnection = new HttpServerConnection(
						mFileUris, connectionsocket);

				mThreadPool.submit(theHttpConnection);
			} catch (IOException e) {
				s("Start HttpServerConnection error." + e);
			}
		}
	}

	// an alias to avoid typing so much!
	private void s(String s2) {
		Log.d(Util.LOG_TAG, s2);
	}

}
