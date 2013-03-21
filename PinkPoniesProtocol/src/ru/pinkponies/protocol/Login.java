package ru.pinkponies.protocol;

import org.msgpack.annotation.Message;

@Message
public class Login extends Packet {
	public static long PACKET_ID = 0;
	
	public String board;
	public String bootloader;
	public String brand;
	public String cpuABI;
	public String cpuABI2;
	public String device;
	// TODO(alexknvl): add more
	
	public Login() {
		
	}
	
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

	@Override
	public String toString() {
		return "Login [board=" + board + ", bootloader=" + bootloader
				+ ", brand=" + brand + ", cpuABI=" + cpuABI + ", cpuABI2="
				+ cpuABI2 + ", device=" + device + "]";
	}

	public String getBoard() {
		return board;
	}

	public void setBoard(String board) {
		this.board = board;
	}

	public String getBootloader() {
		return bootloader;
	}

	public void setBootloader(String bootloader) {
		this.bootloader = bootloader;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getCpuABI() {
		return cpuABI;
	}

	public void setCpuABI(String cpuABI) {
		this.cpuABI = cpuABI;
	}

	public String getCpuABI2() {
		return cpuABI2;
	}

	public void setCpuABI2(String cpuABI2) {
		this.cpuABI2 = cpuABI2;
	}

	public String getDevice() {
		return device;
	}

	public void setDevice(String device) {
		this.device = device;
	}
}
