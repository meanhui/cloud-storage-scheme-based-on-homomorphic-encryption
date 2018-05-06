package homomorphic;

import java.math.BigInteger;

import function.Keygen;

public class Audit {
    /**
     * Audit()
     * @return 生成审计用的种子K2
     * @throws Exception
     */
	public static BigInteger Audit(){
		//Keygen.generateSeeds(2048, "K2");
		BigInteger K2 = Keygen.readSeed("K2");	
		System.out.println("K2: "+K2);
		return K2;
	}
}
