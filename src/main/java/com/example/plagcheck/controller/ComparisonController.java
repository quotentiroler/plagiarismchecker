package com.example.plagcheck.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.lingala.zip4j.exception.ZipException;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.plagcheck.storage.StorageFileNotFoundException;
import com.example.plagcheck.storage.StorageService;

@Controller
@RequestMapping(path = "/")
public class ComparisonController {

	private final StorageService storageService;

	@Autowired
	public ComparisonController(StorageService storageService) {
		this.storageService = storageService;
	}

	@GetMapping("/")
	public String listUploadedFiles(Model model) throws IOException {

		model.addAttribute("files", storageService.loadAll().map(
				path -> {
					if (path.getFileName().toString().endsWith(".txt")
							|| path.getFileName().toString().endsWith(".json"))
						return MvcUriComponentsBuilder.fromMethodName(ComparisonController.class,
								"serveFile", path.getFileName().toString()).build().toUri().toString();
					else
						return null;
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toList()));

		return "uploadForm";
	}

	@GetMapping("/files/{filename:.+}")
	@ResponseBody
	public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

		Resource file = storageService.loadAsResource(filename);
		String contentDisposition = file.getFilename().endsWith(".txt") || file.getFilename().endsWith(".json")
				? "inline"
				: "attachment";

		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
				contentDisposition + "; filename=\"" + file.getFilename() + "\"").body(file);
	}

	@PostMapping("/")
	public String handleFileUpload(@RequestParam("file") MultipartFile file,
			RedirectAttributes redirectAttributes) throws ZipException, JSONException, InterruptedException {

		storageService.store(file);
		redirectAttributes.addFlashAttribute("message",
				"You successfully uploaded " + file.getOriginalFilename() + "!");

		return "redirect:/";
	}

	@PostMapping("/github")
	public String handleGitHubRepos(@RequestParam("repoUrls") String repoUrls,
			RedirectAttributes redirectAttributes) throws JSONException, InterruptedException {
		try {
			List<String> urls = Arrays.asList(repoUrls.split("\\s+"));
			storageService.storeFromGitHub(urls);
			redirectAttributes.addFlashAttribute("message",
					"You successfully processed " + urls.size() + " GitHub repositories!");
		} catch (Exception e) {
			e.printStackTrace();
			redirectAttributes.addFlashAttribute("message",
					"Failed to process GitHub repositories: " + e.getMessage());
		}
		return "redirect:/";
	}

	@PostMapping("/deleteAll")
	public String deleteAllFiles(RedirectAttributes redirectAttributes) {
		try {
			storageService.deleteAll();
			redirectAttributes.addFlashAttribute("message", "You successfully deleted all files!");
		} catch (Exception e) {
			e.printStackTrace();
			redirectAttributes.addFlashAttribute("message", "Failed to delete all files: " + e.getMessage());
		}
		return "redirect:/";
	}

	@ExceptionHandler(StorageFileNotFoundException.class)
	public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
		return ResponseEntity.notFound().build();
	}

}
