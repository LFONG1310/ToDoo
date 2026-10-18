package com.todoo.main;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import com.todoo.TodooApplication;

@SpringBootTest(classes = TodooApplication.class, properties = {
	"spring.datasource.url=jdbc:h2:mem:main-smoke;MODE=MySQL;DB_CLOSE_DELAY=-1",
	"spring.datasource.driver-class-name=org.h2.Driver",
	"spring.datasource.username=sa", "spring.datasource.password=",
	"spring.sql.init.mode=always", "spring.sql.init.schema-locations=file:database/001_notificaciones.sql"
})
class MainApplicationTests {

	@Test
	void contextLoads() {
	}

}
