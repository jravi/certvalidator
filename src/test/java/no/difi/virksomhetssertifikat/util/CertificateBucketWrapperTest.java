package no.difi.virksomhetssertifikat.util;

import no.difi.virksomhetssertifikat.ChainValidator;
import no.difi.virksomhetssertifikat.ValidatorBuilder;
import no.difi.virksomhetssertifikat.ValidatorHelper;
import no.difi.virksomhetssertifikat.api.CertificateBucket;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test exists to show potential use.
 */
public class CertificateBucketWrapperTest {

    @Test
    public void simple() throws Exception {
        // Load keystore
        KeystoreCertificateBucket keystoreCertificateBucket = new KeystoreCertificateBucket(getClass().getResourceAsStream("/peppol-test.jks"), "peppol");
        // Fetch root certificate from keystore
        CertificateBucket rootCertificates = keystoreCertificateBucket.toSimple("peppol-root");
        // Define a wrapper for intermediate certificates, currently empty
        CertificateBucketWrapper intermediateCertificates = new CertificateBucketWrapper(null);

        // Build the validator
        ValidatorHelper validator = ValidatorBuilder.newInstance()
                .append(new ChainValidator(rootCertificates, intermediateCertificates))
                .build();

        // See, no certificates inside wrapper!
        Assert.assertNull(intermediateCertificates.getCertificateBucket());

        // Set intermediate certificate
        intermediateCertificates.setCertificateBucket(keystoreCertificateBucket.toSimple("peppol-ap"));
        // Validate!
        validator.validate(getClass().getResourceAsStream("/peppol-test-ap-difi.cer"));

        try {
            // Currently not valid
            validator.validate(getClass().getResourceAsStream("/peppol-test-smp-difi.cer"));
            Assert.fail("Exception expected!");
        } catch (Exception e) {
            // No action
        }

        // Change intermediate certificate
        intermediateCertificates.setCertificateBucket(keystoreCertificateBucket.toSimple("peppol-smp"));
        // Validate!
        validator.validate(getClass().getResourceAsStream("/peppol-test-smp-difi.cer"));

        try {
            // Currently not valid
            validator.validate(getClass().getResourceAsStream("/peppol-test-ap-difi.cer"));
            Assert.fail("Exception expected!");
        } catch (Exception e) {
            // No action
        }

        // Add certificate to existing bucket inside wrapper
        keystoreCertificateBucket.toSimple((SimpleCertificateBucket) intermediateCertificates.getCertificateBucket(), "peppol-ap");

        // Validate!
        validator.validate(getClass().getResourceAsStream("/peppol-test-ap-difi.cer"));
        // Validate!
        validator.validate(getClass().getResourceAsStream("/peppol-test-smp-difi.cer"));
    }

}