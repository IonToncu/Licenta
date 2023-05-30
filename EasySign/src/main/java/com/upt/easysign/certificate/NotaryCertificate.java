package com.upt.easysign.certificate;

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

import java.io.ByteArrayOutputStream;
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
public class NotaryCertificate {
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

    public static void createCertificate(String firstName, String lastName, String privateKeyPassword, String keyStorePassword) throws Exception {
        BouncyCastleProvider bouncyCastleProvider = new BouncyCastleProvider();
        Security.addProvider(bouncyCastleProvider);

        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        createKeyStore(firstName, lastName, privateKeyPassword, keyStorePassword);

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

    public static byte[] createKeyStore(String firstName,
                                        String lastName,
                                        String privateKeyPassword,
                                        String keyStorePassword) throws Exception {

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(null, null);
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        X509Certificate[] chain = new X509Certificate[1];
        String subjectDN = "CN=" + firstName + " " + lastName + ", OU=Easy Sign,O=Easy Sign";
        chain[0] = generateSelfSignedCertificate(keyPair, subjectDN);
        keyStore.setKeyEntry("myalias", keyPair.getPrivate(), privateKeyPassword.toCharArray(), chain);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        keyStore.store(outputStream, keyStorePassword.toCharArray());
        byte[] keystoreBytes = outputStream.toByteArray();
        outputStream.close();

        return keystoreBytes;
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
