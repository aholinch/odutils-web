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

import java.io.File;

import net.freeutils.httpserver.HTTPServer;
import net.freeutils.httpserver.HTTPServer.FileContextHandler;
import net.freeutils.httpserver.HTTPServer.VirtualHost;

public class ODUtilsServer 
{

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
			server.start();
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}

}
