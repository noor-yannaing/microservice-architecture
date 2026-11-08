package com.wynh.microservices.inventory.repository;
import com.wynh.microservices.inventory.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ReservationRepository extends JpaRepository<Reservation, String> { }