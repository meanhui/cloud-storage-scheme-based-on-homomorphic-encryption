package homomorphic;

import java.math.BigInteger;
import java.security.SecureRandom;

import function.Keygen;

public class Verify {
	
	private static BigInteger h = Keygen.readPrivateKey("privateKey")[0];
	private static BigInteger n = Keygen.readPublicKey("publicKey")[1];
	
	/**
	 * 
	 * @param K1 �������K1
	 * @param K2 �������K2
	 * @param prove Prove�������ص�����
	 * @param index ������
	 * @return ��֤�ɹ�true��ʧ��false
	 */
	public static boolean Verify(BigInteger K1,BigInteger K2,BigInteger[] prove,int index){
		
		//System.out.println("***** Function Verify *****");
		
		int i = index%50 ;

		//����α���ӳ�亯��
		BigInteger FK1 = Pseudo_random(K1, i);
		BigInteger FK2 = Pseudo_random(K2, i);
						

		BigInteger alpha = prove[0];
		BigInteger beta = prove[1];
		
		BigInteger verify = FK1.modPow(FK2, n).multiply(beta.modPow(h, n)).mod(n);
		//System.out.println("verify: "+ verify);
		
		Boolean verifyResult = alpha.equals(verify);
		
		return verifyResult;
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
