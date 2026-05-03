package com.smartcampus.events.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.smartcampus.events.model.Event;
import com.smartcampus.events.model.ParticipationCertificate;
import com.smartcampus.events.model.Registration;
import com.smartcampus.events.repository.ParticipationCertificateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ParticipationCertificateService {

    private final ParticipationCertificateRepository certificateRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Transactional
    public ParticipationCertificate issueIfAbsent(Registration registration) {
        return certificateRepository.findByRegistration(registration)
                .orElseGet(() -> certificateRepository.save(
                        ParticipationCertificate.builder()
                                .registration(registration)
                                .certificateCode("CERT-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase())
                                .build()));
    }

    public byte[] generatePdf(ParticipationCertificate certificate) {
        Registration reg = certificate.getRegistration();
        Event event = reg.getEvent();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 48, 48, 48, 48);
        PdfWriter.getInstance(document, out);
        document.open();

        Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD);
        Font hFont = new Font(Font.HELVETICA, 12, Font.BOLD);
        Font pFont = new Font(Font.HELVETICA, 11, Font.NORMAL);

        Paragraph title = new Paragraph("Participation Certificate", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(18);
        document.add(title);

        Paragraph org = new Paragraph("Smart Campus Event Management System", hFont);
        org.setAlignment(Element.ALIGN_CENTER);
        org.setSpacingAfter(22);
        document.add(org);

        Paragraph body = new Paragraph(
                "This is to certify that " + reg.getStudent().getName()
                        + " has successfully participated in the event \"" + event.getTitle() + "\".",
                pFont);
        body.setAlignment(Element.ALIGN_CENTER);
        body.setSpacingAfter(18);
        document.add(body);

        String completionDate = event.getEndDate() != null ? event.getEndDate().format(DATE_FMT) : "N/A";
        Paragraph completion = new Paragraph("Completion Date: " + completionDate, pFont);
        completion.setAlignment(Element.ALIGN_CENTER);
        completion.setSpacingAfter(22);
        document.add(completion);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);

        addRow(table, "Student Name", reg.getStudent().getName());
        addRow(table, "Event Name", event.getTitle());
        addRow(table, "Unique ID", certificate.getCertificateCode());

        document.add(table);

        Paragraph footer = new Paragraph("Issued by Smart Campus", pFont);
        footer.setAlignment(Element.ALIGN_RIGHT);
        footer.setSpacingBefore(24);
        document.add(footer);

        document.close();
        return out.toByteArray();
    }

    private static void addRow(PdfPTable table, String k, String v) {
        PdfPCell c1 = new PdfPCell(new Phrase(k, new Font(Font.HELVETICA, 10, Font.BOLD)));
        c1.setPadding(8);
        c1.setBackgroundColor(new java.awt.Color(248, 250, 252));
        c1.setBorderColor(new java.awt.Color(226, 232, 240));
        PdfPCell c2 = new PdfPCell(new Phrase(v == null ? "" : v, new Font(Font.HELVETICA, 10, Font.NORMAL)));
        c2.setPadding(8);
        c2.setBorderColor(new java.awt.Color(226, 232, 240));
        table.addCell(c1);
        table.addCell(c2);
    }
}

