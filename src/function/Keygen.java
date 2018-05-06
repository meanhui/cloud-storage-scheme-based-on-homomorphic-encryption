package function;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.xml.crypto.Data;

public class Keygen {
	
	private static final String PUBLIC_FILE = "publicKey";
	private static final String PRIVATE_FILE = "privateKey";
	private static final String PRIME_FILE = "primeKey";
	
	public static void generateSeeds(int bitLength,String fileName){
		BigInteger seed = new BigInteger(bitLength, new SecureRandom());		
		saveSeed(seed, fileName);	
	}
	
	public static List<BigInteger> generateKeys(int numOfPrimes,int bitLength){
		BigInteger n = BigInteger.ONE;
		BigInteger phi = BigInteger.ONE;
		BigInteger e = BigInteger.ONE;
		BigInteger d = BigInteger.ONE;
		//素因子存入List
		List<BigInteger> primeFactors = new ArrayList<>();
		//每个线程生成一个大素数
		ExecutorService executor = Executors.newFixedThreadPool(numOfPrimes);
		List<Future<BigInteger>> primeThread = new ArrayList<Future<BigInteger>>();
		
		Callable<BigInteger> generator = new PrimesGenerator(bitLength);
		
		for (int i = 0; i < numOfPrimes; i++) {
			Future<BigInteger> number = executor.submit(generator);
			primeThread.add(number);
		}
		//生成numOfPrimes个素因子，存入List
        for(Future<BigInteger> num : primeThread){
            try {
            	BigInteger prime = num.get();
            	//System.out.println("prime:"+prime.toString());
            	//System.out.println("bitlenth:"+prime.bitLength());
                n = n.multiply(prime);
                phi = phi.multiply(prime.subtract(BigInteger.ONE));
            	primeFactors.add(prime);
            	
            } catch (InterruptedException | ExecutionException ex) {
                ex.printStackTrace();
            }
        }
        BigInteger p = primeFactors.get(0);
        BigInteger q = primeFactors.get(1);
        BigInteger r = primeFactors.get(2);
        BigInteger s = primeFactors.get(3);
                     
        System.out.println("p="+p.toString());
        System.out.println("q="+q.toString());
        System.out.println("r="+r.toString());
        System.out.println("s="+s.toString());
        
		do {
			e = new BigInteger(phi.bitLength(), new SecureRandom());
		} while (phi.gcd(e).intValue() > 1 || e.compareTo(BigInteger.ONE) < 0 || e.compareTo(phi) > 0);
		
		d = e.modInverse(phi);	
		
		savePrivateKey(n, d, PRIVATE_FILE);
		savePublicKey(n, e, PUBLIC_FILE);
		
		executor.shutdown();
			
		System.out.println("n:"+n.toString());
		System.out.println("lenth of n:"+n.bitLength());
		System.out.println("phi:"+phi.toString());
		System.out.println("d:"+d.toString());
		System.out.println("e:"+e.toString());
		
		savePrimeFactors(primeFactors, PRIME_FILE);
		
		return primeFactors;
	}
	
	public static String encrypt(String msg) {
		BigInteger[] key = readPublicKey(PUBLIC_FILE);
		return (new BigInteger(msg.getBytes())).modPow(key[0], key[1]).toString();
	}
	
	public static String decrypt(String msg) {
		BigInteger[] key = readPrivateKey(PRIVATE_FILE);
		return new String((new BigInteger(msg)).modPow(key[0], key[1]).toByteArray());
	}
	
	public static String decrypt(BigInteger msg) {
		BigInteger[] key = readPrivateKey(PRIVATE_FILE);
		return msg.modPow(key[0], key[1]).toString();
	}
	
	public static String decryptCRT(int k, BigInteger msg, List<BigInteger> factors) {
		BigInteger[] key = readPrivateKey(PRIVATE_FILE);
		BigInteger sum = BigInteger.ONE;
		BigInteger c = msg;
		ExecutorService executor = Executors.newFixedThreadPool(k);
		List<Future<BigInteger>> elements = new ArrayList<Future<BigInteger>>();
		
        for(int i = 0; i < k; i++) {
        	BigInteger factor = factors.get(i);
    		Callable<BigInteger> crt = new CRT(c.modPow(key[0].remainder(factor.subtract(BigInteger.ONE)), factor), factor, key[1]);
            Future<BigInteger> element = executor.submit(crt);
            elements.add(element);
        }
        
        
        for(Future<BigInteger> e : elements){
            try {
            	BigInteger element = e.get();
            	sum = sum.add(element);
            } catch (InterruptedException | ExecutionException ex) {
                ex.printStackTrace();
            }
        }
        executor.shutdown();
        
		return new String(sum.subtract(BigInteger.ONE).mod(key[1]).toByteArray());
	}
	
	/*
	public static String decryptCRT(String msg,List<BigInteger> factors){
		BigInteger[] key = readPrivateKey(PRIVATE_FILE);

		BigInteger c = new BigInteger(msg);
		
		BigInteger p = factors.get(0);
		BigInteger q = factors.get(1);
		BigInteger r = factors.get(2);
		BigInteger s = factors.get(3);
		
		BigInteger mp = c.mod(p).modPow(key[1].mod(p.subtract(BigInteger.ONE)), p);
		BigInteger mq = c.mod(q).modPow(key[1].mod(q.subtract(BigInteger.ONE)), q);
		BigInteger mr = c.mod(r).modPow(key[1].mod(r.subtract(BigInteger.ONE)), r);
		BigInteger ms = c.mod(s).modPow(key[1].mod(s.subtract(BigInteger.ONE)), s);
		
		
		mp = mp.multiply(q.multiply(r).multiply(s).modPow(p.subtract(BigInteger.ONE), key[0]));
		mq = mq.multiply(p.multiply(r).multiply(s).modPow(q.subtract(BigInteger.ONE), key[0]));
		mr = mr.multiply(p.multiply(q).multiply(s).modPow(r.subtract(BigInteger.ONE), key[0]));
		ms = ms.multiply(p.multiply(q).multiply(r).modPow(s.subtract(BigInteger.ONE), key[0]));
		
		BigInteger result = mp.add(mq).add(mr).add(ms);
		//System.out.println(result+"----");
		return new String(result.toByteArray());
	}
	*/
	private static void savePublicKey(BigInteger n, BigInteger e, String file) {
		PrintWriter writer;
		try {
			writer = new PrintWriter(file, "UTF-8");
			writer.println(e);
			writer.println(n);

			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException ex) {
			ex.printStackTrace();
		}
	}
	
	private static void saveSeed(BigInteger seed, String file) {
		PrintWriter writer;
		try {
			writer = new PrintWriter(file, "UTF-8");
			writer.println(seed);

			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException ex) {
			ex.printStackTrace();
		}
	}
	
	private static void savePrimeFactors(List <BigInteger>primeFactors,String file) {
		PrintWriter writer;
		BigInteger prime = null;
		try {
			writer = new PrintWriter(file, "UTF-8");
			for(int i = 0;i<4;i++){
				prime = primeFactors.get(i);
				writer.println(prime);
			}
			
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException ex) {
			ex.printStackTrace();
		}
	}
	
	private static void savePrivateKey(BigInteger n, BigInteger d, String file) {
		PrintWriter writer;
		try {
			writer = new PrintWriter(file, "UTF-8");
			writer.println(d);
			writer.println(n);
			
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException ex) {
			ex.printStackTrace();
		}
	}
	
	public static BigInteger readSeed(String file) {
		BigInteger publicKey = null;
		
		try(BufferedReader br = new BufferedReader(new FileReader(file))) {
		    publicKey = new BigInteger(br.readLine());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return publicKey;
	}
	
	public static BigInteger[] readPublicKey(String file) {
		BigInteger[] publicKey = new BigInteger[2];
		
		try(BufferedReader br = new BufferedReader(new FileReader(file))) {
		    publicKey[0] = new BigInteger(br.readLine());
		    publicKey[1] = new BigInteger(br.readLine());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return publicKey;
	}
	
	public static BigInteger[] readPrivateKey(String file) {
		BigInteger[] privateKey = new BigInteger[2];
		
		try(BufferedReader br = new BufferedReader(new FileReader(file))) {
		    privateKey[0] = new BigInteger(br.readLine());
		    privateKey[1] = new BigInteger(br.readLine());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return privateKey;
	}
	
	public static BigInteger[] readPrimeKey(String file) {
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
	
}
