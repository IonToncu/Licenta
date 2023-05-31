# Most important notes
Issues
Frontend:

    Login

        Get data from backend for requests      [X]
        Fix authentication manager              [X]
        Rewrite logic in service package        [X]
        Register
        Write register page                     [X]

    Test:

        Login as Customer                       [X]
        Login as Notary                         [X]

        Customer:

            Home page                           [X]
            All folders                         [X]
            Client page                         [X]
            Standard user information           [X]
            Create Folder page                  [X]
            Add files in folders                [X]
            Propose to sign                     [X]
        Notary:
            Home page                           [ ]
            Public requests                     [X]

            View folder                         [X]
            Personal list of folders            [X]

            Sign/reject folder/file             [X]
            Client page                         [ ]
            Standard user information           [-]

Backend:


    Uploading file                              [X]
    Add digital sign for notary                 [X]
    Make request safety                         [X]

    Customer: 
        post folder                             [-]
        Home page for notary:                   [-]
        Public folder                           [-]
        Personal signed or not folder           [-]
        Sign folder/file                        [-]

    Notary:
        Sign document                           [-]

## Explanations Notes:

### Signature:
The keys are stored in the file `'myKeyStore.p12'`, which requires a `'decryption password'` for the file.
To access the private key, a password for the `'private key'` is needed.
The signature is made using `'SHA256'`.
```
    1:The 'digitalSignature' method is responsible for applying the digital signature to a PDF file. The parameters of this method include the source file name (the PDF to be signed), the signature field name in the source PDF, the output file name (the signed PDF), the signer's certificate chain, the signer's private key, the digest algorithm (e.g., SHA256), the Bouncy Castle provider (used for cryptography), the cryptographic standard of the digital signature (e.g., CMS), the reason and location of the signature.

    2: The 'main' method is the main method of the application and is responsible for calling the 'digitalSignature' method and verifying the digital signature after it is applied. In this method, the private key and the signer's certificate chain are loaded from a keystore file. After applying the digital signature, the existence of the digital signature is checked and the signature field name is displayed.

    3: The 'createKeyStore' method is responsible for creating and saving a keystore file that contains the signer's private key and certificate chain.

    4: The 'generateSelfSignedCertificate' method is responsible for generating a self-signed X.509 certificate used to sign the PDF document.
```


