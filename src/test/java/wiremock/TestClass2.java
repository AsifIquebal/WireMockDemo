package wiremock;

import io.restassured.response.Response;
import lombok.extern.log4j.Log4j2;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;

@Log4j2
public class TestClass2 {

    Stubs stubs;
    MockBase mockBase;

    @BeforeClass
    public void setUp() {
        mockBase = new MockBase();
        mockBase.turnOffWiremockLogging();
        mockBase.startWireMockServerOnThisPort(2345);
        stubs = new Stubs();
    }

    @AfterClass
    public void tearDown() {
        mockBase.removeAllStub();
        mockBase.stopWireMockServer();
        mockBase.closePrintStream();

    }

    @Test
    public void testForSpecificPort() {
        mockBase.getWireMockServer().stubFor(get(urlEqualTo("/emp/v2"))
                .willReturn(aResponse()
                        .withBody("Employee 001 \nEmployee 002\n")
                        .withStatus(200)
                ));
        Response response = given().spec(mockBase.setRALogFilter())
                .port(2345)
                .when()
                .get("/emp/v2")
                .then()
                .extract()
                .response();
        System.out.println(response.getStatusLine());
        System.out.println(response.asString());

    }


}
