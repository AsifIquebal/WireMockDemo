package wiremock;

import com.github.tomakehurst.wiremock.http.ContentTypeHeader;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.jayway.jsonpath.JsonPath;
import io.restassured.response.Response;
import lombok.extern.log4j.Log4j2;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;

@Log4j2
public class TestClass2 {

    Stubs stubs;
    MockBase mockBase;

    @BeforeClass
    public void setUp() {
        mockBase = new MockBase();
        mockBase.startWireMockServerOnThisPort(2345);
        stubs = new Stubs();
    }

    @AfterClass
    public void tearDown() {
        mockBase.removeResetAllStub();
        mockBase.stopWireMockServer();
        mockBase.closePrintStream();
    }

    @Test
    public void test01_ForSpecificPort() {
        mockBase.getWireMockServer().stubFor(get(urlEqualTo("/emp/v2"))
                .willReturn(aResponse()
                        .withBody("Employee 001 \nEmployee 002\n")
                        .withStatus(200)
                ));
        Response response = given().spec(mockBase.getRequestSpecification())
                .port(2345)
                .when()
                .get("/emp/v2")
                .then()
                .extract()
                .response();
        System.out.println(response.asString());
        Assert.assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void test02_Another() {
        mockBase.getWireMockServer().stubFor(get(urlPathEqualTo("/all/gurus"))
                .willReturn(aResponse()
                        .withHeader(ContentTypeHeader.KEY, "application/json")
                        .withBodyFile("test02.json")));
        Response response = given().spec(mockBase.getRequestSpecification())
                .port(2345)
                .when()
                .get("/all/gurus")
                .then()
                .extract().response();
        List<String> list = JsonPath.read(response.asString(), "$.gurus.[?(@.company=='Parkster')].country");
        Assert.assertEquals(list.get(0), "Sweden");

    }

    @Test
    public void test03_getAllCurrentlyRegisteredStubMapping() {
        Response response = given().spec(mockBase.getRequestSpecification())
                .port(2345)
                .when()
                .get("/__admin/mappings")
                .then()
                .extract().response();
        mockBase.printJson(response.asString());
    }

    @Test
    public void test04_getStubByIdPathParam() {
        Response response = given()
                .port(2345)
                .when()
                .get("/__admin/mappings")
                .then()
                .extract().response();
        List<String> ids = JsonPath.read(response.asString(), "$.mappings.[*].id");
        Response response1 = given().spec(mockBase.getRequestSpecification())
                .port(2345)
                .pathParams("pathParam1", ids.get(0))
                .when()
                .get("/__admin/mappings/{pathParam1}")
                .then()
                .extract().response();
    }

    @Test
    public void test05_getAllRequestReceivedByWireMock() {
        Response response = given().spec(mockBase.getRequestSpecification())
                .port(2345)
                .when()
                .get("/__admin/requests")
                .then()
                .extract().response();
        // alternately
        List<ServeEvent> allServeEvents = mockBase.getWireMockServer().getAllServeEvents();
        allServeEvents.forEach(a -> System.out.println(a.getRequest().getAbsoluteUrl()));
    }


}
