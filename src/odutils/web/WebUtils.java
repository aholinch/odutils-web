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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.freeutils.httpserver.HTTPServer.Request;
import odutils.util.DateUtil;

public class WebUtils 
{
	private static final Logger logger = Logger.getLogger(WebUtils.class.getName());
	
	public static String getReqBodyAsString(Request req)
	{
		String str = null;
		
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		
		try
		{
			is = req.getBody();
			isr = new InputStreamReader(is,"UTF-8");
			br = new BufferedReader(isr);
			
			int size = 100000;
			
			if(req.getHeaders().contains("Content-Length"))
			{
				try
				{
					size = Integer.parseInt(req.getHeaders().get("Content-Length"));
				}
				catch(Exception hex)
				{
					logger.log(Level.WARNING,"Error parsing content length",hex);
				}
			}
			
			StringBuffer sb = new StringBuffer(size);
			String line = null;
			
			line = br.readLine();
			while(line != null)
			{
				sb.append(line).append("\n");
				line = br.readLine();
			}
			
			str = sb.toString();
			str = URLDecoder.decode(str,"UTF-8");
			
		}
		catch(Exception ex)
		{
			logger.log(Level.WARNING,"Error parsing request body",ex);
		}
		finally
		{
			if(is != null)try {is.close();}catch(Exception ex) {};
			if(isr != null)try {isr.close();}catch(Exception ex) {};
			if(br != null)try {br.close();}catch(Exception ex) {};
		}
		
		return str;
	}
	
	public static Date getDate(String str)
	{
		return DateUtil.getDate(str);
	}
	
	public static int gi(String str)
	{
		int num = 0;
		try {num = Integer.parseInt(str.trim());}catch(Exception ex) {};
		return num;
	}
	
    public static Date getDate(Request req, String param)
    {
    	Date d = null;
    	try
    	{
    		String str = req.getParams().get(param);
    		d = getDate(str);
    	}
    	catch(Exception ex)
    	{
    		// don't care
    	}
    	
    	return d;
    }
	
	public static double getDouble(String str, double defaultVal)
	{
		double num = defaultVal;
		try {num = Double.parseDouble(str.trim());}catch(Exception ex) {};
		return num;
	}
	
    public static double getDouble(Request req, String param, double defaultVal)
    {
    	double num = defaultVal;
    	try {num = Double.parseDouble(req.getParams().get(param).trim());}catch(Exception ex) {};
    	return num;
    }
}
