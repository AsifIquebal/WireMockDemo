package wiremock;

import com.github.tomakehurst.wiremock.http.Fault;
import lombok.extern.log4j.Log4j2;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.SocketException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;

@Log4j2
public class TestClass3 {

    MockBase mockBase;

    @BeforeClass
    public void setUp() {
        mockBase = new MockBase();
    }

    @AfterClass
    public void tearDown() {
        mockBase.removeResetAllStub();
        mockBase.stopWireMockServer();
        mockBase.closePrintStream();
    }

    @Test(expectedExceptions = NoHttpResponseException.class)
    public void test001_BadResponse_EMPTY_RESPONSE() {
        mockBase.getWireMockServer().stubFor(get(urlEqualTo("/fault/empty"))
                .willReturn(aResponse().withStatus(200).withFault(Fault.EMPTY_RESPONSE)));
        given()
                .when()
                .get("/fault/empty")
                .then()
                .extract()
                .response();
    }

    @Test(expectedExceptions = ClientProtocolException.class)
    public void test002_BadResponse_MALFORMED_RESPONSE_CHUNK() {
        mockBase.getWireMockServer().stubFor(get(urlEqualTo("/fault/malformed"))
                .willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK)));
        given()
                .when()
                .get("/fault/malformed")
                .then()
                .extract()
                .response();
    }

    @Test(expectedExceptions = ClientProtocolException.class)
    public void test003_BadResponse_RANDOM_DATA_THEN_CLOSE() {
        mockBase.getWireMockServer().stubFor(get(urlEqualTo("/fault/close"))
                .willReturn(aResponse().withFault(Fault.RANDOM_DATA_THEN_CLOSE)));
        given()
                .log().all()
                .when()
                .get("/fault/close")
                .then()
                .log().all()
                .extract()
                .response();
    }

    @Test(expectedExceptions = SocketException.class)
    public void test004_BadResponse_CONNECTION_RESET_BY_PEER() {
        mockBase.getWireMockServer().stubFor(get(urlEqualTo("/fault/reset"))
                .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)));
        given()
                .log().all()
                .when()
                .get("/fault/reset")
                .then()
                .log().all()
                .extract()
                .response();
    }
}
