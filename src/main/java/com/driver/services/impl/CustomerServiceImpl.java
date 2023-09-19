package com.driver.services.impl;

import com.driver.model.*;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function
		Optional<Customer> customerOptional = customerRepository2.findById(customerId);
		if (customerOptional.isEmpty()) {
			return;
		}

		Customer customer = customerOptional.get();
		customerRepository2.delete(customer);
	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query


//		List<Driver> driverList = new ArrayList<>();
//		driverList = driverRepository2.findAll();
//
//		Driver toBeBooked = null;
//		int lowestDriverId = Integer.MAX_VALUE;
//
//		for (Driver driver: driverList) {
//			if (driver.getCab() != null && driver.getCab().getAvailable()) {
//				if (toBeBooked == null || lowestDriverId > driver.getDriverId()) {
//					toBeBooked = driver;
//					lowestDriverId = Math.min(lowestDriverId,driver.getDriverId());
//				}
//			}
//		}
//
////		for(Driver driver : driverList){
////			if(driver.getCab().getAvailable()){
////				toBeBooked = driver;
////				break;
////			}
////		}
//
//		if (toBeBooked == null) {
//			throw new Exception("No cab available!");
//		}
//
//		Optional<Customer> customerOptional = customerRepository2.findById(customerId);
//		if (customerOptional.isEmpty()) {
//			throw new Exception("Customer is not Present");
//		}
//		Customer customer = customerOptional.get();
//
//		TripBooking tripBooking = new TripBooking();
//		tripBooking.setStatus(TripStatus.CONFIRMED);
//		tripBooking.setCustomer(customer);
//		tripBooking.setDistanceInKm(distanceInKm);
//		tripBooking.setDriver(toBeBooked);
//		tripBooking.setFromLocation(fromLocation);
//		tripBooking.setToLocation(toLocation);
//
//		int cost = toBeBooked.getCab().getPerKmRate() * distanceInKm;
//		tripBooking.setBill(cost);
//
//		toBeBooked.getCab().setAvailable(false);
//
//		TripBooking savedTripBooking = tripBookingRepository2.save(tripBooking);
//		toBeBooked.getTripBookingList().add(savedTripBooking);
//		customer.getTripBookingList().add(savedTripBooking);
//		driverRepository2.save(toBeBooked);
//		customerRepository2.save(customer);
//		return savedTripBooking;

		List<Driver> driverList=driverRepository2.findAll();
		Driver driver=null;

		for(Driver d:driverList){
			if(d.getCab().getAvailable() ){
				if( driver==null || driver.getDriverId()>d.getDriverId() )driver=d;
			}
		}
		if(driver==null) throw new Exception("No cab available!");

		TripBooking tripBooking=new TripBooking();

		tripBooking.setFromLocation(fromLocation);
		tripBooking.setToLocation(toLocation);
		tripBooking.setDistanceInKm(distanceInKm);
		tripBooking.setStatus(TripStatus.CONFIRMED);

		//bill to be set when booking or when completed?

		tripBooking.setDriver(driver);
		driver.getTripBookingList().add(tripBooking);

		driver.getCab().setAvailable(false);

		Customer customer=customerRepository2.findById(customerId).get();

		customer.getTripBookingList().add(tripBooking);
		tripBooking.setCustomer(customer);

		//will it cause duplicate when saving both customer and driver as tripbooking is chile of both
		driverRepository2.save(driver);
		customerRepository2.save(customer);

		tripBookingRepository2.save(tripBooking);
		return tripBooking;

	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		Optional<TripBooking> optionalTripBooking = tripBookingRepository2.findById(tripId);

		if (optionalTripBooking.isEmpty()) {
			return;
		}

		TripBooking tripBooking = optionalTripBooking.get();
		tripBooking.setStatus(TripStatus.CANCELED);
		tripBooking.getDriver().getCab().setAvailable(true);
		tripBooking.setBill(0);

		tripBookingRepository2.save(tripBooking);

	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
//		Optional<TripBooking> optionalTripBooking = tripBookingRepository2.findById(tripId);
//		if (optionalTripBooking.isEmpty()) return;
//
//		TripBooking tripBooking = optionalTripBooking.get();
//		tripBooking.setStatus(TripStatus.COMPLETED);
//		tripBooking.getDriver().getCab().setAvailable(true);
//
//		tripBookingRepository2.save(tripBooking);

		TripBooking tripBooking=tripBookingRepository2.findById(tripId).get();
		tripBooking.setStatus(TripStatus.COMPLETED);


		Driver driver=tripBooking.getDriver();
		Cab cab=driver.getCab();
		cab.setAvailable(true);

		int bill=tripBooking.getDistanceInKm()*cab.getPerKmRate();
		tripBooking.setBill(bill);

		driverRepository2.save(driver);

		tripBookingRepository2.save(tripBooking);
	}
}
