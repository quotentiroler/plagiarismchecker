package com.example.plagcheck.storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.plagcheck.ProcessJSON.ExcludeFingerprints;
import com.example.plagcheck.ProcessJSON.JSONComparison;
import com.example.plagcheck.ProcessJSON.JSONMaker;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

@Service
public class FileSystemStorageService implements StorageService {

	private Path rootLocation;

	@Autowired
	public FileSystemStorageService(StorageProperties properties) {
		this.rootLocation = Paths.get(properties.getLocation());
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
					System.out.println(destinationFile.toString());
					ZipFile zip = new ZipFile(destinationFile.toString());
					zip.extractAll(destinationFile.getParent().toString());
					Files.deleteIfExists(destinationFile);
					System.out.println("Zip extracted, create fingerprints");
					JSONMaker.runThis(destinationFile.getParent());
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
			return Files.walk(this.rootLocation, 1)
					.filter(path -> !path.equals(this.rootLocation))
					.map(this.rootLocation::relativize);
		} catch (IOException e) {
			throw new StorageException("Failed to read stored files", e);
		}

	}

	@Override
	public Path load(String filename) {
		return rootLocation.resolve(filename);
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

	@Override
	public void deleteAll() {
		FileSystemUtils.deleteRecursively(rootLocation.toFile());
	}

	@Override
	public void init() {
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
