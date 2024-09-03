package com.monetize360.contact_application.web;

import com.monetize360.contact_application.domain.Contact;
import com.monetize360.contact_application.dto.ContactDto;
import com.monetize360.contact_application.serivce.ContactService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;


@RestController
@RequestMapping("/api/contacts")
public class ContactController {
    @Autowired
    private ContactService contactService;

    @PostMapping("/add")
    public ResponseEntity<ContactDto> addContact(@RequestParam("userId")Integer userId, @Valid @RequestBody ContactDto contactDto) throws IOException {

        ContactDto createdContact = contactService.insertContact(userId,contactDto);
        return new ResponseEntity<>(createdContact, HttpStatus.CREATED);

    }

    @PutMapping("/update")
    public ResponseEntity<Contact> updateContact(
            @RequestParam("userId") Integer userId,
            @RequestParam("id") Integer id,
            @RequestBody ContactDto contact) {

        Contact updatedContact = contactService.updateContact(userId,id, contact);
        return new ResponseEntity<>(updatedContact, HttpStatus.OK);

    }

    @GetMapping("/get")
    public ResponseEntity<ContactDto> getContactById(@RequestParam("id") Integer id,@RequestParam("userId") Integer userId) {

        ContactDto contact = contactService.getContactById(id,userId);
        if (contact != null) {
            return new ResponseEntity<>(contact, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteContactById(@PathVariable("id") Integer id) {

        contactService.deleteContact(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);

    }

    @GetMapping("/all")
    public ResponseEntity<List<ContactDto>> getAllContactsByUser(@RequestParam("userId") Integer userId) {
        System.out.println("hello2");
          List<ContactDto> contacts = contactService.getAllContacts(userId);
        return new ResponseEntity<>(contacts, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ContactDto>> searchContact(@RequestParam("userId") Integer userId,@RequestParam("search") String search) {

        List<ContactDto> contacts = contactService.searchContact(userId,search);
        if (!contacts.isEmpty()) {
            return new ResponseEntity<>(contacts, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

    }

    @GetMapping("sort")
    public ResponseEntity<List<ContactDto>> getAllSortedContacts(
            @RequestParam("userId") Integer userId,
            @RequestParam("field") String field,
            @RequestParam("direction") String direction) {

        List<ContactDto> contacts = contactService.getContactBySort(userId,field, direction);
        return new ResponseEntity<>(contacts, HttpStatus.OK);

    }


    @GetMapping("/paginated")
    public ResponseEntity<List<ContactDto>> getPaginatedContacts(
            @RequestParam("userId") Integer userId,
            @RequestParam int pageNumber,
            @RequestParam int pageSize) {
        List<ContactDto> contactsPage = contactService.getContactsWithPagination(userId, pageNumber, pageSize);
        return new ResponseEntity<>(contactsPage, HttpStatus.OK);
    }

    @GetMapping("/qr")
    public ResponseEntity<byte[]> getQRCodeByPhoneNumber(@RequestParam("mobile") String phoneNumber) throws IOException {

        BufferedImage image = contactService.generateQRCodeForContact(phoneNumber);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] imageBytes = baos.toByteArray();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);

        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }
    @GetMapping("/share")
    public ResponseEntity<String> shareContactByEmail(
            @RequestParam("email") String email,
            @RequestParam("mobile") String mobile) throws IOException, MessagingException {
            BufferedImage qrImage = contactService.generateQRCodeForContact(mobile);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            contactService.sendEmailWithQRCode(mobile,email, imageBytes);

            return new ResponseEntity<>("Email sent successfully", HttpStatus.OK);
    }

    @GetMapping("/import")
    public ResponseEntity<Resource> importContactsFromDb(@RequestParam("userId") Integer userId) {
        String csvData = contactService.generateContactsCsv(userId);
        Resource resource =  new ByteArrayResource((csvData.getBytes()));
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=contacts.csv");
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);

    }

    @PostMapping("/export")
    public ResponseEntity<String> exportContactsToDb(@RequestParam("userId")Integer userId,@RequestBody MultipartFile file) throws IOException {
        contactService.saveContactsFromCsv(userId,file);
        return new ResponseEntity<>("contacts uploaded successfully",HttpStatus.OK);
    }








}


