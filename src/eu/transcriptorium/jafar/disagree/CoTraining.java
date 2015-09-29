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


public class CoTraining {

	private static final int Percent = 10;
	private static final int Iterations = 1;
	private static  double Thresholds = 0.35;
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
	    int Iter=0;
	    double T=0;
		while(Iter<Iterations && T<Thresholds){
			
			LanguageBuliding LmIn=new LanguageBuliding();
			
			LmIn.Output="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/InBentham"; 
			LmIn.Cutoff="0";
			
            LanguageBuliding LmOut=new LanguageBuliding();
			
            LmOut.Output="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/OutBentham";
            LmOut.Cutoff="0";
            LmIn.start(); LmOut.start();
            
            try {
				LmIn.join();
				LmOut.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            File  dir = new File("/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/CleanedAndNormalized");
            String[] children = dir.list();
            children=dir.list();
            
            if (children == null) {
                   System.out.println("The directory does not exist!" );   
            } else {
                String Model="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/InBentham";
                
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
                T1.Model=Model;T1.Output="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/In/";
                T1.OutputType="ppl_in_"; 
                
                
                T2.firstIndex= (int)children.length/4 +1;T2.lastIndex=(int) children.length/2; T2.FileList=children;
                T2.Model=Model;T2.Output="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/In/";
                T2.OutputType="ppl_in_"; 

                T3.firstIndex= (int)children.length/2 +1;T3.lastIndex=(int) (3*children.length)/4;T3.FileList=children;
                T3.Model=Model;T3.Output="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/In/";
                T3.OutputType="ppl_in_"; 

                T4.firstIndex= (int)(3*children.length)/4 +1;T4.lastIndex=(int) children.length-1;T4.FileList=children;
                T4.Model=Model;T4.Output="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/In/";
                T4.OutputType="ppl_in_"; 

               
                Model="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/OutBentham";
                OutT1.firstIndex=0;OutT1.lastIndex=(int) children.length/8; OutT1.FileList=children;
                OutT1.Model=Model;OutT1.Output="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/Out/";
                OutT1.OutputType="ppl_out_"; 
                 
                OutT2.firstIndex= (int)children.length/8 +1;OutT2.lastIndex=(int) children.length/4; OutT2.FileList=children;
                OutT2.Model=Model;OutT2.Output="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/Out/";
                OutT2.OutputType="ppl_out_"; 

                OutT3.firstIndex= (int)children.length/4 +1;OutT3.lastIndex=(int) (3*children.length)/8;OutT3.FileList=children;
                OutT3.Model=Model;OutT3.Output="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/Out/";
                OutT3.OutputType="ppl_out_"; 

                OutT4.firstIndex= (int)(3*children.length)/8 +1;OutT4.lastIndex=(int)(children.length)/2;OutT4.FileList=children;
                OutT4.Model=Model;OutT4.Output="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/Out/";
                OutT4.OutputType="ppl_out_"; 
                
                OutT5.firstIndex=(int)(children.length)/2 +1;OutT5.lastIndex=(int) (5*children.length)/8; OutT5.FileList=children;
                OutT5.Model=Model;OutT5.Output="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/Out/";
                OutT5.OutputType="ppl_out_"; 
                 
                OutT6.firstIndex= (5*children.length)/8 +1;OutT6.lastIndex=(int) (6*children.length)/8; OutT6.FileList=children;
                OutT6.Model=Model;OutT6.Output="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/Out/";
                OutT6.OutputType="ppl_out_"; 

                OutT7.firstIndex= (int)(6*children.length)/8 +1;OutT7.lastIndex=(int) (7*children.length)/8;OutT7.FileList=children;
                OutT7.Model=Model;OutT7.Output="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/Out/";
                OutT7.OutputType="ppl_out_"; 

                OutT8.firstIndex= (int)(7*children.length)/8 +1;OutT8.lastIndex=(int) children.length-1;OutT8.FileList=children;
                OutT8.Model=Model;OutT8.Output="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/Out/";
                OutT8.OutputType="ppl_out_"; 

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
			
			dir = new File("/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/Out");
			System.out.println("\n\n  Out domain correlation \n");
	        String inputpath="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/Out/";
	        ArrayList<String> Outppl1 = new ArrayList<String> ();
	        ArrayList<String> OutFileNames = new ArrayList<String> ();
			
	        Outppl1=CorrelationFunction(dir, inputpath,OutFileNames);
				
			ArrayList<String> RelatedFilesNameOut= new ArrayList<String>();
			ArrayList<String> OutRanks= new ArrayList<String>();
			
			OutRanks=SortedRanks(Outppl1, RelatedFilesNameOut);
			
				
			System.out.println("\n\n  in domain correlation \n");
	        dir = new File("/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/In");
	        inputpath="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/In/";
	        ArrayList<String> Inppl1 = new ArrayList<String> ();
	        ArrayList<String> InFileNames = new ArrayList<String> ();

			Inppl1=CorrelationFunction(dir, inputpath,InFileNames);
					
			ArrayList<String> RelatedFilesNameIn= new ArrayList<String>();
			ArrayList<String> InRanks= new ArrayList<String>();
			
			InRanks=SortedRanks(Inppl1, RelatedFilesNameIn);
			
		/*	System.out.println("RelatedFilesNameIn=============================================");
			for(int i=0; i<RelatedFilesNameIn.size();i++)
				System.out.println(RelatedFilesNameIn.get(i));*/
		
			int Threshould=(int)(Inppl1.size()*Percent)/100;
			System.out.println("Threshould===== "+Threshould);
			T=(Double.parseDouble( InRanks.get(Threshould-1)) + Double.parseDouble(OutRanks.get(Threshould-1)))/2  ;
			
			System.out.println("Thersholdddddddddddddddddddddddddddddddddddddddddd  =============== "+T);
			
			ArrayList<String> ConfidentOutFileName= new ArrayList<String>();
			ArrayList<String> AddingToIn= new ArrayList<String>();
			ArrayList<String> AddingToOut= new ArrayList<String>();

			ArrayList<String> ConfidentInFileName= new ArrayList<String>();

			// High confidence predictions of LM(Out)

			for(int i=0;i<Threshould;i++){
				ConfidentOutFileName.add(RelatedFilesNameOut.get(i));
		//		System.out.println("Out ranks "+InRanks.get(i)+"Out domain = "+RelatedFilesNameOut.get(i)+ "  In ranks "+OutRanks.get(i)+" In domain= "+RelatedFilesNameIn.get(i) );
			}
			
			// High confidence predictions of LM(In)
			for(int i=0;i<Threshould;i++){
				ConfidentInFileName.add(RelatedFilesNameIn.get(i));
		//		System.out.println("Out ranks "+InRanks.get(i)+"Out domain = "+RelatedFilesNameOut.get(i)+ "  In ranks "+OutRanks.get(i)+" In domain= "+RelatedFilesNameIn.get(i) );
			}
			
			String PathIn,PathOut;
			PathIn="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/In/";
			PathOut="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/Out/";
			for(int i=0;i<Threshould;i++){
		//		System.out.println("Existance InRanks === "+InRanks.get(i)+"  OuRanks== "+OutRanks.get(i));
		//		System.out.println("\n General ConfidentOutFileName(j)==  "+ConfidentOutFileName.get(i)+ " RelatedFilesNameIn(i)== "+RelatedFilesNameIn.get(i)+"\n");


				if(!ConfidentOutFileName.contains(ConfidentInFileName.get(i))){
					//int j=ConfidentOutFileName.indexOf(RelatedFilesNameIn.get(i));
					System.out.println("\n After selection ConfidentOutFileName(j)==  "+RelatedFilesNameIn.get(i)+ " RelatedFilesNameIn(i)== "+RelatedFilesNameIn.get(i)+"\n");
					AddingToOut.add(ConfidentInFileName.get(i));
		//			System.out.println("Selected  InRanks === "+InRanks.get(i)+"  OuRanks== "+OutRanks.get(i));
					RemoveFromPPLFiles(PathIn,PathOut,ConfidentInFileName.get(i));
				}
				if(!ConfidentInFileName.contains(ConfidentOutFileName.get(i))){
					//int j=ConfidentOutFileName.indexOf(RelatedFilesNameIn.get(i));
					System.out.println("\n After selection ConfidentOutFileName(j)==  "+RelatedFilesNameIn.get(i)+ " RelatedFilesNameIn(i)== "+RelatedFilesNameIn.get(i)+"\n");
					AddingToIn.add(ConfidentOutFileName.get(i));
		//			System.out.println("Selected  InRanks === "+InRanks.get(i)+"  OuRanks== "+OutRanks.get(i));
					RemoveFromPPLFiles(PathIn,PathOut,ConfidentOutFileName.get(i));
				}
					
			}
		
	           // Adding a set of high-confidence predictions to Out-domain sets which has been predicted by LM(in)
			
	        String OutputTextFile="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/SeletedNormalizedText.txt" ;

	        System.out.println(" List of files to be added to Out-domain training set");
	        for(int i=0; i<AddingToOut.size();i++)
	        	System.out.println("AddingToOut==="+AddingToOut.get(i));
	        
			RetrivedBestMaches(AddingToOut,OutputTextFile);
		 
			String OutputTextFileOriginal="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/SeletedTextOriginalCleaned.txt" ;

			RetrivedBestMachesCleanedText(AddingToOut,OutputTextFileOriginal);  
			
            String OutTraing="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/OutBentham/";
            String NewCleanedTrainingSet=OutputTextFileOriginal;
            String NewNormalizedTrainingSet=OutputTextFile;
			AddToTrainingSet(OutTraing,NewCleanedTrainingSet,NewNormalizedTrainingSet);
            
			RemoveFileofSelectedText(OutputTextFile,OutputTextFileOriginal);
			
			RemoveCleanedTextFilesFromCorpora(AddingToOut);
				
			RemoveNormalizedTextFilesFromCorpora(AddingToOut);
				
			
           // Adding a set of high-confidence predictions to Out-domain sets which has been predicted by LM(out)
			
	        OutputTextFile="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/SeletedNormalizedText.txt" ;

	        System.out.println("List of file to be added to In-Domain training set");
	        for(int i=0;i<AddingToIn.size();i++)
	        	System.out.println("AddingToIn===" +AddingToIn.get(i));
			RetrivedBestMaches(AddingToIn,OutputTextFile);
		 
			OutputTextFileOriginal="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/SeletedTextOriginalCleaned.txt" ;

			RetrivedBestMachesCleanedText(AddingToIn,OutputTextFileOriginal);  
			
            NewCleanedTrainingSet=OutputTextFileOriginal;
            NewNormalizedTrainingSet=OutputTextFile;
            String InTraing="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/InBentham/";

			AddToTrainingSet(InTraing,NewCleanedTrainingSet,NewNormalizedTrainingSet);
            
			RemoveFileofSelectedText(OutputTextFile,OutputTextFileOriginal);
					
		   
			
            RemoveCleanedTextFilesFromCorpora(AddingToIn);
			
			RemoveNormalizedTextFilesFromCorpora(AddingToIn);
			
            
            Iter++;
            System.out.println("Iterationnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnn==================="+Iter);

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
			ArrayList<String> resultingFileName) {
		// TODO Auto-generated method stub
		for(String st:resultingFileName){
			st=st.substring(0, st.lastIndexOf(":"));
			st=st.substring(st.lastIndexOf("/"));
			st=st.substring(0, st.lastIndexOf("n")-1);
			st="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/CleanedText"+st;
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
		ArrayList<String> resultingFileName, String outputTextFile) throws IOException {
		
  
		PrintWriter output= new PrintWriter(new BufferedWriter(new FileWriter(outputTextFile)));
		
		for(String st:resultingFileName){
			st=st.substring(0, st.lastIndexOf(":"));
			st=st.substring(st.lastIndexOf("/"));
			st=st.substring(0, st.lastIndexOf("n")-1);
			st="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/CleanedText"+st;
	//		System.out.println(st);
			
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
    		}*/ // for now do not remove the files
				
		}
		output.flush();
		output.close();
		System.out.println(" The file has been created in ");

		
	}





	private static void RetrivedBestMaches(ArrayList<String> resultingFileName,
			String outputTextFile) throws IOException {
		// TODO Auto-generated method stub
		
	/*	File file =new File(outputTextFile);
		
		file.createNewFile();
			
				
        BufferedWriter output = new BufferedWriter(new FileWriter(file,true));*/
      
		PrintWriter output= new PrintWriter(new BufferedWriter(new FileWriter(outputTextFile)));
		
		String listOfFiles="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/ListofSelectedNormalizedFiles.txt";
		
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





	public static ArrayList<String> CorrelationFunction(File dir, String inputpath, ArrayList<String> FileNames) throws IOException {
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
        
       
        for(int i=0; i<OOVs.size(); i++){
        	Result.add(String.valueOf( ppl1.get(i)*OOVs.get(i))+","+FileNames.get(i));
        	
      //  	Result.add(String.valueOf( (double)(1.0/ppl1.get(i))*(1.0-OOVs.get(i)))+","+FileNames.get(i));

        	// Criterian is adding ppl and OOV or multiplying or 1/ppl*(1-OOVs)
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
