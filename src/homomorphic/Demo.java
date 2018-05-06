package homomorphic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import function.FileSplit;
import function.Keygen;

public class Demo {
	
	private static final int k = 4;
	private static final int d = 512;
	private static final String OSDIR = "outsource\\";
	private static final String RSDIR = "reconstruction\\";
	private static final String OPDIR = "output\\";
	
	private static final String DEMOFILE = "testFile1.xml";
	
	private static BigInteger n = Keygen.readPublicKey("publicKey")[1];
	private static BigInteger g = Keygen.readPublicKey("publicKey")[0];

	public static void main(String[] args) {
		
		long timeStamp;

		List<BigInteger> factors = new ArrayList<>();

		//密钥生成
		//Keygen.generateKeys(k, d);
		BigInteger []prime = readPrimeKey("primeKey");
		for(int i=0;i<k;i++){
			factors.add(prime[i]);
		}
		
		//生成随机种子K1
		//Keygen.generateSeeds(2048,"K1");
		//读取K1
		BigInteger K1 = Keygen.readSeed("K1");	

		FileSplit split = new FileSplit();
		
		assert split.deleteDir(OPDIR);
		assert split.deleteDir(OSDIR);
		assert split.deleteDir(RSDIR);
		
		long fileLength = readFile(DEMOFILE).length();
		int blockLength = 160;
		int blockNumber = (int) (fileLength/blockLength+1);	
		String msg;
		
		//---------分割文件---------		
		try {		
			split.splitByNumber(DEMOFILE, blockNumber);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		BigInteger [] outSourceData;
		
		
		System.out.println("block number: " + blockNumber);
		timeStamp = System.currentTimeMillis();
		//---------数据分块加密外包---------
		for(int i = 1;i<=blockNumber-1;i++){
			msg = readFile(OPDIR + i + ".file");
			outSourceData = Outsource.Outsource(msg, OSDIR+i, K1,i);
		}	
		copyFile(OPDIR+blockNumber+".file", RSDIR+blockNumber+".file");			
		System.out.println("Outsource time : " + (System.currentTimeMillis() - timeStamp));
		
		//-------------接下来是验证过程-----------		
		BigInteger K2 = Audit.Audit();
		
		int count=0;
		
		List<Integer> error = new ArrayList<>();
		BigInteger [] prove;
		
		//文件块的数量
		blockNumber = GetFileNum(OSDIR)/2;
		System.out.println("Block number: " + blockNumber);

		for(int i=1;i<=blockNumber;i++){
			timeStamp = System.currentTimeMillis();
			prove = Prove.Prove(OSDIR+i, K2, i);
			System.out.println("prove time : " + (System.currentTimeMillis() - timeStamp));
			//ALPHA = ALPHA.multiply(prove[0]).mod(n);
			//BETA = BETA.multiply(prove[1]).mod(n);
			timeStamp = System.currentTimeMillis();
			if(Verify.Verify(K1, K2, prove, i)){
				System.out.println("verify : " + (System.currentTimeMillis() - timeStamp));
				count++;
			}
			else{
				error.add(i);
			}
		}		
		
		//---------解密和重构-------------
		if(error.size()==0)
		{
			timeStamp = System.currentTimeMillis();
			DataReconstruction.DataReconstruction(k, factors,OSDIR,DEMOFILE);
			System.out.println("Data reconstruction time : " + (System.currentTimeMillis() - timeStamp));
		}
		else
		{
			System.out.print("Error block:	");
			for(int i = 0;i<error.size();i++)
			System.out.print(error.get(i)+" ");
		}
		
	}
	
	private static BigInteger[] readPrimeKey(String file) {
		BigInteger[] privateKey = new BigInteger[4];
		
		try(BufferedReader br = new BufferedReader(new FileReader(file))) {
		    privateKey[0] = new BigInteger(br.readLine());
		    privateKey[1] = new BigInteger(br.readLine());
		    privateKey[2] = new BigInteger(br.readLine());
		    privateKey[3] = new BigInteger(br.readLine());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return privateKey;
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
	
	public static void copyFile(String oldPath, String newPath) { 
		try { 
			int bytesum = 0; 
			int byteread = 0; 
			File oldfile = new File(oldPath); 
			if (oldfile.exists()) { //文件存在时 
			InputStream inStream = new FileInputStream(oldPath); //读入原文件 
			FileOutputStream fs = new FileOutputStream(newPath); 
			byte[] buffer = new byte[1444]; 
			while ( (byteread = inStream.read(buffer)) != -1) { 
				bytesum += byteread; //字节数 文件大小 
				//System.out.println(bytesum); 
				fs.write(buffer, 0, byteread); 
			} 
			inStream.close(); 
			fs.close();
		} 
			
		} 
		catch (Exception e) { 
			System.out.println("复制单个文件操作出错"); 
			e.printStackTrace(); 

		} 

	}
	
	public static int GetFileNum(String path){
		File file = new File(path);
		String files[] = file.list();
		return files.length;		
	}

}
