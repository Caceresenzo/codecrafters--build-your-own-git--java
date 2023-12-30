package git.tree;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TreeEntryModeTypeTest {

	@Test
	void match() {
		assertEquals(TreeEntryModeType.DIRECTORY, TreeEntryModeType.match(0b0100000000000000));

		assertEquals(TreeEntryModeType.REGULAR_FILE, TreeEntryModeType.match(0b1000000110100100));
		assertEquals(TreeEntryModeType.REGULAR_FILE, TreeEntryModeType.match(0b1000000110110100));
		assertEquals(TreeEntryModeType.REGULAR_FILE, TreeEntryModeType.match(0b1000000111101101));

		assertEquals(TreeEntryModeType.SYMBOLIC_LINK, TreeEntryModeType.match(0b1010000000000000));

		assertEquals(TreeEntryModeType.GITLINK, TreeEntryModeType.match(0b1110000000000000));
	}

}