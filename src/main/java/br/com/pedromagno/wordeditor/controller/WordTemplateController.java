package br.com.pedromagno.wordeditor.controller;

import br.com.pedromagno.wordeditor.service.WordTemplateService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/docx")
public class WordTemplateController {

    private final WordTemplateService wordTemplateService;

    public WordTemplateController(WordTemplateService wordTemplateService) {
        this.wordTemplateService = wordTemplateService;
    }

    @PostMapping("/extract")
    public ResponseEntity<Map<String, String>> extrairTemplateWord(@RequestParam("file") MultipartFile file) throws IOException {
        try{
            if(file.isEmpty() || !file.getOriginalFilename().endsWith(".docx")){
                return ResponseEntity.badRequest().body(Map.of("error", "Arquivo inválido. Por favor, envie um arquivo .docx não vazio."));
            }
            Map<String, String> campos = wordTemplateService.extrairPlaceholdersDoTemplate(file);
            return ResponseEntity.ok(campos);

        } catch (IOException e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Erro ao extrair placeholders: " + e.getMessage()));
        }
    }

    @PostMapping("/replace")
    public ResponseEntity<byte[]> substituirPlaceholders(@RequestParam("file") MultipartFile file, @RequestBody Map<String, String> valorDosCampos) throws IOException {
        try{
            if(file.isEmpty() || !file.getOriginalFilename().endsWith(".docx")){
                return ResponseEntity.badRequest().body(null);
            }
            byte[] arquivoModificado = wordTemplateService.substituirPlaceholders(file, valorDosCampos);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=" + file.getOriginalFilename());

            return ResponseEntity.ok().headers(headers).body(arquivoModificado);
        }catch (IOException e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


}
