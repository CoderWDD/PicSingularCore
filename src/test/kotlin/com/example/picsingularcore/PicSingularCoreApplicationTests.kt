package com.example.picsingularcore

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatterBuilder

@SpringBootTest
class PicSingularCoreApplicationTests {

	@Test
	fun contextLoads() {
		println(LocalDateTime.now().format(DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd HH:mm").toFormatter()))
	}

}
