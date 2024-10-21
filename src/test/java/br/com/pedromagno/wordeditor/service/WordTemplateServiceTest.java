package br.com.pedromagno.wordeditor.service;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class WordTemplateServiceTest {

    @InjectMocks
    private WordTemplateService wordTemplateService;

    @Mock
    private MultipartFile file;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testExtrairPlaceholdersDoTemplate() throws IOException {
        XWPFDocument documento = new XWPFDocument();
        documento.createParagraph().createRun().setText("Este é um {placeholder} de teste presente no documento.");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        documento.write(outputStream);

        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        MultipartFile mockFile = Mockito.mock(MultipartFile.class);
        when(mockFile.getInputStream()).thenReturn(inputStream);

        Map<String, String> campos = wordTemplateService.extrairPlaceholdersDoTemplate(mockFile);

        assertEquals(1, campos.size());
        assertEquals(true, campos.containsKey("placeholder"));
    }

    @Test
    public void testSubistituirPlaceholdersDoTemplate() throws IOException {
        XWPFDocument documento = new XWPFDocument();
        XWPFParagraph paragraph = documento.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText("Nome: {nome}, Order: {orderId} is confirmed.\nItens: {item}");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        documento.write(outputStream);
        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        MultipartFile mockFile = Mockito.mock(MultipartFile.class);
        when(mockFile.getInputStream()).thenReturn(inputStream);

        Map<String, String> campos = new LinkedHashMap<>();
        campos.put("nome", "Pedro");
        campos.put("orderId", "12345");
        campos.put("item", "Hamburguer, Batata, Coca-Cola");

        byte[] resultado = wordTemplateService.substituirPlaceholders(mockFile, campos);
        XWPFDocument documentoalterado = new XWPFDocument(new ByteArrayInputStream(resultado));
        StringBuilder textoAlterado = new StringBuilder();

        documentoalterado.getParagraphs().forEach(paragrafo -> {
            paragrafo.getRuns().forEach(r -> {
                String texto = r.getText(0);
                if(texto != null) {
                    textoAlterado.append(texto);
                }
            });
        });
        String expectedText = "Nome: Pedro, Order: 12345 is confirmed.\nItens: Hamburguer, Batata, Coca-Cola";
        assertTrue(textoAlterado.toString().contains(expectedText));
    }
}
