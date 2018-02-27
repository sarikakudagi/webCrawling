import java.io.*;

import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.*;
import java.util.Scanner;

public class ReadFromUrl {

	private static Queue<String> urlsFound = new LinkedList<String>();
	// Maintain a HashMap to keep track on all the URL's visited so we do not
	// crawl multiple times
	private static HashMap<String, Boolean> urlsVisited = new HashMap<String, Boolean>();
	private static ArrayList<String> emailsFound = new ArrayList<String>();
	private static String domainName;
	private static int maxPageDepth = 2;

	// finds and stores URL's, email ID's found while crawling
	private static void findUrlsAndEmailsOnSite(String url) throws Exception {
		URL oracle = new URL(url);
		try {
			InputStreamReader input = new InputStreamReader(oracle.openStream());
			BufferedReader in = new BufferedReader(input);
			// Pattern to look for URL. If any Urls exist(in same domain), add
			// it to urlsFound queue
			final Pattern urlPattern = Pattern
					.compile(
							"(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
									+ "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
									+ "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
							Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
									| Pattern.DOTALL);

			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				// Pattern to match an email address
				Matcher emailMatcher = Pattern.compile(
						"[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+")
						.matcher(inputLine);

				while (emailMatcher.find()) {
					if (!emailsFound.contains(emailMatcher.group()))
						emailsFound.add(emailMatcher.group());
				}
				Matcher UrlMatcher = urlPattern.matcher(inputLine);
				while (UrlMatcher.find()) {
					String urlFound = inputLine.substring(UrlMatcher.start(1),
							UrlMatcher.end(0));
					// discard any URLs with .png, .jpg, .css or .svg file
					// extension
					if (getDomainName(urlFound).equals(domainName)
							&& !urlsVisited.containsKey(urlFound)
							&& !urlFound.contains(".jpg")
							&& !urlFound.contains(".ico")
							&& !urlFound.contains(".png")
							&& !urlFound.contains(".svg")
							&& !urlFound.contains(".css")) {
						urlsFound.add(urlFound);
						urlsVisited.put(urlFound, false);
					}
				}
			}
			in.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	// Extracts the domain from the given url
	private static String getDomainName(String url) throws URISyntaxException {
		String domainPattern = "^(?:[^/]+://)?([^/:]+)";
		Matcher domainMatcher = Pattern.compile(domainPattern).matcher(url);
		String host = "";
		if (domainMatcher.find()) {
			int start = domainMatcher.start(1);
			int end = domainMatcher.end(1);
			host = url.substring(start, end);
		}
		if (host == null)
			return "";
		return host.startsWith("www.") ? host.substring(4) : host;
	}

	// Iterates through urls found in the provided site
	private static void processSite(String url) throws Exception {
		findUrlsAndEmailsOnSite(url);
		maxPageDepth--;
		while (true) {
			int queueSize = urlsFound.size();
			if (queueSize == 0 || maxPageDepth == 0)
				break;
			while (queueSize > 0 && maxPageDepth > 0) {
				String nextURL = urlsFound.poll();
				if (nextURL != null && !urlsVisited.get(nextURL)) {
					urlsVisited.put(nextURL, true);
					if (!nextURL.startsWith("http://")) {
						nextURL = "http://" + nextURL;
					}
					findUrlsAndEmailsOnSite(nextURL);
					queueSize--;
				}
			}
			maxPageDepth--;
		}
	}

	// Processes the url provided through user input
	private static void crawlingDriver() throws Exception {
		System.out.println("Enter a URL address:");
		Scanner sc = new Scanner(System.in);
		String url = sc.next();
		if (url.startsWith("www")) {
			url = "http://" + url;
		} else if (!url.startsWith("http://www")) {
			url = "http://www." + url;
		} else if (url.startsWith("https://www.")) {
			// do nothing
		}
		if (isValidURL(url)) {
			domainName = getDomainName(url);
			System.out.println("Processing...This might take a while!");
			processSite(url);
			if (!emailsFound.isEmpty())
				System.out.println("Found these email addresses:");
			else
				System.out.println("No Email Addresses found");
			for (String email : emailsFound)
				System.out.println(email);
		} else {
			System.out
					.println("Enter a valid URL with a protocol. For instance http://www.example.com");
		}
	}

	// check if it is a valid URL
	private static boolean isValidURL(String url) {
		try {
			new URL(url).toURI();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static void main(String[] str) throws Exception {
		crawlingDriver();
	}
}