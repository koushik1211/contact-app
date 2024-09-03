package com.monetize360.contact_application.dao;

import com.monetize360.contact_application.domain.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ContactDao extends JpaRepository<Contact, Integer> {

    @Query("SELECT c FROM Contact c WHERE c.user.id=?1 and (c.firstname LIKE ?2 OR c.lastname LIKE ?2 OR c.email LIKE ?2 or c.mobile LIKE ?2) AND c.deleted = false")
    public List<Contact> searchContact(Integer userId ,String search);

    public List<Contact> findByDeletedFalse();

    Contact findByMobile(String mobile);
    Contact findByIdAndUserId(Integer id, Integer userId);
   List<Contact> findByUserIdAndDeletedFalse(Integer userId);
    List<Contact> findByUserId(Integer id, Sort sort);
    Page<Contact> findByUserId(Integer userId, Pageable page);


}
