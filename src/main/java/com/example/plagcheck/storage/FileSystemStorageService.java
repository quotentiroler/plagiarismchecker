package com.example.plagcheck.storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.plagcheck.util.DirPreparation;
import com.example.plagcheck.util.ExcludeFingerprints;
import com.example.plagcheck.util.JSONComparison;
import com.example.plagcheck.util.JSONMaker;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FileSystemStorageService implements StorageService {

	private Path rootLocation;

	@Autowired
	public FileSystemStorageService(StorageProperties properties) {
		this.rootLocation = Paths.get(properties.getLocation());
	}

	public void storeFromGitHub(List<String> repoUrls) throws JSONException, InterruptedException {
		try {
			for (String url : repoUrls) {
				// Extract author and repo names from the URL
				String[] urlParts = url.split("/");
				String authorName = urlParts[urlParts.length - 2];
				String repoName = urlParts[urlParts.length - 1];

				// Create a directory for the repository
				String dirName = "task-" + authorName + "-" + repoName;
				Path destinationFile = this.rootLocation.resolve(
						Paths.get(dirName))
						.normalize().toAbsolutePath();
				if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
					// This is a security check
					throw new StorageException(
							"Cannot store file outside current directory.");
				}

				// Clone the repository to the created directory
				Git git = Git.cloneRepository()
						.setURI(url)
						.setDirectory(destinationFile.toFile())
						.call();

				git.gc().setExpire(new Date()).call();
				// Prepare the root directory for further processing
				DirPreparation.processFiles(destinationFile);

			}

			// Create JSON Files
			JSONMaker.runThis(this.rootLocation);

			// Delete all directories starting with "task"
			File rootDir = this.rootLocation.toFile();
			for (File file : rootDir.listFiles()) {
				if (file.getName().startsWith("task")) {
					FileUtils.deleteDirectory(file);
				}
			}

			// Compare JSON files
			JSONComparison jc = new JSONComparison(Paths.get("upload-dir/results"));
			this.setLocation("upload-dir/results/out");

		} catch (IOException | GitAPIException e) {
			throw new StorageException("Failed to store file from GitHub.", e);
		}
	}

	@Override
	public void store(MultipartFile file) throws JSONException, InterruptedException {
		try {
			if (file.isEmpty()) {
				throw new StorageException("Failed to store empty file.");
			}
			Path destinationFile = this.rootLocation.resolve(
					Paths.get(file.getOriginalFilename()))
					.normalize().toAbsolutePath();
			if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
				// This is a security check
				throw new StorageException(
						"Cannot store file outside current directory.");
			}
			try (InputStream inputStream = file.getInputStream()) {
				Files.copy(inputStream, destinationFile,
						StandardCopyOption.REPLACE_EXISTING);

				if (destinationFile.toString().contains(".zip")) {
					ZipFile zip = new ZipFile(destinationFile.toString());
					// Extracts the zip file
					zip.extractAll(destinationFile.getParent().toString());
					Files.deleteIfExists(destinationFile);

					// Create JSON files
					JSONMaker.runThis(destinationFile.getParent());

					// Delete all directories starting with "task"
					File rootDir = this.rootLocation.toFile();
					for (File x : rootDir.listFiles()) {
						if (x.getName().startsWith("task")) {
							FileUtils.deleteDirectory(x);
						}
					}

					JSONComparison jc = new JSONComparison(Paths.get("upload-dir/results"));
					this.setLocation("upload-dir/results/out");
				}

				if (destinationFile.toString().endsWith(".json")) {
					File results = new File("upload-dir/results");

					ExcludeFingerprints.letsGo(destinationFile, results.toPath());

					JSONComparison jc = new JSONComparison(
							Paths.get("upload-dir/results/fpExcluded"));
					this.setLocation("upload-dir/results/fpExcluded/out");
				}
			}
		} catch (IOException e) {
			throw new StorageException("Failed to store file.", e);
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public Stream<Path> loadAll() {
		try {
			init();
			return Files.walk(this.rootLocation)
					.filter(path -> !path.equals(this.rootLocation))
					.map(this.rootLocation::relativize);
		} catch (NoSuchFileException e) {
			// Log the error and continue with the next file
			log.error("Failed to read file: " + e.getFile(), e);
			return Stream.empty();
		} catch (IOException e) {
			throw new StorageException("Failed to read stored files", e);
		}
	}

	//search through the rootLocation recursively for the file with the given filename
	@Override
	public Path load(String filename) {
		try {
			Optional<Path> file = Files.walk(this.rootLocation)
				.filter(p -> p.getFileName().toString().equals(filename))
				.findFirst();
	
			if (file.isPresent()) {
				return file.get();
			} else {
				throw new StorageFileNotFoundException("Could not read file: " + filename);
			}
		} catch (IOException e) {
			throw new StorageException("Failed to read stored files", e);
		}
	}

	@Override
	public Resource loadAsResource(String filename) {
		try {
			Path file = load(filename);
			Resource resource = new UrlResource(file.toUri());
			if (resource.exists() || resource.isReadable()) {
				return resource;
			} else {
				throw new StorageFileNotFoundException(
						"Could not read file: " + filename);

			}
		} catch (MalformedURLException e) {
			throw new StorageFileNotFoundException("Could not read file: " + filename, e);
		}
	}

	// delete all files in rootLocation
	@Override
	public void deleteAll() {
		FileSystemUtils.deleteRecursively(rootLocation.toFile());
		init();
	}

	@Override
	public void init() {
		this.setLocation("upload-dir");
		try {
			Files.createDirectories(rootLocation);
		} catch (IOException e) {
			throw new StorageException("Could not initialize storage", e);
		}
	}

	@Override
	public void setLocation(String location) {
		rootLocation = Paths.get(location);

	}

	public Path getRootLocation() {
		return rootLocation;
	}

	@Override
	public JSONObject makeJSON(Path srcPath) throws IOException {
		JSONMaker m = new JSONMaker(srcPath);
		return m.getResult();
	}
}
