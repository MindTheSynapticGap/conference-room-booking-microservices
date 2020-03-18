package com.bestgroup.userservice;

import com.bestgroup.userservice.entities.User;
import com.bestgroup.userservice.entities.UserBooking;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserBookingRepository bookingRepository;

    @Autowired
    public UserService(UserRepository userRepository, UserBookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }

    public List<User> retrieveAllUsers() {
        return userRepository.findAll();
    }

    public ResponseEntity<Object> newUser(User user) {
        User savedUser = userRepository.save(user);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedUser.getId()).toUri();

        return ResponseEntity.created(location).build();
    }

    public User retrieveUser(int id) {
        Optional<User> userWrappedInOptional = userRepository.findById(id);
        userWrappedInOptional.orElseThrow(() -> new UserNotFoundException("id: " + id));

        return userWrappedInOptional.get();
    }

    public boolean removeUser(int id) {
        Optional<User> optionalUser = userRepository.findById(id);

        if(!optionalUser.isPresent()) {
            return false;
        }

        userRepository.deleteById(id);
        return true;
    }

    public User updateUser(int id, User updatedUser) {
        userRepository.findById(id)
                .map(user -> {
                    user.setFirstName(updatedUser.getFirstName());
                    user.setLastName((updatedUser.getLastName()));
                    userRepository.save(user);
                    return user;
                });
        return updatedUser;
    }

    public List<UserBooking> retrieveUserBookings(@PathVariable int id) {
        Optional<User> optionalUser = userRepository.findById(id);

        if(!optionalUser.isPresent()) {
            throw new UserNotFoundException("id: " + id);
        }

        //TODO communicate with second microservice and retrieving booking information
        return optionalUser.get().getBookings();
    }

    public UserBooking addUserBooking(int userId, int bookingId ){
       Optional<User> optionalUser = userRepository.findById(userId);
       if(!optionalUser.isPresent()) {
           throw new UserNotFoundException("id: " + userId);
       }
       return bookingRepository.save(new UserBooking(bookingId,optionalUser.get()));
    }

    public List<UserBooking> getUserBookings(List<Integer> bookings) {
        return  bookingRepository.findAllById(bookings);
    }
}
