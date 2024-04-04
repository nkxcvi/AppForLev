package org.example;

import kz.gov.pki.kalkan.asn1.cryptopro.CryptoProObjectIdentifiers;
import kz.gov.pki.kalkan.asn1.knca.KNCAObjectIdentifiers;
import kz.gov.pki.kalkan.asn1.pkcs.PKCSObjectIdentifiers;
import kz.gov.pki.kalkan.jce.provider.KalkanProvider;
import kz.gov.pki.kalkan.jce.provider.cms.CMSProcessable;
import kz.gov.pki.kalkan.jce.provider.cms.CMSProcessableByteArray;
import kz.gov.pki.kalkan.jce.provider.cms.CMSSignedData;
import kz.gov.pki.kalkan.jce.provider.cms.CMSSignedDataGenerator;
import kz.gov.pki.kalkan.xmldsig.KncaXS;
import org.apache.xml.security.encryption.XMLCipherParameters;
import org.apache.xml.security.utils.Base64;
import org.apache.xml.security.utils.Constants;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CertStore;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Enumeration;

public class ECPHelper {
    private static final String SIGN_METHOD_GOST = Constants.MoreAlgorithmsSpecNS + "gost34310-gost34311";
    private static final String DIGEST_METHOD_GOST = Constants.MoreAlgorithmsSpecNS + "gost34311";
    private static final String SIGN_METHOD_RSA = Constants.MoreAlgorithmsSpecNS + "rsa-sha1";
    private static final String DIGEST_METHOD_RSA = Constants.MoreAlgorithmsSpecNS + "sha1";
    private static final String SIGN_METHOD_RSA_256 = Constants.MoreAlgorithmsSpecNS + "rsa-sha256";
    private static final String DIGEST_METHOD_RSA_256 = XMLCipherParameters.SHA256;

    private static final String KEY_IIN = "SERIALNUMBER=IIN";
    private static final String KEY_BIN = "OU=BIN";
    private static final String KEY_ORGANISATION_NAME = "O=\"";
    private static final String KEY_NAME1 = "CN=";
    private static final String KEY_NAME2 = "GIVENNAME=";
    private static final String KEY_TRUST_CENTER = "CN=";


    private static final String LOG_TAG = "ECPHelper";

    static {
        // Добавление провайдера в java.security.Security
        boolean exists = false;
        Provider[] providers = Security.getProviders();
        for (Provider p : providers) {
            if (p.getName().equals(KalkanProvider.PROVIDER_NAME)) {
                exists = true;
            }
        }
        if (!exists) {
            Security.addProvider(new KalkanProvider());
            KncaXS.loadXMLSecurity();
        } else {
            KncaXS.loadXMLSecurity();
        }
    }

    public String createPKCS7(String text, byte[] ecp, String password, boolean attached) {

        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            InputStream is = new ByteArrayInputStream(ecp);
            ks.load(is, password.toCharArray());
            is.close();

            Enumeration en = ks.aliases();
            String alias = null;
            while (en.hasMoreElements()) {
                alias = en.nextElement().toString();
            }

            PrivateKey privateKey = (PrivateKey) ks.getKey(alias, password.toCharArray());
            X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
            X509Certificate chain[] = new X509Certificate[]{cert};

            CertStore chainStore = CertStore.getInstance("Collection",
                    new CollectionCertStoreParameters(Arrays.asList(chain)), KalkanProvider.PROVIDER_NAME);
            CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
            if (cert.getSigAlgOID().equals(PKCSObjectIdentifiers.sha1WithRSAEncryption.getId())) {
                generator.addSigner(privateKey, cert, CMSSignedDataGenerator.DIGEST_SHA1);
            } else if (cert.getSigAlgOID().equals(PKCSObjectIdentifiers.sha256WithRSAEncryption.getId())) {
                generator.addSigner(privateKey, cert, CMSSignedDataGenerator.DIGEST_SHA256);
            } else if (cert.getSigAlgOID().equals(KNCAObjectIdentifiers.gost34311_95_with_gost34310_2004.getId())) {
                generator.addSigner(privateKey, cert, CMSSignedDataGenerator.DIGEST_GOST34311_95);
            } else if (cert.getSigAlgOID().equals(CryptoProObjectIdentifiers.gostR3411_94_with_gostR34310_2004.getId())) {
                generator.addSigner(privateKey, cert, CMSSignedDataGenerator.DIGEST_GOST3411_GT);
            } else {
                return null;
            }
            generator.addCertificatesAndCRLs(chainStore);

            CMSProcessable content = new CMSProcessableByteArray(text.getBytes());

            CMSSignedData signedData = generator.generate(content, attached, KalkanProvider.PROVIDER_NAME);
            byte[] signedDataEncoded = signedData.getEncoded();

            return new String(Base64.encode(signedDataEncoded, 0));
        } catch (Exception e) {
            e.printStackTrace();
            return "error: " + e.getLocalizedMessage();
        }
    }
}
