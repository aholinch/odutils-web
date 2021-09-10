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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.freeutils.httpserver.HTTPServer.ContextHandler;
import net.freeutils.httpserver.HTTPServer.Request;
import net.freeutils.httpserver.HTTPServer.Response;
import odutils.ephem.CartesianState;
import odutils.ephem.USSFSGP4;
import odutils.util.DateUtil;
import odutils.ephem.od.CartToTLE;
import odutils.ephem.od.ODTask;
import odutils.ephem.od.ODTaskRunner;
import odutils.ephem.od.ObservationSet;
import odutils.ephem.od.OrbitState;
import odutils.ephem.od.SGP4ODTask;
import odutils.ephem.od.SGP4XPODTask;
import sgp4.TLE;

public class RESTHandler implements ContextHandler 
{
	private static final Logger logger = Logger.getLogger(RESTHandler.class.getName());
	
    public static final String INITPATH = "/rest";
    
	@Override
	public int serve(Request req, Response resp) throws IOException 
	{
		// usually in a new Thread so reset the context
		DateUtil.setDefaultTimeZone();
		
		int status = 0;
		
		String path = req.getPath();
		if(path.startsWith(INITPATH))
		{
			path = path.substring(INITPATH.length());
		}
		
		path = path.toLowerCase();
		if(path.contains("cart2tle"))
		{
			status = doCart2TLE(req,resp);
		}
		else if(path.contains("tle2cart"))
		{
			status = doTLE2Cart(req,resp);			
		}
		else if(path.contains("sgp4odres") || path.contains("sgp4xpodres"))
		{
			status = doSGP4ODResult(req,resp);			
		}
		else if(path.contains("sgp4od"))
		{
			status = doSGP4OD(req,resp);			
		}
		else if(path.contains("sgp4xpod"))
		{
			status = doSGP4XPOD(req,resp);			
		}
		else
		{
			resp.send(404, "Unknown rest method " + path);
		}
		
        return status;
    }
	
	public int doCart2TLE(Request req, Response resp) throws IOException
	{
		CartesianState cart = parseVector(req);
		System.out.println(cart);
		if(cart == null)
		{
			resp.send(500, "Error parsing cart");
			return -1;
		}
		
		TLE tle1 = CartToTLE.cartToTLE(cart, "99999", false);
		TLE tle2 = CartToTLE.cartToTLE(cart, "99999", true);
		TLE tle3 = CartToTLE.cartToXPTLE(cart, "99999");

		String response = "0 Open Source SGP4\r\n"+tle1.getLine1()+"\r\n"+tle1.getLine2();
		response += "\r\n"+"0 USSF SGP4\r\n"+tle2.getLine1()+"\r\n"+tle2.getLine2();
		response += "\r\n"+"0 USSF SGP4-XP\r\n"+tle3.getLine1()+"\r\n"+tle3.getLine2();
		
		resp.getHeaders().add("Content-Type", "text");
		resp.send(200, response);
		return 0;
	}

	public int doTLE2Cart(Request req, Response resp) throws IOException
	{
		String line1 = req.getParams().get("line1");
		String line2 = req.getParams().get("line2");
		
		TLE tle = new TLE(line1,line2);
		System.out.println(tle.getObjectID() + "\t" + tle.getEpoch() + "\t" + tle.getElType());
		Date start = WebUtils.getDate(req, "start");
		Date stop = WebUtils.getDate(req, "stop");
		double stepSec = WebUtils.getDouble(req, "stepsec", 60);
		//stepSec*=1.0000000000001;
		
		long te = tle.getEpoch().getTime();
		
		if(start == null)
		{
			start = tle.getEpoch();
		}
		if(stop == null)
		{
			stop =  new Date(start.getTime()+86400l*1000l);
		}
		
		long t1 = start.getTime();
		long t2 = stop.getTime();
		long dt = (long)(1000.0*stepSec);

		double ms2min = 1.0d/60000.0d;
		
		double mse1 = t1-te;
		mse1 *= ms2min;
		double mse2 = t2-te;
		mse2 *= ms2min;

		int size = (int)((t2-t1)/dt);
		if(size < 10) size = 10;
		List<Double> mses = new ArrayList<Double>(size);

		double mse = mse1;
		
		System.out.println(start + "\t" + stop + "\t" + mse + "\t" + stepSec);
		while(t1<t2)
		{
			mses.add(mse);
			t1+=dt;
			mse = t1 - te;
			mse*=ms2min;
		}
		mses.add(mse2);
		
		List<CartesianState> carts = USSFSGP4.getCarts(mses, tle);
		
		String response = null;
		
		size = carts.size();
		CartesianState cart = null;
		StringBuffer sb = new StringBuffer(size*200);
		
		for(int i=0; i<size; i++)
		{
			cart = carts.get(i);
			sb.append(cart.toString()).append("\r\n");
		}
		
		response = sb.toString();
		resp.getHeaders().add("Content-Type", "text");
		resp.send(200, response);
		return 0;
	}
	
	public int doSGP4OD(Request req, Response resp) throws IOException
	{
		String str = WebUtils.getReqBodyAsString(req);
		if(str.startsWith("carts="))str = str.substring(6).trim();
		List<CartesianState> carts = str2Carts(str);
		String ret = "Not enough carts";
		if(carts.size()>2)
		{
			ObservationSet obs = new ObservationSet();
			obs.setCarts(carts);
			
			SGP4ODTask task = new SGP4ODTask();
			task.setEpoch(carts.get(carts.size()-1).getEpoch());
			task.setObservationSet(obs);
			
			ret = ODTaskRunner.getInstance().submitTask(task);
		}
		resp.getHeaders().add("Content-Type", "text");
		resp.send(200, ret);

		return 0;
	}

	public int doSGP4ODResult(Request req, Response resp) throws IOException
	{
		String taskid = req.getParams().get("taskid");
		ODTask task = ODTaskRunner.getInstance().getTask(taskid);
		String ret = null;
		if(task != null)
		{
			OrbitState orbit = task.getSolvedState();
			
			String line0 = "0 rms="+orbit.getRMS() +" iters="+orbit.getNumIters();
			TLE tle = orbit.getTLE();
			ret = line0+"\r\n"+tle.getLine1()+"\r\n"+tle.getLine2();
		}
		else
		{
			ret = "No result yet for task " + taskid;
		}
		resp.getHeaders().add("Content-Type", "text");
		resp.send(200, ret);

		return 0;
	}

	public int doSGP4XPOD(Request req, Response resp) throws IOException
	{
		String str = WebUtils.getReqBodyAsString(req);
		if(str.startsWith("carts="))str = str.substring(6).trim();
		List<CartesianState> carts = str2Carts(str);
		String ret = "Not enough carts";
		if(carts.size()>2)
		{
			ObservationSet obs = new ObservationSet();
			obs.setCarts(carts);
			
			SGP4XPODTask task = new SGP4XPODTask();
			task.setEpoch(carts.get(carts.size()-1).getEpoch());
			task.setObservationSet(obs);
			
			ret = ODTaskRunner.getInstance().submitTask(task);
		}
		resp.getHeaders().add("Content-Type", "text");
		resp.send(200, ret);

		return 0;
	}

	public List<CartesianState> str2Carts(String str)
	{
		String lines[] = str.split("\n");
		int size =lines.length;
		List<CartesianState> carts = new ArrayList<CartesianState>(size);
		CartesianState cart = null;
		for(int i=0; i<size; i++)
		{
			cart = str2Cart(lines[i].trim());
			if(cart != null)
			{
				carts.add(cart);
			}
		}
		
		return carts;
	}
	
	public CartesianState str2Cart(String str)
	{
		CartesianState cart = null;
		String epoch = null;
		double rx = 0;
		double ry = 0;
		double rz = 0;
		double vx = 0;
		double vy = 0;
		double vz = 0;
		
		try
		{
			String sa[] = str.split(",");
			if(sa.length>6)
			{
				epoch = sa[0].trim();
				rx = WebUtils.getDouble(sa[1],0);
				ry = WebUtils.getDouble(sa[2],0);
				rz = WebUtils.getDouble(sa[3],0);
				vx = WebUtils.getDouble(sa[4],0);
				vy = WebUtils.getDouble(sa[5],0);
				vz = WebUtils.getDouble(sa[6],0);
			}
		}
		catch(Exception ex)
		{
			logger.log(Level.INFO,"Unable to parse vec",ex);
		}
		
		if(epoch != null)
		{
			cart = new CartesianState();
			cart.setEpoch(WebUtils.getDate(epoch));
			cart.rx = rx;
			cart.ry = ry;
			cart.rz = rz;
			cart.vx = vx;
			cart.vy = vy;
			cart.vz = vz;
		}
		return cart;

	}
	public CartesianState parseVector(Request req) 
	{
		CartesianState cart = null;
		String epoch = null;
		double rx = 0;
		double ry = 0;
		double rz = 0;
		double vx = 0;
		double vy = 0;
		double vz = 0;
		
		try
		{
			String vec = req.getParams().get("vec");
			if(vec != null)
			{
				String sa[] = vec.split(",");
				if(sa.length>6)
				{
					epoch = sa[0].trim();
					rx = WebUtils.getDouble(sa[1],0);
					ry = WebUtils.getDouble(sa[2],0);
					rz = WebUtils.getDouble(sa[3],0);
					vx = WebUtils.getDouble(sa[4],0);
					vy = WebUtils.getDouble(sa[5],0);
					vz = WebUtils.getDouble(sa[6],0);
				}
			}
		}
		catch(Exception ex)
		{
			logger.log(Level.INFO,"Unable to parse vec",ex);
		}
		
		if(epoch == null)
		{
			try
			{
				epoch = req.getParams().get("epoch");
				rx = WebUtils.getDouble(req,"rx",0);
				ry = WebUtils.getDouble(req,"ry",0);
				rz = WebUtils.getDouble(req,"rz",0);
				vx = WebUtils.getDouble(req,"vx",0);
				vy = WebUtils.getDouble(req,"vy",0);
				vz = WebUtils.getDouble(req,"vz",0);
			}
			catch(Exception ex)
			{
				logger.log(Level.INFO,"Unable to parse vec",ex);
			}			
		}
		
		if(epoch != null)
		{
			cart = new CartesianState();
			cart.setEpoch(WebUtils.getDate(epoch));
			cart.rx = rx;
			cart.ry = ry;
			cart.rz = rz;
			cart.vx = vx;
			cart.vy = vy;
			cart.vz = vz;
		}
		return cart;
	}

}
