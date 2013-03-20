package ru.pinkponies.protocol;

public final class Login {
	public static long PACKET_ID = 0;
	
	public String board;
	public String bootloader;
	public String brand;
	public String cpuABI;
	public String cpuABI2;
	public String device;
	// TODO(alexknvl): add more
	
	public Login(String board, String bootloader, String brand, String cpuABI,
			String cpuABI2, String device) {
		super();
		this.board = board;
		this.bootloader = bootloader;
		this.brand = brand;
		this.cpuABI = cpuABI;
		this.cpuABI2 = cpuABI2;
		this.device = device;
	}
}
