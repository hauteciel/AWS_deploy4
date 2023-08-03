package com.lec.spring.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.lec.spring.domain.Book;
import com.lec.spring.domain.BookRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("dev")    // dev profile 로 테스트가 진행됨.
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

class BookApiControllerTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BookRepository bookRepository;

    // @Autowired
    // private EntityManager em;

    @AfterEach
    public void tearDown() throws Exception {
        bookRepository.deleteAll();

        // 이거 붙이고 테스트 하려면 Repository나 Service 레이어에서 단위테스트할 때 사용하자!!
        // em.createNativeQuery("ALTER TABLE book COLUMN id RESTART WITH
        // 1").executeUpdate();
    }

    @Order(1)
    @Test
    public void 책목록보기_테스트() {

        List<Book> books = Arrays.asList(
                new Book("제목1", "내용1", "메타코딩"),
                new Book("제목2", "내용2", "메타코딩"));
        bookRepository.saveAll(books);

        // 테스트 시작
        ResponseEntity<String> response = restTemplate.exchange("/api/book",
                HttpMethod.GET, null, String.class);
        System.out.println("================================================================================");
        System.out.println(response.getBody());
        System.out.println("================================================================================");
        // 테스트 검증
        DocumentContext dc = JsonPath.parse(response.getBody());
        String title1 = dc.read("$.[0].title");
        String title2 = dc.read("$.[1].title");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("제목1", title1);
        assertEquals("제목2", title2);
    }

    @Order(2)
    @Test
    public void 책등록_테스트() throws Exception {

        // 데이터 준비
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = new ObjectMapper().writeValueAsString(new Book("제목3", "내용3", "메타코딩"));
        HttpEntity<String> request = new HttpEntity<String>(body, headers);

        // 테스트 시작
        ResponseEntity<String> response = restTemplate.exchange("/api/book", HttpMethod.POST, request, String.class);
        System.out.println("================================================================================");
        System.out.println(response.getBody());
        System.out.println("================================================================================");

        // 테스트 검증
        DocumentContext dc = JsonPath.parse(response.getBody());
        Long id = dc.read("$.id", Long.class); // read할때 꼭 캐스팅해줘야 한다. 무조건 Integer로 변환해서 받음.
        String title = dc.read("$.title");
        String content = dc.read("$.content");
        String author = dc.read("$.author");

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(3L, id);
        assertEquals("제목3", title);
        assertEquals("내용3", content);
        assertEquals("메타코딩", author);
    }

}