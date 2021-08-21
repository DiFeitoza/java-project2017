package main;

import java.util.Date;
import java.util.InputMismatchException;
import java.util.Scanner;

import data.City;
import data.Flight;
import data.User;
import exceptions.PermissionDeniedException;
import exceptions.StatusUnavailableException;

/**
 * This is the main class of flight system project designed by group No.25
 * 
 * <p>The feature of this flight system:
 * <ul>
 * 		<li>containing all of the functions required</li>
 * 		<li>using xml file to save data</li>
 * 		<li>saving data to file and changing flight status automatically</li>
 * 		<li>able to search by city and search by date</li>
 * 		<li>add flight automatically</li>
 * 		<li>having high extensibility</li>
 * 		<li>using custom exception to deal with status and permission problem</li>
 * </ul>
 * 
 * <p>This project has a complete git repository, and you can trace the develop procedure if you like.
 *
 */
public class Main {
	static {
		printHelp(true);
		scanner = new Scanner(System.in);
		server = new MainServer();
	}
	
	static Scanner scanner;
	static MainServer server;
	static MainSearch search = new MainSearch(server, scanner);
	static ControllerFlight ctrFlight = new ControllerFlight(server, scanner);

	public static void menu(String string, String[] param) {
		switch(string) {
			case "help":
			case "h":
				printHelp(false);
				break;
			case "exit":
			case "e":
				break;
			case "list":
			case "l":
				list(param);
				break;
			case "login":
			case "log":
				login(param);
				break;
			case "register":
			case "r":
				register();
				break;
			case "search":
			case "s":
				if (param == null || param.length == 0) {
					search();
				} else {
					server.search(param[0]);
				}
				break;
			case "add":
				add(param);
				break;
			case "delete":
			case "d":
				delete(param);
				break;
			case "reserve":
			case "re":
				reserve(param);
				break;
			case "unsubscribe":
			case "unsub":
				unsubscribe();
				break;
			case "pay":
				pay();
				break;
			case "publish":
			case "pub":
				pub(param);
				break;
			case "change":
				change(param);
				break;
			default:
				if (!string.equals("")) {
					systemMessage("Unknown command: Type 'help' for more information.");
				}
				break;
		}
	}

	public static void main(String[] args) {
		// DONE(Dong) UI design
		String string = "";
		String[] param;
		while (!(string.equals("exit") || string.equals("e"))) {
			systemMessage(">");
			string = scanner.nextLine();
			string = string.replaceAll("\\s+", " ");
			string = string.replaceAll("^\\s+", "");
			string = string.replaceAll("\\s+$", "");
			if (string.contains(" ")) {
				String[] cmd = string.split(" ");
				string = cmd[0];
				param = new String[cmd.length - 1];
				for (int i = 1; i < cmd.length; i++) {
					param[i - 1] = cmd[i]; 
				}
			} else {
				param = null;
			}
			menu(string, param);
		}
		scanner.close();
		server.stop();
	}
	
	public static void systemMessage(String str) {
		System.out.println(str);
	}
	
	/*
	 * These are subUI or a wizard to lead User to do specific work
	 */
	private static void change(String[] param) {
		try {
			switch (param[0]) {
			case "flight":
				try {
					ctrFlight.changeFlight(Integer.valueOf(param[1]));
				} catch (NumberFormatException e) {
					System.out.printf("'%s' is not a flight ID\n", param[1]);
				} catch (PermissionDeniedException e) {
					systemMessage(e.getMessage());
				}
				break;
			case "city":
				try {
					City city = server.getCity(Integer.valueOf(param[1]));
					if (city != null) {
						city.setCityName(param[2]);
						systemMessage("Succeed!");
					} else {
						systemMessage("Failed: no such city");
					}
				} catch (NumberFormatException e) {
					System.out.printf("'%s' is not a city ID\n", param[0]);
				} catch (PermissionDeniedException e) {
					systemMessage(e.getMessage());
				}
				break;
			case "username":
				try {
					User user = server.getCurrentUser();
					user.setUserName(param[1]);
					systemMessage("Succeed!");
				} catch (PermissionDeniedException e) {
					systemMessage(e.getMessage());
				}
				break;
			case "password":
				try {
					User user = server.getCurrentUser();
					user.changePass(param[1]);
					systemMessage("Succeed!");
				} catch (PermissionDeniedException e) {
					systemMessage(e.getMessage());
				}
				break;
			default:
				systemMessage("Please input what to change");
				break;
			}
		} catch (NullPointerException | IndexOutOfBoundsException e) {
			systemMessage("Format error: type 'help' for more information");
		}		
	}

	private static void pay() {
		try {
			systemMessage("Please input your password: ");
			if (!server.checkPass(scanner.nextLine())) {
				throw new PermissionDeniedException("Password Error");
			}
			server.displayOrder();
			do {
				systemMessage("please select the index of order to pay(-1 to exit): ");
				try {
					int index = Integer.valueOf(scanner.nextLine());
					if (index == -1) {
						break;
					}
					systemMessage("Are you sure to pay this order?");
					if (scanner.nextLine().toLowerCase().equals("y")) {
						server.pay(index);
						systemMessage("Succeed!");						
					} else {
						systemMessage("Cancled");
					}
				} catch (NumberFormatException e) {
					systemMessage("Please input the right index");
				} catch (StatusUnavailableException e) {
					systemMessage("Pay failed: " + e.getMessage());
				} catch (IndexOutOfBoundsException e) {
					systemMessage("Error: no such order");
				}
			} while (true);
		} catch (PermissionDeniedException e) {
			systemMessage(e.getMessage());
		}
	}

	private static void unsubscribe() {
		try {
			systemMessage("Please Input your password: ");
			if (!server.checkPass(scanner.nextLine())) {
				throw new PermissionDeniedException("Password Error");
			}
			server.displayOrder();
			do {
				systemMessage("please select the index of order to cancel(-1 to exit): ");
				try {
					int index = Integer.valueOf(scanner.nextLine());
					if (index == -1) {
						break;
					}
					systemMessage("Are you sure to cancel this order?");
					if (scanner.nextLine().toLowerCase().equals("y")) {
						if (server.cancel(index)) {
							systemMessage("Reserving money has returned");
						}
						systemMessage("Succeed!");	
						
					} else {
						systemMessage("Operator canceld");
					}
				} catch (NumberFormatException e) {
					systemMessage("Please input the right index");
				} catch (StatusUnavailableException e) {
					systemMessage("Cancel failed: " + e.getMessage());
				} catch (IndexOutOfBoundsException e) {
					systemMessage("Error: no such order");
				}
			} while (true);
		} catch (PermissionDeniedException e) {
			systemMessage(e.getMessage());
		}
	}

	private static void list(String[] param) {
		if (param != null && param.length >= 1) {
			try {
				switch (param[0]) {
				case "city":
					if (param.length == 1) {
						server.displayCity();
					} else {
						for (int i = 1; i < param.length; i++) {
							server.displayCity(Integer.valueOf(param[i]));
						}
					}
					break;
				case "flight":
					if (param.length == 1) {
						server.displayFlight();						
					} else {
						for (int i = 1; i < param.length; i++) {
							try {
								server.displayFlight(Integer.valueOf(param[i]));
							} catch (NumberFormatException e) {
								System.out.printf("'%s' is not a flight\n", param[i]);
							}								
						}
					}						
					break;
				case "daemon":
					server.displayDaemon();						
					break;
				case "user":
					if (param.length == 1) {
						try {
							server.displayUser();
						} catch (PermissionDeniedException e) {
							systemMessage(e.getMessage());
						}							
					} else {
						for (int i = 1; i < param.length; i++) {
							try {
								if (!server.displayUser(Integer.valueOf(param[i]))) {
									System.out.printf("Can't find user with id '%s'\n", param[i]);
								}
							} catch (NumberFormatException e) {
								System.out.printf("'%s' is not a user id.\n", param[i]);
							}
						}
					}
					break;
				case "order":
					systemMessage("Please Input your password: ");
					if (!server.checkPass(scanner.nextLine())) {
						throw new PermissionDeniedException("Password Error");
					}
					server.displayOrder();
					break;
				default:
					systemMessage("Format error: you can only list city, user, flight or order");
					break;
				}
			} catch (PermissionDeniedException e) {
				systemMessage(e.getMessage());
			}								
		} else {
			systemMessage("Format error: please input what to list");
		}
	}

	private static void pub(String[] param) {
		if (param != null && param.length >= 1) {
			for (String p : param) {
				try {
					Flight flight = server.getFlight(Integer.valueOf(p));
					if (flight != null) {
						flight.publish();
					} else {
						System.out.printf("can't find flight with id '%s'\n", p);
					}
				} catch (NumberFormatException e) {
					System.out.printf("'%s' is not a flight ID\n", p);
				} catch (StatusUnavailableException | PermissionDeniedException e) {
					System.out.printf("cannot publish flight with id '%s': %s\n", p, e.getMessage());
				}
			} 
		} else {
			systemMessage("Format error: use 'publish [ID1] [ID2] ...' to publish flihgts");
		}
	}

	private static void login(String[] param) {
		if (param != null && param.length == 2) {
			if (server.login(param[0], param[1])) {
				systemMessage("Login succeed: ");
				if (server.isAdmin()) {
					systemMessage("You are administrator");
				} else {
					systemMessage("You are passenger");
				}
			} else {
				systemMessage("Login failed");
			}
		} else {
			systemMessage("Format error: please use 'login [username] [password]' to login");
		}
	}
	
	private static void reserve(String[] param) {
		if (param != null && param.length >= 1) {
			for (String para : param) {
				try {
					if (server.reserveFlight(Integer.parseInt(para))) {
						systemMessage("succeed in " + para);
					} else {
						systemMessage("no flight with id " + para);
					}
				} catch (NumberFormatException e) {
					System.out.printf("'%s' is not a flight id\n", para);
				} catch (PermissionDeniedException | StatusUnavailableException e) {
					System.out.printf("cannot reserve filght with id '%s': %s\n",para, e.getMessage());
				}
			}
		}
	}

	private static void add(String[] param) {
		// DONE(Dong) add
		if (param != null && param.length > 0) {
			switch (param[0]) {
			case "city":
				try {
					addCity(param[1]);
				} catch (IndexOutOfBoundsException e) {
					addCity(null);
				}
				break;
			case "flight":
				addFlight();
				break;
			case "admin":
				addAdmin();
				break;
			default:
				systemMessage("You can only add a city, flight or admin");
				break;
			}
		} else {
			systemMessage("Format error: please use 'add (city|flight|admin)' or 'add city [cityname]' to add");
		}
	}

	private static void delete(String[] param) {
		// DONE(Dong) delete
		if (param != null && param.length >= 2) {
			switch (param[0]) {
			case "flight":
				try {
					for (int i = 1; i < param.length; i++) {
						try {
							if (server.deleteFlight(Integer.parseInt(param[i]))) {
								System.out.printf("Successfully delete flight '%s'!\n", param[i]);
							} else {
								System.out.printf("Delete flight '%s' failed: no such flight\n", param[i]);
							}
						} catch (NumberFormatException e) {
							System.out.printf("'%s' is not a flight id!\n", param[i]);
						} catch (StatusUnavailableException e) {
							System.out.printf("Delete flight '%s' failed: %s\n", param[i], e.getMessage());
						}
					}
				} catch (PermissionDeniedException e) {
					systemMessage(e.getMessage());
				}
				break;
			case "daemon":
				try {
					for (int i = 1; i < param.length; i++) {
						try {
							if (server.deleteFlightDaemon(Integer.parseInt(param[i]))) {
								System.out.printf("Successfully delete flight daemon '%s'!\n", param[i]);
							} else {
								System.out.printf("Delete flight daemon '%s' failed: no such flight daemon\n", param[i]);
							}
						} catch (NumberFormatException e) {
							System.out.printf("'%s' is not a flight daemon id!\n", param[i]);
						} catch (StatusUnavailableException e) {
							System.out.printf("Delete flight daemon '%s' failed: %s\n", param[i], e.getMessage());
						}
					}
				} catch (PermissionDeniedException e) {
					systemMessage(e.getMessage());
				}
				break;
			case "city":
				try {
					for (int i = 1; i < param.length; i++) {
						try {
							if (server.deleteCity(Integer.parseInt(param[i]))) {
								System.out.printf("Successfully delete city '%s'!\n", param[i]);
							} else {
								System.out.printf("Delete city '%s' failed: no such city\n", param[i]);
							}
						} catch (NumberFormatException e) {
							System.out.printf("'%s' is not a city id!\n", param[i]);
						} catch (StatusUnavailableException e) {
							System.out.printf("Delete city '%s' failed: %s\n", param[i], e.getMessage());
						}
					}
				} catch (PermissionDeniedException e) {
					systemMessage(e.getMessage());
				}
				break;
			case "user":
				try {
					for (int i = 1; i < param.length; i++) {
						try {
							if (server.deleteUser(Integer.parseInt(param[i]))) {
								System.out.printf("Successfully delete user '%s'!\n", param[i]);
							} else {
								System.out.printf("Delete user '%s' failed: no such user\n", param[i]);
							}
						} catch (NumberFormatException e) {
							System.out.printf("'%s' is not a user id!\n", param[i]);
						}
					}
				} catch (PermissionDeniedException e) {
					systemMessage(e.getMessage());
				}
				break;
			default:
				systemMessage("You can only delete a city, flight or user");
				break;
			}
		} else {
			systemMessage("Format error: please use 'delete (city|flight|user) [ID1] [ID2] ...' to delete");
		}
	}

	private static void search() {
		search.search();
	}
	
	private static void addAdmin() {
		// DONE(Peng) addAdmin UI
		systemMessage("Please enter the Username : ");
		String userName = scanner.nextLine();
		systemMessage("Please enter the password : ");
		String password = scanner.nextLine();
		try {
			server.addAdmin(userName, password);
			systemMessage("Added successfully");
		} catch (PermissionDeniedException e) {
			systemMessage(e.getMessage());
		}
	}

	private static void register() {
		// DONE(Zhu) register UI
		systemMessage("Please input your username: ");
		String username;
		username = scanner.nextLine();
		systemMessage("Please input your identity card number: ");
		String idNumber;
		idNumber = scanner.nextLine();
		while(idNumber.length() > 18 || idNumber.length() < 18){
			systemMessage("Please input the correct identity card number, 18 characters: ");
		idNumber = scanner.nextLine();
		}
		String password,password2;
		systemMessage("Please input your password: ");
		password = scanner.nextLine();
		do {
			systemMessage("Please input your password again: ");
		password2 = scanner.nextLine();	
		} while (!(password.equals(password2)));		
		server.addPassenger(username, idNumber, password2);
		systemMessage("Succeed in creating your account!");
		
	}

	private static void addFlight() {
		// DONE(Peng) addFlight UI
		try {
			systemMessage("Available City: ");
			server.displayCity();
			systemMessage("flightName: ");
			String flightName = scanner.nextLine();
			systemMessage("Please enter the Starttime,formatted with : year-month-date-hr-min-sec: ");
			String[] startime = scanner.nextLine().split("-");
			int year = Integer.parseInt(startime[0]);
			int month = Integer.parseInt(startime[1]);
			int date = Integer.parseInt(startime[2]);
			int hr = Integer.parseInt(startime[3]);
			int min = Integer.parseInt(startime[4]);
			int sec = Integer.parseInt(startime[5]);
			Date startTime = Flight.calendar(year, month, date, hr, min, sec);
			systemMessage("Please enter the arrivetime,formatted with : year-month-date-hr-min-sec: ");
			String[] arrivetime=scanner.nextLine().split("-");
			int year1 = Integer.parseInt(arrivetime[0]);
			int month1 = Integer.parseInt(arrivetime[1]);
			int date1 = Integer.parseInt(arrivetime[2]);
			int hr1 = Integer.parseInt(arrivetime[3]);
			int min1 = Integer.parseInt(arrivetime[4]);
			int sec1 = Integer.parseInt(arrivetime[5]);
			Date arriveTime = Flight.calendar(year1, month1, date1, hr1, min1, sec1);
			if (arriveTime.before(startTime) || startTime.before(new Date())) {
				throw new NumberFormatException();
			}
			systemMessage("Period of the flight(day)(0 for no period): ");
			int period = scanner.nextInt();
			systemMessage("startCityID: ");
			int startCityID = scanner.nextInt();
			systemMessage("arriveCityID: ");
			int arriveCityID = scanner.nextInt();
			if (startCityID == arriveCityID) {
				throw new NumberFormatException();
			}
			systemMessage("price");
			int price = scanner.nextInt();
			systemMessage("seatCapacity: ");
			int seatCapacity=scanner.nextInt();
			systemMessage("distance(m): ");
			int distance = scanner.nextInt();
			scanner.nextLine();
			if (!server.createFlightDaemon(flightName, startTime, arriveTime, period, startCityID, arriveCityID, price, seatCapacity, distance)) {
				systemMessage("Error in cityID. retry?");
				if (scanner.nextLine().toLowerCase().equals("y")) {
					addFlight();
				}
			} else {
				systemMessage("Flight added successfully\n");
			}
		} catch (PermissionDeniedException e) {
			systemMessage(e.getMessage());
		} catch (IndexOutOfBoundsException | NumberFormatException | InputMismatchException e) {
			systemMessage("Input error. retry?");
			if (scanner.nextLine().toLowerCase().equals("y")) {
				addFlight();				
			}
		}
	}
	
	private static void addCity(String cityname) {
		// DONE(Peng) addCity UI
		if (cityname == null) {
			systemMessage("Please enter a valid city name: ");
			cityname = scanner.nextLine();
		}
		try {
			server.addCity(cityname);
			systemMessage("City added successfully");
		} catch (PermissionDeniedException e) {
			systemMessage(e.getMessage());
		}
	}

	private static void printHelp(boolean isMini) {
		// DONE(Dong) Help
		if (isMini) {
			systemMessage("Welcome to flight system!\n"
					+ "please use 'login [username] [password]' to login or use 'register' to register an account\n"
					+ "type 'help' for more information.\n");
		} else {
			systemMessage("Usage: command [param...]\n"
					+ "Available command: \n\n"
					+ "\tlogin|log [username] [password]\n"
					+ "\t\tlogin with username and password\n\n"
					+ "\tregister\n"
					+ "\t\tregister an account\n\n"
					+ "\tsearch|s [flightName]\n"
					+ "\t\tsearch flight with specific name\n\n"
					+ "\tsearch|s\n"
					+ "\t\tsearch flight with some filter\n\n"
					+ "\tlist|l (city|user|flight) [ID]\n"
					+ "\t\tlist all city, users(only for adminstrator), flight and in the server, or list the element with specific ID in detail\n\n"
					+ "\tlist|l order\n"
					+ "\t\tlist the order\n\n"
					+ "\tlist|l daemon\n"
					+ "\t\tlist flight daemon\n\n"
					+ "\tadd (city|admin|flight)\n"
					+ "\t\tadd a city administrator or flight daemon(only for adminstrator)\n\n"
					+ "\tdelete|d (city|user|flight|daemon) [ID1] [ID2] ....\n"
					+ "\t\tdelete city, user, flight or flight daemon with specific ID(only for adminstrator)\n"
					+ "\t\t\t**caution: delete flight daemon will also delete corresponding flight with status UNPUBLISHED\n\n"
					+ "\treserve|re [ID1] [ID2] ....\n"
					+ "\t\treserve flights with specific ID\n\n"
					+ "\tunsubscribe|unsub\n"
					+ "\t\tgoes into unsubscribe page\n\n"
					+ "\tpay\n"
					+ "\t\tgoes into pay page\n\n"
					+ "\tchange flight [ID]\n"
					+ "\t\tchange flight daemon information with specific ID(only for adminstrator)\n\n"
					+ "\tchange city [ID] [newName]\n"
					+ "\t\tchange city name with specific ID(only for adminstrator)\n\n"
					+ "\tchange (username|password) [newName|newPass]\n"
					+ "\t\tchange username or password\n\n"
					+ "\thelp|h\n"
					+ "\t\tprint this help information\n\n"
					+ "\texit|e\n"
					+ "\t\texit this program\n\n"
					);
		}
	}

}
