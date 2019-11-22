package com.algorithims.sa;

import java.net.*;
import java.util.HashMap;
import java.io.*;

import org.json.JSONObject;
 
import com.library.source.MethodDoc;
 

import info.debatty.java.stringsimilarity.Cosine;

public class NLPWebService {

	public static void main(String[] args) {
		MethodDoc m1 = new MethodDoc("hello how is your day", "first string to compare");
		MethodDoc m2 = new MethodDoc("hello day is friendly", "second string to compare");
		System.out.println(new NLPWebService().informationExtraction("comMain"));
		// System.out.println(new
		// NLPWebService().getSimilarityScoreWithNLP(m1.fillName,m2.fillName));

	}

	// get Cosine Similarity using azure API
	public String informationExtraction(String text) {

		// remove dots
		text = text.replace(".", " ");

		// Split string when an uppercase letter is found
		String[] tokens = text.split("(?=\\p{Upper})");
		text = "";
		for (String word : tokens) {
			text += " " + word;
		}

		return text;
	}

	/*
	 * Apply Text Pre-Processing 1- remove remove_punct 2- remove stop words 3-
	 * Lemmatizer 4 get Cosine Similarity using 'python-service', make sure run
	 * service using 'python manage.py runserver'
	 */

	public SimilarityScoreWithNLP getSimilarityScoreWithNLP(String text1, String text2) {
		SimilarityScoreWithNLP similarityScoreWithNLP = new SimilarityScoreWithNLP();
		double score = 0.0;
		if (text1.equals(text2)) {
			similarityScoreWithNLP.score = 1.0;
			return similarityScoreWithNLP;
		}
		if (text1.length() == 0 || text2.length() == 0) {
			return similarityScoreWithNLP;
		}

		try {
			String query = "http://127.0.0.1:8000/nlp/";
			String json = "{\"text1\":\"" + text1.replaceAll("\n", " ").replaceAll("\"", " ") + "\",\"text2\":\""
					+ text2.replaceAll("\n", " ").replaceAll("\"", " ") + "\"}";
			// System.out.println(json);
			URL url = new URL(query);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5000);
			conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestMethod("POST");

			OutputStream os = conn.getOutputStream();
			os.write(json.getBytes("UTF-8"));
			os.close();

			// read the response
			InputStream in = new BufferedInputStream(conn.getInputStream());
			String result = org.apache.commons.io.IOUtils.toString(in, "UTF-8");
			JSONObject jsonObject = new JSONObject(result);

			in.close();
			conn.disconnect();

			// return jsonObject.getDouble("score");
			similarityScoreWithNLP.text1NLP = jsonObject.getString("text1NLP");
			similarityScoreWithNLP.text2NLP = jsonObject.getString("text2NLP");
			similarityScoreWithNLP.score = jsonObject.getDouble("score");
			return similarityScoreWithNLP;

		} catch (Exception e) {
			System.out.println(e.getMessage() + ": make sure run 'python-service', using 'python3 manage.py runserver'");
		}

		return similarityScoreWithNLP;
	}

	// Save in ememory for fast query
	static HashMap<String, Double> mappingTableScore = new HashMap();

	// get Cosine Similarity using local library
		public double getCosineSimilarity(String text1, String text2, TextEngineering textEngineeringOption) {

			double score = 0.0;

			if (text1.length() == 0 || text2.length() == 0) {
				return 0.0;
			}

			if (text1.equals(text2)) {
				return 1.0;
			}

			String keyText = text1 + "|| " + text2 + "|| " + textEngineeringOption.name();
			// If we find that similariyt between these two strings already calculated just
			// retrieve from memory
			if (mappingTableScore.get(keyText) != null) {
				// System.out.println("Find in table");
				return mappingTableScore.get(keyText);
			}

			if (textEngineeringOption == TextEngineering.informationExtraction
					|| textEngineeringOption == TextEngineering.all) {
				text1 = informationExtraction(text1);
				text2 = informationExtraction(text2);
			}
			if (textEngineeringOption == TextEngineering.textPreprocessing
					|| textEngineeringOption == TextEngineering.all) {
				SimilarityScoreWithNLP similarityScoreWithNLP = getSimilarityScoreWithNLP(text1, text2);
				text1 = similarityScoreWithNLP.text1NLP;
				text2 = similarityScoreWithNLP.text2NLP;
			}

			// JaroWinkler jw = new JaroWinkler();
			Cosine cos = new Cosine(2);
			// double score= jw.similarity(newDarKeyPhrase, dar.des) ;
			score = (cos.similarity(text1, text2));
			// Save in Table
			mappingTableScore.put(keyText, score);
			// System.out.println("mappingTableScore:"+ mappingTableScore.size());
			return score;
		}
 
}
