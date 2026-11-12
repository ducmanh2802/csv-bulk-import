package dev.manhd.csvimport.web;

import dev.manhd.csvimport.importer.CsvImporter;
import dev.manhd.csvimport.importer.ImportResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/imports")
class ImportController {

    private final Map<String, CsvImporter> importers;

    ImportController(List<CsvImporter> importers) {
        this.importers = importers.stream().collect(Collectors.toMap(CsvImporter::name, Function.identity()));
    }

    /** {@code curl -F file=@customers.csv "localhost:8080/api/imports?strategy=streaming"} */
    @PostMapping
    ImportResult importCustomers(@RequestParam("file") MultipartFile file,
                                 @RequestParam(defaultValue = "streaming") String strategy) throws IOException {
        CsvImporter importer = importers.get(strategy);
        if (importer == null) {
            throw new IllegalArgumentException("unknown strategy '" + strategy + "', use one of " + importers.keySet());
        }
        try (InputStream in = file.getInputStream()) {
            return importer.importCsv(in);
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail badRequest(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}
