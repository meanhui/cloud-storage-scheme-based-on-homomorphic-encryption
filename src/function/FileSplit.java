package function;

import org.apache.commons.codec.binary.Base64;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;


public class FileSplit {
	
	private static final String file_path = "reconstruction\\";
	private static final String split_path = "output\\";
	
    /**
     * �ָ�
     * @param fileName �ļ�·��
     * @param Number �ֿ��ļ�����
     * @throws Exception
     */
    public void splitByNumber(String fileName,int Number) throws Exception{
    	
    	if(!deleteDir(split_path)){
    		System.out.println("Failed to init the floder!");
    		System.exit(0);
    	}
    	else{
    		System.out.println("Folder initialization");
    	}
    	
        File oldFile=new File(fileName);
        BufferedInputStream in=new BufferedInputStream(new FileInputStream(oldFile));
        String file=encode(in);
        int length=file.length();
        System.out.println("�ַ������ȣ�"+length);
        int size=length/Number;
        int start=0,end=size;
        BufferedOutputStream out=null;
        File newFile=null;
        String str_temp="";
        int i;
        for(i=0;i<Number-1;i++){
            str_temp=i + "\n";
            str_temp+=file.substring(start,end);
            newFile=new File(split_path+randNumber(i)+".file");
            out=new BufferedOutputStream(new FileOutputStream(newFile));
            out.write(str_temp.getBytes());
            out.close();
            start+=size;
            end+=size;
        }
        str_temp= Number-1 + "\n";
        str_temp+=file.substring(start);
        newFile=new File(split_path+randNumber(i)+".file");
        out=new BufferedOutputStream(new FileOutputStream(newFile));
        out.write(str_temp.getBytes());
        out.close();
        
        System.out.println("Successfully divided the file into " + Number + "pieces");
        
        return;
    }

    /**
     * �ļ��ϲ�
     * @param path
     * @throws Exception
     */
    public void mergeByName(String path,String newFileName) throws Exception{
        File file=new File(path);
        File list[]=file.listFiles();
        Map<String,String> map=new HashMap<String, String>();

        for(File f:list){
            BufferedReader reader=new BufferedReader(new InputStreamReader(new FileInputStream(f)));
            String str_head=reader.readLine();
            String id=str_head;
            //String id=Integer.parseInt(str_head)+"";
            //System.out.println("id: " + id);
            map.put(id,f.getAbsolutePath());
            reader.close();
        }

        StringBuffer stringBuffer=new StringBuffer();
        int i=1;
        for(i=0;i<list.length;i++){
            File f=new File(map.get(String.valueOf(i)));
            BufferedReader reader=new BufferedReader(new InputStreamReader(new FileInputStream(f)));
            reader.readLine();
            String temp=null;
            while ((temp=reader.readLine())!=null){
                stringBuffer.append(temp);
            }
            reader.close();
        }
              
        
        BufferedOutputStream out=new BufferedOutputStream(new FileOutputStream(file_path+newFileName));
        out.write(decode(stringBuffer.toString()));
        out.close();
        
        System.out.println("File merge successfully");
    }

    /**
     * ����
     * @param in
     * @return
     * @throws IOException
     */
    public String encode(InputStream in) throws IOException{
        byte[] data = new byte[in.available()];
        in.read(data);
        return new String(Base64.encodeBase64(data));
    }

    /**
     * ����
     * @param base64Str
     * @return
     * @throws IOException
     */
    public byte[] decode(String base64Str)throws IOException{
        return Base64.decodeBase64(base64Str.getBytes());
    }

    /**
     * �����
     * @return
     */
    public String randNumber(int i){
        int number= i+1 ;
        String str= String.valueOf(number);
        str=str.replace(".","");
        return str;
    }
    
    //����ļ���
    public boolean deleteDir(String path){  
        File file = new File(path);  
        if(!file.exists()){//�ж��Ƿ��ɾ��Ŀ¼�Ƿ����  
            System.err.println("The dir are not exists!");  
            return false;  
        }  
          
        String[] content = file.list();//ȡ�õ�ǰĿ¼�������ļ����ļ���  
        for(String name : content){  
            File temp = new File(path, name);  
            if(temp.isDirectory()){//�ж��Ƿ���Ŀ¼  
                deleteDir(temp.getAbsolutePath());//�ݹ���ã�ɾ��Ŀ¼�������  
                temp.delete();//ɾ����Ŀ¼  
            }else{  
                if(!temp.delete()){//ֱ��ɾ���ļ�  
                    System.err.println("Failed to delete " + name);  
                }  
            }  
        }  
        return true;  
    }  
    
	public String readFile(String filename) {
		String msg = null;

		try {
			msg = new String(Files.readAllBytes(Paths.get(filename)),
							StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return msg;
	}
	
 /*
    public static void main(String[] args){
    	long fileLength = readFile("test_file4.rar").length();
    	int blockLength = 160;
    	
        try {
            //�ֿ�
            new FileSplit().splitByNumber("test_file4.rar",(int) (fileLength/blockLength+1));
            //�ϲ�
            //new FileSplit(). mergeByName(split_path);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
  */  
}