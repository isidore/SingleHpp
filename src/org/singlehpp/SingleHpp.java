package org.singlehpp;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.lambda.functions.Function1;

import com.spun.util.io.FileUtils;
import com.spun.util.logger.SimpleLogger;

public class SingleHpp {

	public static List<File> getAllFiles(String directory) {
		FileFilter filter = p -> p.getName().endsWith(".h") || p.getName().endsWith(".cpp");
		File[] list = FileUtils.getRecursiveFileList(new File(directory), filter);
		Arrays.sort(list);
		return Arrays.asList(list);
	}

	public static String createTextFor(String directory) throws IOException {

		List<File> files = getAllFiles(directory);
		List<Parts> parts = getParts(files);

		return combineParts(parts, files);
	}

	public static List<Parts> getParts(List<File> files) throws IOException {
		List<Parts> parts = new ArrayList<>();
		for (File file : files) {
			parts.add(processFile(file));
		}
		return sortPartsByDependencies(parts);
	}

	private static List<Parts> sortPartsByDependencies(List<Parts> parts) {
		for (int i = 0; i < parts.size(); i++) {
			Parts lowest = parts.get(i);
			for (int j = i + 1; j < parts.size(); j++) {
				Parts next = parts.get(j);
				if (lowest.dependsOn(next)) {
					parts.remove(j);
					parts.add(0, next);
					i = -1;
					break;
				}
			}
		}
		return parts;
	}

	private static String combineParts(List<Parts> parts, List<File> files) {
		StringBuffer text = new StringBuffer();
		List<String> headers = reduceHeaders(parts, files);
		for (String header : headers) {
			text.append(header);
		}
		for (Parts p : parts) {
			text.append(" // ******************** From: " + p.fileName + "\n");
			text.append(p.code + "\n");
		}
		return text.toString();
	}

	private static List<String> reduceHeaders(List<Parts> parts, List<File> files) {
		ArrayList<String> headers = new ArrayList<>();
		String[] flatMap = parts.stream().flatMap(p -> p.includes.stream()).toArray(String[]::new);
		String[] paths = files.stream().map(f -> f.getAbsolutePath()).toArray(String[]::new);
		for (String header : flatMap) {
			if (!headers.contains(header)
					&& !Stream.of(paths).anyMatch(p -> p.endsWith(Parts.getFileFromInclude(header)))) {
				headers.add(header);
			}
		}
		return headers;
	}

	public static String getAfterLast(String match, String fromString) {
		int lastIndexOf = fromString.lastIndexOf(match);
		if (0 < lastIndexOf) {
			return fromString.substring(lastIndexOf + 1);
		} else {
			return fromString;
		}

	}

	public static String substring(String string, int start, int end) {
		end = end < 0 ? string.length() + end : end;
		if (0 <= start && 0 <= end && end <= string.length()) {
			return string.substring(start, end);
		} else {
			return "";
		}
	}

	public static String removeComments(String readFile) {
		return readFile.replaceAll("(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)", "");
	}

	public static String replaceAll(String text, String regex, Function1<Matcher, String> replacer) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(text);
		StringBuffer result = new StringBuffer();
		while (matcher.find()) {
			matcher.appendReplacement(result, replacer.call(matcher));
		}
		matcher.appendTail(result);
		return result.toString();
	}

	public static Parts processFile(File file) throws IOException {
		String fullText = FileUtils.readFile(file);
		HashMap<String, String> unalterable = new HashMap<>();
		String masked = handledUnalterable(fullText, unalterable);
		String code = removeComments(masked);
		final Parts p = new Parts();

		String maskedMinusIncludes = replaceAll(code, "#include.*\n", m -> {
			p.includes.add(m.group());
			return "";
		});
		p.fileName = file.getName();
		p.code = maskedMinusIncludes;
		for (String key : unalterable.keySet()) {
			p.code = p.code.replaceAll(key, unalterable.get(key));
		}
		return p;
	}

	public static String handledUnalterable(String fullText, HashMap<String, String> unalterable) {
		String masked = replaceAll(fullText, "\\/\\/ <SingleHpp unalterable>(?s).*<\\/SingleHpp>", m -> {
			String id = UUID.randomUUID().toString();
			unalterable.put(id, m.group());
			return id;
		});
		return masked;
	}

	public static void create(String directory, String outputFile) throws IOException {
		FileUtils.writeFile(new File(outputFile), createTextFor(directory));
		SimpleLogger.event("Writing " + outputFile);
	}

	public static void main(String[] args) throws IOException {
		SimpleLogger.event("Starting...");
		create(".", args[0]);
	}
}
