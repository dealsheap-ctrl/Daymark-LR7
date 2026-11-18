package org.gradle.wrapper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.Comparator;
import java.util.stream.Stream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/** Minimal Gradle bootstrap launcher used when the project is opened on a clean workstation. */
public final class GradleWrapperMain {
    public static void main(String[] args) throws Exception {
        Path project = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
        Path propertiesPath = project.resolve("gradle/wrapper/gradle-wrapper.properties");
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(propertiesPath)) {
            properties.load(input);
        }
        String distributionUrl = properties.getProperty("distributionUrl");
        if (distributionUrl == null || distributionUrl.trim().isEmpty()) {
            throw new IllegalStateException("distributionUrl is missing in " + propertiesPath);
        }
        String fileName = distributionUrl.substring(distributionUrl.lastIndexOf('/') + 1);
        String version = fileName.replace("-bin.zip", "").replace("-all.zip", "");
        String expectedSha256 = properties.getProperty("distributionSha256Sum", "").trim();
        if (!expectedSha256.matches("(?i)[0-9a-f]{64}")) {
            throw new IllegalStateException("A valid distributionSha256Sum is required in " + propertiesPath);
        }
        String userHome = System.getenv("GRADLE_USER_HOME");
        Path gradleUserHome = userHome == null || userHome.isEmpty()
                ? Paths.get(System.getProperty("user.home"), ".gradle")
                : Paths.get(userHome);
        Path cache = gradleUserHome.resolve("wrapper/dists").resolve(version + "-bin/daymark-bootstrap");
        Path gradleHome = cache.resolve(version);
        Path executable = gradleHome.resolve(isWindows() ? "bin/gradle.bat" : "bin/gradle");
        Path verificationMarker = cache.resolve(".verified-sha256");

        boolean verifiedCache = Files.isRegularFile(executable)
                && Files.isRegularFile(verificationMarker)
                && Files.readString(verificationMarker).trim().equalsIgnoreCase(expectedSha256);
        if (!verifiedCache) {
            deleteRecursively(cache);
            Files.createDirectories(cache);
            Path zip = cache.resolve(fileName + ".part");
            download(distributionUrl, zip);
            String actualSha256 = sha256(zip);
            if (!actualSha256.equalsIgnoreCase(expectedSha256)) {
                Files.deleteIfExists(zip);
                throw new IOException("Gradle distribution checksum mismatch. Expected "
                        + expectedSha256 + " but received " + actualSha256);
            }
            unzip(zip, cache);
            Files.deleteIfExists(zip);
            Files.writeString(verificationMarker, expectedSha256);
        }
        if (!Files.isRegularFile(executable)) {
            throw new IOException("Gradle distribution was extracted, but launcher was not found: " + executable);
        }

        List<String> command = new ArrayList<>();
        if (isWindows()) {
            command.add("cmd.exe");
            command.add("/c");
        }
        command.add(executable.toString());
        for (String arg : args) command.add(arg);
        Process process = new ProcessBuilder(command).directory(project.toFile()).inheritIO().start();
        System.exit(process.waitFor());
    }


    private static String sha256(Path file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream input = new BufferedInputStream(Files.newInputStream(file))) {
            byte[] buffer = new byte[64 * 1024];
            int read;
            while ((read = input.read(buffer)) != -1) digest.update(buffer, 0, read);
        }
        return java.util.HexFormat.of().formatHex(digest.digest());
    }

    private static void deleteRecursively(Path directory) throws IOException {
        if (!Files.exists(directory)) return;
        try (Stream<Path> paths = Files.walk(directory)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }

    private static void download(String address, Path destination) throws IOException {
        System.out.println("Downloading Gradle distribution: " + address);
        HttpURLConnection connection = (HttpURLConnection) new URL(address).openConnection();
        connection.setConnectTimeout(30000);
        connection.setReadTimeout(60000);
        connection.setInstanceFollowRedirects(true);
        connection.setRequestProperty("User-Agent", "Daymark-Gradle-Bootstrap/1.0");
        int status = connection.getResponseCode();
        if (status < 200 || status >= 300) throw new IOException("Gradle download failed with HTTP " + status);
        try (InputStream in = new BufferedInputStream(connection.getInputStream());
             OutputStream out = new BufferedOutputStream(Files.newOutputStream(destination))) {
            byte[] buffer = new byte[64 * 1024];
            int read;
            while ((read = in.read(buffer)) != -1) out.write(buffer, 0, read);
        } finally {
            connection.disconnect();
        }
    }

    private static void unzip(Path archive, Path destination) throws IOException {
        try (ZipInputStream zip = new ZipInputStream(new BufferedInputStream(Files.newInputStream(archive)))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                Path target = destination.resolve(entry.getName()).normalize();
                if (!target.startsWith(destination)) throw new IOException("Invalid path inside Gradle archive");
                if (entry.isDirectory()) {
                    Files.createDirectories(target);
                } else {
                    Files.createDirectories(target.getParent());
                    Files.copy(zip, target, StandardCopyOption.REPLACE_EXISTING);
                }
                zip.closeEntry();
            }
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }
}
