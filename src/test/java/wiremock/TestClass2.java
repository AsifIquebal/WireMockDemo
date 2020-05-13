package wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import io.restassured.response.Response;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.restassured.RestAssured.given;

public class TestClass2 {

    Stubs stubs;
    MockBase mockBase;
    WireMockServer wireMockServer;

    @BeforeClass
    public void setUp() {
        mockBase = new MockBase();
        mockBase.turnOffWiremockLogging();
        wireMockServer = new WireMockServer(wireMockConfig().port(2345));
        wireMockServer.start();
        System.out.println(wireMockServer.port());
        stubs = new Stubs();
    }

    @AfterClass
    public void tearDown() {
        mockBase.stopWireMockServer();
        mockBase.closePrintStream();
    }

    @Test
    public void testForSpecificPort() {
        StubMapping stubMapping = wireMockServer.stubFor(get(urlEqualTo("/emp/v2"))
                .willReturn(aResponse()
                        .withBody("Employee 1\nEmployee2\n")
                        .withStatus(200)
                ));
        Response response = given().spec(mockBase.setRALogFilter())
                .baseUri("http://localhost:2345/")
                .basePath("/emp")
                .port(2345)
                .when()
                .get("v2")
                .then()
                .extract()
                .response();
        System.out.println(response.getStatusLine());
        System.out.println(response.asString());
        wireMockServer.removeStub(stubMapping);
    }


}
