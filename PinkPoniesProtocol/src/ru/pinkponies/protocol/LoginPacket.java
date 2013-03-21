package ru.pinkponies.protocol;

import org.msgpack.annotation.Message;

@Message
public class LoginPacket extends Packet {
	public String board;
	public String bootloader;
	public String brand;
	public String cpuABI;
	public String cpuABI2;
	public String device;
	// TODO(alexknvl): add more
	
	public LoginPacket() {
		super();
	}
	
	public LoginPacket(String board, String bootloader, String brand, String cpuABI,
			String cpuABI2, String device) {
		super();
		this.board = board;
		this.bootloader = bootloader;
		this.brand = brand;
		this.cpuABI = cpuABI;
		this.cpuABI2 = cpuABI2;
		this.device = device;
	}

	@Override
	public String toString() {
		return "Login [board=" + board + ", bootloader=" + bootloader
				+ ", brand=" + brand + ", cpuABI=" + cpuABI + ", cpuABI2="
				+ cpuABI2 + ", device=" + device + "]";
	}
}
