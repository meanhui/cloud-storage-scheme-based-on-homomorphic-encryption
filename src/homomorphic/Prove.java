package homomorphic;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;

import function.Keygen;

public class Prove {
	
	private static BigInteger n = Keygen.readPublicKey("publicKey")[1];

	/**
	 * 
	 * @param file 加密源文件
	 * @param K2 审计种子
	 * @param index 块索引
	 * @return [alpha,beta]
	 */
	public static BigInteger[] Prove(String file,BigInteger K2,int index){
		System.out.println("Block " + index);
		System.out.println("***** Function Prove *****");
		BigInteger[] prove = new BigInteger[2];
		
		//System.out.println(readFile(file+"_ai"));
		
		BigInteger ai = new BigInteger(readFile(file+"_ai"));
		BigInteger bi = new BigInteger(readFile(file+"_bi"));
				
		int i = index%50;
		BigInteger FK2 = Pseudo_random(K2, i);
		//System.out.println("fk2(1): " + FK2);
		
		BigInteger alpha = ai.modPow(FK2, n);
		BigInteger beta = bi.modPow(FK2, n);
		//System.out.println("alpha: " + alpha);
		//System.out.println("beta: " + beta);
		
		prove[0] = alpha;
		prove[1] = beta;
		
		return prove;
	}


	/**
	 * 
	 * @param filename 文件名
	 * @return 文件内容String
	 */
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
	
	public static BigInteger Pseudo_random(BigInteger seed,int i){
		int fk=0;
		SecureRandom demo = null;
		demo =  new SecureRandom(seed.pow(i+1).toByteArray());
		fk = Math.abs(demo.nextInt());	
		BigInteger FK = new BigInteger(fk+"");
		
		return FK;
	}
	
}
