/**
 * @author pengluyu
 *
 * FileDescriptorUtil.java
 * 7:29:55 AM 2014
 */

package cn.louispeng.imagefilter.bindercommon;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author pengluyu
 */
public class FileDescriptorUtil {
    /**
     * Read byte array from file descriptor
     * 
     * @param fd
     * @return
     * @throws IOException
     */
    public static byte[] read(FileDescriptor fd) throws IOException {
        FileInputStream inStream = new FileInputStream(fd);
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[0x100000];
        int len = -1;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }

        outStream.close();
        inStream.close();
        return outStream.toByteArray();
    }

    /**
     * Write byte array to file descriptor
     * 
     * @param fd
     * @param data
     * @throws IOException
     */
    public static void write(FileDescriptor fd, byte[] data) throws IOException {
        FileOutputStream outStream = new FileOutputStream(fd);
        outStream.write(data);
        outStream.close();
    }
}
