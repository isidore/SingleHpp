package org.singlehpp.tests;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import org.approvaltests.Approvals;
import org.junit.Test;
import org.singlehpp.Parts;
import org.singlehpp.SingleHpp;

public class FileProcessingTest {

	@Test
	public void testRemove() {

		assertEquals("  int i; \n  int j;", SingleHpp.removeComments("  int i; // this is the first value\n  int j;"));
		assertEquals("  int i;int j;",
				SingleHpp.removeComments("  int i;/* // \nthis is the first value\n   ** \n */int j;"));
	}

	@Test
	public void testGetFiles() throws Exception {
		Approvals.verifyAll("files", SingleHpp.getAllFiles("/Users/llewellyn/Github/ApprovalTests.cpp/ApprovalTests"));
	}

	@Test
	public void testGetSortedFiles() throws Exception {
		String directory = "/Users/llewellyn/Github/ApprovalTests.cpp/ApprovalTests";
		List<File> files = SingleHpp.getAllFiles(directory);
		List<Parts> parts = SingleHpp.getParts(files);
		Approvals.verifyAll("Sorted Files", parts, p -> p.fileName);
	}

	@Test
	public void testGetFileFromInclude() throws Exception {
		String[] includes = { "#include   \"..\\myfile.h\"", "#include   <string>" };
		Approvals.verifyAll("", includes, h -> h + " : " + Parts.getFileFromInclude(h));

	}

	@Test
	public void testUnalterable() throws Exception {
		String text = "// <SingleHpp unalterable>\n" + "#ifdef APPROVALS_CATCH\n" + "#define CATCH_CONFIG_MAIN\n"
				+ "#endif\n" + "\n" + "#include <Catch.hpp>\n" + "// </SingleHpp>";
		HashMap<String, String> unalterable = new HashMap<>();
		String result = SingleHpp.handledUnalterable(text, unalterable);
		Approvals.verifyAll("Unalterable", unalterable.values());

	}

	@Test
	public void testCreate() throws Exception {
		Approvals.verify(SingleHpp.createTextFor("/Users/llewellyn/Github/ApprovalTests.cpp/ApprovalTests"));
	}

	@Test
	public void demo() throws Exception {
		String outputFile = "/Users/llewellyn/Github/ApprovalTests.Cpp.StarterProject/lib/ApprovalTests.hpp";
		SingleHpp.create("/Users/llewellyn/Github/ApprovalTests.cpp/ApprovalTests", outputFile);
	}
}
