import java.io.*;
import java.util.zip.*;
import java.nio.file.*;

public class ReplaceDex {
    public static void main(String[] args) throws Exception {
        Path inPath = Paths.get(args[0]);
        Path newDex = Paths.get(args[1]);
        Path outPath = Paths.get(args[2]);
        
        byte[] newDexBytes = Files.readAllBytes(newDex);
        
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outPath.toFile()))) {
            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(inPath.toFile()))) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    if (entry.getName().equals("classes.dex")) {
                        zis.closeEntry();
                        // Replace with new dex, using DEFLATED (original was DEFLATED)
                        ZipEntry newEntry = new ZipEntry("classes.dex");
                        newEntry.setMethod(ZipEntry.DEFLATED);
                        zos.putNextEntry(newEntry);
                        zos.write(newDexBytes);
                        zos.closeEntry();
                        System.out.println("Replaced classes.dex (" + newDexBytes.length + " bytes)");
                    } else {
                        // Copy other entries preserving method
                        ZipEntry outEntry = new ZipEntry(entry.getName());
                        int method = entry.getMethod();
                        outEntry.setMethod(method);
                        byte[] data = zis.readAllBytes();
                        
                        if (method == ZipEntry.STORED) {
                            outEntry.setSize(data.length);
                            outEntry.setCompressedSize(data.length);
                            CRC32 crc = new CRC32();
                            crc.update(data);
                            outEntry.setCrc(crc.getValue());
                        }
                        
                        zos.putNextEntry(outEntry);
                        zos.write(data);
                        zos.closeEntry();
                        zis.closeEntry();
                    }
                }
            }
        }
        System.out.println("Done: " + outPath);
    }
}
