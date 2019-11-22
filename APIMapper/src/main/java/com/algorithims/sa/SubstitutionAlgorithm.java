package com.algorithims.sa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import com.database.mysql.MigrationMappingDB;
import com.library.source.MigratedLibraries;
import com.project.settings.AppSettings;
import com.segments.build.Segment;

/**
 * apply Substitution Algorithm on list of fragments to generate function
 * mapping
 */
public class SubstitutionAlgorithm {

	DocsSimilarity docsSimilarity = new DocsSimilarity();  
	ArrayList<Segment> segmentList;
 

	public SubstitutionAlgorithm(final ArrayList<Segment> segmentList) {
		this.segmentList = new ArrayList<Segment>();
		this.segmentList.addAll(segmentList);
	

	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		DocsSimilarity docsSimilarity = new DocsSimilarity();
		ArrayList<Segment> segmentListAll = new ArrayList<Segment>();
		// segment 1
		segmentListAll.add(new Segment(new ArrayList(Arrays.asList("a", "b", "a*", "b*")),
				new ArrayList(Arrays.asList("a", "b")), new ArrayList(Arrays.asList("a*", "b*"))));
		// segment 2
		Segment segment1 = new Segment(new ArrayList(Arrays.asList("a", "a*")), new ArrayList(Arrays.asList("a")),
				new ArrayList(Arrays.asList("a*")));
		segment1.frequency = 1;
		segmentListAll.add(segment1);

		Segment segment2 = new Segment(new ArrayList(Arrays.asList("b", "b*")), new ArrayList(Arrays.asList("b")),
				new ArrayList(Arrays.asList("bs*")));
		segment2.frequency = 10;
		segmentListAll.add(segment2);
		// segment 3
		segmentListAll.add(new Segment(new ArrayList(Arrays.asList("a", "b", "c", "a*", "b*", "c*")),
				new ArrayList(Arrays.asList("a", "b", "c")), new ArrayList(Arrays.asList("a*", "b*", "c*"))));
		// segment 4
		segmentListAll.add(new Segment(new ArrayList(Arrays.asList("a", "b", "a*")),
				new ArrayList(Arrays.asList("a", "b")), new ArrayList(Arrays.asList("a*"))));
		SubstitutionAlgorithm sa = new SubstitutionAlgorithm(segmentListAll);
		ArrayList<Segment> segmentListAllOrdered = sa.orderSegments(segmentListAll);
		Collections.sort(segmentListAllOrdered);
		for (Segment segment : segmentListAllOrdered) {
			segment.print();
		}
	}

	ArrayList<Segment> orderSegments(ArrayList<Segment> segmentListAll) {
		ArrayList<Segment> segmentListAllOrdered = new ArrayList<Segment>();

		segmentListAllOrdered.addAll(segmentListAll);
		return segmentListAllOrdered;
	}

	public void start() {

		if (segmentList.size() < 2) {
			System.err.println("Only one Segment cannot do interset");
			return;
		}

		boolean hasMoreIntersection = false;
		int useLDTimes = 0; // number of times that we call LD
		ArrayList<Segment> segmentListAll = new ArrayList<Segment>();
		segmentListAll.addAll(segmentList);
		// System.err.println("____________________");
		// printFragments(segmentListAll);

		do {

			// when segmentListAll=1 we done no longer search
			if (segmentListAll.size() == 1) {

				break;
			}
			// Sort segments
			Collections.sort(segmentListAll);
			ArrayList<Segment> segmentListNew = new ArrayList<Segment>();
			
			// search for intersections between two fragments
			for (int i = 0; i < segmentListAll.size(); i++) {
				hasMoreIntersection = false;
				for (int iNext = 0; iNext < segmentListAll.size(); iNext++) {  
					if (i == iNext) {
						continue;
					}
					ArrayList<Segment> intersectingSegments = new ArrayList<Segment>();
					segmentListNew.clear();
					segmentListNew.addAll(segmentListAll);
					 
					Segment segment1 = segmentListAll.get(i);
					Segment segment2 = segmentListAll.get(iNext);

					 
					ReturnIntersectedSegment returnIntersectedSegment = intersectSegments(segment1, segment2);
					if (returnIntersectedSegment.hasIntersection) {
						 // If we found intersection add ad new fragments and remove shared code between two fragments
						hasMoreIntersection = true;
						// intersectingSegments.addAll( intersectSegments(segment1,segment2) );
						intersectingSegments.addAll(returnIntersectedSegment.intersectedSegment);
						segmentListNew.remove(segment1);
						segmentListNew.remove(segment2);

					}
					 
					// In no more intersections founds using library documenation to find more interesctions
					for (Segment newSegment : intersectingSegments) {
						boolean isAdded = false;
						for (int j = 0; j < segmentListNew.size(); j++) {
							// TODO: need fix for n-m mapping
							if (segmentListNew.get(j).addedCode.equals(newSegment.addedCode)
									&& segmentListNew.get(j).removedCode.equals(newSegment.removedCode)) {
								segmentListNew.get(j).frequency = segmentListNew.get(j).frequency + 1; // segmentListNew.get(j).frequency+
								isAdded = true;
							}
						}
						if (isAdded == false) {
							segmentListNew.add(newSegment);
						}
					} // complete intersectingSegments

					if (hasMoreIntersection) {
						break;
					}
				} // search in segment

				// System.out.println("-------------------------------");
				if (hasMoreIntersection) {
					break;
				}
			}
			segmentListAll.clear();
			segmentListAll.addAll(segmentListNew);

			if (hasMoreIntersection == false) {

				if (AppSettings.isUsingLD) {
					// Check if we have N-M mapping with Library doumenation could break the toy
					Segment segmentBreakToy = docsSimilarity.maxLDBreakToy(segmentListAll);
					if ((segmentBreakToy.addedCode.size() > 0) && (segmentBreakToy.removedCode.size() > 0)) {
						segmentListAll.add(segmentBreakToy);
						hasMoreIntersection = true;
						useLDTimes++;
					}

				}

			}
			// DEBUG only
			// System.out.println("\tData after try");
			// System.out.println("==============================");
			// printFragments( segmentListAll);
			// System.out.println("==============================");
		} while (hasMoreIntersection);

	 
		// DEBUG only
		System.out.println("print with Substitution Algorithm(" + segmentListAll.size() + ") segments");
		// TODO: return this after fix bug

		printFragments(segmentListAll);

		// Save to the database
		MigrationMappingDB migrationMapping = new MigrationMappingDB();
		for (Segment segmentToSave : segmentListAll) {
			// TODO: this temploty to keep at lest see the segment 2 times
			// if(segmentToSave.frequency<2){ continue;}
			migrationMapping.addMigrationMappingWithConfident(segmentToSave, ConfidentType.Substitution);

		}
 
	}

	// TODO: same function twice in block
	ReturnIntersectedSegment intersectSegments(Segment segment1, Segment segment2) {
		ReturnIntersectedSegment returnIntersectedSegment = new ReturnIntersectedSegment();
		returnIntersectedSegment.hasIntersection = false;
		// DEBUG only
		// segment1.print();
		// segment2.print();
		// System.out.println("s1:"+ segment1.addedCode.toString() +":"+
		// segment1.removedCode.toString());
		// System.out.println("s1:"+ segment2.addedCode.toString() +":"+
		// segment2.removedCode.toString());
		// build intersected functions
		Segment segmentNew = new Segment();
		Segment segment1New = new Segment();
		segment1New.frequency = segment1.frequency; // Inherit the frequency
		Segment segment2New = new Segment();
		segment2New.frequency = segment2.frequency;// Inherit the frequency

		for (String funName : segment1.removedCode) {
			if (segment2.removedCode.contains(funName)) {
				if (segmentNew.removedCode.contains(funName) == false) {
					segmentNew.removedCode.add(funName);
				}
			}
		}
		for (String funName : segment1.addedCode) {
			if (segment2.addedCode.contains(funName)) {
				if (segmentNew.addedCode.contains(funName) == false) {
					segmentNew.addedCode.add(funName);
				}
			}
		}

		// new segment 1 removedCode
		for (String funName : segment1.removedCode) {
			if (segmentNew.removedCode.contains(funName) == false) {
				segment1New.removedCode.add(funName);
			}
		}
		// new segment 1 addedCode
		for (String funName : segment1.addedCode) {
			if (segmentNew.addedCode.contains(funName) == false) {
				segment1New.addedCode.add(funName);
			}
		}

		// new segment 2 removedCode
		for (String funName : segment2.removedCode) {
			if (segmentNew.removedCode.contains(funName) == false) {
				segment2New.removedCode.add(funName);
			}
		}
		// new segment 2 addedCode
		for (String funName : segment2.addedCode) {
			if (segmentNew.addedCode.contains(funName) == false) {
				segment2New.addedCode.add(funName);
			}
		}

		if (segmentNew.addedCode.size() == 0 || segmentNew.removedCode.size() == 0) {
			// System.out.println("-----------NOT INTERSECT------------");
			return returnIntersectedSegment;
		}

		// TODO: this code may need improrve in find 1:N
		// generate new Segment 1 and 2 without Segment
		if (segmentNew.addedCode.size() > 0 && segmentNew.removedCode.size() > 0) {
			segmentNew.frequency = segment1.frequency + segment2.frequency;
			returnIntersectedSegment.intersectedSegment.add(segmentNew);
			returnIntersectedSegment.hasIntersection = true;
		}
		if (segment1New.addedCode.size() > 0 && segment1New.removedCode.size() > 0)
			returnIntersectedSegment.intersectedSegment.add(segment1New);
		if (segment2New.addedCode.size() > 0 && segment2New.removedCode.size() > 0)
			returnIntersectedSegment.intersectedSegment.add(segment2New);
		// System.out.println("returnIntersectedSegment.hasIntersection:"+returnIntersectedSegment.hasIntersection);
		// System.out.println("----------PRINT INTERSECTED-----------");
		// printFragments(returnIntersectedSegment.intersectedSegment);
		return returnIntersectedSegment;
	}

	boolean hasIntersect(Segment segment1, Segment segment2) {
		boolean hasAddIntersect = false;
		boolean hasRemoveIntersect = false;
		for (String funName : segment1.removedCode) {
			if (segment2.removedCode.contains(funName)) {
				hasRemoveIntersect = true;
				break;
			}
		}
		for (String funName : segment1.addedCode) {
			if (segment2.addedCode.contains(funName)) {
				hasAddIntersect = true;
				break;
			}
		}

		return hasAddIntersect && hasRemoveIntersect;

	}

	void printFragments(ArrayList<Segment> segmentList) {
		int fragmentNumber = 1;
		for (Segment segment : segmentList) {
			System.out.println((fragmentNumber++) + "- ****************** Fragment has (" + segment.removedCode.size()
					+ ") function ******************");
			segment.print();
			System.out.println("frequency:" + segment.frequency);

		}
	}

}

class ReturnIntersectedSegment {
	ArrayList<Segment> intersectedSegment = new ArrayList<Segment>();
	boolean hasIntersection;
}
