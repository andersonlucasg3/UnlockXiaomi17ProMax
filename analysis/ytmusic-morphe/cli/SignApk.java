import com.android.apksig.ApkSigner;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.List;

public class SignApk {
    public static void main(String[] args) throws Exception {
        // args: keystore input.apk storePass alias keyPass output.apk
        Security.addProvider(new BouncyCastleProvider());
        KeyStore ks = KeyStore.getInstance("BKS");
        try (FileInputStream fis = new FileInputStream(args[0])) {
            ks.load(fis, args[2].isEmpty() ? null : args[2].toCharArray());
        }
        PrivateKey key = (PrivateKey) ks.getKey(args[3], args[4].toCharArray());
        if (key == null) throw new IllegalStateException("Chave privada nao encontrada para alias " + args[3]);
        X509Certificate cert = (X509Certificate) ks.getCertificate(args[3]);
        ApkSigner.SignerConfig cfg = new ApkSigner.SignerConfig.Builder(args[3], key, List.of(cert)).build();
        new ApkSigner.Builder(List.of(cfg))
                .setInputApk(new File(args[1]))
                .setOutputApk(new File(args[5]))
                .build()
                .sign();
        System.out.println("SIGNED OK: " + args[5]);
    }
}
