package homomorphic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import function.FileSplit;
import function.Keygen;

public class DataReconstruction {

	private static final String RSDIR = "reconstruction\\";
	
    /**
     * DataReconstruction(int k,List<BigInteger> factors,String outsourceDir)
     * @param k 私钥质数个数
     * @param factor 私钥列表
     * @param outsourceDir 输出的目录
     * @throws Exception
     */
	public static void DataReconstruction(int k,List<BigInteger> factors,String outsourceDir,String DEMOFILE) {
		File file=new File(outsourceDir);
		String fileName,encData,decData;
		int index = 0;
        for(File temp:file.listFiles()){
            if(temp.isFile()){
            	fileName = temp.getName();

                if(fileName.endsWith("bi")){
                	index++;
                	encData =  readFile(outsourceDir + fileName);
                	decData = Keygen.decryptCRT(k, new BigInteger(encData), factors);
                	save(decData, RSDIR+index+".file");

                }
            }
            
        }
        FileSplit fp = new FileSplit();
        try {       	
			fp.mergeByName(RSDIR,DEMOFILE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static String readFile(String filename) {
		String msg = null;

		try {
			msg = new String(Files.readAllBytes(Paths.get(filename)),
							StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return msg;
	}
	
	public static void save(String text, String filename) {
		PrintWriter writer;
		try {
			writer = new PrintWriter(filename, "UTF-8");
			writer.print(text);
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException ex) {
			ex.printStackTrace();
		}
	}
	
}
