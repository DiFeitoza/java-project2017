package data;

import java.io.Serializable;
import java.util.Date;

import exceptions.StatusUnavailableException;

public class Order implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6646579220998951615L;
	private Passenger passenger;
	private Flight flight;
	private Date createDate;
	private OrderStatus status;
	
	public Order(Passenger passenger, Flight flight, int seat) throws StatusUnavailableException {
		this.passenger = passenger;
		this.flight = flight;
		createDate = new Date(); //now
		status = OrderStatus.UNPAID;
		flight.addPassenger(passenger, seat, true);
	}
	
	public Order(Passenger passenger, Flight flight) throws StatusUnavailableException {
		this.passenger = passenger;
		this.flight = flight;
		createDate = new Date(); //now
		status = OrderStatus.UNPAID;
		flight.addPassenger(passenger);
	}
	
	public boolean isCancle() {
		return status == OrderStatus.CANCLE ? true : false;
	}
	
	public boolean isPaid() {
		return status == OrderStatus.PAID ? true : false;
	}
	
	public boolean isUnpaid() {
		return status == OrderStatus.CANCLE ? true : false;
	}
	
	@Override
	public int hashCode() {
        return status.hashCode();
	}
	
	@Override
	public String toString() {
		return String.format(
				"----------------------------\n"
				+ "Passenger: %s\n"
				+ "Flight: %s\n"
				+ "Seat: %s\n"
				+ "Set Off Date: %s\n"
				+ "Create Date: %s\n"
				+ "Status: %s\n"
				+ "----------------------------", 
				passenger.userName,
				flight == null ? "deleted" : flight.getFlightName(),
				isCancle() ? "null" : String.valueOf(getSeat()), 
				flight == null ? "flight deleted" : flight.getStartTime().toString(),
				createDate.toString(),
				status.name());
	}
	
	public Passenger getPassager() {
		return passenger;
	}

	public Integer getSeat() {
		return flight == null ? null : flight.passagers().get(passenger);
	}

	public Flight getFlight() {
		return flight;
	}

	public Date getCreatDate() {
		return createDate;
	}
	
	protected void setCreatDate(Date creatDate) {
		this.createDate = creatDate;
	}

	public OrderStatus getStatus() {
		return status;
	}
	
	protected void setStatus(OrderStatus status) {
		this.status = status;
		if (isCancle()) {
			for (Passenger passenger : flight.getPassagers().keySet()) {
				if (passenger == this.passenger) {
					flight.getPassagers().remove(passenger);
				}
			}
		}
	}
	
	public void pay() throws StatusUnavailableException {
		if (isUnpaid()) {
			status = OrderStatus.PAID;
		} else {
			throw new StatusUnavailableException(status);
		}
	}
	
	public boolean cancle() throws StatusUnavailableException {
		if (!isCancle()) {
			boolean re;
			if (isPaid()) {
				re = true;
			} else {
				re = false;
			}
			flight.removePassenger(passenger);			
			status = OrderStatus.CANCLE;
			return re;
		} else {
			throw new StatusUnavailableException(status);
		}
	}
	
	public void printOrder() throws StatusUnavailableException {
		// DONE(Peng) printOrder
		if (isPaid()) {
			System.out.println("Passager :" + getPassager());
			System.out.println("Your Seat :" + getSeat());
			System.out.println("Your Flight :" + getFlight());
			System.out.println("Create date :" + getCreatDate());
			System.out.println("OrderStatus :" + getStatus());
	    	
	    }
		else {
			 throw new StatusUnavailableException();
		}
	}

	public void remove() {
		if (!isCancle()) {
			for (Passenger passenger : flight.getPassagers().keySet()) {
				if (passenger == this.passenger) {
					flight.getPassagers().remove(passenger);
				}
			}
			passenger.orderList.remove(this);
			if (flight.getPassagers().size() < flight.getSeatCapacity()) {
				flight.flightStatus = FlightStatus.AVAILABLE;
			} 
		}
	}

}
