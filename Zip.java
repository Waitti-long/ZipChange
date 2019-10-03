import javax.imageio.ImageIO;
import java.awt.*;
import java.io.*;
import java.util.Objects;
import java.util.Random;
import java.util.zip.*;

public class Zip {
    public static void main(String[] args){
        Zip.change("./zip.zip","./anewzip.zip");
        /*
        if(args.length>=2){
            if(args.length>=3){
                if(args[2].equals("-u")){
                    Zip.setMode(Mode.Uncompressed);
                }else{
                    Zip.setMode(Mode.Compressed);
                }
            }
            Zip.change(args[0],args[1]);
        }else{
            System.out.println("args should in this format:original destination CompressedSizeOrUnCompressedSize(optional,-u/-c)");
        }*/
    }

    enum Mode{
        Compressed,Uncompressed
    }

    private static Mode mode=Mode.Compressed;

    public static void setMode(Mode mode1){
        mode=mode1;
    }

    public static void change(String org,String dest){
        change(org,dest,".\\"+new Random().nextDouble());
    }

    public static void change(String org,String dest,String tmp){
        if(!tmp.endsWith("\\")){
            tmp+="\\";
        }
        File test=new File(tmp);
        if (test.exists()){
            System.out.println("随机过程错误，请重新运行");
        }else{
            ZipInputStream zin = null;
            ZipOutputStream out = null;
            String local=tmp;

            try{
                int BUFFER=512;
                zin=new ZipInputStream(new FileInputStream(org));
                out = new ZipOutputStream(new FileOutputStream(dest));
                ZipEntry entry;
                File file;
                String base;
                while((entry=zin.getNextEntry())!=null){
                    file = new File(local+entry.getName());
                    if(!entry.isDirectory()){
                        long length;
                        if(mode==Mode.Compressed){
                            length=entry.getCompressedSize();
                        }else{
                            length=entry.getSize();
                        }
                        //System.out.println(file);
                        File dir=new File(file.toString().substring(0,file.toString().lastIndexOf("\\")));
                        if(!dir.exists()){
                            dir.mkdirs();
                        }
                        OutputStream os=new FileOutputStream(file);
                        int readLength=0;
                        byte[] buffer=new byte[BUFFER];
                        while ((readLength=zin.read(buffer,0,BUFFER))!=-1){
                            os.write(buffer,0,readLength);
                        }
                        os.close();
                        if(isImage(file)){
                            String tail=file.toString().substring(file.toString().lastIndexOf("."));
                            File file1=new File(dir.toString()+"\\"+length*8+"bit"+tail);
                            int append=1;
                            while (file1.exists()){
                                file1=new File(dir.toString()+"\\"+length*8+"bit"+"$"+(append++)+tail);
                            }
                            file.renameTo(file1);
                            // System.out.println(file1);
                            write(out,file1,file1.toString().replace(local,""),local);
                        }else{
                            write(out,file,file.toString().replace(local,""),local);
                        }
                    }else{
                        out.putNextEntry(new ZipEntry(entry.getName()+"\\"));
                    }
                    zin.closeEntry();
                }
                zin.close();
                out.close();
            } catch (Exception e){
                e.printStackTrace();
            } finally {
                if(zin!=null){
                    try {
                        zin.close();
                        Objects.requireNonNull(out).close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
            File file=new File(local.substring(0,local.lastIndexOf("\\")));
            delFiles(file);
        }
    }

    private static boolean delFiles(File file){
        if(!file.exists()){
            return false;
        }
        if(file.isDirectory()){
            File[] files=file.listFiles();
            if (files != null) {
                for(File f:files){
                    delFiles(f);
                }
            }
        }
        return file.delete();
    }

    private static boolean isImage(File file){
            try{
                Image image = ImageIO.read(file);
                return image!=null;
            }catch (IOException e){
                return false;
        }
    }

    private static void write(ZipOutputStream out, File file, String name, String local)throws IOException{
        out.putNextEntry(new ZipEntry(file.toString().replace(local,"")));
        System.out.println(file.toString().replace(local,""));
        FileInputStream in=new FileInputStream(file);
        int length1;
        while ((length1=in.read())!=-1){
            out.write(length1);
        }
        in.close();
    }
}
