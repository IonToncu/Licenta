# Cele mai importante notite
---
## Probleme
Frontend:

    - Login
        - Get data from backend for requests [X]
    - Register
        - Write register page: [X]

        Test: Login as Customer     [X]
                        as Notar    [-]
        Customer:
            Home page                                       [-]
                -All folders                                [-]
            Client page                                     [-]
                -Standard user information                  [-]
            Create Folder page                              [-]
                -Add files in folders                       [-]
                    -propose to sign                        [-]
        Notar:
            Home page                                       [-]
                -Public requests                            [-]
                    - view folder (get folder as posonal)   [-]

                -Personal list of folders                   [-]
                    - Sign/regect folder/file               [-]
            Client page                                     [-]
                - standard user information                 [-]

Backend:

    - Regenerate project with new name  [X]
        - Uploading file                [X]
    - Add digital sign for notar        [-]
    - Make request safty                [-]
---

## Explicatii
Notite:
    Semnatura:
    cheile sunt pastrate in fisierul `myKeyStore.p12` care are nevoie de o `parola pentru decriptarea` fisierului
    pentru a primi acces la cheia privata este nevoie de o parola pentru `cheie privata`
    Semnatura se face `SHA256`

    1: Metoda "digitalSignature" este responsabilă pentru aplicarea semnăturii digitale pe un fișier PDF. Parametrii acestei metode includ numele fișierului sursă (PDF-ul care urmează să fie semnat), numele câmpului de semnătură din PDF-ul sursă, numele fișierului de ieșire (PDF-ul semnat), lanțul de certificate ale semnatarului, cheia privată a semnatarului, algoritmul digest (de ex. SHA256), furnizorul Bouncy Castle (care este utilizat pentru criptografie), standardul de criptare al semnăturii digitale (de ex. CMS), motivul și locația semnăturii.

    2: Metoda "main" este metoda principală a aplicației și este responsabilă pentru apelarea metodei "digitalSignature" și verificarea semnăturii digitale după aplicarea acesteia. În această metodă, se încarcă cheia privată și lanțul de certificate ale semnatarului dintr-un fișier de tip keystore. După aplicarea semnăturii digitale, se verifică existența semnăturii digitale și se afișează numele câmpului de semnătură.

    3: Metoda "createKeyStore" este responsabilă pentru crearea și salvarea unui fișier de tip keystore care conține cheia privată și lanțul de certificate ale semnatarului.

    4: Metoda "generateSelfSignedCertificate" este responsabilă pentru generarea unui certificat X.509 auto-semnat utilizat pentru a semna documentul PDF.
--- 
# Code for digital sign
```

import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.forms.fields.PdfSignatureFormField;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.*;


import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;

import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Map;


//src/main/resources/logo.png
public class Main {
    public static void digitalSignature(String sourceFile, String signatureFieldName, String outputFile, Certificate[] certificateChain, PrivateKey privateKey, String digestAlgorithm,
                                        String bouncyCastleProvider, PdfSigner.CryptoStandard cryptoStandardSubFilter, String reason, String location)
            throws GeneralSecurityException, IOException {

        PdfReader pdfReader = new PdfReader(sourceFile);
        PdfSigner pdfSigner = new PdfSigner(pdfReader, new FileOutputStream(outputFile), new StampingProperties());

        // Create the signature appearance
        PdfSignatureAppearance pdfSignatureAppearance = pdfSigner.getSignatureAppearance()
                .setReason(reason)
                .setLocation(location);

        // This name corresponds to the name of the field that already exists in the document.
        pdfSigner.setFieldName(signatureFieldName);


        pdfSignatureAppearance.setRenderingMode(PdfSignatureAppearance.RenderingMode.NAME_AND_DESCRIPTION);

        IExternalSignature iExternalSignature = new PrivateKeySignature(privateKey, digestAlgorithm, bouncyCastleProvider);
        IExternalDigest iExternalDigest = new BouncyCastleDigest();

        // Sign the document using the detached mode, CMS, or CAdES equivalent.
        pdfSigner.signDetached(iExternalDigest, iExternalSignature, certificateChain, null, null, null, 0, cryptoStandardSubFilter);
    }

    public static void main(String[] args) throws Exception {
        BouncyCastleProvider bouncyCastleProvider = new BouncyCastleProvider();
        Security.addProvider(bouncyCastleProvider);

        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        createKeyStore();
        keyStore.load(new FileInputStream("mykeystore.p12"), "keystorepassword".toCharArray());
        String alias = keyStore.aliases().nextElement();
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, "mypassword".toCharArray());
        Certificate[] certificateChain = keyStore.getCertificateChain(alias);

        digitalSignature("src/main/resources/test.pdf",
                "vladislav Vladislavovici",
                "src/main/resources/output.pdf",
                certificateChain,
                privateKey,
                DigestAlgorithms.SHA256,
                bouncyCastleProvider.getName(),
                PdfSigner.CryptoStandard.CMS,
                "Reason", "Location");

        PdfDocument pdfDoc = new PdfDocument(new PdfReader("src/main/resources/output.pdf"));
        PdfAcroForm form = PdfAcroForm.getAcroForm(pdfDoc, false);
        Map<String, com.itextpdf.forms.fields.PdfFormField> fields = form.getFormFields();
        for (String name : fields.keySet()) {
            PdfFormField field = fields.get(name);
            if (field instanceof PdfSignatureFormField) {
                System.out.println("Signature name: " + name);
            }
        }
        pdfDoc.close();
    }

    public static void createKeyStore() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, null);
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        X509Certificate[] chain = new X509Certificate[1];
        chain[0] = generateSelfSignedCertificate(keyPair, "CN=Dulgher Dan, OU=My Department,O=My Organization");
        keyStore.setKeyEntry("myalias", keyPair.getPrivate(), "mypassword".toCharArray(), chain);
        keyStore.store(new FileOutputStream("mykeystore.p12"), "keystorepassword".toCharArray());
    }
    public static X509Certificate generateSelfSignedCertificate(KeyPair keyPair, String subjectDN) throws Exception {
        X500Name subject = new X500Name(subjectDN);
        BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());
        Date notBefore = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
        Date notAfter = new Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000);
        X509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(subject, serial, notBefore, notAfter, subject, keyPair.getPublic());
        ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSA").build(keyPair.getPrivate());
        return new JcaX509CertificateConverter().getCertificate(builder.build(signer));
    }

}
```


