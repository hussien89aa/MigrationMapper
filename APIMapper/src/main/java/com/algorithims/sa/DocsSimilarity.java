package com.algorithims.sa;

import java.util.ArrayList;

import com.database.mysql.LibraryDocumentationDB;
import com.library.Docs.MethodDocs;
import com.library.source.MethodObj;
import com.library.source.MigratedLibraries;
import com.project.info.CPObject;
import com.project.info.CartesianProduct;
import com.segments.build.Segment;
 

/*
 * This Class measure similarity between list of fragments function
 */
public class DocsSimilarity {

	NLPWebService nlpWebService = new NLPWebService();

 

	public DocsSimilarity( ) {
	

	}
	
	 

	// This method return the max two funtions similar in doucmenation
	public Segment maxLDBreakToy(ArrayList<Segment> segmentListAll) {
		Segment segmentBreakToy = new Segment();

		// get fragment has N-M mapping
		ArrayList<Segment> segmentListNeedBreak = new ArrayList<Segment>(); // Segment that we may could break using
																			// library documentation
		for (Segment segment : segmentListAll) {
			if (segment.getTotalLinesNumbers() > 2) {
				segmentListNeedBreak.add(segment);
			}
		}
		// Tray to break the toy
		// Find the simiarlity in docs between added and removed functions
		if (segmentListNeedBreak.size() > 0) {

			ArrayList<CPObject> listOfCP = new ArrayList<CPObject>();
			for (Segment segment : segmentListNeedBreak) {
				listOfCP = generateListCPwithLD(listOfCP, segment);
			}

			// get max similarity function mapping
			CPObject cpObjectMaxFrequnecy = new CPObject(null, null);
			cpObjectMaxFrequnecy.Frequency = 0;
			for (CPObject cpObject : listOfCP) {
				if (cpObject.Frequency > cpObjectMaxFrequnecy.Frequency) {
					cpObjectMaxFrequnecy = cpObject;
				}
			}
			// set if we have node has max more than 50 will make it as new segment and sed
			// it to the system
			if (cpObjectMaxFrequnecy.Frequency > 50) {
				segmentBreakToy.removedCode.add(cpObjectMaxFrequnecy.value1);
				segmentBreakToy.addedCode.add(cpObjectMaxFrequnecy.value2);

			}

		}

		return segmentBreakToy;
	}

	// generate CP between two list of library documentation
	public ArrayList<CPObject> generateListCPwithLD(ArrayList<CPObject> listOfCP,Segment segment) {
		
		if (segment.fromLibVersion.length() == 0 || segment.toLibVersion.length() == 0) {
			System.err.println("Missing Library documenation cannot apply LD on algorithim, fromLibrary: " + segment.fromLibVersion
					+ ",toLibrary: " + segment.toLibVersion);
			// return;
		} else {
			System.out.println("Loading library documenation");
		}
		// load library docs ;
		ArrayList<MethodDocs> fromLibrary = new LibraryDocumentationDB().getDocs(segment.fromLibVersion, "");

		// load library docs ;
		ArrayList<MethodDocs> toLibrary = new LibraryDocumentationDB().getDocs(segment.toLibVersion, "");
		

		for (String functionNameRemoved : segment.removedCode) {

			MethodObj methodFormObj = MethodObj.GenerateSignature(functionNameRemoved);
			MethodDocs methodFromDocs = MethodDocs.GetObjDocs(fromLibrary, methodFormObj);

			for (String functionNameAdd : segment.addedCode) {
				CPObject cPObject = new CPObject(functionNameRemoved, functionNameAdd);
				int index = cPObject.isFound(listOfCP);
				if (index == -1) {
					// Apply lcs
					// double SimilarDegree= new functionSignature().LCS(funcName2, funName1);

					MethodObj methodToObj = MethodObj.GenerateSignature(functionNameAdd);
					MethodDocs methodToDocs = MethodDocs.GetObjDocs(toLibrary, methodToObj);

					double SimilarDegree = nlpWebService.getCosineSimilarity(methodFromDocs.description,
							methodToDocs.description, TextEngineering.textPreprocessing) * 100;
					/*
					 * DEBUG System.out.println("-----------=======----------");
					 * System.out.println(functionNameAdd);
					 * 
					 * System.out.println(functionNameRemoved); System.out.println("------");
					 * System.out.println(methodToObj.fullMethodName);
					 * System.out.println(methodFormObj.fullMethodName);
					 * 
					 * System.out.println("------"); System.out.println( methodToDocs.description );
					 * System.out.println("------"); System.out.println( methodFromDocs.description
					 * ); System.out.println("------"); System.out.println(SimilarDegree);
					 * System.out.println("-----------=======----------");
					 */

					// At lest they are similar
					if (SimilarDegree > 0) {
						cPObject.Frequency = Math.round((float) SimilarDegree);
						/*
						 * See if we have one of the function in another mapping If if found with high
						 * similar we will ignore new mapping If if found with lower similar we will
						 * update new mapping with exisit one
						 */
						boolean isFoundHighFrequency = false;
						for (int i = 0; i < listOfCP.size(); i++) {
							if (listOfCP.get(i).value1.equals(cPObject.value1)
									|| listOfCP.get(i).value1.equals(cPObject.value2)
									|| listOfCP.get(i).value2.equals(cPObject.value1)
									|| listOfCP.get(i).value2.equals(cPObject.value2)) {
								// if the frequency higher than what we have we will ignore new mapping
								if (listOfCP.get(i).Frequency > cPObject.Frequency) {
									isFoundHighFrequency = true;
									// if the frequency lower than what we have we will update to use our case
								} else if (listOfCP.get(i).Frequency < cPObject.Frequency) {
									isFoundHighFrequency = true;
									listOfCP.set(i, cPObject);
								}
							}
						}
						if (isFoundHighFrequency == false) {
							listOfCP.add(cPObject);
						}
					}

				}

			}
		}

		return listOfCP;
	}

}
