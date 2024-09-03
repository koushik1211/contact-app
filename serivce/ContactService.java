package com.monetize360.contact_application.serivce;

import com.monetize360.contact_application.domain.Contact;
import com.monetize360.contact_application.dto.ContactDto;
import jakarta.mail.MessagingException;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

public interface ContactService {
    ContactDto insertContact(Integer userId, ContactDto contactDto) throws IOException;

    Contact updateContact(Integer userId, Integer id, ContactDto updatedContact);
    ContactDto getContactById(Integer id, Integer userId);

    void deleteContact(Integer id);

    List<ContactDto> getAllContacts(Integer userId);

    List<ContactDto> searchContact(Integer userId,String search);

    public List<ContactDto> getContactBySort(Integer userId,String field,String direction);

    //List<ContactDto> getContactsByFilter(String filterField, String filterValue);

     List<ContactDto> getContactsWithPagination(Integer userId,int pageNumber, int pageSize) ;

    public BufferedImage generateQRCodeForContact(String phoneNumber);
    public void sendEmailWithQRCode(String mobile,String to, byte[] qrCodeImage) throws MessagingException;
    String generateContactsCsv(Integer userId);
    public void saveContactsFromCsv(Integer userId,MultipartFile file) throws IOException;



}
