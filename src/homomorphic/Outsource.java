package homomorphic;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.SecureRandom;

import function.Keygen;

public class Outsource {
	
	private static BigInteger n = Keygen.readPublicKey("publicKey")[1];
	private static BigInteger g = Keygen.readPublicKey("publicKey")[0];
	
	/**
	 * 
	 * @param data �������
	 * @param encFileName ��������ļ�
	 * @param K1 �������K1
	 * @param index �������
	 * @return [ai,bi]
	 */
	public static BigInteger[] Outsource(String data,String encFileName,BigInteger K1,int index){
		//System.out.println("***** Function Outsource *****");
		//�����������K1��K2
		//System.out.println(index);
		//BigInteger seed1 = K1;		
		
		//System.out.println("K1: " + K1);
					
		int i = index%50;	
		BigInteger FK1 = Pseudo_random(K1, i);		
		//System.out.println("fk1(1): "+FK1);
		
		String originalData = data;
		//System.out.println("originalData: " + originalData);
		
		BigInteger di = new BigInteger(originalData.getBytes());

		//����ai��bi
		BigInteger ai = di.multiply(FK1);
		BigInteger bi = di.modPow(g,n);
									
		BigInteger[] outSourceData = new BigInteger[2];
		outSourceData[0] = ai;
		outSourceData[1] = bi;
		
		//System.out.println("ai: " + ai);
		//System.out.println("bi: " + bi);
		
		PrintWriter writer;
		
		try {
			writer = new PrintWriter(encFileName+"_ai", "UTF-8");
			writer.print(ai);
			writer.close();
			writer = new PrintWriter(encFileName+"_bi", "UTF-8");
			writer.print(bi);
			writer.close();
			
		} catch (FileNotFoundException | UnsupportedEncodingException ex) {
			ex.printStackTrace();
		}
		
		return outSourceData;
	}
	
	/**
	 * 
	 * @param seed ����������
	 * @param i ����Index
	 * @return ���ӳ��
	 */
	public static BigInteger Pseudo_random(BigInteger seed,int i){
		int fk=0;
		SecureRandom demo = null;
		demo =  new SecureRandom(seed.pow(i+1).toByteArray());
		fk = Math.abs(demo.nextInt());	
		BigInteger FK = new BigInteger(fk+"");
		
		return FK;
	}
	
}
