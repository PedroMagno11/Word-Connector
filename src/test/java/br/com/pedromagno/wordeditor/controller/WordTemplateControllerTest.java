package br.com.pedromagno.wordeditor.controller;

import br.com.pedromagno.wordeditor.service.WordTemplateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class WordTemplateControllerTest {

    @InjectMocks
    private WordTemplateController wordTemplateController;

    @Mock
    private WordTemplateService wordTemplateService;

    @Mock
    private MultipartFile file;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void testExtrairTemplateWord() throws IOException {
        // Arrange
        String docContent = "This is a template with {placeholder1} and {placeholder2}.";
        MockMultipartFile file = new MockMultipartFile("file", "template.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", docContent.getBytes());

        Map<String, String> expected = new LinkedHashMap<>();
        expected.put("placeholder1", "");
        expected.put("placeholder2", "");

        when(wordTemplateService.extrairPlaceholdersDoTemplate(any(MultipartFile.class))).thenReturn(expected);

        // Act
        ResponseEntity<Map<String, String>> response = wordTemplateController.extrairTemplateWord(file);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expected, response.getBody());
        verify(wordTemplateService, times(1)).extrairPlaceholdersDoTemplate(any(MultipartFile.class));
    }

    @Test
    void testExtrairTemplateWordEmptyFile() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "template.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", new byte[0]);

        // Act
        ResponseEntity<Map<String, String>> response = wordTemplateController.extrairTemplateWord(file);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(Map.of("error", "Arquivo inválido. Por favor, envie um arquivo .docx não vazio."), response.getBody());
        verify(wordTemplateService, never()).extrairPlaceholdersDoTemplate(any(MultipartFile.class));
    }

    @Test
    void testExtrairTemplateWordInvalidExtension() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "template.txt", "text/plain", "dummy content".getBytes());

        // Act
        ResponseEntity<Map<String, String>> response = wordTemplateController.extrairTemplateWord(file);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(Map.of("error", "Arquivo inválido. Por favor, envie um arquivo .docx não vazio."), response.getBody());
        verify(wordTemplateService, never()).extrairPlaceholdersDoTemplate(any(MultipartFile.class));
    }

    @Test
    void testExtrairTemplateWordIOException() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "template.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "dummy content".getBytes());

        when(wordTemplateService.extrairPlaceholdersDoTemplate(any(MultipartFile.class))).thenThrow(new IOException("Erro ao extrair placeholders"));

        // Act
        ResponseEntity<Map<String, String>> response = wordTemplateController.extrairTemplateWord(file);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(Map.of("error", "Erro ao extrair placeholders: Erro ao extrair placeholders"), response.getBody());
        verify(wordTemplateService, times(1)).extrairPlaceholdersDoTemplate(file);
    }
}
