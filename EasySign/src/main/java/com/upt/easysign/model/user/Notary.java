package com.upt.easysign.model.user;

import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.*;
import lombok.Data;
import lombok.ToString;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.*;
import java.security.*;
import java.security.cert.Certificate;

@Entity
@Table(name = "notary")
@Data
public class Notary extends User{
    @ToString.Exclude
    @Column(name = "certificate")
    private byte[] certificate;

    public byte[] signDocument(byte[] sourceFileBytes,
                               String signatureFieldName,
                               String password) throws GeneralSecurityException, IOException {

        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        BouncyCastleProvider bouncyCastleProvider = new BouncyCastleProvider();
        Security.addProvider(bouncyCastleProvider);

        keyStore.load(new ByteArrayInputStream(certificate), password.toCharArray());
        String alias = keyStore.aliases().nextElement();
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, password.toCharArray());
        Certificate[] certificateChain = keyStore.getCertificateChain(alias);

        return digitalSignature(sourceFileBytes,
                            signatureFieldName,
                            certificateChain,
                            privateKey,
                            DigestAlgorithms.SHA256,
                            bouncyCastleProvider.getName(),
                            PdfSigner.CryptoStandard.CMS,
                            "Reason", "Location");
    }

    public static byte[] digitalSignature(byte[] sourceFileBytes,
                                          String signatureFieldName,
                                          Certificate[] certificateChain,
                                          PrivateKey privateKey,
                                          String digestAlgorithm, String bouncyCastleProvider, PdfSigner.CryptoStandard cryptoStandardSubFilter,
                                          String reason, String location) throws GeneralSecurityException, IOException {

        PdfReader pdfReader = new PdfReader(new ByteArrayInputStream(sourceFileBytes));
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfSigner pdfSigner = new PdfSigner(pdfReader, outputStream, new StampingProperties());

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

        byte[] signedPdfBytes = outputStream.toByteArray();
        outputStream.close();

        return signedPdfBytes;
    }
}
