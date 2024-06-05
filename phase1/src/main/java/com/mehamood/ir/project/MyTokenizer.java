package com.mehamood.ir.project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyTokenizer {
	static Porter stemmer = new Porter();
	static StopWordFinder stopWordFinder = new StopWordFinder();
	static FolderIterator folderIterator = new FolderIterator();
	static HashMap<String, Integer> fileDictionary = new HashMap<>();
	static HashMap<String, Integer> wordDictionary = new HashMap<>();
	//static int fileId = 1;
	static int wordId = 1;

	public static void main(String[] args) throws IOException {

		File[] fileNames = folderIterator.getFileNames(); // to get the paths of all files
		for (File file : fileNames) { // iterating through each file
			String fileName = file.toString();
			List<Document> documents = extractDocuments(fileName); // to get all documents in each file
			for (Document document : documents) { // iterating through each document
				List<String> tokens = tokenizeDocument(document.getContent());
				// System.out.println("Docno: " + document.getDocno());
				String[] fileID = document.getDocno().split("-");
				fileDictionary.put(document.getDocno(), Integer.valueOf(fileID[1]));
				// System.out.println("Tokens: " + tokens);

				// System.out.println(tokens.size());
				
			}

		}
		wordDictionary.remove("");

		PrintStream printStream = new PrintStream(new FileOutputStream("parser_output.txt"));
		System.setOut(printStream);
		Map<String, Integer> wordTreeMap = new TreeMap<>(wordDictionary);
		for (Map.Entry<String, Integer> entry : wordTreeMap.entrySet()) {
			System.out.printf("%20s" + "     " + "%15s", entry.getKey(), entry.getValue());
			System.out.println();
		}

		Map<String, Integer> fileTreeMap = new TreeMap<>(fileDictionary);
		for (Map.Entry<String, Integer> entry : fileTreeMap.entrySet()) {
			System.out.printf("%20s" + "     " + "%15s", entry.getKey(), entry.getValue());
			System.out.println();

		}

		// System.out.println(wordDictionary.size());

	}

	// Extracting Documents from files
	public static List<Document> extractDocuments(String filename) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		StringBuilder sb = new StringBuilder();
		String line;

		while ((line = reader.readLine()) != null) {
			sb.append(line).append(System.lineSeparator());
		}

		reader.close();
		String html = sb.toString();
		List<Document> documents = new ArrayList<Document>();
		Pattern docPattern = Pattern.compile("<DOC>(.*?)</DOC>", Pattern.DOTALL);
		Matcher docMatcher = docPattern.matcher(html);

		while (docMatcher.find()) {
			String docHtml = docMatcher.group(1);
			Pattern docnoPattern = Pattern.compile("<DOCNO>(.*?)</DOCNO>");
			Matcher docnoMatcher = docnoPattern.matcher(docHtml);
			String docno = "";

			if (docnoMatcher.find()) {
				docno = docnoMatcher.group(1);
			}

			Pattern textPattern = Pattern.compile("<TEXT>(.*?)</TEXT>", Pattern.DOTALL);
			Matcher textMatcher = textPattern.matcher(docHtml);
			String textContent = "";

			if (textMatcher.find()) {
				textContent = textMatcher.group(1);
			}

			documents.add(new Document(docno, textContent));
		}

		return documents;
	}

	public static List<String> tokenizeDocument(String textContent) {
		String[] words = textContent.toLowerCase().split("[\\p{Punct}\\s]+"); // to tokenize the words
		List<String> tokens = new ArrayList<String>();
		List<String> stopWordList = stopWordFinder.getStopwords();

		for (String word : words) {
//			if (!Pattern.matches(".*\\d.*", word)) {
			if (!containsNumbers(word)) { // to remove words containing numbers
				if (word != "") {
					if (!stopWordList.contains(word)) { // to remove stop words
						String stem = stemmer.word_stem(word); // to use stemmer algorithm
						tokens.add(stem);
						if (!wordDictionary.containsKey(stem)) {
							wordDictionary.put(stem, wordId);
							wordId++;
						}
					}
				}
			}

		}

		return tokens;
	}

	// to remove strings with numbers
	private static boolean containsNumbers(String str) {
		for (char c : str.toCharArray()) {
			if (Character.isDigit(c)) {
				return true;
			}
		}
		return false;
	}
}
