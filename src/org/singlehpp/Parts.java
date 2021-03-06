package org.singlehpp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parts implements Comparable<Parts> {
	public ArrayList<String> includes = new ArrayList<>();
	public String code;
	public String fileName;

	@Override
	public int compareTo(Parts that) {
		if (this.dependsOn(that)) {
			return 1;
		}
		if (that.dependsOn(this)) {
			return -1;
		}
		return 0;
	}

	public boolean dependsOn(Parts that) {
		List<String> includesNames = getIncludedFilenames();
		return includesNames.contains(that.fileName);
	}

	public List<String> getIncludedFilenames() {
		List<String> includesNames = Arrays.asList(this.includes.stream().map(i -> getFileFromInclude(i))
				.filter(n -> n.endsWith(".h")).toArray(String[]::new));
		return includesNames;
	}

	@Override
	public String toString() {
		return "Parts [" + fileName + "] : " + getIncludedFilenames() + "\n";
	}

	public static String getFileFromInclude(String include) {
		StringBuffer b = new StringBuffer();
		SingleHpp.replaceAll(include, "(<[^>]+>)|(\".*?\")", m -> {
			b.append(SingleHpp.getAfterLast("/", SingleHpp.substring(m.group(), 1, -1)));
			return "";
		});
		return b.toString();
	}
}
