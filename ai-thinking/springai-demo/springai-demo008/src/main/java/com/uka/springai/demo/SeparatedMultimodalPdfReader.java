package com.uka.springai.demo;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.content.Media;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.reader.pdf.layout.PDFLayoutTextStripperByArea;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 分离式多模态 PDF 读取器
 * 提取的文本和图片将分别存储为独立的 Document 对象。
 */
public class SeparatedMultimodalPdfReader implements DocumentReader {

    public static final String METADATA_START_PAGE_NUMBER = "page_number";
    public static final String METADATA_END_PAGE_NUMBER = "end_page_number";
    public static final String METADATA_FILE_NAME = "file_name";

    // 新增：用于区分 Document 类型的元数据 Key
    public static final String METADATA_CONTENT_TYPE = "content_type";
    public static final String CONTENT_TYPE_TEXT = "text";
    public static final String CONTENT_TYPE_IMAGE = "image";

    private static final String PDF_PAGE_REGION = "pdfPageRegion";

    protected final PDDocument document;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    protected String resourceFileName;
    private PdfDocumentReaderConfig config;

    public SeparatedMultimodalPdfReader(String resourceUrl) {
        this(new DefaultResourceLoader().getResource(resourceUrl));
    }

    public SeparatedMultimodalPdfReader(Resource pdfResource) {
        this(pdfResource, PdfDocumentReaderConfig.defaultConfig());
    }

    public SeparatedMultimodalPdfReader(String resourceUrl, PdfDocumentReaderConfig config) {
        this(new DefaultResourceLoader().getResource(resourceUrl), config);
    }

    public SeparatedMultimodalPdfReader(Resource pdfResource, PdfDocumentReaderConfig config) {
        try {
            PDFParser pdfParser = new PDFParser(
                    new org.apache.pdfbox.io.RandomAccessReadBuffer(pdfResource.getInputStream()));
            this.document = pdfParser.parse();
            this.resourceFileName = pdfResource.getFilename();
            this.config = config;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Document> get() {
        List<Document> readDocuments = new ArrayList<>();
        try {
            var pdfTextStripper = new PDFLayoutTextStripperByArea();

            int pageNumber = 1;
            int startPageNumber = 1;

            // 文本暂存区 (受 pagesPerDocument 影响合并)
            List<String> pageTextGroupList = new ArrayList<>();
            // 图片 Document 暂存区 (提取即生成 Document，但在阶段末尾统一加入结果集)
            List<Document> imageDocumentGroupList = new ArrayList<>();

            PDPageTree pages = this.document.getDocumentCatalog().getPages();
            int totalPages = pages.getCount();
            int logFrequency = totalPages > 10 ? totalPages / 10 : 1;
            int pagesPerDocument = getPagesPerDocument(totalPages);

            for (PDPage page : pages) {
                if ((pageNumber - 1) % logFrequency == 0) {
                    logger.info("Processing PDF page: {}", pageNumber);
                }

                // 1. 提取并处理文本
                handleSinglePageText(page, pageNumber, pdfTextStripper, pageTextGroupList);

                // 2. 提取并生成图片 Document (每张图片一个独立的 Document)
                imageDocumentGroupList.addAll(extractImageDocumentsFromPage(page, pageNumber));

                // 达到分组条件时，打包生成最终 Document
                if (pageNumber % pagesPerDocument == 0 || pageNumber == totalPages) {

                    // 生成文本 Document
                    if (!CollectionUtils.isEmpty(pageTextGroupList)) {
                        String combinedText = pageTextGroupList.stream().collect(Collectors.joining());
                        readDocuments.add(createTextDocument(combinedText, startPageNumber, pageNumber));
                        pageTextGroupList.clear();
                    }

                    // 将这几页的图片 Document 加入结果集
                    if (!CollectionUtils.isEmpty(imageDocumentGroupList)) {
                        readDocuments.addAll(imageDocumentGroupList);
                        imageDocumentGroupList.clear();
                    }

                    startPageNumber = pageNumber + 1;
                }
                pageNumber++;
            }
            logger.info("Processed total {} pages, Generated {} documents.", totalPages, readDocuments.size());
            return readDocuments;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleSinglePageText(PDPage page, int pageNumber, PDFLayoutTextStripperByArea pdfTextStripper,
                                      List<String> pageTextGroupList) throws IOException {
        int x0 = (int) page.getMediaBox().getLowerLeftX();
        int xW = (int) page.getMediaBox().getWidth();
        int y0 = (int) page.getMediaBox().getLowerLeftY() + this.config.pageTopMargin;
        int yW = (int) page.getMediaBox().getHeight() - (this.config.pageTopMargin + this.config.pageBottomMargin);

        pdfTextStripper.addRegion(PDF_PAGE_REGION, new Rectangle(x0, y0, xW, yW));
        pdfTextStripper.extractRegions(page);
        var pageText = pdfTextStripper.getTextForRegion(PDF_PAGE_REGION);

        if (StringUtils.hasText(pageText)) {
            pageText = this.config.pageExtractedTextFormatter.format(pageText, pageNumber);
            pageTextGroupList.add(pageText);
        }
        pdfTextStripper.removeRegion(PDF_PAGE_REGION);
    }

    /**
     * 将页面中的每张图片提取为独立的 Document
     */
    private List<Document> extractImageDocumentsFromPage(PDPage page, int pageNumber) throws IOException {
        List<Document> imageDocs = new ArrayList<>();
        PDResources resources = page.getResources();

        if (resources == null) return imageDocs;

        int imageIndex = 1;
        for (COSName xObjectName : resources.getXObjectNames()) {
            PDXObject xObject = resources.getXObject(xObjectName);

            if (xObject instanceof PDImageXObject pdImage) {
                BufferedImage image = pdImage.getImage();
                if (image != null) {
                    String suffix = pdImage.getSuffix();
                    if (suffix == null || suffix.isEmpty()) suffix = "png";

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(image, suffix, baos);
                    byte[] imageBytes = baos.toByteArray();

                    String mimeType = "image/" + suffix;
                    Media media = new Media(MimeTypeUtils.parseMimeType(mimeType), new ByteArrayResource(imageBytes));

                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put(METADATA_START_PAGE_NUMBER, pageNumber);
                    metadata.put(METADATA_END_PAGE_NUMBER, pageNumber);
                    metadata.put(METADATA_FILE_NAME, this.resourceFileName);
                    metadata.put(METADATA_CONTENT_TYPE, CONTENT_TYPE_IMAGE); // 标记为图片
                    metadata.put("image_index", imageIndex);
                    // 构造纯图片的 Document
                    Document imageDoc = new Document(media, metadata);
                    imageDocs.add(imageDoc);

                    imageIndex++;
                }
            }
        }
        return imageDocs;
    }

    private int getPagesPerDocument(int totalPages) {
        if (this.config.pagesPerDocument == PdfDocumentReaderConfig.ALL_PAGES) {
            return totalPages;
        }
        return this.config.pagesPerDocument;
    }

    /**
     * 构造纯文本的 Document
     */
    protected Document createTextDocument(String docText, int startPageNumber, int endPageNumber) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(METADATA_START_PAGE_NUMBER, startPageNumber);
        if (startPageNumber != endPageNumber) {
            metadata.put(METADATA_END_PAGE_NUMBER, endPageNumber);
        }
        metadata.put(METADATA_FILE_NAME, this.resourceFileName);
        metadata.put(METADATA_CONTENT_TYPE, CONTENT_TYPE_TEXT); // 标记为文本

        // 构造不带 Media 的文本 Document
        return new Document(docText, metadata);
    }
}