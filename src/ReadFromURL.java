import java.io.*;

import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.*;
import java.util.Scanner;




public class ReadFromURL {
	private static Queue<String> allURLs = new LinkedList<String>();
	//Maintain a HashMap to keep track on all the URL's visited so we do not crawl multiple times 
	private static HashMap<String,Boolean> visited = new HashMap<String,Boolean>();
	private static ArrayList<String> emailIdFound = new ArrayList<String>();
	private static String domainname;
	
	private static void crawling(String url) throws Exception{
		URL oracle = new URL(url);
		try{
			InputStreamReader input =  new InputStreamReader(oracle.openStream());
			BufferedReader in = new BufferedReader(input);
			//Pattern to look for URL. If any URL exist(in same domain), add it to allURLs queue
	        final Pattern urlPattern = Pattern.compile(
	                "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
	                        + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
	                        + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
	                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        
	        String inputLine;
	        while ((inputLine = in.readLine()) != null){
        	//Pattern to match an email address
             Matcher m = Pattern.compile("[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+").matcher(inputLine);
            
             while (m.find()) {
                 if(!emailIdFound.contains(m.group()))
                	 emailIdFound.add(m.group());
             	}
	             Matcher matcher = urlPattern.matcher(inputLine);
	             while (matcher.find()) {
	            	 String urlFound = inputLine.substring(matcher.start(1),matcher.end(0));
	            	 //discard any URLs with .png, .jpg or .svg file extension
	            	 if(getDomainName(urlFound).equals(domainname) && !visited.containsKey(urlFound) && !urlFound.contains(".jpg") && !urlFound.contains(".ico") && !urlFound.contains(".png") && !urlFound.contains(".svg")){
	                    allURLs.add(urlFound);
	                    visited.put(urlFound, false);
	            	 }
	             }
	        }
	        in.close();
			}catch(IOException e){
		}
	}
	
	//Method to get the domain name
	public static String getDomainName(String url1) throws URISyntaxException {
		String pattern = "^(?:[^/]+://)?([^/:]+)";
		Matcher m = Pattern.compile(pattern).matcher(url1);
		String host="";
		if (m.find()) {
			int start = m.start(1);
			int end = m.end(1);
			host = url1.substring(start, end);
		}
		if(host == null)
			return "";
	    return host.startsWith("www.") ? host.substring(4) : host;
	}
	
	private static void startCrawling(String url) throws Exception{
		crawling(url);
		while(!allURLs.isEmpty()){
			String nextURL = allURLs.poll();
        	if(nextURL!=null && !visited.get(nextURL)){
        		visited.put(nextURL, true);
        		if (!nextURL.startsWith("http://")) {
        			nextURL = "http://" + nextURL;
        		}/*else if (!nextURL.startsWith("http://www.")) {
        			nextURL = "http://www." + nextURL;
        		}else if(url.startsWith("https://www.")){
        			
        		}*/
        		crawling(nextURL);
        	}
        }
	}
	private static void startCrawlingDriver() throws Exception{
		System.out.println("Enter a URL address:");
		Scanner sc = new Scanner(System.in);
		String url = sc.next();
		 if (url.startsWith("www")) {
			url = "http://" + url;
		}
		 else if (!url.startsWith("http://www")) {
			url = "http://www." + url;
		}
		else if (url.startsWith("https://www.")) {
			//do nothing
		}
		if(isValidURL(url)){
			domainname = getDomainName(url);
			System.out.println("Processing...This might take a while!");
			startCrawling(url);
			if(!emailIdFound.isEmpty())
				System.out.println("Found these email addresses:");
			else
				System.out.println("No Email Addresses found");
			for(String email: emailIdFound)
				System.out.println(email);
		}
		else{
			System.out.println("Enter a valid URL with a protocol. For instance http://www.example.com");
		}
	}
	 public static boolean isValidURL(String url){
        try {
            new URL(url).toURI();
            return true;
        }catch (Exception e) {
            return false;
        }
	 }
	public static void main(String[] str) throws Exception{
		startCrawlingDriver();	
	}
}
	
