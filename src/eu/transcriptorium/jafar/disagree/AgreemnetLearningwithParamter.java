package eu.transcriptorium.jafar.disagree;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;


public class AgreemnetLearningwithParamter {

	
	
	private static final int Percent = 1;
	private static final int Iterations = 20;
	private static  double Thresholds = 0.35;
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		if(args==null)
		{
			System.out.println("Please identify the parameters!");
			System.out.println("Use AgreeCo.jar SelectionPercent NumberIterations Confidence InDomainF OutDominF \n" +
					" ECCONormalizedDomainF ECCOCleanedDomainF OutputInF OutputOutF NameofSelectedSamples.txt TypeOfCriterion Requiredprogramfolder");
			System.out.println("where " +
					"\n - SelectionPercent: is the percentage that the algorithm selects at each iteration." +
					"\n - NumberIterations: is the number of Iterations." +
					"\n - Confidence: is a thershold for confidence. " +
					"\n - InDomainF: is the folder of In domain LM and Data. " +
					"\n - OutDominF: is the folder of Out domain LM and Data." +
					"\n - ECCONormalizedDomainF: is the folder of ECCO Normlaizd data." +
					"\n - ECCOCleanedDomainF: is the folder of ECCO Cleaned data. " +
					"\n - OutputInF: is the output of in-domain LM. " +
					"\n - OutputOutF: is the output of out-domain LM. " +
					"\n - NameofSelectedSamples: the resultin sample selection file." +
					"\n - TypeOfCriterion: Select the type of criterion 1- for adding PPL + OOVs, 2- PPL*OOVs, 3- (1/PPL)*(1-OOVs). " +
					"\n - Requiredprogramfolder: is the folder of required .sh files for LM bulding like testPPLJava.sh trainingLMJava.sh." );
		}
		else if(args.length<12){
			System.out.println("You have used wrong parameters!");
			System.out.println("Use AgreeCo.jar SelectionPercent NumberIterations Confidence InDomainF OutDominF \n " +
					" ECCONormalizedDomainF ECCOCleanedDomainF OutputInF OutputOutF NameofSelectedSamples.txt TypeOfCriterion Requiredprogramfolder");
			System.out.println("where " +
					"\n - SelectionPercent: is the percentage that the algorithm selects at each iteration." +
					"\n - NumberIterations: is the number of Iterations." +
					"\n - Confidence: is a thershold for confidence. " +
					"\n - InDomainF: is the folder of In domain LM and Data. " +
					"\n - OutDominF: is the folder of Out domain LM and Data." +
					"\n - ECCONormalizedDomainF: is the folder of ECCO Normlaizd data." +
					"\n - ECCOCleanedDomainF: is the folder of ECCO Cleaned data. " +
					"\n - OutputInF: is the output of in-domain LM. " +
					"\n - OutputOutF: is the output of out-domain LM. " +
					"\n - NameofSelectedSamples: the resultin sample selection file." +
					"\n - TypeOfCriterion: Select the type of criterion 1- for adding PPL + OOVs, 2- PPL*OOVs, 3- (1/PPL)*(1-OOVs). " +
					"\n - Requiredprogramfolder: is the folder of required .sh files for LM bulding like testPPLJava.sh trainingLMJava.sh." );

		}
		else if(args.length==12){
			int Iter=0;
		    double T=0;
			while( Iter<Integer.parseInt(args[1]) && T<Double.parseDouble(args[2]) ){
				
				LanguageBuliding LmIn=new LanguageBuliding();
				
				LmIn.Output=args[3]; 
				LmIn.Cutoff="0"; LmIn.Requiredprogramfolder=args[11];
				
	            LanguageBuliding LmOut=new LanguageBuliding();
				
	            LmOut.Output=args[4];
	            LmOut.Cutoff="0"; LmOut.Requiredprogramfolder=args[11];
	            LmIn.start(); LmOut.start();
	            
	            try {
					LmIn.join();
					LmOut.join();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            
	            File  dir = new File(args[5]);
	            String[] children = dir.list();
	            children=dir.list();
	            
	            if (children == null) {
	                   System.out.println("The directory does not exist!" );   
	            } else {
	                String Model=args[3];
	                
	                TestLanguageModel T1=new TestLanguageModel();
	                TestLanguageModel T2=new TestLanguageModel();
	                TestLanguageModel T3=new TestLanguageModel();
	                TestLanguageModel T4=new TestLanguageModel();
	                
	                TestLanguageModel OutT1=new TestLanguageModel();
	                TestLanguageModel OutT2=new TestLanguageModel();
	                TestLanguageModel OutT3=new TestLanguageModel();
	                TestLanguageModel OutT4=new TestLanguageModel();
	                TestLanguageModel OutT5=new TestLanguageModel();
	                TestLanguageModel OutT6=new TestLanguageModel();
	                TestLanguageModel OutT7=new TestLanguageModel();
	                TestLanguageModel OutT8=new TestLanguageModel();
	                
	                
	                System.out.println("children.length= "+children.length+" (int)children.length/4 ="+((int)children.length/4 +1)+" (int) children.length/2 "+(int) children.length/2+" (int) (3*children.length)/4"+(int) (3*children.length)/4);

	                T1.firstIndex=0;T1.lastIndex=(int) children.length/4; T1.FileList=children;
	                T1.Model=Model;T1.Output=args[7];
	                T1.OutputType="ppl_in_"; T1.ResourceFolder=args[5]; T1.Requiredprogramfolder=args[11];
	                
	                
	                T2.firstIndex= (int)children.length/4 +1;T2.lastIndex=(int) children.length/2; T2.FileList=children;
	                T2.Model=Model;T2.Output=args[7];
	                T2.OutputType="ppl_in_"; T2.ResourceFolder=args[5]; T2.Requiredprogramfolder=args[11];

	                T3.firstIndex= (int)children.length/2 +1;T3.lastIndex=(int) (3*children.length)/4;T3.FileList=children;
	                T3.Model=Model;T3.Output=args[7];
	                T3.OutputType="ppl_in_"; T3.ResourceFolder=args[5]; T3.Requiredprogramfolder=args[11];

	                T4.firstIndex= (int)(3*children.length)/4 +1;T4.lastIndex=(int) children.length-1;T4.FileList=children;
	                T4.Model=Model;T4.Output=args[7];
	                T4.OutputType="ppl_in_"; T4.ResourceFolder=args[5]; T4.Requiredprogramfolder=args[11]; 

	               
	                Model=args[4];
	                OutT1.firstIndex=0;OutT1.lastIndex=(int) children.length/8; OutT1.FileList=children;
	                OutT1.Model=Model;OutT1.Output=args[8];
	                OutT1.OutputType="ppl_out_"; OutT1.ResourceFolder=args[5]; OutT1.Requiredprogramfolder=args[11];
	                 
	                OutT2.firstIndex= (int)children.length/8 +1;OutT2.lastIndex=(int) children.length/4; OutT2.FileList=children;
	                OutT2.Model=Model;OutT2.Output=args[8];
	                OutT2.OutputType="ppl_out_"; OutT2.ResourceFolder=args[5]; OutT2.Requiredprogramfolder=args[11];

	                OutT3.firstIndex= (int)children.length/4 +1;OutT3.lastIndex=(int) (3*children.length)/8;OutT3.FileList=children;
	                OutT3.Model=Model;OutT3.Output=args[8];
	                OutT3.OutputType="ppl_out_"; OutT3.ResourceFolder=args[5]; OutT3.Requiredprogramfolder=args[11];

	                OutT4.firstIndex= (int)(3*children.length)/8 +1;OutT4.lastIndex=(int)(children.length)/2;OutT4.FileList=children;
	                OutT4.Model=Model;OutT4.Output=args[8];
	                OutT4.OutputType="ppl_out_"; OutT4.ResourceFolder=args[5]; OutT4.Requiredprogramfolder=args[11];
	                
	                OutT5.firstIndex=(int)(children.length)/2 +1;OutT5.lastIndex=(int) (5*children.length)/8; OutT5.FileList=children;
	                OutT5.Model=Model;OutT5.Output=args[8];
	                OutT5.OutputType="ppl_out_"; OutT5.ResourceFolder=args[5]; OutT5.Requiredprogramfolder=args[11];
	                 
	                OutT6.firstIndex= (5*children.length)/8 +1;OutT6.lastIndex=(int) (6*children.length)/8; OutT6.FileList=children;
	                OutT6.Model=Model;OutT6.Output=args[8];
	                OutT6.OutputType="ppl_out_"; OutT6.ResourceFolder=args[5]; OutT6.Requiredprogramfolder=args[11];

	                OutT7.firstIndex= (int)(6*children.length)/8 +1;OutT7.lastIndex=(int) (7*children.length)/8;OutT7.FileList=children;
	                OutT7.Model=Model;OutT7.Output=args[8];
	                OutT7.OutputType="ppl_out_"; OutT7.ResourceFolder=args[5]; OutT7.Requiredprogramfolder=args[11];

	                OutT8.firstIndex= (int)(7*children.length)/8 +1;OutT8.lastIndex=(int) children.length-1;OutT8.FileList=children;
	                OutT8.Model=Model;OutT8.Output=args[8];
	                OutT8.OutputType="ppl_out_"; OutT8.ResourceFolder=args[5]; OutT8.Requiredprogramfolder=args[11];

	                T1.start(); T2.start(); T3.start(); T4.start(); OutT1.start(); OutT2.start(); OutT3.start(); OutT4.start();
	                OutT5.start(); OutT6.start(); OutT7.start(); OutT8.start();
	                
	                try {
	                	T1.join();T2.join();T3.join();T4.join();OutT1.join();OutT2.join();OutT3.join();OutT4.join();
	                	OutT5.join();OutT6.join();OutT7.join();OutT8.join();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

	       
	            }
				
				dir = new File(args[8]);
			//	System.out.println("\n\n  Out domain correlation \n");
		        String inputpath=args[8];
		        ArrayList<String> Outppl1 = new ArrayList<String> ();
		        ArrayList<String> OutFileNames = new ArrayList<String> ();
				
		        Outppl1=CorrelationFunction(dir, inputpath,OutFileNames,Integer.parseInt(args[10]));
					
				ArrayList<String> RelatedFilesNameOut= new ArrayList<String>();
				ArrayList<String> OutRanks= new ArrayList<String>();
				
				OutRanks=SortedRanks(Outppl1, RelatedFilesNameOut);
				
					
		//
		//		System.out.println("\n\n  in domain correlation \n");
		        dir = new File(args[7]);
		        inputpath=args[7];
		        ArrayList<String> Inppl1 = new ArrayList<String> ();
		        ArrayList<String> InFileNames = new ArrayList<String> ();

				Inppl1=CorrelationFunction(dir, inputpath,InFileNames,Integer.parseInt(args[10]) );
						
				ArrayList<String> RelatedFilesNameIn= new ArrayList<String>();
				ArrayList<String> InRanks= new ArrayList<String>();
				
				InRanks=SortedRanks(Inppl1, RelatedFilesNameIn);
				
			/*	System.out.println("RelatedFilesNameIn=============================================");
				for(int i=0; i<RelatedFilesNameIn.size();i++)
					System.out.println(RelatedFilesNameIn.get(i));*/
			
				int Threshould=(int)(Inppl1.size()*Integer.parseInt(args[0]))/100;
				
				T=(Double.parseDouble( InRanks.get(Threshould-1)) + Double.parseDouble(OutRanks.get(Threshould-1)))/2  ;
				
				System.out.println("                    Thershold= "+T);
				
				ArrayList<String> ConfidentOutFileName= new ArrayList<String>();
				ArrayList<String> ResultingFileName= new ArrayList<String>();

				for(int i=0;i<Threshould;i++){
					ConfidentOutFileName.add(RelatedFilesNameOut.get(i));
			//		System.out.println("Out ranks "+InRanks.get(i)+"Out domain = "+RelatedFilesNameOut.get(i)+ "  In ranks "+OutRanks.get(i)+" In domain= "+RelatedFilesNameIn.get(i) );
				}
				
				String PathIn,PathOut;
				PathIn=args[7];
				PathOut=args[8];
				for(int i=0;i<Threshould;i++){
			//		System.out.println("Existance InRanks === "+InRanks.get(i)+"  OuRanks== "+OutRanks.get(i));
			//		System.out.println("\n General ConfidentOutFileName(j)==  "+ConfidentOutFileName.get(i)+ " RelatedFilesNameIn(i)== "+RelatedFilesNameIn.get(i)+"\n");


					if(ConfidentOutFileName.contains(RelatedFilesNameIn.get(i))){
						//int j=ConfidentOutFileName.indexOf(RelatedFilesNameIn.get(i));
					//	System.out.println("\n After selection ConfidentOutFileName(j)==  "+RelatedFilesNameIn.get(i)+ " RelatedFilesNameIn(i)== "+RelatedFilesNameIn.get(i)+"\n");
						ResultingFileName.add(RelatedFilesNameIn.get(i));
			//			System.out.println("Selected  InRanks === "+InRanks.get(i)+"  OuRanks== "+OutRanks.get(i));
						RemoveFromPPLFiles(PathIn,PathOut,RelatedFilesNameIn.get(i));
					}
						
				}
			
				
				String OutputTextFile="SeletedNormalizedText.txt" ;

				RetrivedBestMaches(ResultingFileName,OutputTextFile);
			 
				String OutputTextFileOriginal="SeletedTextOriginalCleaned.txt" ;

				RetrivedBestMachesCleanedText(ResultingFileName,OutputTextFileOriginal,args[6], args[9]);  
				
	            String InTraing=args[3]+"/";
	            String NewCleanedTrainingSet=OutputTextFileOriginal;
	            String NewNormalizedTrainingSet=OutputTextFile;
				AddToTrainingSet(InTraing,NewCleanedTrainingSet,NewNormalizedTrainingSet);
	            
				String OutTraing= args[4]+"/";
	            AddToTrainingSet(OutTraing,NewCleanedTrainingSet,NewNormalizedTrainingSet);
	           
	            RemoveFileofSelectedText(OutputTextFile,OutputTextFileOriginal);
				
	            RemoveCleanedTextFilesFromCorpora(ResultingFileName,args[6]);
				
				RemoveNormalizedTextFilesFromCorpora(ResultingFileName);
				
	            
	            Iter++;
	            System.out.println(" \n\n =========================== Iteration============== "+Iter);

			}
			
			
		}
		else {
			System.out.println("Wrong  number of parameters!");
			System.out.println("Use AgreeCo.jar SelectionPercent NumberIterations Confidence InDomainF OutDominF \n" +
					" ECCONormalizedDomainF ECCOCleanedDomainF OutputInF OutputOutF NameofSelectedSamples.txt TypeOfCriterion Requiredprogramfolder");
			System.out.println("where \n" +
					"- SelectionPercent: is the percentage that the algorithm selects at each iteration." +
					" \n - NumberIterations: is the number of Iterations." +
					" \n - Confidence: is a thershold for confidence. " +
					"\n - InDomainF: is the folder of In domain LM and Data. " +
					"\n - OutDominF: is the folder of Out domain LM and Data." +
					"\n - ECCONormalizedDomainF: is the folder of ECCO Normlaizd data." +
					"\n - ECCOCleanedDomainF: is the folder of ECCO Cleaned data. " +
					"\n - OutputInF: is the output of in-domain LM. " +
					"\n - OutputOutF: is the output of out-domain LM. " +
					"\n - NameofSelectedSamples: the resultin sample selection file." +
					"\n - TypeOfCriterion: Select the type of criterion 1- for adding PPL + OOVs, 2- PPL*OOVs, 3- (1/PPL)*(1-OOVs). \n" +
					"- Requiredprogramfolder: is the folder of required .sh files for LM bulding like testPPLJava.sh trainingLMJava.sh." );

		}
		
		
	}
	
	
	
	
	
	private static void RemoveNormalizedTextFilesFromCorpora(
			ArrayList<String> resultingFileName) {
		// TODO Auto-generated method stub
		for(String st:resultingFileName){
			st=st.substring(0, st.lastIndexOf(":"));
			File rm = new File(st);
			 
    		if(rm.delete()){
    			System.out.println(rm.getName() + " Normalized file is deleted!");
    		}  // for now do not remove the files
			
				
		}
		

		
	}


	private static void RemoveCleanedTextFilesFromCorpora(
			ArrayList<String> resultingFileName, String FilePath) {
		// TODO Auto-generated method stub
		for(String st:resultingFileName){
			st=st.substring(0, st.lastIndexOf(":"));
			st=st.substring(st.lastIndexOf("/"));
			st=st.substring(0, st.lastIndexOf("n")-1);
			st=FilePath+st;
			File rm = new File(st);
			 
    		if(rm.delete()){
    			System.out.println(rm.getName() + " The Cleaned File is deleted!");
    		} // for now do not remove the files
				
		}
		
	}





	private static void RemoveFileofSelectedText(String outputTextFile,
			String outputTextFileOriginal) {
		// TODO Auto-generated method stub
		
		File nrmcleand = new File(outputTextFile);
		if(nrmcleand.delete())
			System.out.println(" The Selected Normalized Text in this iteration has benn removed !!!!!");
		
		File rmcleand = new File(outputTextFileOriginal);
		if(rmcleand.delete())
			System.out.println(" The Selected Cleaned Text in this iteration has benn removed !!!!!");
		
	}





	private static void RemoveFromPPLFiles(String pathIn, String pathOut,
			String FileNameForDelete) {
		// TODO Auto-generated method stub
		
		String st=FileNameForDelete.substring(0, FileNameForDelete.lastIndexOf(":"));
		st=st.substring(st.lastIndexOf("/")+1);
		//st=st.substring(0, st.lastIndexOf("n")-1);
		String Inst=pathIn+"ppl_in_"+st+".log";
//		System.out.println(st);
		String Outst=pathOut+"ppl_out_"+st+".log";

		File Inrm = new File(Inst);
		 
		if(Inrm.delete()){
			System.out.println(Inrm.getName() + " is deleted!");
		}
		else{
			System.out.println("errooooooooooooooooooooooooooooooooooooooooooooooooorrrrrrrrrrr"+Inst);
		}
			
		File Outrm = new File(Outst);
		 
		if(Outrm.delete()){
			System.out.println(Outrm.getName() + " is deleted!");
		}
	}





	public static void AddToTrainingSet(String TraingFolder, String newCleanedTrainingSet, String newNormalizedTrainingSet) throws IOException {
		// TODO Auto-generated method stub
		
		
		/////////////////////  Adding to the Cleaned Training Set
		  File file =new File(TraingFolder+"cleanedText.txt");
			
			if(! file.exists()){
				System.out.println("The file does not exist !!!!!!!!!!!!!!!!");
			//	file.createNewFile();
				
			}
			else{
		        BufferedWriter output = new BufferedWriter(new FileWriter(file,true));
		        
		        
		        InputStream is = new FileInputStream (newCleanedTrainingSet);
				String UTF8="utf8";
				
				BufferedReader br= new BufferedReader(new InputStreamReader(is,UTF8));
				
				String line=br.readLine();
				while(line!=null){
					output.append(line);
					output.newLine();
				//	System.out.println(line);
					line=br.readLine();
				}
				br.close();
				output.flush();
				output.close();
				
			}
			   //////////                Addining to normalized training set
			file =new File(TraingFolder+"normalizedText.txt");
			
			if(! file.exists()){
				System.out.println("The file does not exist !!!!!!!!!!!!!!!!");
			//	file.createNewFile();
				
			}
			else{
		        BufferedWriter output = new BufferedWriter(new FileWriter(file,true));
		        
		        
		        InputStream is = new FileInputStream (newNormalizedTrainingSet);
				String UTF8="utf8";
				
				BufferedReader br= new BufferedReader(new InputStreamReader(is,UTF8));
				
				String line=br.readLine();
				while(line!=null){
					output.append(line);
					output.newLine();
				//	System.out.println(line);
					line=br.readLine();
				}
				br.close();
				output.flush();
				output.close();
				
				
			}
	}





	private static void RetrivedBestMachesCleanedText(		
		ArrayList<String> resultingFileName, String outputTextFile, String FilePath, String args) throws IOException {
		
  
		
        File fileList =new File(args);
		
		if(! fileList.exists()){
			System.out.println("The sampledSelection file has been created!");
			fileList.createNewFile();
			
		}
		
        BufferedWriter outlist = new BufferedWriter(new FileWriter(fileList,true));

	
		PrintWriter output= new PrintWriter(new BufferedWriter(new FileWriter(outputTextFile)));
		
		for(String st:resultingFileName){
			st=st.substring(0, st.lastIndexOf(":"));
			st=st.substring(st.lastIndexOf("/"));
			st=st.substring(0, st.lastIndexOf("n")-1);
			st=FilePath+st;
	//		System.out.println(st);
			
			InputStream is = new FileInputStream (st);
			String UTF8="utf8";
			
			BufferedReader br= new BufferedReader(new InputStreamReader(is,UTF8));
			
			String line=br.readLine();
			while(line!=null){
				output.append(line);
				output.println();
				
				outlist.write(line);
				outlist.newLine();
			//	System.out.println(line);
				line=br.readLine();
			}
			br.close();
		/*	File rm = new File(st);
			 
    		if(rm.delete()){
    			System.out.println(rm.getName() + " is deleted!");
    		}*/ // for now do not remove the files
				
		}
		output.flush();
		output.close();
		outlist.flush();
		outlist.close();
		System.out.println(" The file has been created in ");

		
	}





	private static void RetrivedBestMaches(ArrayList<String> resultingFileName,
			String outputTextFile) throws IOException {
		// TODO Auto-generated method stub
		
	
		PrintWriter output= new PrintWriter(new BufferedWriter(new FileWriter(outputTextFile)));
		
		String listOfFiles="ListofSelectedNormalizedFiles.txt";
		
        File fileList =new File(listOfFiles);
		
		if(! fileList.exists()){
			System.out.println("The file has been created!");
			fileList.createNewFile();
			
		}
		
        BufferedWriter outlist = new BufferedWriter(new FileWriter(fileList,true));

		
		for(String st:resultingFileName){
			st=st.substring(0, st.lastIndexOf(":"));
			String filename=st.substring(st.lastIndexOf("/")+1);
			outlist.write(filename);
			outlist.newLine();
			InputStream is = new FileInputStream (st);
			String UTF8="utf8";
			
			BufferedReader br= new BufferedReader(new InputStreamReader(is,UTF8));
			
			String line=br.readLine();
			while(line!=null){
				output.append(line);
				output.println();
				
			
			//	System.out.println(line);
				line=br.readLine();
			}
			br.close();
		/*	File rm = new File(st);
			 
    		if(rm.delete()){
    			System.out.println(rm.getName() + " is deleted!");
    		}*/  // for now do not remove the files
			
				
		}
		output.flush();
		output.close();
		outlist.flush();
		outlist.close();
		System.out.println(" The file has been created in ");
		
	}





	private static ArrayList<String> SortedRanks(ArrayList<String> ppl1,
			ArrayList<String> relatedFilesName) {
		// TODO Auto-generated method stub
		
		
		ArrayList<String> Ranks= new ArrayList<String>();
		Collections.sort(ppl1);
		for(int i=0; i<ppl1.size();i++){
			String str = ppl1.get(i);
		
		    StringTokenizer st2 = new StringTokenizer(str, ",");
		    
		    Ranks.add((String) st2.nextElement());
		    relatedFilesName.add((String) st2.nextElement());
		    
		 }
		return Ranks;
	}





	public static ArrayList<String> CorrelationFunction(File dir, String inputpath, ArrayList<String> FileNames, Integer TypeOfCriterion) throws IOException {
		// TODO Auto-generated method stub
        
		String[] children = dir.list();
       
        ArrayList<Double> Sent  = new ArrayList<Double> ();
        ArrayList<Double> Logprobs  = new ArrayList<Double> ();
        ArrayList<Double> ppl1 = new ArrayList<Double> ();
        ArrayList<Double> Word  = new ArrayList<Double> ();
        ArrayList<Double> OOVs = new ArrayList<Double> ();
        ArrayList<String> Result = new ArrayList<String> ();
        
        if (children == null) {
               System.out.println("The directory does not exist!" );   
        } else {
               for (int i=0; i<children.length; i++) {
            	  String filename = children[i];
                  BufferedReader br = new BufferedReader(new FileReader(inputpath+filename));
                  String line=br.readLine();
                  int j=1;
                  while(line != null){
                	  String[] seperate= line.split(" ");
                	  if(j==1){
                		     FileNames.add(seperate[1]);
                		     Sent.add(Double.parseDouble(seperate[2]));
                		     Word.add(Double.parseDouble(seperate[4]));
                		     OOVs.add((double) (Double.parseDouble(seperate[6])/Double.parseDouble(seperate[4])));
                	  }
                	  else {
                		     Logprobs.add(Double.parseDouble(seperate[3]));
                		     ppl1.add(Math.log10(Double.parseDouble(seperate[7])));
                	  }
                	  j++;
                      line=br.readLine();
                      
                  }
                  br.close();
	        }
        
	   }
        
/*       double SentWord_Correlation=Correlationlogppl1(Sent,Word) ;
       System.out.println("Correlation between Sentences and Words= "+SentWord_Correlation);
        double Wordppl1_Correlation=Correlationlogppl1(Word,ppl1) ;
        System.out.println("Correlation between Words and ppl1= "+Wordppl1_Correlation);
        double OOVsppl1_Correlation=Correlationlogppl1(OOVs,ppl1) ;
        System.out.println("Correlation between OOVs and ppl1= "+OOVsppl1_Correlation);
         double OOVslogprob_Correlation=Correlationlogppl1(OOVs,Logprobs) ;
        System.out.println("Correlation between OOVs and Logprob= "+OOVslogprob_Correlation);
        double Wordlogprob_Correlation=Correlationlogppl1(Word,Logprobs) ;
         System.out.println("Correlation between Word and Logprob= "+Wordlogprob_Correlation);
   
        double PPL1logprob_Correlation=Correlationlogppl1(Logprobs,ppl1) ;
        System.out.println("Correlation between Logprob and ppl1 = "+PPL1logprob_Correlation);*/
        
        if(TypeOfCriterion==1){
        	for(int i=0; i<OOVs.size(); i++){
            	Result.add(String.valueOf( ppl1.get(i)+OOVs.get(i))+","+FileNames.get(i));
            	// Criterian is adding ppl and OOV or multiplying or 1/ppl*(1-OOVs)
            }
            
        }
        if(TypeOfCriterion==2){
        	for(int i=0; i<OOVs.size(); i++){
            	Result.add(String.valueOf( ppl1.get(i)*OOVs.get(i))+","+FileNames.get(i));
            	// Criterian is adding ppl and OOV or multiplying or 1/ppl*(1-OOVs)
            }
            
        }
        if(TypeOfCriterion==3){
        	for(int i=0; i<OOVs.size(); i++){
            	Result.add(String.valueOf( (double)(1.0/ppl1.get(i))*(1.0-OOVs.get(i)))+","+FileNames.get(i));
            	// Criterian is adding ppl and OOV or multiplying or 1/ppl*(1-OOVs)
            }
            
        }
        if((TypeOfCriterion!=3)|| (TypeOfCriterion!=2)||(TypeOfCriterion!=1)){
        	System.out.println("**********************  This certrion does not exist, please select the right one (1-3) ********************");
        	System.exit(0);
        	
        }
               
        
        return Result;

	}
	private static double Correlationlogppl1(ArrayList<Double> sent, ArrayList<Double> word) {
		// TODO Auto-generated method stub
		double result = 0,SumScr_x=0,SumScr_y=0;
		double sum_sq_x = 0;
		double sum_sq_y = 0;
		double sum_coproduct = 0;
		double mean_x = 0;
		double mean_y =0;
		for(int i=0;i<sent.size();i++){
			SumScr_x+=sent.get(i);
			SumScr_y+=word.get(i);
		}
		mean_x=SumScr_x/sent.size();
		mean_y=SumScr_y/sent.size();

		for(int i=0;i<sent.size();i++){
			
	       double delta_x = sent.get(i)-mean_x;
		   double delta_y = word.get(i)-mean_y;
		   sum_sq_x += delta_x * delta_x;
		   sum_sq_y += delta_y * delta_y;
		   sum_coproduct += delta_x * delta_y;
		  
		}
		double pop_sd_x = (double) Math.sqrt(sum_sq_x);
		double pop_sd_y = (double) Math.sqrt(sum_sq_y);
		result = sum_coproduct / (pop_sd_x*pop_sd_y);
		
		return result;
		
		
	}
	
	


}
