package oldstuff.eu.transcriptorium.jafar.disagree.DutchData;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class Preprocessing {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
			File dir = new File("/mnt/Projecten/transcriptorium/Data/Corpora/Dutch/DBNL_artes-gemarkeerd/");
	        String[] children = dir.list();
	        String InputList="/mnt/Projecten/transcriptorium/Data/Corpora/Dutch/DBNL_artes-gemarkeerd/";
	        
	       File dirCleaned= new File("/mnt/Projecten/transcriptorium/Data/Corpora/Dutch/CleanedMiddleDuch/");
	        String[] childrenCleaned=dirCleaned.list();
	        String OutputCleanedFiles="/mnt/Projecten/transcriptorium/Data/Corpora/Dutch/CleanedMiddleDuch/";
	  /*      File dirNormalized= new File(args[3]);
	        String[] childrenNormalized=dirCleaned.list();*/
	        System.out.println("children======================================"+children.length+ " "+dir.getPath());
	        
	        for(int i=0; i<children.length; i++)
	        	System.out.println(children[i]);
	        
	        if((children==null)|| (childrenCleaned==null)){
	        	System.out.println("The folder does not exist");
	       // 	System.out.println("dir="+dir.getPath()+"\n cleaned=="+dirCleaned.getPath()+"\n dirNormalized==="+dirNormalized.getPath());
	        	
	        }
	        else{
	        
	                        
	            CleanedAndNormalizedText(children,OutputCleanedFiles,InputList);
	        
	        }
	        
	        System.out.println("           ********                 End of the process!!!                     ***********");
	        			
		
						
	}

	private static void CleanedAndNormalizedText(String[] children,
			String OutputCleanedFiles, String InputList) throws IOException {
		// TODO Auto-generated method stub
		
    //   String OutputCleaned=OutputCleanedFiles;
       File dir = new File(OutputCleanedFiles);

       
       System.out.println("children.length================"+children.length);
       for (int i=0; i<children.length; i++) {
             // Get filename of file or directory
             String filename = InputList+children[i];
             String outFile=OutputCleanedFiles+children[i]+".cl";
             System.out.println("filename============= "+filename);
             RunningCleaningCode(filename, outFile);
    	 }
       
              
	}

	private static void RunningCleaningCode(String filename, String outFile) throws IOException {
		// TODO Auto-generated method stub
		
        PrintWriter output= new PrintWriter(new BufferedWriter(new FileWriter(outFile)));
		
		
			
			InputStream is = new FileInputStream (filename);
			String UTF8="utf8";
			
			BufferedReader br= new BufferedReader(new InputStreamReader(is,UTF8));
			String Begin="BEGIN MNLSE TEKST";
			String End="EINDE MNLSE TEKST";
			
			String line=br.readLine();
			while(line!=null){
				System.out.println(line);

				if(line.contains(Begin)){
					System.out.println(line);
					line=br.readLine();
					System.out.println(line);

				//	line.con
					while(line.contains(End) !=true){
						System.out.println("Cleaned text================================== "+line);
						output.println(line);
						line=br.readLine();

					}
						
				}
			
			//	System.out.println(line);
				line=br.readLine();
			}
			br.close();
		/*	File rm = new File(st);
			 
    		if(rm.delete()){
    			System.out.println(rm.getName() + " is deleted!");
    		}*/ // for now do not remove the files
				
		
		output.flush();
		output.close();
		System.out.println(" The file has been created in ");

		
	}

	
}
