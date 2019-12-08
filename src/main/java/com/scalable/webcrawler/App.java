package com.scalable.webcrawler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * a web crawler to extract the top 5 most widely used js-libraries (included by
 * a script-tag) in search results
 * 
 * tloder 12.07.19
 */

public class App {
	private static final String SEARCH_URL = "http://www.google.de/search"; // https://www.google.de/search?q=greta
	private static final int MAX_NUMBER = 5;
	private static final String PATH_SEPARATOR = "/";
//	private static final String FILE_EXTENSION = ".js";

	private List<String> scriptList = new ArrayList<String>();

	public static void main(String[] args) {
		App app = new App();

		try {
			// get searchterm form stdin
			String searchTerm = app.getSearchTerm();

			// get search result
			System.out.println("getting search results for : " + searchTerm);
			Document doc = app.getSourceCode(String.format("%s?q=%s", SEARCH_URL, searchTerm));

			// search result are located in source code under container <div class="srg"> in
			// <div class="g">
			Elements searchResults = doc.select(".srg .g");
			
			// --------- start collecting js-libraries ----------------------
			// extracting href-attribute from elements and pull up source code
			for (Element e : searchResults) {

				// get element with a-tag
				Elements anchorTagElement = e.getElementsByTag("a");
				if (!anchorTagElement.isEmpty()) {
					// get href-attribute from anchor tag
					String href = anchorTagElement.attr("href");
					System.out.println("\ngetting source code from: " + href);

					// get src-code from search result
					Document searchResultDoc = app.getSourceCode(href);
					if (searchResultDoc == null)
						continue;

					// get script-tags
					Elements scriptElements = searchResultDoc.getElementsByTag("script");
					for (Element scriptElem : scriptElements) {
						// extract src-attribute from script-tag
						String srcAttribute = scriptElem.attr("src");

						// collect them
						if (!"".equals(srcAttribute)) {
							String fileName = app.extractName(srcAttribute);
							if (fileName != null) {
								System.out.println("found script " + fileName);
								app.scriptList.add(fileName);
							}
						}
					}
				} else {
					System.out.println("href-attribute of search result could not be found");

				}
			}
			// ------------ done collecting script libraries
			
			// group and sort them
			// print most widely used: name | count
			System.out.println(String.format("\nTop %d scripts (name | count):", MAX_NUMBER));
			app.scriptList
					.stream()
					.collect(Collectors.groupingBy(new Function<String, String>() {
							public String apply(String t) {
								return t;
							}
						}, Collectors.counting() ) )
					.entrySet()
					.stream()
					.sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
					.limit(MAX_NUMBER)
					.forEach(System.out::println);
			

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	
	
	/*
	 * pull source code from url and return document
	 */
	private Document getSourceCode(String url) {
		try {
			return Jsoup.connect(url).get();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/*
	 * extract fileName from path return only fileNames with suffix .js
	 */
	private String extractName(String path) {
		try {
			int lastIndexOf = path.lastIndexOf(PATH_SEPARATOR);
			String fileName = path.substring(lastIndexOf + 1);
//			if (fileName.endsWith(FILE_EXTENSION)) 
				return fileName;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	
	/*
	 * get input from stdIn
	 */
	private String getSearchTerm(){
		String searchTerm = "";
		
		Scanner scanner = null;
		try {
			while (true) {
				scanner = new Scanner(System.in);
				System.out.print("Enter search term : ");
				searchTerm = scanner.nextLine();

				if (!"".equals(searchTerm)) {
					return searchTerm;
				}
			} 
		} finally {
			if(scanner != null)
				scanner.close();
		}
	}
}
