package git;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.jupiter.api.Test;

class AuthorSignatureTest {

	@Test
	void format() {
		final var when = ZonedDateTime.of(LocalDateTime.of(2023, 12, 25, 8, 42), ZoneId.of("Europe/Paris"));
		final var author = new AuthorSignature("hello", "world@java.lang", when);

		assertEquals("hello <world@java.lang> 1703490120 +0100", author.format());
	}

}