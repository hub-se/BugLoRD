package se.de.hu_berlin.informatik.experiments.defects4j.calls;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import se.de.hu_berlin.informatik.benchmark.api.BuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.api.defects4j.Defects4JBuggyFixedEntity;
import se.de.hu_berlin.informatik.benchmark.modification.Modification;
import se.de.hu_berlin.informatik.utils.files.FileUtils;
import se.de.hu_berlin.informatik.utils.miscellaneous.Log;
import se.de.hu_berlin.informatik.utils.miscellaneous.Pair;
import se.de.hu_berlin.informatik.utils.processors.AbstractProcessor;

/**
 * Runs a single experiment.
 *
 * @author Simon Heiden
 */
public class ERBugDiagnosisEH extends AbstractProcessor<BuggyFixedEntity<?>, BuggyFixedEntity<?>> {

	static int global_level = 0;
	
    @Override
    public BuggyFixedEntity<?> processItem(BuggyFixedEntity<?> buggyEntity) {
        Log.out(this, "Processing %s.", buggyEntity);

        if (buggyEntity instanceof Defects4JBuggyFixedEntity) {
        	Defects4JBuggyFixedEntity d4jEntity = (Defects4JBuggyFixedEntity) buggyEntity;
        	
        	Map<String, List<Modification>> xmlFile = Defects4JBuggyFixedEntity.getModificationsFromXmlFile(d4jEntity.getProject(), d4jEntity.getBugID());
        	if (xmlFile != null) {
        		Log.out(this, "Bug diagnosis XML file already available for %s.", buggyEntity);
        		return buggyEntity;
        	}
        	
        	boolean bugExisted = buggyEntity.requireBug(true);
        	boolean fixExisted = buggyEntity.requireFix(true);

        	//        System.out.println("Please use one of the following options:\n");
        	//        System.out.println("-generate <projectid> <bugnumber> <checkout-path> (-dbg): 
        	// generates all XML files for the specified project (including all debug messages to stdout\n");
        	//        System.out.println("*BWF's -checkout should be executed at least once before -generate is executed*");


        	HashMap<String, String> info = new HashMap<String, String>();
        	HashMap<String, HashMap<String, String>> patches = new HashMap<String, HashMap<String, String>>();

        	info = infos(d4jEntity);
        	patches = patchAnalysis(info, d4jEntity);
        	printXML(info, patches, d4jEntity);
        	
        	if (!bugExisted) {
        		buggyEntity.getBuggyVersion().deleteAllButData();
            }

            if (!fixExisted) {
            	buggyEntity.getFixedVersion().deleteAllButData();
            }
        }
        
        return buggyEntity;
    }

    
    /***
    Function infos temporarily creates the info files and reads them
    ***/
    public static HashMap<String,String> infos(Defects4JBuggyFixedEntity buggyEntity)
    {
        dbg("-- Function info --");
        
        HashMap<String, String> info = new HashMap<String, String>();
        ArrayList<String> read = new ArrayList<String>();

        String filenPath = buggyEntity.getBuggyVersion().getWorkDir(true)
        		.resolve(buggyEntity.getProject() + "_info_bug_" + buggyEntity.getBugID() + ".txt")
        		.toAbsolutePath().toString();
        dbg("File = " + filenPath);
        File myfile = new File(filenPath);
        ProcessBuilder builder;
        Process process;
		try {
			builder = new ProcessBuilder("defects4j", "info", 
					"-p", buggyEntity.getProject(), 
					"-b", buggyEntity.getBugID());
			builder.directory(buggyEntity.getBuggyVersion().getWorkDir(true).toFile());
			
			process = builder.start();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}

        try (InputStream input = new BufferedInputStream(process.getInputStream());
        		PrintStream out = new PrintStream(new FileOutputStream(myfile))) {

        	int counter;
        	int buffer_len=50000;
        	byte[] buffer = new byte[buffer_len];

        	while((counter = input.read(buffer)) != -1 && counter < buffer_len)
        	{
        		out.write(buffer,0,counter);
        		counter=input.read(buffer);
        	}

        	read =  readfile(filenPath);
        	info =  searcharrayInfo(read);

        	out.flush();
        } catch (IOException e) {
        	throw new IllegalStateException(e);
		} finally {
			process.destroy();   
//			myfile.delete();
		}
        dbg("--------------------------");
        return info;
    }
    
    /***
    Function patchanalysis generates a diff between the bugged and fixed versions and saves them temporarily into files, then reads the files
    ***/
     public static HashMap<String, HashMap<String, String>> patchAnalysis(HashMap<String, String> info, 
    		 Defects4JBuggyFixedEntity buggyEntity)
    {
        dbg("-- Function patchanalysis --");
        
        HashMap<String, HashMap<String, String>> locations = new HashMap<String, HashMap<String, String>>(); 
        HashMap<String, String> lines = new HashMap<String, String>();
        ArrayList<String> read = new ArrayList<String>();
        

        buggyEntity.getBuggyVersion().getWorkDir(true)
		.resolve(buggyEntity.getProject() + "_buggy_" + buggyEntity.getBugID() + ".txt")
		.toAbsolutePath().toString();
		
        for(int j = 1; j <= 50; j++)
        {   
            if(info.get("fixlocations"+j) != null)
            {
                Path dir_bug = buggyEntity.getBuggyVersion().getWorkDir(true).toAbsolutePath()
                		.resolve(buggyEntity.getBuggyVersion().getMainSourceDir(true));
                Path dir_fix = buggyEntity.getFixedVersion().getWorkDir(true).toAbsolutePath()
                		.resolve(buggyEntity.getFixedVersion().getMainSourceDir(true));

                String searchedfile = info.get("fixlocations"+j);
//                int slash = searchedfile.lastIndexOf("/");
//                String filey = searchedfile.substring(slash+1);
//                List<File> fi_bug = search(dir_bug, filey);
//                List<File> fi_fix = search(dir_fix, filey);
                
                // for extracting the changes, copy the changed files for easier access...
                File bugFile = dir_bug.resolve(searchedfile).toFile();
                File fixFile = dir_fix.resolve(searchedfile).toFile();
                Path outputDirBug = Paths.get("bugDiagnosis", "diffFiles", "buggy", buggyEntity.getUniqueIdentifier());
                Path outputDirFix = Paths.get("bugDiagnosis", "diffFiles", "fixed", buggyEntity.getUniqueIdentifier());
                try {
                	outputDirBug.toFile().mkdirs();
                	outputDirFix.toFile().mkdirs();
                	FileUtils.copyFileOrDir(bugFile, outputDirBug.resolve(bugFile.getName()).toFile(), 
                			StandardCopyOption.REPLACE_EXISTING);
                	FileUtils.copyFileOrDir(fixFile, outputDirFix.resolve(fixFile.getName()).toFile(), 
                			StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                	// TODO Auto-generated catch block
                	e.printStackTrace();
                }
                
                String filename = buggyEntity.getBuggyVersion().getWorkDir(true)
                		.resolve(buggyEntity.getProject() + "_patch_bug_" + buggyEntity.getBugID() + ".txt").toString();
                dbg("Filename = " + filename);
        
                File myfile = new File(filename);
                ProcessBuilder builder;
                Process process;
                try {
//                	List<File[]> matches = fuzzy_diff(fi_fix, fi_bug);
//                	String buggy = matches.get(0)[1].getCanonicalPath();
//                	String fixy = matches.get(0)[0].getCanonicalPath();

					builder = new ProcessBuilder("diff", 
							dir_bug.resolve(searchedfile).toString(), 
							dir_fix.resolve(searchedfile).toString());
					builder.directory(buggyEntity.getBuggyVersion().getWorkDir(true).toFile());
					
					process = builder.start();
				} catch (IOException e) {
					throw new IllegalStateException(e);
				}

				try (InputStream input = new BufferedInputStream(process.getInputStream());
						PrintStream out = new PrintStream(new FileOutputStream(myfile))) {

					int counter;
					int buffer_len=10000;
					byte[] buffer = new byte[buffer_len];

					while((counter = input.read(buffer)) != -1 && counter < buffer_len)
					{
						out.write(buffer,0,counter);
						counter=input.read(buffer);
					}

					read =  readfile(filename);
					dbg("Array = " + Arrays.toString(read.toArray()));
					lines =  searcharrayPatch(read);
					locations.put("fixlocations"+j,lines);
					dbg("Range = " + locations.get("fixlocations"+j).get("range"));
					out.flush();
				} catch (IOException e) {
					throw new IllegalStateException(e);
				} finally {
					process.destroy();   
					myfile.delete();
				}
            }
            else
            {
                break;
            }
        }
        
        dbg("--------------------------");
        return locations;
    }
    /***
    Function readfile reads lines to split file into reasonable chunks and put them in a list for searching
    ***/
    public static ArrayList<String> readfile(String file) throws IOException
    {
        dbg("-- Function readfile --");
        
        ArrayList<String> linelist = new ArrayList<String>(); 
        FileReader reader = new FileReader(file);
        BufferedReader buffer = new BufferedReader(reader);
        
        String zeile = "";
        
        while ((zeile = buffer.readLine()) != null)
        {
            String[] singlelinearray = zeile.split(":");
            
            for(int j = 0; j < singlelinearray.length; j++)
            {
                linelist.add(singlelinearray[j].trim()); 
            }
        }
        
        dbg("Array = " + Arrays.toString(linelist.toArray()));

        buffer.close();
        dbg("--------------------------");
        return linelist;
    }
    
    /***
    Function searcharray to map data from info-files to keywords 
    ***/
    public static HashMap<String, String> searcharrayInfo(ArrayList<String> splitted) 
    {
        dbg("-- Function searcharray --");
        
        HashMap<String, String> items = new HashMap<String, String>();
        int cnt = 1;
        int cnt2 = 1;
        int cnt6 = 1;

        for(int m = 0; m < splitted.size(); m++)
        {
        	if(splitted.get(m) != null)
        	{

        		if(splitted.get(m).equals("Project ID"))
        		{
        			items.put("projectname", splitted.get(m+1));
        		}

        		if(splitted.get(m).equals("Summary for Bug"))
        		{
        			items.put("bugname", splitted.get(m+1));
        		}

        		if(splitted.get(m).equals("Root cause in triggering tests"))
        		{
        			String[] temp = new String[50];
        			for(int q = 1; q < 40; q++)
        			{
        				if(splitted.get(m+q).equals("--------------------------------------------------------------------------------"))
        				{
        					break;
        				}

        				else
        				{
        					temp[q] = splitted.get(m+q);
        					dbg("Line = " + splitted.get(m+q)); 
        				}
        			}

        			for(int w = 1; w < temp.length; w++)
        			{
        				if(temp[w] != null && temp[w].length() >= 2)
        				{
        					dbg(temp[w]); 
        					String sub = temp[w].substring(0,2);
        					dbg(sub);
        					if(sub.equals("- "))
        					{
        						String raw = temp[w].substring(2);
        						raw = raw.replace(".","/").concat(".java");
        						//                                     int string_split = raw.lastIndexOf("/");
        						//                                     String base = raw.substring(0, string_split);
        						//                                     String file_name = raw.substring(string_split+1);
        						//                                     File file_path = new File(path+"_buggy_"+items.get("bugname"));

        						//                                     File real_name = try_and_find(file_path, file_name);
        						//                                     if(real_name == null)
        						//                                     {
        						items.put("buglocations"+cnt,raw);
        						//                                         cnt++;
        						//                                     }
        						//                                     else
        						//                                     {
        						//                                         String blub = base + "/" + real_name.getName();
        						//                                         items.put("buglocations"+cnt, blub);
        						//                                         dbg("buglocations"+cnt+" = " +items.get("buglocations"+w)); 
        						//                                         cnt++;
        						//                                     }
        					}

        					if(sub.equals("--"))
        					{
        						String cause = "";
        						String subsub = "";


        						for(int k = 0; temp[w+k] != null; k++)
        						{
        							dbg("#"+ temp[w+k] + "#");
        							if(temp[w+k].length() >= 3 )
        							{
        								subsub = temp[w+k].substring(0,2);
        							}
        							if(!temp[w+k].equals("--------------------------------------------------------------------------------") || subsub.equals("-->"))
        							{
        								cause = cause+":"+temp[w+k];
        								dbg("Cause = "+cause);
        							}
        							else
        							{
        								break;
        							}
        						}
        						String excp = cause.substring(5);

        						items.put("exception"+cnt6, excp);
        						dbg("Exception = " + items.get("exception"+cnt6));
        						cnt6++;


        					}
        				}  
        			}


        		}

        		if(splitted.get(m).equals("List of modified sources"))
        		{
        			String[] temp = new String[50];
        			for(int q = 1; q < 40; q++)
        			{
        				if(splitted.get(m+q).equals("--------------------------------------------------------------------------------"))
        				{
        					break;
        				}

        				else
        				{
        					temp[q] = splitted.get(m+q);
        					dbg("Line = " + splitted.get(m+q)); 
        				}
        			}

        			for(int w = 1; w < temp.length; w++)
        			{
        				if(temp[w] != null && temp[w].length() >= 2)
        				{
        					dbg("Element = "+temp[w]);
        					String sub = temp[w].substring(0,2);
        					dbg("Substring = " +sub);
        					if(sub.equals("- "))
        					{

        						items.put("fixlocations"+cnt2, temp[w].substring(2).replace(".","/").concat(".java"));
        						dbg("fixlocations"+cnt2+" = " +items.get("fixlocations"+w));
        						cnt2++;
        					}
        				}
        			}
        		}
        	}
        }

        dbg("--------------------------");
        return items;
    }
    
    /***
    Function searcharray to map data from diff-files to keywords 
    ***/
    public static HashMap<String, String> searcharrayPatch(ArrayList<String> diffFileLines) 
    {
        dbg("-- Function searcharray --");
        
        HashMap<String, String> items = new HashMap<String, String>();

        String regex = "(\\d+\\p{Punct}*\\d*)([acd])(\\d+\\p{Punct}*\\d*)";
        Pattern r = Pattern.compile(regex);
        String[] lines = new String[500];
        int cnt3 = 1;
        int cnt4 = 1;
        int cnt5 = 1;

        for(int n = 0; n < diffFileLines.size(); n++)
        {   
        	Matcher m = r.matcher(diffFileLines.get(n));
        	if(m.find())
        	{
        		String[] split = m.group(1).split("\\p{Punct}+", 0);
        		if(m.group(2).equals("a"))
        		{
        			dbg("Insert = " + m.group(1));
        			items.put("insert"+cnt3,(Integer.valueOf(split[0]) + 1) + ":" + getNumberOfLines(m.group(3).split("\\p{Punct}+", 0)));
        			cnt3++;
        		}
        		if(m.group(2).equals("c"))
        		{
        			dbg("change = " + m.group(1));
        			items.put("change"+cnt4,getLineOrRange(split));
        			cnt4++;
        		}
        		if(m.group(2).equals("d"))
        		{
        			dbg("delete = " + m.group(1));
        			items.put("delete"+cnt5,getLineOrRange(split));
        			cnt5++;
        		}

        		lines[n] = m.group(1);
        	}
        }
        dbg("Lines =");
        dbg(Arrays.toString(lines));

        if(lines[0] != null)
        {
        	String[] split = lines[0].split("\\p{Punct}+", 0);
        	int highest = Integer.parseInt(split.length > 1 ? split[1] : split[0]);
        	int lowest = Integer.parseInt(split[0]);

        	String range = "";

        	for(int v = 1; v < lines.length; v++)
        	{
        		if(lines[v] != null)
        		{            
        			split = lines[v].split("\\p{Punct}+", 0);
        			if(Integer.parseInt(split.length > 1 ? split[1] : split[0]) > highest)
        			{
        				highest = Integer.parseInt(split.length > 1 ? split[1] : split[0]);
        			}
        			if(Integer.parseInt(split[0]) < lowest)
        			{
        				lowest = Integer.parseInt(split[0]);
        			}   
        		}

        	}

        	dbg("Highest = "+highest);
        	dbg("Lowest = " +lowest);

        	if(highest != lowest)
        	{
        		range = Integer.toString(lowest) + "-" + Integer.toString(highest);
        	}
        	if(highest == lowest)
        	{
        		range = Integer.toString(lowest);
        	}
        	items.put("range", range);
        	dbg("Range = " + items.get("range"));
        }

        dbg("--------------------------");
        return items;
    }

    
    private static int getNumberOfLines(String[] split) {
    	if (split.length == 1) {
    		return 1;
    	} else {
    		return Integer.valueOf(split[1]) - Integer.valueOf(split[0]) + 1;
		}
	}


	private static String getLineOrRange(String[] split) {
    	if (split.length == 1) {
    		return split[0];
    	} else {
    		return split[0] + "-" + split[1];
		}
	}


	/***
    Function printXML creates the XML File and puts data in it
    ***/
    public static void printXML(HashMap<String, String> info, 
    		HashMap<String, HashMap<String, String>> patches, 
    		Defects4JBuggyFixedEntity buggyEntity)
    {
        dbg("-- Function printXML --");
        
        try
        {
                //create Document
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.newDocument();
            
                //benchmarkid
                Element root = doc.createElement("defects4j"); 
                doc.appendChild(root);
            
                //projectid
                Element project = doc.createElement("project");
                root.appendChild(project);

                Attr projectid = doc.createAttribute("projectid");
                projectid.setValue(info.get("projectname"));
                project.setAttributeNode(projectid);

                //bugid
                Element bug = doc.createElement("bug");
                project.appendChild(bug);
            
                Attr bugid = doc.createAttribute("bugid");
                bugid.setValue(info.get("bugname"));
                bug.setAttributeNode(bugid);

                //buglocations
                Element buglocations = doc.createElement("tests");
                bug.appendChild(buglocations);

                for(int l = 1; ; l++) {
                	dbg("buglocations"+l+" = "+info.get("buglocations"+l));

                	if(info.get("buglocations"+l) != null) {

                		if(!info.get("buglocations"+l).equals(info.get("buglocations"+(l+1)))) {
                			Element buglocfiles = doc.createElement("testfile");
                			buglocations.appendChild(buglocfiles);

                			Attr bugpath = doc.createAttribute("path");
                			bugpath.setValue(info.get("buglocations"+l));
                			buglocfiles.setAttributeNode(bugpath);

                			Element bugexception = doc.createElement("exception");
                			bugexception.appendChild(doc.createTextNode(info.get("exception"+l)));
                			buglocfiles.appendChild(bugexception);

                			for(int y = 1; y <= 40; y++) {
                				if(info.get("fixlocations"+y) != null && patches.get("fixlocations"+y).get("range") != null) {
                					dbg("Buglocation = "+info.get("buglocations"+l));
                					dbg("Fixlocation = "+info.get("fixlocations"+y));

                					if(info.get("fixlocations"+y).equals(info.get("buglocations"+l))) {
                						Element range = doc.createElement("range");
                						range.appendChild(doc.createTextNode(patches.get("fixlocations"+y).get("range")));
                						buglocfiles.appendChild(range);
                						break;
                					} 
//                					else {
//                						break;
//                					}
                				} else {
                					break;
                				}
                			}
                			//                     
                			//                             Element faultrecognition = doc.createElement("faultrecognition");
                			//                             faultrecognition.appendChild(doc.createTextNode("medium"));
                			//                     
                			//                             bugloclines.appendChild(faultrecognition);
                			//         
                			//                             buglocfiles.appendChild(bugloclines);
                		}
                	} else {
                		break;
                	}
                }

                //fixlocations
                Element fixlocations = doc.createElement("fixlocations");
                bug.appendChild(fixlocations);
                
                Comment comment = doc.createComment("Modifications can be changes, deletes or inserts. Each separate code element should get its own entry. If multiple lines belong to the same modification, they should be put in the same entry, divided by commas.");
                fixlocations.appendChild(comment);
                
                for(int z = 1; ; z++) {
                    dbg("fixlocations"+z+" = "+info.get("fixlocations"+z)); 
                
                    if(info.get("fixlocations"+z) != null) {
                        if(!info.get("fixlocations"+z).equals(info.get("fixlocations"+(z+1)))) {
                            Element fixlocfiles = doc.createElement("file");
                            fixlocations.appendChild(fixlocfiles);
            
                            Attr fixpath = doc.createAttribute("path");
                            fixpath.setValue(info.get("fixlocations"+z));
                            fixlocfiles.setAttributeNode(fixpath);
            
                            List<Pair<Integer, Element>> nodes = new ArrayList<>();
                            for(int ch = 1; ; ch++) {
                                if(patches.get("fixlocations"+z).get("change"+ch) != null) {
                                    Element change = doc.createElement("change");
                                    Attr parentStatements = doc.createAttribute("parent");
                                    parentStatements.setValue(patches.get("fixlocations"+z).get("change"+ch));
                                    change.setAttributeNode(parentStatements);
                                    change.appendChild(doc.createTextNode(patches.get("fixlocations"+z).get("change"+ch)));
                                    nodes.add(new Pair<>(Integer.valueOf(patches.get("fixlocations"+z).get("change"+ch).split("-",0)[0]), change));
                                } else {
                                    break;
                                }
                            }
                            for(int de = 1; ; de++) {
                                if(patches.get("fixlocations"+z).get("delete"+de) != null) {
                                    Element delete = doc.createElement("delete");
                                    delete.appendChild(doc.createTextNode(patches.get("fixlocations"+z).get("delete"+de)));
                                    nodes.add(new Pair<>(Integer.valueOf(patches.get("fixlocations"+z).get("delete"+de).split("-",0)[0]), delete));
                                } else {
                                    break;
                                }
                            }
                            for(int in = 1; ; in++) {
                                if(patches.get("fixlocations"+z).get("insert"+in) != null) {   
                                    Element insert = doc.createElement("insert");
                                    String[] split = patches.get("fixlocations"+z).get("insert"+in).split(":", 0);
                                    insert.appendChild(doc.createTextNode(split[0]));

                                    Attr lines = doc.createAttribute("numberlines");
                                    lines.setValue(split[1]);
                                    insert.setAttributeNode(lines);
                                    
                                    nodes.add(new Pair<>(Integer.valueOf(split[0]), insert));
                                } else {
                                    break;
                                }
                            }
                            
                            nodes.sort((k,l) -> Integer.compare(k.first(), l.first()));
                            
                            for (Pair<Integer, Element> node : nodes) {
                            	fixlocfiles.appendChild(node.second());
                            }
                            
                            Element bugtypes = doc.createElement("bugtypes");
                            fixlocfiles.appendChild(bugtypes);
                        
                            Element bugtypeid = doc.createElement("id");
                        
                        
                            Attr bugtypeno = doc.createAttribute("id");
                            bugtypeno.setValue("");
                            bugtypeid.setAttributeNode(bugtypeno);
                            
                            Attr bugtypenoline = doc.createAttribute("lines");
                            bugtypenoline.setValue("");
                            bugtypeid.setAttributeNode(bugtypenoline);
                        
                            Element description = doc.createElement("description");
                            description.appendChild(doc.createTextNode(" "));
                            bugtypeid.appendChild(description);

                            bugtypes.appendChild(bugtypeid);
                        }
                    } else {
                        break;
                    }
                }

                //gaps
                Element numberfixedstatements = doc.createElement("numberfixedlines");
                numberfixedstatements.appendChild(doc.createTextNode(" "));
                bug.appendChild(numberfixedstatements);
            
                
                        
                //create XML file
                TransformerFactory tfactory = TransformerFactory.newInstance();
                Transformer t = tfactory.newTransformer();
                DOMSource source = new DOMSource(doc);
                Path outputDir = Paths.get("bugDiagnosis", "d4j-faults", buggyEntity.getProject());
                outputDir.toFile().mkdirs();
                StreamResult result = new StreamResult(outputDir
                		.resolve("bugdiagnosis_"+buggyEntity.getProject()+"-"+buggyEntity.getBugID()+".xml").toString());
            
                t.setOutputProperty(OutputKeys.INDENT, "yes");
                t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

                t.transform(source, result);
                
                dbg("--------------------------");
                dbg("#################################");
        } 
        
        catch (ParserConfigurationException pce) 
        {
            pce.printStackTrace();
        } 
        
        catch (TransformerException te) 
        {
            te.printStackTrace();
        }
        
    }
    
    /***
    Function dbg prints out all debug messages
    ***/
    public static void dbg(String message)
    {
        if(global_level != 0)
        {
            System.out.println(message);
        }
    }

}

