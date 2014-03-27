/**
 * @author pengluyu
 *
 * FileDescriptorUtil.java
 * 7:29:55 AM 2014
 */

package cn.louispeng.imagefilter.bindercommon;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author pengluyu
 */
public class FileUtils {
    /**
     * Read byte array from file descriptor
     * 
     * @param fd
     * @return
     * @throws IOException
     */
    public static byte[] read(FileDescriptor fd) throws IOException {
        FileInputStream inStream = new FileInputStream(fd);
        return read(inStream);
    }

    /**
     * Read byte array from File
     * 
     * @param filepath
     * @return
     * @throws IOException
     */
    public static byte[] read(String filepath) throws IOException {
        FileInputStream inStream = new FileInputStream(new File(filepath));
        return read(inStream);
    }

    /**
     * Read byte array from File
     * 
     * @param fd
     * @return
     * @throws IOException
     */
    public static byte[] read(File file) throws IOException {
        FileInputStream inStream = new FileInputStream(file);
        return read(inStream);
    }

    private static byte[] read(FileInputStream inStream) throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[0x100000];
        int len = -1;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }

        outStream.flush();
        outStream.close();
        inStream.close();
        return outStream.toByteArray();
    }

    public static void write(byte[] data, File outFile) throws IOException {
        if (null != data && null != outFile) {
            FileOutputStream outStream = new FileOutputStream(outFile);
            write(data, outStream);
        }
    }

    public static void write(byte[] data, String outFilepath) throws IOException {
        if (null != data && null != outFilepath) {
            FileOutputStream outStream = new FileOutputStream(outFilepath);
            write(data, outStream);
        }
    }

    private static void write(byte[] data, FileOutputStream outStream) throws IOException {
        outStream.write(data);
    }
}
