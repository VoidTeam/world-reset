package net.voidteam.wreset;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by Robby Duke on 1/4/15.
 * Copyright (c) 2015
 */
public class ZipDirectory {
    public static void zipDirectory(String srcDir, String tgtZipfile) throws IOException {
        File directory = new File(srcDir);

        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("No such directory :  " + srcDir);
        }

        System.out.println("Zipping directory : " + srcDir);

        /**
         * Create ZIP file
         */
        ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(tgtZipfile));
        zipDir(zipOut, directory);
        zipOut.close();
    }


    private static void zipDir(ZipOutputStream zipOut, File directory) {

        try {
            String [] filePaths = directory.list();
            FileInputStream fileInput = null;
            byte [] buf = new byte[1024];
            String entryName = null;
            int index = 0;
            File child = null;

            /**
             * Add each file as ZIP entry
             */
            for (int i = 0 ; i < filePaths.length; i++) {
                child = new File(directory, filePaths[i]);
                entryName = child.getAbsolutePath();

                /**
                 * Remove the drive prefix,
                 *
                 * C:\NEW2\ABC   to NEW2\ABC
                 * C:\NEW2\file_ops.txt   to NEW2\file_ops.txt
                 * C:\NEW2\CCCC\AAA\IPC.LOG to NEW2\CCCC\AAA\IPC.LOG
                 *
                 * If we don't remove the prefix, a directory
                 * named C: is getting created in the ZIP file.
                 * To avoid this remove the directory prefix.
                 */
                index = entryName.indexOf(File.separator);
                if (index > 0) {
                    entryName = entryName.substring(index + 1);
                }

                System.out.println("Adding : " + child.getAbsolutePath());

                /**
                 * Zip child directory recursively
                 */
                if (child.isDirectory()) {
                    /**
                     * Add ZIP directory entry
                     */
                    zipOut.putNextEntry(new ZipEntry(entryName + "/"));
                    zipOut.closeEntry();

                    /**
                     * Iterate child directory
                     */
                    zipDir(zipOut, child);
                } else {
                    /**
                     * Zip actual file content
                     */
                    fileInput = new FileInputStream(child);

                    /**
                     * Add ZIP file entry
                     */
                    zipOut.putNextEntry(new ZipEntry(entryName));

                    /**
                     * Copy file content to ZIP stream
                     */
                    int len;
                    while ((len = fileInput.read(buf)) > 0) {
                        zipOut.write(buf, 0, len);
                    }

                    fileInput.close();
                    zipOut.closeEntry();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
