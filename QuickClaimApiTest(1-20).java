package com.example.api.tests;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class QuickClaimApiTests {

    @Autowired
    private WebTestClient webTestClient;

    // Method to set required headers
    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("RequestID", "33222wsee34444");
        return headers;
    }

    /** Test 1: Successful Claim Creation **/
    @Test
    public void testQuickClaimCreation_Success() {
        String requestBody = """
        {
            "claimId": 0,
            "claimType": "16001",
            "firstName": "John",
            "lastName": "Doe",
            "phoneNumber": "6470000000",
            "emailAddress": "john.doe@example.com",
            "legalDisclaimer": {
                "lawFirmRepresentation": "Y",
                "lawFirmName": "ABC Law Firm"
            }
        }
        """;

        webTestClient.post()
            .uri("/dev/api/i-claims/v1/quick-claim")
            .headers(httpHeaders -> httpHeaders.addAll(getHeaders()))
            .bodyValue(requestBody)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.CREATED)
            .expectBody()
            .jsonPath("$.status").isEqualTo("CREATED")
            .jsonPath("$.data[0].claimReferenceNumber").exists();
    }

    /** Test 2: Invalid Email Address Format **/
    @Test
    public void testQuickClaimCreation_InvalidEmail() {
        String requestBody = """
        {
            "emailAddress": "invalid-email",
            "claimId": 0,
            "claimType": "16001"
        }
        """;

        webTestClient.post()
            .uri("/dev/api/i-claims/v1/quick-claim")
            .headers(httpHeaders -> httpHeaders.addAll(getHeaders()))
            .bodyValue(requestBody)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
            .expectBody()
            .jsonPath("$.status").isEqualTo("ERROR")
            .jsonPath("$.message").exists();
    }

    /** Test 3: Empty Request Body **/
    @Test
    public void testQuickClaimCreation_EmptyBody() {
        webTestClient.post()
            .uri("/dev/api/i-claims/v1/quick-claim")
            .headers(httpHeaders -> httpHeaders.addAll(getHeaders()))
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST);
    }

    /** Test 4: Missing Header **/
    @Test
    public void testQuickClaimCreation_MissingHeader() {
        String requestBody = """
        {
            "claimId": 0,
            "claimType": "16001",
            "firstName": "John",
            "lastName": "Doe"
        }
        """;

        webTestClient.post()
            .uri("/dev/api/i-claims/v1/quick-claim")
            .bodyValue(requestBody)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST);
    }

    /** Test 5: SQL Injection Attempt **/
    @Test
    public void testQuickClaimCreation_SQLInjection() {
        String requestBody = """
        {
            "firstName": "' OR 1=1 --",
            "claimId": 0,
            "claimType": "16001"
        }
        """;

        webTestClient.post()
            .uri("/dev/api/i-claims/v1/quick-claim")
            .headers(httpHeaders -> httpHeaders.addAll(getHeaders()))
            .bodyValue(requestBody)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST);
    }

    /** Test 6: Unsupported Media Type **/
    @Test
    public void testQuickClaimCreation_UnsupportedMediaType() {
        String requestBody = """
        {
            "claimId": 0,
            "claimType": "16001"
        }
        """;

        webTestClient.post()
            .uri("/dev/api/i-claims/v1/quick-claim")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
            .bodyValue(requestBody)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    /** Test 7: Future Date in Request **/
    @Test
    public void testQuickClaimCreation_FutureDate() {
        String requestBody = """
        {
            "createdOn": "2030-01-01",
            "claimId": 0,
            "claimType": "16001"
        }
        """;

        webTestClient.post()
            .uri("/dev/api/i-claims/v1/quick-claim")
            .headers(httpHeaders -> httpHeaders.addAll(getHeaders()))
            .bodyValue(requestBody)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST);
    }

    /** Test 8: XSS Attack Attempt **/
    @Test
    public void testQuickClaimCreation_XSSAttack() {
        String requestBody = """
        {
            "firstName": "<script>alert('XSS')</script>",
            "claimId": 0,
            "claimType": "16001"
        }
        """;

        webTestClient.post()
            .uri("/dev/api/i-claims/v1/quick-claim")
            .headers(httpHeaders -> httpHeaders.addAll(getHeaders()))
            .bodyValue(requestBody)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST);
    }

    /** Test 9: Duplicate Submission **/
    @Test
    public void testQuickClaimCreation_DuplicateSubmission() {
        String requestBody = """
        {
            "claimId": 1,
            "claimType": "16001",
            "firstName": "John"
        }
        """;

        webTestClient.post()
            .uri("/dev/api/i-claims/v1/quick-claim")
            .headers(httpHeaders -> httpHeaders.addAll(getHeaders()))
            .bodyValue(requestBody)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }

    /** Test 10: Missing Required Fields **/
    @Test
    public void testQuickClaimCreation_MissingFields() {
        String requestBody = """
        {
            "firstName": "John",
            "lastName": "Doe"
        }
        """;

        webTestClient.post()
            .uri("/dev/api/i-claims/v1/quick-claim")
            .headers(httpHeaders -> httpHeaders.addAll(getHeaders()))
            .bodyValue(requestBody)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST);
    }

    /** Test 11: Invalid Claim Type **/
    @Test
    public void testQuickClaimCreation_InvalidClaimType() {
        String requestBody = """
        {
            "claimId": 0,
            "claimType": "99999",
            "firstName": "John",
            "lastName": "Doe"
        }
        """;

        webTestClient.post()
            .uri("/dev/api/i-claims/v1/quick-claim")
            .headers(httpHeaders -> httpHeaders.addAll(getHeaders()))
            .bodyValue(requestBody)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST);
    }

    /** Test 12: Maximum Field Length Exceeded **/
    @Test
    public void testQuickClaimCreation_MaxFieldLengthExceeded() {
        String requestBody = """
        {
            "firstName": "JohnJohnJohnJohnJohnJohnJohnJohnJohnJohn",
            "lastName": "Doe",
            "claimId": 0,
            "claimType": "16001"
        }
        """;

        webTestClient.post()
            .uri("/dev/api/i-claims/v1/quick-claim")
            .headers(httpHeaders -> httpHeaders.addAll(getHeaders()))
            .bodyValue(requestBody)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST);
    }

    /** Test 13: Invalid Phone Number Format **/
    @Test
    public void testQuickClaimCreation_InvalidPhoneNumberFormat() {
        String requestBody = """
        {
            "phoneNumber": "invalid-phone",
            "claimId": 0,
            "claimType": "16001",
            "firstName": "John",
            "lastName": "Doe"
        }
        """;

        webTestClient.post()
            .uri("/dev/api/i-claims/v1/quick-claim")
            .headers(httpHeaders -> httpHeaders.addAll(getHeaders()))
            .bodyValue(requestBody)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST);
    }

    /** Test 14: Invalid Marital Status Code **/
    @Test
    public void testQuickClaimCreation_InvalidMaritalStatus() {
        String requestBody = """
        {
            "maritalStatus": "99999",
            "claimId": 0,
            "claimType": "16001",
            "firstName": "John",
            "lastName": "Doe"
        }
        """;

        webTestClient.post()
            .uri("/dev/api/i-claims/v1/quick-claim")
            .headers(httpHeaders -> httpHeaders.addAll(getHeaders()))
            .bodyValue(requestBody)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST);
    }

    /** Test 15: Invalid JSON Format **/
    @Test
    public void testQuickClaimCreation_InvalidJSONFormat() {
        String requestBody = """
        {
            "claimId": 0,
            "claimType": "16001",
            "firstName": "John",
            "lastName": "Doe",
        """; // Invalid JSON syntax

        webTestClient.post()
            .uri("/dev/api/i-claims/v1/quick-claim")
            .headers(httpHeaders -> httpHeaders.addAll(getHeaders()))
            .bodyValue(requestBody)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST);
    }

    /** Test 16: Missing Consent to Transfer **/
    @Test
    public void testQuickClaimCreation_MissingConsentToTransfer() {
        String requestBody = """
        {
            "claimId": 0,
            "claimType": "16001",
            "firstName": "John",
            "lastName": "Doe"
        }
        """;

        webTestClient.post()
            .uri("/dev/api/i-claims/v1/quick-claim")
            .headers(httpHeaders -> httpHeaders.addAll(getHeaders()))
            .bodyValue(requestBody)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST);
    }

    /** Test 17: Invalid Injury Type Code **/
    @Test
    public void testQuickClaimCreation_InvalidInjuryType() {
        String requestBody = """
        {
            "injuryType": "99999",
            "claimId": 0,
            "claimType": "16001",
            "firstName": "John",
            "lastName": "Doe"
        }
        """;

        webTestClient.post()
            .uri("/dev/api/i-claims/v1/quick-claim")
            .headers(httpHeaders -> httpHeaders.addAll(getHeaders()))
            .bodyValue(requestBody)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST);
    }

    /** Test 18: Valid but Minimal Input **/
    @Test
    public void testQuickClaimCreation_MinimalInputSuccess() {
        String requestBody = """
        {
            "claimId": 0,
            "claimType": "16001",
            "firstName": "John",
            "lastName": "Doe"
        }
        """;

        webTestClient.post()
            .uri("/dev/api/i-claims/v1/quick-claim")
            .headers(httpHeaders -> httpHeaders.addAll(getHeaders()))
            .bodyValue(requestBody)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.CREATED);
    }

    /** Test 19: Invalid Preferred Communication Method **/
    @Test
    public void testQuickClaimCreation_InvalidPreferredCommunicationMethod() {
        String requestBody = """
        {
            "preferredCommunicationMethodType": "99999",
            "claimId": 0,
            "claimType": "16001",
            "firstName": "John",
            "lastName": "Doe"
        }
        """;

        webTestClient.post()
            .uri("/dev/api/i-claims/v1/quick-claim")
            .headers(httpHeaders -> httpHeaders.addAll(getHeaders()))
            .bodyValue(requestBody)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST);
    }

    /** Test 20: Special Characters in Last Name **/
    @Test
    public void testQuickClaimCreation_SpecialCharactersInLastName() {
        String requestBody = """
        {
            "lastName": "!@#$%^&*",
            "claimId": 0,
            "claimType": "16001",
            "firstName": "John"
        }
        """;

        webTestClient.post()
            .uri("/dev/api/i-claims/v1/quick-claim")
            .headers(httpHeaders -> httpHeaders.addAll(getHeaders()))
            .bodyValue(requestBody)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
