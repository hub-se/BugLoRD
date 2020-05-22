package se.de.hu_berlin.informatik.benchmark.api.defects4j;

import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.utils.experiments.cv.CrossValidationUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Misc;

import java.io.File;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

public class Defects4JBase {

    public final static String SEP = File.separator;

    public final static String PROP_FILE_NAME = "defects4jProperties.ini";

    public static String[] getAllBugIDs(String projectId) {
        return getAllActiveBugIDs(getProjectFromId(projectId));
    }
    
    public static String[] getAllActiveBugIDs(Defects4JProject project) {
        int maxID = getMaxBugID(project);
        String[] result = new String[maxID - project.excluded.length];
        int index = 0;
        for (int i = 1; i < maxID + 1; ++i) {
        	if (!isExcluded(i, project)) {
        		result[index++] = String.valueOf(i);
        	}
        }
        return result;
    }
    
    private static boolean isExcluded(int i, Defects4JProject project) {
		for (int j : project.excluded) {
			if (i == j) {
				return true;
			}
		}
		return false;
	}

	// extracted from Defects4J v2.0 - https://github.com/rjust/defects4j
    public static enum Defects4JProject {
    	CHART("Chart", "jfreechart", 26),
    	CLI("Cli", "commons-cli", 40, 6),
    	CLOSURE("Closure", "closure-compiler", 176, 63, 93),
    	CODEC("Codec", "commons-codec", 18),
    	COLLECTIONS("Collections", "commons-collections", 28, 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24),
    	COMPRESS("Compress", "commons-compress", 47),
    	CSV("Csv", "commons-csv", 16),
    	GSON("Gson", "gson", 18),
    	JACKSON_CORE("JacksonCore", "jackson-core", 26),
    	JACKSON_DATABIND("JacksonDatabind", "jackson-databind", 112),
    	JACKSON_XML("JacksonXml", "jackson-dataformat-xml", 6),
    	JSOUP("Jsoup", "jsoup", 93),
    	JX_PATH("JxPath", "commons-jxpath", 22),
    	LANG("Lang", "commons-lang", 65, 2),
    	MATH("Math", "commons-math", 106),
    	MOCKITO("Mockito", "mockito", 38),
    	TIME("Time", "joda-time", 27, 21);
    	
    	private String id;
		private String name;
		private int bugs;
		private int[] excluded;

		Defects4JProject(String id, String name, int bugs, int... excluded) {
			this.id = id;
			this.name = name;
			this.bugs = bugs;
			this.excluded = excluded;
    		
    	}

		public String getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public int getBugs() {
			return bugs;
		}

		public int[] getExcluded() {
			return excluded;
		}
		
		@Override
		public String toString() {
			return id;
		}
    }
    
    public static int getMaxBugID(String projectId) {
    	return getMaxBugID(getProjectFromId(projectId));
    }

	public static Defects4JProject getProjectFromId(String projectId) {
		return Misc.getEnumFromToString(Defects4JProject.class, projectId);
	}
    
    public static int getMaxBugID(Defects4JProject project) {
    	if (project == null) {
        	return 0;
        } else {
			return project.bugs;
		}
    }

    public static Defects4JEntity[] getAllBugs(String project) {
        int maxID = getMaxBugID(project);
        Defects4JEntity[] result = new Defects4JEntity[maxID];
        for (int i = 0; i < maxID; ++i) {
            result[i] = Defects4JEntity.getBuggyDefects4JEntity(project, String.valueOf(i + 1));
        }
        return result;
    }

    public static EnumSet<Defects4JProject> getAllProjects() {
        return EnumSet.allOf(Defects4JProject.class);
    }
    
    public static String[] getAllProjectIDs() {
    	EnumSet<Defects4JProject> set = EnumSet.allOf(Defects4JProject.class);
    	String[] result = new String[set.size()];
    	int i = 0;
    	for (Iterator<Defects4JProject> iterator = set.iterator(); iterator.hasNext();) {
			Defects4JProject defects4jProject = iterator.next();
			result[i++] = defects4jProject.id;
		}
        return result;
    }

    public static boolean validateProject(String project, boolean abortOnError) {
        for (final Defects4JProject element : Defects4JBase.getAllProjects()) {
            if (element.id.equals(project)) {
                return true;
            }
        }

        if (abortOnError) {
            Log.abort(
                    Defects4JBase.class,
                    "Chosen project has to be one of: %s", 
                    Misc.enumToString(Defects4JProject.class));
        }
        return false;
    }

    public static boolean validateProjectAndBugID(String project, int parsedID, boolean abortOnError) {
        if (!validateProject(project, abortOnError)) {
            return false;
        }

        if (parsedID < 1) {
            if (abortOnError)
                Log.abort(Defects4JBase.class, "Bug ID is negative.");
            else
                return false;
        }

        int maxID = getMaxBugID(project);
        if (parsedID > maxID) {
            if (abortOnError) {
                Log.abort(Defects4JBase.class, "Bug ID may only range from 1 to %d for project %s.", maxID, project);
            } else {
                return false;
            }
        }

        return true;
    }

    public static List<BuggyFixedEntity<?>>[] generateNBuckets(BuggyFixedEntity<?>[] array, int n, Long seed,
                                                               Path csvOutput) {
        List<BuggyFixedEntity<?>>[] buckets = CrossValidationUtils.drawFromArrayIntoNBuckets(array, n, seed);

        CrossValidationUtils.generateFileFromBuckets(buckets, BuggyFixedEntity::getUniqueIdentifier, csvOutput);

        return buckets;
    }

    public static List<BuggyFixedEntity<?>>[] readBucketsFromFile(Path csvFile) {

        return CrossValidationUtils.getBucketsFromFile(csvFile, Defects4JBase::parseIdentifier);
    }

    private static BuggyFixedEntity<?> parseIdentifier(String k) {
        String[] items = k.split(Defects4JBuggyFixedEntity.SEPARATOR_CHAR, 0);
        if (items.length != 2) {
            Log.err(Defects4JBase.class, "'%s' is not a parseable identifier for a Defects4J entity.", k);
            return null;
        }
        return new Defects4JBuggyFixedEntity(items[0], items[1]);
    }

}
