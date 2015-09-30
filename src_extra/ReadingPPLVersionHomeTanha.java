package eu.transcriptorium.jafar.disagree;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;

import  org.apache.poi.hssf.usermodel.HSSFSheet;
import  org.apache.poi.hssf.usermodel.HSSFWorkbook;
import  org.apache.poi.hssf.usermodel.HSSFRow;
import  org.apache.poi.hssf.usermodel.HSSFCell;



public class ReadingPPLVersionHomeTanha {

	private static final int Percent = 3;
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
	
		File dir = new File("/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/Out");
		System.out.println("\n\n  Out domain correlation \n");

        String ExcelFilename="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/OutDomainFile.xls" ;
        String inputpath="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/Out/";
        ArrayList<String> Outppl1 = new ArrayList<String> ();
        ArrayList<String> OutFileNames = new ArrayList<String> ();
		
        Outppl1=CorrelationFunction(dir,ExcelFilename, inputpath,OutFileNames);
	
		
		ArrayList<String> RelatedFilesNameOut= new ArrayList<String>();
		ArrayList<String> OutRanks= new ArrayList<String>();
		
		OutRanks=SortedRanks(Outppl1, RelatedFilesNameOut);
		
			
		System.out.println("\n\n  in domain correlation \n");
        dir = new File("/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/In");
        ExcelFilename="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/InDomainFile.xls" ;
        inputpath="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/In/";
        ArrayList<String> Inppl1 = new ArrayList<String> ();
        ArrayList<String> InFileNames = new ArrayList<String> ();

		Inppl1=CorrelationFunction(dir,ExcelFilename, inputpath,InFileNames);
		
		
		ArrayList<String> RelatedFilesNameIn= new ArrayList<String>();
		ArrayList<String> InRanks= new ArrayList<String>();
		
		InRanks=SortedRanks(Inppl1, RelatedFilesNameIn);
	
		int Threshould=(int)(Inppl1.size()*Percent)/100;
		
		ArrayList<String> ConfidentOutFileName= new ArrayList<String>();
		ArrayList<String> ResultingFileName= new ArrayList<String>();

		for(int i=0;i<Threshould;i++){
			ConfidentOutFileName.add(RelatedFilesNameOut.get(i));
		}
		for(int i=0;i<Threshould;i++){
			System.out.println("Existance InRanks === "+InRanks.get(i)+"  OuRanks== "+OutRanks.get(i));

			if(ConfidentOutFileName.contains(RelatedFilesNameIn.get(i))){
				ResultingFileName.add(RelatedFilesNameIn.get(i));
				System.out.println("Selected  InRanks === "+InRanks.get(i)+"  OuRanks== "+OutRanks.get(i));
			}
				
		}
	
		
        String OutputTextFile="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/SeletedText.txt" ;

		RetrivedBestMaches(ResultingFileName,OutputTextFile);
	 
		String OutputTextFileOriginal="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/SeletedTextOriginalCleaned.txt" ;

		RetrivedBestMachesCleanedText(ResultingFileName,OutputTextFileOriginal);  
		
	}
	
	
	
	
	
	private static void RetrivedBestMachesCleanedText(
		
		ArrayList<String> resultingFileName, String outputTextFile) throws IOException {
		// TODO Auto-generated method stub
		PrintWriter output= new PrintWriter(new BufferedWriter(new FileWriter(outputTextFile)));
		for(String st:resultingFileName){
			st=st.substring(0, st.lastIndexOf(":"));
			st=st.substring(st.lastIndexOf("/"));
			st=st.substring(0, st.lastIndexOf("n")-1);
			st="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/CleanedText"+st;
	//		System.out.println(st);
			BufferedReader br= new BufferedReader(new FileReader(st));
			String line=br.readLine();
			while(line!=null){
				output.append(line);
				output.println();
			//	System.out.println(line);
				line=br.readLine();
			}
			br.close();
				
		}
		output.flush();
		output.close();
		System.out.println(" The file has been created in ");

		
	}





	private static void RetrivedBestMaches(ArrayList<String> resultingFileName,
			String outputTextFile) throws IOException {
		// TODO Auto-generated method stub
		
		PrintWriter output= new PrintWriter(new BufferedWriter(new FileWriter(outputTextFile)));
		
		String listOfFiles="/mnt/Projecten/transcriptorium/Tools/languagemodeling/TestSampleSelection/ListofSelectedNormalizedFiles.txt";
		PrintWriter outlist= new PrintWriter(new BufferedWriter(new FileWriter(listOfFiles)));

		for(String st:resultingFileName){
			st=st.substring(0, st.lastIndexOf(":"));
		//	System.out.println(st);
			String filename=st.substring(st.lastIndexOf("/")+1);
			outlist.write(filename);
			outlist.println();
		//	System.out.println(filename);
			BufferedReader br= new BufferedReader(new FileReader(st));
			String line=br.readLine();
			while(line!=null){
				output.append(line);
				output.println();
			//	System.out.println(line);
				line=br.readLine();
			}
			br.close();
				
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





	public static ArrayList<String> CorrelationFunction(File dir, String ExcelFilename, String inputpath, ArrayList<String> FileNames) throws IOException {
		// TODO Auto-generated method stub
        String[] children = dir.list();

		HSSFWorkbook workbook=new HSSFWorkbook();
        HSSFSheet sheet =  workbook.createSheet("FirstSheet");  

        HSSFRow rowhead=   sheet.createRow((short)0);
        rowhead.createCell((short) 0).setCellValue("File Name");
        rowhead.createCell((short) 1).setCellValue("Sentences");
        rowhead.createCell((short) 2).setCellValue("Words");
        rowhead.createCell((short) 3).setCellValue("OOVs");
        rowhead.createCell((short) 4).setCellValue("Logprobs");
        rowhead.createCell((short) 5).setCellValue("ppl1");

        
        HSSFRow row=   sheet.createRow((short)1);
       
        ArrayList<Double> Sent  = new ArrayList<Double> ();
        ArrayList<Double> Logprobs  = new ArrayList<Double> ();
        ArrayList<Double> ppl1 = new ArrayList<Double> ();

        ArrayList<Double> Word  = new ArrayList<Double> ();
        ArrayList<Double> OOVs = new ArrayList<Double> ();
        ArrayList<String> Result = new ArrayList<String> ();

      
        
        if (children == null) {
               System.out.println("The directory does not exist!" );   
        } else {
          //   System.out.println("\n\n \t \t Text prepocessing of the original text is started! and It takes a few seconds!");
       //      PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("d:/eccomerged.txt")));
            int p=1;
             for (int i=0; i<children.length; i++) {
            	  String filename = children[i];
                  BufferedReader br = new BufferedReader(new FileReader(inputpath+filename));
                  String line=br.readLine();
                  int j=1;
                  while(line != null){
                //      out.append(line);
                	  String[] seperate= line.split(" ");
                	  if(j==1){
                		     row.createCell((short) 0).setCellValue(seperate[1]);
                		     FileNames.add(seperate[1]);
                		     row.createCell((short) 1).setCellValue(seperate[2]);
                	//	     System.out.println("Integer.parseInt(seperate[2])== "+Integer.parseInt(seperate[2]));
                		     Sent.add(Double.parseDouble(seperate[2]));
                		     row.createCell((short) 2).setCellValue(seperate[4]);
                		     Word.add(Double.parseDouble(seperate[4]));
                		     
                	//	     System.out.println("Integer.parseInt(seperate[4])== "+Integer.parseInt(seperate[4]));

                		     row.createCell((short) 3).setCellValue(seperate[6]);
                		     OOVs.add((double) (Double.parseDouble(seperate[6])/Double.parseDouble(seperate[4])));

                	  }
                	  else {
                		     row.createCell((short) 4).setCellValue(seperate[3]);
                		     Logprobs.add(Double.parseDouble(seperate[3]));

                		     row.createCell((short) 5).setCellValue(seperate[7]);
                		     ppl1.add(Math.log10(Double.parseDouble(seperate[7])));

                		  
                	  }
                	//  System.out.println();
                	  j++;
                      line=br.readLine();
                      
                  }
                  br.close();
                  p++;
                  row=   sheet.createRow((short)p);

	        }
        
	   }
        
   /*      for(int k=0; k<OOVs.size(); k++)
        	 System.out.println("ppl1=="+ppl1.get(k));
         
         for(int k=0; k<OOVs.size(); k++)
        	 System.out.println("OOVs=="+OOVs.get(k));*/
        FileOutputStream fileOut =  new FileOutputStream(ExcelFilename);
        workbook.write(fileOut);
        fileOut.close();
        System.out.println("Your excel file has been generated!");
        
  //      LinearCorrelation corrtest= new LinearCorrelation();
        
        
        double SentWord_Correlation=Correlationlogppl1(Sent,Word) ;
        
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
        
        System.out.println("Correlation between Logprob and ppl1 = "+PPL1logprob_Correlation);
        
       
        for(int i=0; i<OOVs.size(); i++){
        	Result.add(String.valueOf(ppl1.get(i)+OOVs.get(i))+","+FileNames.get(i));
    ///      	System.out.println("ppl1=="+Result.get(i));

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
