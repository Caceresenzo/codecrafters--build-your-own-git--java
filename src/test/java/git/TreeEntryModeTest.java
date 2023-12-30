package git;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TreeEntryModeTest {

	@Test
	void format() {
		assertEquals("040000", new TreeEntryMode(TreeEntryModeType.DIRECTORY, 0).format());
		assertEquals("100644", new TreeEntryMode(TreeEntryModeType.REGULAR_FILE, 0b110100100).format());
		assertEquals("100664", new TreeEntryMode(TreeEntryModeType.REGULAR_FILE, 0b110110100).format());
		assertEquals("100755", new TreeEntryMode(TreeEntryModeType.REGULAR_FILE, 0b111101101).format());
		assertEquals("120000", new TreeEntryMode(TreeEntryModeType.SYMBOLIC_LINK, 0).format());
		assertEquals("160000", new TreeEntryMode(TreeEntryModeType.GITLINK, 0).format());
	}

}