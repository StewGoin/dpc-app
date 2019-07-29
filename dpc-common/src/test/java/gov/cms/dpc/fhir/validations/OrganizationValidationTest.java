package gov.cms.dpc.fhir.validations;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import gov.cms.dpc.fhir.DPCIdentifierSystem;
import gov.cms.dpc.fhir.validations.profiles.OrganizationProfile;
import org.hl7.fhir.dstu3.hapi.ctx.DefaultProfileValidationSupport;
import org.hl7.fhir.dstu3.hapi.validation.FhirInstanceValidator;
import org.hl7.fhir.dstu3.hapi.validation.ValidationSupportChain;
import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.dstu3.model.Reference;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class OrganizationValidationTest {

    private static FhirValidator fhirValidator;
    private static DPCProfileSupport dpcModule;
    private static FhirContext ctx;

    @BeforeAll
    static void setup() {
        ctx = FhirContext.forDstu3();
        final FhirInstanceValidator instanceValidator = new FhirInstanceValidator();

        fhirValidator = ctx.newValidator();
        fhirValidator.setValidateAgainstStandardSchematron(false);
        fhirValidator.setValidateAgainstStandardSchema(false);
        fhirValidator.registerValidatorModule(instanceValidator);


        dpcModule = new DPCProfileSupport(ctx);
        final ValidationSupportChain chain = new ValidationSupportChain(new DefaultProfileValidationSupport(), dpcModule);
        instanceValidator.setValidationSupport(chain);
    }

    @Test
    void testIdentifier() {
        final Organization organization = generateFakeOrganization();
        organization.addAddress(generateFakeAddress());

        final ValidationResult result = fhirValidator.validateWithResult(organization);
        assertAll(() -> assertFalse(result.isSuccessful(), "Should have failed validation"),
                () -> assertEquals(1, result.getMessages().size(), "Should have a single failure"));

        organization.addIdentifier().setSystem(DPCIdentifierSystem.MBI.getSystem()).setValue("test-mbi-value");

        final ValidationResult r2 = fhirValidator.validateWithResult(organization);
        assertAll(() -> assertFalse(r2.isSuccessful(), "Should have failed validation"),
                () -> assertEquals(2, r2.getMessages().size(), "Should have two failures for ID"));

        // Add correct NPI
        organization.addIdentifier().setSystem(DPCIdentifierSystem.NPPES.getSystem()).setValue("test-value");

        final ValidationResult r3 = fhirValidator.validateWithResult(organization);
        assertTrue(r3.isSuccessful(), "Should have passed");
    }

    @Test
    void testAddress() {
        final Organization organization = generateFakeOrganization();
        organization.addIdentifier().setSystem(DPCIdentifierSystem.NPPES.getSystem()).setValue("test-value");

        final ValidationResult result = fhirValidator.validateWithResult(organization);
        assertAll(() -> assertFalse(result.isSuccessful(), "Should have failed validation"),
                () -> assertEquals(1, result.getMessages().size(), "Should have a single failure"));

        // Add a text based Address
        organization.addAddress().setText("7500 Security Blvd").setType(Address.AddressType.PHYSICAL).setUse(Address.AddressUse.HOME);

        final ValidationResult r2 = fhirValidator.validateWithResult(organization);
        assertAll(() -> assertFalse(r2.isSuccessful(), "Should have failed validation"),
                () -> assertEquals(6, r2.getMessages().size(), "Should have multiple address failures"));

        // Add valid address
        organization.setAddress(Collections.singletonList(generateFakeAddress()));

        final ValidationResult r3 = fhirValidator.validateWithResult(organization);
        assertTrue(r3.isSuccessful(), "Should have passed");
    }

    private Organization generateFakeOrganization() {
        final Organization organization = new Organization();
        final Meta meta = new Meta();
        meta.addProfile(OrganizationProfile.PROFILE_URI);
        organization.setMeta(meta);
        organization.addEndpoint(new Reference("Endpoint/test-endpoint"));

        organization.setId("test-organization");
        organization.setName("Test Organization");

        return organization;
    }

    private Address generateFakeAddress() {
        final Address address = new Address();
        address.addLine("1800 Pennsylvania Ave NW");
        address.setCity("Washington");
        address.setState("DC");
        address.setPostalCode("20006");
        address.setCountry("US");
        address.setUse(Address.AddressUse.HOME);
        address.setType(Address.AddressType.PHYSICAL);

        return address;
    }
}