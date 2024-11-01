/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.maya2002yagan.supermarket_management;

import org.hibernate.Session;
import org.hibernate.query.Query;

/**
 *
 * @author maya2
 */
public class RoleDAO {
    public Role getRoleByName(String role){
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            Query<Role> query = session.createQuery("FROM Role WHERE position = :position", Role.class);
            query.setParameter("position", role);
            return query.uniqueResult();
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
