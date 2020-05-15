package wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.response.Response;
import lombok.extern.log4j.Log4j2;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import wiremock.myPojos.Student;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;

@Log4j2
public class Demo_withTestNG {

    WireMockServer wireMockServer;

    @BeforeClass
    public void setUp() {
        wireMockServer = new WireMockServer();
        wireMockServer.start();
    }

    @AfterClass
    public void tearDown() {
        if (null != wireMockServer && wireMockServer.isRunning()) {
            wireMockServer.shutdownServer();
        }
    }

    @Test
    public void test1() {
        stubFor(get(urlEqualTo("/api/1"))
                .willReturn(
                        aResponse()
                                .withHeader("Content-Type", "text/plain")
                                .withHeader("Accept", "text")
                                .withStatusMessage("Everything goes well...")
                                .withStatus(200)
                                .withBody("End point called successfully...")

                )
        );
        Response response =
                given()
                        .when()
                        .get("/api/1")
                        .then()
                        .extract()
                        .response();
        log.info(response.asString());
    }

    @Test
    public void test2() {
        Student student = new Student();
        student.name("qsif");
        student.roll(2);
        student.std(10);

        Student student2 = new Student().name("test").std(1).roll(2);

        stubFor(get(urlEqualTo("/getinfo/johanHaleby"))
                .willReturn(
                        aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withStatus(200)
                                .withBodyFile("test.json")
                )
        );
        Response response =
                given()
                        .when()
                        .get("/getinfo/johanHaleby")
                        .then()
                        .extract()
                        .response();
        System.out.println(response.asString());
    }

    @Test
    public void testEveryThing() {
        stubFor(any(urlPathEqualTo("/everything"))
                .withHeader("Accept", containing("xml"))
                .withCookie("session", matching(".*12345.*"))
                .withQueryParam("search_term", equalTo("WireMock"))
                .withBasicAuth("jeff@example.com", "jeff")
                .withRequestBody(equalToXml("<search-results />"))
                .withRequestBody(matchingXPath("//search-results"))
                .withMultipartRequestBody(
                        aMultipart()
                                .withName("info")
                                .withHeader("Content-Type", containing("charset"))
                                .withBody(equalToJson("{}"))
                )
                .willReturn(aResponse()));

        Response response = given()
                .header("Accept","xml")
                .header("Authorization","Basic amVmZkBleGFtcGxlLmNvbTpqZWZm")
                .cookie("session","12345")
                .queryParam("search_term","WireMock")
                .body("<search-results />")
                .body("//search-results")
                //.auth().basic("jeff@example.com", "jeffteenjefftyjeff")

                .multiPart("info","{}","charset")
                .get("/everything")
                .then().extract().response();


    }


}
