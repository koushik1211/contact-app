package com.monetize360.contact_application.serivce;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.monetize360.contact_application.dao.ContactDao;
import com.monetize360.contact_application.dao.UserDao;
import com.monetize360.contact_application.domain.Contact;
import com.monetize360.contact_application.domain.User;
import com.monetize360.contact_application.dto.ContactDto;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;

@Service //indicates business logic
@Slf4j
public class ContactServiceImpl implements ContactService {


    private final ContactDao contactDao;

    public ContactServiceImpl(ContactDao contactDao) {
        this.contactDao = contactDao;
    }

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private UserDao userDao;

    @Override
    public ContactDto insertContact(Integer id, ContactDto contactDto) {
        Optional<User> user=userDao.findById(id);
        Contact contact = objectMapper.convertValue(contactDto, Contact.class);
        contact.setUser(user.get());
        Contact createdContact = contactDao.save(contact);
        return objectMapper.convertValue(createdContact, ContactDto.class);
    }

    @Override
    public Contact updateContact(Integer userId, Integer id, ContactDto updatedContact) {
        Contact existingContact = contactDao.findByIdAndUserId(id,userId);

        existingContact.setFirstname(updatedContact.getFirstname());
        existingContact.setLastname(updatedContact.getLastname());
        existingContact.setEmail(updatedContact.getEmail());
        existingContact.setMobile(updatedContact.getMobile());

        return contactDao.save(existingContact);
    }

    @Override
    public ContactDto getContactById(Integer id,Integer userId) {

            Contact contact = contactDao.findByIdAndUserId(id,userId);
                return objectMapper.convertValue(contact, ContactDto.class);

    }

    @Override
    public void deleteContact(Integer id) {

            contactDao.deleteById(id);

    }

    @Override
    public List<ContactDto> getAllContacts(Integer userid) {

            List<Contact> contacts = contactDao.findByUserIdAndDeletedFalse(userid);
            log.info("contact count size is:{}", contacts.size());

            return objectMapper.convertValue(contacts, new TypeReference<List<ContactDto>>() {
            });


    }

    @Override
    public List<ContactDto> searchContact(Integer userId, String search) {

            List<Contact> contacts = contactDao.searchContact(userId,search);
            return objectMapper.convertValue(contacts, new TypeReference<List<ContactDto>>() {
            });


    }

    @Override
    public List<ContactDto> getContactBySort(Integer userId,String field, String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), field);
        List<Contact> contacts = contactDao.findByUserId(userId,sort);
        return objectMapper.convertValue(contacts, new TypeReference<List<ContactDto>>() {
        });

    }


    @Override
    public List<ContactDto> getContactsWithPagination(Integer userId,int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Contact> pagedResult = contactDao.findByUserId(userId,pageable);

        List<Contact> contacts = pagedResult.getContent();

        return objectMapper.convertValue(contacts, new TypeReference<List<ContactDto>>() {
        });
    }

    @Override
    public BufferedImage generateQRCodeForContact(String phoneNumber) {
        Contact contact = contactDao.findByMobile(phoneNumber);
        if (contact == null) {
            throw new RuntimeException("Contact not found for phone number: " + phoneNumber);
        }

        String vCard = "BEGIN:VCARD\n" +
                "VERSION:3.0\n" +
                "FN:" + contact.getFirstname() + " " + contact.getLastname() + "\n" +
                "EMAIL:" + contact.getEmail() + "\n" +
                "TEL:" + contact.getMobile() + "\n" +
                "END:VCARD";

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix;

        try {
            bitMatrix = qrCodeWriter.encode(vCard, BarcodeFormat.QR_CODE, 200, 200);
        } catch (WriterException e) {
            throw new RuntimeException("Error generating QR code", e);
        }

        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }

        return image;
    }

    @Override
    public void sendEmailWithQRCode(String mobile,String to, byte[] qrCodeImage) throws MessagingException {

        Contact contact = contactDao.findByMobile(mobile);
        MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setTo(to);
            helper.setSubject(" Contact QR Code");
            helper.setText("Please find the attached QR code for the contact information of "+contact.getFirstname());
            helper.addAttachment("contact-qrcode.png", new ByteArrayResource(qrCodeImage));
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(to);
//        message.setSubject("helloi");
//        message.setText("random");
//        emailSender.send(message);


        emailSender.send(mimeMessage);


    }

    @Override
    public String generateContactsCsv(Integer userId) {
        List<Contact> contacts=contactDao.findByUserIdAndDeletedFalse(userId);
        return convertContactsToCsv(contacts);
    }

    @Override
    public void saveContactsFromCsv(Integer userId,MultipartFile file) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",");

                Contact contact = new Contact();
                contact.setFirstname(fields[0]);
                contact.setLastname(fields[1]);
                contact.setEmail(fields[2]);
                contact.setMobile(fields[3]);
                Optional<User> user= userDao.findById(userId);
                contact.setUser(user.get());
                contactDao.save(contact);
            }

    }

    public String convertContactsToCsv(List<Contact> contacts)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("ID,First Name,Last Name,Email,Mobile\n");
        for (Contact contact : contacts) {
            sb.append(contact.getId()).append(",")
                    .append(contact.getFirstname()).append(",")
                    .append(contact.getLastname()).append(",")
                    .append(contact.getEmail()).append(",")
                    .append(contact.getMobile()).append("\n");
        }

        return sb.toString();
    }


}

    






