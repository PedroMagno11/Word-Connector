package br.com.pedromagno.wordeditor.service;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class WordTemplateService {

    public Map<String, String> extrairPlaceholdersDoTemplate(MultipartFile template) throws IOException {
        Map<String, String> camposJson = new LinkedHashMap<>();
        String regex = "\\{(.*?)\\}";
        Pattern pattern = Pattern.compile(regex);

        try (InputStream templateInputStream = template.getInputStream();
             XWPFDocument documento = new XWPFDocument(templateInputStream)) {

            for (XWPFParagraph paragraph : documento.getParagraphs()) {
                for (XWPFRun run : paragraph.getRuns()) {
                    String texto = run.getText(0);
                    if (texto != null) {
                        Matcher matcher = pattern.matcher(texto);
                        while (matcher.find()) {
                            String campo = matcher.group(1).strip();
                            String campoJson = formatarCampoEncontrado(campo);
                            if (!campoJson.isEmpty()) { // Valida se o campo não é vazio
                                camposJson.put(campoJson, ""); // Usa o campo como chave e define seu valor como vazio
                            }
                        }
                    }
                }
            }
        }
        return camposJson;
    }


    public byte[] substituirPlaceholders(MultipartFile template, Map<String, String> campos) throws IOException {

        try(InputStream templateInputStream = template.getInputStream();
        XWPFDocument documento = new XWPFDocument(templateInputStream);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()){

            for (XWPFParagraph paragraph : documento.getParagraphs()) {
                for(XWPFRun run : paragraph.getRuns()) {
                    String texto = run.getText(0);
                    if(texto != null) {
                        for(Map.Entry<String, String> campo: campos.entrySet()){
                            texto = texto.replace("{" + campo.getKey() + "}", campo.getValue());
                        }
                        run.setText(texto, 0);
                    }
                }
            }
            documento.write(outputStream);
            return outputStream.toByteArray();
        }
    }


    private String formatarCampoEncontrado(String campo) {
        // troca os espaços por underscore e remove caracteres inválidos
        String campoComUnderscores = campo.replaceAll("\\s+", "_");
        String campoValido = campoComUnderscores.replaceAll("[^a-zA-Z0-9_]", "");
        return campoValido;
    }
}
