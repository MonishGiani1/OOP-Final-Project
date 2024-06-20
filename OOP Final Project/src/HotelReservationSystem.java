import java.time.LocalDate;
import java.util.*;

class RoomNotAvailableException extends Exception {
    public RoomNotAvailableException(String message) {
        super(message);
    }
}

interface Billing {
    double calculateBill();
}


class Guest {
    private String name;
    private String phoneNumber;

    public Guest(String name, String phoneNumber) {
        this.name = name;
        this.phoneNumber = phoneNumber;
    }


    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}

enum RoomType {
    STANDARD, DELUXE, SUITE
}

class Room {
    private int roomNumber;
    private RoomType roomType;
    private boolean available;

    public Room(int roomNumber, RoomType roomType) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.available = true;
    }

    // Getters
    public int getRoomNumber() {
        return roomNumber;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public boolean isAvailable() {
        return available;
    }

    public void bookRoom() {
        this.available = false;
    }

    public void freeRoom() {
        this.available = true;
    }
}

class Reservation implements Billing {
    private static final double STANDARD_ROOM_RATE = 100.0;
    private static final double DELUXE_ROOM_RATE = 150.0;
    private static final double SUITE_ROOM_RATE = 250.0;

    private Guest guest;
    private Room room;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    public Reservation(Guest guest, Room room, LocalDate checkInDate, LocalDate checkOutDate) {
        this.guest = guest;
        this.room = room;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        room.bookRoom(); // Book the room upon reservation
    }

    @Override
    public double calculateBill() {
        // Simplified bill calculation based on room type and duration
        long nights = checkInDate.until(checkOutDate).getDays();
        double roomCharge = 0.0;
        switch (room.getRoomType()) {
            case STANDARD:
                roomCharge = nights * STANDARD_ROOM_RATE;
                break;
            case DELUXE:
                roomCharge = nights * DELUXE_ROOM_RATE;
                break;
            case SUITE:
                roomCharge = nights * SUITE_ROOM_RATE;
                break;
        }
        return roomCharge;
    }

    // Getters
    public Guest getGuest() {
        return guest;
    }

    public Room getRoom() {
        return room;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }
}

class Hotel {
    private List<Room> rooms;
    private List<Reservation> reservations;

    public Hotel() {
        this.rooms = new ArrayList<>();
        this.reservations = new ArrayList<>();
    }

    // Method to add a room to the hotel
    public void addRoom(Room room) {
        rooms.add(room);
    }

    public boolean isRoomAvailable(RoomType roomType, LocalDate checkInDate, LocalDate checkOutDate) {
        for (Room room : rooms) {
            if (room.getRoomType() == roomType && room.isAvailable()) {
                // Check if room is available for the entire date range
                boolean isAvailable = true;
                for (LocalDate date = checkInDate; date.isBefore(checkOutDate); date = date.plusDays(1)) {
                    // Check availability for each day in the range
                    if (!room.isAvailable()) {
                        isAvailable = false;
                        break;
                    }
                }
                if (isAvailable) {
                    return true;
                }
            }
        }
        return false;
    }

    public Reservation makeReservation(Guest guest, RoomType roomType, LocalDate checkInDate, LocalDate checkOutDate)
            throws RoomNotAvailableException {
        if (!isRoomAvailable(roomType, checkInDate, checkOutDate)) {
            throw new RoomNotAvailableException("Room of type " + roomType + " not available for specified dates.");
        }

        // Find an available room of the specified type
        Room availableRoom = null;
        for (Room room : rooms) {
            if (room.getRoomType() == roomType && room.isAvailable()) {
                availableRoom = room;
                break;
            }
        }

        Reservation reservation = new Reservation(guest, availableRoom, checkInDate, checkOutDate);
        reservations.add(reservation);
        return reservation;
    }

    public void modifyReservation(Reservation reservation, LocalDate newCheckInDate, LocalDate newCheckOutDate)
            throws RoomNotAvailableException {
        RoomType roomType = reservation.getRoom().getRoomType();
        if (!isRoomAvailable(roomType, newCheckInDate, newCheckOutDate)) {
            throw new RoomNotAvailableException("Room of type " + roomType + " not available for specified dates.");
        }

        reservation.getRoom().freeRoom(); // Free up the current room
        reservation = new Reservation(reservation.getGuest(), reservation.getRoom(), newCheckInDate, newCheckOutDate);
        reservations.add(reservation);
    }

    public void cancelReservation(Reservation reservation) {
        reservation.getRoom().freeRoom(); // Free up the room upon cancellation
        reservations.remove(reservation);
    }

    public void checkOutGuest(Guest guest) {
        for (Reservation reservation : reservations) {
            if (reservation.getGuest().equals(guest)) {
                reservation.getRoom().freeRoom(); // Free up the room upon checkout
                reservations.remove(reservation);
                break;
            }
        }
    }


    public void printAllReservations() {
        System.out.println("All Reservations:");
        for (Reservation reservation : reservations) {
            System.out.println("Guest: " + reservation.getGuest().getName() +
                    ", Room Number: " + reservation.getRoom().getRoomNumber() +
                    ", Check-in Date: " + reservation.getCheckInDate() +
                    ", Check-out Date: " + reservation.getCheckOutDate() +
                    ", Total Bill: $" + reservation.calculateBill());
        }
    }

    public List<Reservation> getReservations() {
        return reservations;
    }
}

public class HotelReservationSystem {
    public static void main(String[] args) {
        Hotel hotel = new Hotel();
        Scanner scanner = new Scanner(System.in);

        // Adding rooms to the hotel
        hotel.addRoom(new Room(101, RoomType.STANDARD));
        hotel.addRoom(new Room(102, RoomType.STANDARD));
        hotel.addRoom(new Room(201, RoomType.DELUXE));
        hotel.addRoom(new Room(202, RoomType.DELUXE));
        hotel.addRoom(new Room(301, RoomType.SUITE));

        try {
            while (true) {
                System.out.println("\nWelcome to Hotel Reservation System");
                System.out.println("1. Make a Reservation");
                System.out.println("2. Modify a Reservation");
                System.out.println("3. Cancel a Reservation");
                System.out.println("4. Check-out a Guest");
                System.out.println("5. Print All Reservations");
                System.out.println("6. Exit");
                System.out.print("Enter your choice (1-6): ");
                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1:
                        System.out.print("Enter guest name: ");
                        String guestName = scanner.nextLine();
                        System.out.print("Enter guest phone number: ");
                        String phoneNumber = scanner.nextLine();
                        Guest guest = new Guest(guestName, phoneNumber);

                        System.out.println("Available Room Types:");
                        System.out.println("1. STANDARD");
                        System.out.println("2. DELUXE");
                        System.out.println("3. SUITE");
                        System.out.print("Select room type (1-3): ");
                        int roomTypeChoice = scanner.nextInt();
                        scanner.nextLine(); // Consume newline

                        RoomType roomType = null;
                        switch (roomTypeChoice) {
                            case 1:
                                roomType = RoomType.STANDARD;
                                break;
                            case 2:
                                roomType = RoomType.DELUXE;
                                break;
                            case 3:
                                roomType = RoomType.SUITE;
                                break;
                            default:
                                System.out.println("Invalid room type choice. Reservation canceled.");
                                continue;
                        }

                        System.out.print("Enter check-in date (YYYY-MM-DD): ");
                        String checkInDateString = scanner.nextLine();
                        LocalDate checkInDate = LocalDate.parse(checkInDateString);

                        System.out.print("Enter check-out date (YYYY-MM-DD): ");
                        String checkOutDateString = scanner.nextLine();
                        LocalDate checkOutDate = LocalDate.parse(checkOutDateString);

                        try {
                            Reservation reservation = hotel.makeReservation(guest, roomType, checkInDate, checkOutDate);
                            System.out.println("Reservation successfully made.");
                            System.out.println("Room number: " + reservation.getRoom().getRoomNumber());
                            System.out.println("Total Bill: $" + reservation.calculateBill());
                        } catch (RoomNotAvailableException e) {
                            System.out.println("Room not available for the specified dates. Reservation canceled.");
                        }
                        break;

                    case 2:
                        System.out.print("Enter guest name for modification: ");
                        String guestNameModify = scanner.nextLine();

                        System.out.print("Enter new check-in date (YYYY-MM-DD): ");
                        String newCheckInDateString = scanner.nextLine();
                        LocalDate newCheckInDate = LocalDate.parse(newCheckInDateString);

                        System.out.print("Enter new check-out date (YYYY-MM-DD): ");
                        String newCheckOutDateString = scanner.nextLine();
                        LocalDate newCheckOutDate = LocalDate.parse(newCheckOutDateString);

                        // Find the reservation to modify
                        Reservation reservationToModify = null;
                        for (Reservation reservation : hotel.getReservations()) {
                            if (reservation.getGuest().getName().equalsIgnoreCase(guestNameModify)) {
                                reservationToModify = reservation;
                                break;
                            }
                        }

                        if (reservationToModify != null) {
                            try {
                                hotel.modifyReservation(reservationToModify, newCheckInDate, newCheckOutDate);
                                System.out.println("Reservation successfully modified.");
                                System.out.println("New check-in date: " + reservationToModify.getCheckInDate());
                                System.out.println("New check-out date: " + reservationToModify.getCheckOutDate());
                            } catch (RoomNotAvailableException e) {
                                System.out.println("Room not available for the specified dates. Modification canceled.");
                            }
                        } else {
                            System.out.println("Guest not found in reservations. Modification canceled.");
                        }
                        break;

                    case 3:
                        System.out.print("Enter guest name for cancellation: ");
                        String guestNameCancel = scanner.nextLine();

                        Reservation reservationToCancel = null;
                        for (Reservation reservation : hotel.getReservations()) {
                            if (reservation.getGuest().getName().equalsIgnoreCase(guestNameCancel)) {
                                reservationToCancel = reservation;
                                break;
                            }
                        }

                        if (reservationToCancel != null) {
                            hotel.cancelReservation(reservationToCancel);
                            System.out.println("Reservation successfully canceled.");
                        } else {
                            System.out.println("Guest not found in reservations. Cancellation canceled.");
                        }
                        break;

                    case 4:
                        System.out.print("Enter guest name for check-out: ");
                        String guestNameCheckOut = scanner.nextLine();

                        // Find the guest to check out
                        Guest guestToCheckOut = null;
                        for (Reservation reservation : hotel.getReservations()) {
                            if (reservation.getGuest().getName().equalsIgnoreCase(guestNameCheckOut)) {
                                guestToCheckOut = reservation.getGuest();
                                break;
                            }
                        }

                        if (guestToCheckOut != null) {
                            hotel.checkOutGuest(guestToCheckOut);
                            System.out.println("Guest successfully checked out.");
                        } else {
                            System.out.println("Guest not found in reservations. Check-out canceled.");
                        }
                        break;

                    case 5:
                        hotel.printAllReservations();
                        break;

                    case 6:
                        System.out.println("Exiting Hotel Reservation System. Thank you!");
                        scanner.close();
                        System.exit(0);

                    default:
                        System.out.println("Invalid choice. Please enter a number from 1 to 6.");
                }
            }

        } catch (Exception e) {
            System.out.println("Error occurred: " + e.getMessage());
        }
    }
}

