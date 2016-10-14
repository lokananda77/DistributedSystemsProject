package example.hello;

        
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.io.*;
public class Server implements Hello {
        
	static Calendar c = Calendar.getInstance();
	
	static int num_nodes=1;
	static int k_per_node=2;
	int uid=0;
	
	static String node_ips[]=new String[100];
	
	static Hashtable<Integer,Vector<String>> DHT = new Hashtable<Integer,Vector<String>>();
	Vector<String> toCrawl = new Vector<String>();
	StringBuffer page=new StringBuffer("");
    
	public Server() {}

	public void get_page(String url)
	{
		
		String tmp;
		c= Calendar.getInstance();
		System.out.println(c.get(Calendar.HOUR)+":"+c.get(Calendar.MINUTE)+":"+c.get(Calendar.SECOND)+":"+c.get(Calendar.MILLISECOND)+" Reading page source from "+url);
		try{
			FileInputStream fstream = new FileInputStream(url);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br= new BufferedReader(new InputStreamReader(in));
			tmp = br.readLine();
			while( tmp != null)
			{
				page.append(tmp);
				tmp = br.readLine();
			}
			
			System.out.println("***********************");
			System.out.println(page);
			System.out.println("***********************");
			in.close();
			
		}
		catch(Exception e)
		{
		e.printStackTrace();
		}
	}
	public boolean crawl_link()
	{
		//System.out.println("here 4");
		//System.out.println(page);
		int first,strq,endq;
		first=page.indexOf("<a href=");
		if((first)!=-1)
		{
			//System.out.println("here 5");
			strq=page.indexOf("\"", first+1);
			endq=page.indexOf("\"", strq+1);
			isCrawled(page.substring(strq+1,endq),uid);
				//toCrawl.addElement(page.substring(strq,endq));
			//System.out.println("here 1");
			//page=(StringBuffer)page.substring(endq+1,page.length());
			page=page.delete(0, endq);
			return true;
		}
		return false;	
	}
	public void addToDHT(String url)
	{
		int k=getHash(url);
		//System.out.println(k+" "+url);
		if(DHT.containsKey(k))
		{
		 (DHT.get(k)).addElement(url);	
		 c= Calendar.getInstance();
		  System.out.println(c.get(Calendar.HOUR)+":"+c.get(Calendar.MINUTE)+":"+c.get(Calendar.SECOND)+":"+c.get(Calendar.MILLISECOND)+" Updating DHT : "+url);
		}
		else
		{
			Vector<String> tmp=new Vector<String>();
			tmp.addElement(url);
			DHT.put(k, tmp);
			c= Calendar.getInstance();
			System.out.println(c.get(Calendar.HOUR)+":"+c.get(Calendar.MINUTE)+":"+c.get(Calendar.SECOND)+":"+c.get(Calendar.MILLISECOND)+" Updating DHT : "+url);
		}
		
	}
	public void crawl_page()
	{
		String pg=toCrawl.firstElement();
		//System.out.println(pg);
		toCrawl.remove(0);
		//System.out.println("here 1.2");
		addToDHT(pg);
		//System.out.println("here 1.5");
		get_page(pg);
		
		//System.out.println("here 2");
		while(true)
		{
			if(!crawl_link())
			{
			//	System.out.println("here 3");
				break;
			}
		}
	}
    public void addToCrawl(String url)
    {
    	int nid=getHash(url)/k_per_node;
    		
    		String host = node_ips[nid];
    		//System.out.println(host);
            try {
                Registry registry = LocateRegistry.getRegistry(host,1099);
                Hello stub = (Hello) registry.lookup("Hello");
                stub.isCrawled(url,uid);
                
            } catch (Exception e) {
                System.err.println("Node exception: " + e.toString());
                e.printStackTrace();
            }	
    }
    public boolean isCrawled(String url,int sid)
    {
    	Vector urllist=DHT.get(getHash(url));
    	int nid=getHash(url)/k_per_node;
    	//System.out.println("here 6");
        if(sid!=uid)
        {
        	c= Calendar.getInstance();
        	System.out.println(c.get(Calendar.HOUR)+":"+c.get(Calendar.MINUTE)+":"+c.get(Calendar.SECOND)+":"+c.get(Calendar.MILLISECOND)+" Recieving "+url+" from "+sid+" to crawl");
        }
    	//System.out.println("job"+url+" "+nid);
    	if(urllist!=null)
    	{
			if(urllist.indexOf(url)>=0)
			{
				c= Calendar.getInstance();
				System.out.println(c.get(Calendar.HOUR)+":"+c.get(Calendar.MINUTE)+":"+c.get(Calendar.SECOND)+":"+c.get(Calendar.MILLISECOND)+" Already crawled : "+url);
				return true; 
			}	
    	}
    	//System.out.println(uid+" ?==? "+nid);
    	
    	if(nid==uid)
    	{
    		c= Calendar.getInstance();
    		System.out.println(c.get(Calendar.HOUR)+":"+c.get(Calendar.MINUTE)+":"+c.get(Calendar.SECOND)+":"+c.get(Calendar.MILLISECOND)+" Adding to the Crawl Queue : "+url);
    		toCrawl.addElement(url);
    	}
    	
    	else
    	{
    		c= Calendar.getInstance();
    		System.out.println(c.get(Calendar.HOUR)+":"+c.get(Calendar.MINUTE)+":"+c.get(Calendar.SECOND)+":"+c.get(Calendar.MILLISECOND)+" Sending "+url+" to Node "+nid);
    		addToCrawl(url);
    	}
    	return false; 	
    }
    
    public int getHash(String url)
    {
    	int i=0,k=0;
    	String tmp=url;
    	//System.out.println(tmp);
    	
    	//String tmp=url.substring(url.indexOf('.'),url.substring(url.indexOf('.')+1,url.length()).indexOf('/'));
    	while(i<tmp.length())
    	{
    		
    		
    		k+=(int)tmp.charAt(i);
    		i++;
    		
    	}
    	//System.out.println(k);
    	return k%(num_nodes*k_per_node);
    	
    }
    
    public Vector<String> sendVector(int key)
    {
    	
    	return DHT.get(key);
    }
    public static void main(String args[]) {
        
    	
    	node_ips[0]="169.254.11.189";
    	node_ips[1]="169.254.146.125";
    	
    	int sh=c.get(Calendar.HOUR);
    	int sm=c.get(Calendar.MINUTE);
    	int ss=c.get(Calendar.SECOND);
    	int sms=c.get(Calendar.MILLISECOND);
    	
    	 //System.err.println("Server read0");
        try {
            Server obj = new Server();
            Hello stub = (Hello) UnicastRemoteObject.exportObject(obj, 0);
            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.getRegistry();
            registry.bind("Hello", stub);
            c= Calendar.getInstance();
            System.err.println(c.get(Calendar.HOUR)+":"+c.get(Calendar.MINUTE)+":"+c.get(Calendar.SECOND)+":"+c.get(Calendar.MILLISECOND)+" Crawler starts");
            obj.toCrawl.addElement("C:\\ex\\1.html");
            while(true)
            {
	        while(obj.toCrawl.size()>0)
	            {
	        	//System.out.println("here 1");
	            	obj.crawl_page();
	            //System.out.println("here 10");
	            }
	        //System.out.println("here end");
	        c = Calendar.getInstance();
            if((c.get(Calendar.SECOND)-ss>5)||(c.get(Calendar.MINUTE)-sm==1))
            	break;
        	}
            System.out.println(c.get(Calendar.HOUR)+":"+c.get(Calendar.MINUTE)+":"+c.get(Calendar.SECOND)+":"+c.get(Calendar.MILLISECOND)+" Crawler Ends");
        }
        catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        	}
        for(int p=0;p<num_nodes*k_per_node;p++)
        {
        	int j=0;
        	if(DHT.containsKey(p))
        	{
        		
        		Vector<String> urlls=DHT.get(p);
        		System.out.println(urlls.size());
        		while(j<urlls.size())
        		{
        			System.out.println(j+" "+p+":"+urlls.get(j));
        			j++;
        			
        		}
        		
        	}
        	else
        	{
        		
        		Vector<String> urlls=new Vector<String>();
        		j=0;
        		String host = node_ips[p/k_per_node];
        		//System.out.println(host);
                try {
                    Registry registry = LocateRegistry.getRegistry(host,1099);
                    Hello stub = (Hello) registry.lookup("Hello");
                    urlls=stub.sendVector(p);
                    
                } catch (Exception e) {
                    System.err.println("Node exception: " + e.toString());
                    e.printStackTrace();
                }
        		
        		while(j<urlls.capacity())
        		{
        			System.out.println(p+":"+urlls.get(j));
        			j++;
        			
        		}
        		
        	}
        	
        }
    }
    
}
