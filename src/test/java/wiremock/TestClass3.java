package wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.response.Response;
import lombok.extern.log4j.Log4j2;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static io.restassured.RestAssured.given;

@Log4j2
public class TestClass3 {

    MockBase mockBase;

    @BeforeClass
    public void setUp() {
        mockBase = new MockBase();
        mockBase.turnOffWiremockLogging();
    }


    @Test
    public void test001_dynamicPort() {
        WireMockServer wireMockServer = new WireMockServer(wireMockConfig().dynamicPort());
        wireMockServer.start();
        log.info("Started WireMockServer at Port: " + wireMockServer.port());
        wireMockServer.stubFor(get(urlEqualTo("/some/thing"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "text/plain")
                        .withBody("Hello World!")));
        Response response = given().spec(mockBase.setRALogFilter())
                .port(wireMockServer.port())
                .when()
                .get("/some/thing")
                .then()
                .extract()
                .response();
        Assert.assertEquals(response.getBody().asString(), "Hello World!");
        wireMockServer.shutdown();
    }

}
