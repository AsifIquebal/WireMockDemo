package wiremock;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.ContentTypeHeader;
import com.jayway.jsonpath.JsonPath;
import io.restassured.http.ContentType;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import lombok.extern.log4j.Log4j2;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import wiremock.myPojos.Guru;
import wiremock.myPojos.Student;

import java.io.File;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;

@Log4j2
public class TestClass1 {

    Stubs stubs;
    String token;
    MockBase mockBase;

    @BeforeClass
    public void setUp() {
        mockBase = new MockBase();
        stubs = new Stubs();
        token = mockBase.getAuthToken("asif", "superSecret");
        System.out.println(token);
    }

    @AfterClass
    public void tearDown() {
        mockBase.removeResetAllStub();
        mockBase.stopWireMockServer();
        mockBase.closePrintStream();
    }

    @Test
    public void test01_Sample1() {
        mockBase.getWireMockServer().stubFor(get(urlEqualTo("/api/1"))
                .willReturn(
                        aResponse()
                                .withHeader("Content-Type", "text/plain")
                                .withHeader("Accept", "text")
                                .withStatusMessage("Everything goes well...")
                                .withStatus(200)
                                .withBody("End point called successfully...")
                )
        );
        Response response = given()
                .when()
                .get("/api/1")
                .then()
                .extract()
                .response();
        log.info(response.asString());
    }

    @Test
    public void test02_Sample2() {
        Student student = Student.builder().name("Harry").std(1).roll(2).build();
        System.out.println(student);
        mockBase.getWireMockServer().stubFor(post(urlPathEqualTo("/post/v2"))
                .withRequestBody(matchingJsonPath("$.name"))
                .willReturn(aResponse().withStatus(200)));
        Response response = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .when()
                .body(student)
                .post("/post/v2")
                .then()
                .extract()
                .response();
        Assert.assertEquals(response.getStatusCode(), 200, "Status Code didn't matched...");
    }

    @Test
    public void test03_BasicAuth() {
        stubs.getStubForBasicAuthHeader(mockBase.getWireMockServer());
        Response response = given()
                .header("Authorization", token)
                .when()
                .get("/basic/auth/case-insensitive")
                .then()
                .extract().response();
        log.info(response.asString());
        Assert.assertEquals(response.getStatusCode(), 200, "Failed: Status Code didn't matched");
    }

    @Test
    public void test04_sampleGet_queryParams() {
        stubs.getStubForToolQuery(token, mockBase.getWireMockServer());
        Response response = given()
                .auth().oauth2(token)
                // oauth2 does the same thing, it puts the token into the header
                //.header("Authorization", token)
                .when()
                .queryParam("name", "Wiremock")
                .get("/tool/mocking")
                .then().log().headers().extract().response();
        Headers headers = response.getHeaders();
        for (Header header : headers) {
            if (null != header) {
                System.out.println("Header Name: " + header.getName() + ", Header Value: " + header.getValue());
            }
        }
        int num = JsonPath.read(response.asString(), "$.number");
        mockBase.printJson(response.asString());
        Assert.assertEquals(num, 123, "Failed: Number field mismatch");
    }

    @Test
    public void test05_samplePostJsonPayload() {
        mockBase.getWireMockServer().stubFor(post(urlPathEqualTo("/form/params"))
                .withRequestBody(matchingJsonPath("$.gurus[?(@.tool == 'Rest Assured')]"))
                .willReturn(aResponse().withBodyFile("test02.json")));
        File file = new File("src/test/resources/__files/test02.json");
        Response response = given()
                .when()
                .body(file)
                .post("/form/params")
                .then().extract().response();
        List<Guru> gurus = JsonPath.read(response.asString(), "$.gurus[?(@.tool =~ /^[r|R]est.*/)]");
        System.out.println(gurus.get(0));
    }

    @Test
    public void test06_multiPart() {
        File file = new File("src/test/resources/__files/test02.json");
        mockBase.getWireMockServer().stubFor(any(urlPathEqualTo("/everything"))
                .withHeader("Accept", containing("json"))
                .withCookie("session", matching(".*12345.*"))
                .withQueryParam("search_term", equalTo("WireMock"))
                .withBasicAuth("asif", "superSecret")
                .withMultipartRequestBody(aMultipart()
                        .withName("file")
                        .withHeader("Content-Type", containing("json"))
                        .withBody(containing("gurus"))
                )
                .willReturn(aResponse().withStatus(200)));

        Response response = given()
                .accept(ContentType.JSON)
                .auth().preemptive().basic("asif", "superSecret")
                .contentType("multipart/form-data")
                .cookie("session", "12345")
                .queryParam("search_term", "WireMock")
                .multiPart("file", file, "application/json")
                .when()
                .get("/everything")
                .then().extract().response();
        Assert.assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void test07_jsonPathParamExample() {
        mockBase.getWireMockServer().stubFor(get(urlPathEqualTo("/all/gurus"))
                .willReturn(aResponse()
                        .withHeader(ContentTypeHeader.KEY, "application/json")
                        .withBodyFile("test02.json")));

        List<Guru> gurus01 = given()
                .when().get("/all/gurus").then()
                .extract().jsonPath().getList("gurus", Guru.class);
        System.out.println("Size: " + gurus01.size() + "\n" + gurus01.get(0).toString());

        List<Guru> gurus02 = given()
                .when().get("/all/gurus").then()
                .extract().jsonPath().param("id", 2).getList("gurus.findAll {it.id == id}", Guru.class);
        System.out.println("Size: " + gurus02.size() + "\n" + gurus02.get(0).toString());

        List<Guru> gurus03 = given()
                .when().get("/all/gurus").then()
                .extract().jsonPath().param("id", 2).getList("gurus.findAll { it -> it.id == id }", Guru.class);
        System.out.println("Size: " + gurus03.size() + "\n" + gurus03.get(0).toString());

        /*Response response = given()
                .contentType(ContentType.JSON)
                .when()
                .body(file)
                .post("/form/params")
                .then().extract().response();*/
        //List<Guru> gurus = JsonPath.read(response.asString(), "$.gurus[?(@.tool =~ /^[r|R]est.*/)]");
        //Guru guru1 = response.as(Guru.class);
        //System.out.println(guru1);

        /*Guru guru;
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectReader objectReader = objectMapper.reader().forType(new TypeReference<List<MyClass>>(){});
        List<MyClass> result = objectReader.readValue(inputStream);
        ObjectMapper mapper = new ObjectMapper();

        mapper.readValue(gurus.get(0).toString(), new TypeReference<List<Guru>>(){});

        System.out.println("===============\n"+gurus1.get(0).toString()+"===============\n");*/
    }

    @Test
    public void test08_responseDefinitionBuilder() {
        ResponseDefinitionBuilder responseDefinitionBuilder = new ResponseDefinitionBuilder();
        responseDefinitionBuilder
                .withHeader(ContentTypeHeader.KEY, "application/json")
                .withStatus(200)
                .withStatusMessage("Status: OK")
                .withBody("This is a test");
        //WireMock.stubFor(WireMock.get("/tool/selenium")
        mockBase.getWireMockServer().stubFor(WireMock.get("/tool/selenium")
                .willReturn(responseDefinitionBuilder));
        Response response = given()
                .when()
                .get("/tool/selenium")
                .then().extract().response();
        System.out.println(response.getStatusLine());
        System.out.println(response.getHeaders());
        System.out.println(response.getBody().asString());
    }

    @Test
    public void test09_responseFile() {
        ResponseDefinitionBuilder responseDefinitionBuilder = new ResponseDefinitionBuilder();
        responseDefinitionBuilder
                .withHeader(ContentTypeHeader.KEY, "application/json")
                .withStatus(200)
                .withStatusMessage("Status: OK")
                .withBodyFile("test.json");
        mockBase.getWireMockServer().stubFor(WireMock.get(urlPathEqualTo("/getinfo/guru"))
                .withQueryParam("name", equalTo("johan-haleby"))
                .willReturn(responseDefinitionBuilder)
        );
        Response response = given()
                .when()
                .queryParam("name", "johan-haleby")
                .get("/getinfo/guru")
                .then().extract().response();
        System.out.println(response.asString());
    }

    @Test
    public void test10_fixedDelay() {
        // uncommenting this will make the test work
        // Thread.sleep(5000);

        mockBase.getWireMockServer().stubFor(put(urlEqualTo("/cars/1"))
                .withRequestBody(containing("Nissan"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(5000)
                        .withBody("{ \"name\": \"Nissan Maxima\" }")));
        Response response = given()
                .when()
                .body("{\"name\": \"Nissan\"}")
                .put("/cars/1")
                .then().extract().response();
        System.out.println(response.asString());
    }

    @Test
    public void test11_chunkedDribbleDelay() {
        mockBase.getWireMockServer().stubFor(get("/chunked/delayed")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("Hello world!")
                        .withChunkedDribbleDelay(5, 5000)));
        // With the above settings the Hello world! response body will be broken into five chunks and returned one at a time with a 1s gap between each
        Response response = given()
                .when()
                .get("/chunked/delayed")
                .then().extract().response();
        System.out.println(response.asString());
    }

    @Test
    public void test_TODO_stateFul() {
        // todo
        ResponseDefinitionBuilder responseDefinitionBuilder01 = new ResponseDefinitionBuilder();
        responseDefinitionBuilder01
                .withHeader(ContentTypeHeader.KEY, "application/json")
                .withStatus(200)
                .withStatusMessage("Status: OK")
                .withBody("KEY:01");
        ResponseDefinitionBuilder responseDefinitionBuilder02 = new ResponseDefinitionBuilder();
        responseDefinitionBuilder02
                .withHeader(ContentTypeHeader.KEY, "application/json")
                .withStatus(200)
                .withStatusMessage("Status: OK")
                .withBody("KEY:02");

        mockBase.getWireMockServer().stubFor(get(urlPathEqualTo("/todo/items"))//.inScenario("TestScenario")
                        .withQueryParam("num", equalTo("a"))
                        .withQueryParam("num", equalTo("b"))
                        //.whenScenarioStateIs(STARTED)
                        .willReturn(responseDefinitionBuilder01)
                //.willSetStateTo("2nd Value")
        );
        mockBase.getWireMockServer().stubFor(get(urlPathEqualTo("/todo/items"))//.inScenario("TestScenario")
                        .withQueryParam("num", equalTo("b"))
                        //.whenScenarioStateIs(STARTED)
                        .willReturn(responseDefinitionBuilder02)
                //.willSetStateTo("Cancel")
        );

        Response response = given()
                .when()
                .queryParam("num", "b")
                .get("/todo/items")
                .then().extract().response();
        System.out.println(response.asString());

    }

    @Test
    public void test_TODO_Priority() {
        //TODO
        //Catch-all case
        mockBase.getWireMockServer().stubFor(get(urlMatching("/api/.*")).atPriority(5)
                .willReturn(aResponse().withStatus(401)));

        //Specific case
        mockBase.getWireMockServer().stubFor(get(urlEqualTo("/api/specific-resource")).atPriority(1) //1 is highest
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("Resource state")));
    }

}
