/* 

Copyright 2021 aholinch

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
    
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

*/
package odutils.web;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;

import net.freeutils.httpserver.HTTPServer;
import net.freeutils.httpserver.HTTPServer.FileContextHandler;
import net.freeutils.httpserver.HTTPServer.VirtualHost;

public class ODUtilsServer 
{
	/**
	 * Method to open the browser to localhost page
	 */
	public static void spawnBrowserOpen()
	{
		Runnable r = new Runnable() {
			public void run()
			{
				//System.out.println("Attempting to open browser");
				try {
					Thread.sleep(1100);
					Desktop.getDesktop().browse(new URI("http://localhost:9000"));
				} catch(Exception ex) {};
				
			}
		};
		
		Thread t = new Thread(r);
		t.start();
	}

	public static void main(String[] args) 
	{
		try
		{
			int port = 9000;
			HTTPServer server = new HTTPServer(port);
			VirtualHost host = server.getVirtualHost(null);  // default virtual host
			
			//host.addContext("/rest", new RESTHandler());
			host.addContext("/rest", new RESTHandler(), "GET","POST");
			host.addContext("/", new FileContextHandler(new File("web")));
			
			if(args != null && args.length>1)
			{
				if(args[0].toLowerCase().equals("browser") && args[1].toLowerCase().equals("true"))
				{
					spawnBrowserOpen();
				}
			}
			
			server.start();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

}
