package me.DevTec.TheAPI.Utils.NMS;

import org.bukkit.Material;

public class ParticleData {
	private final Material material;
	private final byte data;
	private final int[] packetData;

	public ParticleData(Material material, byte data) {
		this.material = material;
		this.data = data;
		this.packetData = new int[] { material.getId(), data };
	}

	public Material getMaterial() {
		return material;
	}

	public byte getData() {
		return data;
	}

	public int[] getPacketData() {
		return packetData;
	}

	public String getPacketDataString() {
		return "_" + packetData[0] + "_" + packetData[1];
	}
}
